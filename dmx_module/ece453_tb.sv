module ece453_tb();
	reg 		clk, rst;
	reg	[3:0]	addr, byte_addr;
	reg			read_en, write_en;
	reg	[31:0]	read_data, write_data;
	reg	[31:0]	inputs, outputs;
	reg			irq_out;

	ece453 DUT(
		.clk(clk),
		.reset(rst),
		.slave_address(addr),
		.slave_read(read_en),
		.slave_write(write_en),
		.slave_readdata(read_data),
		.slave_writedata(write_data),
		.slave_byteenable(byte_addr),
		.gpio_inputs(inputs),
		.gpio_outputs(outputs),
		.irq_out(irq_out)
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
		addr = 4'b0;
		read_en = 1'b0;
		write_en = 1'b0;
		write_data = 32'b0;
		byte_addr = 4'b0;
		inputs = 32'b0;

		// wait a quarter clock cycle so changes happen before edge
		#5;
		
		// deassert reset after one clock cycle
		#20;
		rst = 1'b0;
		
		#20;
		
		// write all 0x00ff_ffff to LED 0
		write_data = 32'h00FF_FFFF;
		addr = 4'h8;
		write_en = 1'b1;
		#20;
		// write all 1s to LED 1
		write_data = 32'h0011_1111;
		addr = 4'h9;
		write_en = 1'b1;
		#20;
		// write all 2s to LED 2
		write_data = 32'h0022_2222;
		addr = 4'ha;
		write_en = 1'b1;
		#20;
		// write all 3s to LED 3
		write_data = 32'h0033_3333;
		addr = 4'hb;
		write_en = 1'b1;
		#20;
		// write all 4s to LED 4
		write_data = 32'h0044_4444;
		addr = 4'hc;
		write_en = 1'b1;
		#20;
		// write all 5s to LED 5
		write_data = 32'h0055_5555;
		addr = 4'hd;
		write_en = 1'b1;
		#20;
		// write all 6s to LED 6
		write_data = 32'h0066_6666;
		addr = 4'he;
		write_en = 1'b1;
		#20;
		// write all 7s to LED 7
		write_data = 32'h0077_7777;
		addr = 4'hf;
		write_en = 1'b1;
		#20;
		
		// enable interrupts
		write_data = 32'h0000_0001;
		addr = 4'h3;
		#20;
		
		
		// send out signals
		write_data = 32'b1;
		addr = 4'h1;
		write_en = 1'b1;
		#20;
		write_en = 1'b0;
		
		@(posedge irq_out);
		
		$stop;
	end

endmodule
