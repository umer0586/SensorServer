package github.umer0586.sensorserver.activities

import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import github.umer0586.sensorserver.R
import java.io.IOException

class DeviceAxisActivity : AppCompatActivity()
{


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_axis)
        val deviceAxisImage = findViewById<AppCompatImageView>(R.id.device_axis)
        try
        {
            val ims = assets.open("axis_device.png")
            val d = Drawable.createFromStream(ims, null)
            deviceAxisImage.setImageDrawable(d)
            ims.close()
        }
        catch (ex: IOException)
        {
            return
        }
    }
}