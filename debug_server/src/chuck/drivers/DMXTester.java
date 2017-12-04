package chuck.drivers;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Test program to debug DMX driver. Prompts user to interact with driver.
 * 
 * @author Joseph Eichenhofer
 */
public class DMXTester {
	
	public static void main(String[] args) {
		DMXDriver driver = null;
		int[] dmx = null;

		try {
			driver = new DMXDummy();
		} catch (IOException ex) {
			ex.printStackTrace();
			System.out.println("Make sure you ran insmod and started this program as root.");
			System.exit(-1);
		}

		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		boolean quit = false;
		String input = null;
		String[] splitInput = null;

		printHelp();

		do {
			System.out.print(">");
			try {
				input = reader.readLine().toLowerCase();
				splitInput = input.split(" ");
			} catch (IOException ex) {
				ex.printStackTrace();
				System.exit(-1);
			}
			if (splitInput[0].startsWith("quit")) {
				quit = true;
			} else if (splitInput[0].startsWith("clear")) {
				try {
					driver.clearDMX();
				} catch (IOException ex) {
					ex.printStackTrace();
					System.exit(-1);
				}
			} else if (splitInput[0].startsWith("set")) {
				int address, value;
				try {
					if (splitInput.length != 3)
						throw new IllegalArgumentException("Set command requires two arguments");
					address = Integer.parseInt(splitInput[1]);
					value = Integer.parseInt(splitInput[2]);
					driver.setDMX(address, value);
				} catch (IllegalArgumentException ex) {
					System.out.println(ex.getMessage());
					printHelp();
				} catch (IOException ex) {
					ex.printStackTrace();
					System.exit(-1);
				}
			} else if (input.startsWith("read")) {
				int address;
				try {
					if (splitInput.length != 2)
						throw new IllegalArgumentException("Read command requires one arguments");
					address = Integer.parseInt(splitInput[1]);
					System.out.println("Read: " + driver.getDMX(address) + " at address: " + address);
				} catch (IllegalArgumentException ex) {
					System.out.println(ex.getMessage());
					printHelp();
				}
			} else if (input.startsWith("save")) {
				dmx = driver.getDmx();
				System.out.println("Read DMX array:");
				printDMX(dmx);
			} else if (input.startsWith("restore")) {
				try {
					driver.setDMX(dmx);
					System.out.println("Restored DMX array:");
					printDMX(dmx);
				} catch (IOException ex) {
					ex.printStackTrace();
					System.exit(-1);
				}
			} else if (input.startsWith("help")) {
				printHelp();
			} else {
				System.out.println("invalid command");
			}
		} while (!quit);
		
		System.exit(0);
	}

	public static void printHelp() {
		System.out.println("Commands:");
		System.out.println("\tquit");
		System.out.println("\tclear");
		System.out.println("\tset addr val");
		System.out.println("\tread addr");
		System.out.println("\tsave");
		System.out.println("\trestore");
		System.out.println("\thelp");
	}
	
	public static void printDMX(int[] dmxVals) {
		if (dmxVals.length != 513)
			throw new IllegalArgumentException("DMX array must be 513 ints long");
		
		for (int i = 1 ; i <= 512 ; i++) {
			if (i % 16 == 1) {
				System.out.printf("\n");
				System.out.printf("%4d:", i);
			}
			System.out.printf("%4d", dmxVals[i]);
		}
		System.out.println("");
	}
}
