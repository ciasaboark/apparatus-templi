#ifndef ZIGBEE_H
#define ZIGBEE_H

/* global defines */
#define COORDINATOR_ADDRESS 0x00000000
#define XBEE_PIN 12
#define MAX_DATA_SIZE = 32

#include <XBee.h>
#include <SoftwareSerial.h>
#include "Message.h"

class Zigbee {
    public:
	/* newName can only have a max character limit of 10. */
        Zigbee(char *name, int rx_pin, int tx_pin);
	~Zigbee();

        void start(int buad_rate = 9600); //start the serial communication with the default data transmission rate
        void sendCommand(char *command);
        void sendBinary(uint8_t *data, int data_length);
        void sendMessageFragment(Message *obj);
		
	private:
        Xbee 		    *xbee;
        SoftwareSerial 	    *softSerial;
        XBeeResponse 	    *response;
        ZBRxResponse 	    *rx;
        ModemStatusResponse *msr;
        char name[10];
};

#endif
