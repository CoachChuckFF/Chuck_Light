package chuck;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import chuck.defines.Connection;
import chuck.defines.Modes;
import chuck.drivers.DMXDriver;
import chuck.threads.HeartBeatThread;
import chuck.threads.UDPServerThread;
import chuck.threads.UserCLIThread;

/**
 * Chuck Light server application. Starts heartbeat thread and udp listener
 * thread, then waits for commands from the udp listener thread via the shared
 * queue.
 * 
 * @author Joseph Eichenhofer and Christian Krueger
 */
public class ServerApp {

	private DMXDriver dmx;
	private ProfileManager profiles;
	private byte[] dmxVals; //1 indexed
	private byte[] dmxTempVals; //1 indexed
	private byte currentState;
	private boolean serverRunning = false;
	private DatagramSocket serverSocket;

	private HeartBeatThread heartbeat;
	private UDPServerThread udpListen;
	private UserCLIThread cli;
	
	private int currentLightIndex;

	/**
	 * Shared synchronous queue of commands to process. Producer is UDPServerThread, consumer is main execution.
	 */
	private BlockingQueue<WirelessCommand> commandQ;

	/**
	 * Start the server program.
	 * 
	 * @param args n/a
	 */
	public static void main(String[] args) {
		ServerApp serv = new ServerApp();
		serv.init();
		serv.startServer();
	}
	
	public void init(){
		profiles = new ProfileManager();
		/*try {
			// instantiate dmx driver
			dmx = new DMXDriver();
			System.out.println("DMX Driver Initialized");
		} catch (IOException ex) {
			// fatal error if unable to instantiate driver
			ex.printStackTrace();
			System.exit(-1);
		}*/
	}
	
	public void startMainCLI(){
		cli = new UserCLIThread(dmxVals, profiles, this);
		cli.setPriority(Thread.MIN_PRIORITY);
		cli.start();

	}

	/**
	 * Instantiate the DMX driver, datagram socket, and two threads. Then wait for
	 * commands from the wireless controller (via udp thread). Parses the command
	 * bytes, then interprets the change of state based on current state and the
	 * type of command.
	 */
	public void startServer() {
		
		dmxVals = new byte[513];
		dmxTempVals = new byte[513];
		currentState = Modes.IDLE;
		currentLightIndex = 0;
		
		try {
			// instantiate server socket
			serverSocket = new DatagramSocket(Connection.DMX_PORT);
			System.out.println("Server Socket Initialized");
		} catch (SocketException ex) {
			// fatal error if unable to instantiate server socket
			ex.printStackTrace();
			System.exit(-1);
		}
		
		//create new command queue
		commandQ = new LinkedBlockingQueue<WirelessCommand>();

		// start heartbeat thread
		heartbeat = new HeartBeatThread(serverSocket, currentState);
		heartbeat.setPriority(Thread.NORM_PRIORITY);
		heartbeat.start();
		
		System.out.println("Heartbeat Thread Started");
		// start udp listener thread
		udpListen = new UDPServerThread(serverSocket, commandQ);
		udpListen.setPriority(Thread.MAX_PRIORITY);
		udpListen.start();
		System.out.println("UDP Thread Started");

		// TODO: implement state machine and transitions

		startMainCLI();
		
		WirelessCommand currCommand = null;
		serverRunning = true;
		while (serverRunning) {
			// take element from queue, blocking until something is there
			try {
				currCommand = commandQ.take();
			} catch (InterruptedException ex) {
				// for now, treat interruptedexception as fatal error
				ex.printStackTrace();
				System.exit(-1);
			}

			System.out.println("Received packet.");
			if (!currCommand.parse()) {
				// if unable to parse the command, ignore it
				System.out.println("Received invalid packet");
				continue;
			}

			switch (currCommand.getID()) {
			case Connection.COMMAND_PACKET_ID:
				System.out.println("received command packet");
				break;
			default:
				System.out.println(currCommand.toString());
				break;
			}

			// for now, just toggle the light on and off with each received packet
			/*try {
				if (currCommand.getID() == Connection.POLL_PACKET_ID || currCommand.getID() == Connection.POLL_REPLY_PACKET_ID)
					continue;
				if (on) {
					dmx.setDMX(1, 0);
				} else {
					dmx.setDMX(1, 255);
				}
				on = !on;
			} catch (IOException e) {
				// treat ioexception as fatal error
				e.printStackTrace();
				System.exit(-1);
			}*/
			
			/* ---------- Main State Machine ------------------- */
			switch(currentState)
			{
				case Modes.CHASE:
				break;
				case Modes.IDLE:
					//switch(currCommand.action)
				break;
				case Modes.LIGHT_SELECTION:
				break;
				case Modes.CONTROL_SELECTION:
				break;
				case Modes.COLOR_WHEEL:
				break;
				case Modes.DMX:
				break;
				case Modes.PRESET:
				break;
				case Modes.PARTY:
				break;
				case Modes.SCARY:
				break;
				default:
					System.out.println("State out of bounds!");
				break;
			}
		}
	}
	
	public void stopServer(){
		if(!udpListen.equals(null))
			try {
				udpListen.redrum();
				udpListen.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		if(!heartbeat.equals(null))
			try {
				heartbeat.redrum();
				heartbeat.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		if(!serverSocket.equals(null))
			serverSocket.close();
		
		/*try {
			dmx.clearDMX();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		serverRunning = false;
	}
	
	public ProfileManager getProfileManager(){
		return profiles;
	}
	
	public byte[] getDMXVals(){
		return dmxVals;
	}
	
	public boolean isServerRunning()
	{
		return serverRunning;
		
	}
	
}
