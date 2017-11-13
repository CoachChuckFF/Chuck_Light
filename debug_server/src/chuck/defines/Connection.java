package chuck.defines;

public class Connection {
	public static final int DMX_PORT = 6454;
	
	public static final byte[] ID = {'J', 'E', 0x10, 'M', 'K', 0x03, 'C', 'K'};
	
	public static final byte DATA_PACKET_ID = (byte) 0x33;
	public static final byte COMMAND_PACKET_ID = (byte) 0x36;
	public static final byte POLL_PACKET_ID = (byte) 0x87;
	public static final byte POLL_REPLY_PACKET_ID = (byte) 0x1E;
	
	public static final byte HARD_RESET_COMMAND = (byte) 0x99;
	public static final byte SOFT_RESET_COMMAND = (byte) 0x39;
	
	public static final byte USER_ACTION_DATA = (byte) 0x13;
	public static final byte JOYSTIC_DATA = (byte) 0x23;
	public static final byte GYRO_DATA = (byte) 0x33;
}
