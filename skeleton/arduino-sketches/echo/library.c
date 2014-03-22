#include "library.h"


/* This function will populate the message struct with the oppropiate data.
   It is the responsibility of the caller to free the memory that is being used
   by the message structure. */
void processMessage(message_t *message, uint8_t *buffer) {
	if(buffer_length >= 15) {
		message->signature = buffer[0];

		if(startByte == 13) {
			message->option = buffer[1];
			message->data_length = buffer[2];

			/* place buffer[3] in the first half of fragment_number,
			   and place buffer[4] in the second half of fragment_number.
			   e.g fragment_number should look like "[ buffer[3] buffer[4] ]" */
			*((uint8_t *) &message.fragment_number) = buffer[3];
			*(((uint8_t *) &message.fragment_number)++) = buffer[4]; //increment to the second byte of fragment_number
			message->data = malloc(message->data_length * sizeof(char));
			memcpy(message->data, buffer[5], message->data_length);
		}
	}
}

void sendCommand(message_t *message) {

}
