import 'dart:convert';

import 'package:get_it/get_it.dart';
import 'package:sensors_dashboard/model/server_info.dart';
import "package:http/http.dart" as http;

class InfoRepository {
  Future<int> getWebSocketPortNo() async {
    try {
      final serverInfo = GetIt.instance.get<ServerInfo>();
      final url = "http://${serverInfo.address}/wsport";

      final response = await http.get(Uri.parse(url));
      final json = jsonDecode(response.body);

      return json["portNo"];
    } on Exception catch (_) {
      return Future.error(Exception("Failed to get websocket portNo"));      
    }
  }
}
