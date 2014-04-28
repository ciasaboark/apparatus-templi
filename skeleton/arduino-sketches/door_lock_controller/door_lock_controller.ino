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

/**
* SAMPLE MESSAGES
* all:READY
* 0D 00 06 00 00 41 4c 4c 00 00 00 00 00 00 00 52 45 41 44 59 3f
*
* LOCAL:5
* 0D 00 02 00 00 4c 4f 43 41 4c 00 00 00 00 00 35 30
*
* LOCAL:RESET
* 0D 00 05 00 00 4c 4f 43 41 4c 00 00 00 00 00 52 45 53 45 54
*
* LED_FLASH:5
* 0D 00 01 00 00 4c 45 44 5f 46 4c 41 53 48 00 35

0D 00 05 00 00 4c 45 44 5f 46 4c 41 53 48 00 
**/
 
#include <XBee.h>
#include <SoftwareSerial.h>

const String BROADCAST_TAG = "ALL";
const String MODULE_NAME = "LOCAL";

SoftwareSerial softSerial = SoftwareSerial(10, 11);
//download Xbee library from https://code.google.com/p/xbee-arduino/
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
        pinMode(7, OUTPUT);        

	/*
	* Place module specific setup code here
	*/

	Serial.print("!!!!!READYREADY\n");
}

//The main loop should continuously check for messages incoming from
//+ the serial line and packets received from the Xbee
void loop() {
	//we need at least one full message header to begin processing
	if (Serial.available() >= 15) {
		//TODO check that the start byte is correct and that the fragment size
		//+ is not too large
		//debugreading message header");
		byte startByte = Serial.read();
		if (startByte == (byte)0x0D) {
			byte optionsByte = Serial.read();
			byte dataLengthByte = Serial.read();
			byte fragmentNoBytes[2];
			fragmentNoBytes[0] = Serial.read();
			fragmentNoBytes[1] = Serial.read();
			byte destinationBytes[10];
			for (int i = 0; i < 10; i++) {
				destinationBytes[i] = Serial.read();
			}

			int dataLength = (int)dataLengthByte;

			String destination = byteArrayToString(destinationBytes, 10);

			byte dataBytes[dataLength];
			for (int i = 0; i < dataLength; i++) {
				while (!Serial.available()) {
					delay(30);	//block until a byte is ready
				}
				dataBytes[i] = Serial.read();
			}

			//If the message was addressed to us then we can begin processing
			if (MODULE_NAME.compareTo(destination) == 0) {
				//debugmessage address to us");
				processMessage(optionsByte, fragmentNoBytes, destination, dataBytes, dataLength);
			} else {

				//If this was a broadcast message then we will broadcast the message before
				//+ starting our own processing
				if (BROADCAST_TAG.compareTo(destination) == 0) {
					//debugmessage address to broadcast");
					broadcastMessage(startByte, optionsByte, dataLengthByte, fragmentNoBytes, destinationBytes, dataBytes);
					processMessage(optionsByte, fragmentNoBytes, destination, dataBytes, dataLength);
				} else {
					//just transmit the message
					//debugforwarding message");
					broadcastMessage(startByte, optionsByte, dataLengthByte, fragmentNoBytes, destinationBytes, dataBytes);
				}
			}
		}
	}

	//block for 30ms waiting on a full packet
	xbee.readPacket(30);
	
	if (xbee.getResponse().isAvailable()) {
		////debugincoming xbee packet");
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

void broadcastMessage(byte startByte, byte optionsByte, byte dataLengthByte, byte fragmentNoBytes[], byte destinationBytes[], byte dataBytes[]) {
	//debugbroadcastMessage()");
	uint8_t payload [15 + (unsigned int)dataLengthByte];
	payload[0] = startByte;
	payload[1] = optionsByte;
	payload[2] = dataLengthByte;
	payload[3] = fragmentNoBytes[0];
	payload[4] = fragmentNoBytes[1];
	for (int i = 0, j = 5; i < 10; i++, j++) {
		payload[j] = destinationBytes[i];
	}
	for (int i = 0, j = 15; i < (int)dataLengthByte; i++, j++) {
		payload[j] = dataBytes[i];
	}

	XBeeAddress64 addr64 = XBeeAddress64(0x00000000, 0x0000FFFF);  //broadcast address
	ZBTxRequest zbTx = ZBTxRequest(addr64, payload, sizeof(payload));
	xbee.send(zbTx);
}


/*
* sendMessage
* Sends a message back to the controller via the USB serial connection
*/
void sendMessage(String command) {
	//debugsendMessage()");
	//TODO check size of command for fragmentation
	byte startByte = (byte)0x0D;
	byte optionsByte = (byte)0b00000000;
	byte dataLengthByte = (byte)command.length();
	byte fragmentNoBytes[] = {(byte)0x00, (byte)0x00};
	byte destinationBytes[10] = {0};
	MODULE_NAME.getBytes(destinationBytes, MODULE_NAME.length() + 1);
	byte dataBytes [command.length()];
	command.getBytes(dataBytes, sizeof(dataBytes) + 1);

	Serial.write(startByte);
	Serial.write(optionsByte);
	Serial.write(dataLengthByte);
	Serial.write(fragmentNoBytes, 2);
	Serial.write(destinationBytes, 10);
	Serial.write(dataBytes, command.length());
}

void sendBinary(byte data[], int dataLength) {
	//debugsendBinary()");
	//TODO check size of data for fragmentation
	byte startByte = (byte)0x0D;
	byte optionsByte = (byte)0b10000000;
	byte dataLengthByte = (byte)dataLength;
	byte fragmentNoBytes[] = {(byte)0x00, (byte)0x00};
	byte destinationBytes[10];
	MODULE_NAME.getBytes(destinationBytes, MODULE_NAME.length());

	Serial.write(startByte);
	Serial.write(optionsByte);
	Serial.write(dataLengthByte);
	Serial.write(fragmentNoBytes, 2);
	Serial.write(destinationBytes, 10);
	Serial.write(data, dataLength);
}

//is there really no built-in way to do this?
String byteArrayToString(byte data[], int dataLength) {
	String str = "";
	for (int i = 0; i < dataLength; i++) {
		byte b = data[i];
		if (b != 0x00) {
			str += (char)data[i];
		}
	}
	return str;
}


void processMessage(byte optionsByte, byte fragmentNoBytes[], String destination, byte dataBytes[], int dataLength) {
	//debugprocessMessage()");
	if (destination.compareTo(BROADCAST_TAG) == 0) {
		//the only broadcast command we know of is "READY?" to which we
		//+ should respond with "READY"
		sendMessage("foo");
	} else if (destination.compareTo(MODULE_NAME) == 0) {
		//this was a message to us.  Execute the command based on
		//+ whether the data is text or binary
		if ((optionsByte & 0b10000000) == 0) {
			//text command
			executeCommand(byteArrayToString(dataBytes, dataLength));
		} else {
			executeBinary(dataBytes, dataLength);
		}
	}
}

/*
* executeCommand
* Module specific code to handle incomming commands from the controller
*/
void executeCommand(String command) 
{
  if(command.compareTo("0") == 0)
  {   
   digitalWrite(7, HIGH);    
  }
  else if(command.compareTo("1") == 0)
  {
    digitalWrite(7, LOW);   
  }
}


/*
* executeBinary
* Module specific code to handle incomming binary from the controller
*/
void executeBinary(byte data[], int dataLength) {
}
