#include "Zigbee.h"


Zigbee::Zigbee(char *newName, uint8_t rx_pin, uint8_t tx_pin) {
    xbee = new XBee();
    softSerial = new SoftwareSerial(rx_pin, tx_pin);
    msr = new ModemStatusResponse();
    response = new XBeeResponse();
    rx = new ZBRxResponse();
    name = (char *)malloc(sizeof(char) * 11);
    if(name != NULL) {
		memset(name, NULL, 11); //null out the name memory
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
    xbee->begin(*softSerial); 
}

/* this is just a convenience method so the user does not have to calculate the lenghth and convert
   it to an uint8_t array */
void Zigbee::sendCommand(String command) {
    int length = command.length();
    char array[length]; //stack allocating
    command.toCharArray(array, length);
    sendMessage((uint8_t *)array, length, (uint8_t)0);
}

void Zigbee::sendBinary(uint8_t *data, int length) {
	sendMessage(data, length, (uint8_t)1);
}

void Zigbee::sendMessage(uint8_t *command, int length, uint8_t type) {
    XBeeAddress64 addr64 = XBeeAddress64(0x00000000, 0x00000000);  //coordinator address

    if(length <= MAX_DATA_SIZE) {
	uint8_t message_fragment[length + 15];
    	memset(message_fragment, NULL, 69); //NULL out the array
    
    	message_fragment[0] = (uint8_t)0x0D;
    	message_fragment[1] = type << 7;
    	message_fragment[2] = (uint8_t)length; //the length of the message_fragment
        message_fragment[3] = 0; //the fragment number
		message_fragment[4] = 0; //is a two byte field

		for(int y = 5; y < 15; y++) {
            message_fragment[y] = (uint8_t)name[y - 5]; //copy the name into the array indexes
    	}
        for(int i = 15; i < length + 15; i++) {
            message_fragment[i] = command[i - 15];
        }
        ZBTxRequest zbTx = ZBTxRequest(addr64, message_fragment, length + 15);
        xbee->send(zbTx);
    }
    else {
	/* this is still going to be an issue. Will fix later*/
        uint16_t total_fragments = length / MAX_DATA_SIZE;
		uint16_t message_count = 0;
		
		while(message_count != total_fragments) {
			uint8_t message_fragment[69];
			memset(message_fragment, NULL, 69); //NULL out the array
    
			message_fragment[0] = (uint8_t)0x0D;
			message_fragment[1] = type << 7;
			message_fragment[2] = length / total_fragments; //the length of the data fragment

			for(int y = 5; y < 15; y++) {
				message_fragment[y] = (uint8_t)name[y - 5]; //copy the name into the array indexes
			}
        
			for(uint16_t x = 0; x <= total_fragments - 1; x++) { 
				/* Cast the message_fragment[3] to be an uint16_t pointer , so I can set 
				the two bytes field of message fragment number by dereferencing the address */
				*((uint16_t*)(&message_fragment[3])) = message_count; 
			
				for(int z = 15; z < length + 15; z++) { 
					message_fragment[z] = command[z - 15];
				}
				//as of right now with the casting bug up top, the size will always be 69 bytes
				ZBTxRequest zbTx = ZBTxRequest(addr64, message_fragment, 69); 
				xbee->send(zbTx);
				message_count++;  
			}				
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
				} 			

				Serial.println("incoming zigbee packet");
				Serial.println("processing message");
				uint8_t *data = rx->getData();

				Serial.print("Start byte: " );
				Serial.println(data[0]);
				
				Serial.print("data length: " );
				Serial.println(data[2]);
				
				Serial.print("my name from the Zigbee class is: ");
				Serial.println(name);				

				Serial.println("***");
				Serial.print("my name from the sent message is: ");
				for (int i = 5; i < 15; i++) {
					Serial.print((char*)data[i]); //this will be the Ascii values if Serial.print does not interpret it as chars
				}

				Serial.println("***");

				int sameName = strcmp((char *)data + 5, name);
				Serial.print("The value of the name compare is (should be zero if they are equal): ");
				Serial.println(sameName);

				int broadcastID = strcmp((char*)data[10], "ALL");
				Serial.print("broadcastID: ");
				Serial.println(broadcastID);

				uint16_t *number = ((uint16_t*)&data[3]); //fragment number
				//uint8_t *command = (uint8_t*)data[15];

				if(sameName == 0 && data[0] == (uint8_t)0x0D) {
					Serial.println("message addressed to us");
					if( (data[2] + 15) == rx->getDataLength() ) {
						Serial.println("message  was correct length");
						message = new Message(data[0], data[1], data[2], *number,  (char*)data[5], (uint8_t*)data[15]);
					}	
				} 

				else if (broadcastID == 0 && data[0] == (uint8_t)0x0D) {
					Serial.println("message addressed to ALL");
					digitalWrite(7, HIGH);
					delay(100);
					digitalWrite(7, LOW);
					sendCommand(name);
				}
				delete data; //we no longer need data because the message has a copy of it now				
			} 

			else if (xbee->getResponse().getApiId() == MODEM_STATUS_RESPONSE) {
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
		} 
		
		else if (xbee->getResponse().isError()) {
			Serial.print("Error reading packet.  Error code: ");  
			Serial.println(xbee->getResponse().getErrorCode());
		}
		return message;
}
