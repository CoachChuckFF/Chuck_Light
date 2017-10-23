module ece453_tb();
	reg 		clk, rst;
	reg	[9:0]	addr;
	reg [7:0]	data;
	reg			write;
	wire		dmx;

	dmx512 DUT(
		.clk(clk),			// Clock
		.rst(rst),			// Asynchronous reset active high
		.write_addr(addr),	// address to write byte (from 1 to 512)
		.write_data(data),	// byte to write into 512 array
		.write_en(write),	// signal to write a byte (can write one byte per cycle)
		.dmx_signal(dmx)	// output dmx signal (continuous loop)
	);

	initial begin
		clk = 1'b0;
		forever begin
			#10 clk = ~clk;
		end
	end // initial

	initial begin
		// initialize signals for test
		rst = 1'b1;
		addr = 10'b0;
		data = 8'b0;
		write = 1'b0;

		// wait a quarter clock cycle so changes happen before edge
		#5;
		
		// deassert reset after one clock cycle
		#20;
		rst = 1'b0;
		
		#20;
		
		// write ff to last byte
		addr = 10'd512;
		data = 8'hff;
		write = 1'b1;
		#20;
		
		$stop;
	end

endmodule
