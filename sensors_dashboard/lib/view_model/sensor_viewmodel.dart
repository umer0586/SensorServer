
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
  bool get connecting => _connecting;

  String _values = "";
  String get values => _values;

  bool _connectionFailed = false;
  bool get connectionFailed => _connectionFailed;

  String _closeMessage = "";
  String get closeMessage => _closeMessage;

  String _errorMessage = "";
  String get errorMessage => _errorMessage;

  bool _hasError = false;
  bool get hasError => _hasError;

  static const connectionTimeOutSecs = 2;

  // A url for testing connections without repolying app to Android
  final testUrl = "ws://192.168.0.103:8080/sensor/connect";

  web.WebSocket? _websocket;
 
  void connect(Sensor sensor) async {

    _setConnecting(true);
  

    Timer(const Duration(seconds: connectionTimeOutSecs), () {
      if (!isConnected) {
        log("connection timed out after $connectionTimeOutSecs");
        _websocket?.close();
        _setConnected(false);
        _setErrorMessage("Connection Timmed Out");
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
        _values = json["values"].toString();
        notifyListeners();
      }
      
    });

    _websocket?.onClose.listen((closeEvent){
      _setConnected(false);   
    });

    _websocket?.onError.listen((event){
      _setConnected(false);
      _setConnectionFailed(true);
     _setErrorMessage("Error Occurred on Connection");
    
    });

    
  }

  void disconnect(){
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

  void _setConnectionFailed(bool connectionFailed){
    if(_connectionFailed == connectionFailed){
      return;
    }

    _connectionFailed = connectionFailed;
    notifyListeners();
  }

void _setErrorMessage(String message){
  _errorMessage = message;
  _hasError = true;
  notifyListeners();
}

@override
  void dispose() {
    super.dispose();
    _websocket?.close();
  }

}