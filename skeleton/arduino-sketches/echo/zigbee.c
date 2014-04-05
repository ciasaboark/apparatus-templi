#include "zigbee.h"

Zigbee::Zigbee() {
	xbee = XBee();
	softSerial = SoftwareSerial(10, 11);
	msr = ModemStatusResponse();
	response = XBeeResponse();
	rx = ZBRxResponse();
}

Zigbee::~Zigbee() {
}

uint8_t sendCommand(Message message) {
	uint8_t success = 0;
}
