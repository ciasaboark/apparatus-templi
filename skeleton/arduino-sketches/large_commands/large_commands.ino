#include <XBee.h>
#include <SoftwareSerial.h>

/**
 * Copyright (c) 2009 Andrew Rapp. All rights reserved.
 *
 * This file is part of XBee-Arduino.
 *
 * XBee-Arduino is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * XBee-Arduino is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with XBee-Arduino.  If not, see <http://www.gnu.org/licenses/>.
 */
 


const String BROADCAST_TAG = "ALL";
const String MODULE_NAME = "LAZY";
const int MAX_DATA_SIZE = 69;
const int XBEE_5V = 12;	//pin number that powers the attached Xbee
uint8_t serialNumber [8];

// const byte START_BYTE = 0b00001101;

SoftwareSerial softSerial = SoftwareSerial(10, 11);
XBee xbee = XBee();
XBeeResponse response = XBeeResponse();
ZBRxResponse rx = ZBRxResponse();
ModemStatusResponse msr = ModemStatusResponse();

void setup() {  
	// start serial
	Serial.begin(115200);
	softSerial.begin(9600);
	
	xbee.begin(softSerial);  //xbee is connected to pins 10 & 11
	
	//set pins 4 - 9 to output mode
	for (int i = 4; i < 10; i++) {
		pinMode(i, OUTPUT);
	}

	//turn on the Xbee, and give it a second to boot
	debug("turning on Xbee");
	pinMode(XBEE_5V, OUTPUT);
	digitalWrite(XBEE_5V, HIGH);
	delay(1000);


	uint8_t shCmd[] = {'S','H'};
	uint8_t slCmd[] = {'S','L'};
	AtCommandRequest atRequest = AtCommandRequest(shCmd);
	AtCommandResponse atResponse = AtCommandResponse();
	
	//request the high serial bits
	debug("requesting SH");
	xbee.send(atRequest);

	if (xbee.readPacket(5000)) {
		if (xbee.getResponse().getApiId() == AT_COMMAND_RESPONSE) {
	  		xbee.getResponse().getAtCommandResponse(atResponse);
		  	if (atResponse.isOk()) {
		    	if (atResponse.getValueLength() == 4) {
		      		for (int i = 0; i < 4; i++) {
		        		serialNumber[i] = atResponse.getValue()[i];
		      		}
		    	}
			}
		}  
	} else {
		debug("error reading serial high bits");
	}

	debug("requesting SL");
	atRequest = AtCommandRequest(slCmd);
	xbee.send(atRequest);
	if (xbee.readPacket(5000)) {
		if (xbee.getResponse().getApiId() == AT_COMMAND_RESPONSE) {
	  		xbee.getResponse().getAtCommandResponse(atResponse);
		  	if (atResponse.isOk()) {
		    	if (atResponse.getValueLength() == 4) {
		      		for (int i = 0, j = 4; i < 4; i++, j++) {
		        		serialNumber[j] = atResponse.getValue()[i];
		      		}
		    	}
		    }
		}  
	} else {
		debug("error reading serial low bits");
	}

	Serial.print("read serial number as: ");
	for (int i = 0; i < 8; i++) {
		Serial.print(serialNumber[i]);
		Serial.print(" ");
	}
	Serial.println(".");

	Serial.println("Setup done");
}

// continuously reads packets, looking for ZB Receive or Modem Status
void loop() {
	//we don't care about any incoming messages.  Instead we
	//+ sleep for a few seconds before waking to send a fake
	//+ sensor reading
	debug("sending reading");
	// String long_command = "Once upon a midnight dreary, while I pondered weak and weary,\
	// Over many a quaint and curious volume of forgotten lore,\
	// While I nodded, nearly napping, suddenly there came a tapping,\
	// As of some one gently rapping, rapping at my chamber door.\
	// `'Tis some visitor,' I muttered, `tapping at my chamber door -\
	// Only this, and nothing more.'";
	String long_command = "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789";
	sendCommand(long_command);
	delay(100);
}

void processMessage(uint8_t message[], int messageLength) {
	debug("processMessage()");
	//flashLED(5, 4);
	if (messageLength >= 15) {
		// debug("reading start byte");
		uint8_t startByte = message[0];
		if (startByte == (uint8_t) 0x0D) {
			uint8_t optionsByte = message[1];
			uint8_t dataLength = (uint8_t)message[2];
			uint8_t fragmentNumber = (message[3] << 8 | message[4]);
			char* destination = "          ";
			memcpy(destination, message+5, 10);
			String destinationString = String(destination);
			Serial.print("destination string: ");
			Serial.println(destinationString);

			// debug("reading data block");
			uint8_t data[dataLength];
			memcpy(data, message+15, dataLength);

			
			//(destination, 10);
			// debug("incoming message addressed to " + destinationString);
			Serial.print("fragment number: ");
			Serial.println(fragmentNumber);
			Serial.print(" data size: ");
			Serial.println(dataLength);

			if (MODULE_NAME.compareTo(destinationString) == 0) {
				debug("message to us");
				//processFragment(optionsByte, dataLength, fragmentNumber, destinationString, data);
			} else if (BROADCAST_TAG.compareTo(destinationString) == 0) {
				//a broadcast request, respond with our serial number
				debug("broadcast message");
				String serialString = byteArrayToString(serialNumber, 8);
				debug("responding with ");
				debug(serialString);
				// sendCommand(serialString);
				sendCommand("foo");
			} else {
				debug("message not for us");
			}
		} else {
			debug("invalid start byte, discarding message");
		}
	} else {
		//flashLED(6, 3);
		//message is too short to be properly formed
		//TODO send error log
		debug("received message too short to be valid, discarding");
	}
}


String byteArrayToString(uint8_t data[], int dataLength) {
	debug("byteArrayToString()");
	String response = "";
	for (int i = 0; i < dataLength; i++) {
		char curChar = (char)data[i];
		if (curChar != NULL) {
			response += String(curChar);
		}
	}
	debug(response);
	return response;
}

void sendCommand(String command) {
	debug("sendCommand()\n   ");
	debug(command);
	//flashLED(7, 1);
	if (command.length() <= MAX_DATA_SIZE) {
		sendCommandFragment(command, 0);
	} else {
		int fragmentNumber = command.length() / MAX_DATA_SIZE;
		int curPos = 0;
		while (fragmentNumber >= 0 || curPos < command.length()) {
			String commandFragment;
			if (curPos + MAX_DATA_SIZE > command.length()) {
				commandFragment = command.substring(curPos, command.length());
			} else {
				commandFragment = command.substring(curPos, curPos + MAX_DATA_SIZE);
			}
			sendCommandFragment(commandFragment, fragmentNumber);
			curPos += MAX_DATA_SIZE;
			fragmentNumber--;	
		}
	}
}

void debug(String message) {
	Serial.println(message);
	Serial.flush();
}


void sendCommandFragment(String commandFragment, int fragmentNo) {
	//flashLED(6, 3);
	debug("sendCommandFragment()");
	uint8_t payload [15 + commandFragment.length()];
	payload[1] = (byte)0x00;
	payload[2] = (byte)commandFragment.length();
	byte fragmentBytes[sizeof(int)];
	memcpy(fragmentBytes, & fragmentNo, sizeof(int));
	payload[3] = fragmentBytes[1];
	payload[4] = fragmentBytes[0];

	byte destinationBytes[10] = {0};
	MODULE_NAME.getBytes(destinationBytes, MODULE_NAME.length() + 1);
	for (int i = 0, j = 5; i < 10; i++, j++) {
		payload[j] = destinationBytes[i];
	}

	byte commandBytes[commandFragment.length()];
	commandFragment.getBytes(commandBytes, commandFragment.length() + 1);
	for (int i = 0, j = 15; i < commandFragment.length(); i++, j++) {
		payload[j] = commandBytes[i];
	}

	payload[0] = (byte)0x0D;
	XBeeAddress64 addr64 = XBeeAddress64(0x00000000, 0x00000000);  //coordinator address
	ZBTxRequest zbTx = ZBTxRequest(addr64, payload, sizeof(payload));
	xbee.send(zbTx);

	//TESTING - maybe this will keep the coordinator from being overwhelmed
	// delay(500);
}

void sendBinary(byte data[], int dataLength) {
	debug("sendBinary()");
	//TODO check if message needs to be fragmented
	uint8_t payload [15 + dataLength];
	payload[0] = (byte)0x0D;
	payload[1] = (byte)0b10000000;
	payload[2] = (byte)dataLength;
	payload[3] = (byte)0x00;
	payload[4] = (byte)0x00;

	byte destinationBytes[10] = {0};
	MODULE_NAME.getBytes(destinationBytes, MODULE_NAME.length() + 1);
	for (int i = 0, j = 5; i < 10; i++, j++) {
		payload[j] = destinationBytes[i];
	}

	for (int i = 0, j = 15; i < dataLength; i++, j++) {
		payload[j] = data[i];
	}

	XBeeAddress64 addr64 = XBeeAddress64(0x00000000, 0x00000000);  //coordinator address
	ZBTxRequest zbTx = ZBTxRequest(addr64, payload, sizeof(payload));
	xbee.send(zbTx);
}


