#ifndef TYPES_H
#define TYPES_H

/* values will take on defaults 
   Binary = 0, Text = 1, Hex = 3, Octal = 4 */
typedef enum {BINARY, TEXT, HEX, OCTAL} MESSAGE_FORMAT;

typedef struct {
	uint8_t  format;
	uint8_t  data_length;
	uint8_t  *data;
} message_t;

typedef struct {
	uint8_t   fragment_number;
	uint8_t   fragment_length;
	message_t *message; //not sure if the needs to be a copy of the message 
} payload_t;		    //or if the xbee library will handle it by just having a pointer

#endif
