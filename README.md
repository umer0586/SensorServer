# SensorServer
Android app which streams phone's motion, environmental and position sensors to **Websocket** clients over Wi-fi or USB.
 

![server](https://user-images.githubusercontent.com/35717992/146649500-f4f1aadf-60e0-4305-81bc-f7db21540bd7.gif)    ![connections](https://user-images.githubusercontent.com/35717992/146649573-9b86ff77-565c-46ef-900b-63350f4eac3b.gif)    ![sensors](https://user-images.githubusercontent.com/35717992/146649578-adb5f0eb-4a7a-462a-9e16-264f4599903f.gif)






 This app can stream phone's sensors (motion, environmental and position) to Websocket clients in realtime. A websocket client could be a web browser or any application running on a PC or a mobile device which uses **Websocket Client API**.  You can easily get a **Websocket library** for your favourite programming language. 
 
 
 
 
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
 
 A snapshot from accelerometer.
 
**Note** : *Use [this page](https://developer.android.com/guide/topics/sensors/sensors_motion) to know what each value in **values** array corresponds to*  
 
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

## Undocumented (mostly QTI) sensors on Android devices
Some Android devices have additional sensors like **Coarse Motion Classifier** `(com.qti.sensor.motion_classifier)`, **Basic Gesture** `(com.qti.sensor.basic_gestures)` etc  which are not documented on offical android docs. Please refer to this [Blog](https://louis993546.medium.com/quick-tech-support-undocumented-mostly-qti-sensors-on-android-devices-d7e2fb6c5064) for corresponding values in `values` array  

## Supports multiple connections to multiple sensors simultaneously

 **Many Websocket clients** can connect to one `type` of a Sensor. So connecting to **`/sensor/connect?type=android.sensor.accelerometer`** three times will create three different connections to the accelerometer sensor and each connected client will then receive accelerometer data at the same time.
 
Moreover, from one or different machines you can connect to different types of sensors as well i-e one **Websocket Client object** could connect to accelerometer and other **Websocket Client object** to gyroscope. All active connections can be viewed by selecting **Connections** navigation button.
 
## Test with Websocket testing tools 
Before writing your own websocket client, test this app with any websocket testing tools available on the web or playstore. You can use http://livepersoninc.github.io/ws-test-page/ for testing purpose.

## Sample Websocket client (python) 
Here is a simple websocket client in python using [websocket-client api](https://github.com/websocket-client/websocket-client) which receives live data from accelerometer sensor.

```python
import websocket
import json

def on_message(ws, message):
    values = json.loads(message)['values']
    x = values[0]
    y = values[1]
    z = values[2]
    print('x =',x,'y = ',y,'z = ',z)

def on_error(ws, error):
    print("error occurred")
    print(error)

def on_close(ws, close_code, reason):
    print("connection close")
    print("close code : ", close_code)
    print("reason : ", reason  )

def on_open(ws):
    print("connection open")
    

if __name__ == "__main__":
    ws = websocket.WebSocketApp("ws://192.168.0.101:8081/sensor/connect?type=android.sensor.accelerometer",
                              on_open=on_open,
                              on_message=on_message,
                              on_error=on_error,
                              on_close=on_close)

    ws.run_forever()
 
```
There is another python websocket API which is based on `asyncio` [https://github.com/aaugustin/websockets](https://github.com/aaugustin/websockets)

```python
import asyncio
from websockets import connect

async def accelerometer(uri):
    async with connect(uri) as websocket:
        while True:
            data = await websocket.recv()
            print(data)
            
URI = "ws://192.168.0.101:8081/sensor/connect?type=android.sensor.accelerometer"
asyncio.run(accelerometer(URI))

```


## Connecting over USB (using ADB)
To connect over USB make sure `USB debugging` option is enable in your phone and `ADB` (android debug bridge) is available in your machine
* **Step 1 :** Enable `Local Host` option in app
* **Step 2** : Run adb command `adb forward tcp:8081 tcp:8081` (8081 is just for example) from client
* **Step 3** : use address `ws://localhost:8081:/sensor/connect?type=<sensor type here>` to connect 

Make sure you have installed your android device driver and `adb devices` command detects your connected android phone.

# APK Download ⏬
Download latest *APK* from [Release page](https://github.com/umer0586/SensorServer/releases) *(requires Android 5.0)* .

# Issues and Contributions
Post a new issue if you encounter any. To contribute, fork this repo then create feature/fix branch and send pull request for that branch.
