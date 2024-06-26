package github.umer0586.sensorserver.webserver

import android.content.Context
import com.yanzhenjie.andserver.AndServer
import com.yanzhenjie.andserver.Server
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.lang.Exception
import java.net.InetAddress
import java.util.concurrent.TimeUnit

data class HttpServerInfo(val address: String, val portNo : Int){
    val fullAddress get() = "$address:$portNo"
    val baseUrl get() = "http://$fullAddress"
}
class HttpServer(val context : Context, val address : String, val portNo : Int) {

    private val scope = CoroutineScope(Dispatchers.IO)
    private var server : Server? = null

    private var onStart : ((HttpServerInfo) -> Unit)? = null
    private var onStop : (() -> Unit)? = null
    private var onError : ((Exception) -> Unit)? = null

    val isRunning get() = server?.isRunning ?: false
    val httpServerInfo get() = HttpServerInfo(address = address, portNo = portNo)

    fun startServer(){
     scope.launch {
         server = AndServer.webServer(context).apply {
             port(portNo)
             timeout(10, TimeUnit.SECONDS)
             inetAddress(InetAddress.getByName(address))
             listener(object : Server.ServerListener{
                 override fun onStarted() {
                     onStart?.invoke(httpServerInfo)
                 }

                 override fun onStopped() {
                     onStop?.invoke()
                 }

                 override fun onException(e: java.lang.Exception?) {
                     e?.let {
                         onError?.invoke(it)
                     }
                 }

             })
         }.build()

         server?.startup()
     }
    }

    fun stopServer(){
        server?.apply {
            if(isRunning){
                shutdown()
                scope.cancel()
            }
        }
    }

    fun setOnStart(onStart : ((HttpServerInfo) -> Unit)?){
        this.onStart = onStart
    }
    fun setOnStop(onStop : (() -> Unit)?){
        this.onStop = onStop
    }
    fun setOnError(onError : ((Exception) -> Unit)?){
        this.onError = onError
    }


}