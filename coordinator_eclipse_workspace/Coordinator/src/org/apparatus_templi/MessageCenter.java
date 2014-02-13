package org.apparatus_templi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

public class MessageCenter implements Runnable {
	private static final String TAG = "MessageCenter";
	
	//holds all incoming messages
	private LinkedBlockingDeque<Message> messageQueue = new LinkedBlockingDeque<Message>();
	
	//holds all incoming bytes
	private LinkedBlockingQueue<Byte> incomingBytes = new LinkedBlockingQueue<Byte>();
	
	//group incoming message fragments by the destination byte[]
	private HashMap<String, FragmentedMessage> fragmentedMessages = new HashMap<String, FragmentedMessage>();;
	
	private boolean readMessages = false;
	
	private static MessageCenter instance;
	private SerialConnection serialConnection;
	private boolean serialConnectionReady = false;
	
	private MessageCenter() {
		//Singleton pattern
	}
	
	private void readMessage() throws InterruptedException, IOException {
		//Read the start byte by itself so we can see if this
		//+ is really the beginning of a message
		//TODO check for a correct start byte 0x0D
		byte startByte = incomingBytes.take();
		if (startByte == Message.START_BYTE) {
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
			String destination = "";
			try {
				destination = new String(destinationBytes, "UTF-8").trim();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			
			int dataLength = (int)dataLengthByte;
			Log.d(TAG, "incoming message: data len: " + dataLength + " fragNum: " + fragmentNum);
			
			//wait until the payload data is avaiable
			while (incomingBytes.size() < dataLength) {
//				Log.d(TAG, "waiting on " + dataLength + " data bytes, " + incomingBytes.size() + " available");
				Thread.yield();
				Thread.sleep(30);
			}
			//read the payload data
			payloadData = new byte[dataLength];
			for (int i = 0; i < dataLength; i++) {
				payloadData[i] = incomingBytes.take();
			}
			
			if (fragmentedMessages.containsKey(destination) || fragmentNum != 0) {
				storeMessageFragment(optionsByte, dataLengthByte, fragmentNum, destination, payloadData);
			} else if (fragmentedMessages.containsKey(destination) && fragmentNum == 0) {
				storeMessageFragment(optionsByte, dataLengthByte, fragmentNum, destination, payloadData);
				if (fragmentedMessages.get(destination).isMessageComplete()) {
					Log.d(TAG, "fragmented message to " + destination + " is complete");
					Message m = fragmentedMessages.get(destination).getCompleteMessage();
					fragmentedMessages.remove(destination);
					Log.d(TAG, "adding incoming message addressed to: " + m.getDestination() + " to the queue");
					messageQueue.put(m);
				} else {
					Log.d(TAG, "waiting on more fragments for message to: " + destination);
				}
			} else {
				Message m = new Message(optionsByte, dataLength, destination, payloadData);
				Log.d(TAG, "adding incoming message addressed to: " + m.getDestination() + " to the queue");
				messageQueue.put(m);
			}
			
			
		} else {
			//Since this byte did not match the start byte we will discard it.
			//+ This process will continue until a proper start byte is found.
			Log.w(TAG, "readMessage() read a malformed message, discarding this byte");
		}
	}

	private void storeMessageFragment(byte optionsByte, int dataLength, int fragmentNum,
											 String destination, byte[] payloadData) throws IOException {
		MessageFragment mf = new MessageFragment(optionsByte, dataLength, fragmentNum, destination, payloadData);
		FragmentedMessage fragments = fragmentedMessages.get(destination);
		if (fragments != null) {
			fragments.storeFragment(mf);
			fragmentedMessages.put(destination, fragments);
		} else {
			fragments = new FragmentedMessage();
			fragments.storeFragment(mf);
			
			//If this fragment completes the message then put it in the queue
			//+ otherwise place it back in the map
			if (fragments.isMessageComplete()) {
				Message m = fragments.getCompleteMessage();
				messageQueue.add(m);
			} else {
				fragmentedMessages.put(destination, fragments);
			}
		}
		
	}
	
	static MessageCenter getInstance() {
		if (instance == null) {
			instance = new MessageCenter();
		}
		return instance;
	}
	
	synchronized Message getMessage() {
		Message m = null;
		m = messageQueue.poll();
		return m;
	}
	
	synchronized boolean isMessageAvailable() {
		return !messageQueue.isEmpty();
	}
	
	synchronized int queuedMessages() {
		return messageQueue.size();
	}

	void incomingSerial(byte b) throws InterruptedException, IOException {
//		Log.d(TAG, "incomingSerial(0x" + Integer.toString(b, 16) + ") char:" + new String(new byte[] {b}));
		incomingBytes.put(b);
	}
	
	synchronized boolean sendCommand(String moduleName, String command) {
		boolean sendMessage = true;
		boolean messageSent = false;
		byte[] commandBytes = {};
		
		try {
			commandBytes = command.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			sendMessage = false;
		}
		
		if (sendMessage) {
			messageSent = sendMessage(moduleName, commandBytes, Message.OPTION_TYPE_TEXT);
		}
		
		return messageSent;
	}
	
	synchronized boolean sendBinary(String moduleName, byte[] data) {
		return sendMessage(moduleName, data, Message.OPTION_TYPE_BIN);
	}
	
	private boolean sendMessage(String moduleName, byte[] data, byte options) {
		boolean messageSent = false;
		//If the data block will fit within a single fragment, then send it
		if (data.length <= Message.MAX_DATA_SIZE) {
			messageSent = sendMessageFragment(moduleName, data, 0, options);
		} else {
			//break the data into multiple fragments of MAX_DATA_SIZE or less
			int curPos = 0;
			int fragmentNum = (data.length / Message.MAX_DATA_SIZE);
			while (fragmentNum >= 0) {
				int payloadSize = ((data.length - curPos) > Message.MAX_DATA_SIZE) ? Message.MAX_DATA_SIZE : data.length - curPos;
				byte[] payload = new byte[payloadSize];
				for (int i = 0; i < payloadSize; i++, curPos++) {
					payload[i] = data[curPos];
				}
				messageSent = sendMessageFragment(moduleName, payload, fragmentNum, options);
				fragmentNum--;
			}
		}
		return messageSent;
	}
	
	private boolean sendMessageFragment(String moduleName, byte[] data, int fragmentNumber, byte optionsByte) {
		//TODO add support for more options
		boolean sendMessage = true;
		boolean messageSent = false;
		if (data.length <= Message.MAX_DATA_SIZE && fragmentNumber >= 0) {
			byte[] fragmentNumberBytes = ByteBuffer.allocate(4).putInt(fragmentNumber).array();
			byte[] fragmentNum = {fragmentNumberBytes[2], fragmentNumberBytes[3]};
			byte[] destinationBytes = new byte[10];
			
			
			try {
				destinationBytes = moduleName.getBytes("UTF-8");
			} catch (UnsupportedEncodingException e) {
				Log.d(TAG, "unable to translate destination '" + moduleName + "' into UTF-8 byte[]");
				sendMessage = false;
			}
	
			//build the outgoing message byte[]
			if (sendMessage) {
				messageSent = true;
				Log.d(TAG, "sendMessageFragment() sending '" + data.length + "' bytes to '" + moduleName
						+ "', fragment num: " + fragmentNumber + " of type "
						+ ((optionsByte == Message.OPTION_TYPE_TEXT) ? "text" : "bin"));
				byte[] message = new byte[15 + data.length];
				message[0] = Message.START_BYTE;
				message[1] = optionsByte;
				message[2] = (byte)data.length;
				message[3] = fragmentNum[0];
				message[4] = fragmentNum[1];
				//copy the destination bytes
				for (int i = 5, j = 0; j < destinationBytes.length; i++, j++) {
					message[i] = destinationBytes[j];
				}
				
				//copy the data bytes
				for (int i = 15, j = 0; j < data.length; i++, j++) {
					message[i] = data[j];
				}
				
				serialConnection.writeData(message);
			}
		}
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		return messageSent;
	}

	void beginReadingMessages() {
		readMessages = true;
	}
	
	byte[] readBytesUntil(byte termByte) throws InterruptedException {
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
	
	boolean isSerialConnected() {
		return serialConnectionReady;
	}

	void setSerialConnection(SerialConnection serialConnection) {
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
	static final int MAX_DATA_SIZE = 40;
	static final byte START_BYTE = (byte)0x0D;
	
	static final byte OPTION_TYPE_TEXT = (byte)0b0000_0000;
	static final byte OPTION_TYPE_BIN  = (byte)0b1000_0000;
	
	static final int TEXT_TRANSMISSION = 0;
	static final int BINARY_TRANSMISSION = 1;

	
	private byte options;
	private int dataLength;
	private String destination;
	private byte[] data;
	
	public Message(byte options, int dataLength, String destination, byte[] data) {
		this.options = options;
		this.dataLength = dataLength;
		this.destination = destination;
		this.data = data;
	}

	public Message() {}
	
	public String getDestination() {
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

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public int getTransmissionType() {
		return (options & OPTION_TYPE_BIN) >> 7;
	}
}
	
	
class MessageFragment {
	private byte options;
	private int  dataLength;
	private int fragmentNo;
	private String destination;
	private byte[] data;
	
	public MessageFragment (byte options, int dataLength, int fragmentNum, String destination, byte[] data) {
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

	public String getDestination() {
		return destination;
	}

	public byte[] getData() {
		return data;
	}
}

class FragmentedMessage {
	static final String TAG = "FragmentedMessage";
	private SortedMap<Integer, MessageFragment> fragments = new TreeMap<Integer, MessageFragment>();
	
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