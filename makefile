DEBUG = -g
NODEBUG = -g:none
OPTIMIZE = -O
JC = javac $(DEBUB)

SRC_PATH = coordinator_eclipse_workspace/Coordinator/src/org/apparatus_templi
COMPILE_DIRECTORY = coordinator_eclipse_workspace/Coorinator/bin;

# Java allows you to compile source files by having their file names listed in a file
# serperated by a blank or a line break. That is what is being done here. 
list_files:
	ls -R $(SRC_PATH) > file_list.txt 

all:
	$(JC) $(DEBUG) -sourcepath $(SRC_PATH) -classpath $(COMPILE_DIRECTORY) @file_list.txt

clean:
	\rm -r '/home/christopher/git/apparatus-templi/coordinator_eclipse_workspace/Coordinator/bin' 
	\rm file_list.txt
