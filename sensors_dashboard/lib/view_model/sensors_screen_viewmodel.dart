
import 'package:flutter/material.dart';
import 'package:sensors_dashboard/model/repository/sensors_repository.dart';
import 'package:sensors_dashboard/model/sensor.dart';

class SensorsScreenViewmodel with ChangeNotifier{

  List<Sensor> _sensors = [];
  List<Sensor> get sensors => _sensors;

  final repo = SensorsRepository();

  void fetechSensors(){
    repo.getSensors().then((sensors){
      _sensors = sensors;
    });
  }

  Future<List<Sensor>> getSensors() => repo.getSensors(); 
 
  
}