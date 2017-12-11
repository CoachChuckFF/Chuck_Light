/*led_controller.c*/
#include "lib/led_controller.h"

#define TAG "LED Controller"

#define TIMER_UNIT 113

void init_led_controller()
{
    gpio_config_t io_conf;

    //LED gpio setup
    if(!GPIO_IS_VALID_GPIO(RED_PIN) ||
        !GPIO_IS_VALID_GPIO(GREEN_PIN) ||
        !GPIO_IS_VALID_GPIO(BLUE_PIN))
        ESP_LOGI(TAG, "Invalid GPIO");

    io_conf.intr_type = GPIO_PIN_INTR_DISABLE;
    io_conf.mode = GPIO_MODE_OUTPUT;
    io_conf.pin_bit_mask = GPIO_OUTPUT_PIN_SEL;
    io_conf.pull_down_en = 0;
    io_conf.pull_up_en = 0;
    gpio_config(&io_conf);

    gpio_set_level(RED_PIN, HIGH);
    gpio_set_level(GREEN_PIN, HIGH);
    gpio_set_level(BLUE_PIN, HIGH);

}

void pre_connect_animation()
{
  static uint8_t state = 0;

  switch (state) {
    case 0:
      if(dash())
        state++;
    break;
    case 1:
      if(sub_letter_break())
        state++;
    break;
    case 2:
      if(dot())
        state++;
    break;
    case 3:
      if(sub_letter_break())
        state++;
    break;
    case 4:
      if(dot())
        state++;
    break;
    case 5:
      if(sub_letter_break())
        state++;
    break;
    case 6:
      if(dot())
        state++;
    break;
    case 7:
      if(letter_break())
        state++;
    break;
    case 8:
      if(dot())
        state++;
    break;
    case 9:
      if(letter_break())
        state++;
    break;
    case 10:
      if(dot())
        state++;
    break;
    case 11:
      if(letter_break())
        state++;
    break;
    case 12:
      if(dot())
        state++;
    break;
    case 13:
      if(sub_letter_break())
        state++;
    break;
    case 14:
      if(dash())
        state++;
    break;
    case 15:
      if(sub_letter_break())
        state++;
    break;
    case 16:
      if(dash())
        state++;
    break;
    case 17:
      if(sub_letter_break())
        state++;
    break;
    case 18:
      if(dot())
        state++;
    break;
    case 19:
      if(word_break())
        state = 0;
    break;
  }

}

uint8_t dot()
{
  static uint16_t tick = 0;

  if(!tick++)
  {
    gpio_set_level(RED_PIN, HIGH);
    gpio_set_level(GREEN_PIN, HIGH);
    gpio_set_level(BLUE_PIN, HIGH);
  }
  else if(tick > TIMER_UNIT)
  {
    gpio_set_level(RED_PIN, LOW);
    gpio_set_level(GREEN_PIN, LOW);
    gpio_set_level(BLUE_PIN, LOW);
    tick = 0;
    return 1;
  }

  return 0;

}

uint8_t dash()
{
  static uint16_t tick = 0;

    if(!tick++)
    {
      gpio_set_level(RED_PIN, HIGH);
      gpio_set_level(GREEN_PIN, HIGH);
      gpio_set_level(BLUE_PIN, HIGH);
    }
    else if(tick > TIMER_UNIT * 3)
    {
      gpio_set_level(RED_PIN, LOW);
      gpio_set_level(GREEN_PIN, LOW);
      gpio_set_level(BLUE_PIN, LOW);
      tick = 0;
      return 1;
    }

    return 0;
}

uint8_t sub_letter_break()
{
  static uint16_t tick = 0;

  if(tick++ > TIMER_UNIT)
  {

    tick = 0;
    return 1;
  }

  return 0;
}

uint8_t letter_break()
{
  static uint16_t tick = 0;

  if(tick++ > TIMER_UNIT * 3)
  {

    tick = 0;
    return 1;
  }

  return 0;
}

uint8_t word_break()
{
  static uint16_t tick = 0;

  if(tick++ > TIMER_UNIT * 7)
  {

    tick = 0;
    return 1;
  }

  return 0;
}

void set_leds(uint8_t mode)
{
  switch(mode)
  {
    case CHASE_MODE:
      gpio_set_level(RED_PIN, LOW);
      gpio_set_level(GREEN_PIN, LOW);
      gpio_set_level(BLUE_PIN, HIGH);
    break;
    case IDLE_MODE:
      gpio_set_level(RED_PIN, LOW);
      gpio_set_level(GREEN_PIN, HIGH);
      gpio_set_level(BLUE_PIN, LOW);
    break;
    case LIGHT_SELECTION_MODE:
      gpio_set_level(RED_PIN, LOW);
      gpio_set_level(GREEN_PIN, HIGH);
      gpio_set_level(BLUE_PIN, HIGH);
    break;
    case CONTROL_SELECTION_MODE:
      gpio_set_level(RED_PIN, HIGH);
      gpio_set_level(GREEN_PIN, LOW);
      gpio_set_level(BLUE_PIN, LOW);
    break;
    case COLOR_WHEEL_MODE:
      gpio_set_level(RED_PIN, HIGH);
      gpio_set_level(GREEN_PIN, LOW);
      gpio_set_level(BLUE_PIN, HIGH);
    break;
    case DMX_MODE:
      gpio_set_level(RED_PIN, HIGH);
      gpio_set_level(GREEN_PIN, HIGH);
      gpio_set_level(BLUE_PIN, LOW);
    break;
    case PRESET_MODE:
      gpio_set_level(RED_PIN, HIGH);
      gpio_set_level(GREEN_PIN, HIGH);
      gpio_set_level(BLUE_PIN, HIGH);
    break;
    case PARTY_MODE:
      gpio_set_level(RED_PIN, LOW);
      gpio_set_level(GREEN_PIN, LOW);
      gpio_set_level(BLUE_PIN, LOW);
    break;
    case SCARY_MODE:
      gpio_set_level(RED_PIN, LOW);
      gpio_set_level(GREEN_PIN, LOW);
      gpio_set_level(BLUE_PIN, LOW);
    break;
  }
}

void set_red(uint8_t level)
{
  gpio_set_level(RED_PIN, level);
}

void set_green(uint8_t level)
{
  gpio_set_level(GREEN_PIN, level);
}

void set_blue(uint8_t level)
{
  gpio_set_level(BLUE_PIN, level);
}
