module ece453_tb();
	`include "ece453.vh"

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
		
		// write 0xff to all four input bytes
		write_data = 32'hffff_ffff;
		addr = DMX_DATA_ADDR;
		write_en = 1'b1;
		#20;
		// set third to last byte as address (to write final three bytes)
		write_data = 32'h0000_01fe;
		addr = DMX_ADDR_ADDR;
		write_en = 1'b1;
		#20;
		// set size to much greater than four to signal writing all bytes
		write_data = 32'h0022_2222;
		addr = DMX_SIZE_ADDR;
		write_en = 1'b1;
		#20;
		
		// enable interrupts
		write_data = 32'h0000_0001;
		addr = IM_ADDR;
		write_en = 1'b1;
		#20;
		
		// commit byte changes
		write_data = CONTROL_DMX_START_MASK;
		addr = CONTROL_ADDR;
		write_en = 1'b1;
		#20;
		write_en = 1'b0;
		
		// read value of status right away
		addr = STATUS_ADDR;
		read_en = 1'b1;
		#20;
		
		// read value of status again
		addr = STATUS_ADDR;
		read_en = 1'b1;
		#20;

		// read value of status again
		addr = STATUS_ADDR;
		read_en = 1'b1;
		#20;

		$stop;
	end

endmodule
