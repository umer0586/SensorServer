package github.umer0586.sensorserver;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.util.Log;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import github.umer0586.util.JsonUtil;
import github.umer0586.util.SensorUtil;


public class SensorWebSocketServer extends WebSocketServer implements SensorEventListener {

    private static final String TAG = SensorWebSocketServer.class.getName();

    private int sensorDelay = SensorManager.SENSOR_DELAY_FASTEST;
    private static final String CONNECTION_PATH = "/sensor/connect";

    private SensorManager sensorManager;
    private SensorUtil sensorUtil;

    //To track the list of sensors registered
    private List<Sensor> registeredSensors;

    //Callbacks
    private OnServerStartListener onServerStartListener;
    private OnServerStopppedListener onServerStopppedListener;
    private ConnectionInfoListener connectionInfoListener;
    private OnServerErrorListener onServerErrorListener;

    private boolean serverStartUpFailed = false;

    //websocket close codes ranging 4000 - 4999 are for application's custom messages
    private static final int CLOSE_CODE_SENSOR_NOT_FOUND = 4001;
    private static final int CLOSE_CODE_UNSUPPORTED_REQUEST = 4002;
    private static final int CLOSE_CODE_TYPE_PARAMETER_MISSING = 4003;
    private static final int CLOSE_CODE_SERVER_STOPPED = 4004;
    private static final int CLOSE_CODE_CONNECTION_CLOSED_BY_APP_USER = 4005;



    public SensorWebSocketServer(Context context, InetSocketAddress address)
    {
        super(address);
        sensorManager = (SensorManager) context.getSystemService(context.SENSOR_SERVICE);
        sensorUtil = SensorUtil.getInstance(context);
        registeredSensors = new ArrayList<>();

    }

    // ws://host:port/sensor/connect?type=<sensorType>
    @Override
    public void onOpen(WebSocket clientWebsocket, ClientHandshake handshake)
    {
        Log.i(TAG,"New connection established " + clientWebsocket.getRemoteSocketAddress() + " Resource descriptor : " + clientWebsocket.getResourceDescriptor());

        //Parse uri so that we can read parameters from query
        Uri uri = Uri.parse(clientWebsocket.getResourceDescriptor());

        if(!uri.getPath().equalsIgnoreCase(CONNECTION_PATH))
        {
            clientWebsocket.close(CLOSE_CODE_UNSUPPORTED_REQUEST,"Unsupported request");
            return;
        }

        Log.i(TAG, "param type " + uri.getQueryParameter("type"));

        String paramType = uri.getQueryParameter("type");

        //if type param doesn't exit in the query
        if(paramType == null)
        {
            clientWebsocket.close(CLOSE_CODE_TYPE_PARAMETER_MISSING,"<type> param required");
            //do not proceed further
            return;
        }

        paramType = paramType.toLowerCase();

        // sensorUtil.getSensorFromStringType(String) returns null when invalid sensor type is passed or when sensor type is not supported by the device
        Sensor requestedSensor = sensorUtil.getSensorFromStringType( paramType );

        //If client has requested invalid or unsupported sensor
        // then close client Websocket connection and return ( i-e do not proceed further)
        if(requestedSensor == null)
        {
            clientWebsocket.close(CLOSE_CODE_SENSOR_NOT_FOUND,"Sensor of type " + uri.getQueryParameter("type") + " not found");
            return;
        }

        //At this point paramType is valid (e.g android.sensor.light etc..)

        /*
            if this WebSocket Server has already registered itself for some type of sensor (e.g android.sensor.light)
            then we don't have to registered this Server for the same sensor again
        */

        if(registeredSensors.contains(requestedSensor))
        {
            Log.i(TAG, "Sensor " + paramType + " already registered, skipping registration");

            //for new requesting client attach a tag of requested sensor type with client
            clientWebsocket.setAttachment(requestedSensor);

            //Update registry
             registeredSensors.add(requestedSensor);

            Log.i(TAG, "Connections : " + getConnectionCount());

            notifyConnectionInfoList( );
            // no need to call sensorManager.registerListener();
            return;
        }


         if(requestedSensor != null)
         {
            //Register requested sensor
            sensorManager.registerListener(this,requestedSensor, getSensorDelay());

            // Update registry
             registeredSensors.add(requestedSensor);

             /*
              Attach info with newly connected client
              so that this Servers knows which client has requested which type of sensor
              */
             clientWebsocket.setAttachment(requestedSensor);
             Log.i(TAG, "Connections : " + getConnectionCount());
         }


        notifyConnectionInfoList();

    }

    @Override
    public void onClose(WebSocket clientWebsocket, int code, String reason, boolean remote)
    {
        Log.i(TAG,"Connection closed " + clientWebsocket.getRemoteSocketAddress() + " with exit code " + code + " additional info: " + reason);

        // Get sensor type of recently closed client
        Sensor sensor = clientWebsocket.getAttachment();

        if(sensor == null)
            return;

        // When client has closed connection, how many clients receiving same sensor data from this server
        long sensorUseCount = getSensorConnectionCount(sensor);


        Log.i(TAG, "Sensor : " + sensor.getStringType() + " Usage : " + sensorUseCount );

        /*
            Suppose we have 3 clients each receiving light sensor data \
            if we unregister this server for light sensor when only one client is disconnected \
            then 2 other connected client won't recieve light sensor data anymore

        */

        //  Unregister sensor if and only if one client is using it
        if(sensorUseCount == 1)
             sensorManager.unregisterListener(this, sensor);

        registeredSensors.remove(sensor);

        Log.i(TAG, "Connections : " + getConnectionCount());
        notifyConnectionInfoList();
    }

    @Override
    public void onMessage(WebSocket conn, String message)
    {

    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer message)
    {

    }
    // following doc taken from original source
    /**
     * Called when errors occurs. If an error causes the websocket connection to fail {@link #onClose(WebSocket, int, String, boolean)} will be called additionally.<br>
     * This method will be called primarily because of IO or protocol errors.<br>
     * If the given exception is an RuntimeException that probably means that you encountered a bug.<br>
     *
     * @param conn Can be null if there error does not belong to one specific websocket. For example if the servers port could not be bound.
     * @param ex The exception causing this error
     **/
    @Override
    public void onError(WebSocket conn, Exception ex)
    {
        // error occurred on websocket conn (we don't notify anything to the user about this for now)
        if(conn != null)
            Log.e(TAG,"an error occurred on connection " + conn.getRemoteSocketAddress());

        // if conn is null than we have error related to server
        if(conn == null)
        {
            /*
                seeing the implementation of onError(conn,ex), this method
                always invokes stop() whether server is running or not,
                So onError() would invoke stop() when some exception like BindException occurs (because of port already in use)
            */
            if (onServerErrorListener != null)
                onServerErrorListener.onError(ex); // listener must filter exception by itself

            serverStartUpFailed = true; // we will use this in stop() method to check if there was an exception during server startup
        }

            ex.printStackTrace();

    }

    @Override
    public void onStart()
    {

        if(this.onServerStartListener != null)
            this.onServerStartListener.onServerStarted();

        Log.i(TAG,"server started successfully " + this.getAddress());
    }

   @Override
    public void stop() throws IOException, InterruptedException
   {
        closeAllConnections();
        super.stop();
        Log.d(TAG, "stop() called");

            if( onServerStopppedListener != null && !serverStartUpFailed )
                this.onServerStopppedListener.onServerStopped();




    }

    /*
        1. calling webSocketServerObj.run() starts server.
        2. WebSocketServer do not run on a separate thread by default,
           so we need to make sure that we run server on separate thread
     */
    @Override
    public void run()
    {
        new Thread (()->super.run()).start();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent)
    {
        // Loop through each connected client
        for( WebSocket webSocket : getConnections())
        {

            // Send data as per sensor type requested by client
            if( ((Sensor) webSocket.getAttachment()) !=null )
                if( ((Sensor) webSocket.getAttachment()).getType() == sensorEvent.sensor.getType() && !webSocket.isClosing())
                    webSocket.send(JsonUtil.toJSON(sensorEvent.values));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i)
    {

    }

    private int getSensorConnectionCount(Sensor sensor)
    {
        //java.util.Stream requires android API 24

        int usageCount = 0;
        for(Sensor registeredSensor : registeredSensors)
            if(registeredSensor.getType() == sensor.getType())
                usageCount++;

            return usageCount;

    }

    private List<InetSocketAddress> getClientsAddressBySensor(Sensor sensor)
    {
        List<InetSocketAddress> clientSocketAddresses = new ArrayList<>();

        for(WebSocket webSocket : getConnections())
        {
            if( ((Sensor) webSocket.getAttachment()) !=null )
                if( ((Sensor) webSocket.getAttachment()).getType() == sensor.getType())
                    clientSocketAddresses.add(webSocket.getRemoteSocketAddress());

        }

        return clientSocketAddresses;
    }

    public void setSensorDelay(int sensorDelay)
    {
        switch(sensorDelay)
        {
            case   SensorManager.SENSOR_DELAY_FASTEST:
            case   SensorManager.SENSOR_DELAY_NORMAL:
            case   SensorManager.SENSOR_DELAY_UI:
            case   SensorManager.SENSOR_DELAY_GAME:
                this.sensorDelay = sensorDelay;
                break;
            default:
                throw new IllegalArgumentException("Sensor Delay type Unsupported");
        }
    }

    public int getSensorDelay()
    {
        return sensorDelay;
    }

    /*
        Each time client connects or disconnects,
        we create a new ArrayList of ConnectionInfo and notify it to listener
     */
    private void notifyConnectionInfoList()
    {
        // registeredSensors (List) may contain duplicate entry (too keep track of sensor usage count) ,
        // converting List to Set removes those duplicate entries
        Set<Sensor> sensorsSet = new HashSet<Sensor>( new ArrayList<>(registeredSensors));

        ArrayList<ConnectionInfo> connectionInfos = new ArrayList<>();

        for(Sensor sensor : sensorsSet)
            connectionInfos.add( new ConnectionInfo(sensor, getClientsAddressBySensor(sensor) ) );

        if(connectionInfoListener != null)
            this.connectionInfoListener.onConnectionInfo(connectionInfos);
    }

    public int getConnectionCount()
    {
        return getConnections().size();
    }

    public void closeAllConnections()
    {
        for(WebSocket webSocket : getConnections())
            webSocket.close(CLOSE_CODE_SERVER_STOPPED,"Server stopped");
    }

    public void closeConnectionBySensor(Sensor sensor)
    {
        for(WebSocket webSocket : getConnections())
        {
            if( ((Sensor) webSocket.getAttachment()) !=null )
            {
                if( ((Sensor) webSocket.getAttachment()).getType() == sensor.getType() )
                    webSocket.close(CLOSE_CODE_CONNECTION_CLOSED_BY_APP_USER,"connection closed by app user");
            }
        }
    }


    public void setOnServerStartListener(OnServerStartListener onServerStartListener)
    {
        this.onServerStartListener = onServerStartListener;
    }

    public void setOnServerStopped(OnServerStopppedListener onServerStopppedListener)
    {
        this.onServerStopppedListener = onServerStopppedListener;
    }

    public void setConnectionInfoListener(ConnectionInfoListener connectionInfoListener)
    {
        this.connectionInfoListener = connectionInfoListener;
    }

    public void setOnServerError(OnServerErrorListener onServerErrorListener)
    {
        this.onServerErrorListener = onServerErrorListener;
    }
}
