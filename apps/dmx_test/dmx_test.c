/*
 * Test application for updating dmx values.
 */

#include <stdio.h>

#include "../include/ece453.h"

#define DMX_ADDR "/sys/kernel/ece453/dmx_addr"
#define DMX_DATA "/sys/kernel/ece453/dmx_data"
#define DMX_SIZE "/sys/kernel/ece453/dmx_size"

int main(int argc, char **argv)
{
  if (argc < 3) {
    printf("usage: %s dmx_address dmx_value\n\r", argv[0]);
    return -1;
  }

  ece453_reg_write(DMX_ADDR, atoi(argv[1]));
  ece453_reg_write(DMX_DATA, atoi(argv[2]));
  ece453_reg_write(DMX_SIZE, 1);
  ece453_reg_write(CONTROL_REG, 1);
}

