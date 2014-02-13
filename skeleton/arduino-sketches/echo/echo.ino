#include <StandardCplusplus.h>
#include <system_configuration.h>
#include <unwind-cxx.h>
#include <utility.h>
#include <XBee.h>
#include <SoftwareSerial.h>
#include <vector>
#include <string>

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
const String MODULE_NAME = "ECHO";
const int MAX_DATA_SIZE = 69;
//Each message fragment can hold up to MAX_DATA_SIZE bytes in its payload
//+ the max reassembled message size is set by the max number of fragments.
//+ Note that this value is much larger than the amount of memory the
//+ Arduinos have, so it really only applies to outgoing messages
const int MAX_MESSAGE_SIZE = MAX_DATA_SIZE * 65536;

//The max (reassembled) message size this particular arduino supports.
const int MAX_MESSAGE_SIZE_SUPPORTED = MAX_DATA_SIZE * 40;

// const byte START_BYTE = 0b00001101;

//TODO store object reverencing fragment number and data block instead of a simple byte array
std::vector<byte> fragmentedData;
// byte fragmentedData[MAX_MESSAGE_SIZE_SUPPORTED];
// int fragmentIndex = Max

SoftwareSerial softSerial = SoftwareSerial(10, 11);
XBee xbee = XBee();
XBeeResponse response = XBeeResponse();
ZBRxResponse rx = ZBRxResponse();
ModemStatusResponse msr = ModemStatusResponse();

void setup() {  
	// start serial
	Serial.begin(9600);
	softSerial.begin(9600);
	
	xbee.begin(softSerial);  //xbee is connected to pins 10 & 11
	
	//set pins 4 - 9 to output mode
	for (int i = 4; i < 10; i++) {
		pinMode(i, OUTPUT);
	}
	debug("Setup done");
}

// continuously reads packets, looking for ZB Receive or Modem Status
void loop() {
	// flashLED(5, 1);
		xbee.readPacket(300);
		
		if (xbee.getResponse().isAvailable()) {
			debug("--------\nzigbee packet available");
			
			// got something
			
			if (xbee.getResponse().getApiId() == ZB_RX_RESPONSE) {
				debug("zigbee rx response packet");
				// got a zb rx packet
				
				// now fill our zb rx class
				xbee.getResponse().getZBRxResponse(rx);
						
				if (rx.getOption() == ZB_PACKET_ACKNOWLEDGED) {
						// the sender got an ACK
				} else {
						// we got it (obviously) but sender didn't get an ACK
				}
				debug("incoming zigbee packet");
				// uint8_t data[rx.getDataLength()];
				// memcpy(data, )
				// for (int i = 0; i < rx.getDataLength(); i++) {
				// 	data[i] = rx.getData(i);
				// }
				// debug("processing message");
				processMessage(rx.getData(), rx.getDataLength());
					
			} else if (xbee.getResponse().getApiId() == MODEM_STATUS_RESPONSE) {
				xbee.getResponse().getModemStatusResponse(msr);
				// the local XBee sends this response on certain events, like association/dissociation
				
				if (msr.getStatus() == ASSOCIATED) {
					// yay this is great.  flash led
				} else if (msr.getStatus() == DISASSOCIATED) {
					// this is awful.. flash led to show our discontent
				} else {
					// another status
				}
			} else {
				// not something we were expecting   
			}
		} else if (xbee.getResponse().isError()) {
			//nss.print("Error reading packet.  Error code: ");  
			//nss.println(xbee.getResponse().getErrorCode());
		}
}

void flashLED(int pinNum, int flashes) {
	for (int i = 0; i < flashes; i++) {
		digitalWrite(pinNum, HIGH);
		delay(100);
		digitalWrite(pinNum, LOW);
		delay(100);
	}
}

void processMessage(uint8_t message[], int messageLength) {
	debug("processMessage()");
	//flashLED(5, 4);
	if (messageLength >= 15) {
		// debug("reading start byte");
		uint8_t startByte = message[0];
		// memcpy(startByte, message+0, 1);
		// debug("reading options byte");
		uint8_t optionsByte = message[1];
		// memcpy(optionsByte, message+1, 1);
		// debug("reading data length");
		uint8_t dataLength = (uint8_t)message[2];
		// memcpy(NULL, message+2, 1);
		// debug("reading fragment number");
		// uint16_t fragmentNumber = ((message[3] << 8) + message[4]);
		uint8_t fragmentNumber = (message[4]);
		// debug("reading destination");
		char* destination = "          ";
		memcpy(destination, message+5, 10);
		destination[10] = NULL;
		String destinationString = String(destination);
		// Serial.print("destination string: ");
		// Serial.println(destinationString);

		// debug("reading data block");
		uint8_t data[dataLength];
		memcpy(data, message+15, dataLength);

		
		//(destination, 10);
		// debug("incoming message addressed to " + destinationString);
		Serial.print("fragment number: ");
		Serial.println(fragmentNumber);
		Serial.print(" data size: ");
		Serial.println(dataLength);

		if (fragmentNumber == 0) {
			// processFragment(optionsByte, dataLength, fragmentNumber, destination, data);
		}



	} else {
		//flashLED(6, 3);
		//message is too short to be properly formed
		//TODO send error log
	}
}

void processFragment(uint8_t* optionsByte, uint8_t dataLength, uint16_t fragmentNumber, String destination, uint8_t* data) {

}
// void processFragment(byte optionsByte, byte dataLengthByte, int fragmentNo, byte data[]) {
// 	//flashLED(6, 1);
// 	debug("processFragment() processing fragment number ");
// 	Serial.println(fragmentNo);
// 	//We assume that this fragment number is one less than
// 	//+ the previously received one.
// 	int dataLength = (int)dataLengthByte;
// 	//place the bytes into the vector
// 	debug("placing data into vector\nVector size originally ");
// 	Serial.println(fragmentedData.size());
// 	for (int i = 0; i < dataLength ; i++) {
// 		fragmentedData.insert(0, data[i]);
// 	}
// 	debug("new vector size");
// 	Serial.println(fragmentedData.size());
// 	// delay(1000);

// 	if (fragmentNo == 0) {
// 		debug("fragment was number 0");
// 		//flashLED(6, 2);
// 		//it looks like the C++ library we are using does not support vector::data()
// 		//+ to get a direct pointer to the array backing the vector.  Rebuild a byte[]
// 		//+ from the data in the vector

// 		//TODO find a better way to do this
// 		int vectorSize = fragmentedData.size();
// 		debug("vector size: ");
// 		Serial.println(vectorSize);		

// 		if ((optionsByte & 0b10000000) == 0) {
// 			byte fullData[vectorSize + 1];
// 			for (int i = 0; i < vectorSize; i++) {
// 				fullData[i] = fragmentedData[i];
// 			}
// 			fullData[vectorSize] = NULL;
// 			debug("message was text");
// 			//this was a text command
// 			String command = byteArrayToString(fullData, sizeof(fullData));
// 			executeCommand(command);
// 		} else {
// 			byte fullData[vectorSize];
// 			for (int i = 0; i < vectorSize; i++) {
// 				fullData[i] = fragmentedData[i];
// 			}
// 			debug("message was bin");
// 			//this was a binary command
// 			executeBinary(fullData, sizeof(fullData));
// 		}
// 		//clear the fragmented data
// 		fragmentedData.clear();
// 	}
// }

// void processMessage(uint8_t message[], int messageLength) {
//   if (messageLength >= 15) {
//     byte destinationBytes[10];
//     for (int i = 0, j = 5; i < 10; i++, j++) {
//       destinationBytes[i] = message[j];
//     }
//     String destination = byteArrayToString(destinationBytes, 10);
//     if (MODULE_NAME.compareTo(destination) == 0) {
//       //this was a message to us
//       byte startByte = message[0];
//       byte optionsByte = message[1];
//       byte dataLengthByte = message[2];
//       byte fragmentNoBytes[2];
//       fragmentNoBytes[0] = message[3];
//       fragmentNoBytes[1] = message[4];
//       byte dataBytes[messageLength];
//       for (int i = 0, j = 15; i < messageLength; i++, j++) {
//         dataBytes[i] = message[j];
//       }

//       if ((optionsByte & 0b10000000) == 0) {
//         //this was a text command
//         executeCommand(byteArrayToString(dataBytes, (int)dataLengthByte));
//       } else {
//         //this was a binary command
//         executeBinary(dataBytes, (int)dataLengthByte);
//       }
//     } else if (BROADCAST_TAG.compareTo(destination) == 0) {
//       //a broadcast message.  respond with "READY"
//       sendMessage("READY");
//     } else {
//       //this was a message for a different module
//     }    
//   } else {
//     //message is too short to be properly formed
//     //TODO send error log
//   }
// }


String byteArrayToString(byte data[], int dataLength) {
	debug("byteArrayToString()");
	// String str = "";
	// for (int i = 0; i < dataLength; i++) {
	// 	String c = String((char)data[i]);
	// 	Serial.print(c + " ");
	// 	if (c != NULL) {  //omit null characters
	// 		str += (char)data[i];
	// 		Serial.print("!");
	// 	} else {
	// 		Serial.print("x");
	// 	}
	// }

	String str = String((char*) data);
	str.trim();
	debug("decoded string '" + str + "'");
	return str;
}

void sendCommand(String command) {
	debug("sendCommand()");
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
	// debug("sendMessage()");
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
	delay(500);
}

void sendBinary(byte data[], int dataLength) {
	// debug("sendBinary()");
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


/*
* executeCommand
* Module specific code to handle incomming commands from the controller
*/
void executeCommand(String command) {
	//flashLED(6, 1);
	//just repeat the given command
	sendCommand(command);
}


/*
* executeBinary
* Module specific code to handle incomming binary from the controller
*/
void executeBinary(byte data[], int dataLength) {
	// debug("executeBinary()");
	//debug("executeBinary");
	//module specific code here
	/*
	* WARNING: The data in the byte array does not have double newlines converted into singles.
	* you will need to to this during your processing.
	*/
}
