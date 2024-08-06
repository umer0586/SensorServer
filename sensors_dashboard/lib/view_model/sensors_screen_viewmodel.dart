
import 'package:flutter/foundation.dart';
import 'package:sensors_dashboard/model/data/sensor.dart';
import 'package:sensors_dashboard/model/repository/sensors_repository.dart';

class SensorsScreenViewmodel with ChangeNotifier{

  List<Sensor> _sensors = [];
  List<Sensor> get sensors => _sensors;

  final SensorsRepository sensorsRepository;
  SensorsScreenViewmodel({required this.sensorsRepository});

  void fetchSensors(){

    if(kReleaseMode) {
      sensorsRepository.getSensors().then((sensors) {
        _sensors = sensors;
      });
    }

  }

  Future<List<Sensor>> getSensors() => sensorsRepository.getSensors();
 
  
}