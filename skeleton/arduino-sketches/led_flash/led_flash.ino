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

void processMessage(uint8_t message[], int messageLength) {
  if (messageLength >= 15) {
    byte destinationBytes[10];
    for (int i = 0, j = 5; i < 10; i++, j++) {
      destinationBytes[i] = message[j];
    }
    String destination = byteArrayToString(destinationBytes, 10);
    if (MODULE_NAME.compareTo(destination) == 0) {
      //this was a message to us
      byte startByte = message[0];
      byte optionsByte = message[1];
      byte dataLengthByte = message[2];
      byte fragmentNoBytes[2];
      fragmentNoBytes[0] = message[3];
      fragmentNoBytes[1] = message[4];
      byte dataBytes[messageLength];
      for (int i = 0, j = 15; i < messageLength; i++, j++) {
        dataBytes[i] = message[j];
      }

      if ((optionsByte & 0b10000000) == 0) {
        //this was a text command
        executeCommand(byteArrayToString(dataBytes, (int)dataLengthByte));
      } else {
        //this was a binary command
        executeBinary(dataBytes, (int)dataLengthByte);
      }
    } else if (BROADCAST_TAG.compareTo(destination) == 0) {
      //a broadcast message.  respond with "READY"
      sendMessage("READY");
    } else {
      //this was a message for a different module
    }    
  } else {
    //message is too short to be properly formed
    //TODO send error log
  }
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

// void processCommand(String destination, String command) {
//  if  (destination == BROADCAST_TAG) {
//    //respond to broadcast requests with "READY"
//    sendMessage("READY");
//  } else if (destination == MODULE_NAME) {
//    //module specific parsing here
//    unsigned long int pin_num = command.toInt();
//    if (pin_num == 0) {
//      //the command could not be parsed
//      //debugMessage("Error converting " + command + " to an integer value");
//      // sendMessage("FAIL");
//    } else if (pin_num < 4 || pin_num > 9) {
//      //the given value was out of range
//      //debugMessage("Value of " + (String)pin_num + " is out of range");
//      // sendMessage("FAIL");
//    } else {
//      //debugMessage("Flashing LED num " + (String)pin_num);
//      //flash the LED
//      digitalWrite(pin_num, HIGH);
//      delay(300);
//      digitalWrite(pin_num, LOW);
//      //respond to the controler to indicate that the LED was flashed
//      // sendMessage("OK");
//    }
//  }
// }

void sendMessage(String command) {
  // debug("sendMessage()");
  uint8_t payload [15 + command.length()];
  payload[1] = (byte)0x00;
  payload[2] = (byte)command.length();
  payload[3] = (byte)0x00;
  payload[4] = (byte)0x00;

  byte destinationBytes[10] = {0};
  MODULE_NAME.getBytes(destinationBytes, MODULE_NAME.length() + 1);
  for (int i = 0, j = 5; i < 10; i++, j++) {
    payload[j] = destinationBytes[i];
  }

  byte commandBytes[command.length()];
  command.getBytes(commandBytes, command.length() + 1);
  for (int i = 0, j = 15; i < command.length(); i++, j++) {
    payload[j] = commandBytes[i];
  }

  payload[0] = (byte)0x0D;
  XBeeAddress64 addr64 = XBeeAddress64(0x00000000, 0x00000000);  //coordinator address
  ZBTxRequest zbTx = ZBTxRequest(addr64, payload, sizeof(payload));
  xbee.send(zbTx);
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
