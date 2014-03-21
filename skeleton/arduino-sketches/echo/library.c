#include "library.h"


/* This function will populate the message struct with the opproiate data.
   It is the responsibility of the caller to free the memory that is being used
   by the message structure. */
void processMessage(message_t *message, uint8_t *buffer) {
	if(buffer_length >= 15) {
		message->start = buffer[0];

		if(startByte == 13) {
			message->option = buffer[1];
			message->data_length = buffer[2];
			message->fragment_number = buffer[4];
			message->data = malloc(message->data_length * sizeof(char));
			memcpy(message->data, buffer[5], message->data_length);
		}
	}
}
