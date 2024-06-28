import 'dart:convert';
import 'dart:io';
import '../lib/model/sensor.dart';

const int port = 8080;
const sensorsList = [
  Sensor(name: "Accelerometer", type: "android.sensor.accelerometer"),
  Sensor(name: "Gyroscope", type: "android.sensor.gyroscope"),
  Sensor(name: "Light", type: "android.sensor.light"),
  Sensor(name: "Gravity", type: "android.sensor.gravity"),
  Sensor(name: "Linear Acceleration", type: "android.sensor.linear_acceleration"),
  Sensor(name: "Rotation Vector", type: "android.sensor.rotation_vector"),
  Sensor(name: "Orientation", type: "android.sensor.orientation"),
];

void main(List<String> args) async {

  // Bind the server to the port
  HttpServer server = await HttpServer.bind(InternetAddress.loopbackIPv4,port);

  print('Server started : ${server.address.address}:${server.port}');

  // Listen for incoming requests
  server.listen((HttpRequest request) async {
    request.response.headers.set("Access-Control-Allow-Origin", "*");
    request.response.headers.set("Access-Control-Allow-Methods", "GET,PUT,PATCH,POST,DELETE");

    print("request : ${request.requestedUri.path}");
    // Check if it's a GET request

    if (request.method == 'GET') {
      // Get the requested path
      final String path = request.uri.path;
      switch(path){
        case "/sensors":
        handleSensorsRequest(request);
        break;
        case "/wsport":
        handlePortRequest(request);
        break;
      }

    } else {
      // Handle other methods (optional)
      request.response.statusCode = HttpStatus.methodNotAllowed;
      await request.response.close();
    }
  });



}

  void handleSensorsRequest(HttpRequest request) async {

    final List<Map<String, dynamic>> jsonList = [];
        for (var sensor in sensorsList) {
          jsonList.add(sensor.toJson());
        }

        request.response
          ..statusCode = HttpStatus.ok
          ..write(jsonEncode(jsonList));

        await request.response.close();

  }

  void handlePortRequest(HttpRequest request) async {
    final response = {"portNo": 8081};
        request.response
          ..statusCode = HttpStatus.ok
          ..write(jsonEncode(response));

        await request.response.close();
  }
