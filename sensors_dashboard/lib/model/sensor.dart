
class Sensor {
  
  final String name;
  final String type;
  
  const Sensor({required this.name, required this.type});

  factory Sensor.fromJson(Map<String,dynamic> json){
    return Sensor(name: json["name"], type: json["type"]);
  }

  Map<String,dynamic> toJson(){
    return {
      "name" : name,
      "type" : type
    };
  }

}