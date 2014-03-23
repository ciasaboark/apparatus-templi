#include "zigbee.h"
 void sendCommand(message_t *message) {
		
}

/* This is only for testing right now. Eventually this will fragment messages,
   but it only packages up on message for now. */
void assemblePayload(message_t *message) {
	payload_t *payload = malloc(sizeof(payload_t));
	if(payload != NULL) {
		payload->fragment_number = fragment;
		/* The payload_t is six bytes + how ever many bytes the message data buffer is */
		payload->fragment_length = sizeof(payload_t) + message->data_length;
		payload->message = message;
	}	
}
