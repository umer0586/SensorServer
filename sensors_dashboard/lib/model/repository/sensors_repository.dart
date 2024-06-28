import 'dart:convert';
import 'dart:developer';

import 'package:get_it/get_it.dart';
import 'package:sensors_dashboard/model/sensor.dart';
import 'package:http/http.dart' as http;
import 'package:sensors_dashboard/model/server_info.dart';

class SensorsRepository {
  

  Future<List<Sensor>> getSensors() async {
    try {

      final serverInfo = GetIt.instance.get<ServerInfo>();
      final url = "http://${serverInfo.address}/sensors";
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
