#include "lib/konami.h"

#define TAG "Konami Code"

#define KONAMI_TIMEOUT 300000

uint8_t konami_state = 0;
uint8_t reverse_konami_state = 0;
uint32_t konami_tick_count = 0;

void clear_konami()
{
  konami_state = 0;
  reverse_konami_state = 0;
  konami_tick_count = 0;
}

void konami_tick()
{
  if(!konami_tick_count)
    return;

  if(konami_tick_count++ > KONAMI_TIMEOUT)
    clear_konami();
}

uint8_t check_konami(uint8_t direction, uint8_t button)
{
  // if no event return
  if(!direction && !button)
    return 0;

  if(direction == UP && !konami_state && !reverse_konami_state)
  {
    konami_state = 1;
    konami_tick_count = 1;
    return 0;
  }
  else if(direction == UP && konami_state == 1)
  {
    konami_state++;
    konami_tick_count = 1;
    return 0;
  }
  else if(direction == DOWN && konami_state == 2)
  {
    konami_state++;
    konami_tick_count = 1;
    return 0;
  }
  else if(direction == DOWN && konami_state == 3)
  {
    konami_state++;
    konami_tick_count = 1;
    return 0;
  }
  else if(direction == LEFT && konami_state == 4)
  {
    konami_state++;
    konami_tick_count = 1;
    return 0;
  }
  else if(direction == RIGHT && konami_state == 5)
  {
    konami_state++;
    konami_tick_count = 1;
    return 0;
  }
  else if(direction == LEFT && konami_state == 6)
  {
    konami_state++;
    konami_tick_count = 1;
    return 0;
  }
  else if(direction == RIGHT && konami_state == 7)
  {
    konami_state++;
    konami_tick_count = 1;
    return KONAMI_SEMICOMPLETE;
  }
  else if(button == B2 && konami_state == 8)
  {
    konami_state++;
    konami_tick_count = 1;
    return KONAMI_SEMICOMPLETE;
  }
  else if(button == B1 && konami_state == 9)
  {
    konami_state++;
    konami_tick_count = 1;
    return KONAMI_SEMICOMPLETE;
  }
  else if(button == PS2_B && konami_state == 10)
  {
    clear_konami();
    return KONAMI_COMPLETE;
  }

  //reverse Konami
  if(direction == DOWN && !konami_state && !reverse_konami_state)
  {
    reverse_konami_state = 1;
    konami_tick_count = 1;
    return 0;
  }
  else if(direction == DOWN && reverse_konami_state == 1)
  {
    reverse_konami_state++;
    konami_tick_count = 1;
    return 0;
  }
  else if(direction == UP && reverse_konami_state == 2)
  {
    reverse_konami_state++;
    konami_tick_count = 1;
    return 0;
  }
  else if(direction == UP && reverse_konami_state == 3)
  {
    reverse_konami_state++;
    konami_tick_count = 1;
    return 0;
  }
  else if(direction == RIGHT && reverse_konami_state == 4)
  {
    reverse_konami_state++;
    konami_tick_count = 1;
    return 0;
  }
  else if(direction == LEFT && reverse_konami_state == 5)
  {
    reverse_konami_state++;
    konami_tick_count = 1;
    return 0;
  }
  else if(direction == RIGHT && reverse_konami_state == 6)
  {
    reverse_konami_state++;
    konami_tick_count = 1;
    return 0;
  }
  else if(direction == LEFT && reverse_konami_state == 7)
  {
    reverse_konami_state++;
    konami_tick_count = 1;
    return KONAMI_SEMICOMPLETE;
  }
  else if(button == PS2_B && reverse_konami_state == 8)
  {
    reverse_konami_state++;
    konami_tick_count = 1;
    return KONAMI_SEMICOMPLETE;
  }
  else if(button == B1 && reverse_konami_state == 9)
  {
    reverse_konami_state++;
    konami_tick_count = 1;
    return KONAMI_SEMICOMPLETE;
  }
  else if(button == B2 && reverse_konami_state == 10)
  {
    clear_konami();
    return REV_KONAMI_COMPLETE;
  }


  clear_konami();
  return 0; //should never get here
}
