import 'dart:async';
import 'dart:convert';
import 'dart:developer' as dev;
import 'dart:math';

import 'package:fl_chart/fl_chart.dart';
import 'package:flutter/foundation.dart';
import 'package:get_it/get_it.dart';

import 'package:sensors_dashboard/model/repository/info_repository.dart';
import 'package:sensors_dashboard/model/sensor.dart';
import 'package:sensors_dashboard/model/server_info.dart';
import 'package:web/web.dart' as web;

class SensorGraphViewmodel with ChangeNotifier {
  bool _connected = false;
  bool get isConnected => _connected;

  bool _connecting = false;
  bool get isConnecting => _connecting;

  double _maxY = 5;
  double get maxY => _maxY;

  double get minY => -1 * maxY;

  final List<FlSpot> _xData = [];
  List<FlSpot> get xData => _xData;

  final List<FlSpot> _yData = [];
  List<FlSpot> get yData => _yData;

  final List<FlSpot> _zData = [];
  List<FlSpot> get zData => _zData;

  var _iterations = 0;

  // limit of x,y and z Data list
  static const dataLimit = 100;

  static const connectionTimeOutSecs = 2;

  // A url for testing connections without deploying app to Android
  final testUrl = "ws://192.168.18.3:8080/sensor/connect";

  web.WebSocket? _websocket;

  void connect(Sensor sensor) async {
    _setConnecting(true);

    Timer(const Duration(seconds: connectionTimeOutSecs), () {
      if (!isConnected) {
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

      dev.log(
          "getting websocket port from http://${serverInfo.address}/wsport");
      final webSocketPort = await InfoRepository().getWebSocketPortNo();
      final webSocketServerAddress = "ws://${serverInfo.iP}:$webSocketPort";

      _websocket = web.WebSocket(
          "$webSocketServerAddress/sensor/connect?type=${sensor.type}");
    }

    _websocket?.onOpen.listen((event) {
      _setConnected(true);
    });

    _websocket?.onMessage.listen((messageEvent) {
      if (xData.length > dataLimit) {
        _xData.removeAt(0);
        _yData.removeAt(0);
        _zData.removeAt(0);
      }

      final data = messageEvent.data.toString();
      final json = jsonDecode(data);

      // TODO : unable to read read json array into List<Double>
      List<double> values = json["values"]
          .toString()
          .replaceAll("[", "")
          .replaceAll("]", "")
          .split(",")
          .map((e) => double.parse(e))
          .toList();

      final timestamp = double.parse(json["timestamp"].toString());

      _xData.add(FlSpot(timestamp, values[0]));
      _yData.add(FlSpot(timestamp, values[1]));
      _zData.add(FlSpot(timestamp, values[2]));

      // set max value for Y axis to max of x,y and z
      final maxValue = [values[0], values[1], values[2]].reduce(max) + 3;
      // This ensures that the Y-axis maximum always accommodates the largest data point
      _maxY = maxValue > _maxY ? maxValue : _maxY;

      // This block introduces a mechanism to adjust _maxY downwards after a certain number of iterations.
      // This could be useful if the data being displayed is trending downwards and you want the Y-axis to "zoom in" to better represent the current range of values.
      if (_iterations > dataLimit) {
        _maxY = maxValue < _maxY ? maxValue : _maxY;
        _iterations = 0;
      }

      _iterations++;

      if (_xData.isNotEmpty) {
        notifyListeners();
      }

    });

    _websocket?.onClose.listen((closeEvent) {
      _setConnected(false);
    });

    _websocket?.onError.listen((event) {
      _setConnected(false);
    });
  }

  void disconnect() async {
    _websocket?.close();
  }

  void _setConnected(bool connected) {
    _setConnecting(false);

    if (_connected == connected) {
      return;
    }

    _connected = connected;
    notifyListeners();
  }

  void _setConnecting(bool connecting) {
    if (_connecting == connecting) {
      return;
    }

    _connecting = connecting;
    notifyListeners();
  }

  @override
  void dispose() {
    super.dispose();
    _websocket?.close();
  }
}
