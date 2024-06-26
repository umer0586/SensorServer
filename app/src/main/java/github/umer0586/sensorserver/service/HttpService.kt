package github.umer0586.sensorserver.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import github.umer0586.sensorserver.R
import github.umer0586.sensorserver.activities.MainActivity
import github.umer0586.sensorserver.broadcastreceiver.BroadcastMessageReceiver
import github.umer0586.sensorserver.customextensions.getHotspotIp
import github.umer0586.sensorserver.customextensions.getIp
import github.umer0586.sensorserver.setting.AppSettings
import github.umer0586.sensorserver.webserver.HttpServer
import github.umer0586.sensorserver.webserver.HttpServerInfo
import java.net.UnknownHostException


interface HttpServerStateListener {
    fun onStart(httpServerInfo : HttpServerInfo)
    fun onStop()
    fun onError(exception: java.lang.Exception)
    fun onRunning(httpServerInfo: HttpServerInfo)
}
class HttpService : Service() {

    private var httpServer : HttpServer? = null;
    val serverInfo get() = httpServer?.httpServerInfo
    val isServerRunning get() = httpServer?.isRunning ?: false

    private var serverStateListener : HttpServerStateListener? = null;

    private lateinit var appSettings: AppSettings

    // Binder given to clients
    private val binder: IBinder = LocalBinder()

    //Intents broadcast by Fragment/Activity are received by this service via MessageReceiver (BroadCastReceiver)
    private lateinit var broadcastMessageReceiver: BroadcastMessageReceiver

    companion object
    {


        private val TAG: String = HttpService::class.java.getSimpleName()
        const val CHANNEL_ID = "HTTP-service-channel"

        // cannot be zero
        const val ON_GOING_NOTIFICATION_ID = 934

        // Broadcast intent action (published by other app's component) to stop server thread
        val ACTION_STOP_SERVER = "ACTION_STOP_SERVER_" + HttpService::class.java.getName()
    }


    override fun onCreate()
    {
        super.onCreate()
        Log.d(TAG, "onCreate()")
        createNotificationChannel()
        appSettings = AppSettings(applicationContext)
        broadcastMessageReceiver = BroadcastMessageReceiver(applicationContext)

        with(broadcastMessageReceiver)
        {

            setOnMessageReceived { intent ->
                onMessage(intent)
            }

            registerEvents(
                    IntentFilter().apply {
                        addAction(ACTION_STOP_SERVER)
                    }
            )
        }


    }

    fun onMessage(intent: Intent)
    {
        Log.d(TAG, "onMessage() called with: intent = [$intent]")
        if (intent.action == ACTION_STOP_SERVER)
        {
            httpServer?.stopServer()

        }


    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int
    {

        Log.d(TAG, "onStartCommand()")
        handleAndroid8andAbove()

        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        val ipAddress : String? = when{
            appSettings.isLocalHostOptionEnable() -> "127.0.0.1"
            appSettings.isAllInterfaceOptionEnabled() -> "0.0.0.0"
            appSettings.isHotspotOptionEnabled() -> wifiManager.getHotspotIp() // could be null
            else -> wifiManager.getIp() // could be null

        }

        if(ipAddress == null)
        {
            serverStateListener?.onError(UnknownHostException("Unable to obtain hotspot IP"))

            // Not calling a handleAndroid8andAbove() immediately after onStartCommand
            // would cause application to crash as we are not calling startForeground() here before returning
            stopForeground()
            return START_NOT_STICKY
        }



        httpServer = HttpServer(
                context = applicationContext,
                address = ipAddress,
                portNo = 9090 )

        httpServer?.setOnStart { serverInfo ->
            onStarted(serverInfo)
            serverStateListener?.onStart(serverInfo)
        }

        httpServer?.setOnStop {
            httpServer?.apply {
                serverStateListener?.onStop();
                //remove the service from foreground but don't stop (destroy) the service
                //stopForeground(true)
                stopForeground()
            }
        }

        httpServer?.setOnError { exception ->
            serverStateListener?.onError(exception)
            //remove the service from foreground but don't stop (destroy) the service
            //stopForeground(true)
            stopForeground()
        }


        httpServer?.startServer()

        return START_NOT_STICKY
    }

    // http server onStart callback
    private fun onStarted(serverHttpServerInfo: HttpServerInfo) {


        // intent to start activity
        val activityIntent = Intent(this, MainActivity::class.java)

        // Intent to be broadcast (when user press action button in notification)
        val broadcastIntent = Intent(ACTION_STOP_SERVER)

        // create a pending intent that can invoke an activity (use to open activity from notification message)
        val pendingIntentActivity = PendingIntent.getActivity(this, 0, activityIntent, PendingIntent.FLAG_IMMUTABLE)

        // create a pending intent that can fire broadcast (use to send broadcast when user taps action button from notification)
        val pendingIntentBroadcast = PendingIntent.getBroadcast(this,0,broadcastIntent, PendingIntent.FLAG_IMMUTABLE)

        val notificationBuilder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .apply {
                    setSmallIcon(R.drawable.ic_radar_signal)
                    setContentTitle("Web server Running...")
                    setContentText(serverHttpServerInfo.baseUrl)
                    setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    setContentIntent(pendingIntentActivity) // Set the intent that will fire when the user taps the notification
                    addAction(android.R.drawable.ic_lock_power_off,"stop", pendingIntentBroadcast)
                    setAutoCancel(false) // don't cancel notification when user taps it
                }


        val notification = notificationBuilder.build()
        startForeground(ON_GOING_NOTIFICATION_ID, notification)

    }

    private fun createNotificationChannel()
    {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            Log.d(TAG, "createNotificationChannel() called")
            val name: CharSequence = "Sensor-Server"
            val description = "Notifications from SensorServer"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = description
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService( NotificationManager::class.java )
            notificationManager.createNotificationChannel(channel)
        }
    }

    /*
     * For Android 8 and above there is a framework restriction which required service.startForeground()
     * method to be called within five seconds after call to Context.startForegroundService()
     * so make sure we call this method even if we are returning from service.onStartCommand() without calling
     * service.startForeground()
     *
     * */
    private fun handleAndroid8andAbove()
    {
        val TEMP_NOTIFICATION_ID = 651

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            val tempNotification = NotificationCompat.Builder(
                    applicationContext, CHANNEL_ID
            )
                    .setSmallIcon(R.drawable.ic_signal)
                    .setContentTitle("")
                    .setContentText("").build()
            startForeground(TEMP_NOTIFICATION_ID, tempNotification)
            //stopForeground(true)
            stopForeground()
        }
    }

    @Suppress("DEPRECATION")
    private fun stopForeground()
    {
        /*
        If the device is running an older version of Android,
        we fallback to stopForeground(true) to remove the service from the foreground and dismiss the ongoing notification.
        Although it shows as deprecated, it should still work as expected on API level 21 (Android 5).
         */

        // for Android 7 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            stopForeground(STOP_FOREGROUND_REMOVE)
        else
        // This method was deprecated in API level 33.
        // Ignore deprecation message as there is no other alternative method for Android 6 and lower
            stopForeground(true)
    }

    override fun onDestroy()
    {
        super.onDestroy()
        Log.d(TAG, "onDestroy()")

        httpServer?.stopServer()

        broadcastMessageReceiver.unregisterEvents()

    }

    override fun onBind(intent: Intent): IBinder
    {
        return binder
    }

    fun checkState()
    {
        httpServer?.apply {
            if(isRunning){
                serverStateListener?.onRunning(this.httpServerInfo)
            }
        }
    }

    fun setServerStateListener(myHttpServerStateListener: HttpServerStateListener?){
        this.serverStateListener = myHttpServerStateListener
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder()
    {

        // Return this instance of LocalService so clients can call public methods
        val service: HttpService
            get() = this@HttpService // Return this instance of LocalService so clients can call public methods

    }


}


