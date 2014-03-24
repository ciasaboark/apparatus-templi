#include "message.h"

/* This function will populate the message struct with the oppropiate data.
   It is the responsibility of the caller to free the memory that is being used
   by the message structure. You should free the memory that message->data points 
   to first before freeing the memory used by message struct. */
message_t* assembleMessage(uint8_t *buffer) {
	message_t *message = (message_t *)malloc(sizeof(message_t));
	if(message != NULL) {
		message->format = buffer[0];
		message->data_length = buffer[1];
		message->data = malloc(message->data_length * sizeof(char));
		if(message->data != NULL) {
			memcpy(message->data, &buffer[5], message->data_length);	
		}
	}
	return message;
}

