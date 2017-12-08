package chuck.lighting;

import java.awt.Color;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import chuck.dmx.DMXDriver;

/**
 * Chuck Lighting Profile Class
 * 
 * Represents a single fixture in the lighting setup.
 *
 * @author Christian Krueger
 */
public class FixtureProfile implements Comparable<FixtureProfile> {

	private static final String[] COLOR_NAMES = {"red", "green", "blue", "amber", "white"};
	
	private DMXDriver dmxDriver;
	private String name;
	private int address;
	private int defaultColorOffs;

	private boolean isSelected = false;

	/**
	 * Map channel name (e.g., red) to channel offset. Address of red dmx value is
	 * getAddress() + getOffset("red")
	 */
	private Map<String, Integer> channelMap;

	/**
	 * Mirror of dmx values for this fixture (for saving and resetting state)
	 */
	private int[] dmxVals;

	/**
	 * Constructor. Create this fixture profile with a reference to the dmx driver,
	 * name, initial address, and channel settings. The channels array is a list of
	 * channel names to set for this fixture. For example, for a default 11-channel
	 * light, you can use LightingDefines.DEFAULT_CHANNELS.
	 * 
	 * @param dmx
	 *            reference to dmx driver (for changing this fixture's dmx values)
	 * @param fixtureName
	 *            name of this fixture (largely for debugging purposes)
	 * @param dmxAddress
	 *            dmx address for this fixture (must be within [1:512])
	 * @param channels
	 *            string containing the name for each channel (must be at least one
	 *            value, none can be empty)
	 */
	public FixtureProfile(DMXDriver dmx, String fixtureName, int dmxAddress, String[] channels) {
		// check arguments
		if (name == null || name == "")
			throw new IllegalArgumentException("empty name not allowed");
		// address must be within [1:512], check lower bound
		if (address < 1)
			throw new IllegalArgumentException("address must be at least 1");
		// cannot have a zero channel fixture, or a fixture with more than 512 channels
		if (channels.length < 1 || channels.length > 512)
			throw new IllegalArgumentException("number of channels must be within [1:512]");
		// check upper bound of address plus number of channels, can be at most 513
		// (address = 512, channels = 1)
		if (channels.length + address > 513)
			throw new IndexOutOfBoundsException("fixture tries to put channel outside of 512 bytes");

		dmxDriver = dmx;
		name = fixtureName;
		address = dmxAddress;

		defaultColorOffs = 0;

		// create the channel map
		channelMap = new HashMap<String, Integer>();
		for (int i = 0; i < channels.length; i++) {
			// make sure string is not empty
			if (channels[i] == null || channels[i].equals(""))
				throw new IllegalArgumentException("channel names cannot be null or empty");
			// add the value, and check return; put returns null when a new key is specified
			if (channelMap.put(channels[i], i) != null)
				throw new IllegalArgumentException("channels contains duplicate string");
		}

		// instantiate dmx mirror
		dmxVals = new int[channels.length];
	}

	public String getFixtureName() {
		return name;
	}

	public void setFixtureName(String name) {
		this.name = name;
	}

	public int getAddress() {
		return this.address;
	}

	public void setAddress(int address) {
		this.address = address;
	}

	/**
	 * Get the number of channels that this fixture occupies.
	 * 
	 * @return number of channels specified for this fixture
	 */
	public int getNumChannels() {
		return channelMap.size();
	}

	/**
	 * Get the current dmx values associated with this fixture
	 * 
	 * @return int array containing the dmx values set for this fixture (the length
	 *         of this array is equal to the number of channels for this fixture)
	 */
	public int[] getDMXVals() {
		return dmxVals.clone();
	}

	/**
	 * Set the dmx values for this fixture. Array specified must be same length as
	 * this fixture's number of channels.
	 * 
	 * @param dmxValueArray
	 *            array holding dmx values to set
	 * @throws IOException
	 *             if unable to access driver
	 */
	public void setDMXVals(int[] dmxValueArray) throws IOException {
		// ensure arrays are same size
		if (dmxValueArray.length != dmxVals.length)
			throw new IllegalArgumentException("array must be same size as numchannels");
		// ensure all dmx values are within range
		for (int i = 0; i < dmxValueArray.length; i++) {
			if (dmxValueArray[i] < 0 || dmxValueArray[i] > 255)
				throw new IllegalArgumentException("all dmx values must be within [0:255]");
		}
		// write the dmx values to the shadow and driver
		for (int i = 0; i < dmxValueArray.length; i++) {
			dmxVals[i] = dmxValueArray[i];
			dmxDriver.setDMX(address + i, dmxVals[i]);
		}
	}

	/**
	 * Sets the rgb color of this fixture. Only touches red, green, blue addresses
	 * in dmx module. Fails if red, green, and blue channels are not specified for
	 * this fixture.
	 * 
	 * @param color
	 *            color to set this fixture
	 * @throws IOException
	 *             if unable to access dmx driver
	 * @throws UnsupportedOperationException
	 *             if this fixture does not have red, green, and blue channels
	 */
	public void setColor(Color color) throws IOException, UnsupportedOperationException {
		if (channelMap.contains("red") &&)) {
			
		}
		// make sure rgb addresses are set
		if (!(checkRange(redOffs) && checkRange(blueOffs) && checkRange(greenOffs)))
			throw new IllegalStateException(
					String.format("Called set color on fixture without rgb; (addr,r,g,b) = (%d,%d,%d)\n", address,
							redOffs, greenOffs, blueOffs));
		dmxVals[redOffs] = color.getRed();
		dmxVals[greenOffs] = color.getGreen();
		dmxVals[blueOffs] = color.getBlue();

		// check for default case for optimal write speed
		if (redOffs == 1 && greenOffs == 2 && blueOffs == 3) {
			dmxDriver.setDMX(address + redOffs, color.getRed(), color.getGreen(), color.getBlue());
			return;
		}
		// otherwise just set them individually
		dmxDriver.setDMX(address + redOffs, color.getRed());
		dmxDriver.setDMX(address + greenOffs, color.getGreen());
		dmxDriver.setDMX(address + blueOffs, color.getBlue());
	}

	/**
	 * Sets the dmx dimmer value.
	 * 
	 * @param dimmerVal
	 *            fixtures new value
	 * @throws IOException
	 *             if unable to access dmx driver files
	 */
	public void setDimmerValue(int dimmerVal) throws IOException {
		dmxVals[channelMap.get("dimmer")] = dimmerVal;
		dmxDriver.setDMX(address + channelMap.get("dimmer"), dimmerVal);
	}

	/**
	 * Set the value of one of this fixtures channels. Channel is zero-indexed.
	 * 
	 * @param channel
	 *            relative channel to change (e.g., channel 0 will change this
	 *            fixtures first value)
	 * @param value
	 *            new value to put at dmx channel
	 * @throws IOException
	 *             if unable to access dmx driver files
	 */
	public void setChannelManual(int channel, int value) throws IOException {
		if (channel < 0 || channel >= dmxVals.length)
			throw new IllegalArgumentException("channel must be between [0:numChannels]");
		if (value < 0 || value >= 255)
			throw new IllegalArgumentException("value must be within [0:255]");

		dmxVals[channel] = value;
		dmxDriver.setDMX(address + channel, value);
	}

	/**
	 * Get whether or not this light has at least one color channel (red, green,
	 * blue, amber, white) and it is set to a non-zero dmx value.
	 * 
	 * @return true if and only if this fixture has a non-zero-valued color channel
	 */
	public boolean hasColor() {		
		for (String color : COLOR_NAMES) {
			if (channelMap.containsKey(color) && dmxVals[channelMap.get(color)] != 0) {
				return true;
			}
		}

		return false;
	}

	public void setDefaultColorOffest() {
		for (String color : COLOR_NAMES) {
			if (channelMap.containsKey(color)) {
				defaultColorOffs = channelMap.get(color);
			}
		}
	}

	public int getDefaultColorOffest() {
		return defaultColorOffs;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean selected) {
		isSelected = selected;
	}

	/**
	 * Comparison based on address; used for sorting in correct addressable order.
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(FixtureProfile light) {
		return this.address - light.getAddress();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String light = "";
		light += "----- " + this.name + " -----\n";
		light += "1. Address: " + this.address + "\n";
		light += "2. Channels: " + this.numChannels + "\n";
		light += "3. Dimmer: " + this.dimmerOffs + "\n";
		light += "4. Red: " + this.redOffs + "\n";
		light += "5. Green: " + this.greenOffs + "\n";
		light += "6. Blue: " + this.blueOffs + "\n";
		light += "7. Amber: " + this.amberOffs + "\n";
		light += "8. White: " + this.whiteOffs + "\n";
		light += "9. Strobe: " + this.strobeOffs + "\n";
		light += "10. Zoom: " + this.zoomOffs + "\n";
		light += "11. Pan: " + this.panOffs + "\n";
		light += "12. Pan Fine: " + this.panFineOffs + "\n";
		light += "13. Tilt: " + this.tiltOffs + "\n";
		light += "14. Tilt Fine: " + this.tiltFineOffs + "\n";
		light += "-------------" + "\n";
		return light;
	}

	/**
	 * @return csv representation for this fixture
	 */
	public String getCSV() {
		return this.name + "," + this.address + "," + this.numChannels + "," + this.dimmerOffs + "," + this.redOffs
				+ "," + this.greenOffs + "," + this.blueOffs + "," + this.amberOffs + "," + this.whiteOffs + ","
				+ this.strobeOffs + "," + this.zoomOffs + "," + this.panOffs + "," + this.panFineOffs + ","
				+ this.tiltOffs + "," + this.tiltFineOffs;
	}

	/**
	 * Check if the given address and offset are valid dmx parameters.
	 * 
	 * @param offs
	 *            feature offset
	 * @return true if and only if the sum of offset and address is within [1:512]
	 *         and offset is not negative
	 */
	private boolean checkRange(int offs) {
		// negative offset not valid
		if (offs < 0)
			return false;
		// check that sum is within [1:512]
		int newAddr = address + offs;
		return newAddr >= 1 && newAddr <= 512;
	}
}
