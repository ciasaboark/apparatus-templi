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
 
#include <XBee.h>
#include <SoftwareSerial.h>

const byte TEXT_TRANSMISSION = (byte)0b00000000;
const byte BIN_TRANSMISSION  = (byte)0b10000000;
const byte SAFETY_BIT        = (byte)0b00100000;
const byte PROTOCOL_V0       = (byte)0b00000000;
const byte PROTOCOL_V1       = (byte)0b00000001;
const byte PROTOCOL_V2       = (byte)0b00000010;

const byte protocolVersion = PROTOCOL_V1;

const String BROADCAST_TAG = "ALL";
const String MODULE_NAME = "LOCAL";

SoftwareSerial softSerial = SoftwareSerial(10, 11);
XBee xbee = XBee();
XBeeResponse response = XBeeResponse();
ZBRxResponse rx = ZBRxResponse();
ModemStatusResponse msr = ModemStatusResponse();

byte* overflowBuffer = NULL;

void setup() {
	// start serial
	Serial.begin(9600);
	softSerial.begin(9600);
	xbee.begin(softSerial);  //xbee is connected to pins 10 & 11
	//send the ready signal back to the controller
	pinMode(5, OUTPUT);
	pinMode(6, OUTPUT);
	pinMode(7, OUTPUT);
	Serial.print("!!!!!READYREADY\n");
}

// continuously reads packets, looking for ZB Receive or Modem Status
void loop() {
	//we wait for at least two bytes to be in the buffer
	if (Serial.available() > 1) {
		//debug("serial data avail");
		byte startByte = NULL;
		if (overflowBuffer != NULL) {
			//debug("data in overflow buffer");
			startByte = overflowBuffer[0];
			overflowBuffer = NULL;
		} else {
			startByte = Serial.read();
		}

		boolean binTransmission = startByte & (1 << 7);
		if (binTransmission) {
			//debug("bin transmission");
		} else {
			//debug("text transmission");
		}

		// Serial.print("transmission protocol: '");
		unsigned char transProt = (startByte & 0x0F);	//mask the upper 4 bits
		// Serial.write((int)transProt);
		// Serial.println("'");


		if (binTransmission) {  //binary transmission
			//debug("bin trans");
			if (transProt == 1) {
				//debug("begin reading bin message");
				readBinMessage_v1(startByte);
			} else if (transProt == 2) {
				//unimplemented until protocol v2 is complete
				//debug("protocol 2 not supported yet");
			} else {
				//debug("uknown protocol verison");
			}
		} else {  //text transmission
			//debug("txt trans");
			if (transProt == 1) {
				//debug("begin reading text message");
				readTextMessage_v1(startByte);
			} else if (transProt == 2) {
				//unimplemented until protocol v2 is complete
				//debug("protocol 2 not supported yet");
			} else {
				//debug("uknown protocol verison");
			}
		}
	}

	//block for 30ms waiting on a full packet
	xbee.readPacket(30);
	
	if (xbee.getResponse().isAvailable()) {
		//debug("incoming xbee packet");
		// got something
		if (xbee.getResponse().getApiId() == ZB_RX_RESPONSE) {
			// got a zb rx packet
			// now fill our zb rx class
			xbee.getResponse().getZBRxResponse(rx);
			if (rx.getOption() == ZB_PACKET_ACKNOWLEDGED) {
				// the sender got an ACK
			} else {
				// we got it (obviously) but sender didn't get an ACK
			}
			
			uint8_t data[rx.getDataLength()];
		  
			for (int i = 0; i < rx.getDataLength(); i++) {
				//All data received on the xbee should be forwarded to the
				//+ USB serial connection.
				//TODO validate that a correctly formed message was received
				Serial.write(rx.getData(i));
			}
		  
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

/*
* readTextMessage_v1
* Read a full plain text message from the USB serial connection
* then send it
*/
void readTextMessage_v1(byte startByte) {
	//debug("readTextMessage_v1");
	String destination = "";
	String command = "";
	char inChar = Serial.read();
	String curChar = String(inChar);
	//debug("read char " + curChar);
	//read in the destination header
	while (curChar != ":") {
		//debug("curChar '" + curChar + "' not ':'");
		destination += curChar;
		//debug("destination now : " + destination);
		//block until more input is ready
		while (Serial.available() < 1) {}
		char inChar = Serial.read();
		curChar = String(inChar);
	}
	//debug("finished reading dest");
  
	//block until more data is available, then read the command
	while (Serial.available() < 1) {}
	inChar = Serial.read();
	// curChar = String(inChar);
	while (inChar != (char)0x0A) {
		//debug("inChar now: " + String(inChar));
		command += String(inChar);
		//debug("command now: " + command);
		while (Serial.available() < 1) {}
		inChar = Serial.read();
	}

	//debug("done reading command");
  
	processTextMessage(startByte, destination, command);
}


/*
* readBinMessage_v1
* Reads a full message in from the USB serial line. Since messages in
* protocol v1 do not have fixed sizes we have to be careful about when
* to stop reading in data. Normally a byte of 0x0A (a newline char) is
* the terminating byte, but since that byte may have been a natural part
* of the data block we check for doubled newlines (0x0A0A) to indicate
* that the message is not compete. A single newline without a following
* twin marks the end of the transmission.
*/
void readBinMessage_v1(byte startByte) {
	//debug("readBinMessage_v1");
	String destination = "";

	//block until input is ready, then read the destination field
	while (Serial.available() < 1){};
	String curChar = String(Serial.read());

	while (curChar != ":") {
		destination += curChar;
		//block until input is ready
		while (Serial.available() < 1) {}
		curChar = String(Serial.read());
	}

	//Read the data block into an array.  Since we don't
	//+ know the size of the incomming data we have to allocate
	//+ enough space to hold the largest possible data frame
	byte data[1000] = {NULL};
	int bufPos = 0;
	byte curByte = Serial.read();
	//TODO check for double newlines embedded
	boolean transmissionDone = false;
	while (!transmissionDone) {
		if (curByte == 0x0A) {
			//newlines within the data block may be a terminator or a
			//+ natural part of the transmission.  We need to check if
			//+ this newline is followed by another, but in a non-blocking
			//+ manner.  We will allow up to 100ms for more data to arrive
			//+ before assuming that this was a terminating newline
			for (int i = 0; i < 10; i++) {
				if (Serial.available() != 0) {
					byte newlineCheck = Serial.read();
					if (newlineCheck == 0x0A) {
						data[bufPos++] = curByte;
						data[bufPos++] = newlineCheck;
						//please don't judge me
						goto cont;
					} else {
						//place this byte into the overflow buffer
						overflowBuffer[0] = newlineCheck;
						transmissionDone = true;
						//this will be cleaner in protocol v2, I promise
						goto end;
					}
				} else {
					delay(10);
				}
			}
			transmissionDone = true;
			goto end;
		}

		data[bufPos] = curByte;
		bufPos++;
		cont:
		//block until more data is avaiable
		while (Serial.available() < 1) {}
		curByte = Serial.read();
		end:;
	}

	//Tack on the terminating newline
	data[++bufPos] = (byte)0x0A;

	processBinMessage(startByte, destination, data, sizeof(data));
}

/*
* processBinMessage
* Processes a message incomming from the USB serial connection.  If this
* message was addressed to the local arduino then it will be passed off
* to the executeBinary function, otherwise it will be sent to the local
* Xbee for broadcast.
*/
void processBinMessage(byte startByte, String destination, byte data[], int dataLength) {
	//debug("processBinMessage");
	if (destination == BROADCAST_TAG) {
		//Respond to all broadcast requests with "READY", then forward
		//+ the message to the other modules
		sendMessage("READY");
		broadcastBinMessage(startByte, destination, data, dataLength);
	} else if (destination == MODULE_NAME) {
		executeBinary(data, dataLength);
	} else {
		//message to all other modules are forwarded to the Xbee
		broadcastBinMessage(startByte, destination, data, dataLength);
	}
}

/*
* processTextMessage
* Processes a message incomming from the USB serial connection.  If this
* message was addressed to the local arduino then it will be passed off
* to the executeCommand function, otherwise it will be sent to the local
* Xbee for broadcast.
*/
void processTextMessage(byte startByte, String destination, String command) {
	//debug("processTextMessage");
	if  (destination == BROADCAST_TAG) {
		//Respond to all broadcast requests with "READY", then forward
		//+ the message to the other modules
		sendMessage("READY");
		broadcastTextMessage(startByte, destination, command);
	} else if (destination == MODULE_NAME) {
		executeCommand(command);
	} else {
		//messages to all other modules are forwarded to the Xbee
		broadcastTextMessage(startByte, destination, command);
	}
}

/*
* broadcastTextMessage
* Formats the given command into a complete message addressed to the
* given destination, then broadcasts this message over the Xbee
* network.  The message length is not checked, so care should be
* taken that the complete message will fit within a single transmission.
*/
void broadcastTextMessage(byte startByte, String destination, String command) {
	//debug("broadcastTextMessage");

	//the payload will hold the entire application layer protocol
	uint8_t payload [1 + destination.length() + 1 + command.length() + 1];
	int pos = 1;

	//write the start byte
	payload[0] = startByte;

	//write the module name as the address field
	for (int i = 0; i < destination.length(); i++, pos++) {
		payload[pos] = destination[i];
	}
  
	//write the header terminator
	payload[pos++] = 0x3A;  //":"
  
	//write the command field
	for (int i = 0; i < command.length(); i++, pos++) {
		payload[pos] = command[i];
	}
  
	//write the terminator
	payload[pos] = 0x0A;

	XBeeAddress64 addr64 = XBeeAddress64(0x00000000, 0x0000FFFF);  //broadcast address
	ZBTxRequest zbTx = ZBTxRequest(addr64, payload, sizeof(payload));
	xbee.send(zbTx); 
}

/*
* Formats the given binary data into a complete message addressed to the
* given destination, then bradcasts this message over the Xbee
* network. The message length is not checked, so care should be taken
* that the complete message will fit within a single transmission.
*/
void broadcastBinMessage(byte startByte, String destination, byte data[], int dataLength) {
	//debug("broadcastBinMessage");
	//the payload will hold the entire application layer protocol
	uint8_t payload [1 + destination.length() + 1 + dataLength + 1];
	int pos = 1;

	//write the start byte
	payload[0] = startByte;

	//write the module name as the address field
	for (int i = 0; i < destination.length(); i++, pos++) {
		payload[pos] = destination[i];
	}
  
	//write the header terminator
	payload[pos++] = 0x3A;  //":"
  
	//write the command field
	for (int i = 0; i < dataLength; i++, pos++) {
		payload[pos] = data[i];
	}

	//write the terminator
	payload[pos] = 0x0A;

	XBeeAddress64 addr64 = XBeeAddress64(0x00000000, 0x0000FFFF);  //broadcast address
	ZBTxRequest zbTx = ZBTxRequest(addr64, payload, sizeof(payload));
	xbee.send(zbTx);
}

/*
* sendMessage
* Sends a message back to the controller via the USB serial connection
*/
void sendMessage(String command) {
	//debug("sendMessage");
	char startByte = TEXT_TRANSMISSION | SAFETY_BIT | protocolVersion;
	String message = "";
	message += String(startByte);
	message += MODULE_NAME;
	message += ":";
	message += command;
	message += "\n";
	Serial.print(message);
}

// void debug(String message) {
// 	Serial.println(message);
// }

/*
* executeCommand
* Module specific code to handle incomming commands from the controller
*/
void executeCommand(String command) {
	//debug("executeCommand");
	//module specific code here
	if (command == "RESET") {
		for (int i = 5; i < 8; i++) {
			digitalWrite(i, LOW);
		}
	} else {
		String pin = String(command[0]);
		unsigned int pin_num = pin.toInt();
		String st = String(command[1]);
		unsigned int state = st.toInt();
		if (pin_num != 0) {
			if (pin_num < 5 || pin_num > 7) {
				// sendMessage("FAIL" + pin);
			} else {
				if (state == 1) {
					digitalWrite(pin_num, HIGH);
					// sendMessage("OK" + pin);
				} else {
					digitalWrite(pin_num, LOW);
					// sendMessage("OK" + pin);
				}
			}
		} else {
			// sendMessage("FAIL" + pin);
		}
	}
}


/*
* executeBinary
* Module specific code to handle incomming binary from the controller
*/
void executeBinary(byte data[], int dataLength) {
	//debug("executeBinary");
	//module specific code here
	/*
	* WARNING: The data in the byte array does not have double newlines converted into singles.
	* you will need to to this during your processing.
	*/
}
