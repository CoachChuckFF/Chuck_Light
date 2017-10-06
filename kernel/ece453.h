/*
 * Linux 2.6 and 3.0 'parrot' sample device driver
 *
 * Copyright (c) 2011, Pete Batard <pete@akeo.ie>
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

#define DRIVER_NAME "ece453_driver"

#define DEVICE_NAME "socfpga"
#define CLASS_NAME "ece453"


#define AUTHOR "ECE453 Staff"
#define DESCRIPTION "Example sysfs driver ECE453 DE1-SoC Mezzanine Card"
#define VERSION "0.1"

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
#define ECE453_UNUSED_OFFSET    7 
#define ECE453_WS2812B_0_OFFSET 8 
#define ECE453_WS2812B_1_OFFSET 9 
#define ECE453_WS2812B_2_OFFSET 10 
#define ECE453_WS2812B_3_OFFSET 11 
#define ECE453_WS2812B_4_OFFSET 12 
#define ECE453_WS2812B_5_OFFSET 13 
#define ECE453_WS2812B_6_OFFSET 14 
#define ECE453_WS2812B_7_OFFSET 15 

//*******************************************************************
// Register Bit definitions
//*******************************************************************
#define GPIO_OUT_LEDS_BIT_NUM               0
#define GPIO_OUT_LEDS_MASK                  (0x3FF << GPIO_OUT_LEDS_BIT_NUM)
#define GPIO_OUT_CAP_SEN_RST_BIT_NUM        27
#define GPIO_OUT_CAP_SEN_RST_MASK           (0x1 << GPIO_OUT_CAP_SEN_RST_BIT_NUM)
#define GPIO_OUT_LCD_RST_BIT_NUM            28
#define GPIO_OUT_LCD_RST_MASK               (0x1 << GPIO_OUT_LCD_RST_BIT_NUM)
#define GPIO_OUT_LCD_BCK_LITE_BIT_NUM       29
#define GPIO_OUT_LCD_BCK_LITE_MASK          (0x1 << GPIO_OUT_LCD_BCK_LITE_BIT_NUM)
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

#define STATUS_WS2812B_BUSY_BIT_NUM         0
#define STATUS_WS2812B_BUSY_MASK            (0x1 << STATUS_WS2812B_BUSY_BIT_NUM)

#define IRQ_WS2812B_DONE_BIT_NUM            0
#define IRQ_WS2812B_DONE_MASK               (0x1 << IRQ_WS2812B_DONE_BIT_NUM)

#define CONTROL_WS2812B_START_BIT_NUM       0
#define CONTROL_WS2812B_START_MASK          (0x1 << CONTROL_WS2812B_START_BIT_NUM)





/* We'll use our own macros for printk */
#define dbg(format, arg...) do { if (debug) pr_info(CLASS_NAME ": %s: " format , __FUNCTION__ , ## arg); } while (0)
#define err(format, arg...) pr_err(CLASS_NAME ": " format, ## arg)
#define info(format, arg...) pr_info(CLASS_NAME ": " format, ## arg)
#define warn(format, arg...) pr_warn(CLASS_NAME ": " format, ## arg)
