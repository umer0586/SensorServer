
import '../data/sensor.dart';

abstract class SensorsRepository {

  Future<List<Sensor>> getSensors();

}