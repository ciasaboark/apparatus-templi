#ifndef ZIGBEE_H
#define ZIGBEE_H

/* global defines */
#define COORDINATOR_ADDRESS 0x00000000
#define XBEE_PIN 12
#define MAX_DATA_SIZE 69

#include <Arduino.h>
#include <SoftwareSerial.h>
#include <XBee.h>
#include "Message.h"

class Zigbee {
    public:
	/* newName can only have a max character limit of 10. */
        Zigbee(char *name, uint8_t rx_pin, uint8_t tx_pin);
		~Zigbee();

        void start(int buad_rate = 9600); //start the serial communication with the default data transmission rate 9600
        void sendCommand(String command);
        void sendBinary(uint8_t *command, int data_length);
        void sendMessage(uint8_t *command, int data_length, uint8_t type);
        void sendMessageFragment(Message *obj);
		
		/* This method mallocs and returns a message object. If you plan to use the 
		   message, you should check it against null because if the xbee does not have a 
		   message, this will return the message as a NULL pointer. It should be freed by the caller. */
		Message* receiveMessage();

		
     private:
        XBee 	       	    *xbee;
        SoftwareSerial 	    *softSerial;
        XBeeResponse 	    *response;
        ZBRxResponse        *rx;
        ModemStatusResponse *msr;
        char *name;
};

#endif
