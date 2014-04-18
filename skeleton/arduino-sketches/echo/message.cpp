#include "Message.h"

Message::Message(uint8_t sigByte, uint8_t option, uint8_t length, uint16_t frag_number, char *destination_address, uint8_t* payld) {
	signature = sigByte;
	optionByte = option;
	data_length = length;
	fragment_number = frag_number;
	destination_name = destination_address; //this probably will not work
	payload = payld;
}

Message::~Message() { }

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

void Message::setDestinationName(String module) {
	destination_name = module.toCharArray();
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
