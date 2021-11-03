# SensorServer
 An Android app which streams phone's motion sensors to **Websocket** client 
 
 This app sends sensor data from Android device to Websocket clients in realtime. A websocket client could be a web browser or any application running on PC or mobile device which uses **Websocket API**.
 
 To receive sensor data, client must connect using following **URL**.
 
                         ws://IPAddress:port/sensor/connect?type=<sensor type here> 
 
 
  Value for the `type` parameter can be found by navigating to **Available Sensors** in the app. 
 
 For example
 
 * For **accelerometer** `/sensor/connect?type=android.sensor.accelerometer` .
 
 * For **orientation** `/sensor/connect?type=android.sensor.orientation` .

 * so on... 
 
 Once connected, client will receive sensor data in `JSON Array` (float type values) through `websocket.onMessage(String)` method. Description of each data value at index in an array can be obtained from https://developer.android.com/guide/topics/sensors/sensors_motion   
 

 **Many clients** can connect to one `type` of Sensor. So connecting to `/sensor/connect?type=android.sensor.gravity` three times will create three different connections to the gravity sensor and each connected client will then recieve gravity sensor data at the same time. Moreover, from one or different machines you can connect to different type of sensors i-e one websocket client object could be connected to step detector sensor and other to gyroscope
 
