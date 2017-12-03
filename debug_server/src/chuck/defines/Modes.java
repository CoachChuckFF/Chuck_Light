package chuck.defines;

/**
 * Defines for current executing state of the main application.
 * 
 * @author Christian Krueger
 *
 */
public class Modes {
	public static final byte CHASE = 0;
	public static final byte IDLE = 1;
	public static final byte LIGHT_SELECTION = 2;
	public static final byte CONTROL_SELECTION = 3;
	public static final byte COLOR_WHEEL = 4;
	public static final byte DMX = 5;
	public static final byte PRESET = 6;
	public static final byte PARTY = 7;
	public static final byte SCARY = 8;
	
	public static final int MAX_CHASE_DELAY = 3000;
	public static final int MIN_CHASE_DELAY = 100;
}
