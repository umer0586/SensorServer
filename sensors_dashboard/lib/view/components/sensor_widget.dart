
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:sensors_dashboard/model/sensor.dart';
import 'package:sensors_dashboard/view_model/sensor_viewmodel.dart';

/// A widget which represents sensor of Android device
class SensorWidget extends StatelessWidget {
  final Sensor sensor;
  const SensorWidget(this.sensor,{super.key});


  @override
  Widget build(BuildContext context) {

    final viewModel = Provider.of<SensorViewModel>(context);
    final colorScheme = Theme.of(context).colorScheme;
    final textTheme = Theme.of(context).textTheme;
    
        
    return Card(
      elevation: 15,
      shadowColor: colorScheme.secondary,
      child: Container(
        decoration: BoxDecoration(
            color: colorScheme.secondaryContainer,
            borderRadius: const BorderRadius.only(topLeft: Radius.circular(30),topRight:  Radius.circular(30) )),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.center,
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Expanded(
              flex: 2,
                child: Center(
              child: Container(
                width: double.infinity,
                height: double.infinity,
                alignment: Alignment.center,
                decoration: BoxDecoration(
                    borderRadius: BorderRadius.circular(10),
                    color: colorScheme.inversePrimary),
                child: FittedBox(
                  child: Text(
                    sensor.name,
                    style: TextStyle(fontSize: textTheme.headlineSmall?.fontSize),
                  ),
                ),
              ),
            )),
          
              Expanded(
                flex: 6,
                  child: Center(
                child: viewModel.isConnecting ? const CircularProgressIndicator()
                    : StreamBuilder<String>(
                  stream: viewModel.sensorDataStream,
                  builder: (context, snapshot){

                    if(snapshot.hasData && snapshot.data != null){
                      return Text(snapshot.data.toString());
                    }

                    return const Text("No data");
                  },
                )
              )),
        
            Expanded(
              flex: 2,
              child: Padding(
                padding: const EdgeInsets.all(5),
                child: Row(
                  children: [
                    Expanded(
                      child: SizedBox(
                        width: double.infinity,
                        height: double.infinity,
                        child: _Button(
                          "Connect",
                          disabled: viewModel.isConnected,
                          onPressed: (){
                            viewModel.connect(sensor);
                          }),
                        ),
                    ),
              
                    const SizedBox(width: 10,),
                  
                     Expanded(
                      child: SizedBox(
                        width: double.infinity,
                        height: double.infinity,
                        child: _Button(
                          "Disconnect",
                          disabled: !viewModel.isConnected,
                          onPressed: (){
                            viewModel.disconnect();
                          }),
                        ),
                    ),
                  ],
                ),
              ),
            )
          ],
        ),
      ),
    );
  }
}

class _Button extends StatelessWidget {
  
  final bool disabled;
  final String text;
  final VoidCallback onPressed;
  const _Button(this.text, {super.key, this.disabled = false, required this.onPressed});

 
  @override
  Widget build(BuildContext context) {

     final buttonStyle = ElevatedButton.styleFrom(
        shape: RoundedRectangleBorder(
      borderRadius: BorderRadius.circular(10),
    ));

    return ElevatedButton(
      style: buttonStyle,
      onPressed: disabled ? null : onPressed, // Button becomes disable with onPressed = null
       child: FittedBox(child: Text(text)));
  }
}


