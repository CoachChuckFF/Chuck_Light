
/*

  Author:  Joe Krachey
  Date:  01/03/2017

*/


module ece453(
	// signals to connect to an Avalon clock source interface
	clk,
	reset,
	// signals to connect to an Avalon-MM slave interface
	slave_address,
	slave_read,
	slave_write,
	slave_readdata,
	slave_writedata,
	slave_byteenable,
	gpio_inputs,
	gpio_outputs,
	irq_out
);

	// most of the set values will only be used by the component .tcl file.  The DATA_WIDTH and MODE_X = 3 influence the hardware created.
	// ENABLE_SYNC_SIGNALS isn't used by this hardware at all but it provided anyway so that it can be exposed in the component .tcl file
	// to control the stubbing of certain signals.
	//parameter ENABLE_SYNC_SIGNAL	S = 0;  // only used by the component .tcl file, 1 to expose user_chipselect/write/read, 0 to stub them
	

	//*******************************************************************
	// Module Interface
	//*******************************************************************
	input clk;
	input reset;
	
	// slave interface
	input [3:0] slave_address;
	input slave_read;
	input slave_write;
	output wire [31:0] slave_readdata;
	input [31:0] slave_writedata;
	input [3:0] slave_byteenable;

	input [31:0] gpio_inputs;
	output [31:0] gpio_outputs;
	output wire irq_out;

	`include "ece453.vh"

	//*******************************************************************
	// Register Set
	//*******************************************************************
	reg  [31:0] dev_id_r;
	reg  [31:0] gpio_in_r;
	reg  [31:0] gpio_out_r;
	
	//*******************************************************************
	// Wires/Reg
	//*******************************************************************
	wire  [31:0] gpio_in;
	wire  [31:0] gpio_out;


	//*******************************************************************
	// Register Read Assignments
	//*******************************************************************
	assign slave_readdata = ( (slave_address == DEV_ID_ADDR )    && slave_read )	? dev_id_r :
	                        ( (slave_address == GPIO_IN_ADDR )   && slave_read ) 	? gpio_in_r :
	                        ( (slave_address == GPIO_OUT_ADDR )  && slave_read ) 	? gpio_out_r : 32'h00000000;

	
	
	//*******************************************************************
	// Output Assignments
	//*******************************************************************
   
	// IRQ indicating that an interrupt is active	
	assign irq_out = 0;
	assign gpio_outputs = gpio_out_r; 

	//*******************************************************************
	// Register Input Equations
	//*******************************************************************
	

	
	// Input signals for registers
	assign gpio_in		= gpio_inputs;
	assign gpio_out  	= ( (slave_address == GPIO_OUT_ADDR ) 	&& slave_write ) ? slave_writedata : gpio_out_r;
	
	
	//*******************************************************************
	// Registers
	//*******************************************************************
	always @ (posedge clk or posedge reset)
	begin
		if (reset == 1)
		begin
		   dev_id_r	<= 32'hECE45300;
			gpio_in_r	<= 32'h00000000;
			gpio_out_r	<= 32'h00000000;

			
		end
		else
		begin
		   dev_id_r		<= 32'hECE45300;
			gpio_in_r	<= gpio_in;
			gpio_out_r	<= gpio_out;
		end
	end								
											
endmodule






