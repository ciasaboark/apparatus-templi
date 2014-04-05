#ifndef ZIGBEE_H
#define ZIGBEE_H

#include <XBee.h>
#include <SoftwareSerial.h>
#include "Message.h"

#define COORDINATOR_ADDRESS 0x00000000
#define XBEE_PIN 12

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

		uint8_t sendCommand(Message message);
};

#endif
