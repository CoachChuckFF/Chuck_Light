/*ps2_controller.h*/
#ifndef PS2_CONTROLLER_H
#define PS2_CONTROLLER_H

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <stdint.h>

#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "freertos/queue.h"
#include "freertos/event_groups.h"
#include "driver/adc.h"
#include "esp_log.h"

#define X_PIN 34
#define Y_PIN 35
#define Z_PIN 32 //for refrence only - PS2 button controlled in button_controllers

#define X_ADC ADC1_CHANNEL_7
#define Y_ADC ADC1_CHANNEL_6

#define CENTER  0
#define UP      1
#define DOWN    2
#define LEFT    3
#define RIGHT   4

void init_ps2_controller(void);

uint8_t read_direction(uint8_t continuous_read_enable);
void read_xy(int *ret_val);
void read_rgb(uint8_t *ret_val);
void print_xy(void);

#endif
