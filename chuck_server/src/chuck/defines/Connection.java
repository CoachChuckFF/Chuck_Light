package chuck.defines;

/**
 * Byte definitions for packet ids and other communication packet binary metadata
 * 
 * @author Joseph Eichenhofer and Christian Krueger
 *
 */
public class Connection {
	public static final int DMX_PORT = 6454;

	public static final byte[] ID = { 'J', 'E', 0x10, 'M', 'K', 0x03, 'C', 'K' };

	public static final byte DATA_PACKET_ID = (byte) 0x33;
	public static final byte COMMAND_PACKET_ID = (byte) 0x36;
	public static final byte POLL_PACKET_ID = (byte) 0x87;
	public static final byte POLL_REPLY_PACKET_ID = (byte) 0x1E;
	
	public static final int HEARTBEAT_INTERVAL = 3000;

	public static final byte HARD_RESET_COMMAND = (byte) 0x99;
	public static final byte SOFT_RESET_COMMAND = (byte) 0x39;

	public static final byte USER_ACTION_DATA = (byte) 0x13;
	public static final byte JOYSTIC_DATA = (byte) 0x23;
	public static final byte GYRO_DATA = (byte) 0x33;
	
	public static final byte UP = (byte) 0x11;
	public static final byte DOWN = (byte) 0x12;
	public static final byte LEFT = (byte) 0x13;
	public static final byte RIGHT = (byte) 0x14;
	
	public static final byte PS2 = (byte) 0x01;
	public static final byte PS2_LONG = (byte) 0x41;
	public static final byte B1 = (byte) 0x02;
	public static final byte B2 = (byte) 0x03;
	public static final byte B12 = (byte) 0x04;
	
	public static final byte KONAMI = (byte) 0x31;
	public static final byte REV_KONAMI = (byte) 0x32;
	public static final byte SEMI_KONAMI = (byte) 0x33;
}
