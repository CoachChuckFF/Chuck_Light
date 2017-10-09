/*
 * ws2812b.c
 * Signal recving program in the user space
 * Originated from http://people.ee.ethz.ch/~arkeller/linux/code/signal_user.c
 * Modified by daveti
 * Aug 23, 2014
 * root@davejingtian.org
 * http://davejingtian.org
 */
#include <signal.h>
#include <stdio.h>
#include <string.h>
#include <fcntl.h>
#include <sys/types.h>
#include <unistd.h>
#include <stdbool.h>
#include "../include/ece453.h"

#define SIG_TEST 44 /* we define our own signal, hard coded since SIGRTMIN is different in user and in kernel space */ 

#define PID "/sys/kernel/ece453/pid"
#define KEY0_BIT (1 << GPIO_IN_BUTTONS_BIT_NUM)
#define KEY1_BIT (1 << (GPIO_IN_BUTTONS_BIT_NUM + 1))

bool key0_press = false;
bool key0_reles = false;
bool key1_press = false;
bool key1_reles = false;
bool exit_press = false;

//*****************************************************************************
//*****************************************************************************
int set_pid(void)
{

  char buf[10];
  int fd = open(PID, O_WRONLY);
  if(fd < 0) {
    perror("open");
    return -1;
  }
  sprintf(buf, "%i", getpid());
  if (write(fd, buf, strlen(buf) + 1) < 0) {
    perror("fwrite"); 
    return -1;
  }
  close(fd);
  return 0;
}

//*****************************************************************************
//*****************************************************************************
int clear_pid(void)
{

  char buf[10];
  int fd = open(PID, O_WRONLY);
  if(fd < 0) {
    perror("open");
    return -1;
  }
	
  memset(buf,0,10);
  if (write(fd, buf, strlen(buf) + 1) < 0) {
    perror("fwrite"); 
    return -1;
  }
  close(fd);
  return 0;
}

// clear pid and set interupt mask to zero
void cleanup() {
  ece453_reg_write(WS2812_0_REG, 0x0);
  ece453_reg_write(WS2812_1_REG, 0x0);
  ece453_reg_write(WS2812_2_REG, 0x0);
  ece453_reg_write(WS2812_3_REG, 0x0);
  ece453_reg_write(WS2812_4_REG, 0x0);
  ece453_reg_write(WS2812_5_REG, 0x0);
  ece453_reg_write(WS2812_6_REG, 0x0);
  ece453_reg_write(WS2812_7_REG, 0x0);
  ece453_reg_write(CONTROL_REG, 0x1);
  
  clear_pid();
  ece453_reg_write(IM_REG, 0);
}

//*****************************************************************************
//*****************************************************************************
void receiveData(int n, siginfo_t *info, void *unused)
{
  if (info->si_int == KEY0_BIT) {
    key0_reles = key0_press;
    key0_press = true;
  }
  if (info->si_int == KEY1_BIT) {
    key1_reles = key1_press;
    key1_press = true;
  }
  if (!(key0_press || key1_press)) {
    cleanup();
    perror("unknown intterrupt\n\r");
  }
}

//*****************************************************************************
//*****************************************************************************
void control_c_handler(int n, siginfo_t *info, void *unused)
{
  cleanup();
  exit_press = true;
}


//*****************************************************************************
//*****************************************************************************
int main(int argc, char **argv)
{
  struct sigaction led_sig;
  struct sigaction ctrl_c_sig;

  struct timespec debounce_time;

  uint8_t intensity = 0xFF/2;
  int curr_color = 0;
  uint colors[] = {
    0x000000, // off
    0x000077, // blue
    0x007700, // green
    0x770000, // red
    0x003333, // cyan
    0x330033, // magenta
    0x333300, // yellow
    0x202020  // white
  };

  // Set up handler for information set from the kernel driver
  led_sig.sa_sigaction = receiveData;
  led_sig.sa_flags = SA_SIGINFO;
  sigaction(SIG_TEST, &led_sig, NULL);

  // Set up handler for when the user presses ctrl-c to stop the application
  ctrl_c_sig.sa_sigaction = control_c_handler;
  ctrl_c_sig.sa_flags = SA_SIGINFO;
  sigaction(SIGINT, &ctrl_c_sig, NULL);

  // Configure the IP module 
  clear_pid();
  set_pid();

  // enable reception of a signal when the user presses KEY[0] or KEY[1]
  ece453_reg_write(IM_REG, (KEY0_BIT | KEY1_BIT));

  printf("Press KEY0 or KEY1\n\r");
  
  // Loop until ctrl-c causes exit_press to be true
  while (!exit_press) {
    // write out current color to leds
    ece453_reg_write(WS2812_0_REG, colors[curr_color]);
    ece453_reg_write(WS2812_1_REG, colors[curr_color]);
    ece453_reg_write(WS2812_2_REG, colors[curr_color]);
    ece453_reg_write(WS2812_3_REG, colors[curr_color]);
    ece453_reg_write(WS2812_4_REG, colors[curr_color]);
    ece453_reg_write(WS2812_5_REG, colors[curr_color]);
    ece453_reg_write(WS2812_6_REG, colors[curr_color]);
    ece453_reg_write(WS2812_7_REG, colors[curr_color]);
    ece453_reg_write(CONTROL_REG, 0x1);

    // wait for keypress interrupt
    pause();

    if (key0_reles) {
      // key zero pressed, increment or wrap
      curr_color = (curr_color == 7) ? 0 : curr_color + 1;
      key0_press = false;
      key0_reles = false;
    } else if (key1_reles) {
      // key one pressed, decrement or wrap
      curr_color = (curr_color == 0) ? 7 : curr_color - 1;
      key1_press = false;
      key1_reles = false;
    }
  }

  cleanup();
  return 0;
}
