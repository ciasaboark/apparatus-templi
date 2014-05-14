@echo off
java -version:1.7 -version > nul 2>&1
if %ERRORLEVEL% == 0 goto found
	echo "Error: Apparatus Templi requires a Java JRE (version 7 or later)to run."
	echo "You can download a copy from http://java.com/download"
	pause
goto end

:found
echo Starting Apparatus Templi
if %PROCESSOR_ARCHITECTURE%==x86 (
	echo "Starting with 32 bit libraries"
  	start javaw -Djava.library.path=lib/RXTX/win-x86/ -jar apparatus-templi.jar %*
) else (
	echo "Starting with 64 bit libraries"
  	start javaw -Djava.library.path=lib/RXTX/win-x64/ -jar apparatus-templi.jar %*
)

:end