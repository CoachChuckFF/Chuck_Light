#include "lib/udp_controller.h"

#define TAG "UDP Controller"

#define UDP_PORT 7153

const uint8_t ID[8] = {'J', 'E', 0x10, 'M', 'K', 0x03, 'C', 'K'};

struct udp_pcb *UDP;
ip_addr_t *IP;
ip_addr_t *DEST_IP = NULL;
uint16_t PORT;

packetHeader HEADER;
dataPacket DATA_PACKET;
pollReplyPacket POLL_REPLY_PACKET;

void init_udp_controller()
{
    IP = IP_ADDR_ANY;
    PORT = UDP_PORT;
    UDP = udp_new();

    ESP_LOGI(TAG, "Bind %d", udp_bind(UDP, IP, PORT));

    udp_recv(UDP, udp_recieve, NULL);

    //setup packets
    memcpy(HEADER._id, ID, 8);

    //may have to change
    DATA_PACKET._header = HEADER;
    DATA_PACKET._header._packet_type = DATA_PACKET_ID;
    POLL_REPLY_PACKET._header = HEADER;
    POLL_REPLY_PACKET._header._packet_type = POLL_REPLY_PACKET_ID;

}

void udp_recieve(void *arg,
                  struct udp_pcb *pcb,
                  struct pbuf *p,
                  const ip_addr_t *addr,
                  u16_t port)
{
  uint8_t i;

  for(i = 0; i < sizeof(ID); i++)
  {
    if(((uint8_t*)p->payload)[i] != ID[i])
    {
      ESP_LOGI(TAG, "Invalid Packet");
      goto FREE_P;
    }
  }

  switch(((packetHeader *)(p->payload))->_packet_type)
  {
    case COMMAND_PACKET_ID:

    set_mode(((commandPacket *)(p->payload))->_header._mode); //update mode
    //TODO add in other commands

    switch(((commandPacket *)(p->payload))->_command)
    {
      case 0:
        //don't do anything
      break;
      case HARD_RESET_COMMAND:
        esp_restart();
      break;
      case SOFT_RESET_COMMAND:
        esp_restart();
      break;
      default:
        ESP_LOGI(TAG, "Unknown command: %d",
                      ((commandPacket *)(p->payload))->_command);
      break;
    }


    break;
    case POLL_PACKET_ID:
      memcpy(DEST_IP, addr, sizeof(const ip_addr_t));
      //TODO add in batter level
      send_poll_reply_packet(get_mode(),
                              0.69,
                              0,
                              NULL);
    break;
  }


FREE_P:
  pbuf_free(p);
}

void send_data_packet(uint8_t data_type, uint8_t *data)
{

  struct pbuf *p;
  int ret_val;
  uint8_t i;

  if(DEST_IP == NULL)
  {
    ESP_LOGI(TAG, "No Address - Data");
    return;
  }

  p = pbuf_alloc(PBUF_TRANSPORT, sizeof(DATA_PACKET), PBUF_RAM);

  udp_connect(UDP, DEST_IP, PORT);

  DATA_PACKET._header._mode = get_mode();
/* //Dont actually need
  DATA_PACKET._user_action = 0;
  memset(DATA_PACKET._joystick, 0, sizeof(DATA_PACKET._joystick));
  memset(DATA_PACKET._gyro, 0, sizeof(DATA_PACKET._gyro));
*/

  DATA_PACKET._data_type = data_type;

  switch(data_type)
  {
    case USER_ACTION_DATA:
      DATA_PACKET._user_action = data[0];
    break;
    case JOYSTICK_DATA:
      DATA_PACKET._joystick[0] = data[0];
      DATA_PACKET._joystick[1] = data[1];
    break;
    case GYRO_DATA:
      //TODO this
      DATA_PACKET._gyro[0] = 0x69;
    break;
  }

  memcpy(p->payload, &DATA_PACKET, sizeof(DATA_PACKET));

  ret_val = udp_send(UDP, p);
  udp_disconnect(UDP);

  if(ret_val)
    ESP_LOGI(TAG, "Send Data Packet Error %d", ret_val);
}

void send_poll_reply_packet(uint8_t mode,
                            float battery_level,
                            uint8_t msg_len,
                            uint8_t *msg)
{
  if(DEST_IP == NULL)
  {
    ESP_LOGI(TAG, "No Address - Poll Reply");
    return;
  }


}
