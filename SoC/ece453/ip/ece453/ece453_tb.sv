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
		rst = 1'b0;
		forever begin
			#10 clk = ~clk;
		end
	end // initial

	initial begin
		// initialize signals for test
		addr = 4'b0;
		read_en = 1'b0;
		write_en = 1'b0;
		write_data = 32'b0;
		byte_addr = 4'b0;
		inputs = 32'b0;

		// wait a quarter clock cycle so changes happen before edge
		#5;

		$finish;
	end

endmodule
