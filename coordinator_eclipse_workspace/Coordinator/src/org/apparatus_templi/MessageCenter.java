package org.apparatus_templi;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingDeque;

public class MessageCenter {
	//holds all incoming messages
	private static LinkedBlockingDeque<Message> messageQueue = new LinkedBlockingDeque<Message>();
	
	//holds all incoming bytes
	private static ByteArrayOutputStream incomingBytes;
	
	//incoming message fragments a
	private static HashMap<byte[], MessageFragment> messageFragments;
	
	
	private static final byte TEXT_TRANSMISSION = (byte)0b0000_0000;
	private static final byte BIN_TRANSMISSION  = (byte)0b1000_0000;

	private static SerialConnection serialConnection;
	private static boolean connectionReady = false;
	
	private static synchronized void putMessage() {
		
	}
	
	public static synchronized Message getMessage() {
		Message m = null;
		m = messageQueue.poll();
		return m;
	}
	
	public static synchronized boolean isMessageAvailable() {
		return !messageQueue.isEmpty();
	}
	
	public static synchronized int queuedMessages() {
		return messageQueue.size();
	}

	public static synchronized void incomingSerial(byte b) {
		incomingBytes.write(b);
		if (incomingBytes.size() >= 15) {
			//enough bytes have been read to form a complete message,
			//+ begin processing
			readMessage();
		}
		
	}

	private static void readMessage() {
		// TODO Auto-generated method stub
		
	}
		
}

class Message {
	public static final int TYPE_TEXT = 0;
	public static final int TYPE_BIN  = 1;
	
	private byte[] destination;
	private byte[] data;
	
	public byte[] getDestination() {
		return destination;
	}
	public void setDestination(byte[] destination) {
		this.destination = destination;
	}
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}
}
	
	
class MessageFragment {
	public byte getOptions() {
		return options;
	}
	public void setOptions(byte options) {
		this.options = options;
	}
	public byte getDataLength() {
		return dataLength;
	}
	public void setDataLength(byte dataLength) {
		this.dataLength = dataLength;
	}
	public byte[] getFragmentNo() {
		return fragmentNo;
	}
	public void setFragmentNo(byte[] fragmentNo) {
		this.fragmentNo = fragmentNo;
	}
	public byte[] getDestination() {
		return destination;
	}
	public void setDestination(byte[] destination) {
		this.destination = destination;
	}
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}
	private byte options;
	private byte  dataLength;
	private byte[] fragmentNo;
	private byte[] destination;
	private byte[] data;
}

class FragmentedMessage {
	private HashMap<Integer, MessageFragment> fragments;
	
	public void storeFragment(Integer fragmentNumber, MessageFragment fragment) {
		
	}
	
	public boolean isMessageComplete() {
		//TODO
		return false;
	}
	
	public Message getFullMessage() {
		//TODO
		return null;
	}
	}
	
	
}



