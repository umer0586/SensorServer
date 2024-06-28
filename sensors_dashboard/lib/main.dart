import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:sensors_dashboard/model/server_info.dart';
import 'package:sensors_dashboard/view/screens/sensors_screen.dart';
import 'package:sensors_dashboard/view_model/sensors_screen_viewmodel.dart';
import 'package:get_it/get_it.dart';

void main() {
  if(kDebugMode){
    GetIt.instance.registerSingleton<ServerInfo>(ServerInfo());
  }
  else if(kReleaseMode){
    GetIt.instance.registerSingleton<ServerInfo>(ServerInfo(deployed: true));
  }
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
        debugShowCheckedModeBanner: false,
        title: 'Sensors Dashboard',
        theme: ThemeData(
          colorScheme: ColorScheme.fromSeed(
              seedColor: Colors.teal),
          useMaterial3: true,
        ),
        home: ChangeNotifierProvider(
            create: (context) => SensorsScreenViewmodel(),
            child: SensorsScreen()));
  }
}
