#Test sending a series of commands to a specific driver
if [ $# -lt 2 ]; then
	echo "Usage: send_command.sh Driver Command <cycles>";
	echo "<cycles> may be omitted to use the default 100 cycles";
	exit 1;
fi

CYCLES=$3;

if [ $# -lt 3 ]; then
	CYCLES=100;
fi


echo "Sending command '$2' to driver '$1' with '$CYCLES' cycles"

time for i in `seq 1 $CYCLES`; do
	wget -q http://192.168.0.102:8000/send_command?driver=$1\&command=$2 -O - > /dev/null &&
	echo ".\c"
done
