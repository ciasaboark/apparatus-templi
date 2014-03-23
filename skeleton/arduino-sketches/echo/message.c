#include "message.h"

/* This function will populate the message struct with the oppropiate data.
   It is the responsibility of the caller to free the memory that is being used
   by the message structure. */
void assembleMessage(message_t *message, uint8_t *buffer) {
	if(buffer != NULL) {
		message->format = buffer[0];
		message->data_length = buffer[1];
		message->data = malloc(message->data_length * sizeof(char));
		if(message->data != NULL) {
			memcpy(message->data, &buffer[5], message->data_length);	
		}
	}
}

