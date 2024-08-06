
import 'dart:async';
import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:sensors_dashboard/model/data/sensor.dart';
import 'package:sensors_dashboard/model/data/server_info.dart';
import 'package:sensors_dashboard/model/repository/info_repository.dart';
import 'package:web/web.dart' as web;

/// A ViewModel for SensorWiget
class SensorViewModel with ChangeNotifier{
  
  bool _connected = false;
  bool get isConnected => _connected;

  bool _connecting = false;
  bool get isConnecting => _connecting;


  static const connectionTimeOutSecs = 2;

  final StreamController<String> _sensorDataStreamController = StreamController.broadcast();
  Stream<String> get sensorDataStream => _sensorDataStreamController.stream;

  web.WebSocket? _websocket;

  final InfoRepository infoRepository;
  final ServerInfo serverInfo;

  SensorViewModel({required this.infoRepository, required this.serverInfo});
 
  void connect(Sensor sensor) async {

    _setConnecting(true);
  

    Timer(const Duration(seconds: connectionTimeOutSecs), () {
      if (!isConnected) {
        _sensorDataStreamController.add("connection timed out after $connectionTimeOutSecs");
        _websocket?.close();
        _setConnected(false);

      }
    });


    _websocket = web.WebSocket("${serverInfo.sensorConnectionUrl}?type=${sensor.type}");


    _websocket?.onOpen.listen((event){
      _setConnected(true);
    });

    _websocket?.onMessage.listen((messageEvent){

      final data = messageEvent.data;
      if (data != null) {
        final json = jsonDecode(data.toString());
        _sensorDataStreamController.add(json["values"].toString());
      }
      
    });

    _websocket?.onClose.listen((closeEvent) {
      _setConnected(false);
      _sensorDataStreamController.add("No Data");
    });

    _websocket?.onError.listen((event){
      _setConnected(false);
      _sensorDataStreamController.addError("Error occurred \n $event");
    
    });

    
  }

  void disconnect() async {
    _websocket?.close();
  }



  void _setConnected(bool connected){
    
    _setConnecting(false);

    if(_connected == connected){
      return; 
    }

    _connected = connected;
    notifyListeners();
 
  }

  void _setConnecting(bool connecting){
    
    if(_connecting == connecting){
      return;
    }

    _connecting = connecting;
    notifyListeners();
  }

@override
  void dispose() {
    super.dispose();
    _websocket?.close();

    if(!_sensorDataStreamController.isClosed){
      _sensorDataStreamController.close();
    }

  }

}