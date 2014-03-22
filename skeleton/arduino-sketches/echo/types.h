#ifndef TYPES_H
#define TYPES_H

typedef struct {
	uint8_t  signature;
	uint8_t  option;
	uint8_t  data_length;
	uint16_t fragment_number;
	uint8_t  *data; 
} message_t;

typedef struct {
	uint8_t   signature;
	uint8_t   fragment_length;
	message_t *message;
} payload_t;

#endif
