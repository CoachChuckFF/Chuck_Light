package chuck.threads;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import chuck.ProfileManager;
import chuck.ServerApp;
import chuck.WirelessCommand;

public class UserCLIThread extends Thread {

	private int[] dmxVals;
	private ProfileManager profiles;
	private ServerApp app;
	
	public UserCLIThread(ProfileManager profiles, ServerApp app) {
		this.profiles = profiles;
		this.app = app;
	}
	
	public void run() {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		boolean quit = false;
		String input = null;
		String[] splitInput = null;

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
				System.out.println("Goodbye");
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
					System.out.println("Server Stopped");
				}
				else
				{
					app.startServer();
					quit = true;

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
