package github.umer0586.sensorserver.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import java.io.IOException;
import java.io.InputStream;

import github.umer0586.sensorserver.R;

public class DeviceAxisActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_axis);
        AppCompatImageView deviceAxisImage = findViewById(R.id.device_axis);

        try
        {

            InputStream ims = getAssets().open("axis_device.png");
            Drawable d = Drawable.createFromStream(ims, null);
            deviceAxisImage.setImageDrawable(d);
            ims.close();

        } catch (IOException ex)
        {
            return;
        }

    }
}