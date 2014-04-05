#include "message.h"

Message::Message(uint8_t sigByte, uint8_t option, uint8_t length, uint16_t frag_number, String destination_address, uint8_t* payld) : signature(sigByte), format(option), data_length(length), fragment_number(frag_number), destination(destination_address) {
	payload = payld;
}

Message::~Message() {
	delete payload; 
}

Message(const obj) {

}

void Message::setSignatureByte(uint8_t sigByte) {
	signature = sigByte;
}

uint8_t Message::getSignatureByte() {
	return signature;
}

void Message::setFormat(uint8_t option) {
	format = option;
}

uint8_t Message::getFormat() {
	return format;
}

void Message::setDestination(String module) {
	destination = module;
}

String Message::getDestination() {
	return destination;
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
