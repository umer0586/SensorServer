package github.umer0586.sensorserver.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import github.umer0586.sensorserver.BuildConfig
import github.umer0586.sensorserver.R

class AboutActivity : AppCompatActivity()
{


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        findViewById<View>(R.id.donationBtn).setOnClickListener {
            openLink("http://www.buymeacoffee.com/umerfarooq")
        }
        findViewById<View>(R.id.sourceCodeBtn).setOnClickListener {
            openLink("http://github.com/umer0586/SensorServer")
        }

        val version = findViewById<AppCompatTextView>(R.id.app_version)
        version.text = "v" + BuildConfig.VERSION_NAME
    }

    private fun openLink(link: String)
    {
        val intent = Intent(Intent.ACTION_VIEW)
        if (intent.resolveActivity(applicationContext.packageManager) != null)
        {
            intent.data = Uri.parse(link)
            startActivity(Intent.createChooser(intent, "Select Browser"))
        }
        else
        {
            Toast.makeText(applicationContext, "Browser app not found", Toast.LENGTH_SHORT).show()
        }
    }
}