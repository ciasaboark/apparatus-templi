#include <SoftwareSerial.h>
#include <XBee.h>
#include "Zigbee.h"

//char name[] = {"Module"}; //the name must be 10 characters or less
Zigbee zigbee("test", 10, 11); //RX and TX pins

void setup() {
    Serial.begin(115200);
    pinMode(7, OUTPUT);

 /* this method takes a parameter for the serial data transmission
    rate. However, it will default to 9600 if no parameter is given */
    zigbee.start(); 
}

void loop() {
    String command = String("this is a message to send");
    zigbee.sendCommand(command);
    Message *message = zigbee.receiveMessage();
    Serial.println("checking for packet");
    if (message != NULL) {
    	Serial.println("got a message");
    	char* thisData = (char*)message->getPayload();
    	
    	String commandString = String();
    	Serial.println(commandString);

    	uint8_t dataLength = message->getDataLength();

    	// for (int i = 0; i < dataLength + 15; i++) {
    	// 	Serial.println((char*)thisData[i]);
    	// }

    	//char command[dataLength] = "";
    	//command[dataLength] = '\0';
    	// for (int i = 0; i < dataLength; i++) {
    	// 	command[i] = thisData[i];
    	// }
    	// Serial.println(command);
    	
    	char test[] = {"abc"};
    	Serial.println(itoa(thisData[0],test, 10));
    	Serial.println(test);
    	// executeCommand(thisData);

    	delete message;
    } else {
    	Serial.println("message was null");
    }
}

void executeCommand(uint8_t* command) {
	Serial.println("executing command");
	char c1 = (char)command[0];
	Serial.println(c1);
	// Serial.println(*command[0]);
	// Serial.println(*command[1]);
	// Serial.println(*command[2]);
	// Serial.println(*command[3]);
	digitalWrite(7, HIGH);
	delay(3000);
	digitalWrite(7, LOW);
	// Serial.println(command);

}
