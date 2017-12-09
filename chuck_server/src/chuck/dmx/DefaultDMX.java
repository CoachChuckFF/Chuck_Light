package chuck.dmx;

import java.nio.file.*;
import java.io.IOException;
import java.util.Arrays;

/**
 * Interface to DMX verilog module as defined in custom kernel drivers.
 * 
 * @author Joseph Eichenhofer
 *
 */
public class DefaultDMX implements DMXDriver {

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

	private int[] dmxShadow;

	private static final boolean debug_writes = false;

	/**
	 * Instantiates sys file paths, check read/write access, clear dmx values to
	 * zero.
	 * 
	 * @throws IOException
	 *             if unable to read/write any registers
	 */
	public DefaultDMX() throws IOException {
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

		dmxShadow = new int[513];
		clearDMX();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see chuck.drivers.DMXDriver#getDMX(int)
	 */
	@Override
	public int getDMX(int address) {
		if (address <= 0 || address > 512)
			throw new IllegalArgumentException("DMX address must be within [1:512]");
		return dmxShadow[address];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see chuck.drivers.DMXDriver#getDmx()
	 */
	@Override
	public int[] getDmx() {
		return dmxShadow.clone();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see chuck.drivers.DMXDriver#setDMX(int, int[])
	 */
	@Override
	public void setDMX(int address, int... values) throws IOException {
		if (address <= 0 || address > 512)
			throw new IllegalArgumentException("DMX address must be within [1:512]");
		if (values.length == 0)
			throw new IllegalArgumentException("Must supply at least one value.");
		if (values.length > 4)
			throw new IllegalArgumentException("Must supply at most four values.");
		if (Arrays.stream(values).anyMatch(val -> val < 0 || val > 255))
			throw new IllegalArgumentException("DMX values must be within [0:255]");

		// package four byte-length int values into one int
		// data_val holds the four bytes in 32 bits
		int data_val = 0;
		// bit_offset is the number of bits to shift each value into data_val
		// curr_val holds the truncated byte from each input value
		int bit_offset, curr_val;
		for (int i = 0; i < values.length; i++) {
			bit_offset = 8 * i;
			curr_val = values[i] & 0xff;
			data_val += curr_val << bit_offset;
			dmxShadow[address + i] = curr_val;
		}

		// write the four bytes out to the dmx register
		write_reg(data, data_val);
		write_reg(addr, address);
		write_reg(size, values.length);
		write_reg(ctrl, 0x1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see chuck.drivers.DMXDriver#setDMX(int[])
	 */
	@Override
	public void setDMX(int[] values) throws IOException {
		if (values.length != 513) {
			throw new IllegalArgumentException("values array must be 513 elements long");
		}

		for (int i = 1; i < 512; i = i + 4) {
			setDMX(i, values[i], values[i + 1], values[i + 2], values[i + 3]);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see chuck.drivers.DMXDriver#clearDMX()
	 */
	@Override
	public void clearDMX() throws IOException {
		for (int i = 1; i < 512; i = i + 4) {
			setDMX(i, 0, 0, 0, 0);
		}
	}

	/**
	 * Helper function to write a 32 bit value into the specified sys file register.
	 * 
	 * @param reg
	 *            path to the register for writing
	 * @param val
	 *            32 bit value to write to the register
	 * @throws IOException
	 *             if unable to write the register.
	 */
	private static void write_reg(Path reg, int val) throws IOException {
		byte[] buf = String.format("%08x\0", val).getBytes();
		if (buf.length != 9)
			throw new IllegalArgumentException(
					String.format("buffer length was %d from value %08x when trying to write to %s", buf.length, val,
							reg.toString()));

		Files.write(reg, buf, StandardOpenOption.SYNC);
		if (debug_writes)
			System.out.printf("Wrote %08x to %s\n", val, reg.toString());
	}
}
