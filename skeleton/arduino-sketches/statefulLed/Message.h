#ifndef MESSAGE_H
#define MESSAGE_H

#include <Arduino.h>

class Message {	
	public:
		Message(uint8_t sigByte, uint8_t option, uint8_t length, uint16_t frag_number, char *destination_address, uint8_t* payld);
		~Message();
		Message(const Message *obj);

		void setSignatureByte(uint8_t sigByte);
		uint8_t getSignatureByte();

		void setOptionByte(uint8_t option);
		uint8_t getOptionByte();

		void setDestinationName(char *module);
		char* getDestination();

		void setDataLength(uint8_t length);
		uint8_t getDataLength();
		
		void setFragmentNumber(uint16_t fragment_number);
		uint16_t getFragmentNumber();

		void setPayload(uint8_t *buffer);
		uint8_t* getPayload();
		
	private:
		uint8_t  signature;
		uint8_t  optionByte;
		uint8_t  data_length;
		uint16_t fragment_number;
		char     *destination_name;		
		uint8_t  *payload;
		
};

#endif
