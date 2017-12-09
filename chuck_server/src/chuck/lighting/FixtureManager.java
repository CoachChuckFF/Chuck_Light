package chuck.lighting;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

import chuck.defines.*;
import chuck.dmx.DMXDriver;

/**
 * Chuck Lighting Profile Manager Class Contains functions to keep track of
 * current profiles and to add profiles to set.
 * 
 * Manager class for keeping track of a set of fixtures (current setup).
 * 
 * @author Christian Krueger
 */
public class FixtureManager {

	/**
	 * driver to pass into fixtures for updating dmx values
	 */
	private DMXDriver driver;
	
	/**
	 * path to directory containing set files
	 */
	private Path set_dir;

	/**
	 * Lighting profile set. Thread safe for updating.
	 */
	private CopyOnWriteArrayList<FixtureProfile> set;

	/**
	 * Constructor. Create an empty profile set.
	 * 
	 * @throws IOException
	 *             if unable to create default directories
	 */
	public FixtureManager(DMXDriver dmx) throws IOException {
		// set our driver reference
		driver = dmx;
		// get the path to our set directory
		set_dir = Paths.get(Filepaths.SET_DIR).toRealPath();
		// create the set directory if it doesn't exist
		Files.createDirectories(set_dir);
		// create the empty set
		set = new CopyOnWriteArrayList<FixtureProfile>();

		set.add(new FixtureProfile(dmx, "colorize zoom", 1, LightingDefines.ZOOM_DEFAULT_CHANNELS));
		set.add(new FixtureProfile(dmx, "snake-eye mini", 12, LightingDefines.SNAKEYE_DEFAULT_CHANNELS));
		set.add(new FixtureProfile(dmx, "colorize exa", 26, LightingDefines.EXA_DEFAULT_CHANNELS));
	}

	/**
	 * Constructor. Try to open a file for creating this fixture set.
	 * 
	 * @param dmx
	 *            reference to dmx driver
	 * @param filename
	 *            name of set file that was created by this application (for opening)
	 */
	public FixtureManager(DMXDriver dmx, String filename) throws IOException {
		// call basic constructor to instantiate members
		this(dmx);
		// don't allow sneaky filenames
		Path setFile = set_dir.resolve(filename).toRealPath();
		if (!setFile.getParent().equals(set_dir))
			throw new IllegalArgumentException("path traversal detected");

		// get an object stream for reading file
		ObjectInputStream ins = new ObjectInputStream(Files.newInputStream(setFile));
		// get the number of fixtures in the set
		int numFixtures = ins.readInt();
		// read and add each fixture to set
		FixtureProfile currFixture;
		for (int i = 0 ; i < numFixtures ; i++) {
			try {
				// read the fixture object
				currFixture = (FixtureProfile) ins.readObject();
				currFixture.setDMXDriver(dmx);
				set.add(currFixture);
			} catch (ClassCastException | ClassNotFoundException ex) {
				// error reading fixtures from file
				throw new IOException("read non-fixture object from file (" + ex.getMessage() + ")");
			}
		}
		// sort the fixture set (by address)
		Collections.sort(set);
	}

	/**
	 * Save this fixture manager's set to a file in the set directory.
	 * 
	 * @param filename
	 * 	name of set file
	 * @throws IOException
	 *  if unable to perform the operation
	 */
	public void saveSetFile(String filename) throws IOException {
		if (!filename.endsWith(".set")) {
			// only save to .set files
			filename = filename + ".set";
		}
		// don't allow sneaky traversals
		Path setFile = set_dir.resolve(filename).toRealPath();
		if (!setFile.getParent().equals(set_dir)) {
			throw new IllegalArgumentException("path traversal detected");
		}
		
		// delete the old version
		Files.deleteIfExists(setFile);
		// create a new empty file
		Files.createFile(setFile);
		// get the output stream
		ObjectOutputStream stream = new ObjectOutputStream(Files.newOutputStream(setFile));
		// write the number of fixtures
		stream.writeInt(set.size());
		// write each fixture
		for (FixtureProfile f : set) {
			stream.writeObject(f);
		}
		
	}

	/**
	 * Prompt user for command line inputs to edit profiles.
	 * 
	 * @param reader
	 *            Buffered reader from commandline input source
	 */
	public void managerCLI(BufferedReader reader) {
		boolean quit = false;
		String input = null;
		String[] splitInput = null;

		printMainHelp();

		do {
			System.out.print("Set Manager>");
			try {
				input = reader.readLine().toLowerCase();
				splitInput = input.split(" ");
			} catch (IOException ex) {
				ex.printStackTrace();
				System.exit(-1);
			}
			if (splitInput[0].startsWith("q")) {
				quit = true;
			} else if (splitInput[0].startsWith("a")) {
				try {
					addProfileToSetCLI(reader);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (splitInput[0].startsWith("e")) {
				try {
					editProfileInSetCLI(reader);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (splitInput[0].startsWith("d")) {
				try {
					deleteProfileInSetCLI(reader);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (splitInput[0].startsWith("s")) {
				try {
					saveSetCLI(reader);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else if (splitInput[0].startsWith("l")) {
				try {
					loadSetCLI(reader);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else if (input.startsWith("p")) {
				System.out.println(this.toString());
			} else if (input.startsWith("h")) {
				printMainHelp();
			} else {
				System.out.println("Input Error");
				printMainHelp();
			}
		} while (!quit);
	}

	private void addProfileToSetCLI(BufferedReader reader) throws IOException {
		String input;
		FixtureProfile light;

		String name;
		int address;
		int channels;

		System.out.println("Enter light information");
		System.out.println("To cancel enter 'q'");
		System.out.print("Light Name: ");
		input = reader.readLine();

		if (input.equals("q") || input.equals(""))
			return;

		name = input;

		System.out.print("Light Address: ");
		input = reader.readLine();

		if (input.equals("q") || input.equals(""))
			return;

		address = Integer.parseInt(input);

		System.out.print("Light Channels: ");
		input = reader.readLine();

		if (input.equals("q") || input.equals(""))
			return;

		channels = Integer.parseInt(input);

		light = new FixtureProfile(driver, name, address, channels);

		System.out.println("Enter in the channel number for the following functions");
		System.out.print("Dimmer: ");
		input = reader.readLine();

		if (input.equals("q"))
			return;

		if (input.equals(""))
			light.setDimmer(-1);
		else
			light.setDimmer(Integer.parseInt(input));

		System.out.print("Red: ");
		input = reader.readLine();

		if (input.equals("q"))
			return;

		if (input.equals(""))
			light.setRed(-1);
		else
			light.setRed(Integer.parseInt(input));

		System.out.print("Green: ");
		input = reader.readLine();

		if (input.equals("q"))
			return;

		if (input.equals(""))
			light.setGreen(-1);
		else
			light.setGreen(Integer.parseInt(input));

		System.out.print("Blue: ");
		input = reader.readLine();

		if (input.equals("q"))
			return;

		if (input.equals(""))
			light.setBlue(-1);
		else
			light.setBlue(Integer.parseInt(input));

		System.out.print("Amber: ");
		input = reader.readLine();

		if (input.equals("q"))
			return;

		if (input.equals(""))
			light.setAmber(-1);
		else
			light.setAmber(Integer.parseInt(input));

		System.out.print("White: ");
		input = reader.readLine();

		if (input.equals("q"))
			return;

		if (input.equals(""))
			light.setWhite(-1);
		else
			light.setWhite(Integer.parseInt(input));

		System.out.print("Strobe: ");
		input = reader.readLine();

		if (input.equals("q"))
			return;

		if (input.equals(""))
			light.setStrobe(-1);
		else
			light.setStrobe(Integer.parseInt(input));

		System.out.print("Zoom: ");
		input = reader.readLine();

		if (input.equals("q"))
			return;

		if (input.equals(""))
			light.setZoom(-1);
		else
			light.setZoom(Integer.parseInt(input));

		System.out.print("Pan: ");
		input = reader.readLine();

		if (input.equals("q"))
			return;

		if (input.equals(""))
			light.setPan(-1);
		else
			light.setPan(Integer.parseInt(input));

		System.out.print("Pan Fine: ");
		input = reader.readLine();

		if (input.equals("q"))
			return;

		if (input.equals(""))
			light.setPanFine(-1);
		else
			light.setPanFine(Integer.parseInt(input));

		System.out.print("Tilt: ");
		input = reader.readLine();

		if (input.equals("q"))
			return;

		if (input.equals(""))
			light.setTilt(-1);
		else
			light.setTilt(Integer.parseInt(input));

		System.out.print("Tilt Fine: ");
		input = reader.readLine();

		if (input.equals("q"))
			return;

		if (input.equals(""))
			light.setTiltFine(0);
		else
			light.setTiltFine(Integer.parseInt(input));

		light.setDefaultColorOffest();

		set.add(light);
		Collections.sort(set);
	}

	private void editProfileInSetCLI(BufferedReader reader) throws IOException {
		Iterator<FixtureProfile> iterator;
		String input;
		int choice = 0;

		if (set.size() == 0) {
			System.out.println("Nothing to edit");
			return;
		}

		System.out.println("Choose Light to Edit 0-" + (set.size() - 1));
		System.out.println("'q' to quit");

		iterator = set.iterator();

		// while loop
		while (iterator.hasNext()) {
			System.out.println(choice++ + ". " + iterator.next().getFixtureName());
		}

		System.out.print("Edit> ");

		input = reader.readLine();

		if (input.equals("q") || input.equals(""))
			return;

		choice = Integer.parseInt(input);
		if (choice > set.size() || choice < 0)
			return;

		System.out.println("Enter choice then value");
		System.out.println("Example to change red channel to 3 enter + '3 3'");
		System.out.println("'q' to quit");
		do {
			System.out.println(set.get(choice).toString());
			System.out.print("Edit " + set.get(choice).getFixtureName() + ">");
			input = reader.readLine();

			if (input.equals("q"))
				return;

			String[] edit = input.split(" ");

			if (edit.length > 1) {
				switch (Integer.parseInt(edit[0])) {
				case 0:
					set.get(choice).setFixtureName(edit[1]);
					break;
				case 1:
					set.get(choice).setAddress(Integer.parseInt(edit[1]));
					break;
				case 2:
					set.get(choice).setNumChannels(Integer.parseInt(edit[1]));
					break;
				case 3:
					set.get(choice).setDimmer(Integer.parseInt(edit[1]));
					break;
				case 4:
					set.get(choice).setRed(Integer.parseInt(edit[1]));
					break;
				case 5:
					set.get(choice).setGreen(Integer.parseInt(edit[1]));
					break;
				case 6:
					set.get(choice).setBlue(Integer.parseInt(edit[1]));
					break;
				case 7:
					set.get(choice).setAmber(Integer.parseInt(edit[1]));
					break;
				case 8:
					set.get(choice).setWhite(Integer.parseInt(edit[1]));
					break;
				case 9:
					set.get(choice).setStrobe(Integer.parseInt(edit[1]));
					break;
				case 10:
					set.get(choice).setZoom(Integer.parseInt(edit[1]));
					break;
				case 11:
					set.get(choice).setPan(Integer.parseInt(edit[1]));
					break;
				case 12:
					set.get(choice).setPanFine(Integer.parseInt(edit[1]));
					break;
				case 13:
					set.get(choice).setTilt(Integer.parseInt(edit[1]));
					break;
				case 14:
					set.get(choice).setTiltFine(Integer.parseInt(edit[1]));
					break;
				default:
					System.out.println("Error");
					break;
				}
			} else
				System.out.println("Error");

		} while (true);

	}

	private void deleteProfileInSetCLI(BufferedReader reader) throws IOException {
		Iterator<FixtureProfile> iterator;
		String input;
		int choice = 0;

		if (set.size() == 0) {
			System.out.println("Nothing to Delete");
			return;
		}

		System.out.println("Choose Light to Delete 0-" + (set.size() - 1));
		System.out.println("'q' to quit");

		iterator = set.iterator();

		do {
			iterator = set.iterator();
			choice = 0;
			while (iterator.hasNext()) {
				System.out.println(choice++ + ". " + iterator.next().getFixtureName());
			}

			System.out.print("Delete> ");

			input = reader.readLine();

			if (input.equals("q") || input.equals(""))
				return;

			choice = Integer.parseInt(input);
			if (choice > set.size() || choice < 0)
				return;

			set.remove(choice);

			if (set.size() == 0)
				return;

		} while (true);

	}

	private void saveSetCLI(BufferedReader reader) throws IOException {
		String input = "";

		System.out.println("Current Set Files");
		System.out.println("'q' to quit");

		Files.list(Paths.get(Filepaths.SET_DIR)).forEach(file -> {
			if (Files.isDirectory(file))
				System.out.println("Directory " + file.getFileName());
			else
				System.out.println(file.getFileName());
		});

		System.out.print("Enter filename: ");

		input = reader.readLine();

		if (input.equals("q") || input.equals(""))
			return;

		writeSetFile(Paths.get(Filepaths.SET_DIR).resolve(input));

	}

	private void loadSetCLI(BufferedReader reader) throws IOException {
		Path[] listOfFiles = Files.list(Paths.get(Filepaths.SET_DIR)).toArray(Path[]::new);
		String input = "";

		System.out.println("Current Set Files");
		System.out.println("Enter Number to Load");
		System.out.println("'q' to quit");

		for (int i = 0; i < listOfFiles.length; i++) {
			if (Files.isRegularFile(listOfFiles[i])) {
				System.out.println(i + ". " + listOfFiles[i].getFileName());
			} else if (Files.isDirectory(listOfFiles[i])) {
				System.out.println("Directory " + listOfFiles[i].getFileName());
			}
		}

		System.out.print("Load> ");

		input = reader.readLine();

		if (input.equals("q") || input.equals(""))

			return;

		if (Integer.parseInt(input) > listOfFiles.length - 1 || Integer.parseInt(input) < 0) {
			System.out.println("Bad Selection");
			return;
		}

		openSetFile(listOfFiles[Integer.parseInt(input)]);

	}

	private static void printMainHelp() {
		System.out.println("Set Manager Commands:");
		System.out.println("\ta: add profile");
		System.out.println("\te: edit profile");
		System.out.println("\td: delete profile");
		System.out.println("\ts: save set");
		System.out.println("\tl: load set");
		System.out.println("\tp: print current set");
		System.out.println("\th: help");
		System.out.println("\tq: quit/back");
	}

	/**
	 * Get the number of fixtures held in this manager's set.
	 * 
	 * @return
	 * 	number of lights in current set
	 */
	public int getLightCount() {
		return set.size();
	}

	/**
	 * Get a reference to one of the lights in this set.
	 * 
	 * @param index
	 * 	zero-based index into address-sorted list of fixtures
	 * @return
	 * 	fixture profile representing light at that index
	 */
	public FixtureProfile getLight(int index) {
		return set.get(index);
	}
}
