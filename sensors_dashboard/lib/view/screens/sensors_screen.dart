import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:sensors_dashboard/model/sensor.dart';
import 'package:sensors_dashboard/view/components/sensor_widget.dart';
import 'package:sensors_dashboard/view_model/sensor_viewmodel.dart';
import 'package:sensors_dashboard/view_model/sensors_screen_viewmodel.dart';

class SensorsScreen extends StatelessWidget {
  const SensorsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    //debugPrint("SensorScreen()");

    final viewModel = Provider.of<SensorsScreenViewmodel>(context);
    
    return Scaffold(
        appBar: AppBar(
          automaticallyImplyLeading: false,
          title: const Text("Sensors"),
          centerTitle: true,
        ),
        body: SafeArea(
          child: Center(
            child: FutureBuilder<List<Sensor>>(
              future: viewModel.getSensors(),
              builder: (context, snapshot) {
                if (snapshot.connectionState == ConnectionState.waiting) {
                  return const CircularProgressIndicator();
                }
                if (snapshot.hasData) {
                  final sensors = snapshot.data;
            
                  if (sensors != null) {
                    return Padding(
                      padding: const EdgeInsets.symmetric(horizontal: 30),
                      child: _SensorGrid(sensors: sensors,)
                    );
                  }
                }
            
                if(snapshot.hasError){
                  return const Text("Failed to Load sensors");
                }
            
                return const Text("No data");
              },
            ),
          ),
        ));
  }
}

class _SensorGrid extends StatelessWidget {
  final List<Sensor> sensors;
  const _SensorGrid({super.key, required this.sensors});

  @override
  Widget build(BuildContext context) {
    final screenWidth = MediaQuery.of(context).size.width;
    return GridView.builder(
      // Normally, Gridview/Listview remove items that are no longer visible to save memory.
      // We need to change this behavior because each SensorWidget have connection to a server.
      // If SensorWiget gets removed while scrolling, it will be dispose and the connection is lost.
      // Setting cacheExtent to a high value tells the view to keep more items in memory even if they're not visible.
      // the widget will be dispose and connection will be lost
      cacheExtent: double.maxFinite,
      itemCount: sensors.length,
      itemBuilder: (context, index) {
        return ChangeNotifierProvider(
          // Handle each SensorWidget's state in SensorViewModel
          // Each SensorWidget should have separate websocket connection
          create: (context) => SensorViewModel(),
          child: SensorWidget(sensors[index]));
      },
      gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
          crossAxisCount: screenWidth <= 430 ? 1 : 3 ,
           crossAxisSpacing: 15, 
           mainAxisSpacing: 15),
    );
    
  }
}
