package chuck.lighting;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import sun.awt.image.ToolkitImage;

/**
 * Tests the xy converter by displaying the color wheel from converted values.
 * Adapted from https://stackoverflow.com/a/41904118
 * 
 * @author Joseph Eichenhofer
 *
 */
public class XYConverterTestGUI {
	/**
	 * Range of values expected to be converted in xy plane
	 */
	private static final int RANGE = 4096;

	/**
	 * Open a window containing the color wheel.
	 * 
	 * @param args
	 *            n/a
	 */
	public static void main(String[] args) {
		new XYConverterTestGUI();
	}

	/**
	 * Constructor. Adds this object to the event queue so swing displays it using
	 * the override runnable definition, which creates a test pane containing the
	 * color wheel image.
	 */
	public XYConverterTestGUI() {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					// set look and feel to default
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
						| UnsupportedLookAndFeelException e) {
					// fatal error if unable to set look and feel
					e.printStackTrace();
					System.exit(-1);
				}

				// create the frame to hold colorwheel
				JFrame frame = new JFrame("ColorWheel Test");
				// exit the program when we close the window
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				// add the color wheel panel (defined below) to the window
				frame.add(new ColorWheelTestPane());
				// display the window
				frame.pack();
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
			}
		});
	}

	/**
	 * Private JPanel to display the color wheel.
	 * 
	 * @author Joseph Eichenhofer
	 *
	 */
	@SuppressWarnings("serial")
	private class ColorWheelTestPane extends JPanel {
		private ToolkitImage img;

		public ColorWheelTestPane() {
			// instantiate the converter
			XYConverter converter = new XYConverter();
			// instantiate an image buffer
			BufferedImage b_img = new BufferedImage(RANGE, RANGE, BufferedImage.TYPE_INT_ARGB);

			// convert each of the x-y values and put them in an image to display
			for (int x = 0; x < RANGE; x++) {
				for (int y = 0; y < RANGE; y++) {
					b_img.setRGB(x, y, converter.getColor(x, y).getRGB());
				}
			}
			
			// scale image to fit to screen
			img = (ToolkitImage) b_img.getScaledInstance(1024, 1024, Image.SCALE_SMOOTH);
		}
		
        /* (non-Javadoc)
         * @see javax.swing.JComponent#getPreferredSize()
         */
        @Override
        public Dimension getPreferredSize() {
            return img == null ? new Dimension(200, 200) : new Dimension(img.getWidth(), img.getHeight());
        }
        
        /* (non-Javadoc)
         * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
         */
        @Override
        protected void paintComponent(Graphics g) {
        	// call default paint
            super.paintComponent(g);
            // cast to g2d for showing image
            Graphics2D g2d = (Graphics2D) g.create();
            // draw the colorwheel
            if (img != null) {
                g2d.drawImage(img, 0, 0, this);
            }
            g2d.dispose();
        }
	}
}
