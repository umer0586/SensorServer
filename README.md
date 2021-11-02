# SensorServer
 An Android app which streams phone's motion sensors to **Websocket** client 
 
 This app sends sensor data from Android device to Websocket clients in realtime. A websocket client could be a web browser or any application running on PC or mobile device which uses **Websocket API**.
 
 To receive sensor data, client must connect using `ws://IPAddress:port/sensor/connect?type=<sensor type here>` . Once connected, client will receive sensor data in `JSON` (Json Array) format through `websocket.onMessage()` method . 
 
 Value for the `type` parameter can be found by navigating to `available sensors` in app. 
 
 For example
 
 For accelerometer `/sensor/connect?type=android.sensor.accelerometer` .
 
 for light sensor `/sensor/connect?type=android.sensor.light` etc .
 
 
