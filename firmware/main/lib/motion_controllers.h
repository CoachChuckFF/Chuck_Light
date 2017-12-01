/*motion_controllers.h*/
#ifndef MOTION_CONTROLLERS_H
#define MOTION_CONTROLLERS_H

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "esp_system.h"
#include "driver/spi_master.h"
#include "soc/gpio_struct.h"
#include "driver/gpio.h"
#include "esp_heap_caps.h"
#include "esp_log.h"

#define MISO_PIN 23
#define MOSI_PIN 25
#define CLK_PIN  19
#define CS_PIN   22 //could just tie high
#define DRDY_PIN 21

#define WRITE 0x00
#define READ 0x80

//registers
#define WHOAMI 0x0F
#define CTRL_REG8 0x22

//useful value macros
#define REBOOT_MOTION 0x05;

void init_motion_controllers(void);
void restart_motion_device(void);
void read_motion(uint8_t *ret_val);
void read_motion_reg(uint8_t reg);
void write_motion_reg(uint8_t reg, uint8_t val);

#endif
