package github.umer0586.sensorserver;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import github.umer0586.util.JsonUtil;
import github.umer0586.util.SensorUtil;


public class SensorWebSocketServer extends WebSocketServer implements SensorEventListener {

    private static final String TAG = SensorWebSocketServer.class.getName();

    private int samplingRate = 200000;//default value normal rate
    private static final String CONNECTION_PATH_SINGLE_SENSOR = "/sensor/connect";
    private static final String CONNECTION_PATH_MULTIPLE_SENSORS = "/sensors/connect";
    private static final HashMap<String, Object> response = new HashMap<>();

    private HandlerThread handlerThread;
    private Handler handler;

    private SensorManager sensorManager;
    private SensorUtil sensorUtil;

    //To track the list of sensors registered
    private List<Sensor> registeredSensors;

    //Callbacks
    private ServerStartListener serverStartListener;
    private ServerStopListener serverStopListener;
    private ServerErrorListener serverErrorListener;
    private ConnectionsChangeListener connectionsChangeListener;


    private boolean serverStartUpFailed = false;
    private boolean isRunning = false;

    //websocket close codes ranging 4000 - 4999 are for application's custom messages
    public static final int CLOSE_CODE_SENSOR_NOT_FOUND = 4001;
    public static final int CLOSE_CODE_UNSUPPORTED_REQUEST = 4002;
    public static final int CLOSE_CODE_TYPE_PARAMETER_MISSING = 4003;
    public static final int CLOSE_CODE_SERVER_STOPPED = 4004;
    public static final int CLOSE_CODE_CONNECTION_CLOSED_BY_APP_USER = 4005;
    public static final int CLOSE_CODE_INVALID_JSON_ARRAY = 4006;
    public static final int CLOSE_CODE_TOO_FEW_SENSORS = 4007;
    public static final int CLOSE_CODE_NO_SENSOR_SPECIFIED = 4008;


    public SensorWebSocketServer(Context context, InetSocketAddress address)
    {
        super(address);
        sensorManager = (SensorManager) context.getSystemService(context.SENSOR_SERVICE);
        sensorUtil = SensorUtil.getInstance(context);
        registeredSensors = new ArrayList<>();

    }


    @Override
    public void onOpen(WebSocket clientWebsocket, ClientHandshake handshake)
    {
        Log.i(TAG, "New connection established " + clientWebsocket.getRemoteSocketAddress() + " Resource descriptor : " + clientWebsocket.getResourceDescriptor());

        //Parse uri so that we can read parameters from query
        Uri uri = Uri.parse(clientWebsocket.getResourceDescriptor());

        // ws://host:port/sensor/connect?type=<sensorType>
        if (uri.getPath().equalsIgnoreCase(CONNECTION_PATH_SINGLE_SENSOR))
        {
            Log.i(TAG, "param type " + uri.getQueryParameter("type"));
            handleSingleSensorRequest(uri, clientWebsocket);

          //ws://host:port/sensors/connect?types=["type1","type2"...]
        } else if (uri.getPath().equalsIgnoreCase(CONNECTION_PATH_MULTIPLE_SENSORS))
        {
            Log.i(TAG, "param types " + uri.getQueryParameter("types"));
            handleMultiSensorRequest(uri,clientWebsocket);

        } else
        {
            String errorMessage = "Unsupported request \n , " +
                    "use " + CONNECTION_PATH_SINGLE_SENSOR + "?type=<sensorType> for single sensor on single websocket connection and \n" +
                    " " + CONNECTION_PATH_MULTIPLE_SENSORS + "?types=[\"type1\",\"type2\", . . . ] for multiple sensors on single websocket connection";
            clientWebsocket.close(CLOSE_CODE_UNSUPPORTED_REQUEST, errorMessage);
        }

    }

    /**
     * Helper method to handle multiple sensor request on single websocket connection
     * this method is used in onOpen() method
    */
    private void handleMultiSensorRequest(Uri uri, WebSocket clientWebsocket)
    {
        if(uri.getQueryParameter("types") == null)
        {
            clientWebsocket.close(CLOSE_CODE_TYPE_PARAMETER_MISSING,"<Types> parameter required");
            return;
        }
        List<String> requestedSensorTypes = JsonUtil.readJSONArray(uri.getQueryParameter("types"));

        if (requestedSensorTypes == null)
        {
            clientWebsocket.close(CLOSE_CODE_INVALID_JSON_ARRAY, "Syntax error : " + uri.getQueryParameter("types") + " is not valid JSON array");
            return;
        }

        if (requestedSensorTypes.size() == 1)
        {
            clientWebsocket.close(CLOSE_CODE_TOO_FEW_SENSORS, "At least two sensor types must be specified");
            return;
        }

        if(requestedSensorTypes.isEmpty())
        {
            clientWebsocket.close(CLOSE_CODE_NO_SENSOR_SPECIFIED," No sensor specified");
            return;
        }

        Log.i(TAG, "requested sensors : " + requestedSensorTypes);

        List<Sensor> requestedSensorList = new ArrayList<Sensor>();

        for (String requestedSensorType : requestedSensorTypes)
        {
            Sensor sensor = sensorUtil.getSensorFromStringType(requestedSensorType);

            if (sensor == null)
            {
                clientWebsocket.close(CLOSE_CODE_SENSOR_NOT_FOUND, "sensor of type " + requestedSensorType + " not found");
                requestedSensorList.clear();
                return;
            }
            requestedSensorList.add(sensor);
        }

        registerMultipleSensors(requestedSensorList, clientWebsocket);
    }


    /**
     * Helper method to handle single sensor request on single websocket connection
     * this method is used in onOpen() method
     */
    private void handleSingleSensorRequest(Uri uri, WebSocket clientWebsocket)
    {
        String paramType = uri.getQueryParameter("type");

        //if type param doesn't exit in the query
        if(paramType == null)
        {
            clientWebsocket.close(CLOSE_CODE_TYPE_PARAMETER_MISSING,"<type> param required");
            //do not proceed further
            return;
        }

        if(paramType.isEmpty())
        {
            clientWebsocket.close(CLOSE_CODE_NO_SENSOR_SPECIFIED,"No sensor specified");
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
        registerSensor(requestedSensor,clientWebsocket);
        notifyConnectionsChanged();
    }

    // Helper method used in handleMultiSensorRequest()
    private void registerMultipleSensors(List<Sensor> sensors, WebSocket clientWebsocket)
    {

        //for new requesting client attach a tag of requested sensor type with client
        clientWebsocket.setAttachment(sensors);

        for(Sensor sensor : sensors)
        {

        /*
            if this WebSocket Server has already registered itself for some type of sensor (e.g android.sensor.light)
            then we don't have to registered this Server for the same sensor again
        */

            if(registeredSensors.contains(sensor))
            {
                Log.i(TAG, "Sensor " + sensor.getStringType() + " already registered, skipping registration");

                //Update registry
                registeredSensors.add(sensor);
                notifyConnectionsChanged();

                // no need to call sensorManager.registerListener();
                return;
            }


            if(sensor != null)
            {
             /*
              Register requested sensor
              sensor events will be reported to main thread if handler is not provided
              see https://stackoverflow.com/questions/23209804/android-sensor-registerlistener-in-a-separate-thread
              and https://pastebin.com/QuHd0LNU
            */
                sensorManager.registerListener(this,sensor, getSamplingRate() , handler);

            /*
            TODO:
             android offical docs say (https://developer.android.com/reference/android/hardware/SensorManager)
             Note: Don't use this method (registerListener) with a one shot trigger sensor such as Sensor#TYPE_SIGNIFICANT_MOTION.
             Use requestTriggerSensor(android.hardware.TriggerEventListener, android.hardware.Sensor) instead.

             */


                // Update registry
                registeredSensors.add(sensor);
                notifyConnectionsChanged();

            }


        }
    }

    // Helper method used in  handleSingleSensorRequest()
    private void registerSensor(Sensor requestedSensor, WebSocket clientWebsocket)
    {

        /*
        Attach info with newly connected client
        so that this Servers knows which client has requested which type of sensor
         */
        clientWebsocket.setAttachment(requestedSensor);

        /*
            if this WebSocket Server has already registered itself for some type of sensor (e.g android.sensor.light)
            then we don't have to registered this Server for the same sensor again
        */

        if(registeredSensors.contains(requestedSensor))
        {
            Log.i(TAG, "Sensor " + requestedSensor.getStringType() + " already registered, skipping registration");

            //Update registry
            registeredSensors.add(requestedSensor);
            notifyConnectionsChanged();

            // no need to call sensorManager.registerListener();
            return;
        }


        if(requestedSensor != null)
        {
             /*
              Register requested sensor
              sensor events will be reported to main thread if handler is not provided
              see https://stackoverflow.com/questions/23209804/android-sensor-registerlistener-in-a-separate-thread
              and https://pastebin.com/QuHd0LNU
            */
            sensorManager.registerListener(this,requestedSensor, getSamplingRate() , handler);

            /*
            TODO:
             android offical docs say (https://developer.android.com/reference/android/hardware/SensorManager)
             Note: Don't use this method (registerListener) with a one shot trigger sensor such as Sensor#TYPE_SIGNIFICANT_MOTION.
             Use requestTriggerSensor(android.hardware.TriggerEventListener, android.hardware.Sensor) instead.

             */


            // Update registry
            registeredSensors.add(requestedSensor);
            notifyConnectionsChanged();

        }
    }

    @Override
    public void onClose(WebSocket clientWebsocket, int code, String reason, boolean remote)
    {
        Log.i(TAG,"Connection closed " + clientWebsocket.getRemoteSocketAddress() + " with exit code " + code + " additional info: " + reason);


        if(clientWebsocket.getAttachment() instanceof Sensor)
        {
            // Get sensor type of recently closed client
            Sensor sensor = clientWebsocket.getAttachment();
            unregisterSensor(sensor);
        }
        else if (clientWebsocket.getAttachment() instanceof ArrayList)
        {
            List<Sensor> sensors = clientWebsocket.getAttachment();
            for(Sensor sensor : sensors)
                unregisterSensor(sensor);
        }


    }
    // This method is used in OnClose()
    private void unregisterSensor(Sensor sensor)
    {
        if(sensor == null)
            return;
        // When client has closed connection, how many clients receiving same sensor data from this server
        long sensorConnectionCount = getSensorConnectionCount(sensor);


        Log.i(TAG, "Sensor : " + sensor.getStringType() + " Connections : " + sensorConnectionCount );

        /*
            Suppose we have 3 clients each receiving light sensor data \
            if we unregister this server for light sensor when only one client is disconnected \
            then 2 other connected client won't receive light sensor data anymore

        */

        //  Unregister sensor if and only if one client is using it
        if(sensorConnectionCount == 1)
            sensorManager.unregisterListener(this, sensor);


        registeredSensors.remove(sensor);

        Log.i(TAG, "Total Connections : " + getConnectionCount());
        notifyConnectionsChanged();

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
            if (serverErrorListener != null)
                serverErrorListener.onServerError(ex); // listener must filter exception by itself

            serverStartUpFailed = true; // we will use this in stop() method to check if there was an exception during server startup
        }

            ex.printStackTrace();

    }

    @Override
    public void onStart()
    {

        if(this.serverStartListener != null)
        {
            ServerInfo serverInfo = new ServerInfo(getAddress().getHostName(),getPort());
            this.serverStartListener.onServerStarted(serverInfo);
        }

        isRunning = true;
        Log.i(TAG,"server started successfully " + this.getAddress());
        Log.i(TAG, "sampling rate " + getSamplingRate());

    }

   @Override
    public void stop() throws IOException, InterruptedException
   {
        closeAllConnections();
        super.stop();
        Log.d(TAG, "stop() called");

            if( serverStopListener != null && !serverStartUpFailed )
                this.serverStopListener.onServerStopped();

            if(handlerThread.isAlive())
                handlerThread.quitSafely();

        isRunning = false;

    }

    public boolean isRunning()
    {
        return isRunning;
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

        // see https://stackoverflow.com/questions/23209804/android-sensor-registerlistener-in-a-separate-thread
        handlerThread = new HandlerThread("Handler Thread");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent)
    {
       // Log.i(TAG, "onSensorChanged: Thread " + Thread.currentThread().getName());
       // Log.i(TAG, "onSensorChanged: Sensor " + sensorEvent.sensor.getStringType());
        if(getConnectionCount() == 0 )
            Log.e(TAG," Sensor event reported when no client in connected");

        response.clear();

        // Loop through each connected client
        for( WebSocket webSocket : getConnections() )
        {
            // Send data as per sensor type requested by client
            if (webSocket.getAttachment() instanceof Sensor)
            {
                Sensor clientAssociatedSensor = webSocket.getAttachment();

                if (clientAssociatedSensor != null)

                    if (clientAssociatedSensor.getType() == sensorEvent.sensor.getType() && !webSocket.isClosing())
                    {
                        response.put("values", sensorEvent.values);
                        response.put("timestamp", sensorEvent.timestamp);
                        response.put("accuracy", sensorEvent.accuracy);

                        webSocket.send(JsonUtil.toJSON(response));
                    }

            }
            else if (webSocket.getAttachment() instanceof ArrayList)
            {
                List<Sensor> clientAssociatedSensors = webSocket.getAttachment();

                for(Sensor clientAssociatedSensor : clientAssociatedSensors)
                {
                    if (clientAssociatedSensor != null)

                        if (clientAssociatedSensor.getType() == sensorEvent.sensor.getType() && !webSocket.isClosing())
                        {
                            response.put("values", sensorEvent.values);
                            response.put("timestamp", sensorEvent.timestamp);
                            response.put("accuracy", sensorEvent.accuracy);
                            response.put("type",sensorEvent.sensor.getStringType());

                            webSocket.send(JsonUtil.toJSON(response));
                        }
                }

            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i)
    {

    }

    private int getSensorConnectionCount(Sensor sensor)
    {
        int count = 0;
        for(Sensor registeredSensor : registeredSensors)
            if(registeredSensor.getType() == sensor.getType())
                count++;

        return count;
    }


    public void setSamplingRate(int samplingRate)
    {
        this.samplingRate = samplingRate;
    }

    public int getSamplingRate()
    {
        return samplingRate;
    }


    public void notifyConnectionsChanged()
    {
        Log.d(TAG, "notifyConnectionsChanged() : " + getConnections().size());
        if(connectionsChangeListener != null)
            connectionsChangeListener.onConnectionsChanged(new ArrayList<>(getConnections()));
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

    public void setServerStartListener(ServerStartListener serverStartListener)
    {
        this.serverStartListener = serverStartListener;
    }

    public void setServerStopListener(ServerStopListener serverStopListener)
    {
        this.serverStopListener = serverStopListener;
    }

    public void setServerErrorListener(ServerErrorListener serverErrorListener)
    {
        this.serverErrorListener = serverErrorListener;
    }

    public void setConnectionsChangeListener(ConnectionsChangeListener connectionsChangeListener)
    {
        this.connectionsChangeListener = connectionsChangeListener;
    }
}
