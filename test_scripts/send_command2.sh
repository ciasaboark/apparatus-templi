#Test sending a series of commands to a specific driver
echo "Test: Send Command"
echo "Sending a series of 100 requests to driver: LOCAL..."

time for i in `seq 1 100`; do \
	for j in `seq 0 2`; do
		wget -q http://192.168.0.102:8000/send_command?driver=LOCAL\&command=$j -O - > /dev/null &&
		echo ".\c"
	done
done
