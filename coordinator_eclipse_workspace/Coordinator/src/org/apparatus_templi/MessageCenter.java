package org.apparatus_templi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

public class MessageCenter implements Runnable {
	private static final String TAG = "MessageCenter";
	
	private static final int MAX_DATA_SIZE = 69;
	
	//holds all incoming messages
	private LinkedBlockingDeque<Message> messageQueue = new LinkedBlockingDeque<Message>();
	
	//holds all incoming bytes
	private LinkedBlockingQueue<Byte> incomingBytes = new LinkedBlockingQueue<Byte>();
	
	//group incoming message fragments by the destination byte[]
	private HashMap<byte[], FragmentedMessage> messageFragments = new HashMap<byte[], FragmentedMessage>();;
	
	private boolean readMessages = false;
	
	private static MessageCenter instance;
	private SerialConnection serialConnection;
	private boolean serialConnectionReady = false;
	
	private MessageCenter() {
		//Singleton pattern
	}
	
	private void readMessage() throws InterruptedException, IOException {
		//read the first 15 bytes
		//TODO check for a correct start byte 0x0D
		byte startByte = incomingBytes.take();
		byte optionsByte = incomingBytes.take();
		byte dataLengthByte = incomingBytes.take();
		byte[] fragmentNumBytes = new byte[2];
		fragmentNumBytes[0] = incomingBytes.take();
		fragmentNumBytes[1] = incomingBytes.take();
		byte[] destinationBytes = new byte[10];
		byte[] payloadData;
		int fragmentNum = fragmentNumBytes[0];
		fragmentNum = fragmentNum << 8;
		fragmentNum += fragmentNumBytes[1];
		
		
		for (int i = 0; i < 10; i++) {
			destinationBytes[i] = incomingBytes.take();
		}
		
		int dataLength = (int)dataLengthByte;
//		Log.d(TAG, "incoming message: data len: " + dataLength + " fragNum: " + fragmentNum);
		
		if (startByte == 0x0D) {
			//wait until the payload data is avaiable
			while (incomingBytes.size() < dataLength) {
				Log.d(TAG, "waiting on " + dataLength + " data bytes, " + incomingBytes.size() + " available");
				Thread.yield();
				Thread.sleep(30);
			}
			//read the payload data
			payloadData = new byte[dataLength];
			for (int i = 0; i < dataLength; i++) {
				payloadData[i] = incomingBytes.take();
			}
			
			if (messageFragments.containsKey(destinationBytes) || fragmentNum != 0) {
				storeMessageFragment(optionsByte, dataLengthByte, fragmentNum, destinationBytes, payloadData);
			} else {
				Message m = new Message(optionsByte, dataLength, destinationBytes, payloadData);
				messageQueue.put(m);
			}
			
			
		} else {
			Log.w(TAG, "readMessage() read a malformed message, discarding this byte");
//			readBytesUntil(Message.START_BYTE);
		}
	}

	private void storeMessageFragment(byte optionsByte, int dataLength, int fragmentNum,
											 byte[] destinationBytes, byte[] payloadData) throws IOException {
		MessageFragment mf = new MessageFragment(optionsByte, dataLength, fragmentNum, destinationBytes, payloadData);
		FragmentedMessage fragments = messageFragments.get(destinationBytes);
		if (fragments != null) {
			fragments.storeFragment(mf);
			messageFragments.put(destinationBytes, fragments);
		} else {
			fragments = new FragmentedMessage();
			fragments.storeFragment(mf);
			
			//If this fragment completes the message then put it in the queue
			//+ otherwise place it back in the map
			if (fragments.isMessageComplete()) {
				Message m = fragments.getCompleteMessage();
				messageQueue.add(m);
			} else {
				messageFragments.put(destinationBytes, fragments);
			}
		}
		
	}
	
	public static MessageCenter getInstance() {
		if (instance == null) {
			instance = new MessageCenter();
		}
		return instance;
	}
	
	public synchronized Message getMessage() {
		Message m = null;
		m = messageQueue.poll();
		return m;
	}
	
	public synchronized boolean isMessageAvailable() {
		return !messageQueue.isEmpty();
	}
	
	public synchronized int queuedMessages() {
		return messageQueue.size();
	}

	public void incomingSerial(byte b) throws InterruptedException, IOException {
//		Log.d(TAG, "incomingSerial(0x" + Integer.toString(b, 16) + ") char:" + new String(new byte[] {b}));
		incomingBytes.put(b);
	}
	
	public synchronized boolean sendCommand(String moduleName, String command) {
		//TODO add support for more options
		boolean sendMessage = true;
		boolean messageSent = false;
		int dataLength = 0;
		byte[] fragmentNum = {0x00, 0x00};
		byte[] destinationBytes = new byte[10];
		byte[] commandBytes = {};
		
		try {
			destinationBytes = moduleName.getBytes("UTF-8");
			commandBytes = command.getBytes("UTF-8");
			dataLength = commandBytes.length;
			
			if (dataLength > MessageCenter.MAX_DATA_SIZE) {
				Log.w(TAG, "sendCommand() command is too large, automatic fragmentation not yet supported");
				sendMessage = false;
			}
		} catch (UnsupportedEncodingException e) {
			Log.w(TAG, "unable to translate command '" + command + "' into ASCII byte[]");
			sendMessage = false;
		}

		//build the outgoing message byte[]
		if (sendMessage) {
			messageSent = true;
//			Log.d(TAG, "sendCommand() sending '" + command + "' to '" + moduleName);
			byte[] message = new byte[15 + dataLength];
			message[0] = Message.START_BYTE;
			message[1] = Message.TYPE_TEXT;
			message[2] = (byte)dataLength;
			message[3] = fragmentNum[0];
			message[4] = fragmentNum[1];
			//copy the destination bytes
			for (int i = 5, j = 0; j < destinationBytes.length; i++, j++) {
				message[i] = destinationBytes[j];
			}
			
			//copy the data bytes
			for (int i = 15, j = 0; j < dataLength; i++, j++) {
				message[i] = commandBytes[j];
			}
			
			serialConnection.writeData(message);
		}
		
		
		return messageSent;
	}

	public boolean sendBinary(String moduleName, byte[] data) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public void beginReadingMessages() {
		readMessages = true;
	}
	
	public byte[] readBytesUntil(byte termByte) throws InterruptedException {
		byte[] bytes = {};
		if (readMessages) {
			Log.w(TAG, "asked to read raw bytes while automatic processing is ongoing, returning empty byte[]");
		}
		
		ByteArrayOutputStream inBytes = new ByteArrayOutputStream();
		byte b = incomingBytes.take();
		int discardedBytes = 1;
		while (b != termByte) {
			inBytes.write(b);
			b = incomingBytes.take();
			discardedBytes++;
		}
		bytes = inBytes.toByteArray();
//		Log.d(TAG, "discarded " + discardedBytes + " bytes");
		return bytes;
	}
	
	public boolean isSerialConnected() {
		return serialConnectionReady;
	}

	public void setSerialConnection(SerialConnection serialConnection) {
		this.serialConnection = serialConnection;
		
	}

	@Override
	public void run()  {
		while (true){
			if (readMessages) {
				try {	
					if (incomingBytes.size() >= 15) {
						//enough bytes have been read to form a complete message,
						//+ begin processing
						readMessage();
					} else {
//						Log.d(TAG, "waiting on more serial input");
						Thread.sleep(200);
						Thread.yield();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
	}
}

class Message {
	static final byte START_BYTE = (byte)0x0D;
	
	static final byte TYPE_TEXT = (byte)0b0000_0000;
	static final byte TYPE_BIN  = (byte)0b1000_0000;
	
	static final int TEXT_TRANSMISSION = 0;
	static final int BINARY_TRANSMISSION = 1;

	
	private byte options;
	private int dataLength;
	private byte[] destination;
	private byte[] data;
	
	public Message(byte options, int dataLength, byte[] destination, byte[] data) {
		this.options = options;
		this.dataLength = dataLength;
		this.destination = destination;
		this.data = data;
	}

	public Message() {}
	
	public byte[] getDestination() {
		return destination;
	}

	public byte[] getData() {
		return data;
	}

	public byte getOptions() {
		return options;
	}

	public int getDataLength() {
		return dataLength;
	}	

	public void setOptions(byte options) {
		this.options = options;
	}

	public void setDataLength(int dataLength) {
		this.dataLength = dataLength;
	}

	public void setDestination(byte[] destination) {
		this.destination = destination;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public int getTransmissionType() {
		return (options & TYPE_BIN) >> 7;
	}
}
	
	
class MessageFragment {
	private byte options;
	private int  dataLength;
	private int fragmentNo;
	private byte[] destination;
	private byte[] data;
	
	public MessageFragment (byte options, int dataLength, int fragmentNum, byte[] destination, byte[] data) {
		this.options = options;
		this.dataLength = dataLength;
		this.fragmentNo = fragmentNum;
		this.destination = destination;
		this.data = data;
	}
	
	public byte getOptions() {
		return options;
	}
	
	public int getDataLength() {
		return dataLength;
	}

	public int getFragmentNo() {
		return fragmentNo;
	}

	public byte[] getDestination() {
		return destination;
	}

	public byte[] getData() {
		return data;
	}
}

class FragmentedMessage {
	static final String TAG = "FragmentedMessage";
	private SortedMap<Integer, MessageFragment> fragments;
	
	public void storeFragment(MessageFragment fragment) {
		int fragmentNum = fragment.getFragmentNo();
		if (fragments.containsKey(fragmentNum)) {
			//oops somehow we got two fragments
			Log.e(TAG,  "incoming message fragment '" + fragmentNum + "' will overwrite an existing fragment");
		}
		fragments.put(fragmentNum, fragment);
	}
	
	Message getCompleteMessage() throws IOException {
		Message m = new Message();
		if (!isMessageComplete()) {
			Log.w(TAG, "attempting generating a complete message without all fragments");
		}
		ByteArrayOutputStream dataBytes = new ByteArrayOutputStream();
		MessageFragment firstFragment = fragments.get(fragments.firstKey());
		//TODO check all fragments have the same option byte?
		m.setOptions(firstFragment.getOptions());
		m.setDestination(firstFragment.getDestination());
		m.setDataLength(firstFragment.getDataLength());
		for (Integer fragNum: fragments.keySet()) {
			dataBytes.write(fragments.get(fragNum).getData());
		}
		m.setData(dataBytes.toByteArray());
		
		return m;
	}

	public boolean isMessageComplete() {
		boolean isComplete = true;
		int i = 0;
		for (Integer fragNum: fragments.keySet()) {
			if (i != fragNum) {
				isComplete = false;
				break;
			}
			i++;
		}
		return isComplete;
	}

}


;