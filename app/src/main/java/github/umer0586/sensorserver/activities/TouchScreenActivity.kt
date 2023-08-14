package github.umer0586.sensorserver.activities

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.MotionEvent
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import github.umer0586.sensorserver.R
import github.umer0586.sensorserver.databinding.ActivityTouchScreenBinding
import github.umer0586.sensorserver.service.SensorService
import github.umer0586.sensorserver.service.ServiceBindHelper
import github.umer0586.sensorserver.util.JsonUtil
import github.umer0586.sensorserver.websocketserver.TouchSensors

class TouchScreenActivity : AppCompatActivity(),ServiceConnection
{
    private val TAG = "TouchScreenActivity"
    private var sensorService: SensorService? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_touch_screen)

        val serviceBindHelper = ServiceBindHelper(
            context = applicationContext,
            serviceConnection = this,
            service = SensorService::class.java
        )
        lifecycle.addObserver(serviceBindHelper)

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean
    {
        event?.let {
            sensorService?.sendMotionEvent(it)
        }

        return super.onTouchEvent(event)
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?)
    {
        Log.d(TAG,"onServiceConnected()")

        val localBinder = service as SensorService.LocalBinder
        sensorService = localBinder.service

    }

    override fun onServiceDisconnected(name: ComponentName?)
    {
        TODO("Not yet implemented")
    }
}