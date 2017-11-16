package chuck.drivers;

import java.nio.file.*;
import java.io.IOException;
import java.util.Arrays;

/**
 * Interface to DMX verilog module as defined in custom kernel drivers.
 * 
 * @author Joseph Eichenhofer
 *
 */
public class DMXDriver {

	private static final String ADDR_FILENAME = "/sys/kernel/ece453/dmx_addr";
	private static final String DATA_FILENAME = "/sys/kernel/ece453/dmx_data";
	private static final String SIZE_FILENAME = "/sys/kernel/ece453/dmx_size";
	private static final String CTRL_FILENAME = "/sys/kernel/ece453/control";
	private static final String STAT_FILENAME = "/sys/kernel/ece453/status";

	private Path addr;
	private Path data;
	private Path size;
	private Path ctrl;
	private Path stat;

	/**
	 * Constructor.
	 * Attempt to initialize the driver, checking each sys file register needed.
	 * 
	 * @throws IOException	if any of the sys file registers are not available.
	 */
	public DMXDriver() throws IOException {
		addr = Paths.get(ADDR_FILENAME);
		data = Paths.get(DATA_FILENAME);
		size = Paths.get(SIZE_FILENAME);
		ctrl = Paths.get(CTRL_FILENAME);
		stat = Paths.get(STAT_FILENAME);

		if (!Files.isWritable(addr))
			throw new IOException(ADDR_FILENAME + " not writable.");
		if (!Files.isWritable(data))
			throw new IOException(DATA_FILENAME + " not writable.");
		if (!Files.isWritable(size))
			throw new IOException(SIZE_FILENAME + " not writable.");
		if (!Files.isWritable(ctrl))
			throw new IOException(CTRL_FILENAME + " not writable.");
		if (!Files.isReadable(stat))
			throw new IOException(STAT_FILENAME + " not readable.");
	}

	/**
	 * Set up to four dmx values in the dmx 512 byte array. The first parameter in values is set at
	 * DMX address specified by address. The following three values parameters are set in the adjacent
	 * ascending addresses. For example, setDMX(3, 128, 256, 512) would set address 3 to 128, address
	 * 4 to 256, and address 5 to 512. No other values would be altered.
	 * 
	 * @param address	DMX address of first value to change; must be within [1:512]
	 * @param values	Up to four values within [0:255] (one byte) that will be written at the DMX address
	 * @throws IOException	if unable to access the sys file registers needed to alter the dmx fpga
	 */
	public void setDMX(int address, int... values) throws IOException {
		if (address <= 0 || address > 512)
			throw new IllegalArgumentException("DMX address must be within [1:512]");
		if (values.length == 0)
			throw new IllegalArgumentException("Must supply at least one value.");
		if (values.length > 4)
			throw new IllegalArgumentException("Must supply at most four values.");
		if (Arrays.stream(values).anyMatch(val -> val < 0 || val > 255))
			throw new IllegalArgumentException("DMX values must be within [0:255]");

		int data_val = values[0];
		if (values.length > 1)
			data_val = (values[1] & 0xff) << 8;
		if (values.length > 2)
			data_val = (values[2] & 0xff) << 16;
		if (values.length > 3)
			data_val = (values[3] & 0xff) << 14;

		write_reg(data, data_val);
		write_reg(addr, address);
		write_reg(size, values.length);
		write_reg(ctrl, 0x1);
	}

	/**
	 * Set all values of the DMX array to zero.
	 * 
	 * @throws IOException	if unable to write the necessary registers in sys
	 */
	public void clearDMX() throws IOException {
		for (int i = 1; i < 512; i = i + 4) {
			setDMX(i, 0, 0, 0, 0);
		}
	}

	/**
	 * Helper function to write a 32 bit value into the specified sys file register.
	 * 
	 * @param reg	path to the register for writing
	 * @param val	32 bit value to write to the register
	 * @throws IOException	if unable to write the register.
	 */
	private static void write_reg(Path reg, int val) throws IOException {
		byte[] buf = String.format("%08x\0", val).getBytes();
		if (buf.length != 9)
			throw new IllegalArgumentException(
					String.format("buffer length was %d from value %08x when trying to write to %s", buf.length, val,
							reg.toString()));

		Files.write(reg, buf, StandardOpenOption.SYNC);
	}
}
