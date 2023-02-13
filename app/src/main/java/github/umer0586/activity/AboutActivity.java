package github.umer0586.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import github.umer0586.BuildConfig;
import github.umer0586.R;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        getSupportActionBar().hide();

        findViewById(R.id.donationBtn).setOnClickListener(view->{
            openLink("http://www.buymeacoffee.com/umerfarooq");
        });

        findViewById(R.id.sourceCodeBtn).setOnClickListener(view->{
            openLink("http://github.com/umer0586/SensorServer");
        });

        AppCompatTextView version = findViewById(R.id.app_version);
        version.setText("v"+BuildConfig.VERSION_NAME);

    }

    private void openLink(String link)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if(intent.resolveActivity(getApplicationContext().getPackageManager()) != null)
        {
            intent.setData(Uri.parse(link));
            startActivity(Intent.createChooser(intent,"Select Browser"));
        }
        else
        {
            Toast.makeText(getApplicationContext(),"Browser app not found",Toast.LENGTH_SHORT).show();
        }
    }
}