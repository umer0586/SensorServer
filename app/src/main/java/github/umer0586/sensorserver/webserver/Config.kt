package github.umer0586.sensorserver.webserver

import android.content.Context
import com.yanzhenjie.andserver.annotation.Config
import com.yanzhenjie.andserver.framework.config.WebConfig
import com.yanzhenjie.andserver.framework.website.AssetsWebsite
import com.yanzhenjie.andserver.framework.website.StorageWebsite


@Config
class AppConfig : WebConfig {
    override fun onConfig(context: Context, delegate: WebConfig.Delegate) {

        delegate.addWebsite(AssetsWebsite(context, "/webapp/"))

    }
}