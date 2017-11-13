#ifndef KONAMI_H
#define KONAMI_H

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <stdint.h>

#include "button_controllers.h"
#include "ps2_controller.h"
#include "modes.h"
#include "esp_log.h"

#define KONAMI_COMPLETE 1
#define REV_KONAMI_COMPLETE 2
#define KONAMI_SEMICOMPLETE 3

void clear_konami(void);
void konami_tick(void);
uint8_t check_konami(uint8_t direction, uint8_t button);

#endif
