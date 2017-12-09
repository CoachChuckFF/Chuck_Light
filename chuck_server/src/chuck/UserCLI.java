package chuck;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import chuck.defines.Filepaths;
import chuck.dmx.DMXDriver;
import chuck.dmx.DMXDummy;
// import chuck.dmx.DefaultDMX;
import chuck.lighting.FixtureManager;
import chuck.lighting.FixtureProfile;
import chuck.threads.ServerAppThread;

public class UserCLI {

	private DMXDriver dmx;

	private FixtureManager profiles;
	private ServerAppThread app;

	public UserCLI() {
		try {
			// instantiate dmx driver
			dmx = new DMXDummy();
			System.out.println("DMX Driver Initialized");
			profiles = new FixtureManager(dmx, Filepaths.DEFAULT_SET);
			System.out.println("default profile loaded");
		} catch (IOException ex) {
			// fatal error if unable to instantiate driver
			ex.printStackTrace();
			System.exit(-1);
		}
	}

	public static void main(String[] args) {
		UserCLI cli = new UserCLI();
		cli.startCLI();
	}

	public void startCLI() {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		boolean quit = false;
		String input = null;
		String[] splitInput = null;

		app = new ServerAppThread(dmx, profiles);
		app.start();

		printMainHelp();

		do {
			System.out.print("DMX Controller>");
			try {
				input = reader.readLine().toLowerCase();
				splitInput = input.split(" ");
			} catch (IOException ex) {
				ex.printStackTrace();
				System.exit(-1);
			}
			if (splitInput[0].startsWith("q")) {
				app.stopServer();
				try {
					app.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("Goodbye");
				try {
					dmx.clearDMX();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.exit(-1);
				}
				System.exit(0);
			} else if (splitInput[0].startsWith("c")) {
				quit = true;
				System.out.println("CLI stopping...");
				if (app.isServerRunning())
					System.out.println("Server will continue to run");
			} else if (splitInput[0].startsWith("p")) {
				if (app.isServerRunning())
					System.out.println("stop server first");
				else
					managerCLI(reader);

			} else if (splitInput[0].startsWith("z")) {
				System.out.println(profiles.toString());
			} else if (splitInput[0].startsWith("d")) {
				System.out.println("add in DMX veiwer");

			} else if (splitInput[0].startsWith("s")) {
				if (app.isServerRunning()) {
					app.stopServer();
					try {
						app.join();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						System.exit(-1);
					}
					System.out.println("Server Stopped");
				} else {
					app = new ServerAppThread(dmx, profiles);
					app.start();
				}
			} else if (splitInput[0].startsWith("n")) {
				if (app.isServerRunning()) {
					System.out.println("Add network info");
				} else {
					System.out.println("Server needs to be running");
				}

			} else if (input.startsWith("h")) {
				printMainHelp();
			} else {
				System.out.println("Input Error");
				printMainHelp();
			}
		} while (!quit);

	}

	/**
	 * Prompt user for loading or editing the fixture manager set.
	 * 
	 * @param reader
	 *            commandline input source
	 */
	private void managerCLI(BufferedReader reader) {
		boolean quit = false;
		String input = null;
		String[] splitInput = null;

		printManagerHelp();

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
					profiles.addFixture(addProfileToSetCLI(reader));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException ex) {
					System.out.println("bad argument: " + ex.getMessage());
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
				System.out.println(profiles.toString());
			} else if (input.startsWith("h")) {
				printManagerHelp();
			} else {
				System.out.println("Input Error");
				printManagerHelp();
			}
		} while (!quit);
	}

	/**
	 * Create a fixture profile object by prompting commandline for input.
	 * 
	 * @return newly created fixture profile or NULL if exited without completing
	 * @throws IOException
	 *             if unable to access writer/reader
	 */
	private FixtureProfile addProfileToSetCLI(BufferedReader reader) throws IOException {
		String input;

		String name;
		int address;
		List<String> channels = new ArrayList<String>();

		// prompt
		System.out.println("Enter new light information");
		System.out.println("To cancel enter 'q'");

		// get name
		System.out.print("Light Name: ");
		input = reader.readLine().toLowerCase();
		// check for quit
		if (input.equals("q") || input.equals(""))
			return null;
		name = input;

		// get address
		System.out.print("Light Address: ");
		input = reader.readLine().toLowerCase();
		// check for quit
		if (input.equals("q") || input.equals(""))
			return null;
		// set address, making sure int parses correctly
		try {
			address = Integer.parseInt(input);
		} catch (NumberFormatException ex) {
			// bad address
			System.out.print("invalid address");
			return null;
		}

		// prompt for channel name entry
		System.out.println("For each channel, enter the channel name (in order of channel number)");
		System.out.println("enter 'done' when finished or 'q' to quit");
		int channelNum = 0;
		do {
			// prompt for channel name
			System.out.printf("channel %4d:", channelNum++);
			input = reader.readLine().toLowerCase();
			// check for quit
			if (input.equals("q")) {
				return null;
			}
			// add name to working channel set
			channels.add(input);
		} while (!input.equals("done") && channelNum < 512);

		// exited with done or max num channels, create profile and return it
		return new FixtureProfile(dmx, name, address, channels.toArray(new String[channels.size()]));
	}

	/**
	 * Prompt a user to delete a fixture from the set.
	 * 
	 * @param reader
	 *            command line input source
	 * @throws IOException
	 *             if unable to read from input source
	 */
	private void deleteProfileInSetCLI(BufferedReader reader) throws IOException {
		String input;
		int choice = 0;

		int numLights = profiles.getLightCount();

		if (numLights == 0) {
			System.out.println("Nothing to Delete");
			return;
		}

		System.out.println("Choose Light to Delete [0:" + (numLights - 1) + "]");
		System.out.println("'q' to quit");

		do {
			choice = 0;
			for (int i = 0; i < profiles.getLightCount(); i++) {
				System.out.printf("%4d: %s\n", i, profiles.getLight(i).getFixtureName());
			}

			System.out.print("Delete> ");

			input = reader.readLine().toLowerCase();

			if (input.equals("q") || input.equals(""))
				return;

			try {
				choice = Integer.parseInt(input);
			} catch (NumberFormatException ex) {
				// bad int value
				System.out.println(ex.getMessage());
				continue;
			}

			try {
				// remove the chosen fixture
				profiles.removeFixture(choice);
			} catch (ArrayIndexOutOfBoundsException ex) {
				// bad int value
				System.out.println(ex.getMessage());
				continue;
			}

			if (profiles.getLightCount() == 0) {
				// all profiles deleted
				System.out.println("All fixtures deleted.");
				return;
			}

		} while (true);

	}

	/**
	 * Choose a filename and save the current profile set.
	 * 
	 * @param reader
	 *            command line input source
	 * @throws IOException
	 *             if unable to read the default directory
	 */
	private void saveSetCLI(BufferedReader reader) throws IOException {
		String input = "";

		System.out.println("Current Set Files (you may overwrite or create new)");
		System.out.println("'q' to quit");

		// list the files currently in our set directory
		Files.list(Paths.get(Filepaths.SET_DIR)).forEach(file -> {
			if (Files.isDirectory(file))
				System.out.println("Directory " + file.getFileName());
			else
				System.out.println(file.getFileName());
		});

		// prompt for a filename
		System.out.print("Enter filename: ");
		input = reader.readLine();

		// check if quitting
		if (input.equals("q") || input.equals(""))
			return;

		// save the file
		profiles.saveSetFile(input);
	}

	/**
	 * Prompt user to load a set file. Discards current profile set.
	 * 
	 * @param reader
	 *            command line input source
	 * @throws IOException
	 *             if unable to read default directory
	 */
	private void loadSetCLI(BufferedReader reader) throws IOException {
		// get list of .set files in set directory
		Iterator<Path> fileIter = Files.newDirectoryStream(Paths.get(Filepaths.SET_DIR), "*.set").iterator();
		List<Path> listOfFiles = new ArrayList<Path>();
		String input = "";

		System.out.println("Current Set Files");
		System.out.println("Enter Number of File to Load (WARNING: will overwrite current fixture settings)");
		System.out.println("'q' to quit");

		// list options plus indices
		Path currFile;
		int i = 0;
		while (fileIter.hasNext()) {
			currFile = fileIter.next();
			if (Files.isRegularFile(currFile)) {
				System.out.println(i++ + ". " + currFile.getFileName());
				listOfFiles.add(currFile);
			}
		}

		// prompt for choice
		System.out.print("Load> ");
		input = reader.readLine();
		// check for quit
		if (input.equals("q") || input.equals(""))
			return;
		// parse the choice
		int choice = -1;
		try {
			choice = Integer.parseInt(input);
		} catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
			// bad input
			System.out.println(ex.getMessage());
			return;
		}

		// create the new profile set manager
		profiles = new FixtureManager(dmx, listOfFiles.get(choice).getFileName().toString());
	}

	private void printMainHelp() {
		System.out.println("DMX Controller Commands:");
		System.out.println("\tp: profile manager");
		System.out.println("\tz: print current set");
		System.out.println("\td: dmx viewer");
		if (app.isServerRunning()) {
			System.out.println("\ts: stop server");
			System.out.println("\tn: network info");
		} else
			System.out.println("\ts: start server");
		System.out.println("\th: help");
		System.out.println("\tc: close only CLI");
		System.out.println("\tq: quit all");
	}

	private static void printManagerHelp() {
		System.out.println("Set Manager Commands:");
		System.out.println("\ta: add profile");
		System.out.println("\td: delete profile");
		System.out.println("\ts: save set");
		System.out.println("\tl: load set");
		System.out.println("\tp: print current set");
		System.out.println("\th: help");
		System.out.println("\tq: quit/back");
	}

}
