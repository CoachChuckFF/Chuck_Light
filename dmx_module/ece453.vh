`ifndef _ece453_vh_
`define _ece453_vh_

	//*******************************************************************
	// Register Bit definitions
	//*******************************************************************
	localparam GPIO_OUT_LEDS_BIT_NUM			= 0;
	localparam GPIO_OUT_LEDS_MASK				= (32'h3FF << GPIO_OUT_LEDS_BIT_NUM);
	localparam GPIO_OUT_CAP_SEN_RST_BIT_NUM		= 27;
	localparam GPIO_OUT_CAP_SEN_RST_MASK		= (32'h1 << GPIO_OUT_CAP_SEN_RST_BIT_NUM);
	localparam GPIO_OUT_LCD_RST_BIT_NUM			= 28;
	localparam GPIO_OUT_LCD_RST_MASK			= (32'h1 << GPIO_OUT_LCD_RST_BIT_NUM);
	localparam GPIO_OUT_LCD_BCK_LITE_BIT_NUM	= 29;
	localparam GPIO_OUT_LCD_BCK_LITE_MASK		= (32'h1 << GPIO_OUT_LCD_BCK_LITE_BIT_NUM);
	localparam GPIO_OUT_LCD_CMD_BIT_NUM			= 30;
	localparam GPIO_OUT_LCD_CMD_MASK			= (32'h1 << GPIO_OUT_LCD_CMD_BIT_NUM);
	localparam GPIO_IN_SWITCHES_BIT_NUM			= 0;
	localparam GPIO_IN_SWITCHES_MASK			= (32'h3FF << GPIO_IN_SWITCHES_BIT_NUM);
	localparam GPIO_IN_BUTTONS_BIT_NUM			= 10;
	localparam GPIO_IN_BUTTONS_MASK				= (32'hF << GPIO_IN_BUTTONS_BIT_NUM);
	localparam GPIO_IN_CAP_SEN_IRQ_BIT_NUM		= 14;
	localparam GPIO_IN_CAP_SEN_IRQ_MASK			= (32'h1 << GPIO_IN_CAP_SEN_IRQ_BIT_NUM);
	localparam GPIO_IN_LCD_IRQ_BIT_NUM			= 15;
	localparam GPIO_IN_LCD_IRQ_MASK				= (32'h1 << GPIO_IN_LCD_IRQ_BIT_NUM);

	/* DMX defines */
	localparam GPIO_OUT_DMX_OUTP_BIT_NUM		= 31;
	localparam GPIO_OUT_DMX_OUTP_MASK			= (32'h1 << GPIO_OUT_DMX_OUTP_BIT_NUM);
	localparam GPIO_OUT_DMX_OUTN_BIT_NUM		= 30;
	localparam GPIO_OUT_DMX_OUTN_MASK			= (32'h1 << GPIO_OUT_DMX_OUTN_BIT_NUM);
	localparam STATUS_DMX_BUSY_BIT_NUM			= 0;
	localparam STATUS_DMX_BUSY_MASK				= (32'h1 << STATUS_DMX_BUSY_BIT_NUM);
	localparam IRQ_DMX_DONE_BIT_NUM				= 0;
	localparam IRQ_DMX_DONE_MASK				= (32'h1 << IRQ_DMX_DONE_BIT_NUM);
	localparam CONTROL_DMX_START_BIT_NUM		= 0;
	localparam CONTROL_DMX_START_MASK			= (32'h1 << CONTROL_DMX_START_BIT_NUM);

	//*******************************************************************
	// Register Addresses
	//*******************************************************************
	localparam	DEV_ID_ADDR		= 4'b0000;
	localparam	CONTROL_ADDR	= 4'b0001;
	localparam	STATUS_ADDR		= 4'b0010;
	localparam	IM_ADDR			= 4'b0011;
	localparam	IRQ_ADDR		= 4'b0100;
	localparam	GPIO_IN_ADDR	= 4'b0101;
	localparam	GPIO_OUT_ADDR	= 4'b0110;
	localparam	UNUSED_ADDR0	= 4'b0111;
	localparam	DMX_ADDR_ADDR	= 4'b1000;	/* begin dmx reg addrs */
	localparam	DMX_DATA_ADDR	= 4'b1001;
	localparam	DMX_SIZE_ADDR	= 4'b1010;	/* end dmx reg addrs */
	localparam	UNUSED_ADDR1	= 4'b1011;
	localparam	UNUSED_ADDR2	= 4'b1100;
	localparam	UNUSED_ADDR3	= 4'b1101;
	localparam	UNUSED_ADDR4	= 4'b1110;
	localparam	UNUSED_ADDR5	= 4'b1111;

	localparam	ALL_BITS = 32'hFFFFFFFF;

`endif
