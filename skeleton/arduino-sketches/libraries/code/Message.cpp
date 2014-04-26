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
	/* remember to remove these statements */
	Serial.println("the two print statements below are frm message.cpp constructor");
	Serial.println("we are verifying the first byte of the payload");
	Serial.print("The payld[0] before we malloc and assign it to message is: ");
	Serial.println(payld[0]);
	Serial.print("The payload[0] after we malloc and now message has it: ");
	Serial.println(payload[0]);
	Serial.println("if the two values above are the same then the data is consistent after copying the data over to the message object");
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
	destination_name = (char*)malloc(sizeof(char) * 11);
	if(destination_name != NULL) {
		memset(destination_name, NULL, 11);
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

void Message::setPayload(uint8_t *buffer, int length) {
	if(payload != NULL) {
		delete payload;
	}
	payload = (uint8_t*)malloc(sizeof(length));
	if(payload != NULL) {
		memset(payload, NULL, 11); //NULL out payload. This is not needed but I will remove after testing
		memcpy(payload, buffer, 10);
	}
}

uint8_t* Message::getPayload() {
	return payload;
}
