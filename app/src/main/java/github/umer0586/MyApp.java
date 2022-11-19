package github.umer0586;

import android.app.Application;

import com.bugsnag.android.Bugsnag;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Bugsnag.start(getApplicationContext());
    }
}