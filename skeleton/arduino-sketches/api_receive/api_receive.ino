int debugLED = 6;
int errLED = 5;
int activityLED = 7;

void setup() {
  pinMode(debugLED, OUTPUT);
  pinMode(errLED, OUTPUT);
  pinMode(activityLED, OUTPUT);
  pinMode(8, OUTPUT);
  
  //flash both leds to indicate setup finished
  flashLED(debugLED);
  flashLED(errLED);
  flashLED(activityLED);
  delay(500);
  flashLED(debugLED);
  flashLED(errLED);
  flashLED(activityLED);
  
}

void loop() {
  // make sure everything we need is in the buffer
  if (Serial.available() > 0) {
    // look for the start byte
    byte startByte = Serial.read();
    flashLED(activityLED);
    if (startByte == 0x7E) {
      flashLED(debugLED);
      boolean frameComplete = false;
      //read the two byte frame length
      //block until data is ready
      while (Serial.available() < 2) {}
      byte upper = Serial.read();
      flashLED(activityLED);
      byte lower = Serial.read();
      flashLED(activityLED);
      int frameLength = upper;
      frameLength << 8;
      frameLength += lower;
      
      byte frame [frameLength]; // = byte[frameLength];
      for (int i = 0; i < frameLength; i++) {
        while(Serial.available() == 0) {
          //wait for input to become available
        }
        frame[i] = Serial.read();
        flashLED(activityLED);
      }
      //frame reading is done, flash the debug led twice for success
      flashLED(debugLED);
      delay(500);
      flashLED(debugLED);
    } else {
      //we dont know what this byte is, flash the err led
      flashLED(errLED);
    }
  }
  
  flashLED(8);
  delay(1000);
}

void flashLED(int ledNum) {
  digitalWrite(ledNum, HIGH);
  delay(100);
  digitalWrite(ledNum, LOW);
}




