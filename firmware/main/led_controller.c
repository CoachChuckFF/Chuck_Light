/*led_controller.c*/
#include "lib/led_controller.h"

#define TAG "LED Controller"

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
