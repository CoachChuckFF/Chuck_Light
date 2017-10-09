#ifndef KONAMI_H
#define KONAMI_H

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <stdint.h>

#include "button_controllers.h"
#include "ps2_controller.h"
#include "modes.h"

void clear_konami(void);
uint8_t check_konami(uint8_t, direction, uint8_t button);
void konami_tick(void);

#ifndef
