#include <SoftwareSerial.h>
SoftwareSerial mySerial(10, 11);

void setup() {
  Serial.begin(9600);
  Serial.println("Goodnight moon!");
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
  
  Serial.println("LED_FLASH:5");
  Serial.flush();
  delay(5000);
  
  Serial.println("LED_FLASH:9");
  Serial.flush();
  delay(5000);
  
  Serial.println("LED_FLASH:6");
  Serial.flush();
  delay(5000);
  
  //this is out of range
  Serial.println("LED_FLASH:3");
  Serial.flush();
  delay(5000);
  
  //simulate gibberish
  Serial.println("LED_FLASH:asdfas");
  delay(5000);
  
  //send a message to an unknown module
  Serial.println("TEST_MODULE:abcdef");
  Serial.flush();
  delay(5000);
  
  //ask all modules to respond with their name
  Serial.println("ALL:NAME");
  Serial.flush();
  delay(5000);
}
