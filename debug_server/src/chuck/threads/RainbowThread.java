package chuck.threads;

import java.awt.Color;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import chuck.LightingProfile;
import chuck.defines.LightingDefines;

/**
 * Thread that takes a lighting profile object and changes that fixture's color
 * to indicate the color selection mode.
 *
 * @author Joseph Eichenhofer
 *
 */
public class RainbowThread extends Thread {

	private static final int RAINBOW_RESOLUTION = 50;

	private List<LightingProfile> fixtures;
	private List<Color> rainbow;
	private static boolean running = false;

	/**
	 * Constructor. Prepares for rotating colors on fixture. <br />
	 * Creates rainbow based on logic found https://stackoverflow.com/a/22973823
	 *
	 * @param fixture
	 *            fixture to highlight
	 */
	public RainbowThread(List<LightingProfile> fixtures) {
		this.fixtures = fixtures;

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
		Iterator<Color> colors = rainbow.iterator();
		running = true;

		while (running) {
			if (!colors.hasNext())
				colors = rainbow.iterator();

			Color currColor = colors.next();
			fixtures.forEach(fixture -> {
				try {
					fixture.setColor(currColor);
					fixture.setDimmerValue(255);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});

			try {
				Thread.sleep(LightingDefines.RAINBOW_VISUAL_DELAY);
			} catch (InterruptedException e) {
				// probably interrupted by main thread, just continue
				continue;
			}
		}
	}

	/**
	 * Stop the rainbow thread, resets fixture to the previous values, and joins the
	 * thread. Should be called from the thread that started the rainbow thread.
	 */
	public void kill() throws InterruptedException {
		running = false;
		this.interrupt();
		this.join();
	}
}
