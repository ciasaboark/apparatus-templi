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

/**
 * Singleton pattern that handles message fragmentation and de-fragmentation. Sends completed
 * fragments to the serial connection and reads incoming bytes from the serial line, reassembling
 * completed message fragments, and combines fragments into completed messages before placing them
 * in a queue for higher-level processing.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 */
public class MessageCenter implements Runnable {
	private static final String TAG = "MessageCenter";

	// holds all incoming messages
	private final LinkedBlockingDeque<Message> messageQueue = new LinkedBlockingDeque<Message>();

	// holds all incoming bytes
	private final LinkedBlockingQueue<Byte> incomingBytes = new LinkedBlockingQueue<Byte>();

	// group incoming message fragments by the destination byte[]
	private final HashMap<String, FragmentedMessage> fragmentedMessages = new HashMap<String, FragmentedMessage>();;

	private boolean readMessages = false;

	private static MessageCenter instance;
	private SerialConnection serialConnection;
	private boolean serialConnectionReady = false;

	private MessageCenter() {
		// Singleton pattern
	}

	/**
	 * Attempts to read a valid message from buffered bytes. If the first byte read is a valid start
	 * byte then this method will block until a complete message is read from the serial line. If
	 * the message read is a fragment with a fragment number greater than 0 then the fragment will
	 * be saved for later processing. If the fragment number is 0 and additional fragments have been
	 * saved then a re-assembled message will be generated before being placed into the Message
	 * queue. If the fragment number is 0 and no additional fragments have been saved, then the
	 * Message is immediately placed into the Mesage queue.
	 * 
	 * @throws InterruptedException
	 * @throws IOException
	 */
	private void readMessage() throws InterruptedException, IOException {
		// Read the start byte by itself so we can see if this
		// + is really the beginning of a message
		// TODO check for a correct start byte 0x0D
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

			int dataLength = dataLengthByte;
			// Log.d(TAG, "incoming message: data len: " + dataLength + " fragNum: " + fragmentNum);

			// wait until the payload data is available
			while (incomingBytes.size() < dataLength) {
				// Log.d(TAG, "waiting on " + dataLength + " data bytes, " + incomingBytes.size() +
				// " available");
				Thread.yield();
				Thread.sleep(30);
			}
			// read the payload data
			payloadData = new byte[dataLength];
			for (int i = 0; i < dataLength; i++) {
				payloadData[i] = incomingBytes.take();
			}

			if ((fragmentedMessages.containsKey(destination) || fragmentNum != 0)
					&& fragmentNum <= Message.MAX_FRAG_NUM) {
				storeMessageFragment(optionsByte, dataLengthByte, fragmentNum, destination,
						payloadData);
				if (fragmentNum == 0) {
					if (fragmentedMessages.get(destination).isMessageComplete()) {
						Log.d(TAG, "fragmented message to " + destination + " is complete");
						Message m = fragmentedMessages.get(destination).getCompleteMessage();
						fragmentedMessages.remove(destination);
						// Log.d(TAG, "adding incoming message addressed to: " + m.getDestination()
						// + " to the queue");
						messageQueue.put(m);
					} else {
						Log.d(TAG, "waiting on more fragments for message to: " + destination);
					}
				}
			} else {
				Message m = new Message(optionsByte, dataLength, destination, payloadData);
				// Log.d(TAG, "adding incoming message addressed to: " + m.getDestination() +
				// " to the queue");
				messageQueue.put(m);
			}

		} else {
			// Since this byte did not match the start byte we will discard it.
			// + This process will continue until a proper start byte is found.
			Log.w(TAG, "readMessage() read a malformed message, discarding this byte: '"
					+ new String(new byte[] { startByte }) + "'");
		}
	}

	/**
	 * Stores a message fragment for later processing. If the given message is complete then the
	 * combined message is placed into the Message queue.
	 * 
	 * @param optionsByte
	 *            the options byte of the message fragment
	 * @param dataLength
	 *            the length of the data block of the fragment
	 * @param fragmentNum
	 *            the fragment number of this message
	 * @param destination
	 *            the name of the Driver the completed message should be delivered to
	 * @param payloadData
	 *            the data block of the message fragment
	 * @throws IOException
	 */
	private void storeMessageFragment(byte optionsByte, int dataLength, int fragmentNum,
			String destination, byte[] payloadData) throws IOException {
		MessageFragment mf = new MessageFragment(optionsByte, dataLength, fragmentNum, destination,
				payloadData);
		FragmentedMessage fragments = fragmentedMessages.get(destination);
		if (fragments != null) {
			fragments.storeFragment(mf);
			fragmentedMessages.put(destination, fragments);
		} else {
			fragments = new FragmentedMessage();
			fragments.storeFragment(mf);

			// If this fragment completes the message then put it in the queue
			// + otherwise place it back in the map
			if (fragments.isMessageComplete()) {
				Message m = fragments.getCompleteMessage();
				messageQueue.add(m);
			} else {
				fragmentedMessages.put(destination, fragments);
			}
		}

	}

	/**
	 * Generates a Message with the given option byte already set. If the size of the Message is too
	 * large then it will be broken into as many message fragments a needed before transmission.
	 * 
	 * @param moduleName
	 *            the name of the module that this message should be sent to.
	 * @param data
	 *            the data block of the message.
	 * @param options
	 *            the option byte to use for this Message
	 * @return true if the message was sent, false otherwise.
	 */
	private boolean sendMessage(String moduleName, byte[] data, byte options) {
		assert moduleName != null : "module name can not be null";
		assert !moduleName.equals("") : "module name must be at least 1 ASCII character";
		assert data != null : "command byte array must not be null";

		boolean messageSent = false;
		// If the data block will fit within a single fragment, then send it
		if (data.length <= Message.MAX_DATA_SIZE) {
			messageSent = sendMessageFragment(moduleName, data, 0, options);
		} else {
			// break the data into multiple fragments of MAX_DATA_SIZE or less
			int curPos = 0;
			int fragmentNum = (data.length / Message.MAX_DATA_SIZE);
			while (fragmentNum >= 0) {
				int payloadSize = ((data.length - curPos) > Message.MAX_DATA_SIZE) ? Message.MAX_DATA_SIZE
						: data.length - curPos;
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

	/**
	 * Send a message fragment to the serial connection. This method assumes that the given data can
	 * generate a valid message fragment.
	 * 
	 * @param moduleName
	 *            the name of the module this message fragment should be sent to
	 * @param data
	 *            the data to place in the data block of this message fragment
	 * @param fragmentNumber
	 *            the fragment number to write to this message fragment
	 * @param optionsByte
	 *            the options byte to use for this message fragment
	 * @return true if the fragment was written to the serial connection, false otherwise.
	 */
	private boolean sendMessageFragment(String moduleName, byte[] data, int fragmentNumber,
			byte optionsByte) {
		assert moduleName != null : "module name can not be null";
		assert data != null : "command data can not be null (but may be empty)";

		// TODO add support for more options
		boolean sendMessage = true;
		boolean messageSent = false;
		if (data.length <= Message.MAX_DATA_SIZE && fragmentNumber >= 0 && serialConnectionReady) {
			byte[] fragmentNumberBytes = ByteBuffer.allocate(4).putInt(fragmentNumber).array();
			assert fragmentNumberBytes[0] == 0 && fragmentNumberBytes[1] == 0 : "fragment number is only two bytes, upper two bits should have been 0";
			byte[] fragmentNum = { fragmentNumberBytes[2], fragmentNumberBytes[3] };
			byte[] destinationBytes = new byte[10];

			try {
				destinationBytes = moduleName.getBytes("UTF-8");
			} catch (UnsupportedEncodingException e) {
				Log.d(TAG, "unable to translate destination '" + moduleName + "' into UTF-8 byte[]");
				sendMessage = false;
			}

			// build the outgoing message byte[]
			if (sendMessage) {
				messageSent = true;
				Log.d(TAG, "sendMessageFragment() sending '" + data.length + "' bytes to '"
						+ moduleName + "', fragment num: " + fragmentNumber + " of type "
						+ ((optionsByte == Message.OPTION_TYPE_TEXT) ? "text" : "bin"));
				byte[] message = new byte[15 + data.length];
				message[0] = Message.START_BYTE;
				message[1] = optionsByte;
				message[2] = (byte) data.length;
				message[3] = fragmentNum[0];
				message[4] = fragmentNum[1];
				// copy the destination bytes
				for (int i = 5, j = 0; j < destinationBytes.length; i++, j++) {
					message[i] = destinationBytes[j];
				}

				// copy the data bytes
				for (int i = 15, j = 0; j < data.length; i++, j++) {
					message[i] = data[j];
				}

				messageSent = serialConnection.writeData(message);
			}
		}

		try {
			Thread.sleep(20);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return messageSent;
	}

	/**
	 * Clears all messages and message fragments
	 */
	void flushMessages() {
		if (readMessages) {
			Log.e(TAG, "can not flush message queue while still reading from serial line");
		} else {
			messageQueue.clear();
			incomingBytes.clear();
			fragmentedMessages.clear();
		}
	}

	/**
	 * Returns a reference to the MessageCenter singleton
	 */
	static MessageCenter getInstance() {
		if (instance == null) {
			instance = new MessageCenter();
		}
		return instance;
	}

	/**
	 * Returns the message in the head of the Message queue.
	 * 
	 * @return the message in the head of the Message queue, or null if no Message is available.
	 */
	synchronized Message getMessage() {
		Message m = null;
		m = messageQueue.poll();
		return m;
	}

	/**
	 * Checks if the message queue is empty.
	 * 
	 * @return true if a Message is available, false otherwise.
	 */
	synchronized boolean isMessageAvailable() {
		return !messageQueue.isEmpty();
	}

	/**
	 * Returns the number of messages available in the Message queue.
	 * 
	 * @return the number of messages available in the Message queue.
	 */
	synchronized int queuedMessages() {
		return messageQueue.size();
	}

	/**
	 * Receives a byte from the serial connection, placing that byte into the byte buffer.
	 * 
	 * @param b
	 *            the byte to place in the buffer
	 * @throws InterruptedException
	 * @throws IOException
	 */
	void incomingSerial(byte b) throws InterruptedException, IOException {
		// Log.d(TAG, "incomingSerial(0x" + Integer.toString(b, 16) + ") char:" + new String(new
		// byte[] {b}));
		incomingBytes.put(b);
	}

	/**
	 * Generates a complete Message from the given command addressed to the specified module, then
	 * sends the Message to the serial connection. If the Message generated is too large for a
	 * single transmission then it will automatically be broken into as many fragments as needed
	 * before transmission.
	 * 
	 * @param moduleName
	 *            the name of the module that this message should be sent to.
	 * @param command
	 *            the text data to send to the module
	 * @return true if the message was sent, false otherwise.
	 */
	synchronized boolean sendCommand(String moduleName, String command) {
		boolean sendMessage = true;
		boolean messageSent = false;
		byte[] commandBytes = {};
		if (command == null || moduleName == null) {
			throw new IllegalArgumentException("module name and data must not be null");
		}

		try {
			commandBytes = command.getBytes("UTF-8");
			if (commandBytes.length > (Message.MAX_DATA_SIZE * Message.MAX_FRAG_NUM)) {
				throw new IllegalArgumentException(
						"Command is too large for a single message: length of "
								+ commandBytes.length);
			}
		} catch (UnsupportedEncodingException e) {
			sendMessage = false;
		}

		if (sendMessage) {
			messageSent = sendMessage(moduleName, commandBytes, Message.OPTION_TYPE_TEXT);
		}

		return messageSent;
	}

	/**
	 * Generates a complete Message from the given command addressed to the specified module, then
	 * sends the Message to the serial connection. If the Message generated is too large for a
	 * single transmission then it will automatically be broken into as many fragments as needed
	 * before transmission.
	 * 
	 * @param moduleName
	 *            the name of the module that this message should be sent to.
	 * @param data
	 *            the binary data to send to the module
	 * @return true if the message was sent, false otherwise.
	 */
	synchronized boolean sendBinary(String moduleName, byte[] data) {
		if (data == null || moduleName == null) {
			throw new IllegalArgumentException("module name and data must not be null");
		}
		if (data.length > (Message.MAX_DATA_SIZE * Message.MAX_FRAG_NUM)) {
			throw new IllegalArgumentException("Data is too large for a single message: length of "
					+ data.length);
		}
		return sendMessage(moduleName, data, Message.OPTION_TYPE_BIN);
	}

	/**
	 * Notify the MessageCenter that it should begin checking for messages on its serial connection.
	 */
	void beginReadingMessages() {
		readMessages = true;
	}

	/**
	 * Notify the MessageCenter that it should stop reading messages on its serial connection.
	 */
	void stopReadingMessges() {
		readMessages = false;
	}

	/**
	 * Read bytes from the serial connection a byte matching termByte is found. This method may
	 * block indefinitely.
	 * 
	 * @param termByte
	 *            the byte to watch for.
	 * @return an array of bytes read up to (but not including) the term byte
	 * @throws InterruptedException
	 */
	byte[] readBytesUntil(byte termByte) throws InterruptedException {
		byte[] bytes = {};
		if (readMessages) {
			Log.w(TAG,
					"asked to read raw bytes while automatic processing is ongoing, returning empty byte[]");
		}

		ByteArrayOutputStream inBytes = new ByteArrayOutputStream();
		byte b = incomingBytes.take();
		@SuppressWarnings("unused")
		int discardedBytes = 1;
		while (b != termByte) {
			inBytes.write(b);
			b = incomingBytes.take();
			discardedBytes++;
		}
		bytes = inBytes.toByteArray();
		// Log.d(TAG, "discarded " + discardedBytes + " bytes");
		return bytes;
	}

	/**
	 * Returns true if the serial connection is ready, false otherwise.
	 * 
	 * @return true if the serial connection is ready, false otherwise.
	 */
	boolean isSerialConnected() {
		return serialConnectionReady;
	}

	/**
	 * Sets the serial connection of MessageCenter to the given serialConnection.
	 * 
	 * @param newSerialConnection
	 */
	void setSerialConnection(SerialConnection newSerialConnection) {
		if (this.serialConnection != null) {
			this.serialConnection.close();
		}
		this.stopReadingMessges();
		this.serialConnectionReady = false;
		this.serialConnection = newSerialConnection;
		if (serialConnection != null && serialConnection.isConnected()) {
			this.serialConnectionReady = true;
		}

	}

	/**
	 * Run in a constant loop checking for messages in the byte buffer. If the byte buffer has at
	 * least 15 bytes and the first byte is a valid start byte then execution will block until a
	 * valid message is read.
	 */
	@Override
	public void run() {
		while (true) {
			if (readMessages) {
				try {
					if (incomingBytes.size() >= 15) {
						// enough bytes have been read to form a complete message,
						// + begin processing
						readMessage();
					} else {
						// Log.d(TAG, "waiting on more serial input");
						Thread.sleep(200);
						Thread.yield();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}

	/**
	 * Every Message is composed of one or more fragments. Each fragment contains a fragment number
	 * to specify its ordering relative to all other fragments. Fragment numbers are ordered in
	 * decreasing order, with a fragment number of 0 representing the last fragment of a Message.
	 * 
	 * @author Jonathan Nelson <ciasaboark@gmail.com>
	 * 
	 */
	private class MessageFragment {
		private final byte options;
		private final int dataLength;
		private final int fragmentNo;
		private final String destination;
		private final byte[] data;

		public MessageFragment(byte options, int dataLength, int fragmentNum, String destination,
				byte[] data) {
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

	/**
	 * Wraps a number of {@link MessageFragment}s into a complete Message.
	 * 
	 * @author Jonathan Nelson <ciasaboark@gmail.com>
	 * 
	 */
	private class FragmentedMessage {
		static final String TAG = "FragmentedMessage";
		private final SortedMap<Integer, MessageFragment> fragments = new TreeMap<Integer, MessageFragment>();

		/**
		 * Stores the given MessageFragment.
		 * 
		 * @param fragment
		 */
		public void storeFragment(MessageFragment fragment) {
			int fragmentNum = fragment.getFragmentNo();
			if (fragments.containsKey(fragmentNum)) {
				// oops somehow we got two fragments
				// Log.e(TAG, "incoming message fragment '" + fragmentNum +
				// "' will overwrite an existing fragment");
			}
			fragments.put(fragmentNum, fragment);
		}

		/**
		 * Combines the stored MessageFragments into a single Message.
		 * 
		 * @return the Message generated from the stored message fragments.
		 * @throws IOException
		 *             if the data blocks can not be combined.
		 */
		Message getCompleteMessage() throws IOException {
			Message m = new Message();
			if (!isMessageComplete()) {
				Log.w(TAG, "attempting generating a complete message without all fragments");
			}
			ByteArrayOutputStream dataBytes = new ByteArrayOutputStream();
			MessageFragment firstFragment = fragments.get(fragments.firstKey());
			// TODO check all fragments have the same option byte?
			m.setOptions(firstFragment.getOptions());
			m.setDestination(firstFragment.getDestination());
			m.setDataLength(firstFragment.getDataLength());
			for (Integer fragNum : fragments.keySet()) {
				dataBytes.write(fragments.get(fragNum).getData());
			}
			m.setData(dataBytes.toByteArray());

			return m;
		}

		/**
		 * Checks that all fragments from fragment number 0 to fragment number n have been received.
		 * 
		 * @return true if there are no gaps in the fragments and fragment 0 has been received,
		 *         false otherwise.
		 */
		public boolean isMessageComplete() {
			boolean isComplete = true;
			int i = 0;
			for (Integer fragNum : fragments.keySet()) {
				if (i != fragNum) {
					isComplete = false;
					break;
				}
				i++;
			}
			return isComplete;
		}

	}
}

/**
 * A complete application layer protocol message. This message can be generated from a single
 * message fragment (if only a single message fragment with a fragment number of 0 was received), or
 * can be generated from multiple fragments by combining their data blocks sequential from fragment
 * 0 to fragment n.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * 
 */
class Message {
	static final int MAX_DATA_SIZE = 69;
	static final int MAX_FRAG_NUM = 65535;
	static final byte START_BYTE = (byte) 0x0D;

	static final byte OPTION_TYPE_TEXT = (byte) 0b0000_0000;
	static final byte OPTION_TYPE_BIN = (byte) 0b1000_0000;

	static final int TEXT_TRANSMISSION = 0;
	static final int BINARY_TRANSMISSION = 1;

	private byte options;
	private int dataLength;
	private String destination;
	private byte[] data;

	/**
	 * Build a complete Message from the given data.
	 * 
	 * @param options
	 *            the option byte.
	 * @param dataLength
	 *            the length of the data block
	 * @param destination
	 *            the destination driver that this message should be passed to.
	 * @param data
	 *            the data block of the message.
	 */
	public Message(byte options, int dataLength, String destination, byte[] data) {
		this.options = options;
		this.dataLength = dataLength;
		this.destination = destination;
		this.data = data;
	}

	/**
	 * Build an empty Message
	 */
	public Message() {
	}

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

	/**
	 * Returns an integer representation of the transmission type. Valid types are text(0) and
	 * binary(1). If the transmission type is text, then the data block should be interpreted as
	 * ASCII (or UTF-8) text. If the transmission type is 11 then the data block is binary data of
	 * an indeterminate type.
	 * 
	 */
	public int getTransmissionType() {
		return (options & OPTION_TYPE_BIN) >> 7;
	}
};
