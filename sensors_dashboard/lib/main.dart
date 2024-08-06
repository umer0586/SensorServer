import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:get_it/get_it.dart';
import 'package:provider/provider.dart';
import 'package:sensors_dashboard/model/data/server_info.dart';
import 'package:sensors_dashboard/model/repository/info_repository.dart';
import 'package:sensors_dashboard/model/repository/sensors_repository.dart';
import 'package:sensors_dashboard/model/repository_impl/sensors_repository_impl.dart';
import 'package:sensors_dashboard/view/screens/sensor_graph_screen.dart';
import 'package:sensors_dashboard/view/screens/sensors_screen.dart';
import 'package:sensors_dashboard/view_model/sensor_graph_screen_viewmodel.dart';
import 'package:sensors_dashboard/view_model/sensors_screen_viewmodel.dart';

import 'model/repository_impl/info_repository_imp.dart';

void main() {
  setUpDependencies();
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {

    return MultiProvider(
      providers: [
        ChangeNotifierProvider(
            create: (context) => GetIt.instance.get<SensorsScreenViewmodel>(),
          child: const SensorsScreen(),
        ),
        ChangeNotifierProvider(
            create: (context) => GetIt.instance.get<SensorGraphScreenViewmodel>(),
          child: const SensorGraphScreen(),
        )
      ],

      child: MaterialApp(
          debugShowCheckedModeBanner: false,
          title: 'Sensors Dashboard',
          initialRoute: SensorsScreen.routeName,
          routes: {
            SensorsScreen.routeName : (context) => const SensorsScreen(),
            SensorGraphScreen.routeName : (context) => const SensorGraphScreen()
          },
          theme: ThemeData(
            colorScheme: ColorScheme.fromSeed(
                seedColor: Colors.teal),
            useMaterial3: true,
          ),
      ),
    );
  }
}

void setUpDependencies(){

  if(kDebugMode){
    GetIt.instance.registerSingleton<ServerInfo>(ServerInfo(testIp: "192.168.18.8", testPortNo: 8081));
  }
  else if(kReleaseMode){
    GetIt.instance.registerSingleton<ServerInfo>(ServerInfo(deployed: true));
  }

  final serverInfo = GetIt.instance.get<ServerInfo>();
  GetIt.instance.registerSingleton<SensorsRepository>(SensorsRepositoryImpl(serverInfo: serverInfo));
  GetIt.instance.registerSingleton<InfoRepository>(InfoRepositoryImpl(serverInfo: serverInfo));

  //viewModel dependencies
  GetIt.instance.registerSingleton<SensorsScreenViewmodel>(
      SensorsScreenViewmodel(sensorsRepository: GetIt.instance.get<SensorsRepository>())
  );

  GetIt.instance.registerSingleton<SensorGraphScreenViewmodel>(
      SensorGraphScreenViewmodel(infoRepository: GetIt.instance.get<InfoRepository>(), serverInfo: serverInfo)
  );


}
