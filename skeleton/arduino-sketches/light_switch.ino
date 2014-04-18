#include <Servo.h> 
 
Servo servo;    // create servo object to control a servo 
                // a maximum of eight servo objects can be created 
 
int pos = 0;    // variable to store the servo position 
int light_on_pin = 7;
int light_off_pin = 2;

int illuminate = 0;
 
void setup() { 
  Serial.begin(9600);
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
