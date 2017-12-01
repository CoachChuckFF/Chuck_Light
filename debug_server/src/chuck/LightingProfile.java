package chuck;

/**
 * Chuck Lighting Profile Class
 * 
 * TODO: Christian: describe what this class represents, when/how to use it, etc.
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
	public LightingProfile() {
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
	public LightingProfile(String name, int address, int channels) {
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

	public int getDimmer() {
		return this.dimmer;
	}

	public void setDimmer(int dimmer) {
		this.dimmer = dimmer;
	}

	public int getRed() {
		return this.red;
	}

	public void setRed(int red) {
		this.red = red;
	}

	public int getGreen() {
		return this.green;
	}

	public void setGreen(int green) {
		this.green = green;
	}

	public int getBlue() {
		return this.blue;
	}

	public void setBlue(int blue) {
		this.blue = blue;
	}

	public int getAmber() {
		return this.amber;
	}

	public void setAmber(int amber) {
		this.amber = amber;
	}

	public int getWhite() {
		return this.white;
	}

	public void setWhite(int white) {
		this.white = white;
	}

	public int getStrobe() {
		return this.strobe;
	}

	public void setStrobe(int strobe) {
		this.strobe = strobe;
	}

	public int getZoom() {
		return this.zoom;
	}

	public void setZoom(int zoom) {
		this.zoom = zoom;
	}

	public int getPan() {
		return this.pan;
	}

	public void setPan(int pan) {
		this.pan = pan;
	}

	public int getPanFine() {
		return this.pan_fine;
	}

	public void setPanFine(int pan_fine) {
		this.pan_fine = pan_fine;
	}

	public int getTilt() {
		return this.tilt;
	}

	public void setTilt(int tilt) {
		this.tilt = tilt;
	}

	public int getTiltFine() {
		return this.tilt_fine;
	}

	public void setTiltFine(int tilt_fine) {
		this.tilt_fine = tilt_fine;
	}

	/* (non-Javadoc)
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

}
