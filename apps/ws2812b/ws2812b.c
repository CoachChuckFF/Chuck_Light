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
#define RED   0xFF0000
#define GREEN 0x00FF00
#define KEY0  (1 << GPIO_IN_BUTTONS_BIT_NUM)

bool busy = true;

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


//*****************************************************************************
//*****************************************************************************
void receiveData(int n, siginfo_t *info, void *unused)
{
  if( info->si_int == KEY0)
  {
	  printf("KEY0 Pressed\n\r");
    busy = false;
  }
  else
  {
    printf("Unknow interrupt\n\r");
  }
}

//*****************************************************************************
//*****************************************************************************
void control_c_handler(int n, siginfo_t *info, void *unused)
{
  clear_pid();
  ece453_reg_write(IM_REG, 0);
  busy = false;
}


//*****************************************************************************
//*****************************************************************************
int main(int argc, char **argv)
{
	struct sigaction led_sig;
	struct sigaction ctrl_c_sig;

  // Set up handler for information set from the kernel driver
	led_sig.sa_sigaction = receiveData;
	led_sig.sa_flags = SA_SIGINFO;
	sigaction(SIG_TEST, &led_sig, NULL);

  // Set up handler for when the user presses CNTL-C to stop the application
	ctrl_c_sig.sa_sigaction = control_c_handler;
	ctrl_c_sig.sa_flags = SA_SIGINFO;
	sigaction(SIGINT, &ctrl_c_sig, NULL);

  // Configure the IP module 
  set_pid();

  // Set the WS2812B Interrupts to GREEN 
  ece453_reg_write(WS2812_0_REG, GREEN);
  ece453_reg_write(WS2812_1_REG, GREEN);
  ece453_reg_write(WS2812_2_REG, GREEN);
  ece453_reg_write(WS2812_3_REG, GREEN);
  ece453_reg_write(WS2812_4_REG, GREEN);
  ece453_reg_write(WS2812_5_REG, GREEN);
  ece453_reg_write(WS2812_6_REG, GREEN);
  ece453_reg_write(WS2812_7_REG, GREEN);
  ece453_reg_write(CONTROL_REG, 0x1);

  // enable reception of a signal when the user presses KEY[0]
  ece453_reg_write(IM_REG, KEY0);
      
  printf("Press KEY0\n\r");

  /* Loop forever, waiting for interrupts */
	while (busy) {
		sleep(86400);	/* This will end early when we get an interrupt. */
	}

  clear_pid();

  // Disalbe interrupts
  ece453_reg_write(IM_REG, 0);

  // Set the WS2812B Interrupts to red
  ece453_reg_write(WS2812_0_REG, RED);
  ece453_reg_write(WS2812_1_REG, RED);
  ece453_reg_write(WS2812_2_REG, RED);
  ece453_reg_write(WS2812_3_REG, RED);
  ece453_reg_write(WS2812_4_REG, RED);
  ece453_reg_write(WS2812_5_REG, RED);
  ece453_reg_write(WS2812_6_REG, RED);
  ece453_reg_write(WS2812_7_REG, RED);
  ece453_reg_write(CONTROL_REG, 0x1);
	return 0;
}

