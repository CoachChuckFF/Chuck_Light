package chuck;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import chuck.dmx.DMXDriver;
import chuck.dmx.DefaultDMX;
import chuck.lighting.FixtureManager;
import chuck.threads.ServerAppThread;

public class UserCLI {

	private DMXDriver dmx;

	private FixtureManager profiles;
	private ServerAppThread app;

	public UserCLI() {
		try {
			// instantiate dmx driver
			dmx = new DefaultDMX();
			System.out.println("DMX Driver Initialized");
			profiles = new FixtureManager(dmx);
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
				if(app.isServerRunning())
					System.out.println("Server will continue to run");
			} else if (splitInput[0].startsWith("p")) {
				if(app.isServerRunning())
					System.out.println("stop server first");
				else
					profiles.managerCLI(reader);

			} else if (splitInput[0].startsWith("z")) {
				System.out.println(profiles.toString());
			} else if (splitInput[0].startsWith("d")) {
				System.out.println("add in DMX veiwer");

			} else if (splitInput[0].startsWith("s")) {
				if(app.isServerRunning()){
					app.stopServer();
					try {
						app.join();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						System.exit(-1);
					}
					System.out.println("Server Stopped");
				}
				else
				{
					app = new ServerAppThread(dmx, profiles);
					app.start();
				}
			} else if (splitInput[0].startsWith("n")) {
				if(app.isServerRunning()){
					System.out.println("Add network info");
				}
				else {
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

	public void printMainHelp(){
		System.out.println("DMX Controller Commands:");
		System.out.println("\tp: profile manager");
		System.out.println("\tz: print current set");
		System.out.println("\td: dmx viewer");
		if(app.isServerRunning())
		{
			System.out.println("\ts: stop server");
			System.out.println("\tn: network info");
		}
		else
			System.out.println("\ts: start server");
		System.out.println("\th: help");
		System.out.println("\tc: close only CLI");
		System.out.println("\tq: quit all");
	}

}
