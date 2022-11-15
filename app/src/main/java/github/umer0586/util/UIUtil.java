package github.umer0586.util;

import android.os.Handler;
import android.os.Looper;

public class UIUtil {

    private static Handler handler = new Handler(Looper.getMainLooper());

    public static void runOnUiThread(Runnable runnable)
    {
        handler.post(runnable);
    }

}
