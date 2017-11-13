/*main.c*/
#include <stdio.h>
#include <string.h>
#include <inttypes.h>
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "freertos/event_groups.h"
#include "nvs_flash.h"
#include "esp_log.h"

#include "lib/button_controllers.h"
#include "lib/connection_controller.h"
#include "lib/data_controller.h"
#include "lib/led_controller.h"
#include "lib/motion_controllers.h"
#include "lib/ps2_controller.h"
#include "lib/serial_controller.h"
#include "lib/timer_controller.h"
#include "lib/konami.h"
#include "lib/modes.h"

#define TAG "Main"

extern uint8_t DEBOUNCE_TICK;
uint8_t MODE = SELECTION_MODE;

uint8_t button_event = 0;
uint8_t direction_event = 0;
uint8_t motion_event = 0;

uint8_t buf[4];
uint8_t tick = 0;

void app_main()
{
    /* ------------- Init Functions --------------*/
    ESP_ERROR_CHECK( nvs_flash_init() );
    init_connection_controller();
    init_led_controller();
    init_ps2_controller();
    init_motion_controllers();
    init_button_controllers();
    init_timer_controller();

    //TODO UDP Listhener listens to modes

    /* ------------- Main Loop -------------------*/
    ESP_LOGI(TAG, "-... . . .--.");
    while(1)
    {
      //TODO add a serial terminal
      //printConnectionInfo();
      //TODO if(TIMER){ the button read
      if(DEBOUNCE_TICK)
      {

        switch(read_direction(false)) //mode dependant
        {
          case CENTER:
            //do nothing - no direction specified
            //ESP_LOGI(TAG, "NOTHING");
            direction_event = CENTER;
          break;
          case LEFT:
            ESP_LOGI(TAG, "LEFT");
            set_blue(HIGH);
            direction_event = LEFT;
          break;
          case RIGHT:
            ESP_LOGI(TAG, "RIGHT");
            set_red(HIGH);
            direction_event = RIGHT;
          break;
          case UP:
            ESP_LOGI(TAG, "UP");
            set_red(LOW);
            set_green(LOW);
            set_blue(LOW);
            direction_event = UP;
          break;
          case DOWN:
            ESP_LOGI(TAG, "DOWN");
            set_green(HIGH);
            direction_event = DOWN;
          break;
        }

        switch(read_button())
        {
          case 0:
            //do nothing - no button was pressed
            button_event = 0;
          break;
          case B1:
            ESP_LOGI(TAG, "B1 Pressed");
            button_event = B1;
          break;
          case B2:
            ESP_LOGI(TAG, "B2 Pressed");
            button_event = B2;
          break;
          case B12:
            ESP_LOGI(TAG, "B1 + B2 Pressed");
            button_event = B12;
          break;
          case PS2_B:
            ESP_LOGI(TAG, "PS2 Pressed");
            button_event = PS2_B;
          break;
        }


        /*if(!(tick++ % 8))
          print_xy();*/


        DEBOUNCE_TICK = 0;
        konami_tick();
      }

      switch(MODE)
      {
        case IDLE_MODE:

        break;
        case SELECTION_MODE:

          switch(check_konami(direction_event, button_event))
          {
            case 0:
              //do nothing
            break;
            case KONAMI_SEMICOMPLETE:
              goto SKIP_USER_INPUT;
            break;
            case KONAMI_COMPLETE:
              ESP_LOGI(TAG, "KONAMI!!! -> Enter Party Mode");
              goto SKIP_USER_INPUT;
            break;
            case REV_KONAMI_COMPLETE:
              ESP_LOGI(TAG, "REVERSE-KONAMI!!! -> Enter Scary Mode");
              goto SKIP_USER_INPUT;
            break;
          }

          //TODO send info if event
          //send_data_packet()

SKIP_USER_INPUT:


        break;
        case COLOR_WHEEL_MODE:

        break;
        case PRESET_MODE:

        break;
        case RECENT_MODE:

        break;
        case DMX_MODE:

        break;
        case PARTY_MODE:

        break;
        case SCARY_MODE:

        break;
      }

      direction_event = 0;
      button_event = 0;
    }
}

uint8_t get_mode()
{
  return MODE;
}

void set_mode(uint8_t mode)
{
  MODE = mode;
}
