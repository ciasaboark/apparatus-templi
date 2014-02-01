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
// create reusable response objects for responses we expect to handle 
ZBRxResponse rx = ZBRxResponse();
ModemStatusResponse msr = ModemStatusResponse();

uint8_t payload[] = { 0, 0 };

// SH + SL Address of receiving XBee
XBeeAddress64 addr64 = XBeeAddress64(0x0013a200, 0x403e0f30);
ZBTxRequest zbTx = ZBTxRequest(addr64, payload, sizeof(payload));
ZBTxStatusResponse txStatus = ZBTxStatusResponse();

int statusLed = 6;
int errorLed = 5;
int dataLed = 7;

void flashLed(int pin, int times, int wait) {
    
    for (int i = 0; i < times; i++) {
      digitalWrite(pin, HIGH);
      delay(wait);
      digitalWrite(pin, LOW);
      
      if (i + 1 < times) {
        delay(wait);
      }
    }
}

void setup() {
  pinMode(statusLed, OUTPUT);
  pinMode(errorLed, OUTPUT);
  pinMode(dataLed,  OUTPUT);
  
  // start serial
  Serial.begin(9600);
  softSerial.begin(9600);
  
  xbee.begin(softSerial);
  //set pins 4 - 9 to output mode
  for (int i = 4; i < 10; i++) {
    pinMode(i, OUTPUT);
  }
  flashLed(statusLed, 3, 50);
  
}

uint8_t incomingSerial [3600] = {0};  //max frame size of 3600 bytes
int bytePos = 0;

// continuously reads packets, looking for ZB Receive or Modem Status
void loop() {
//  //read a byte from the usb serial
//  if (Serial.available() > 0) {
//    flashLed(7, 1, 100);
//    uint8_t inByte = Serial.read();
//    if (inByte != 0x0A) {
//      flashLed(statusLed, 1, 100);
//      incomingSerial[bytePos] = inByte;
//      bytePos++;
//    } else {
//      flashLed(statusLed, 2, 100);
//      processMessage(incomingSerial, bytePos);
//      bytePos = 0;
//      //clear the message buffer
//      for (int i = 0; i < sizeof(incomingSerial); i++) {
//        incomingSerial[i] = 0x00;
//      }
//      
//    }
//  }  
  
  
    xbee.readPacket(30);
    
    if (xbee.getResponse().isAvailable()) {
      // got something
      
      if (xbee.getResponse().getApiId() == ZB_RX_RESPONSE) {
        // got a zb rx packet
        
        // now fill our zb rx class
        xbee.getResponse().getZBRxResponse(rx);
            
        if (rx.getOption() == ZB_PACKET_ACKNOWLEDGED) {
            // the sender got an ACK
            flashLed(statusLed, 10, 10);
        } else {
            // we got it (obviously) but sender didn't get an ACK
            flashLed(errorLed, 2, 20);
        }
        // set dataLed PWM to value of the first byte in the data
          uint8_t data[rx.getDataLength()];
          Serial.print("data length: ");
          Serial.println(rx.getDataLength());
          Serial.print("sizeof: ");
          Serial.println(sizeof(data));
          
          for (int i = 0; i < rx.getDataLength(); i++) {
            data[i] = rx.getData(i);
          }
          
          for (int i = 0; i < sizeof(data); i++) {
            Serial.write(data[i]);
          }
          Serial.write('\n');
          processMessage(data, sizeof(data));
          
      } else if (xbee.getResponse().getApiId() == MODEM_STATUS_RESPONSE) {
        xbee.getResponse().getModemStatusResponse(msr);
        // the local XBee sends this response on certain events, like association/dissociation
        
        if (msr.getStatus() == ASSOCIATED) {
          // yay this is great.  flash led
          flashLed(statusLed, 10, 10);
        } else if (msr.getStatus() == DISASSOCIATED) {
          // this is awful.. flash led to show our discontent
          flashLed(errorLed, 10, 10);
        } else {
          // another status
          flashLed(statusLed, 5, 10);
        }
      } else {
        // not something we were expecting
        flashLed(errorLed, 1, 25);    
      }
    } else if (xbee.getResponse().isError()) {
      //nss.print("Error reading packet.  Error code: ");  
      //nss.println(xbee.getResponse().getErrorCode());
    }
}

void processMessage(uint8_t inData[], int inDataLength) {
  Serial.flush();
  byte startByte = inData[0];
  Serial.println("processmessage given data size: ");
  Serial.println(inDataLength);
  
  //read in the destination address
  String destination; // = String((char*)inData[1, sizeof(inData)]);
  int headerEnd = 0;
  for (int i = 1; i < inDataLength; i++) {
    if ((char)inData[i] != (char)0x3A) {
      Serial.println("appending to destination");Serial.flush();
      destination += (char)inData[i];
      headerEnd++;
    } else {
      Serial.println("found :");Serial.flush();
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
    
    Serial.println("header end: " + headerEnd); Serial.flush();
    Serial.println("destination: '" + destination + "'");Serial.flush();
    Serial.println("command: '" + command + "'");Serial.flush();
    
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

void sendMessage(String message) {
  //TODO
}
