`ifndef _ece453_vh_
`define _ece453_vh_

	//*******************************************************************
	// Register Bit definitions
	//*******************************************************************
	localparam GPIO_OUT_LEDS_BIT_NUM		= 0;
	localparam GPIO_OUT_LEDS_MASK		= (32'h3FF << GPIO_OUT_LEDS_BIT_NUM);
	
	localparam GPIO_OUT_CAP_SEN_RST_BIT_NUM	= 27;
	localparam GPIO_OUT_CAP_SEN_RST_MASK	= (32'h1 << GPIO_OUT_CAP_SEN_RST_BIT_NUM);

	localparam GPIO_OUT_LCD_RST_BIT_NUM	= 28;
	localparam GPIO_OUT_LCD_RST_MASK	= (32'h1 << GPIO_OUT_LCD_RST_BIT_NUM);

	localparam GPIO_OUT_LCD_BCK_LITE_BIT_NUM	= 29;
	localparam GPIO_OUT_LCD_BCK_LITE_MASK	= (32'h1 << GPIO_OUT_LCD_BCK_LITE_BIT_NUM);
	
	localparam GPIO_OUT_LCD_CMD_BIT_NUM	= 30;
	localparam GPIO_OUT_LCD_CMD_MASK	= (32'h1 << GPIO_OUT_LCD_CMD_BIT_NUM);
	
	localparam GPIO_OUT_WS2812B_OUT_BIT_NUM	= 31;
	localparam GPIO_OUT_WS2812B_OUT_MASK	= (32'h1 << GPIO_OUT_WS2812B_OUT_BIT_NUM);
	



	localparam GPIO_IN_SWITCHES_BIT_NUM	= 0;
	localparam GPIO_IN_SWITCHES_MASK	= (32'h3FF << GPIO_IN_SWITCHES_BIT_NUM);

	localparam GPIO_IN_BUTTONS_BIT_NUM	= 10;
	localparam GPIO_IN_BUTTONS_MASK		= (32'hF << GPIO_IN_BUTTONS_BIT_NUM);

	localparam GPIO_IN_CAP_SEN_IRQ_BIT_NUM	= 14;
	localparam GPIO_IN_CAP_SEN_IRQ_MASK	= (32'h1 << GPIO_IN_CAP_SEN_IRQ_BIT_NUM);

	localparam GPIO_IN_LCD_IRQ_BIT_NUM	= 15;
	localparam GPIO_IN_LCD_IRQ_MASK		= (32'h1 << GPIO_IN_LCD_IRQ_BIT_NUM);



	localparam STATUS_WS2812B_BUSY_BIT_NUM	= 0;
	localparam STATUS_WS2812B_BUSY_MASK	= (32'h1 << STATUS_WS2812B_BUSY_BIT_NUM);


	localparam IRQ_WS2812B_DONE_BIT_NUM	= 0;
	localparam IRQ_WS2812B_DONE_MASK	= (32'h1 << IRQ_WS2812B_DONE_BIT_NUM);


	localparam CONTROL_WS2812B_START_BIT_NUM	= 0;
	localparam CONTROL_WS2812B_START_MASK	= (32'h1 << CONTROL_WS2812B_START_BIT_NUM);



	//*******************************************************************
	// Register Addresses
	//*******************************************************************
	localparam	DEV_ID_ADDR	= 4'b0000;
	localparam	CONTROL_ADDR	= 4'b0001;
	localparam  	STATUS_ADDR	= 4'b0010;
	localparam	IM_ADDR		= 4'b0011;
	localparam	IRQ_ADDR	= 4'b0100;
	localparam	GPIO_IN_ADDR	= 4'b0101;
	localparam	GPIO_OUT_ADDR	= 4'b0110;
	localparam	UNUSED_ADDR	= 4'b0111;
	localparam	WS2818B_0_ADDR	= 4'b1000;
	localparam	WS2818B_1_ADDR	= 4'b1001;
	localparam	WS2818B_2_ADDR	= 4'b1010;
	localparam	WS2818B_3_ADDR	= 4'b1011;
	localparam	WS2818B_4_ADDR	= 4'b1100;
	localparam	WS2818B_5_ADDR	= 4'b1101;
	localparam	WS2818B_6_ADDR	= 4'b1110;
	localparam	WS2818B_7_ADDR	= 4'b1111;

	localparam	ALL_BITS = 32'hFFFFFFFF;

`endif
