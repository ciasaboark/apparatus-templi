#include <SoftwareSerial.h>
SoftwareSerial mySerial(10, 11);

//char termChar = '0x0a';
String terminator = "\n";

void setup() {
  Serial.begin(9600);
//  Serial.print("Goodnight moon!");
  mySerial.begin(4800);
  mySerial.println("Test");
}

void loop() {
  if (mySerial.available()) {
    Serial.write(mySerial.read());
  }
  
  if (Serial.available()) {
    mySerial.write(Serial.read());
  }
  
  Serial.print("LED_FLASH:5" + terminator);
  delay(5000);
  
  Serial.print("LED_FLASH:9" + terminator);
  delay(5000);
  
  Serial.print("LED_FLASH:6" + terminator);
  delay(5000);
  
  //this is out of range
  Serial.print("LED_FLASH:3" + terminator);
  delay(5000);
  
  //simulate gibberish
  Serial.print("LED_FLASH:asdfas" + terminator);
  delay(5000);
  
  //send a message to an unknown module
  Serial.print("TEST_MODULE:abcdef" + terminator);
  delay(5000);
  
  //ask all modules to respond with their name
  Serial.print("ALL:READY?" + terminator);
  delay(5000);
}
