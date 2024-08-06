import 'dart:convert';
import 'dart:developer';

import "package:http/http.dart" as http;
import 'package:sensors_dashboard/model/data/server_info.dart';
import 'package:sensors_dashboard/model/repository/info_repository.dart';

class InfoRepositoryImpl extends InfoRepository{

  final ServerInfo serverInfo;
  InfoRepositoryImpl({required this.serverInfo});

  @override
  Future<int> getWebSocketPortNo() async {
    try {

      final url = serverInfo.websocketPortUrl;
      log("Fetching websocket port from $url");

      final response = await http.get(Uri.parse(url));
      final json = jsonDecode(response.body);

      return json["portNo"];
    } on Exception catch (_) {
      return Future.error(Exception("Failed to get websocket portNo"));      
    }
  }
}
