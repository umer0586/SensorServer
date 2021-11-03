# SensorServer
 An Android app which streams phone's motion sensors to **Websocket** clients.
 

![connection](https://user-images.githubusercontent.com/35717992/140100371-eb401512-6d60-430f-8909-9a454ee15844.gif) ![connection info](https://user-images.githubusercontent.com/35717992/140101390-82c2b712-a777-4167-82e7-b71322d9495d.gif) ![available sensors](https://user-images.githubusercontent.com/35717992/140101489-08f682e9-96fb-4dbf-a03b-0943b94722c0.gif).




 This app sends sensor data from Android device to Websocket clients in realtime. A websocket client could be a web browser or any application running on PC or mobile device which uses **Websocket Client API**. This might be usefull in a scenario where you wish to test how your system reacts to android motion sensors. 
 
 
 
 
 # Usage
 To receive sensor data, **Websocket client**  must connect to the app using following **URL**.
 
                         ws://IPAddress:port/sensor/connect?type=<sensor type here> 
 
 
  Value for the `type` parameter can be found by navigating to **Available Sensors** in the app. 
 
 For example
 
 * For **accelerometer** `/sensor/connect?type=android.sensor.accelerometer` .
 
 * For **orientation** `/sensor/connect?type=android.sensor.orientation` .

 * so on... 
 
 Once connected, client will receive sensor data in `JSON Array` (float type values) through `websocket.onMessage` method. Description of each data value at index in an array can be obtain from https://developer.android.com/guide/topics/sensors/sensors_motion   
 

 **Many Websocket clients** can connect to one `type` of a Sensor. So connecting to **`/sensor/connect?type=android.sensor.gravity`** three times will create three different connections to the gravity sensor and each connected client will then receive gravity sensor data at the same time. Moreover, from one or different machines you can connect to different types of sensors as well i-e one **Websocket Client object** could connect to step detector sensor and other **Websocket Client object** to gyroscope. All active connections can be viewed by selecting **Connections** navigation button.
 
Before writing your own websocket client test this app with any websocket testing tools available on the web or playstore
 
 
 