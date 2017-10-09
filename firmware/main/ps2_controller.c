/*ps2_controller.c*/
#include "lib/ps2_controller.h"

#define TAG "PS2 Controller"

#define DEBOUNCE_COUNT 3
#define ERROR_TOLERANCE 0.9

#define X_LEFT 0.0
#define Y_DOWN 0.0
#define X_RIGHT 4095.0
#define Y_UP 4095.0
#define X_CENTER 1900.0
#define Y_CENTER 7150.0

//TODO map color wheel
//TODO set movement flags
//TODO set gesture flags

void init_ps2_controller()
{
  //configure ADC - X,Y joystick
  adc1_config_width(ADC_WIDTH_12Bit);
  adc1_config_channel_atten(Y_ADC, ADC_ATTEN_11db);
  adc1_config_channel_atten(X_ADC, ADC_ATTEN_11db);
}

uint8_t read_direction(uint8_t continuous_read_enable)
{
  double x, y;
  static int left_count = 0;
  static int right_count = 0;
  static int up_count = 0;
  static int down_count = 0;
  uint8_t x_dir = 0;
  uint8_t y_dir = 0;

  x = (double) adc1_get_raw(X_ADC);
  y = (double) adc1_get_raw(Y_ADC);

  if(x < ((1 - ERROR_TOLERANCE) * X_CENTER))
  {
    x_dir = LEFT; //left
  }
  else if(x > (ERROR_TOLERANCE) * X_RIGHT)
  {
    x_dir = RIGHT; //right
  } //else x_dir stays at 0 or center

  if(y < ((1 - ERROR_TOLERANCE) * Y_CENTER))
  {
    y_dir = DOWN; //down
  }
  else if(y > (ERROR_TOLERANCE) * Y_UP)
  {
    y_dir = UP; //up
  } //else x_dir stays at 0 or center

  //CENTER
  if(!x_dir && !y_dir)
  {
    //reset debounce counts
    left_count = 0;
    right_count = 0;
    up_count = 0;
    down_count = 0;
    return CENTER;
  }
  //LEFT
  else if(x_dir == LEFT && y_dir == CENTER)
  {
    if(left_count == -1 && !continuous_read_enable)
      return CENTER;

    if(left_count == -1 && continuous_read_enable)
      left_count = 0;

    if(left_count++ > DEBOUNCE_COUNT)
    {
      left_count = -1;
      return LEFT;
    }

    return CENTER;
  }
  //RIGHT
  else if(x_dir == RIGHT && y_dir == CENTER)
  {
    if(right_count == -1 && !continuous_read_enable)
      return CENTER;

    if(right_count == -1 && continuous_read_enable)
      right_count = 0;

    if(right_count++ > DEBOUNCE_COUNT)
    {
      right_count = -1;
      return RIGHT;
    }

    return CENTER;
  }
  //UP
  else if(x_dir == CENTER && y_dir == UP)
  {
    if(up_count == -1 && !continuous_read_enable)
      return CENTER;

    if(up_count == -1 && continuous_read_enable)
      up_count = 0;

    if(up_count++ > DEBOUNCE_COUNT)
    {
      up_count = -1;
      return UP;
    }

    return CENTER;
  }
  //DOWN
  else if(x_dir == CENTER && y_dir == DOWN)
  {
    if(down_count == -1 && !continuous_read_enable)
      return CENTER;

    if(down_count == -1 && continuous_read_enable)
      down_count = 0;

    if(down_count++ > DEBOUNCE_COUNT)
    {
      down_count = -1;
      return DOWN;
    }

    return CENTER;
  }
  //DEFAULT
  else
  {
    left_count = 0;
    right_count = 0;
    up_count = 0;
    down_count = 0;
  }
return CENTER;
}

void read_xy(int *ret_val)
{

  ret_val[0] = adc1_get_raw(X_ADC);
  ret_val[1] = adc1_get_raw(Y_ADC);

}

void print_xy()
{
  ESP_LOGI(TAG, "(%d,%d)", adc1_get_raw(X_ADC)
                         , adc1_get_raw(Y_ADC));
}
