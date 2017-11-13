/*
 */

#ifndef __ECE453_APP_H__
#define __ECE453_APP_H__

#include <string.h>
#include <stdint.h>
#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <getopt.h>
#include <fcntl.h>
#include <sys/ioctl.h>
#include <linux/types.h>

#define CONTROL_REG	"/sys/kernel/ece453/control"
#define DEVICE_ID_REG	"/sys/kernel/ece453/device_id"
#define GPIO_IN_REG	"/sys/kernel/ece453/gpio_in"
#define GPIO_OUT_REG	"/sys/kernel/ece453/gpio_out"
#define IM_REG		"/sys/kernel/ece453/interrupt_mask"
#define IRQ_REG		"/sys/kernel/ece453/irq"
#define PID_REG		"/sys/kernel/ece453/pid"
#define STATUS_REG	"/sys/kernel/ece453/status"

//*******************************************************************
// Register Addresses
//*******************************************************************
#define ECE453_DEV_ID_OFFSET    0 
#define ECE453_CONTROL_OFFSET   1 
#define ECE453_STATUS_OFFSET    2 
#define ECE453_IM_OFFSET        3 
#define ECE453_IRQ_OFFSET       4 
#define ECE453_GPIO_IN_OFFSET   5 
#define ECE453_GPIO_OUT_OFFSET  6 
#define ECE453_DMX_ADDR_OFFSET  8 
#define ECE453_DMX_DATA_OFFSET  9 
#define ECE453_DMX_SIZE_OFFSET  10 

//*******************************************************************
// Register Bit definitions
//*******************************************************************
#define GPIO_OUT_LEDS_BIT_NUM               0
#define GPIO_OUT_LEDS_MASK                  (0x3FF << GPIO_OUT_LEDS_BIT_NUM)
#define GPIO_OUT_CAP_SEN_RST_BIT_NUM        27
#define GPIO_OUT_CAP_SEN_RST_MASK           (0x1 << GPIO_OUT_CAP_SEN_RST_BIT_NUM)
#define GPIO_OUT_LCD_RST_BIT_NUM            28
#define GPIO_OUT_LCD_RST_MASK               (0x1 << GPIO_OUT_LCD_RST_BIT_NUM)
#define GPIO_OUT_LCD_CMD_BIT_NUM            30
#define GPIO_OUT_LCD_CMD_MASK               (0x1 << GPIO_OUT_LCD_CMD_BIT_NUM)
#define GPIO_OUT_WS2812B_OUT_BIT_NUM        31
#define GPIO_OUT_WS2812B_OUT_MASK           (0x1 << GPIO_OUT_WS2812B_OUT_BIT_NUM)

#define GPIO_IN_SWITCHES_BIT_NUM            0
#define GPIO_IN_SWITCHES_MASK               (0x3FF << GPIO_IN_SWITCHES_BIT_NUM)
#define GPIO_IN_BUTTONS_BIT_NUM             10
#define GPIO_IN_BUTTONS_MASK                (0xF << GPIO_IN_BUTTONS_BIT_NUM)
#define GPIO_IN_CAP_SEN_IRQ_BIT_NUM         14
#define GPIO_IN_CAP_SEN_IRQ_MASK            (0x1 << GPIO_IN_CAP_SEN_IRQ_BIT_NUM)
#define GPIO_IN_LCD_IRQ_BIT_NUM             15
#define GPIO_IN_LCD_IRQ_MASK                (0x1 << GPIO_IN_LCD_IRQ_BIT_NUM)

/* DMX Defines */
#define GPIO_OUT_DMX_OUTP_BIT_NUM           31
#define GPIO_OUT_DMX_OUTP_MASK              (0x1 << GPIO_OUT_DMX_OUTP_BIT_NUM)
#define GPIO_OUT_DMX_OUTN_BIT_NUM           29
#define GPIO_OUT_DMX_OUTN_MASK              (0x1 << GPIO_OUT_DMX_OUTN_BIT_NUM)

#define STATUS_DMX_BUSY_BIT_NUM             0
#define STATUS_DMX_BUSY_MASK                (0x1 << STATUS_DMX_BUSY_BIT_NUM)
#define IRQ_DMX_DONE_BIT_NUM                0
#define IRQ_DMX_DONE_MASK                   (0x1 << IRQ_DMX_DONE_BIT_NUM)
#define CONTROL_DMX_START_BIT_NUM           0
#define CONTROL_DMX_START_MASK              (0x1 << CONTROL_DMX_START_BIT_NUM)

/**
 * Print error message s and exit.
 **/
void pabort(const char *s);

//*****************************************************************************
//*****************************************************************************
int ece453_reg_read(char *reg_name);

//*****************************************************************************
//*****************************************************************************
int ece453_reg_write(char *reg_name, int value);

#endif
