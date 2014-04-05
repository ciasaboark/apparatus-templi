#ifndef ZIGBEE_H
#define ZIGBEE_H

#include <XBee.h>
#include <SoftwareSerial.h>
#include "types.h"

#define COORDINATOR_ADDRESS 0x00000000
#define XBEE_PIN 12

class Zigbee {
	private:
		Xbee xbee;
	
	public:
		sendCommand(Message message);
};

#endif
