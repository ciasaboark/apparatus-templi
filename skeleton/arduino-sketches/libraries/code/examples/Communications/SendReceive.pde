#include <SoftwareSerial.h>
#include <XBee.h>
#include <Zigbee.h>

char name[] = {"Module"}; //the name must be 10 characters or less
Zigbee zigbee(name, 10, 11); //RX and TX pins

void setup() {
    Serial.begin(115200);

 /* this method takes a parameter for the serial data transmission
    rate. However, it will default to 9600 if no parameter is given */
    zigbee.start(); 
}

void loop() {
    String command = String("this is a message to send");
    zigbee.sendCommand(command);
    Message *message = zigbee.receiveMessage();
}
