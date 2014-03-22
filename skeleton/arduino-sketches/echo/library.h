#ifndef LIBRARY_H
#define

#include <XBee.h>
#include <SoftwareSerial.h>

typedef struct {
	uint8_t  signature;
	uint8_t  option;
	uint8_t  data_length;
	uint16_t fragment_number;
	uint8_t  *data; //pointer to the data buffer
} message_t;

typedef struct {
	uint8_t   signature;
	uint8_t   fragment_length;
	message_t *message;
} payload;

/* This will populate a message struct from the data supplied by the buffer. 
   It dynamically allocates memory for the data, then copies it from the buffer
   to the message struct buffer. It is the responsability of the caller to free the
   allocated memory */
void processMessage(message_t *message, uint8_t *buffer);

void sendCommand(message_t *message);

void sendBinary(byte *buffer, int dataLength);

#endif
