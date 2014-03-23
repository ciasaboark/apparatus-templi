#include "zigbee.h"
 void sendCommand(message_t *message) {
		
}

/* Build an Xbee packet that is no larger than MAX_DATA_SIZE */
void assemblePayload(message_t *message) {
	int fragment = 0;
	payload_t *payload = malloc(sizeof(payload_t));
	if(payload != NULL) {
		payload->fragment_number = fragment;
	}	
	if(message->data_length > MAX_DATA_SIZE) {
		payload->message = malloc(MAX_PAYLOAD_SIZE - sizeof(payload_t));
		if(payload->message != NULL) {
			//fragment the packet here
		}
	}
}
