/*
 *  Author:	Joe Eichenhofer (adapted from source by Joe Krachey)
 *  Date:	2017-10-23
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
	reg		[31:0]	dmx_addr_r;
	reg		[31:0]	dmx_data_r;
	reg		[31:0]	dmx_size_r;


	//*******************************************************************
	// Wires/Reg
	//*******************************************************************
	wire	[31:0]	control_in;
	wire	[31:0]	status_in;
	wire	[31:0]	im_in;
	reg		[31:0]	irq_in;
	wire	[31:0]	gpio_in;
	wire	[31:0]	gpio_out;
	reg		[31:0]	dmx_addr_in;
	reg		[31:0]	dmx_data_in;
	reg		[31:0]	dmx_size_in;

	reg				dmx_busy;
	wire			dmx_out;

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
							((slave_address == DMX_ADDR_ADDR )	&& slave_read)	? dmx_addr_r :
							((slave_address == DMX_DATA_ADDR )	&& slave_read)	? dmx_data_r :
							((slave_address == DMX_SIZE_ADDR )	&& slave_read)	? dmx_size_r :
							32'h00000000 ;


	//*******************************************************************
	// Output Assignments
	//*******************************************************************

	// IRQ indicating that an interrupt is active
	assign irq_out = | (im_r & irq_r);
	assign gpio_outputs = {dmx_out, ~dmx_out, gpio_out_r[29:0]};

	//*******************************************************************
	// Register Input Equations
	//*******************************************************************

	// Combinational Logic for register inputs.
	always_comb begin
		gpio_in_irqs = gpio_in_r ^ gpio_inputs;
		irq_in = irq_r | gpio_in_irqs;

		// DMX IRQ will get set to 1 only when dmx_busy changes from a 1 to a 0
		if (status_r[STATUS_DMX_BUSY_BIT_NUM] && !dmx_busy) begin
			irq_in = irq_in | IRQ_DMX_DONE_MASK;
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
	assign control_in	= ( (slave_address == CONTROL_ADDR )    && slave_write ) ? slave_writedata : (control_r & ~CONTROL_DMX_START_MASK);
	assign status_in	= (status_r & ~STATUS_DMX_BUSY_MASK) | (dmx_busy << STATUS_DMX_BUSY_BIT_NUM);
	assign im_in		= ( (slave_address == IM_ADDR )			&& slave_write ) ? slave_writedata : im_r;
	assign gpio_in		= gpio_inputs;
	assign gpio_out		= ( (slave_address == GPIO_OUT_ADDR)	&& slave_write ) ? slave_writedata : gpio_out_r;
	assign dmx_addr_in	= ( (slave_address == DMX_ADDR_ADDR)	&& slave_write ) ? slave_writedata : dmx_addr_r;
	assign dmx_data_in	= ( (slave_address == DMX_DATA_ADDR)	&& slave_write ) ? slave_writedata : dmx_data_r;
	assign dmx_size_in	= ( (slave_address == DMX_SIZE_ADDR)	&& slave_write ) ? slave_writedata : dmx_size_r;

	//*******************************************************************
	// Registers
	//*******************************************************************
	always_ff @ (posedge clk or posedge reset) begin
		if (reset) begin
			dev_id_r	<= 32'hECE45300;
			control_r	<= 32'h00000000;
			status_r	<= 32'h00000000;
			im_r		<= 32'h00000000;
			irq_r		<= 32'h00000000;
			gpio_in_r	<= 32'h00000000;
			gpio_out_r	<= 32'h00000000;
			dmx_addr_r	<= 32'h00000000;
			dmx_data_r	<= 32'h00000000;
			dmx_size_r	<= 32'h00000000;
		end else begin
			dev_id_r	<= dev_id_r;
			control_r	<= control_in;
			status_r	<= status_in;
			im_r		<= im_in;
			irq_r		<= irq_in;
			gpio_in_r	<= gpio_in;
			gpio_out_r	<= gpio_out;
			dmx_addr_r	<= dmx_addr_in;
			dmx_data_r	<= dmx_data_in;
			dmx_size_r	<= dmx_size_in;
		end
	end

	/* DMX Module */
	reg			dmx_write;
	dmx512 dmx_mod(
			.clk(clk),
			.rst(reset),
			.write_addr(dmx_addr_r[9:0]),
			.write_data0(dmx_data_r[7:0]),
			.write_data1(dmx_data_r[15:8]),
			.write_data2(dmx_data_r[23:16]),
			.write_data3(dmx_data_r[31:24]),
			.write_size((dmx_size_r > 32'h4) ? 3'h4 : dmx_size_r[2:0]),
			.write_en(dmx_write),
			.dmx_signal(dmx_out)
		);

	/* state register for buffer */
	typedef enum {IDLE, TRANSMIT} state_t;
	state_t curr_state;
	state_t next_state;
	always_ff @(posedge clk or posedge reset) begin
		if (reset) begin
			curr_state <= IDLE;
		end else begin
			curr_state <= next_state;
		end
	end

	always_comb begin
		dmx_busy = 1'b0;
		dmx_write = 1'b0;
		next_state = IDLE;
		
		case (curr_state)
			IDLE: begin
				if (control_r[CONTROL_DMX_START_BIT_NUM]) begin
					dmx_busy = 1'b1;
					dmx_write = 1'b1;
					next_state = TRANSMIT;
				end
			end
			TRANSMIT: begin
				dmx_busy = 1'b1;
				dmx_write = 1'b1;
			end
			
		endcase
	end
endmodule
