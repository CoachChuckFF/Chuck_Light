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

module dmx512(
	input			clk,		// Clock
	input			rst,		// Asynchronous reset active high
	input	[9:0]	write_addr,	// address to write byte (from 1 to 512)
	input	[7:0]	write_data0,// byte(s) to write into 512 array
	input	[7:0]	write_data1,
	input	[7:0]	write_data2,
	input	[7:0]	write_data3,
	input	[2:0]	write_size, // number of bytes (0-4) to write
	input			write_en,	// signal to write a byte (can write one byte per cycle)
	output reg		dmx_signal	// output dmx signal (continuous loop)
);
	/* states for DMX512 timing */
	typedef enum {IDLE, BREAK, MAB, TRANSMIT} state_t;
	/* timing counts */
	localparam IDLE_TIME	= 13'd2500; // 50us  * 50MHz = 2500 cycles
	localparam BREAK_TIME	= 13'd5000;	// 100us * 50MHz = 5000 cycles
	localparam MAB_TIME		= 13'd600;	// 12us  * 50MHz =  600 cycles
	localparam BIT_TIME		= 13'd200;	// 4us   * 50MHz =  200 cycles

	/* state register */
	state_t curr_state;
	state_t next_state;
	always_ff @(posedge clk or posedge rst) begin
		if (rst) begin
			/* reset into idle state */
			curr_state <= IDLE;
		end else begin
			/* take next state from combinational logic */
			curr_state <= next_state;
		end
	end

	/* offset addresses for four bytes to write (if overflow memory index of 512, then set zero) */
	/* set zero for bytes not being written (according to write_size) */
	wire [9:0] addr0, addr1, addr2, addr3;
	assign addr0 = (write_size >= 2'h1 && write_addr <= 10'h200) ? write_addr			: 10'h0;
	assign addr1 = (write_size >= 2'h2 && write_addr <= 10'h1ff) ? write_addr + 10'h1	: 10'h0;
	assign addr2 = (write_size >= 2'h3 && write_addr <= 10'h1fe) ? write_addr + 10'h2	: 10'h0;
	assign addr3 = (write_size >= 2'h4 && write_addr <= 10'h1fd) ? write_addr + 10'h3	: 10'h0;
	/* 512 bytes of dmx data to continuously send */
	reg [7:0] DMX_data [0:512];
	always_ff @(posedge clk or posedge rst) begin
		if (rst) begin
			/* reset all dmx values to zero */
			for (reg [9:0] i = 0; i <= 512; i++) begin
				DMX_data[i] <= 8'b0;
			end
		end else if (write_en) begin
			/* if writing, change only the current bytes (start code at index zero is read only) */
			if (addr0 != 10'h0) begin
				DMX_data[addr0] <= write_data0;
			end
			if (addr1 != 10'h0) begin
				DMX_data[addr1] <= write_data1;
			end
			if (addr2 != 10'h0) begin
				DMX_data[addr2] <= write_data2;
			end
			if (addr3 != 10'h0) begin
				DMX_data[addr3] <= write_data3;
			end
		end
		/* otherwise, no change (keep value) */
	end

	/* counter for current frame being sent (zero is start code, 512 is last dmx byte) */
	reg [9:0] frame_addr;
	reg first_frame;
	reg next_frame;
	always_ff @(posedge clk or posedge rst) begin
		if (rst) begin
			frame_addr <= 10'b0;
		end else if (first_frame) begin
			frame_addr <= 10'b0;
		end else if (next_frame) begin
			frame_addr <= frame_addr + 1'b1;
		end
	end

	/* shift register for current frame of dmx packet */
	reg [10:0] shift_reg;
	reg shift_load;
	reg shift_bit;
	always_ff @(posedge clk or posedge rst) begin
		if (rst) begin
			/* load zero on reset */
			shift_reg <= 11'b0;
		end else if (shift_load) begin
			/* load start bit (zero), data, and end bits (one one) into shift reg */
			shift_reg <= {2'b11, DMX_data[frame_addr], 1'b0};
		end else if (shift_bit) begin
			/* shift reg to send next bit */
			shift_reg <= shift_reg >> 1;
		end
	end

	/* counter for current bit being sent in shift reg */
	reg [3:0] bit_count;
	always_ff @(posedge clk or posedge rst) begin
		if (rst) begin
			/* asynch reset to zero */
			bit_count <= 4'b0;
		end else if (shift_load) begin
			/* synch reset to zero */
			bit_count <= 4'b0;
		end else if (shift_bit) begin
			/* increment count on each shift */
			bit_count <= bit_count + 1'b1;
		end
	end

	/* counter for timing controls */
	reg [12:0] timer;
	reg timer_rst;
	always_ff @(posedge clk or posedge rst) begin
		if (rst) begin
			/* asynch reset to zero */
			timer <= 13'b0;
		end else if (timer_rst) begin
			/* synch reset to zero */
			timer <= 13'b0;
		end else begin
			/* count while enabled */
			timer <= timer + 1'b1;
		end
	end

	always_comb begin
		dmx_signal = 1'b1;
		next_state = IDLE;
		first_frame = 1'b0;
		next_frame = 1'b0;
		shift_load = 1'b0;
		shift_bit = 1'b0;
		timer_rst = 1'b0;

		case (curr_state)
			IDLE: begin
				if (timer == IDLE_TIME) begin
					next_state = BREAK;
					timer_rst = 1'b1;
				end
			end
			BREAK: begin
				dmx_signal = 1'b0;
				next_state = BREAK;
				if (timer == BREAK_TIME) begin
					next_state = MAB;
					first_frame = 1'b1;
					timer_rst = 1'b1;
				end
			end
			MAB: begin
				next_state = MAB;
				if (timer == MAB_TIME) begin
					next_state = TRANSMIT;
					next_frame = 1'b1;
					shift_load = 1'b1;
					timer_rst = 1'b1;
				end
			end
			TRANSMIT: begin
				dmx_signal = shift_reg[0];
				next_state = TRANSMIT;
				if (timer == BIT_TIME) begin
					if (bit_count != 4'd10) begin
						/* bits left in current frame, shift to next bit */
						shift_bit = 1'b1;
					end else if (frame_addr != 10'd513) begin
						/* frames left in current packet, load next frame */
						next_frame = 1'b1;
						shift_load = 1'b1;
					end else begin
						/* finished sending last bit of last frame (done) */
						next_state = IDLE;
					end
					timer_rst = 1'b1;
				end
			end
		endcase
	end

endmodule