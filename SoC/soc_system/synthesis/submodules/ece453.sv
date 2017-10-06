/*
 *  Author:	Joe Krachey
 *  Date:	01/03/2017
 */

module ece453(
	// signals to connect to an Avalon clock source interface
	input			clk,
	input			reset,
	// signals to connect to an Avalon-MM slave interface
	input		[3:0]	slave_address,
	input				slave_read,
	input				slave_write,
	output wire	[31:0]	slave_readdata,
	input		[31:0]	slave_writedata,
	input		[3:0]	slave_byteenable,
	// ece453 in/outs
	input		[31:0]	gpio_inputs,
	output		[31:0]	gpio_outputs,
	output wire			irq_out
);

	// most of the set values will only be used by the component .tcl file.  The DATA_WIDTH and MODE_X = 3 influence the hardware created.
	// ENABLE_SYNC_SIGNALS isn't used by this hardware at all but it provided anyway so that it can be exposed in the component .tcl file
	// to control the stubbing of certain signals.
	//parameter ENABLE_SYNC_SIGNAL  S = 0;  // only used by the component .tcl file, 1 to expose user_chipselect/write/read, 0 to stub them

	`include "ece453.vh"

	//*******************************************************************
	// Register Set
	//*******************************************************************
	reg		[31:0]	dev_id_r;
	reg		[31:0]	control_r;
	reg		[31:0]	status_r;
	reg		[31:0]	im_r;
	reg		[31:0]	irq_r;
	reg		[31:0]	gpio_in_r;
	reg		[31:0]	gpio_out_r;
	reg		[31:0]	ws2812b_0_r;
	reg		[31:0]	ws2812b_1_r;
	reg		[31:0]	ws2812b_2_r;
	reg		[31:0]	ws2812b_3_r;
	reg		[31:0]	ws2812b_4_r;
	reg		[31:0]	ws2812b_5_r;
	reg		[31:0]	ws2812b_6_r;
	reg		[31:0]	ws2812b_7_r;


	//*******************************************************************
	// Wires/Reg
	//*******************************************************************
	wire	[31:0]	control_in;
	wire	[31:0]	status_in;
	wire	[31:0]	im_in;
	reg		[31:0]	irq_in;
	wire	[31:0]	gpio_in;
	wire	[31:0]	gpio_out;
	wire	[31:0]	ws2812b_0_in;
	wire	[31:0]	ws2812b_1_in;
	wire	[31:0]	ws2812b_2_in;
	wire	[31:0]	ws2812b_3_in;
	wire	[31:0]	ws2812b_4_in;
	wire	[31:0]	ws2812b_5_in;
	wire	[31:0]	ws2812b_6_in;
	wire	[31:0]	ws2812b_7_in;

	wire			ws2812b_busy;
	wire			neopixel_out;

	reg		[31:0]	gpio_in_irqs;


	//*******************************************************************
	// Register Read Assignments
	//*******************************************************************
	assign slave_readdata =	((slave_address == DEV_ID_ADDR)		&& slave_read)	? dev_id_r :
							((slave_address == CONTROL_ADDR )	&& slave_read)	? control_r :
							((slave_address == STATUS_ADDR )	&& slave_read)	? status_r :
							((slave_address == IM_ADDR )		&& slave_read)	? im_r :
							((slave_address == IRQ_ADDR )		&& slave_read)	? irq_r :
							((slave_address == GPIO_IN_ADDR )	&& slave_read)	? gpio_in_r :
							((slave_address == GPIO_OUT_ADDR )	&& slave_read)	? gpio_out_r :
							((slave_address == UNUSED_ADDR )	&& slave_read)	? 32'h00000000 :
							((slave_address == WS2818B_0_ADDR )	&& slave_read)	? ws2812b_0_r :
							((slave_address == WS2818B_1_ADDR )	&& slave_read)	? ws2812b_1_r :
							((slave_address == WS2818B_2_ADDR )	&& slave_read)	? ws2812b_2_r :
							((slave_address == WS2818B_3_ADDR )	&& slave_read)	? ws2812b_3_r :
							((slave_address == WS2818B_4_ADDR )	&& slave_read)	? ws2812b_4_r :
							((slave_address == WS2818B_5_ADDR )	&& slave_read)	? ws2812b_5_r :
							((slave_address == WS2818B_6_ADDR )	&& slave_read)	? ws2812b_6_r : ws2812b_7_r ;


	//*******************************************************************
	// Output Assignments
	//*******************************************************************

	// IRQ indicating that an interrupt is active
	assign irq_out = | (im_r & irq_r);
	assign gpio_outputs = {neopixel_out, gpio_out_r[30:0]};

	//*******************************************************************
	// Register Input Equations
	//*******************************************************************

	// Combinational Logic for register inputs.
	always @ (*) begin
		gpio_in_irqs = gpio_in_r ^ gpio_inputs;
		irq_in = irq_r | gpio_in_irqs;

		// WS2812B IRQ will get set to 1 only when ws2812b_busy changes from a 1 to a 0
		if (status_r[STATUS_WS2812B_BUSY_BIT_NUM] && !ws2812b_busy) begin
			irq_in = irq_in | IRQ_WS2812B_DONE_MASK;
		end

		irq_in = irq_in & im_r;

		// Determine the value of the IRQ register
		if(slave_address == IRQ_ADDR) begin
			if(slave_write) begin
				irq_in = irq_r & (~slave_writedata);
			end
		end
	end

	// Input signals for registers
	assign control_in	= ( (slave_address == CONTROL_ADDR )    && slave_write ) ? slave_writedata : (control_r & ~CONTROL_WS2812B_START_MASK);
	assign status_in	= (status_r & ~STATUS_WS2812B_BUSY_MASK) | (ws2812b_busy << STATUS_WS2812B_BUSY_BIT_NUM);
	assign im_in		= ( (slave_address == IM_ADDR )			&& slave_write ) ? slave_writedata : im_r;
	assign gpio_in		= gpio_inputs;
	assign gpio_out		= ( (slave_address == GPIO_OUT_ADDR)	&& slave_write ) ? slave_writedata : gpio_out_r;
	assign ws2812b_0_in	= ( (slave_address == WS2818B_0_ADDR)	&& slave_write ) ? slave_writedata : ws2812b_0_r;
	assign ws2812b_1_in	= ( (slave_address == WS2818B_1_ADDR)	&& slave_write ) ? slave_writedata : ws2812b_1_r;
	assign ws2812b_2_in	= ( (slave_address == WS2818B_2_ADDR)	&& slave_write ) ? slave_writedata : ws2812b_2_r;
	assign ws2812b_3_in	= ( (slave_address == WS2818B_3_ADDR)	&& slave_write ) ? slave_writedata : ws2812b_3_r;
	assign ws2812b_4_in	= ( (slave_address == WS2818B_4_ADDR)	&& slave_write ) ? slave_writedata : ws2812b_4_r;
	assign ws2812b_5_in	= ( (slave_address == WS2818B_5_ADDR)	&& slave_write ) ? slave_writedata : ws2812b_5_r;
	assign ws2812b_6_in	= ( (slave_address == WS2818B_6_ADDR)	&& slave_write ) ? slave_writedata : ws2812b_6_r;
	assign ws2812b_7_in	= ( (slave_address == WS2818B_7_ADDR)	&& slave_write ) ? slave_writedata : ws2812b_7_r;


	//*******************************************************************
	// Registers
	//*******************************************************************
	always @ (posedge clk or posedge reset) begin
		if (reset == 1) begin
			dev_id_r	<= 32'hECE45300;
			control_r	<= 32'h00000000;
			status_r	<= 32'h00000000;
			im_r		<= 32'h00000000;
			irq_r		<= 32'h00000000;
			gpio_in_r	<= 32'h00000000;
			gpio_out_r	<= 32'h00000000;
			ws2812b_0_r	<= 32'h00000000;
			ws2812b_1_r	<= 32'h00000000;
			ws2812b_2_r	<= 32'h00000000;
			ws2812b_3_r	<= 32'h00000000;
			ws2812b_4_r	<= 32'h00000000;
			ws2812b_5_r	<= 32'h00000000;
			ws2812b_6_r	<= 32'h00000000;
			ws2812b_7_r	<= 32'h00000000;
		end else begin
			dev_id_r	<= 32'hECE45300;
			control_r	<= control_in;
			status_r	<= status_in;
			im_r		<= im_in;
			irq_r		<= irq_in;
			gpio_in_r	<= gpio_in;
			gpio_out_r	<= gpio_out;
			ws2812b_0_r	<= ws2812b_0_in;
			ws2812b_1_r	<= ws2812b_1_in;
			ws2812b_2_r	<= ws2812b_2_in;
			ws2812b_3_r	<= ws2812b_3_in;
			ws2812b_4_r	<= ws2812b_4_in;
			ws2812b_5_r	<= ws2812b_5_in;
			ws2812b_6_r	<= ws2812b_6_in;
			ws2812b_7_r	<= ws2812b_7_in;
		end
	end


	//*******************************************************************
	// WS2812B Module
	//*******************************************************************
	neo_driver ece453_neopixels (
		control_r[CONTROL_WS2812B_START_BIT_NUM],
		clk,
		reset,
		ws2812b_0_r[23:0],
		ws2812b_1_r[23:0],
		ws2812b_2_r[23:0],
		ws2812b_3_r[23:0],
		ws2812b_4_r[23:0],
		ws2812b_5_r[23:0],
		ws2812b_6_r[23:0],
		ws2812b_7_r[23:0],
		neopixel_out,
		ws2812b_busy
	);
endmodule


///////////////////////////////////////////////////////////////////////////////////
// ECE453 FPGA NEOPIXEL DRIVER                                                  //
// Jared Pierce                                                                //
//                                                                            //
// Inputs:                                                                   //
// start_Neo - Initiates transmission of data to pixels, active high        //
// clk - 50Mhz clock signal                                                //
// reset - active high reset                                              //
// pixel1..8 - 24 bit color data for each pixel in RGB order.            //
//             Only required valid at start. Value gets flopped in.     //
//                                                                     //
// Outputs:                                                           //
// pixel_data_out - Signal that connects to the Neopixel datain line //
// busy - Signal that the driver is currently transmitting.         //
/////////////////////////////////////////////////////////////////////

module neo_driver
(
	input				start_Neo,
	input				clk,
	input				reset,
	input		[23:0]	pixel1,
	input		[23:0]	pixel2,
	input		[23:0]	pixel3,
	input		[23:0]	pixel4,
	input		[23:0]	pixel5,
	input		[23:0]	pixel6,
	input		[23:0]	pixel7,
	input		[23:0]	pixel8,
	output				pixel_data_out,
	output	reg			busy
);
	// timing parameters
	localparam LONG_TICKS	= 8'd43;	// 0.85us * 50MHz = 42.5 cycles
	localparam SHORT_TICKS	= 8'd20;	// 0.4us * 50MHz = 20 cycles
	localparam BIT_TIME		= 8'd63;	// 1.25us * 50MHz = 62.5 cycles
	localparam RESET_TICKS	= 8'd250;	// 50us   * 50MHz = 250  cycles

	// state registers
	typedef enum {IDLE, TRANSMIT, RESET_SIGNAL} state_t;
	state_t curr_state;
	state_t next_state;
	always_ff @(posedge clk or posedge reset) begin
		if(reset) begin
			// reset to idle state
			curr_state <= IDLE;
		end else begin
			// take next state from combinational logic
			curr_state <= next_state;
		end
	end

	// output reg for smooth signal
	reg				out_reg;
	reg				out_reg_next;
	assign pixel_data_out = out_reg;
	always_ff @(posedge clk or posedge reset) begin
		if (reset) begin
			// asynch reset
			out_reg <= 1'b0;
		end else begin
			// take next value from combinational
			out_reg <= out_reg_next;
		end
	end

	// shift register for output of all 24*8=192 bits
	reg		[191:0]	shift_reg;
	reg				shift_bit;
	reg				load_shift;
	always_ff @(posedge clk or posedge reset) begin
		if (reset) begin
			// reset to all zeros
			shift_reg <= 192'b0;
		end else if (load_shift) begin
			// load register with current pixel values
			shift_reg <= {pixel1, pixel2, pixel3, pixel4, pixel5, pixel6, pixel7, pixel8};
		end else if (shift_bit) begin
			// shift to next current output bit
			shift_reg <= shift_reg << 1;
		end else begin
			// keep value if no signals to change
			shift_reg <= shift_reg;
		end
	end

	// counter for the number of bits to send
	reg		[7:0]	bit_counter_reg;
	always_ff @(posedge clk or posedge reset) begin
		if (reset) begin
			// asynch reset to zero
			bit_counter_reg <= 8'b0;
		end else if (load_shift) begin
			// synch reset to zero (resets when shift register is loaded)
			bit_counter_reg <= 8'b0;
		end else if (shift_bit) begin
			// increment count (increments for every shift in shift register)
			bit_counter_reg <= bit_counter_reg + 1'b1;
		end else begin
			// no signals means keep value
			bit_counter_reg <= bit_counter_reg;
		end
	end

	// counter for timing the output signal
	reg		[7:0]	count_reg;
	reg				count_rst;
	reg				count_en;
	always_ff @(posedge clk or posedge reset) begin
		if (reset) begin
			// asynch reset to zero
			count_reg <= 8'b0;
		end else if (count_rst) begin
			// synch reset
			count_reg <= 8'b0;
		end else if (count_en) begin
			// count each clock
			count_reg <= count_reg + 1'b1;
		end else begin
			// disabled, keep value
			count_reg <= count_reg;
		end
	end

	// combinational logic for state and signals
	always_comb begin
		// internal signals
		next_state		= IDLE;
		shift_bit		= 1'b0;
		load_shift		= 1'b0;
		count_rst		= 1'b0;
		count_en		= 1'b0;
		// output signals
		out_reg_next	= 1'b0;
		busy			= 1'b0;

		// state logic
		case (curr_state)
			IDLE: begin
				// wait for beginning signal
				if (start_Neo) begin
					// move to reset state to begin sequence after capturing pixel values and resetting counter
					next_state	= RESET_SIGNAL;
					load_shift	= 1'b1;
					count_rst	= 1'b1;
				end
			end
			RESET_SIGNAL: begin
				// hold signal low for 50us (waiting RESET_TICKS # cycles) to signal new transmit
				count_en	= 1'b1;
				busy		= 1'b1;
				next_state	= RESET_SIGNAL;
				if (count_reg == RESET_TICKS) begin
					// after 50us of reset, begin transmitting data
					count_rst = 1'b1; // count reset takes prescedence over count enable
					next_state = TRANSMIT;
				end
			end
			TRANSMIT: begin
				// always counting in transmit stage
				count_en	= 1'b1;
				busy		= 1'b1;
				next_state	= TRANSMIT;
				out_reg_next= out_reg;

				if (count_reg < SHORT_TICKS) begin
					// always high for first 0.35us
					out_reg_next = 1'b1;
				end else if (count_reg < LONG_TICKS) begin
					// one or zero for middle section based on MSB of shift reg
					out_reg_next = shift_reg[191];
				end else if (count_reg < BIT_TIME) begin
					// always zero for final timing segment
					out_reg_next = 1'b0;
				end else if (bit_counter_reg < 191) begin
					// timing done for current bit (but not final bit); shift to next bit and reset timing
					shift_bit = 1'b1;
					count_rst = 1'b1;
				end else begin
					// timing done for final bit; reset counter and go to idle
					next_state	= IDLE;
					count_rst	= 1'b1;
				end
			end
			// no default because default signals defined at begining of always block
		endcase
	end
endmodule
