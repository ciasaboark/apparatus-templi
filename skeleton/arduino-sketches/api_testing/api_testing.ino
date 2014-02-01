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

// create the XBee object
XBee xbee = XBee();
XBeeResponse response = XBeeResponse();

// create reusable response objects for responses we expect to handle 
ZBRxResponse rx = ZBRxResponse();
ModemStatusResponse msr = ModemStatusResponse();

SoftwareSerial sSerial = SoftwareSerial(10, 11);
uint8_t upper = 0x00;
uint8_t lower = 0x01;

uint8_t payload[] = { 0, 0 };

// SH + SL Address of receiving XBee
//XBeeAddress64 addr64 = XBeeAddress64(0x0013a200, 0x403e0f30);
XBeeAddress64 addr64 = XBeeAddress64(0x00000000, 0x00000000);
ZBTxRequest zbTx = ZBTxRequest(addr64, payload, sizeof(payload));
ZBTxStatusResponse txStatus = ZBTxStatusResponse();

int pin5 = 0;

int statusLed = 6;
int errorLed = 5;



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
  pinMode(7, OUTPUT);

  Serial.begin(9600);
  sSerial.begin(9600);
  xbee.setSerial(sSerial);
  flashLed(statusLed, 2, 100);
  flashLed(errorLed, 2, 100);
  flashLed(7, 2, 100);
  Serial.println("READY");
}

String incommingSerial = "";

void sendMessage(byte data[]) {
  flashLed(errorLed, 1, 100);
  for (int i = 0; i < sizeof(data); i++) {
    Serial.println(data[i]);
  }
}

void processMessage(uint8_t messageBytes[], int termPos) {
  for (int i = 0; i < termPos; i++) {
    Serial.write(messageBytes[i]);
  }
}

uint8_t incomingSerial [3600] = {0};  //max frame size of 3600 bytes
int bytePos = 0;
String message;

void loop() {
  //read a byte from the usb serial
  if (Serial.available() > 0) {
    flashLed(7, 1, 100);
    uint8_t inByte = Serial.read();
    if (inByte != 0x0A) {
      flashLed(statusLed, 1, 100);
      incomingSerial[bytePos] = inByte;
      bytePos++;
    } else {
      flashLed(statusLed, 2, 100);
      processMessage(incomingSerial, bytePos);
      bytePos = 0;
      //clear the message buffer
      for (int i = 0; i < sizeof(incomingSerial); i++) {
        incomingSerial[i] = 0x00;
      }
      
    }
  }
  
  //read an incoming packet from the xbee (if a packet is avaiable)
  if (xbee.readPacket(30)) {
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
        //analogWrite(dataLed, rx.getData(0));
        flashLed(statusLed, 4, 100);
        Serial.write(rx.getData(0));
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
  
  flashLed(errorLed, 1, 2000);
  delay(1000);
}


