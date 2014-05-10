/*
 * Copyright (C) 2014  Christopher Hagler
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 
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
