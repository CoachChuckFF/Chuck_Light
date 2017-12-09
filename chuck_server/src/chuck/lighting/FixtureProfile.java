package chuck.lighting;

import java.awt.Color;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import chuck.dmx.DMXDriver;

/**
 * Chuck Lighting Profile Class
 * 
 * Represents a single fixture in the lighting setup.
 *
 * @author Joseph Eichenhofer and Christian Krueger
 */
public class FixtureProfile implements Comparable<FixtureProfile>, Serializable {

	/**
	 * Auto-generated version ID for serialization
	 */
	private static final long serialVersionUID = 6480333543917429300L;

	private static final String[] COLOR_NAMES = { "red", "green", "blue", "amber", "white" };

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
	private transient int[] dmxVals;

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
		if (dmx == null)
			throw new IllegalArgumentException("no null dmxdrivers");
		if (fixtureName == null || fixtureName == "")
			throw new IllegalArgumentException("empty name not allowed");
		// address must be within [1:512], check lower bound
		if (dmxAddress < 1)
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

		// configure default color offset
		defaultColorOffs = -1;
		for (String color : COLOR_NAMES) {
			if (channelMap.containsKey(color)) {
				defaultColorOffs = channelMap.get(color);
				break;
			}
		}

		// instantiate dmx mirror
		dmxVals = new int[channels.length];
	}

	/**
	 * Set the driver reference for this fixture. Used by fixture manager for
	 * deserialization of transient variable.
	 * 
	 * @param driver
	 *            reference to dmx driver
	 */
	protected void setDMXDriver(DMXDriver driver) {
		dmxDriver = driver;
	}

	private void writeObject(java.io.ObjectOutputStream stream) throws IOException {
		stream.writeObject(name);
		stream.writeInt(address);
		stream.writeInt(defaultColorOffs);
		// write number of channels, then name of each channel
		stream.writeInt(channelMap.size());
		// sort channels by value (so we get them in order of channel number)
		List<String> channelNames = channelMap.entrySet().stream().sorted(Map.Entry.comparingByValue())
				.map(Map.Entry::getKey).collect(Collectors.toList());
		// write all strings in order
		for (String channelName : channelNames) {
			stream.writeObject(channelName);
		}
	}

	private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
		// get name, must be string
		try {
			name = (String) stream.readObject();
		} catch (ClassCastException ex) {
			throw new IOException("bad serialized fixture; first object not string");
		}
		// validate name length
		if (name.length() == 0)
			throw new IOException("bad serialized fixture; name string is length zero");
		// get address
		address = stream.readInt();
		// get default color offset
		defaultColorOffs = stream.readInt();
		// get number of channels
		int numChannels = stream.readInt();
		// read each channel string
		channelMap = new HashMap<String, Integer>();
		for (int i = 0; i < numChannels; i++) {
			String s;
			try {
				// read the string object and put it into the channelmap
				s = (String) stream.readObject();
				if (s == null || s.equals(""))
					throw new IOException("read empty channel string for channel #" + i);
				if (channelMap.put(s, i) != null) {
					throw new IOException("channels contains duplicate string: " + s);
				}
			} catch (ClassCastException ex) {
				// throw an error if it's not a string
				throw new IOException("bad serialized fixture; readobject returned non-string for channel #" + i);
			}
		}
		if (defaultColorOffs < 0 || defaultColorOffs >= numChannels) {
			// offset cannot exceed channels
			throw new IOException("read defaultColorOffs as " + defaultColorOffs + " with numChannels " + numChannels);
		}
		// check upper bound of address plus number of channels, can be at most 513
		// (address = 512, channels = 1)
		if (numChannels + address > 513)
			throw new IOException("fixture deserialization tries to put channel outside of 512 bytes");

		// create dmx shadow array
		dmxVals = new int[channelMap.size()];
	}

	/**
	 * Get the name configured for this fixture.
	 * 
	 * @return fixture name
	 */
	public String getFixtureName() {
		return name;
	}

	/**
	 * Set this fixture's name.
	 * 
	 * @param name
	 *            new name for this fixture
	 */
	public void setFixtureName(String name) {
		this.name = name;
	}

	/**
	 * This fixture's currently configured address.
	 * 
	 * @return dmx address
	 */
	public int getAddress() {
		return this.address;
	}

	/**
	 * Set a new address for this fixture. Must not put channels above dmx address
	 * 512.
	 * 
	 * @param address
	 *            new address for this fixture
	 */
	public void setAddress(int address) {
		if (address + channelMap.size() > 513)
			throw new IllegalArgumentException("address puts channels beyond dmx 512 address space");

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
	 * Set this fixture to white, using white channel if available.
	 * 
	 * @param intensity
	 *            dmx value for intensity
	 * @throws IOException
	 *             if unable to write dmx driver
	 * @throws UnsupportedOperationException
	 *             if this fixture does not have white or red, green, and blue
	 *             channels
	 */
	public void setWhite(int intensity) throws IOException {
		if (channelMap.containsKey("white"))
			setChannelManual(channelMap.get("white"), intensity);
		else
			setColor(Color.WHITE);
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
		// make sure rgb addresses are set
		if (!(channelMap.containsKey("red") && channelMap.containsKey("green") && channelMap.containsKey("blue"))) {
			throw new UnsupportedOperationException("cannot set color on fixture without rgb channels");
		}

		int redOffs = channelMap.get("red");
		int greenOffs = channelMap.get("green");
		int blueOffs = channelMap.get("blue");

		dmxVals[redOffs] = color.getRed();
		dmxVals[greenOffs] = color.getGreen();
		dmxVals[blueOffs] = color.getBlue();

		// check for default case for optimal write speed
		if (redOffs == 1 && greenOffs == 2 && blueOffs == 3) {
			// write together for efficiency
			dmxDriver.setDMX(address + redOffs, color.getRed(), color.getGreen(), color.getBlue());
		} else {
			// otherwise just set them individually
			dmxDriver.setDMX(address + redOffs, color.getRed());
			dmxDriver.setDMX(address + greenOffs, color.getGreen());
			dmxDriver.setDMX(address + blueOffs, color.getBlue());
		}
	}

	/**
	 * Sets the dmx dimmer value.
	 * 
	 * @param dimmerVal
	 *            fixture's new dimmer value in dmx (must be within [0:255])
	 * @throws IOException
	 *             if unable to access dmx driver files
	 */
	public void setDimmerValue(int dimmerVal) throws IOException {
		if (dimmerVal < 0 || dimmerVal > 255)
			throw new IllegalArgumentException("dimmer value must be within [0:255]");

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
		if (value < 0 || value > 255)
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

	/**
	 * Updates this fixture's shadow array with values from driver (call after
	 * changing dmx values directly through driver)
	 */
	public void syncLight() {
		System.arraycopy(dmxDriver.getDmx(), address, dmxVals, 0, channelMap.size());
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

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Fixture ");
		sb.append('"').append(name).append('"').append(": ");
		sb.append(getNumChannels()).append(" channels at address ").append(address);
		// get channel names to list
		sb.append(" (");
		List<String> channelNames = channelMap.entrySet().stream().sorted(Map.Entry.comparingByValue())
				.map(Map.Entry::getKey).collect(Collectors.toList());
		for (String name : channelNames) {
			sb.append(name).append(',');
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append(")\n");
		return sb.toString();
	}

	/**
	 * Set all channels to zero.
	 * 
	 * @throws IOException
	 *             if unable to write dmx vals
	 */
	public void clearLight() throws IOException {
		for (int i = 0; i < getNumChannels(); i++) {
			setChannelManual(i, 0);
		}

	}
}
