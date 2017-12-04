package chuck.drivers;

import java.awt.EventQueue;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

/**
 * Dummy driver for testing on non-dev machine. Opens window with up-to-date DMX values.
 * 
 * @author Joseph Eichenhofer
 *
 */
public class DMXDummy implements DMXDriver {
	
	
	private ObservableList<Integer> dmxVals = FXCollections.observableArrayList(Collections.nCopies(513, 0));
	private JList<Integer> listView;
	
	/**
	 * Constructor. Creates shadow array and hooks with gui window
	 * 
	 * @throws IOException never
	 */
	public DMXDummy() throws IOException {
		// make swing invoke the gui thread
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

				// create the frame to hold the listview
				JFrame frame = new JFrame("DMX Values");
				// don't want to kill server
				frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
				// add the listview to the frame
				listView = new JList<Integer>(dmxVals.subList(1, 513).toArray(new Integer[512]));
				listView.setLayoutOrientation(JList.HORIZONTAL_WRAP);
				listView.setVisibleRowCount(32);
				listView.setFixedCellWidth(25);
				listView.setBorder(new EmptyBorder(15, 15, 15, 15));
				((DefaultListCellRenderer) listView.getCellRenderer()).setHorizontalAlignment(SwingConstants.RIGHT);
				frame.add(listView);
				// display the window
				frame.pack();
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
			}
		});
		// add a listener to the non-fx thread list so that updates are reflected on fx thread list
		dmxVals.addListener(new ListChangeListener<Integer>() {
			// on change, add a new runnable to the fx thread that will update the fx thread list
			@Override
			public void onChanged(Change<? extends Integer> change) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						while (change.next()) {
							if (change.wasReplaced()) {
								for (int i = change.getFrom() ; i < change.getTo() ; i++) {
									listView.setListData(dmxVals.subList(1, 513).toArray(new Integer[512]));
								}
							}
						}
					}
					
				});
			}
			
		});
	}
	
	@Override
	public int getDMX(int address) {
		if (address <= 0 || address > 512)
			throw new IllegalArgumentException("DMX address must be within [1:512]");
		return dmxVals.get(address).intValue();
	}

	@Override
	public int[] getDmx() {
		return dmxVals.stream().mapToInt(Integer::intValue).toArray();
	}

	@Override
	public void setDMX(int address, int... values) throws IOException {
		if (address <= 0 || address > 512)
			throw new IllegalArgumentException("DMX address must be within [1:512]");
		if (values.length == 0)
			throw new IllegalArgumentException("Must supply at least one value.");
		if (values.length > 4)
			throw new IllegalArgumentException("Must supply at most four values.");
		if (Arrays.stream(values).anyMatch(val -> val < 0 || val > 255))
			throw new IllegalArgumentException("DMX values must be within [0:255]");

		// package four byte-length int values into one int
		// bit_offset is the number of bits to shift each value into data_val
		// curr_val holds the truncated byte from each input value
		int curr_val;
		for (int i = 0; i < values.length; i++) {
			curr_val = values[i] & 0xff;
			dmxVals.set(address + i, curr_val);
		}
	}

	@Override
	public void setDMX(int[] values) throws IOException {
		if (values.length != 513) {
			throw new IllegalArgumentException("values array must be 513 elements long");
		}

		for (int i = 1; i < 512; i = i + 4) {
			setDMX(i, values[i], values[i + 1], values[i + 2], values[i + 3]);
		}
	}

	@Override
	public void clearDMX() throws IOException {
		for (int i = 1; i < 512; i = i + 4) {
			setDMX(i, 0, 0, 0, 0);
		}
	}
}
