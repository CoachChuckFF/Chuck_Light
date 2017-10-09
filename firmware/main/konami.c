#include "lib/konami.h"

#define KONAMI_TIMEOUT 300

uint8_t konami_state = 0;
uint8_t reverse_konami_state = 0;
uint8_t konami_tick = 0;

void clear_konami()
{
  konami_state = 0;
  reverse_konami_state = 0;
  konami_tick = 0;
}

void konami_tick()
{
  if(konami_tick++ < KONAMI_TIMEOUT)
    clear_konami();
}

uint8_t check_konami(uint8_t, direction, uint8_t button)
{

}
