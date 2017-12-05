package chuck;

import java.awt.Color;
import java.io.IOException;
import java.util.Arrays;

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
	private int dimmerOffs;
	private int redOffs;
	private int greenOffs;
	private int blueOffs;
	private int amberOffs;
	private int whiteOffs;
	private int strobeOffs;
	private int zoomOffs;
	private int panOffs;
	private int panFineOffs;
	private int tiltOffs;
	private int tiltFineOffs;
	
	private int defaultColorOffs;
	
	private int[] dmxVals;

	/**
	 * Constructor.
	 * 
	 * @param name
	 * @param address
	 * @param channels
	 */
	public LightingProfile(DMXDriver dmx, String name, int address, int channels) {
		// check arguments
		if (name == null || name == "")
			throw new IllegalArgumentException("empty name not allowed");
		if (address < 1)
			throw new IllegalArgumentException("address must be at least 1");
		if (channels < 1)
			throw new IllegalArgumentException("must have at least one channel");
		if (channels + address > 512)
			throw new IndexOutOfBoundsException("fixture tries to put channel outside of 512 bytes");

		dmxDriver = dmx;
		this.name = name;
		this.address = address;
		this.channels = channels;
		
		dimmerOffs = -1;
		redOffs = -1;
		greenOffs = -1;
		blueOffs = -1;
		amberOffs = -1;
		whiteOffs = -1;
		strobeOffs = -1;
		zoomOffs = -1;
		panOffs = -1;
		panFineOffs = -1;
		tiltOffs = -1;
		tiltFineOffs = -1;
		
		defaultColorOffs = 0;
		
		dmxVals = new int[channels];
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
		this.dimmerOffs = dimmer;
	}

	public void setRed(int red) {
		this.redOffs = red;
	}

	public void setGreen(int green) {
		this.greenOffs = green;
	}

	public void setBlue(int blue) {
		this.blueOffs = blue;
	}

	public void setAmber(int amber) {
		this.amberOffs = amber;
	}

	public void setWhite(int white) {
		this.whiteOffs = white;
	}

	public void setStrobe(int strobe) {
		this.strobeOffs = strobe;
	}

	public void setZoom(int zoom) {
		this.zoomOffs = zoom;
	}

	public void setPan(int pan) {
		this.panOffs = pan;
	}

	public void setPanFine(int pan_fine) {
		this.panFineOffs = pan_fine;
	}

	public void setTilt(int tilt) {
		this.tiltOffs = tilt;
	}

	public void setTiltFine(int tilt_fine) {
		this.tiltFineOffs = tilt_fine;
	}
	
	/**
	 * Get the dmx values associated with this fixture
	 * 
	 * @return int array containing the dmx values set for this fixture (length == #channels)
	 */
	public int[] getDMXVals() {
		return dmxVals.clone();
	}
	
	public void setDMXVals(int[] dmxVals) {
		this.dmxVals = dmxVals.clone();
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
		if (!(checkRange(redOffs) && checkRange(blueOffs) && checkRange(greenOffs)))
			throw new IllegalStateException(
					String.format("Called set color on fixture without rgb; (addr,r,g,b) = (%d,%d,%d)\n", address, redOffs,
							greenOffs, blueOffs));
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
		dmxVals[dimmerOffs] = dimmerVal;
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
		dmxVals[channel] = value;
		dmxDriver.setDMX(address + channel, value);
	}

	public boolean hasColor() {
		
		if(redOffs != -1)
		{
			if(dmxVals[redOffs] != 0)
				return true;
		}
		if(greenOffs != -1)
		{
			if(dmxVals[greenOffs] != 0)
				return true;
		}
		if(blueOffs != -1)
		{
			if(dmxVals[blueOffs] != 0)
				return true;
		}
		if(amberOffs != -1)
		{
			if(dmxVals[amberOffs] != 0)
				return true;
		}
		if(whiteOffs != -1)
		{
			if(dmxVals[whiteOffs] != 0)
				return true;
		}

		
		return false;
		
	}
	
	public void setDefaultColorOffest (){
		if(whiteOffs != -1){
			defaultColorOffs = whiteOffs;
		}else if(redOffs != -1){
			defaultColorOffs = redOffs;
		} else if(greenOffs != -1){
			defaultColorOffs = greenOffs;
		}else if(blueOffs != -1){
			defaultColorOffs = blueOffs;
		}else if(amberOffs != -1){
			defaultColorOffs = amberOffs;
		}
	}
	
	//reads DMX shadow array and updates lights DMX array
	public void syncLight(){
		System.arraycopy(this.dmxDriver.getDmx(), this.address, this.dmxVals, 0, this.channels);
	}
	
	public int getDefaultColorOffest (){
		return this.defaultColorOffs;
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
		return this.name + "," + this.address + "," + this.channels + "," + this.dimmerOffs + ","
				+ this.redOffs + "," + this.greenOffs + "," + this.blueOffs + "," + this.amberOffs + ","
				+ this.whiteOffs + "," + this.strobeOffs + "," + this.zoomOffs + "," + this.panOffs + ","
				+ this.panFineOffs + "," + this.tiltOffs + "," + this.tiltFineOffs;
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
