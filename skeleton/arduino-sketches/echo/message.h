#ifndef LIBRARY_H
#define LIBRARY_H

#include "types.h"

/* This function will populate the message struct with the oppropiate data.
   It is the responsibility of the caller to free the memory that is being used
   by the message structure. You should free the memory that message->data points 
   to first before freeing the memory used by message struct. */
message_t* assembleMessage(uint8_t *buffer);

#endif
