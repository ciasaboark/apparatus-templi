DEBUG = -g
NODEBUG = -g:none
OPTIMIZE = -O
JC = javac $(DEBUB)

SRC_PATH = coordinator_eclipse_workspace/Coordinator/src/org/apparatus_templi
COMPILE_DIRECTORY = coordinator_eclipse_workspace/Coordinator/bin
APACHE_JAR = coordinator_eclipse_workspace/Coordinator/lib/commons-cli-1.2.jar
RXTX = coordinator_eclipse_workspace/Coordinator/lib/RXTX/linux-x86_64/RXTXcomm.jar

CLASS_PATH = -cp $(APACHE_JAR):$(RXTX)


all:	
	mkdir -p $(COMPILE_DIRECTORY)
	$(JC) $(DEBUG) -d $(COMPILE_DIRECTORY) $(CLASS_PATH) $(SRC_PATH)/*.java $(SRC_PATH)/driver/*.java

clean:
	\rm -r 'coordinator_eclipse_workspace/Coordinator/bin' 