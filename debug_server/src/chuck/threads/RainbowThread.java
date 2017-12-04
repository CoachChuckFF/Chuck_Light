package chuck.threads;

import java.awt.Color;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import chuck.LightingProfile;

/**
 * Thread that takes a lighting profile object and changes that fixture's color
 * to indicate the color selection mode.
 * 
 * @author Joseph Eichenhofer
 *
 */
public class RainbowThread extends Thread {

	private static final int RAINBOW_RESOLUTION = 100;

	private LightingProfile fixture;
	private List<Color> rainbow;
	private int[] prevVals;
	private boolean running = true;

	/**
	 * Constructor. Prepares for rotating colors on fixture. <br />
	 * Creates rainbow based on logic found https://stackoverflow.com/a/22973823
	 * 
	 * @param fixture
	 *            fixture to highlight
	 */
	public RainbowThread(LightingProfile fixture) {
		this.fixture = fixture;

		rainbow = new LinkedList<Color>();
		for (int r = 0; r < RAINBOW_RESOLUTION; r++)
			rainbow.add(new Color(r * 255 / RAINBOW_RESOLUTION, 255, 0));
		for (int g = RAINBOW_RESOLUTION; g > 0; g--)
			rainbow.add(new Color(255, g * 255 / RAINBOW_RESOLUTION, 0));
		for (int b = 0; b < RAINBOW_RESOLUTION; b++)
			rainbow.add(new Color(255, 0, b * 255 / RAINBOW_RESOLUTION));
		for (int r = RAINBOW_RESOLUTION; r > 0; r--)
			rainbow.add(new Color(r * 255 / RAINBOW_RESOLUTION, 0, 255));
		for (int g = 0; g < RAINBOW_RESOLUTION; g++)
			rainbow.add(new Color(0, g * 255 / RAINBOW_RESOLUTION, 255));
		for (int b = RAINBOW_RESOLUTION; b > 0; b--)
			rainbow.add(new Color(0, 255, b * 255 / RAINBOW_RESOLUTION));
		rainbow.add(new Color(0, 255, 0));
	}

	/**
	 * Starts rotating the colors on the fixture.
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		prevVals = fixture.getDMXVals();
		Iterator<Color> colors = rainbow.iterator();

		while (running) {
			if (!colors.hasNext())
				colors = rainbow.iterator();
			try {
				fixture.setColor(colors.next());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(-1);
			}
		}

		try {
			for (int i = 0; i < prevVals.length; i++) {
				fixture.setChannelManual(i, prevVals[i]);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}
	}

}
