import 'package:web/web.dart' as web;
class ServerInfo {

  bool deployed;

  String testIp;
  int testPortNo;


  String get address{
    final location = web.window.location;
    return switch(deployed){
      true => "${location.hostname}:${location.port}",
      false => "$testIp:$testPortNo", 
    };
  }

  String get iP{
    return switch(deployed){
      true => web.window.location.hostname,
      false => testIp,
    };
  }

  String get sensorsUrl => "http://$address/sensors";
  String get websocketPortUrl => "http://$address/wsport";
  String get sensorConnectionUrl => "ws://$address/sensor/connect";

  ServerInfo({this.deployed = false, this.testIp = "127.0.0.1", this.testPortNo = 8080});


}