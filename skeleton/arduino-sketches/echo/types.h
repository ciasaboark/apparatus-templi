#ifndef TYPES_H
#define TYPES_H

#define MAX_PAYLOAD_SIZE 69
#define START_BYTE 0x0D


/* values will take on defaults 
   Binary = 0, Text = 1, Hex = 3, Octal = 4 */
typedef enum {BINARY, TEXT} MESSAGE_FORMAT;

typedef struct {
	uint8_t start_byte;
	uint8_t  options;
	uint8_t  data_length;
	uint16_t   fragment_number;
	uint8_t *destinaion;
	uint8_t  *payload;
} message_t;		    //or if the xbee library will handle it by just having a pointer

#endif

/*
sendBinary(uint8_t* command, int length);
sendCommand(String command);
sendFragment(message_t);
*/
