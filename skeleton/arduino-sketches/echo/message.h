#ifndef LIBRARY_H
#define LIBRARY_H

#include "types.h"

#define MAX_MESSAGE_SIZE 69

/* This will populate a message struct from the data supplied by the buffer. 
   It dynamically allocates memory for the data, then copies it from the buffer
   to the message struct buffer. It is the responsability of the caller to free the
   allocated memory */
void assembleMessage(message_t *message, uint8_t *buffer);

#endif
