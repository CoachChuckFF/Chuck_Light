create_clock -period 10.0 [get_pins -compatibility_mode *|fpga_interfaces|peripheral_spim0|sclk_out]
create_clock -period 10.0 [get_pins -compatibility_mode *|fpga_interfaces|peripheral_i2c2|out_clk]