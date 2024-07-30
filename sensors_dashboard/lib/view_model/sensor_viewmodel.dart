
import 'dart:async';
import 'dart:convert';
import 'dart:developer';

import 'package:flutter/foundation.dart';
import 'package:get_it/get_it.dart';

import 'package:sensors_dashboard/model/repository/info_repository.dart';
import 'package:sensors_dashboard/model/sensor.dart';
import 'package:sensors_dashboard/model/server_info.dart';
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

  // A url for testing connections without deploying app to Android
  final testUrl = "ws://192.168.18.3:8080/sensor/connect";

  web.WebSocket? _websocket;
 
  void connect(Sensor sensor) async {

    _setConnecting(true);
  

    Timer(const Duration(seconds: connectionTimeOutSecs), () {
      if (!isConnected) {
        _sensorDataStreamController.add("connection timed out after $connectionTimeOutSecs");
        _websocket?.close();
        _setConnected(false);

      }
    });

    // Use hardcode or testUrl in debug mode.
    if (kDebugMode) {
      _websocket = web.WebSocket("$testUrl?type=${sensor.type}");
    }
    // Get actual address when deployed to Android
    if (kReleaseMode) {
      final serverInfo = GetIt.instance.get<ServerInfo>();
      
      log("getting websocket port from http://${serverInfo.address}/wsport");
      final webSocketPort = await InfoRepository().getWebSocketPortNo();
      final webSocketServerAddress = "ws://${serverInfo.iP}:$webSocketPort";
      
      _websocket = web.WebSocket("$webSocketServerAddress/sensor/connect?type=${sensor.type}");
    }

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