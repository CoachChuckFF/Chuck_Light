module ece453_tb();
	`include "ece453.vh"
	
	localparam NUM_TESTS = 1000000000;

	reg 		clk, rst;
	reg	[3:0]	addr, byte_addr;
	reg			read_en, write_en;
	reg	[31:0]	read_data, write_data;
	reg	[31:0]	inputs, outputs;
	reg			irq_out;

	reg [31:0]	tmp_address, tmp_data, tmp_size;
	
	reg [7:0] DMX_mirror [0:512];
	
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
		
		tmp_address = 32'b0;
		tmp_data = 32'b0;
		tmp_size = 32'b0;
		
		/* reset all dmx values to zero */
		for (reg [9:0] i = 0; i <= 512; i++) begin
			DMX_mirror[i] <= 8'b0;
		end

		// wait a quarter clock cycle so changes happen before edge
		#5;
		
		// deassert reset after one clock cycle
		#20;
		rst = 1'b0;
		#20;
		
		// start testing writes
		for (int i = 0; i < NUM_TESTS; i++) begin
			// record old values to compare
			record_vals();
			// generate random values to test
			tmp_address = $urandom();
			tmp_data = $urandom();
			tmp_size = $urandom();
			// write to module
			write_dmx(tmp_data, tmp_address, tmp_size);
			// check for valid write state
			if (tmp_size == 32'h1) begin
				// check consistancy for size of 1
				if (tmp_address <= 32'd512) begin
					for (reg [9:0] i = 0; i <= 512; i++) begin
						if (i == tmp_address[9:0]) begin
							assert(tmp_data[7:0] == DUT.dmx_mod.DMX_data[i]) else $fatal("assigned wrong single value");
						end else begin
							assert(DMX_mirror[i] == DUT.dmx_mod.DMX_data[i]) else $fatal("wrote more than one value on single write");
						end
					end
				end else begin
					for (reg [9:0] i = 0; i <= 512; i++) begin
						assert(DMX_mirror[i] == DUT.dmx_mod.DMX_data[i]) else $fatal("assigned values when given address greater than 512");
					end
				end
			end else if (tmp_size == 32'h2) begin
				// check consistance for size of 2
				if (tmp_address <= 32'd512) begin
					for (reg [9:0] i = 0; i <= 512; i++) begin
						if (i == tmp_address[9:0]) begin
							assert(tmp_data[7:0] == DUT.dmx_mod.DMX_data[i]) else $fatal("assigned wrong first value in double write");
						end else if (i == (tmp_address[9:0] + 10'b1)) begin
							assert(tmp_data[15:8] == DUT.dmx_mod.DMX_data[i]) else $fatal("assigned wrong second value in double write");
						end else begin
							assert(DMX_mirror[i] == DUT.dmx_mod.DMX_data[i]) else $fatal("assigned out of place value in double write");
						end
					end
				end else begin
					for (reg [9:0] i = 0; i <= 512; i++) begin
						assert(DMX_mirror[i] == DUT.dmx_mod.DMX_data[i]) else $fatal("assigned values when given address greater than 512");
					end
				end
			end else if (tmp_size == 32'h3) begin
				// check consistance for size of 3
				if (tmp_address <= 32'd512) begin
					for (reg [9:0] i = 0; i <= 512; i++) begin
						if (i == tmp_address[9:0]) begin
							assert(tmp_data[7:0] == DUT.dmx_mod.DMX_data[i]) else $fatal("assigned wrong first value in triple write");
						end else if (i == (tmp_address[9:0] + 10'b1)) begin
							assert(tmp_data[15:8] == DUT.dmx_mod.DMX_data[i]) else $fatal("assigned wrong second value in triple write");
						end else if (i == (tmp_address[9:0] + 10'h2)) begin
							assert(tmp_data[23:16] == DUT.dmx_mod.DMX_data[i]) else $fatal("assigned wrong third value in triple write");
						end else begin
							assert(DMX_mirror[i] == DUT.dmx_mod.DMX_data[i]) else $fatal("assigned out of place value in triple write");
						end
					end
				end else begin
					for (reg [9:0] i = 0; i <= 512; i++) begin
						assert(DMX_mirror[i] == DUT.dmx_mod.DMX_data[i]) else $fatal("assigned values when given address greater than 512");
					end
				end
			end else if (tmp_size == 32'h4) begin
				// check consistance for size of 4
				if (tmp_address <= 32'd512) begin
					for (reg [9:0] i = 0; i <= 512; i++) begin
						if (i == tmp_address[9:0]) begin
							assert(tmp_data[7:0] == DUT.dmx_mod.DMX_data[i]) else $fatal("assigned wrong first value in quad write");
						end else if (i == (tmp_address[9:0] + 10'b1)) begin
							assert(tmp_data[15:8] == DUT.dmx_mod.DMX_data[i]) else $fatal("assigned wrong second value in quad write");
						end else if (i == (tmp_address[9:0] + 10'h2)) begin
							assert(tmp_data[23:16] == DUT.dmx_mod.DMX_data[i]) else $fatal("assigned wrong third value in quad write");
						end else if (i == (tmp_address[9:0] + 10'h3)) begin
							assert(tmp_data[31:24] == DUT.dmx_mod.DMX_data[i]) else $fatal("assigned wrong fourth value in quad write");
						end else begin
							assert(DMX_mirror[i] == DUT.dmx_mod.DMX_data[i]) else $fatal("assigned out of place value in quad write");
						end
					end
				end else begin
					for (reg [9:0] i = 0; i <= 512; i++) begin
						assert(DMX_mirror[i] == DUT.dmx_mod.DMX_data[i]) else $fatal("assigned values when given address greater than 512");
					end
				end
			end else begin
				// any size other than 1, 2, 3, or 4 should not change state
				for (reg [9:0] i = 0; i <= 512; i++) begin
					assert(DMX_mirror[i] == DUT.dmx_mod.DMX_data[i]) else $fatal("assigned value when given zero size");
				end
			end
		end

		$stop;
	end

	task write_dmx(input reg [31:0] data, input reg [31:0] address, input reg [31:0] size);
		write_data = data;
		addr = DMX_DATA_ADDR;
		write_en = 1'b1;
		#20;
		write_en = 1'b0;
		#20;
		
		write_data = address;
		addr = DMX_ADDR_ADDR;
		write_en = 1'b1;
		#20;
		write_en = 1'b0;
		#20;
		
		write_data = data;
		addr = DMX_SIZE_ADDR;
		write_en = 1'b1;
		#20;
		write_en = 1'b0;
		#20;
		
		write_data = $urandom() & 32'b1;
		addr = CONTROL_ADDR;
		write_en = 1'b1;
		#20;
		write_en = 1'b0;
		#20;
	endtask
	
	task record_vals();
		for (reg [9:0] i = 0; i <= 512; i++) begin
			DMX_mirror[i] = DUT.dmx_mod.DMX_data[i];
		end
	endtask
	
endmodule
