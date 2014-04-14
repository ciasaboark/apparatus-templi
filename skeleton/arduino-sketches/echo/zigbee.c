#include "zigbee.h"

Zigbee::Zigbee() {
    xbee = XBee();
    softSerial = SoftwareSerial(10, 11);
    msr = ModemStatusResponse();
    response = XBeeResponse();
    rx = ZBRxResponse();
}

Zigbee::~Zigbee() {
}

uint8_t Zigbee::start(int serial_port) {
    uint8_t status = 0;
    softSerial.begin(serial_port);
    xbee.begin(soft_serial);

    return status;
}

uint8_t Zigbee::sendCommand(String command) {
    uint8_t success = 0;
    XBeeAddress64 addr64 = XBeeAddress64(0x00000000, 0x00000000);  //coordinator address
    if(command.length() <= MAX_DATA_SIZE) {
        Message message((uint8_t)0x0D, uint8_t option, (uint8_t)command.length(), 0, String destination_address, command.toCharArray());
	/* Im not really sure about the message.getPayload() because the data is not in the form of an uint8_t pointer. casting may not work
	   either because the data is not incremental by bytes, so I need to look more into the method. Maybe the ZBTxRequest just send a byte of 	     data at a time? */
        ZBTxRequest zbTx = ZBTxRequest(addr64, (uint8_t *)&message, sizeof(message)); //dont think this sizeof is going to work correctly
        xbee.send(zbTx);
    }
    else {
        int total_fragments = message.length() / MAX_DATA_SIZE;
        Message message[total_fragments];
        
	int count = 1;
        for(int i = 0; i <= message.length() - 1; i = i + 69) {
                
            /* this is an issue if the command is of odd size because of casting. 
             * If message.length is 104 bytes then int total_fragments = message.length() / MAX_DATA_SIZE 
             * will be 1. So, it will disccard the rest of the message */				   
            String message_fragment = message.substring(i, MAX_DATA_SIZE * count); 
            message[count-1] = Message message((uint8_t)0x0D, uint8_t option, message_fragment.length(), count-1, String destination_address, message_fragment.toCharArray());
            count++;
        }
	ZBTxRequest zbTx = ZBTxRequest(addr64, (uint8_t *)&message, sizeof(message)); //dont think this sizeof is going to work correctly
	xbee.send(zbTx);
    }	
}

uint8_t Zigbee::sendBinary(uint8_t array, int length) {
    uint8_t success = 0;
}
