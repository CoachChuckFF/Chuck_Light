package chuck;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import chuck.defines.Connection;
import chuck.defines.Modes;
import chuck.drivers.DMXDriver;
import chuck.threads.ChaseThread;
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
	private SceneManager sceneManager;
	private byte[] dmxVals; //1 indexed
	private byte[] dmxTempVals; //1 indexed
	private byte currentState;
	private boolean serverRunning = false;
	private DatagramSocket serverSocket;
	private int chaseSceneDelay = 100;

	private HeartBeatThread heartbeat;
	private UDPServerThread udpListen;
	private UserCLIThread cli;
	private ChaseThread chase;
	
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
		/*try {
			// instantiate dmx driver
			dmx = new DMXDriver();
			System.out.println("DMX Driver Initialized");
		} catch (IOException ex) {
			// fatal error if unable to instantiate driver
			ex.printStackTrace();
			System.exit(-1);
		}*/
		
		profiles = new ProfileManager(dmx);
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
		boolean sendHeartbeat = false;
		
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
		
		//load scenes
		//sceneManager = new SceneManager(dmx.getDmx());
		sceneManager = new SceneManager(new int[513]);
		
		sceneManager.updateSceneFile();
		
		startMainCLI();
		
		WirelessCommand currCommand = null;
		serverRunning = true;
		while (serverRunning) {
			// take element from queue, blocking until something is there
			try {
				currCommand = commandQ.take();
				if(!heartbeat.getConnected())
				{
					heartbeat.setAddress(currCommand.sender_ip);
				}
			} catch (InterruptedException ex) {
				// for now, treat interruptedexception as fatal error
				ex.printStackTrace();
				System.exit(-1);
			}

			if (!currCommand.parse()) {
				// if unable to parse the command, ignore it
				System.out.println("Received invalid packet");
				continue;
			}

			sendHeartbeat = false;
			
			switch (currCommand.getPacketType()) {
			case Connection.DATA_PACKET_ID:
			/* ---------- Main State Machine ------------------- */
				/*for debug purposes*/
				switch(currCommand.getUserActionData()){
				case Connection.UP:
					System.out.println("up");
					break;
				case Connection.DOWN:
					System.out.println("down");
					break;
				case Connection.LEFT:
					System.out.println("left");
					break;
				case Connection.RIGHT:
					System.out.println("right");
					break;
				case Connection.B1:
					System.out.println("b1");
					break;
				case Connection.B2:
					System.out.println("b2");
					break;
				case Connection.PS2:
					System.out.println("ps2");
					break;
				case Connection.B12:
					System.out.println("b12");
					break;
				case Connection.KONAMI:
					System.out.println("ko");
					currentState = Modes.PARTY;
					sendHeartbeat = true;
					break;
				case Connection.REV_KONAMI:
					System.out.println("rko");
					break;
				}
				
				switch(currentState)
				{
					case Modes.CHASE:
						if(currCommand.getDataType() != Connection.USER_ACTION_DATA){
							sendHeartbeat = true;
							break;
						}
						switch(currCommand.getUserActionData()){
						case Connection.UP:
							if(chaseSceneDelay < Modes.MAX_CHASE_DELAY){
								chase.setSceneDelay(chaseSceneDelay+=100);
							}
							break;
						case Connection.DOWN:
							if(chaseSceneDelay > Modes.MIN_CHASE_DELAY){
								chase.setSceneDelay(chaseSceneDelay-=100);
							}
							break;
						case Connection.B2:
							currentState = Modes.IDLE;
							chase.redrum();
							sendHeartbeat = true;
							break;
						}
					break;
					case Modes.IDLE:
						if(currCommand.getDataType() != Connection.USER_ACTION_DATA){
							sendHeartbeat = true;
							break;
						}
						switch(currCommand.getUserActionData()){
						case Connection.LEFT:
							/*try {
								dmx.setDMX(sceneManager.getLastScene().getDmxVals());
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}*/
							System.out.println(Arrays.toString(sceneManager.getLastScene().getDmxVals()));
							break;
						case Connection.RIGHT:
							/*try {
								dmx.setDMX(sceneManager.getNextScene().getDmxVals());
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}*/
							System.out.println(Arrays.toString(sceneManager.getNextScene().getDmxVals()));
							break;
						case Connection.B1:
							currentState = Modes.LIGHT_SELECTION;
							//TODO Highlight first Light
							//sceneManager.setCurrentScene(dmx.getDmx());
							sceneManager.setCurrentScene(new int[513]);
							sendHeartbeat = true;
							break;
						case Connection.B2:
							if(sceneManager.getSceneCount() > 1){
								currentState = Modes.CHASE;
								chase = new ChaseThread(chaseSceneDelay, sceneManager.getSceneArray(), dmx);
								chase.start();
								sendHeartbeat = true;
							} else {
								System.out.println("Can't play chase with less than 2 Scenes");
							}
							break;
						case Connection.PS2:
							//adds scene to list
							if(sceneManager.currentIndex == -1)
							{
								sceneManager.addScene(sceneManager.getCurrentScene());
								sceneManager.updateSceneFile();
							}
							break;
						case Connection.PS2_LONG:
							if(sceneManager.currentIndex != -1)
							{
								sceneManager.deleteScene();
								//dmx.setDMX(sceneManager.getCurrentScene().getDmxVals());
							}
							break;
						case Connection.KONAMI:
							currentState = Modes.PARTY;
							sendHeartbeat = true;
							break;
						case Connection.REV_KONAMI:
							currentState = Modes.SCARY;
							sendHeartbeat = true;
							break;
						}
					break;
					case Modes.LIGHT_SELECTION:
						if(currCommand.getDataType() != Connection.USER_ACTION_DATA){
							sendHeartbeat = true;
							break;
						}
						switch(currCommand.getUserActionData()){
						case Connection.LEFT:
							//TODO Highlight Prevous Light
							break;
						case Connection.RIGHT:
							//TODO Highlight Next Light
							break;
						case Connection.B1:
							currentState = Modes.CONTROL_SELECTION;
							//creates carbon copy of lights as is
							System.arraycopy(dmxVals, 0, dmxTempVals, 0, 513);
							//TODO start colorwheel visualization
							sendHeartbeat = true;
							break;
						case Connection.B2:
							currentState = Modes.IDLE;
							sendHeartbeat = true;
							break;
						}
					break;
					case Modes.CONTROL_SELECTION: //TODO need stateful information - what mode is highlighted
						if(currCommand.getDataType() != Connection.USER_ACTION_DATA){
							sendHeartbeat = true;
							break;
						}
						switch(currCommand.getUserActionData()){
						case Connection.LEFT:
							//TODO Position Dependant
							break;
						case Connection.RIGHT:
							//TODO Position Dependant
							break;
						case Connection.B1:
							//TODO Position Dependant
							currentState = Modes.COLOR_WHEEL;
							sendHeartbeat = true;
							break;
						case Connection.B2:
							currentState = Modes.LIGHT_SELECTION;
							//TODO copy dmxTempVals to Light
							sendHeartbeat = true;
							break;
						}	
					break;
					case Modes.COLOR_WHEEL: 
						if(currCommand.getDataType() == Connection.USER_ACTION_DATA){
							switch(currCommand.getUserActionData()){
							case Connection.B1:
								//TODO SaveChanges
								currentState = Modes.LIGHT_SELECTION;
								sendHeartbeat = true;
								break;
							case Connection.B2:
								currentState = Modes.CONTROL_SELECTION;
								//TODO start Colorwheel visulization
								sendHeartbeat = true;
								break;
							}
						}
						else if(currCommand.getDataType() == Connection.JOYSTIC_DATA)
						{
							System.out.println(Arrays.toString(currCommand.getJoystickData()));
							//TODO RGB stuff with Joystick
						}
						else
						{
							//bad mode
							sendHeartbeat = true;
							break;
						}
					break;
					case Modes.DMX: //TODO needs stateful data - current DMX channel selected
						if(currCommand.getDataType() != Connection.USER_ACTION_DATA){
							sendHeartbeat = true;
							break;
						}
						switch(currCommand.getUserActionData()){
						case Connection.LEFT:
							//TODO Control Last DMX Channel
							break;
						case Connection.RIGHT:
							//TODO Control Next DMX Channel
							break;
						case Connection.UP:
							//TODO Increase DMX value @ channel
							break;
						case Connection.DOWN:
							//TODO Decrease DMX value @ channel
							break;
						case Connection.B1:
							//TODO Save Changes
							currentState = Modes.LIGHT_SELECTION;
							sendHeartbeat = true;
							break;
						case Connection.B2:
							currentState = Modes.CONTROL_SELECTION;
							//start DMX mode visulization 
							sendHeartbeat = true;
							break;
						}
					break;
					case Modes.PRESET:
						switch(currCommand.getUserActionData()){
						case Connection.LEFT:
							//TODO Last Preset
							break;
						case Connection.RIGHT:
							//TODO Next Preset
							break;
						case Connection.B1:
							//TODO Save
							currentState = Modes.LIGHT_SELECTION;
							sendHeartbeat = true;
							break;
						case Connection.B2:
							currentState = Modes.CONTROL_SELECTION;
							//start Preset visulization
							sendHeartbeat = true;
							break;
						}
					break;
					case Modes.PARTY:
						switch(currCommand.getUserActionData()){
						case Connection.B12:
							//TODO Save
							currentState = Modes.IDLE;
							sendHeartbeat = true;
							break;
						}
					break;
					case Modes.SCARY:
						switch(currCommand.getUserActionData()){
						case Connection.B12:
							//TODO Save
							currentState = Modes.IDLE;
							sendHeartbeat = true;
							break;
						}
					break;
					default:
						System.out.println("State out of bounds!");
					break;
				}
				break;
			case Connection.POLL_REPLY_PACKET_ID:
				//Do mode matching maybe?
				System.out.println("Poll Reply");
				break;
			case Connection.POLL_PACKET_ID:
				//Do mode matching maybe?
				System.out.println("Poll");
				break;
			default:
				System.out.println("Unkown Packet Type");
			break;
			}
			
			if(sendHeartbeat)
			{
				heartbeat.setCurrentState(currentState);
				heartbeat.sendHeartbeat();
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
