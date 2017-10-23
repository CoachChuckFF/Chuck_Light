/*button_controllers.h*/
#ifndef BUTTON_CONTROLLERS_H
#define BUTTON_CONTROLLERS_H

#include <stdio.h>
#include <stdint.h>
#include <string.h>
#include <stdlib.h>

#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "freertos/queue.h"
#include "driver/gpio.h"
#include "esp_log.h"

#define PS2_PIN    32
#define B1_PIN     26
#define B2_PIN     27
#define GPIO_INPUT_PIN_SEL (GPIO_SEL_32 | GPIO_SEL_26 | GPIO_SEL_27)

#define PS2_B 0x01
#define B1 0x02
#define B2 0x03
#define B12 0x04

void init_button_controllers(void);

//read_button - if detected debounce
uint8_t read_button(void);

#endif
