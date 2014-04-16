#ifndef LIBRARY_H
#define LIBRARY_H

class Message {
	private:
		uint8_t  signature;
		uint8_t  optionByte;
		uint8_t  data_length;
		uint16_t fragment_number;
		char     destination_name[10];		
		uint8_t  *payload;
	
	public:
		Message(uint8_t sigByte, uint8_t option, uint8_t length, uint16_t frag_number, String destination_address, uint8_t* payld);
		~Message();
		Message(const Message obj);

		void setSignatureByte(uint8_t sigByte);
		uint8_t getSignatureByte();

		void setOptionByte(uint8_t option);
		uint8_t getOptionByte();

		void setDestinationName(String module);
		String getDestinationName();

		void setDataLength(uint8_t length);
		uint8_t getDataLength();
		
		void setFragmentNumber(uint16_t fragment_number);
		uint16_t getFragmentNumber();

		void setPayload(uint8_t *buffer);
		uint8_t* getPayload();
		
};

#endif
