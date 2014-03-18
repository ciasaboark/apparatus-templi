#include <RemoteModule.h>

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
 





// const byte START_BYTE = 0b00001101;

SoftwareSerial softSerial = SoftwareSerial(10, 11);
XBee xbee = XBee();
XBeeResponse response = XBeeResponse();
ZBRxResponse rx = ZBRxResponse();
ModemStatusResponse msr = ModemStatusResponse();

void setup() { 
   MODULE_NAME = "ECHO"; 
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
	uint8_t slCmd[] = {'S', 'L'};
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
			//Serial.print("Error reading packet.  Error code: ");  
			//Serial.println(xbee.getResponse().getErrorCode());
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
void executeBinary(uint8_t data[], int dataLength) {
	debug("executeBinary()");
	//debug("executeBinary");
	//module specific code here
	/*
	* WARNING: The data in the byte array does not have double newlines converted into singles.
	* you will need to to this during your processing.
	*/
}
