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

const byte procotolVersion = PROTOCOL_V1;

const String BROADCAST_TAG = "ALL";
const String MODULE_NAME = "LED_FLASH";

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
}

// continuously reads packets, looking for ZB Receive or Modem Status
void loop() {  
    xbee.readPacket(30);
    
    if (xbee.getResponse().isAvailable()) {
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
          data[i] = rx.getData(i);
        }

        processMessage(data, sizeof(data));
          
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

void processMessage(uint8_t inData[], int inDataLength) {
  Serial.flush();
  byte startByte = inData[0];
  
  //read in the destination address
  String destination; // = String((char*)inData[1, sizeof(inData)]);
  int headerEnd = 0;
  for (int i = 1; i < inDataLength; i++) {
    if ((char)inData[i] != (char)0x3A) {
      destination += (char)inData[i];
      headerEnd++;
    } else {
      headerEnd++;
      break;
    }
  }
  
  //if we were unable to find a ':' in the message then it is not
  //+ valid and we should not proceed
  if (headerEnd != 0) {
    //assumes text transmision for now
    //TODO update to read start byte and read data as needed
    String command;
    for (int i = headerEnd + 1; i < inDataLength - 1; i++) {
      command += (char)inData[i];
    }
    
    processCommand(destination, command);
  } else {
    Serial.println("Invalid message received");
  }  
}

void processCommand(String destination, String command) {
 if  (destination == BROADCAST_TAG) {
   //respond to broadcast requests with "READY"
   sendMessage("READY");
 } else if (destination == MODULE_NAME) {
   //module specific parsing here
   unsigned long int pin_num = command.toInt();
   if (pin_num == 0) {
     //the command could not be parsed
     //debugMessage("Error converting " + command + " to an integer value");
     sendMessage("FAIL");
   } else if (pin_num < 4 || pin_num > 9) {
     //the given value was out of range
     //debugMessage("Value of " + (String)pin_num + " is out of range");
     sendMessage("FAIL");
   } else {
     //debugMessage("Flashing LED num " + (String)pin_num);
     //flash the LED
     digitalWrite(pin_num, HIGH);
     delay(300);
     digitalWrite(pin_num, LOW);
     //respond to the controler to indicate that the LED was flashed
     sendMessage("OK");
   }
 }
}

void sendMessage(String command) {
  byte startByte = procotolVersion | TEXT_TRANSMISSION | SAFETY_BIT;
  
  //the payload will hold the entire application layer protocol
  uint8_t payload [1 + MODULE_NAME.length() + 1 + command.length() + 1];
  int pos = 1;
  
  //write the start byte
  payload[0] = startByte;

  //write the module name as the address field
  for (int i = 0; i < MODULE_NAME.length(); i++, pos++) {
    payload[pos] = MODULE_NAME[i];
  }
  
  //write the header terminator
  payload[pos++] = 0x3A;  //":"
  
  //write the command field
  for (int i = 0; i < command.length(); i++, pos++) {
    payload[pos] = command[i];
  }
  
  //write the terminator
  payload[pos] = 0x0A;
  
  XBeeAddress64 addr64 = XBeeAddress64(0x00000000, 0x00000000);  //address to the coordinator
  ZBTxRequest zbTx = ZBTxRequest(addr64, payload, sizeof(payload));
  xbee.send(zbTx); 
}

void sendBinary(byte data[]) {
  //TODO 
}
