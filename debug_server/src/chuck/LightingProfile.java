package chuck;

import java.awt.Color;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import com.google.common.collect.BiMap;

import chuck.drivers.DMXDriver;

/**
 * Chuck Lighting Profile Class
 * 
 * Represents a single fixture in the lighting setup.
 *
 * @author Christian Krueger and Joseph Eichenhofer
 */
public class LightingProfile implements Comparable<LightingProfile> {
	
	/**
	 * Offset settings for this lighting fixture
	 */
	private Map<String, Integer> offsets;

	/**
	 * name of this light fixture
	 */
	private String name;
	/**
	 * what address the fixture is set at
	 */
	private int address;
	/**
	 * Up-to-date copy of this fixture's dmx values
	 */
	private int[] dmxVals;
	
	private DMXDriver dmxDriver;

	/**
	 * Constructor.
	 * 
	 * @param name
	 * @param address
	 * @param channels
	 */
	public LightingProfile(DMXDriver dmx, String name, int address, BiMap<String, Integer> channelOffsets) {
		if (name == null || name == "")
			throw new IllegalArgumentException("empty name not allowed");
		if (address < 1)
			throw new IllegalArgumentException("address must be at least 1");
		if (channelOffsets.size() < 1)
			throw new IllegalArgumentException("must have at least one channel");
		if (channelOffsets.size() + address > 513)
			throw new IndexOutOfBoundsException("fixture tries to put channel outside of 512 bytes");
		
		this.name = name;
		this.address = address;
		dmxVals = new int[channelOffsets.size()];
		offsets = new HashMap<String, Integer>(channelOffsets);
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAddress() {
		return this.address;
	}

	public void setAddress(int address) {
		this.address = address;
	}

	public int getNumChannels() {
		return offsets.size();
	}
	
	/**
	 * Get the dmx values associated with this fixture
	 * 
	 * @return int array containing the dmx values set for this fixture (length == #channels)
	 */
	public int[] getDMXVals() {
		return dmxVals.clone();
	}

	/**
	 * Sets the rgb color of this fixture. Only touches red, green, blue addresses
	 * in dmx module.
	 * 
	 * @param color
	 *            this fixtures new color
	 * @throws IOException
	 *             if unable to access dmx driver files
	 */
	public void setColor(Color color) throws IOException {
		// make sure rgb addresses are set
		if (!(checkRange(offsets.redOffs) && checkRange(offsets.blueOffs) && checkRange(offsets.greenOffs)))
			throw new IllegalStateException(
					String.format("Called set color on fixture without rgb; (addr,r,g,b) = (%d,%d,%d)\n", address, offsets.redOffs,
							offsets.greenOffs, offsets.blueOffs));
		// check for default case for optimal write speed
		if (offsets.redOffs == 1 && offsets.greenOffs == 2 && offsets.blueOffs == 3) {
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
		dmxDriver.setDMX(address + dimmerOffs, dimmerVal);
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
		dmxDriver.setDMX(address + channel, value);
	}

	/**
	 * Comparison based on address; used for sorting in correct addressable order.
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(LightingProfile light) {
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
		light += "2. Channels: " + this.channels + "\n";
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
		return this.name + "," + this.address + "," + this.channels + "," + this.dimmerOffs + "," + this.redOffs + ","
				+ this.greenOffs + "," + this.blueOffs + "," + this.amberOffs + "," + this.whiteOffs + "," + this.strobeOffs + ","
				+ this.zoomOffs + "," + this.panOffs + "," + this.panFineOffs + "," + this.tiltOffs + "," + this.tiltFineOffs;
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
