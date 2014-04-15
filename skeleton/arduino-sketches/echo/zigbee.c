#include "zigbee.h"

Zigbee::Zigbee(String newName, int rx_pin, int tx_pin ) : name(newName) {
    xbee = XBee();
    softSerial = SoftwareSerial(rx_pin, tx_pin);
    msr = ModemStatusResponse();
    response = XBeeResponse();
    rx = ZBRxResponse();
    //name needs to be 10 bytes
}

Zigbee::~Zigbee() {
}

void Zigbee::start(int serial_port) {
    softSerial.begin(serial_port);
    xbee.begin(soft_serial);
}

void Zigbee::sendCommand(String command) {
    sendBinary((uint8_t *)command.toCharArray(), command.getLength());
}

void Zigbee::sendBinary(uint8_t array, int length) {
    XBeeAddress64 addr64 = XBeeAddress64(0x00000000, 0x00000000);  //coordinator address

    if(command.length() <= MAX_DATA_SIZE) {
        Message message((uint8_t)0x0D, 0, (uint8_t)command.length(), 0, name, (uint8_t*)command.toCharArray());
	uint8_t array[] = {message.getSignatureByte(), message,getOptionByte(), messgae.getDataLength(), message.getFragmentNumber(), message.getDestination(), message.getPayload()];
        ZBTxRequest zbTx = ZBTxRequest(addr64, array, sizeof(array)); //dont think this sizeof is going to work correctly
        xbee.send(zbTx);
    }
    else {
        int total_fragments = command.length() / MAX_DATA_SIZE;
        
	int count = 1;
        for(int i = 0; i <= message.length() - 1; i = i + 69) {
                
            //build the message then send it instead of storing it in an array				   
            String message_fragment = message.substring(i, MAX_DATA_SIZE * count); 
           Message message((uint8_t)0x0D, uint8_t option, message_fragment.length(), count-1, String destination_address, message_fragment.toCharArray());
	uint8_t array[] = {message.getSignatureByte(), message,getOptionByte(), messgae.getDataLength(), message.getFragmentNumber(), message.getDestination(), message.getPayload()];
	   ZBTxRequest zbTx = ZBTxRequest(addr64, array, sizeof(array)); //dont think this sizeof is going to work correctly
	xbee.send(zbTx);
            count++;
        }
    }	
}
}
