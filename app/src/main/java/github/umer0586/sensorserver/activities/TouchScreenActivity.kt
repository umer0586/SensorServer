package github.umer0586.sensorserver.activities

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import github.umer0586.sensorserver.R
import github.umer0586.sensorserver.service.WebsocketService
import github.umer0586.sensorserver.service.ServiceBindHelper

class TouchScreenActivity : AppCompatActivity()
{
    private val TAG = "TouchScreenActivity"
    private var websocketService: WebsocketService? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_touch_screen)

        val serviceBindHelper = ServiceBindHelper(
            context = applicationContext,
            service = WebsocketService::class.java,
            componentLifecycle = lifecycle
        )

        serviceBindHelper.onServiceConnected { binder ->

            val localBinder = binder as WebsocketService.LocalBinder
            websocketService = localBinder.service
        }

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean
    {
        event?.let {
            websocketService?.sendMotionEvent(it)
        }

        return super.onTouchEvent(event)
    }


}