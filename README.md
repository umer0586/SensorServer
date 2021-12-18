# SensorServer
Android app which streams phone's motion sensors to **Websocket** clients.
 

![server](https://user-images.githubusercontent.com/35717992/146649500-f4f1aadf-60e0-4305-81bc-f7db21540bd7.gif)    ![connections](https://user-images.githubusercontent.com/35717992/146649573-9b86ff77-565c-46ef-900b-63350f4eac3b.gif)    ![sensors](https://user-images.githubusercontent.com/35717992/146649578-adb5f0eb-4a7a-462a-9e16-264f4599903f.gif)






 This app streams phone's motion sensors to Websocket clients in realtime. A websocket client could be a web browser or any application running on a PC or a mobile device which uses **Websocket Client API**.  
 
 
 
 
 # Usage
 To receive sensor data, **Websocket client**  must connect to the app using following **URL**.
 
                 ws://<ip>:<port>/sensor/connect?type=<sensor type here> 
 
 
  Value for the `type` parameter can be found by navigating to **Available Sensors** in the app. 
 
 For example
 
 * For **accelerometer** `/sensor/connect?type=android.sensor.accelerometer` .
 
 * For **orientation** `/sensor/connect?type=android.sensor.orientation` .
 
 * For **step detector**  `/sensor/connect?type=android.sensor.step_detector`

 * so on... 
 
 Once connected, client will receive sensor data in `JSON Array` (float type values) through `websocket.onMessage`. Description of each data value at index in an array can be obtain from https://developer.android.com/guide/topics/sensors/sensors_motion   
 
## Supports multiple connections

 **Many Websocket clients** can connect to one `type` of a Sensor. So connecting to **`/sensor/connect?type=android.sensor.gravity`** three times will create three different connections to the gravity sensor and each connected client will then receive gravity sensor data at the same time.
 
Moreover, from one or different machines you can connect to different types of sensors as well i-e one **Websocket Client object** could connect to step detector sensor and other **Websocket Client object** to gyroscope. All active connections can be viewed by selecting **Connections** navigation button.
 
## Test with Websocket testing tools 
Before writing your own websocket client, test this app with any websocket testing tools available on the web or playstore. You can use http://livepersoninc.github.io/ws-test-page/ for testing purpose.
 

# APK Download
Download latest *APK* from [Release page](https://github.com/umer0586/SensorServer/releases) *(requires Android 5.0)* 
