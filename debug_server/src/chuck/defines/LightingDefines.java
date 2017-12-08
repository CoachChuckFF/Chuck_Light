package chuck.defines;

import java.awt.Color;

public class LightingDefines {

	public static final int MAX_CHASE_DELAY = 10000;
	public static final int MIN_CHASE_DELAY = 30;
	public static final int CHASE_STEP = 10;

	public static final int PRESET_VISUAL_DELAY = 400;
	public static final int RAINBOW_VISUAL_DELAY = 25;

	public static final int HIGHLIGHT_VISUAL_DELAY = 50;
	public static final int HIGHLIGHT_DIMMER_STEP = 5;
	public static final int HIGHLIGHT_DIMMER_HIGH_VAL = 255;
	public static final int HIGHLIGHT_DIMMER_LOW_VAL = 190;
	
	public static final String[] DEFAULT_CHANNELS = {"dimmer", "red", "green", "blue", "amber", "white", "strobe", "zoom", "pan", "tilt"};

	public static final Color[] PRESETS = {Color.WHITE, Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA, Color.BLACK};
}
