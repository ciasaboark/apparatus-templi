#include <SoftwareSerial.h>
SoftwareSerial xbee(10, 11);

void setup() {
  Serial.begin(115200);
  xbee.begin(9600);
  Serial.write("READY");
  Serial.write('\n');
}

void loop() {
  //pass incomming bytes back to the coordinator
  if (xbee.available()) {
    Serial.write(xbee.read());
  }
  
  //pass all network to the xbee for now
  //+ this will have to be modified later to listen
  //+ for commands to the local arduino
  if (Serial.available()) {
    xbee.write(Serial.read());
  }
}
