package chuck.defines;

import java.awt.Color;

public class LightingDefines {

	public static final int MAX_CHASE_DELAY = 10000;
	public static final int MIN_CHASE_DELAY = 30;
	public static final int CHASE_STEP = 10;

	public static final int HIGHLIGHT_VISUAL_DELAY = 30;
	public static final int HIGHLIGHT_DIMMER_STEP = 5;
	public static final int HIGHLIGHT_DIMMER_HIGH_VAL = 255;
	public static final int HIGHLIGHT_DIMMER_LOW_VAL = 25;

	public static final int DMX_STEP = 5;
	
	public static final int PRESET_VISUAL_DELAY = 400;
	public static final int RAINBOW_VISUAL_DELAY = 25;
	public static final int DMX_VISUAL_DELAY = HIGHLIGHT_VISUAL_DELAY;

	public static final String[] EXA_DEFAULT_CHANNELS = {"dimmer", "red", "green", "blue", "amber", "white", "uv", "strobe", "auto", "auto_speed", "color_wheel", "unknown"};
	public static final String[] ZOOM_DEFAULT_CHANNELS = {"dimmer", "red", "green", "blue", "amber", "white", "strobe", "zoom", "preset", "unknown1", "unknown2"};
	public static final String[] SNAKEYE_DEFAULT_CHANNELS = {"pan", "tilt", "infinite_tilt", "pan_tilt_speed", "red", "green", "blue", "white", "strobe", "dimmer", "led_program", "program_speed", "color_macros", "auto_program"};
	
	public static final Color[] PRESETS = {Color.WHITE, Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA, Color.BLACK};
}
