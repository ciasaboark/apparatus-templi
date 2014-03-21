#ifndef LIBRARY_H
#define

#include <XBee.h>
#include <SoftwareSerial.h>

typedef strcut {
	uint8_t start;
	uint8_t option;
	uint8_t data_length;
	uint8_t fragment_number;
	uint8_t *data; //pointer to the data buffer
} message_t;

void processMessage(message_t *message, uint8_t *buffer);

void processFragment(uint8_t optionsByte, uint8_t dataLength, uint16_t fragmentNumber, String destination, uint8_t data[]);

void sendCommand(String command);

void sendCommandFragment(String commandFragment, int fragmentNo);

void sendBinary(byte data[], int dataLength);

#endif
