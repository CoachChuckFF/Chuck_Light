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

#define TAG "Main"

extern uint8_t DEBOUNCE_TICK;

uint8_t buf[4];
int tick = 0, tick2 = 0;

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
        switch(read_button())
        {
          case 0:
            //do nothing - no button was pressed
            //ESP_LOGI(TAG, "NOTHING");
          break;
          case B1:
            ESP_LOGI(TAG, "B1 Pressed");
            set_red(HIGH);
          break;
          case B2:
            ESP_LOGI(TAG, "B2 Pressed");
            set_green(HIGH);
          break;
          case B1 + B2:
            ESP_LOGI(TAG, "B1 + B2 Pressed");
            set_red(LOW);
            set_green(LOW);
            set_blue(LOW);
          break;
          case PS2_B:
            ESP_LOGI(TAG, "PS2 Pressed");
            set_blue(HIGH);
          break;
        }

        DEBOUNCE_TICK = 0;

        if(tick++ > 10)
        {
          //restart_motion_device();
          read_motion_reg(0x05);
          read_motion_reg(0x0F);
          write_motion_reg(0x05, 0x33);
          if(tick2++ > 0x07)
          {
            restart_motion_device();
            tick2 = 0;
          }

          tick = 0;
        }
      }

    }
}
