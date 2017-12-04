package chuck;

import java.awt.Color;
import java.io.IOException;

import chuck.drivers.DMXDriver;

/**
 * Chuck Lighting Profile Class
 * 
 * Represents a single fixture in the lighting setup.
 *
 * @author Christian Krueger
 */
public class LightingProfile implements Comparable<LightingProfile> {

	/**
	 * name of this light fixture
	 */
	private String name;
	/**
	 * what address the fixture is set at
	 */
	private int address;
	/**
	 * how many channels the fixture has
	 */
	private int channels;
	private DMXDriver dmxDriver;


	/*
	 * One int for each fixture "function"; the int represents the offset from the address
	 * where you would find this function's byte in the DMX 512 byte array
	 * 
	 * Value is -1 if function does not exist
	 * 
	 * e.g., dimmer byte is at getAddress() + getDimmer()
	 */
	private int dimmer;
	private int red;
	private int green;
	private int blue;
	private int amber;
	private int white;
	private int strobe;
	private int zoom;
	private int pan;
	private int pan_fine;
	private int tilt;
	private int tilt_fine;

	/**
	 * Constructor.
	 */
	public LightingProfile(DMXDriver dmx) {
		dmxDriver = dmx;
		name = "unset";
		address = 0;
		channels = 1;
		
		dimmer = -1;
		red = -1;
		green = -1;
		blue = -1;
		amber = -1;
		white = -1;
		strobe = -1;
		zoom = -1;
		pan = -1;
		pan_fine = -1;
		tilt = -1;
		tilt_fine = -1;
	}

	/**
	 * Constructor.
	 * 
	 * @param name
	 * @param address
	 * @param channels
	 */
	public LightingProfile(DMXDriver dmx, String name, int address, int channels) {
		// populate default values
		this(dmx);
		// check arguments
		if (name == null || name == "")
			throw new IllegalArgumentException("empty name not allowed");
		if (address < 1)
			throw new IllegalArgumentException("address must be at least 1");
		if (channels < 1)
			throw new IllegalArgumentException("must have at least one channel");
		if (channels + address > 512)
			throw new IndexOutOfBoundsException("fixture tries to put channel outside of 512 bytes");

		this.name = name;
		this.address = address;
		this.channels = channels;
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

	public int getChannels() {
		return this.channels;
	}

	public void setChannels(int channels) {
		this.channels = channels;
	}

	public void setDimmer(int dimmer) {
		this.dimmer = dimmer;
	}

	public void setRed(int red) {
		this.red = red;
	}

	public void setGreen(int green) {
		this.green = green;
	}

	public void setBlue(int blue) {
		this.blue = blue;
	}

	public void setAmber(int amber) {
		this.amber = amber;
	}

	public void setWhite(int white) {
		this.white = white;
	}

	public void setStrobe(int strobe) {
		this.strobe = strobe;
	}

	public void setZoom(int zoom) {
		this.zoom = zoom;
	}

	public void setPan(int pan) {
		this.pan = pan;
	}

	public void setPanFine(int pan_fine) {
		this.pan_fine = pan_fine;
	}

	public void setTilt(int tilt) {
		this.tilt = tilt;
	}

	public void setTiltFine(int tilt_fine) {
		this.tilt_fine = tilt_fine;
	}
	
	/**
	 * Sets the rgb color of this fixture. Only touches red, green, blue addresses in dmx module.
	 * 
	 * @param color
	 * 	this fixtures new color
	 * @throws IOException if unable to access dmx driver files
	 */
	public void setColor(Color color) throws IOException {
		
	}
	
	/**
	 * Sets the dmx dimmer value.
	 * 
	 * @param dimmerVal
	 * 	fixtures new value
	 * @throws IOException if unable to access dmx driver files
	 */
	public void setDimmerValue(int dimmerVal) throws IOException {
		
	}
	
	/**
	 * Set the value of one of this fixtures channels. Channel is one-indexed.
	 * 
	 * @param channel
	 * 	relative channel to change (e.g., channel 1 will change this fixtures first value)
	 * @param value
	 * 	new value to put at dmx channel
	 * @throws IOException if unable to access dmx driver files
	 */
	public void setChannelManual(int channel, int value) throws IOException {
		
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

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String light = "";
		light += "----- " + this.name + " -----\n";
		light += "1. Address: " + this.address + "\n";
		light += "2. Channels: " + this.channels + "\n";
		light += "3. Dimmer: " + this.dimmer + "\n";
		light += "4. Red: " + this.red + "\n";
		light += "5. Green: " + this.green + "\n";
		light += "6. Blue: " + this.blue + "\n";
		light += "7. Amber: " + this.amber + "\n";
		light += "8. White: " + this.white + "\n";
		light += "9. Strobe: " + this.strobe + "\n";
		light += "10. Zoom: " + this.zoom + "\n";
		light += "11. Pan: " + this.pan + "\n";
		light += "12. Pan Fine: " + this.pan_fine + "\n";
		light += "13. Tilt: " + this.tilt + "\n";
		light += "14. Tilt Fine: " + this.tilt_fine + "\n";
		light += "-------------" + "\n";
		return light;
	}
	
	/**
	 * @return csv representation for this fixture
	 */
	public String getCSV() {
		return this.name + "," + this.address + "," + this.channels + "," + this.dimmer + ","
				+ this.red + "," + this.green + "," + this.blue + "," + this.amber + ","
				+ this.white + "," + this.strobe + "," + this.zoom + "," + this.pan + ","
				+ this.pan_fine + "," + this.tilt + "," + this.tilt_fine;
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
