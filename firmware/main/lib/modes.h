#ifndef MODES_H
#define MODES_H

#define IDLE_MODE 0
#define SELECTION_MODE 1
#define COLOR_WHEEL_MODE 2
#define PRESET_MODE 3
#define RECENT_MODE 4
#define DMX_MODE 5
#define PARTY_MODE 6 //use konami code. B1 + B2 to exit
#define SCARY_MODE 7 //use reverse konami code. morse code ...---... to start B1 + B2 to exit

extern uint8_t MODE;

#endif
