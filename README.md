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
 

 **Many clients** can connect to one `type` of Sensor, so calling 
 
  ```
  Websocket gravitySensor = new Websocket("ws://IPAddress:port/sensor/connect?type=android.sensor.gravity")
  gravitySensor.connect();
  ```
  several times results in creating three different connects each receving **Gravity** sensor data at same time
 
