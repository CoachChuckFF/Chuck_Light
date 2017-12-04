package chuck.drivers;

import java.io.IOException;

public interface DMXDriver {

	/**
	 * Get the dmx value at the specified address.
	 * 
	 * @param address
	 *            dmx address to fetch, must be within [1:512]
	 * @return value at specified dmx address
	 */
	public int getDMX(int address);
	
	/**
	 * Get all the dmx values in the form of an int array. Array is 513 ints long,
	 * with arr[x] = dmx value at address x. <br />
	 * Implemented by returning a copy of the shadow array.
	 * 
	 * @return 513 int array containing dmx values
	 */
	public int[] getDmx();
	
	/**
	 * Set up to four dmx values in the dmx 512 byte array. <br />
	 * The first parameter in values is set at DMX address specified by address. The
	 * following three values parameters are set in the adjacent ascending
	 * addresses. For example, setDMX(3, 128, 256, 512) would set address 3 to 128,
	 * address 4 to 256, and address 5 to 512. No other values would be altered.
	 * <br />
	 * Also keeps track of the 512 byte array in dmxShadow. <br />
	 * All other methods use this as underlying implementation/access to array.
	 * 
	 * @param address
	 *            DMX address of first value to change; must be within [1:512]
	 * @param values
	 *            Up to four values within [0:255] (one byte) that will be written
	 *            at the DMX address
	 * @throws IOException
	 *             if unable to access the sys file registers needed to alter the
	 *             dmx fpga
	 */
	public void setDMX(int address, int... values) throws IOException;
	
	/**
	 * Set all 512 bytes of the DMX module (bulk load). <br />
	 * Uses setDMX(int address, int... values) as underlying implementation
	 * 
	 * @param values
	 *            elements 1-512 are loaded into the dmx module (must be 513
	 *            elements long, values[0] ignored)
	 * @throws IOException
	 *             if unable to access the sys file registers needed to alter the
	 *             dmx fpga
	 */
	public void setDMX(int[] values) throws IOException;
	
	/**
	 * Set all values of the DMX array to zero. <br />
	 * Uses setDMX(int address, int... values) as underlying implementation
	 * 
	 * @throws IOException
	 *             if unable to write the necessary registers in sys
	 */
	public void clearDMX() throws IOException;
	
}
