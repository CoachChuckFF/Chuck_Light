#ifndef __LCD_H__
#define __LCD_H__
// Copyright (c) 2014, Joe Krachey
// All rights reserved.
//
// Redistribution and use in binary form, with or without modification, 
// are permitted provided that the following conditions are met:
//
// 1. Redistributions in binary form must reproduce the above copyright 
//    notice, this list of conditions and the following disclaimer in 
//    the documentation and/or other materials provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
// THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
// PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR 
// CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
// EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
// PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
// OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
// WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
// NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
// EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.


#include <stdint.h>
#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <getopt.h>
#include <fcntl.h>
#include <sys/ioctl.h>
#include <linux/types.h>
#include <linux/spi/spidev.h>

#include "ece453.h"
#include "fonts.h"

#define SPI_DEVICE		"/dev/spidev32766.0"

#define LCD_NUM_COL		132
#define LCD_NUM_PAGES		8

#define PIO_BASE_NUM  480
#define LCD_BACKLIGHT_BIT   10
#define LCD_CD_BIT          11	
#define LCD_RST_BIT         12	

#define LCD_BACKLIGHT	(PIO_BASE_NUM + LCD_BACKLIGHT_BIT)	
#define LCD_CD		(PIO_BASE_NUM + LCD_CD_BIT)	
#define LCD_RST		(PIO_BASE_NUM + LCD_RST_BIT)	

#define	INPUT		1
#define OUTPUT		0

//*****************************************************************************
// Enable LCD Backlight
//*****************************************************************************
void lcd_enable_backlight(void);

//*****************************************************************************
// Disable LCD Backlight
//*****************************************************************************
void lcd_disable_backlight(void);

//*****************************************************************************
// Set the page address to page 7-0.  The orientation of the 353 carrier card
// puts page 0 at the top of the display and page 7 at the bottom.
//
// See "Set Page Address" in the EADOGS102W-6 data sheet, pg 5.
//
// Make sure to set the command mode correctly!
//*****************************************************************************
void lcd_set_page(uint8_t   page);

//*****************************************************************************
// There are 102 columns in the display.  Use this function to set which colum
// data will be written to.
//
// See "Set Column Address LSB and MSB" in the EADOGS102W-6 data sheet, pg 5.
// This will require two different SPI transactions.
//
// Make sure to set the command mode correctly!
//*****************************************************************************
void lcd_set_column(uint8_t   column);

//*****************************************************************************
// When not in command mode, any data written to the LCD is used to determine
// which pixels are turned ON/OFF for the curretnly active page.  A 1 turns a 
// pixel on and a 0 turns the pixel off.
//*****************************************************************************
void lcd_write_data( uint8_t   data);
  
//*****************************************************************************
// Supports writing a 10 point character to the LCD.  There are "4" lines on 
// the LCD,
//*****************************************************************************
void lcd_write_char_10pts( uint8_t line, char c, uint8_t charIndex);

//*****************************************************************************
// Used to clear the LCD of all pixels
//*****************************************************************************
void lcd_clear(void);

//*****************************************************************************
// Initialize the LCD peripheral
//*****************************************************************************
void lcd_open(
  char        *device, 
  uint8_t     mode,
  uint8_t     bits,
  uint32_t    speed,
  uint16_t    delay
);

//*****************************************************************************
//*****************************************************************************
void lcd_close(void);

#endif

