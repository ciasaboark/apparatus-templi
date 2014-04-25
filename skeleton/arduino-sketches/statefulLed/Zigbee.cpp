#include "Zigbee.h"


Zigbee::Zigbee(char *newName, uint8_t rx_pin, uint8_t tx_pin) {
    xbee = new XBee();
    softSerial = new SoftwareSerial(rx_pin, tx_pin);
    msr = new ModemStatusResponse();
    response = new XBeeResponse();
    rx = new ZBRxResponse();
    name = (char *)malloc(sizeof(char) * 11);
    if(name != NULL) {
        strcpy(name, newName);
    }
}

Zigbee::~Zigbee() {
    delete(xbee);
    delete(softSerial);
    delete(msr);
    delete(response);
    delete(name);
    delete(rx);
}

void Zigbee::start(int buad_rate) {
    softSerial->begin(buad_rate); //data transmission rate. defaults to 9600
    /* Im not sure why it wants me to dereference the the pointer when it expects the address */
    xbee->begin(*softSerial); 
}

/* this is just a convience method so the user does not have to calculate the lenghth and convert
   it to an uint8_t arrray */
void Zigbee::sendCommand(String command) {
    int length = command.length();
    char array[length];
    command.toCharArray(array, length);
    sendMessage((uint8_t *)array, length, (uint8_t)0);
}

void Zigbee::sendBinary(uint8_t *data, int length) {
	sendMessage(data, length, (uint8_t)1);
}

void Zigbee::sendMessage(uint8_t *command, int length, uint8_t type) {
    XBeeAddress64 addr64 = XBeeAddress64(0x00000000, 0x00000000);  //coordinator address

    if(length <= MAX_DATA_SIZE) {
	uint8_t message_fragment[length + 13];
    	memset(message_fragment, 0, 69); //zero out the array
    
    	message_fragment[0] = (uint8_t)0x0D;

    	message_fragment[1] = type << 7;
    	message_fragment[2] = 69; //the length of the message_fragment
        message_fragment[3] = 0; //the fragment number

	for(int y = 4; y < 14; y++) {
            message_fragment[y] = (uint8_t)name[y - 4]; //copy the name into the array indexes
    	}
        for(int i = 14; i < 69; i++) {
            message_fragment[i] = command[i - 14];
        }
        ZBTxRequest zbTx = ZBTxRequest(addr64, message_fragment, sizeof(message_fragment));
        xbee->send(zbTx);
    }
    else {
        int total_fragments = length / MAX_DATA_SIZE;
        uint8_t message_fragment[69];
    	memset(message_fragment, 0, 69); //zero out the array
    
    	message_fragment[0] = (uint8_t)0x0D;
    	message_fragment[1] = 0;
    	message_fragment[2] = 69; //the length of the message_fragment
        message_fragment[3] = 0;  //the fragment number

		for(int y = 4; y < 14; y++) {
            message_fragment[y] = (uint8_t)name[y - 4]; //copy the name into the array indexes
    	}
        
		int index = 0;
   	 	int fragmentNum = 0;
    	for(int x = 0; x <= total_fragments - 1; x++) { 
    		message_fragment[3] = fragmentNum; //the fragment number
	        for(int z = 14; z < 69; z++) {
	            message_fragment[z] = command[index];
    	        index++;
	        }
    		ZBTxRequest zbTx = ZBTxRequest(addr64, message_fragment, sizeof(message_fragment)); 
		    xbee->send(zbTx);
	    	fragmentNum++;        
   	 	}
    }	
}

void Zigbee::sendMessageFragment(Message *obj) {
    
}

Message* Zigbee::receiveMessage() {
	Message *message = NULL;

	xbee->readPacket(300);
		
		if (xbee->getResponse().isAvailable()) {
			Serial.println("--------\nzigbee packet available");
			
			// got something
			
			if (xbee->getResponse().getApiId() == ZB_RX_RESPONSE) {
				Serial.println("zigbee rx response packet");
				// got a zb rx packet
				
				// now fill our zb rx class
//                                ZBRxResponse
				xbee->getResponse().getZBRxResponse(*rx);
						
				if (rx->getOption() == ZB_PACKET_ACKNOWLEDGED) {
						// the sender got an ACK
				} else {
						// we got it (obviously) but sender didn't get an ACK
				}

			

				Serial.println("incoming zigbee packet");
				Serial.println("processing message");
				uint8_t *data = rx->getData();

				Serial.print("Start byte: " );
				Serial.println(data[0]);
				
				Serial.print("data length: " );
				Serial.println(data[2]);
				//TODO strcmp wont know where to stop if name is all 10 bytes
				Serial.print("my name name is: ");
				Serial.println(name);

							
				// char* remoteName = (char*)malloc(11);
				char remoteName[] = "abcdefghij";
				

				Serial.println("***");
				for (int i = 5; i < 15; i++) {
					remoteName[i - 5] = data[i];
				}
				Serial.println("***");

				Serial.print("message addressed to: ");
				Serial.println(remoteName);

				int sameName = strcmp(remoteName,name);
				Serial.print("samename: ");
				Serial.println(sameName);

				int broadcastID = strcmp((char*)data[10], "ALL");
				Serial.print("broadcastID: ");
				Serial.println(broadcastID);
				uint16_t *number = (uint16_t*)data[3];
				uint8_t *command = (uint8_t*)data[15];

				if(sameName == 0 && data[0] == (uint8_t)0x0D) {
					Serial.println("message addressed to us");
					if( (15 + data[2]) == rx->getDataLength() ) {
						Serial.println("message  was correct length");
						message = new Message(data[0], data[1], data[2], *number,  (char*)data[5], (uint8_t*)data[15]);
					}	
				} else if (broadcastID == 0 && data[0] == (uint8_t)0x0D) {
					Serial.println("message addressed to ALL");
					digitalWrite(7, HIGH);
					delay(100);
					digitalWrite(7, LOW);
					sendCommand(name);
				}
					
			} else if (xbee->getResponse().getApiId() == MODEM_STATUS_RESPONSE) {
				xbee->getResponse().getModemStatusResponse(*msr);
				// the local XBee sends this response on certain events, like association/dissociation
				
				if (msr->getStatus() == ASSOCIATED) {
					// yay this is great.  flash led
				} else if (msr->getStatus() == DISASSOCIATED) {
					// this is awful.. flash led to show our discontent
				} else {
					// another status
				}
			} else {
				// not something we were expecting   
			}
		} else if (xbee->getResponse().isError()) {
			Serial.print("Error reading packet.  Error code: ");  
			Serial.println(xbee->getResponse().getErrorCode());
		}
		return message;
}
