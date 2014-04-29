#include <XBee.h>
#include <SoftwareSerial.h>
#include <Zigbee.h>
#include <Servo.h> 

Servo servo;  

int pos = 0;  //pos should either be 0 or 180, never any value in between

Zigbee zigbee("light", 10, 11);
 
void setup() { 
  
  Serial.begin(115200);
  zigbee.start(); //defaults to 9600
  
  servo.attach(9); 
  servo.write(0);  
  
  pinMode(7, OUTPUT);
  pinMode(6, OUTPUT); //set the start state to off
  digitalWrite(6, HIGH);
} 
 
void loop() {  
  Message *message = zigbee.receiveMessage();
  uint8_t *data = message->getPayload();
  
  if(message != NULL) {
    if(data[0] == 49) { //turn the light on
      pos = 180;
      servo.write(pos);
      delay(1000);
      digitalWrite(7, HIGH);
      digitalWrite(6, LOW);
      
      Serial.println("turning on the light");
    }
    else if(data[0] == 48) { //turn the light off
      pos = 0;
      servo.write(pos);
      delay(1000);
      digitalWrite(7, LOW);
      digitalWrite(6, HIGH);
      Serial.println("turning off the light");
    }
    else {
      Serial.println("bad command");
      for(int i = 0; i < 3; i++) {
        digitalWrite(7, HIGH);
        digitalWrite(6, HIGH);
        delay(1000);
        digitalWrite(7, LOW);
        digitalWrite(6, LOW);
        delay(1000);
        String string = String("123456789012345678901234567890123456789012345678901234567890123456789");
        zigbee.sendCommand(string);
      }      
    }
  
    delete message; //free the memory occupied by message. It is OK to delete a null pointer
  }
} 
