package chuck;

import java.awt.Color;
import java.io.IOException;

import chuck.drivers.DMXDriver;

/**
 * Chuck Lighting Profile Class
 * 
 * Represents a single fixture in the lighting setup.
 *
 * @author Christian Krueger and Joseph Eichenhofer
 */
public class LightingProfile implements Comparable<LightingProfile> {
	
	public class LightingProfileChannelOffsets {
		
		/**
		 * Maximum number of channels. NOTE: must match the number of private offset variables defined for this class
		 */
		private static final int MAX_CHANNELS = 12;
		
		/*
		 * One int for each fixture "function"; the int represents the offset from the
		 * address where you would find this function's byte in the DMX 512 byte array
		 * 
		 * Value is -1 if function does not exist
		 * 
		 * e.g., dimmer byte is at getAddress() + getDimmer()
		 */
		private int dimmerOffs = -1;
		private int redOffs = -1;
		private int greenOffs = -1;
		private int blueOffs = -1;
		private int amberOffs = -1;
		private int whiteOffs = -1;
		private int strobeOffs = -1;
		private int zoomOffs = -1;
		private int panOffs = -1;
		private int panFineOffs = -1;
		private int tiltOffs = -1;
		private int tiltFineOffs = -1;
		
		/**
		 * Get the number of channels set in this description.
		 * 
		 * @return
		 * 	number of channels set for this settings description
		 */
		int numChannels() {
			return 
			
			return Math.max(
					dimmerOffs,
					redOffs,
					greenOffs,
					blueOffs,
					amberOffs,
					whiteOffs,
					strobeOffs,
					zoomOffs,
					panOffs,
					panFineOffs,
					tiltOffs,
					tiltFineOffs
					);
		}

		/**
		 * Checks that offset is valid and sets it.
		 * 
		 * @param dimmerOffs new dimmer offset
		 */
		public void setDimmerOffs(int dimmerOffs) {
			if (dimmerOffs < 0 || dimmerOffs >= MAX_CHANNELS)
				throw new IllegalArgumentException("Invalid dimmer offset: " + dimmerOffs);
			this.dimmerOffs = dimmerOffs;
		}

		/**
		 * Checks that offset is valid and sets it.
		 * 
		 * @param redOffs new red offset
		 */
		public void setRedOffs(int redOffs) {
			if (redOffs < 0 || redOffs >= MAX_CHANNELS)
				throw new IllegalArgumentException("Invalid red offset: " + redOffs);
			this.redOffs = redOffs;
		}

		/**
		 * Checks that offset is valid and sets it.
		 * 
		 * @param greenOffs new green offset
		 */
		public void setGreenOffs(int greenOffs) {
			if (greenOffs < 0 || greenOffs >= MAX_CHANNELS)
				throw new IllegalArgumentException("Invalid green offset: " + greenOffs);
			this.greenOffs = greenOffs;
		}

		/**
		 * Checks that offset is valid and sets it.
		 * 
		 * @param blueOffs new blue offset
		 */
		public void setBlueOffs(int blueOffs) {
			if (blueOffs < 0 || blueOffs >= MAX_CHANNELS)
				throw new IllegalArgumentException("Invalid blue offset: " + blueOffs);
			this.blueOffs = blueOffs;
		}

		/**
		 * Checks that offset is valid and sets it.
		 * 
		 * @param amberOffs new amber offset
		 */
		public void setAmberOffs(int amberOffs) {
			if (amberOffs < 0 || amberOffs >= MAX_CHANNELS)
				throw new IllegalArgumentException("Invalid amber offset: " + amberOffs);
			this.amberOffs = amberOffs;
		}

		/**
		 * Checks that offset is valid and sets it.
		 * 
		 * @param whiteOffs new white offset
		 */
		public void setWhiteOffs(int whiteOffs) {
			if (whiteOffs < 0 || whiteOffs >= MAX_CHANNELS)
				throw new IllegalArgumentException("Invalid white offset: " + whiteOffs);
			this.whiteOffs = whiteOffs;
		}

		/**
		 * Checks that offset is valid and sets it.
		 * 
		 * @param strobeOffs new strobe offset
		 */
		public void setStrobeOffs(int strobeOffs) {
			if (strobeOffs < 0 || strobeOffs >= MAX_CHANNELS)
				throw new IllegalArgumentException("Invalid strobe offset: " + strobeOffs);
			this.strobeOffs = strobeOffs;
		}

		/**
		 * Checks that offset is valid and sets it.
		 * 
		 * @param zoomOffs new zoom offset
		 */
		public void setZoomOffs(int zoomOffs) {
			if (zoomOffs < 0 || zoomOffs >= MAX_CHANNELS)
				throw new IllegalArgumentException("Invalid zoom offset: " + zoomOffs);
			this.zoomOffs = zoomOffs;
		}

		/**
		 * Checks that offset is valid and sets it.
		 * 
		 * @param panOffs new pan offset
		 */
		public void setPanOffs(int panOffs) {
			if (panOffs < 0 || panOffs >= MAX_CHANNELS)
				throw new IllegalArgumentException("Invalid pan offset: " + panOffs);
			this.panOffs = panOffs;
		}

		/**
		 * Checks that offset is valid and sets it.
		 * 
		 * @param panFineOffs new panFine offset
		 */
		public void setPanFineOffs(int panFineOffs) {
			if (panFineOffs < 0 || panFineOffs >= MAX_CHANNELS)
				throw new IllegalArgumentException("Invalid panFine offset: " + panFineOffs);
			this.panFineOffs = panFineOffs;
		}

		/**
		 * Checks that offset is valid and sets it.
		 * 
		 * @param tiltOffs new tilt offset
		 */
		public void setTiltOffs(int tiltOffs) {
			if (tiltOffs < 0 || tiltOffs >= MAX_CHANNELS)
				throw new IllegalArgumentException("Invalid tilt offset: " + tiltOffs);
			this.tiltOffs = tiltOffs;
		}

		/**
		 * Checks that offset is valid and sets it.
		 * 
		 * @param tiltFineOffs new tiltFine offset
		 */
		public void setTiltFineOffs(int tiltFineOffs) {
			if (tiltFineOffs < 0 || tiltFineOffs >= MAX_CHANNELS)
				throw new IllegalArgumentException("Invalid tiltFine offset: " + tiltFineOffs);
			this.tiltFineOffs = tiltFineOffs;
		}
	}
	
	/**
	 * Offset settings for this lighting fixture
	 */
	private LightingProfileChannelOffsets offsets;

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
	public LightingProfile(DMXDriver dmx, String name, int address, LightingProfileChannelOffsets channelOffsets) {
		if (name == null || name == "")
			throw new IllegalArgumentException("empty name not allowed");
		if (address < 1)
			throw new IllegalArgumentException("address must be at least 1");
		if (channelOffsets.numChannels() < 1)
			throw new IllegalArgumentException("must have at least one channel");
		if (channelOffsets.numChannels() + address > 512)
			throw new IndexOutOfBoundsException("fixture tries to put channel outside of 512 bytes");
		
		this.name = name;
		this.address = address;
		dmxVals = new int[channelOffsets.numChannels()];
		offsets = channelOffsets;
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
		return offsets.numChannels();
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
