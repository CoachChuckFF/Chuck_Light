package chuck;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

import chuck.defines.*;
import chuck.drivers.DMXDriver;

/**
 * Chuck Lighting Profile Manager Class Contains functions to keep track of
 * current profiles and to add profiles to set.
 * 
 * @author Christian Krueger
 */
public class ProfileManager {

	private static final String DEFAULT_SET = "default.set";
	
	Path profile;
	
	/**
	 * driver to pass into fixtures for updating dmx values
	 */
	private DMXDriver driver;
	
	/**
	 * Lighting profile set. Thread safe for updating.
	 */
	private CopyOnWriteArrayList<LightingProfile> set;
	
	/**
	 * Constructor. Create an empty profile set.
	 * 
	 * @throws IOException if unable to create default directories
	 */
	public ProfileManager(DMXDriver dmx) throws IOException {
		driver = dmx;
		set = new CopyOnWriteArrayList<LightingProfile>();
		profile = Paths.get(Filepaths.SET_DIR, DEFAULT_SET);
		Files.createDirectories(profile.getParent());
		
		LightingProfile defaultLight = new LightingProfile(dmx, "test", 1, 11);
		defaultLight.setDimmer(0);
		defaultLight.setRed(1);
		defaultLight.setGreen(2);
		defaultLight.setBlue(3);
		defaultLight.setDefaultColorOffest();
		
		LightingProfile defaultLight1 = new LightingProfile(dmx, "test1", 12, 11);
		defaultLight1.setDimmer(0);
		defaultLight1.setRed(1);
		defaultLight1.setGreen(2);
		defaultLight1.setBlue(3);
		defaultLight1.setDefaultColorOffest();
		
		LightingProfile defaultLight2 = new LightingProfile(dmx, "test2", 24, 11);
		defaultLight2.setDimmer(0);
		defaultLight2.setRed(1);
		defaultLight2.setGreen(2);
		defaultLight2.setBlue(3);
		defaultLight2.setDefaultColorOffest();
		
		set.add(defaultLight);
		set.add(defaultLight1);
		set.add(defaultLight2);
	}

	/**
	 * reads from set file and populates the set arraylist
	 * 
	 * @param filepath
	 *            "set file" to load into manager
	 * @throws IOException
	 *             if unable to read/parse file
	 */
	public void parseSetFile(Path filepath) throws IOException {
		String line = "";
		String cvsSplitBy = ",";
		LightingProfile light;
		set = new CopyOnWriteArrayList<LightingProfile>();

		try (BufferedReader br = Files.newBufferedReader(filepath)) {
			while ((line = br.readLine()) != null) {
				// use comma as separator
				String[] lightLine = line.split(cvsSplitBy);
				light = new LightingProfile(driver, lightLine[0], Integer.parseInt(lightLine[1]),
						Integer.parseInt(lightLine[2]));
				light.setDimmer(Integer.parseInt(lightLine[3]));
				light.setRed(Integer.parseInt(lightLine[4]));
				light.setGreen(Integer.parseInt(lightLine[5]));
				light.setBlue(Integer.parseInt(lightLine[6]));
				light.setAmber(Integer.parseInt(lightLine[7]));
				light.setWhite(Integer.parseInt(lightLine[8]));
				light.setStrobe(Integer.parseInt(lightLine[9]));
				light.setZoom(Integer.parseInt(lightLine[10]));
				light.setPan(Integer.parseInt(lightLine[11]));
				light.setPanFine(Integer.parseInt(lightLine[12]));
				light.setTilt(Integer.parseInt(lightLine[13]));
				light.setTiltFine(Integer.parseInt(lightLine[14]));

				light.setDefaultColorOffest();
				
				set.add(light);
			}
		} catch (IOException e) {
			// error parsing file, ditch the unfinished arraylist and pass the exception
			set = null;
			throw e;
		}

		// sort via whos address comes first
		Collections.sort(set);
	}

	/**
	 * writes current set to a set file
	 * 
	 * @param filepath
	 *            destination file
	 * @throws IOException
	 *             if unable to write
	 */
	public void writeSetFile(Path filepath) throws IOException {
		if (Files.isDirectory(filepath))
			throw new IllegalArgumentException("set file cannot be directory");
		Files.deleteIfExists(filepath);
		Files.createFile(filepath);
		try (BufferedWriter writer = Files.newBufferedWriter(filepath)) {
			writer.write(this.toString());
		} catch (IOException e) {
			// failed to write, try to cleanup
			Files.delete(filepath);
			throw e;
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
		LightingProfile light;
		
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
		
		light = new LightingProfile(driver, name, address, channels);

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
		Iterator<LightingProfile> iterator;
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
			System.out.println(choice++ + ". " + iterator.next().getName());
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
			System.out.print("Edit " + set.get(choice).getName() + ">");
			input = reader.readLine();

			if (input.equals("q"))
				return;

			String[] edit = input.split(" ");

			if (edit.length > 1) {
				switch (Integer.parseInt(edit[0])) {
				case 0:
					set.get(choice).setName(edit[1]);
					break;
				case 1:
					set.get(choice).setAddress(Integer.parseInt(edit[1]));
					break;
				case 2:
					set.get(choice).setChannels(Integer.parseInt(edit[1]));
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
		Iterator<LightingProfile> iterator;
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
				System.out.println(choice++ + ". " + iterator.next().getName());
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

		parseSetFile(listOfFiles[Integer.parseInt(input)]);

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

	public int getLightCount() {
		return set.size();
	}
	
	public LightingProfile getLight(int index) {
		return set.get(index);
	}

	public String toString() {
		String csv = "";
		LightingProfile temp;
		Iterator<LightingProfile> iterator = set.iterator();

		while (iterator.hasNext()) {
			temp = iterator.next();
			csv += temp.getCSV() + "\n";

		}
		return csv;
	}

}
