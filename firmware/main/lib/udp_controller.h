/*udp_controller.h*/
#ifndef UDP_CONTROLLER
#define UDP_CONTROLLER

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <stdint.h>

#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "freertos/queue.h"
#include "freertos/event_groups.h"
#include "lwip/err.h"
#include "lwip/sockets.h"
#include "lwip/sys.h"
#include "lwip/dns.h"
#include "lwip/udp.h"
#include "lwip/ip4_addr.h"
#include "lwip/ip6_addr.h"
#include "esp_log.h"
#include "button_controllers.h"
#include "ps2_controller.h"
#include "modes.h"

#define DATA_PACKET_ID 0x33
#define COMMAND_PACKET_ID 0x36
#define POLL_PACKET_ID 0x87
#define POLL_REPLY_PACKET_ID 0x1E

#define HARD_RESET_COMMAND 0x99
#define SOFT_RESET_COMMAND 0x39

#define USER_ACTION_DATA 0x13
#define JOYSTICK_DATA 0x23
#define GYRO_DATA 0x33

//all incoming and outgoing packets will have the same preamble
//10 bytes = sizeof(char[10]) = 10 jvm may have diffrent sizing
typedef struct packetHeader {
  uint8_t _id[8];
  uint8_t _packet_type; //always Data_Packet
  uint8_t _mode; //current mode controller is/should be in
}__attribute__((packed)) packetHeader;

//Controller to Basestation
//This sends relevent controller information to the Basestation
//10 + 7 bytes = sizeof(char[17]) = 17 jvm may have diffrent sizing
typedef struct dataPacket {
  packetHeader _header;
  //depending on current mode one of the following
  //data points will be populated
  uint8_t _data_type; //user_action,joystick,gyro
  uint8_t _user_action; //up,down,left,right,b1,b2,b_ps2,konami,reverse_konami
  int _joystick[2]; //[0] x direction & [1] y direction
  int _gyro[3]; //TODO finalize gyroscope length
}__attribute__((packed)) dataPacket;

//Basestation to Controller
//This packet is usually sent in response of the dataPacklet
//10 + 1 bytes = sizeof(char[11]) = 11 jvm may have diffrent sizing
typedef struct commandPacket {
    packetHeader _header;
    //_header._mode will dictate what mode the controller is in
    uint8_t _command; //send command like reset/request info...
}__attribute__((packed)) commandPacket;

//Basestation to Controller
//Used as a heartbeat to make sure Controller is still connected
//10 bytes = sizeof(char[10]) = 10 jvm may have diffrent sizing
typedef struct pollPacket {
  packetHeader _header;
}__attribute__((packed)) pollPacket;

//Controller to Basestation
//Controller heartbeat/used for debugging
//10 + 4 + 2 + ? bytes = sizeof(char[16 + ?]) = 16 + ? jvm may have diffrent sizing - ? means is multi sized
typedef struct pollReplyPacket {
  packetHeader _header;
  float _battery_level;
  uint8_t _error_code;
  uint8_t _message_length;
  char *_message;
}__attribute__((packed)) pollReplyPacket;

void init_udp_controller(void);

void udp_recieve(void *arg,
                  struct udp_pcb *pcb,
                  struct pbuf *p,
                  const ip_addr_t *addr,
                  u16_t port);

void send_data_packet(uint8_t data_type, uint8_t user_data, int *other_data);
void send_poll_reply_packet(uint8_t mode,
                            float battery_level,
                            uint8_t msg_len,
                            uint8_t *msg);


#endif
