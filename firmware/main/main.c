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
#include "lib/udp_controller.h"
#include "lib/konami.h"
#include "lib/modes.h"

#define TAG "Main"

#define XY_REFRESH_RATE 30
#define PARTY_REFRESH_RATE 10

extern uint8_t DEBOUNCE_TICK;
uint8_t MODE = IDLE_MODE;

uint8_t button_event = 0;
uint8_t direction_event = 0;
uint8_t motion_event = 0;

int joystick_data[2];
int gyro_data[3];

uint8_t buf[4];
uint8_t tick = 0;
uint8_t party_tick = 0;
uint8_t xy_tick = 0;
uint8_t party_sub_mode = 0;

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
    init_udp_controller();

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
            direction_event = LEFT;
          break;
          case RIGHT:
            ESP_LOGI(TAG, "RIGHT");
            direction_event = RIGHT;
          break;
          case UP:
            ESP_LOGI(TAG, "UP");
            direction_event = UP;
          break;
          case DOWN:
            ESP_LOGI(TAG, "DOWN");
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
	          read_motion_reg(0x0F);
          break;
          case PS2_LONG:
            ESP_LOGI(TAG, "PS2 Long Hold");
            button_event = PS2_LONG;
          break;
        }

        DEBOUNCE_TICK = 0;
        konami_tick();
        party_tick++;
        xy_tick++;
      }

      switch(MODE)
      {
        case CHASE_MODE:

        break;
        case IDLE_MODE:

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
              direction_event = KONAMI_COMPLETE;
            break;
            case REV_KONAMI_COMPLETE:
              ESP_LOGI(TAG, "REVERSE-KONAMI!!! -> Enter Scary Mode");
              direction_event = REV_KONAMI_COMPLETE;
            break;
          }
        break;
        case LIGHT_SELECTION_MODE:


        break;
        case CONTROL_SELECTION_MODE:


        break;
        case COLOR_WHEEL_MODE:
        if(xy_tick > XY_REFRESH_RATE)
        {
          read_xy(joystick_data);
          //print_xy();
          send_data_packet(JOYSTICK_DATA, 0, joystick_data);
          xy_tick = 0;
        }

        break;
        case DMX_MODE:


        break;
        case PRESET_MODE:


        break;
        case PARTY_MODE:
          //send gyro data
          if(party_tick > PARTY_REFRESH_RATE)
          {
            switch(party_sub_mode)
            {
              case 1:
                set_red(HIGH);
                set_green(LOW);
                set_blue(LOW);
              break;
              case 2:
                set_red(LOW);
                set_green(HIGH);
                set_blue(LOW);
              break;
              case 3:
                set_red(LOW);
                set_green(LOW);
                set_blue(HIGH);
              break;
              case 4:
                set_red(LOW);
                set_green(LOW);
                set_blue(LOW);
                party_sub_mode = 0;
              break;
              default:
                party_sub_mode = 1;
            }
            party_sub_mode++;
            party_tick = 0;
          }

        break;
        case SCARY_MODE:
          //send gyro data

        break;
      }

      if(direction_event || button_event)
      {
        send_data_packet(USER_ACTION_DATA, (direction_event) ? direction_event : button_event, NULL);
      }

SKIP_USER_INPUT:

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
  set_leds(mode);
}
