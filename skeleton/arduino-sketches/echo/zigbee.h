#ifndef ZIGBEE_H
#define ZIGBEE_H

#include <XBee.h>
#include <SoftwareSerial.h>
#include "Message.h"

#define COORDINATOR_ADDRESS 0x00000000
#define XBEE_PIN 12
#define MAX_DATA_SIZE = 32

class Zigbee {
    private:
        Xbee xbee;
        SoftwareSerial softSerial;
        XBeeResponse response;
        ZBRxResponse rx;
        ModemStatusResponse msr;

    public:
        Zigbee()
	~Zigbee();
        void start(int serial_port, int softSerial_port);
        
        uint8_t sendCommand(String command);
        uint8_t sendBinary(uint8_t data, int data_length);
};

#endif
