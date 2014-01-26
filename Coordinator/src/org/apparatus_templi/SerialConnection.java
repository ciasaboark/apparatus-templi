package org.apparatus_templi;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TooManyListenersException;


public class SerialConnection implements SerialPortEventListener {
	
	private final String TAG = "SerialConnection";

	
	public SerialConnection() {
		super();
	}

	
	/*
	 * BEGIN IMPORTED CODE
	 */
    private  SerialPort serialPort = null;

    //input and output streams for sending and receiving data
    private  InputStream input = null;
    private  OutputStream output = null;

    //the timeout value for connecting with the port
    final  int TIMEOUT = 2000;

    //some ascii values for for certain things
    final  int SPACE_ASCII = 32;
    final  int DASH_ASCII = 45;
    final  int NEW_LINE_ASCII = 10;

    //a string for recording what goes on in the program
    //this string is written to the GUI
     String logText = "";
	
  //search for all the serial ports
    //pre style="font-size: 11px;": none
    //post: adds all the found ports to a combo box on the GUI
//    public  void searchForPorts()
//    {
//        ports = CommPortIdentifier.getPortIdentifiers();
//
//        while (ports.hasMoreElements())
//        {
//            CommPortIdentifier curPort = (CommPortIdentifier)ports.nextElement();
//
//            //get only serial ports
//            if (curPort.getPortType() == CommPortIdentifier.PORT_SERIAL)
//            {
//                //window.cboxPorts.addItem(curPort.getName());
//                portMap.put(curPort.getName(), curPort);
//            }
//        }
//    }
    
    //connect to the selected port in the combo box
    //pre style="font-size: 11px;": ports are already found by using the searchForPorts
    //method
    //post: the connected comm port is stored in commPort, otherwise,
    //an exception is generated
    public boolean connect(String selectedPort)
    {
    	boolean connected = false;
    	try {
	    	CommPortIdentifier portId = CommPortIdentifier.getPortIdentifier(selectedPort);
	    	serialPort = (SerialPort) portId.open(TAG, 5000);
            Log.d(TAG, selectedPort + " opened successfully.");
            connected = true;
            
            //set the port parameters
            int baudRate = 57600; // 57600bps
            try {
            	// Set serial port to 57600bps-8N1..my favourite
            	serialPort.setSerialPortParams(
                baudRate,
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE);
            } catch (UnsupportedCommOperationException ex) {
            	System.err.println(ex.getMessage());
            }
            
            try {
            	serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
            	// OR
            	// If CTS/RTS is needed
            	//serialPort.setFlowControlMode(
            	//      SerialPort.FLOWCONTROL_RTSCTS_IN | 
            	//      SerialPort.FLOWCONTROL_RTSCTS_OUT);
            	} catch (UnsupportedCommOperationException ex) {
            		System.err.println(ex.getMessage());
            	}
            
        } catch (PortInUseException e) {
            Log.e(TAG, "port " + selectedPort + " is in use. (" + e.toString() + ")");
        } catch (Exception e) {
        	Log.e(TAG, "Failed to open " + selectedPort + "(" + e.toString() + ")");
        }
    	
    	return connected;
    }
    
   
    
    public  boolean initIOStream() {
        boolean successful = false;

        try {
            input = serialPort.getInputStream();
            output = serialPort.getOutputStream();
            writeData("TEST");

            successful = true;
            return successful;
        } catch (IOException e) {
            Log.e(TAG, "I/O Streams failed to open. (" + e.toString() + ")");
            return successful;
        }
    }
    
    
  //starts the event listener that knows whenever data is available to be read
    //pre style="font-size: 11px;": an open serial port
    //post: an event listener for the serial port that knows when data is received
    public void initListener()
    {
        try
        {
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
        }
        catch (TooManyListenersException e)
        {
            logText = "Too many listeners. (" + e.toString() + ")";
//            window.txtLog.setForeground(Color.red);
//            window.txtLog.append(logText + "\n");
        }
    }
    
    
  //disconnect the serial port
    //pre style="font-size: 11px;": an open serial port
    //post: closed serial port
    public void disconnect()
    {
        //close the serial port
        try
        {
            writeData("TEST");

            serialPort.removeEventListener();
            serialPort.close();
            input.close();
            output.close();
//            setConnected(false);
//            window.keybindingController.toggleControls();

            logText = "Disconnected.";
//            window.txtLog.setForeground(Color.red);
//            window.txtLog.append(logText + "\n");
        }
        catch (Exception e)
        {
            logText = "Failed to close " + serialPort.getName()
                              + "(" + e.toString() + ")";
//            window.txtLog.setForeground(Color.red);
//            window.txtLog.append(logText + "\n");
        }
    }
    
    
    //what happens when data is received
    //pre style="font-size: 11px;": serial event is triggered
    //post: processing on the data it reads
    public void serialEvent(SerialPortEvent evt) {
        if (evt.getEventType() == SerialPortEvent.DATA_AVAILABLE)
        {
        	Log.d(TAG, "serial data available for reading");
        	Coordinator.setIoReady(true);
            try
            {
                byte singleData = (byte)input.read();

                if (singleData != NEW_LINE_ASCII)
                {
                    logText = new String(new byte[] {singleData});
//                    window.txtLog.append(logText);
                }
                else
                {
//                    window.txtLog.append("\n");
                }
            }
            catch (Exception e)
            {
                Log.e(TAG, "Failed to read data. (" + e.toString() + ")");
//                window.txtLog.setForeground(Color.red);
//                window.txtLog.append(logText + "\n");
            }
        }
    }
    
    
    //method that can be called to send data
    //pre style="font-size: 11px;": open serial port
    //post: data sent to the other device
    public  void writeData(String data)
    {
        try
        {
        	Log.d(TAG, "writing message to output");
            output.write(data.getBytes());
            output.flush();
        }
        catch (Exception e)
        {
            logText = "Failed to write data. (" + e.toString() + ")";
//            window.txtLog.setForeground(Color.red);
//            window.txtLog.append(logText + "\n");
        }
    }
	
	/*
	 * END IMPORTED CODE
	 */
}
