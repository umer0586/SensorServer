import 'package:fl_chart/fl_chart.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:sensors_dashboard/view_model/sensor_graph_viewmodel.dart';

import '../../model/sensor.dart';

class SensorGraphWidget extends StatelessWidget {
  final Sensor sensor;
  const SensorGraphWidget(this.sensor, {super.key});

  @override
  Widget build(BuildContext context) {
    //debugPrint("GraphScreen()");

    final viewModel = Provider.of<SensorGraphViewmodel>(context);
    const xDataColor = Colors.green;
    const yDataColor = Colors.blue;
    const zDataColor = Colors.red;

    return  Scaffold(
      appBar: AppBar(
        title: Text(sensor.name),
        leading: IconButton(icon: const Icon(Icons.arrow_back_rounded), onPressed: (){

          Navigator.of(context).pop();

        },),
        centerTitle: true,

        actions: [
          TextButton(onPressed: (){
            if(viewModel.isConnected){
              viewModel.disconnect();
            }else{
              viewModel.connect(sensor);
            }
          }, child: !viewModel.isConnected ? const Text("Connect") : const Text("Disconnect")),

        ],
      ),
      body: Stack(
        children: [
          const Positioned(
            left: 100,
            top: 50,
            child: Column(
              children: [
                Text("X", style: TextStyle(color: xDataColor),),
                Text("Y", style: TextStyle(color: yDataColor),),
                Text("Z", style: TextStyle(color: zDataColor),),
              ],
            ),
          ),
          Align(
            alignment: Alignment.center,
            child: Padding(
              padding: const EdgeInsets.all(8.0),
              child: LineChart(
                  duration: const Duration(milliseconds: 1),

                  LineChartData(
                      gridData: const FlGridData(show: false),
                      borderData: FlBorderData(show: false),
                      titlesData: const FlTitlesData(
                          bottomTitles: AxisTitles(sideTitles: SideTitles(showTitles: false)),
                          topTitles: AxisTitles(sideTitles: SideTitles(showTitles: false))
                      ),
                      lineTouchData: const LineTouchData(enabled: false),
                      minY: viewModel.minY,
                      maxY: viewModel.maxY,
                      lineBarsData: [

                        lineChartBarData(viewModel.xData , xDataColor),
                        lineChartBarData(viewModel.yData, yDataColor),
                        lineChartBarData(viewModel.zData, zDataColor),

                      ]
                  )
              ),
            ),
          ),
        ]
      ),
    );
  }

  LineChartBarData lineChartBarData(List<FlSpot> spotData , [Color color = Colors.red]){
    return LineChartBarData(
      color: color,
      spots: spotData,
        dotData: const FlDotData(
          show: false,
        ),
      //curveSmoothness: 2,
      isCurved: true
    );
  }

}
