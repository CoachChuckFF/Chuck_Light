#ifndef MODES_H
#define MODES_H

#define CHASE_MODE 0
#define IDLE_MODE 1
#define LIGHT_SELECTION_MODE 2
#define CONTROL_SELECTION_MODE 3
#define COLOR_WHEEL_MODE 4
#define DMX_MODE 5
#define PRESET_MODE 6
#define PARTY_MODE 7 //led off//use konami code. B1 + B2 to exit
#define SCARY_MODE 8 //led off//use reverse konami code. morse code ...---... to start B1 + B2 to exit



extern uint8_t MODE;

uint8_t get_mode(void);
void set_mode(uint8_t mode);

#endif
