/*button_controllers.c*/
#include "lib/button_controllers.h"

#define TAG "Button Controllers"

#define DEBOUNCE_COUNT 3

#define LONG_HOLD_COUNT 300

void init_button_controllers()
{
    gpio_config_t io_conf;

    //LED gpio setup
    if(!GPIO_IS_VALID_GPIO(PS2_PIN) ||
        !GPIO_IS_VALID_GPIO(B1_PIN) ||
        !GPIO_IS_VALID_GPIO(B2_PIN))
        ESP_LOGI(TAG, "Invalid GPIO");

    io_conf.intr_type = GPIO_PIN_INTR_DISABLE;
    io_conf.mode = GPIO_MODE_INPUT;
    io_conf.pin_bit_mask = GPIO_INPUT_PIN_SEL;
    io_conf.pull_down_en = 0;
    io_conf.pull_up_en = 1;
    gpio_config(&io_conf);

}

uint8_t read_button()
{
  uint8_t i;
  static int ps2_count = 0;
  static int b1_count = 0;
  static int b2_count = 0;
  static int b12_count = 0;
  static int ps2_long_count = 0;

  //B1 + B2 pressed
  if(!gpio_get_level(B1_PIN) && !gpio_get_level(B2_PIN))
  {
    if(b12_count == -1)
      return 0;

    if(++b12_count > DEBOUNCE_COUNT)
    {
      b12_count = -1;
      return B12;
    }
    else
      return 0;
  }
  else
  {
    b12_count = 0;
  }

  //B1 pressed
  if(!gpio_get_level(B1_PIN))
  {
    if(b1_count == -1)
      return 0;

    if(b1_count++ > DEBOUNCE_COUNT)
    {
      b1_count = -1;
      return B1;
    }
    else
      return 0;
  }
  else
  {
    b1_count = 0;
  }

  //B2 pressed
  if(!gpio_get_level(B2_PIN))
  {
    if(b2_count == -1)
      return 0;

    if(b2_count++ > DEBOUNCE_COUNT)
    {
      b2_count = -1;
      return B2;
    }
    else
      return 0;
  }
  else
  {
    b2_count = 0;
  }

  //PS2 pressed
  if(!gpio_get_level(PS2_PIN))
  {

    if(ps2_count == -1)
    {
      if(ps2_long_count == -2)
        return 0;

      if(ps2_long_count++ > LONG_HOLD_COUNT)
      {
        ps2_long_count = -2;
        return PS2_LONG;
      }
      return 0;
    }

    if(ps2_count++ > DEBOUNCE_COUNT)
    {
      ps2_count = -1;
      return PS2_B;
    }
    else
      return 0;
  }
  else
  {
    ps2_count = 0;
    ps2_long_count = 0;
  }

  return 0;
}
