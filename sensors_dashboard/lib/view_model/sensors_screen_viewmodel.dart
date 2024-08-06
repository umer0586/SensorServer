
import 'package:flutter/foundation.dart';
import 'package:sensors_dashboard/model/data/sensor.dart';
import 'package:sensors_dashboard/model/repository/sensors_repository.dart';

class SensorsScreenViewmodel with ChangeNotifier{

  final SensorsRepository sensorsRepository;
  SensorsScreenViewmodel({required this.sensorsRepository});

  Future<List<Sensor>> getSensors() => sensorsRepository.getSensors();
 
  
}