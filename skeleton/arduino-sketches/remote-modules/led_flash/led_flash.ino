/**********************
* Example remote module sketch
* Waits for a command from the controller then flashes an LED
* attached to the specified pin (4 - 9)
*
* Accepted Commands:
*  (int) - single digit int value between 4 - 9 (inclusive)
*
* Responses:
*  "OK" - led was flashed
*  "FAIL" - command could not be parsed, or value was out of range
**********************/

//the name of the remote module should be unique
String module_name = "LED_FLASH";

//reserved names
String broadcast_tag = "ALL";
String debug_tag = "DEBUG";

//whether or not to send debugging messages over the network
const boolean DEBUG = true;

String terminator = "\n";
//char terminator = '0x0A';

void setup() {
	Serial.begin(9600);
	//set pins 4 - 9 to output mode
	for (int i = 4; i < 10; i++) {
		pinMode(i, OUTPUT);
	}
}

void loop() {
	String message;
	boolean lineTerminated = false;
	
	//read a line from serial input
	while (!lineTerminated) {
		if (Serial.available()) {
			//delay for a bit so the buffer will fill
			delay(3);
			if (Serial.available() > 0) {
				char c = Serial.read();
				if (c != '\n') {
					message += c;
				} else {
					lineTerminated = true;
				}
			}
		} else {
			//delay(1000);
		}
	}
		
		//is this message to us?
		if (message.startsWith(broadcast_tag + ':')) {
			//This message was broadcast to every remote module
			//+ For now just respond with the module's name
//                        debugMessage("Received broadcast message");
			sendMessage("READY");
		} else if (message.startsWith(module_name + ':')) {
			//This message was send specifically to this module.
			//This example module expects commands in the form
			//+ of a single digit int, indicating the pin number
			//+ of the LED to flash.
//			debugMessage("Got a new message: " + message);
			
			//strip the header from the message
			String command = message.substring(message.indexOf(':') + 1);
//			debugMessage("Command received: " + command);
			
			//convert the string to an int.  If the string does not represent
			//+ an integer value pin_num will be given a value of 0
			unsigned long int pin_num = command.toInt();
			
			if (pin_num == 0) {
				//the command could not be parsed
//				debugMessage("Error converting " + command + " to an integer value");
				sendMessage("FAIL");
			} else if (pin_num < 4 || pin_num > 9) {
				//the given value was out of range
//				debugMessage("Value of " + (String)pin_num + " is out of range");
				sendMessage("FAIL");
			} else {
//				debugMessage("Flashing LED num " + (String)pin_num);
				
				//flash the LED
				digitalWrite(pin_num, HIGH);
				delay(300);
				digitalWrite(pin_num, LOW);
				
				//respond to the controler to indicate that the LED was flashed
				sendMessage("OK");
			}
		} else {
//			debugMessage("Received a message for a different module: " + message);
		}
		
		//erase the current message
		message = "";
	 //delay(1000); 
}


//Send message over the network, prefixed with the module name
void sendMessage(String mess) {
 //TODO convert any bytes matching the terminator byte into an escaped double byte
  Serial.print(module_name + ":" + mess + terminator);
  Serial.flush();
}


//Send debugging output to the serial connection
//+ Output can be disabled by modifying the DEBUG
//+ const boolean in the header.
void debugMessage(String mess) {
  //TODO convert any bytes matching the terminator byte into an escaped double byte
  Serial.print("DEBUG" + (String)':' + module_name + ":" + mess + terminator);
  Serial.flush();
}

/*String[] splitCommands(String command, char delimeter) {
	//Break the command into seperate strings based on the
	//+ given delimeter
}

void processCommands(String[] commands) {
	//walk through the commands processing each
  
}*/
