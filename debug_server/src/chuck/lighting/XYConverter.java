package chuck.lighting;

import java.awt.Color;

/**
 * Class to quickly convert xy-values to rgb values. Uses rgb arrays to lookup
 * values based on xy-index into arrays. Our analog stick has a range of 0-4095,
 * so the arrays are one fourth of that range at 1024*1024.
 * 
 * @author Joseph Eichenhofer
 *
 */
public class XYConverter {
	/**
	 * Range of values that are expected to be converted (after being scaled)
	 */
	public static final int VALUE_RANGE = 4096;
	/**
	 * Divisor for calculating color wheel size from value range
	 */
	private static final int SCALE = 4;
	/**
	 * HSV value/
	 */
	private static final float HSV_VALUE = 1.0f;

	private static final int RADIUS = (VALUE_RANGE / SCALE) / 2;
	private static final int WHITE_RADIUS = RADIUS / 16;

	private int red[][] = new int[2*RADIUS][2*RADIUS];
	private int green[][] = new int[2*RADIUS][2*RADIUS];
	private int blue[][] = new int[2*RADIUS][2*RADIUS];

	/**
	 * Generate the rgb lookup arrays. Adapted from
	 * https://medium.com/@bantic/hand-coding-a-color-wheel-with-canvas-78256c9d7d43
	 */
	public XYConverter() {
		Color currColor;
		float distance, angle;
		int adjustedX, adjustedY;
		float hue, saturation;

		for (int x = -RADIUS; x < RADIUS; x++) {
			for (int y = -RADIUS; y < RADIUS; y++) {
				// calculate array indices from zero-centered xy
				adjustedX = x + RADIUS;
				adjustedY = y + RADIUS;

				// calculate distance from center
				distance = (float) Math.sqrt(x * x + y * y);
				// calculate degree angle off zero
				angle = (float) (Math.atan2(x, y) * (180 / Math.PI));

				// ignore outside of radius (leave black)
				if (distance > RADIUS) {
					continue;
				}

				// create a pocket of white in center
				if (distance < WHITE_RADIUS) {
					red[adjustedX][adjustedY] = 255;
					green[adjustedX][adjustedY] = 255;
					blue[adjustedX][adjustedY] = 255;
					continue;
				}

				// hue needs to be a fraction of 360 for the hsb method
				hue = angle / 360;
				// saturation is the distance from center as a fraction of radius
				saturation = distance / RADIUS;
				// create a color from hue, saturation, value (needs floats)
				currColor = Color.getHSBColor(hue, saturation, HSV_VALUE);

				// set values in array
				red[adjustedX][adjustedY] = currColor.getRed();
				green[adjustedX][adjustedY] = currColor.getGreen();
				blue[adjustedX][adjustedY] = currColor.getBlue();
			}
		}
	}

	/**
	 * Gets an rgb color from the xy coordinates within the color wheel.
	 * 
	 * @param x
	 *            x value of location in color wheel
	 * @param y
	 *            y value of location in color wheel
	 * @return color (containing RGB values) of the xy location in the color wheel
	 */
	public Color getColor(int x, int y) {
		if (x >= VALUE_RANGE || y >= VALUE_RANGE)
			throw new IllegalArgumentException("(x,y) values must be within [0:" + (VALUE_RANGE - 1) + "]");

		return new Color(red[x / SCALE][y / SCALE], green[x / SCALE][y / SCALE], blue[x / SCALE][y / SCALE]);
	}
}
