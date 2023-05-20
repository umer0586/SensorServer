# SensorServer
 
### Effortlessly stream real-time sensor data from your phone to multiple WebSocket clients, enabling them to monitor the device's movement, environment, and position in real-time.

![server](https://user-images.githubusercontent.com/35717992/146649500-f4f1aadf-60e0-4305-81bc-f7db21540bd7.gif)    ![connections](https://user-images.githubusercontent.com/35717992/146649573-9b86ff77-565c-46ef-900b-63350f4eac3b.gif)    ![sensors](https://user-images.githubusercontent.com/35717992/146649578-adb5f0eb-4a7a-462a-9e16-264f4599903f.gif)






Android app which let you stream real-time sensor data from your phone to Websocket clients. Clients, including web browsers and other applications, are able to receive streamed data through the WebSocket client API. Since this application functions as a Websocket Server, you will require a Websocket Client API to establish a connection with the application. To obtain a Websocket library for your preferred programming language click [here](https://github.com/facundofarias/awesome-websockets). 
 
 
 
 
 # Usage
 To receive sensor data, **Websocket client**  must connect to the app using following **URL**.
 
                 ws://<ip>:<port>/sensor/connect?type=<sensor type here> 
 
 
  Value for the `type` parameter can be found by navigating to **Available Sensors** in the app. 
 
 For example
 
 * For **accelerometer** `/sensor/connect?type=android.sensor.accelerometer` .
 
 * For **gyroscope** `/sensor/connect?type=android.sensor.gyroscope` .
 
 * For **step detector**  `/sensor/connect?type=android.sensor.step_detector`

 * so on... 
 
 Once connected, client will receive sensor data in `JSON Array` (float type values) through `websocket.onMessage`. 
 
 A snapshot from accelerometer.
 
 ```json
{
  "accuracy": 2,
  "timestamp": 3925657519043709,
  "values": [0.31892395,-0.97802734,10.049896]
}
 ```
![axis_device](https://user-images.githubusercontent.com/35717992/179351418-bf3b511a-ebea-49bb-af65-5afd5f464e14.png)

where

| Array Item  | Description |
| ------------- | ------------- |
| values[0]  | Acceleration force along the x axis (including gravity)  |
| values[1]  | Acceleration force along the y axis (including gravity)  |
| values[2]  | Acceleration force along the z axis (including gravity)  |

And [timestamp](https://developer.android.com/reference/android/hardware/SensorEvent#timestamp) is the time in nanoseconds at which the event happened

Use `JSON` parser to get these individual values.

 
**Note** : *Use  following links to know what each value in **values** array corresponds to*
- For motion sensors [/topics/sensors/sensors_motion](https://developer.android.com/guide/topics/sensors/sensors_motion)
- For position sensors [/topics/sensors/sensors_position](https://developer.android.com/guide/topics/sensors/sensors_position)
- For Environmental sensors [/topics/sensors/sensors_environment](https://developer.android.com/guide/topics/sensors/sensors_environment)

## Undocumented (mostly QTI) sensors on Android devices
Some Android devices have additional sensors like **Coarse Motion Classifier** `(com.qti.sensor.motion_classifier)`, **Basic Gesture** `(com.qti.sensor.basic_gestures)` etc  which are not documented on offical android docs. Please refer to this [Blog](https://louis993546.medium.com/quick-tech-support-undocumented-mostly-qti-sensors-on-android-devices-d7e2fb6c5064) for corresponding values in `values` array  

## Supports multiple connections to multiple sensors simultaneously

 **Many Websocket clients** can connect to one `type` of a Sensor. So connecting to **`/sensor/connect?type=android.sensor.accelerometer`** three times will create three different connections to the accelerometer sensor and each connected client will then receive accelerometer data at the same time.
 
Moreover, from one or different machines you can connect to different types of sensors as well i-e one **Websocket Client object** could connect to accelerometer and other **Websocket Client object** to gyroscope. All active connections can be viewed by selecting **Connections** navigation button.
 
## Test with Websocket testing tools 
You can test this app with any websocket testing tools available on the web or playstore. You can use http://livepersoninc.github.io/ws-test-page/ for testing purpose. Use multiple tabs for multiple websocket connections

## Example: Websocket client (Python) 
Here is a simple websocket client in python using [websocket-client api](https://github.com/websocket-client/websocket-client) which receives live data from accelerometer sensor.

```python
import websocket
import json


def on_message(ws, message):
    values = json.loads(message)['values']
    x = values[0]
    y = values[1]
    z = values[2]
    print(f"x = {x} , y = {y} , z = {z}")

def on_error(ws, error):
    print("error occurred")
    print(error)

def on_close(ws, close_code, reason):
    print("connection close")
    print("close code : ", close_code)
    print("reason : ", reason  )

def on_open(ws):
    print("connected")
    

def connect(url):
    ws = websocket.WebSocketApp(url,
                              on_open=on_open,
                              on_message=on_message,
                              on_error=on_error,
                              on_close=on_close)

    ws.run_forever()
 
 
connect("ws://192.168.0.101:8080/sensor/connect?type=android.sensor.accelerometer") 

```
 *Your device's IP might be different when you tap start button, so make sure you are using correct IP address at client side*
 
## Using Multiple Sensors Over single Websocket Connection
You can also use to multiple sensors over single websocket connection. To use multiple sensors over single websocket connection use following **URL**.

                 ws://<ip>:<port>/sensors/connect?types=["<type1>","<type2>","<type3>"...]

By connecting using above URL you will receive JSON response containing sensor data along with a type of sensor. See complete example at [Using Multiple Sensors On Single Websocket Connection](https://github.com/umer0586/SensorServer/wiki/Using-Multiple-Sensors-On-Single-Websocket-Connection)

## Getting Device Location Using GPS and Network Provider
You access device location using following url.

                 ws://<ip>:<port>/location?provider=<location provider type>
                 
Where a `provider` could be `gps` or `network`. In Android, both GPS and Network providers can be used to get location information. However, the accuracy and precision of each provider may vary depending on various factors. GPS is a satellite-based system that provides more accurate location information than the Network provider. GPS provides location information based on satellite signals, which can be affected by various factors such as weather conditions, buildings, and other obstacles that can block the signal. On the other hand, Network provider determines the location based on the cell tower and Wi-Fi signals in the area. Network provider can provide a faster location fix than GPS, but its accuracy may not be as precise as GPS. The network provider will be removed in future releases since it's not useful in the context of this app. Also, please ensure that you have granted location permission to the app through your phone's installed app settings.

JSON response contains following key fields.
```json
{
"longitude": "longitude in degrees",
"latitude": "latitude in degrees",
"altitude": "The altitude of location in meters above the WGS84 reference ellipsoid",
"bearing": "bearing at the time of this location in degrees. Bearing is the horizontal direction of travel of this device and is unrelated to the device orientation. The bearing is guaranteed to be in the range [0, 360)",
"accuracy": "Estimated horizontal accuracy radius in meters of this location at the 68th percentile confidence level",
"speed": "Speed at the time of this location in meters per second",
"time": "the Unix epoch time of this location fix, in milliseconds since the start of the Unix epoch (00:00:00 January 1, 1970 UTC)"
}
```

Fields only for Android 8.0 and above.

```json

{
"speedAccuracyMetersPerSecond": "Estimated speed accuracy in meters per second of this location at the 68th percentile confidence level",
"bearingAccuracyDegrees": "Estimated bearing accuracy in degrees of this location at the 68th percentile confidence level",
"elapsedRealtimeNanos": "Time of this fix in nanoseconds of elapsed realtime since system boot",
"verticalAccuracyMeters": "The estimated altitude accuracy in meters of this location at the 68th percentile confidence level"
}
```



## Real Time plotting
See [Real Time Plot of Accelerometer (Python)](https://github.com/umer0586/SensorServer/wiki/Real-Time-Plot-Example-(-Python)) using this app

![result](https://user-images.githubusercontent.com/35717992/208961337-0f69757e-e85b-4637-8c39-fa5554d85921.gif)

## Connecting over Hotspot :fire:
If you don't have Wifi network at your work place you can directly connect websocket clients to the app by enabling **Hotspot Option** from settings. Just make sure that websocket clients are connected to your phone's hotspot


## Connecting over USB (using ADB)
To connect over USB make sure `USB debugging` option is enable in your phone and `ADB` (android debug bridge) is available in your machine
* **Step 1 :** Enable `Local Host` option in app
* **Step 2** : Run adb command `adb forward tcp:8081 tcp:8081` (8081 is just for example) from client
* **Step 3** : use address `ws://localhost:8081:/sensor/connect?type=<sensor type here>` to connect 

Make sure you have installed your android device driver and `adb devices` command detects your connected android phone.


# APK Download ⏬ ![version](https://img.shields.io/badge/version-3.2.1-blue) 
Download latest *APK* from [Release page](https://github.com/umer0586/SensorServer/releases) *(requires Android 5.0 or above)* . You can also get this app from [F-Droid](https://f-droid.org/en/) by adding [https://apt.izzysoft.de/fdroid/repo](https://apt.izzysoft.de/fdroid/repo) repository in F-Droid client 
##
_You can appreciate this work by buying me a coffee_ :coffee: [https://www.buymeacoffee.com/umerfarooq](https://www.buymeacoffee.com/umerfarooq) 

