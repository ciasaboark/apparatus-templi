DEBUG = -g
OPTIMIZE = -O
JC = javac $(DEBUB)
SRC_PATH = --sorucepath coordinator_eclipse_workspace/Coordinator/src/org/apparatus_templi
CLASS_PATH = -classptah '/home/christopher/git/apparatus-templi/coordinator_eclipse_workspace/Coordinator/bin'
COMPILE_DIRECTORY = $(CLASS_PATH) 

#Class Files#
ControllerModule:       $(SRC_PATH)/driver/ControllerModule.java
Driver:                 $(SRC_PATH)/driver/Driver.java
SensorModule:           $(SRC_PATH)/driver/SensorModule.java
Coordinator:            $(SRC_PATH)/Coordinator.java
DummySerialConnection:  $(SRC_PATH)/DummySerialConnection.java
EventGenerator:         $(SRC_PATH)/EventGenerator.java
Event:		        $(SRC_PATH)/Event.java
EventWatcher: 	        $(SRC_PATH)EventWatcher.java
Log: 		        $(SRC_PATH)/Log.java
MessageCenter: 		$(SRC_PATH)/MessageCenter.java
Preferences: 		$(SRC_PATH)/Preferences.java
SerialConnection:	$(SRC_PATH)/SerialConnection.java
ServerDriverTest: 	$(SRC_PATH)/ServerDriverTest.java
SimpleHttpServer: 	$(SRC_PATH)/SimpleHttpServer.java
UsbSerialConnection:	$(SRC_PATH)/UsbSerialConnection.java

all:
	$(JC) $(DEBUG) ControllerModule Driver SensorModule Coordinator DummySerialConnection EvventGenerator Event EventWatcher Log MessageCenter Preferences SerialConnection ServerDriverTest SimpleHttpServer UsbSerialConnection

clean:
	\rm -r '/home/christopher/git/apparatus-templi/coordinator_eclipse_workspace/Coordinator/bin' 
