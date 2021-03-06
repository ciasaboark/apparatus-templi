#include "Message.h"

Message::Message(uint8_t sigByte, uint8_t option, uint8_t length, uint16_t frag_number, char *destination_address, uint8_t* payld) {
	signature = sigByte;
	optionByte = option;
	data_length = length;
	fragment_number = frag_number;
	destination_name = (char*)malloc(11);
	
	if (destination_name != NULL) {
		memset(destination_name, NULL, 11);
		memcpy(destination_name, destination_address, 10);
	}
	

	payload = (uint8_t*)malloc(length);
	if (payload != NULL) {
		memcpy(payload, payld, length);
	}
	Serial.println(payld[0]);
	Serial.println(payload[0]);
}

Message::~Message() {
	delete payload;
	delete destination_name;
}

void Message::setSignatureByte(uint8_t sigByte) {
	signature = sigByte;
}

uint8_t Message::getSignatureByte() {
	return signature;
}

void Message::setOptionByte(uint8_t option) {
	optionByte  = option;
}

uint8_t Message::getOptionByte() {
	return optionByte;
}

void Message::setDestinationName(char *module) {
	if(destination_name != NULL) {
		delete (destination_name);
	}
	destination_name = (char*)malloc(sizeof(char) * 10);
	if(destination_name != NULL) {
		memcpy(destination_name, module, 10);
	}
}

char* Message::getDestination() {
	return destination_name;
}

void Message::setDataLength(uint8_t length) {
	data_length = length;
}

uint8_t Message::getDataLength() {
	return data_length;
}
		
void Message::setFragmentNumber(uint16_t frag_number) {
	fragment_number = frag_number;
}

uint16_t Message::getFragmentNumber() {
	return fragment_number;
}

void Message::setPayload(uint8_t *buffer) {
	if(payload != NULL) {
		delete payload;
	}
	payload = buffer;
}

uint8_t* Message::getPayload() {
	return payload;
}
