import 'dart:convert';
import 'dart:developer';

import 'package:flutter/foundation.dart';
import 'package:http/http.dart' as http;
import 'package:sensors_dashboard/model/data/sensor.dart';
import 'package:sensors_dashboard/model/data/server_info.dart';
import 'package:sensors_dashboard/model/repository/sensors_repository.dart';

class SensorsRepositoryImpl extends SensorsRepository{
  final ServerInfo serverInfo;

  SensorsRepositoryImpl({required this.serverInfo});

  @override
  Future<List<Sensor>> getSensors() async {
    try {

      if(kDebugMode){
        if(kDebugMode){
          log("Loading fake list of sensors");
          return Future<List<Sensor>>.delayed(const Duration(seconds: 1),(){
            return const <Sensor>[
              Sensor(name: "Accelerometer", type: "android.sensor.accelerometer"),
              Sensor(name: "Gyroscope", type: "android.sensor.gyroscope"),
              Sensor(name: "Light", type: "android.sensor.light"),
              Sensor(name: "Gravity", type: "android.sensor.gravity"),
            ];
          });
        }
      }

      final url = serverInfo.sensorsUrl;
      log("fetching sensors from $url");
    
      
      final response = await http.get(Uri.parse(url));
      final sensorsJson = jsonDecode(response.body);

      final sensors = <Sensor>[];

      for(dynamic sensor in sensorsJson){
        sensors.add(Sensor.fromJson(sensor));
      }

      return sensors;
      

    } on Exception catch (_) {
      log("Failed to load sensors");      
      return Future.error(Exception("Unable to load sensors"));      
    
    }
  }
}
