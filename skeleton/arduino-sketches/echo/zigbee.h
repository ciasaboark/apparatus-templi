#ifndef ZIGBEE_H
#define ZIGBEE_H

#include "types.h"

#define MAX_PAYLOAD_SIZE 84 
#define XBEE_PIN 12

void assemblePayload(message_t message);

#endif
