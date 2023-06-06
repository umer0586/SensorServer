package github.umer0586.sensorserver.util

import android.os.Handler
import android.os.Looper

object UIUtil
{


    private val handler = Handler(Looper.getMainLooper())
    fun runOnUiThread(runnable: Runnable?)
    {
        handler.post(runnable!!)
    }
}