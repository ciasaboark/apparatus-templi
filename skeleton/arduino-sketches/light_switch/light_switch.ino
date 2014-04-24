#include <XBee.h>

#include <SoftwareSerial.h>
#include <Zigbee.h>
#include <Servo.h> 

Servo servo;  


int illuminate = 0;
int pos = 0;  
  
int light_on_pin = 7;
int light_off_pin = 6;

char name[]  = {"123456789"};
char name1[] = {"987654321"};

Zigbee zigbee1(name, 11, 10);
Zigbee zigbee2(name1, 2, 3);
 
void setup() { 
  
  Serial.begin(115200);
  zigbee1.start(); //defaults to 9600
  zigbee2.start();
  
  servo.attach(4); 
  servo.write(pos);  
  
  pinMode(light_on_pin, OUTPUT);
  digitalWrite(light_off_pin, HIGH);
  pinMode(light_off_pin, OUTPUT);
} 
 
 
void loop() {  
 if (Serial.available() > 0) {
   illuminate = Serial.read();
 }
 if(illuminate == 49 && pos != 180) {
   for(pos = 0; pos <= 180; pos++) {
     servo.write(pos);
   }
   delay(1000);
   digitalWrite(light_off_pin, LOW);
   digitalWrite(light_on_pin, HIGH);
   illuminate = -1;
 }
 
 if(illuminate == 48 && pos != 1) {
   for(pos = 180; pos >= 1; pos--) {
     servo.write(pos);
   }
   delay(1000);
   digitalWrite(light_off_pin, HIGH);
   digitalWrite(light_on_pin, LOW);
   illuminate = -1;
 }
}
