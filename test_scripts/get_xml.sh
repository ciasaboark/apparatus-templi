#Test sending a series of commands to a specific driver
if [ $# -lt 2 ]; then
	echo "Usage: get_xml.sh driver type <cycles>";
	echo "type must be one of 'widget' or 'full'";
	echo "<cycles> may be omitted to use the default value of 100";
	exit 1;
fi

TYPE=$2;
if [ "$TYPE" != "widget" ] && [ "$TYPE" != "full" ]; then
	echo "Unknown type: $TYPE";
	echo "Usage: get_xml.sh driver type <cycles>";
        echo "type must be one of 'widget' or 'full'";
        exit 1;
fi

CYCLES=$3;

if [ $# -lt 3 ]; then
	CYCLES=100;
fi


if [ "$TYPE" == "full" ]; then
	echo "Requesting full page xml for driver '$1' ($CYCLES cycles)";
else
	echo "Requesting widget xml for driver '$1' ($CYCLES cycles)";
fi

time for i in `seq 1 $CYCLES`; do
	wget -q http://192.168.0.102:8000/$TYPE.xml?driver=$1 -O - > /dev/null &&
	echo ".\c"
done
