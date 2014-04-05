#ifndef TYPES_H
#define TYPES_H

#define MAX_PAYLOAD_SIZE 69
#define START_BYTE 0x0D


/* values will take on defaults 
   Binary = 0, Text = 1, Hex = 3, Octal = 4 */
typedef enum {BINARY, TEXT} MESSAGE_FORMAT;

typedef struct {
	uint8_t  start_byte;	//0x0D signature byte for the message
	uint8_t  options;	//upper bit is transmission type (e.g text = 1. binary = 1). 
	uint8_t  data_length;	
	uint16_t fragment_number;
	char	 destination[10] = {NULL};
	uint8_t  *payload;	//command to send to the driver
} message_t;		    //or if the xbee library will handle it by just having a pointer

#endif
