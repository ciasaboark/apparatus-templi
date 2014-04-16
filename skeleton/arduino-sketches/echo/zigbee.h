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
        char name[10];

    public:
	/* newName can only have a max character limit of 10. */
        Zigbee(String newNname, int rx_pin, int tx_pin)
	~Zigbee();

        void start(int serial_port);
        void sendCommand(String command);
        void sendBinary(uint8_t *data, int data_length);
	void sendMessageFragment(Message *obj);
};

#endif
