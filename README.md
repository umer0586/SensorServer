# SensorServer
![GitHub](https://img.shields.io/github/license/umer0586/SensorServer) ![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/umer0586/SensorServer) ![GitHub all releases](https://img.shields.io/github/downloads/umer0586/SensorServer/total?label=GitHub%20downloads) ![Android](https://img.shields.io/badge/Android%205.0+-3DDC84?style=flat&logo=android&logoColor=white) ![F-Droid](https://img.shields.io/f-droid/v/github.umer0586.sensorserver) ![Websocket](https://img.shields.io/badge/protocol-websocket-green)
 
### Easily stream real-time sensor data from your phone to multiple WebSocket clients, allowing them to monitor the device's movement, environment, and position in real-time.

<img src="https://github.com/umer0586/SensorServer/blob/main/fastlane/metadata/android/en-US/images/phoneScreenshots/01.png" width="250" heigth="250"> <img src="https://github.com/umer0586/SensorServer/blob/main/fastlane/metadata/android/en-US/images/phoneScreenshots/02.png" width="250" heigth="250"> <img src="https://github.com/umer0586/SensorServer/blob/main/fastlane/metadata/android/en-US/images/phoneScreenshots/03.png" width="250" heigth="250"> <img src="https://github.com/umer0586/SensorServer/blob/main/fastlane/metadata/android/en-US/images/phoneScreenshots/04.png" width="250" heigth="250"> <img src="https://github.com/umer0586/SensorServer/blob/main/fastlane/metadata/android/en-US/images/phoneScreenshots/05.png" width="250" heigth="250"> <img src="https://github.com/umer0586/SensorServer/blob/main/fastlane/metadata/android/en-US/images/phoneScreenshots/06.png" width="250" heigth="250">
<img src="https://github.com/umer0586/SensorServer/blob/main/fastlane/metadata/android/en-US/images/phoneScreenshots/07.png" width="250" heigth="250">





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

Multiple WebSocket clients can connect to a specific type of sensor. For example, by connecting to `/sensor/connect?type=android.sensor.accelerometer` multiple times, separate connections to the accelerometer sensor are created. Each connected client will receive accelerometer data simultaneously.

Additionally, it is possible to connect to different types of sensors from either the same or different machines. For instance, one WebSocket client object can connect to the accelerometer, while another WebSocket client object can connect to the gyroscope. To view all active connections, you can select the "Connections" navigation button.
 
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
    print("x = ", x , "y = ", y , "z = ", z )

def on_error(ws, error):
    print("error occurred ", error)
    
def on_close(ws, close_code, reason):
    print("connection closed : ", reason)
    
def on_open(ws):
    print("connected")
    

def connect(url):
    ws = websocket.WebSocketApp(url,
                              on_open=on_open,
                              on_message=on_message,
                              on_error=on_error,
                              on_close=on_close)

    ws.run_forever()
 
 
connect("ws://192.168.0.103:8080/sensor/connect?type=android.sensor.accelerometer") 

```
 *Your device's IP might be different when you tap start button, so make sure you are using correct IP address at client side*
 
## Using Multiple Sensors Over single Websocket Connection
You can also connect to multiple sensors over single websocket connection. To use multiple sensors over single websocket connection use following **URL**.

                 ws://<ip>:<port>/sensors/connect?types=["<type1>","<type2>","<type3>"...]

By connecting using above URL you will receive JSON response containing sensor data along with a type of sensor. See complete example at [Using Multiple Sensors On Single Websocket Connection](https://github.com/umer0586/SensorServer/wiki/Using-Multiple-Sensors-On-Single-Websocket-Connection). Avoid connecting too many sensors over single connection

## Reading Touch Screen Data
By connecting to the address `ws://<ip>:<port>/touchscreen`, clients can receive touch screen events in following JSON formate.

|   Key   |   Value                  |
|:-------:|:-----------------------:|
|   x     |         x coordinate of touch           |
|   y     |         y coordinate of touch          |
| action  | ACTION_MOVE or ACTION_UP or ACTION_DOWN |

"ACTION_DOWN" indicates that a user has touched the screen.
"ACTION_UP" means the user has removed their finger from the screen.
"ACTION_MOVE" implies the user is sliding their finger across the screen.
See [Controlling Mouse Movement Using SensorServer app](https://github.com/umer0586/SensorServer/wiki/Controlling-Mouse-Using-SensorServer-App)

## Getting Device Location Using GPS
You can access device location through GPS using following URL.

                 ws://<ip>:<port>/gps
                 

JSON response contains following key fields.

| Field       | Description                                                                                                            |
|-------------|------------------------------------------------------------------------------------------------------------------------|
| longitude   | Longitude in degrees.                                                                                                 |
| latitude    | Latitude in degrees.                                                                                                  |
| altitude    | The altitude of the location in meters above the WGS84 reference ellipsoid.                                         |
| bearing     | Bearing at the time of this location in degrees. Bearing is the horizontal direction of travel and is unrelated to device orientation. The bearing is guaranteed to be in the range \[0, 360). |
| accuracy    | Estimated horizontal accuracy radius in meters of this location at the 68th percentile confidence level.           |
| speed       | Speed at the time of this location in meters per second.                                                            |
| time        | The Unix epoch time of this location fix, in milliseconds since the start of the Unix epoch (00:00:00 January 1, 1970 UTC). |

Fields only for Android 8.0 and above.

| Field                     | Description                                                                                                     |
|---------------------------|-----------------------------------------------------------------------------------------------------------------|
| speedAccuracyMetersPerSecond | Estimated speed accuracy in meters per second of this location at the 68th percentile confidence level.     |
| bearingAccuracyDegrees    | Estimated bearing accuracy in degrees of this location at the 68th percentile confidence level.                |
| elapsedRealtimeNanos      | Time of this fix in nanoseconds of elapsed realtime since system boot.                                        |
| verticalAccuracyMeters    | The estimated altitude accuracy in meters of this location at the 68th percentile confidence level.          |



## Real Time plotting
See [Real Time Plot of Accelerometer (Python)](https://github.com/umer0586/SensorServer/wiki/Real-Time-Plot-Example-(-Python)) using this app

![result](https://user-images.githubusercontent.com/35717992/208961337-0f69757e-e85b-4637-8c39-fa5554d85921.gif)



https://github.com/umer0586/SensorServer/assets/35717992/2ebf865d-529e-4702-8254-347df98dc795

## Testing in a Web Browser
You can also view your phone's sensor data in a Web Browser. Open the app's navigation drawer menu and enable `Test in a Web Browser`.Once the web server is running, the app will display an address on your screen. This address will look something like `http://<ip>:<port>`.On your device or another computer on the same network, open a web browser and enter that address. The web browser will now display a list of all the sensors available on your device. The web interface have options to connect to and disconnect from individual sensors, allowing you to view their real-time data readings.

<img width="747" alt="web-browser-image" src="https://github.com/umer0586/SensorServer/assets/35717992/e4bbbe70-17d3-4246-b714-184f832f1ab4">


This web app is built using Flutter and its source could be found under [sensors_dashboard](https://github.com/umer0586/SensorServer/tree/main/sensors_dashboard). However, there's one current limitation to be aware of. The app is built with Flutter using the `--web-renderer canvaskit` option. This means that the resulting app will have some dependencies that need to be downloaded from the internet. This means that any device accessing the web app through a browser will require an internet connection to function properly.


## Connecting over Hotspot :fire:
If you don't have Wifi network at your work place you can directly connect websocket clients to the app by enabling **Hotspot Option** from settings. Just make sure that websocket clients are connected to your phone's hotspot


## Connecting over USB (using ADB)
To connect over USB make sure `USB debugging` option is enable in your phone and `ADB` (android debug bridge) is available in your machine
* **Step 1 :** Enable `Local Host` option in app
* **Step 2** : Run adb command `adb forward tcp:8081 tcp:8081` (8081 is just for example) from client
* **Step 3** : use address `ws://localhost:8081:/sensor/connect?type=<sensor type here>` to connect 

Make sure you have installed your android device driver and `adb devices` command detects your connected android phone.


# Installation

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
    alt="Get it on F-Droid"
    height="100">](https://f-droid.org/packages/github.umer0586.sensorserver)
    
OR

Download latest *APK* from Github's [Release page](https://github.com/umer0586/SensorServer/releases) ![GitHub all releases](https://img.shields.io/github/downloads/umer0586/SensorServer/total?label=GitHub%20downloads)

*(requires Android 5.0 or above)* . 

## Found this useful
<a href="https://www.buymeacoffee.com/umerfarooq" target="_blank"><img src="https://cdn.buymeacoffee.com/buttons/v2/default-yellow.png" alt="Buy Me A Coffee" style="height: 60px !important;width: 217px !important;" ></a>

Send Bitcoin at 1NHkiJmjUdjqbCKJf6ZksGKMvYu52Q5tew 

OR

Scan following QR code with bitcoin wallet app to send bitcoins

[<img src="https://github.com/umer0586/SensorServer/assets/35717992/11cd8194-6e9c-469c-a09f-06fd1bc93acd" height="200">](https://github.com/umer0586/SensorServer/assets/35717992/11cd8194-6e9c-469c-a09f-06fd1bc93acd)


