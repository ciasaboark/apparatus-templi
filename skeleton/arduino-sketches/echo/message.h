#ifndef LIBRARY_H
#define LIBRARY_H

#include <XBee.h>
#include <SoftwareSerial.h>
#include "types.h"

/* This will populate a message struct from the data supplied by the buffer. 
   It dynamically allocates memory for the data, then copies it from the buffer
   to the message struct buffer. It is the responsability of the caller to free the
   allocated memory */
void processMessage(message_t *message, uint8_t *buffer);

void sendCommand(message_t *message);

void sendBinary(byte *buffer, int dataLength);

#endif
