#include <XBee.h>
#include <SoftwareSerial.h>
#include <Zigbee.h>
#include <Servo.h> 

Servo servo;  


int illuminate = 0;
int pos = 0;  
  
int light_on_pin = 7;
int light_off_pin = 6;

Zigbee zigbee("light", 10, 11);
 
void setup() { 
  
  Serial.begin(115200);
  zigbee.start(); //defaults to 9600
  
  servo.attach(4); 
  servo.write(pos);  
  
  pinMode(light_on_pin, OUTPUT);
  digitalWrite(light_off_pin, HIGH);
  pinMode(light_off_pin, OUTPUT); //set the start state to off
} 
 
 
void loop() {  
  Message *message = zigbee.receiveMessage();
  uint8_t *data = message->getPayload();
  
  if(data != NULL && data[0] == 1 && pos != 180) {
    pos = 180;
    servo.write(pos);
    delay(1000);
    pinMode(light_on_pin, HIGH);
    pinMode(light_off_pin, LOW);
    Serial.print("turning on the light");
  }
  else if(data != NULL && data[0] == 0 && pos != 0) {
    pos = 0;
    servo.write(pos);
    delay(1000);
    pinMode(light_on_pin, LOW);
    pinMode(light_off_pin, HIGH);
    Serial.print("turning off the light");
  }
  else {
    Serial.print("bad command");
    //bad command
  }
  delete message; //free the memory occupied by message
  Serial.print("freeing the memory held by message");
}
