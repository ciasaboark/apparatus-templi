#include "zigbee.h"

/* Allocates and returns a payload struct. It is up to the user to free 
   the memory that has been allocated the the payload */
payload_* assemblePayload(message_t *message) {
 	payload_t *payload = malloc(sizeof(payload_t));
	if(payload != NULL) {
		payload->fragment_number = fragment;

		/* The payload_t is six bytes + how ever many bytes the message data buffer is */
		payload->fragment_length = sizeof(payload_t) + message->data_length;
		payload->message = message;
	}
	return payload;
}

/* Not complete */
void sendCommand(message_t *message) {	
	payload_t *payload = assemblePayload(message);
	
	XBeeAddress64 addr64 = XBeeAddress64(COORDINATOR_ADDRESS, COORDINATOR_ADDRESS);  //coordinator address
	ZBTxRequest zbTx = ZBTxRequest(addr64, payload, payload->fragment_length);
	xbee.send(zbTx); //just here for illustration 

	/* free the memory once the payload has been sent 
	   It is possible that the memory could be used again
	   so memory will not have to be allocated multiple times 
           for fragmented messages */
	free(message->data);
	free(message);
	free(payload);
}
