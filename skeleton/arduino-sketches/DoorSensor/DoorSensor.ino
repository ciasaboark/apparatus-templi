#include <Serial.h>

int sensorPin = 2;

void setup(){
  Serial.begin(9600);
  pinMode(sensorPin, INPUT);
  digitalWrite(sensorPin, HIGH);
}

void loop(){
  int magnetSensor = digitalRead(sensorPin);
  
  if(magnetSensor == LOW){
    Serial.println("Closed!");
  }
 
  if(magnetSensor == HIGH){
    Serial.println("Opened!");
  }
  
  delay(2000);
}
