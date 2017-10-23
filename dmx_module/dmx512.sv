
module dmx512(
	input			clk,		// Clock
	input			rst,		// Asynchronous reset active high
	input	[9:0]	write_addr,	// address to write byte (from 1 to 512)
	input	[7:0]	write_data,	// byte to write into 512 array
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

	/* 512 bytes of dmx data to continuously send */
	reg [7:0] DMX_data [0:512];
	always_ff @(posedge clk or posedge rst) begin
		if (rst) begin
			/* reset all dmx values to zero */
			for (int i = 0; i <= 512; i++) begin
				DMX_data[i] <= 8'b0;
			end
		end else if (write_en && (write_addr != 10'b0)) begin
			/* if writing, change only the current byte (start code is read only) */
			DMX_data[write_addr] <= write_data;
		end
		/* otherwise, no change (keep value) */
	end

	/* counter for current frame being sent (zero is start code, 512 is last) */
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
			shift_reg <= {1'b0, DMX_data[frame_addr], 2'b11};
		end else if (shift_bit) begin
			/* shift reg to send next bit */
			shift_reg <= shift_reg << 1;
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
				dmx_signal = shift_reg[10];
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