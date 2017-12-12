package chuck.threads;

import java.awt.Color;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import chuck.defines.Connection;
import chuck.defines.LightingDefines;
import chuck.defines.Modes;
import chuck.lighting.XYConverter;
import chuck.defines.WirelessCommand;
import chuck.dmx.DMXDriver;
import chuck.lighting.FixtureManager;
import chuck.lighting.FixtureProfile;
import chuck.lighting.SceneManager;

/**
 * Chuck Light server application. Starts heartbeat thread and udp listener
 * thread, then waits for commands from the udp listener thread via the shared
 * queue.
 * 
 * @author Joseph Eichenhofer and Christian Krueger
 */
public class ServerAppThread extends Thread {

	private DMXDriver dmx;
	private FixtureManager profiles;
	private SceneManager sceneManager;
	private ArrayList<FixtureProfile> selectedLights;
	private byte currentState;
	private boolean serverRunning = false;
	private DatagramSocket serverSocket;
	private int chaseSceneDelay = 100;
	
	private XYConverter colorConverter = null;
	
	private HeartBeatThread heartbeat = null;
	private UDPServerThread udpListen = null;
	private ChaseThread chase = null;
	private HighlightThread highlight = null;
	private PresetVisualThread presetVisual = null;
	private RainbowThread rainbowVisual = null;
	private DMXVisualThread dmxVisual = null;
	
	private int currentLightIndex;
	private int currentPresetIndex;
	private int currentControlIndex;
	private int currentChannelIndex;
	
	private int min = Integer.MAX_VALUE;
	private int max = Integer.MIN_VALUE;
	
	private boolean canChangeDMX;

	/**
	 * Shared synchronous queue of commands to process. Producer is UDPServerThread, consumer is main execution.
	 */
	private BlockingQueue<WirelessCommand> commandQ;

	public ServerAppThread(DMXDriver driver, FixtureManager profManager) {
		super();
		dmx = driver;
		profiles = profManager;
	}
	
	/**
	 * Instantiate the DMX driver, datagram socket, and two threads. Then wait for
	 * commands from the wireless controller (via udp thread). Parses the command
	 * bytes, then interprets the change of state based on current state and the
	 * type of command.
	 */
	public void run() {
		
		currentState = Modes.IDLE;
		currentLightIndex = 0;
		currentPresetIndex = 0;
		currentControlIndex = 0;
		currentChannelIndex = 0;
		canChangeDMX = false;
		boolean sendHeartbeat = false;
		
		int xVal = 0, yVal = 0;
		int currentChannelVal = 0;
		
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
		try {
			sceneManager = new SceneManager(dmx.getDmx());
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
			System.exit(-1);
		}
		
		try {
			sceneManager.updateSceneFile();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
			System.exit(-1);
		}
		
		colorConverter = new XYConverter();
				
		WirelessCommand currCommand = null;
		serverRunning = true;
				
		while (serverRunning) {
			// take element from queue, blocking until something is there
			try {
				currCommand = commandQ.poll(1, TimeUnit.SECONDS);
				
				if (currCommand == null)
					continue;
				
//				if(!heartbeat.getConnected())
//				{
//					heartbeat.setAddress(currCommand.getSender_ip());
//				}
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
				case Connection.PS2_LONG:
					System.out.println("ps2 long");
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
							if(chaseSceneDelay < LightingDefines.MAX_CHASE_DELAY){
								chase.setSceneDelay(chaseSceneDelay+=LightingDefines.CHASE_STEP);
							}
							break;
						case Connection.DOWN:
							if(chaseSceneDelay > LightingDefines.MIN_CHASE_DELAY){
								chase.setSceneDelay(chaseSceneDelay-=LightingDefines.CHASE_STEP);
							}
							break;
						case Connection.B2:
							currentState = Modes.IDLE;

							redrumChase();
							
							revertScene();
							
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
							try {
								dmx.setDMX(sceneManager.getLastScene().getDmxVals());
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							System.out.println(sceneManager.getCurrentIndex());
							break;
						case Connection.RIGHT:
							try {
								dmx.setDMX(sceneManager.getNextScene().getDmxVals());
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							System.out.println(sceneManager.getCurrentIndex());
							break;
						case Connection.DOWN:
							try {
								dmx.setDMX(sceneManager.getCurrentScene().getDmxVals());
								sceneManager.setCurrentScene(sceneManager.getCurrentScene().getDmxVals());
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							System.out.println(sceneManager.getCurrentIndex());
							break;
						case Connection.B1:
							currentState = Modes.LIGHT_SELECTION;
							sceneManager.setCurrentScene(dmx.getDmx());
							startHighlight();
							sendHeartbeat = true;
							break;
						case Connection.B2:
							if(sceneManager.getSceneCount() > 1){
								currentState = Modes.CHASE;
								startChase();
								sendHeartbeat = true;
							} else {
								System.out.println("Can't play chase with less than 2 Scenes");
							}
							break;
						case Connection.PS2:
							//adds scene to list
							if(sceneManager.getCurrentIndex() == -1)
							{
								sceneManager.addScene(dmx.getDmx());
								try {
									sceneManager.updateSceneFile();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
									System.exit(-1);
								}
							}

							break;
						case Connection.PS2_LONG:
							if(sceneManager.getCurrentIndex() != -1)
							{
								sceneManager.deleteScene();

								try {
									sceneManager.updateSceneFile();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
									System.exit(-1);
								}
								
								revertScene();
							} else {
								
								sceneManager.deleteScene(sceneManager.getSceneCount()-1);
								try {
									dmx.clearDMX();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

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
							if(!profiles.getLight(currentLightIndex).isSelected()) {
								highlight.removeLight(profiles.getLight(currentLightIndex));
								revertScene();
								highlight.updateDefaultColor();
							}
							
							if(--currentLightIndex < 0)
								currentLightIndex = profiles.getLightCount() - 1;
							
							if(!profiles.getLight(currentLightIndex).isSelected())
								highlight.addLight(profiles.getLight(currentLightIndex));
							break;
						case Connection.RIGHT:
							if(!profiles.getLight(currentLightIndex).isSelected()) {
								highlight.removeLight(profiles.getLight(currentLightIndex));
								revertScene();
								highlight.updateDefaultColor();
							}
							
							if(--currentLightIndex < 0)
								currentLightIndex = profiles.getLightCount() - 1;
							
							if(!profiles.getLight(currentLightIndex).isSelected())
								highlight.addLight(profiles.getLight(currentLightIndex));
							break;
						case Connection.PS2:
							if(profiles.getLight(currentLightIndex).isSelected()) {
								profiles.getLight(currentLightIndex).setSelected(false);
							} else {
								profiles.getLight(currentLightIndex).setSelected(true);
							}
							break;
						case Connection.PS2_LONG:
								clearSelected();
								highlight.addLight(profiles.getLight(currentLightIndex));
							break;
						case Connection.B1:
							currentState = Modes.CONTROL_SELECTION;
							
							selectedLights = redrumHighlight();
							
							int temp = selectedLights.get(0).getNumChannels();
							for (FixtureProfile light : selectedLights) {
								if(temp != light.getNumChannels())
								{
									
									canChangeDMX = false;
									break;
								}

								canChangeDMX = true;
								temp = light.getNumChannels();

							}
							
							clearSelected();
							revertScene();
							
							currentControlIndex = 1;
							startPresetVisual();
							sendHeartbeat = true;
							break;
						case Connection.B2:
							redrumHighlight();
							
							clearSelected();
							
							revertScene();
							
							currentState = Modes.IDLE;
							sendHeartbeat = true;
							break;
						}
					break;
					case Modes.CONTROL_SELECTION:
						if(currCommand.getDataType() != Connection.USER_ACTION_DATA){
							sendHeartbeat = true;
							break;
						}
						switch(currCommand.getUserActionData()){
						case Connection.LEFT:
							switch(currentControlIndex)
							{
							case -1:
								redrumDMXVisual();
								currentControlIndex = 1;
								revertScene();
								startPresetVisual();
								break;
							case 0:
								redrumRainbow();
								if(canChangeDMX) {
									currentControlIndex = -1;
									revertScene();
									startDMXVisual();
								}
								else {
									currentControlIndex = 1;
									startPresetVisual();
								}
								break;
							case 1:
								redrumPresetVisual();
								currentControlIndex = 0;
								revertScene();
								startRainbow();
								break;
							}
							break;
						case Connection.RIGHT:
							switch(currentControlIndex)
							{
							case -1:
								redrumDMXVisual();
								currentControlIndex = 0;
								revertScene();
								startRainbow();
								break;
							case 0:
								redrumRainbow();
								currentControlIndex = 1;
								revertScene();
								startPresetVisual();
								break;
							case 1:
								redrumPresetVisual();
								if(canChangeDMX) {
									currentControlIndex = -1;
									revertScene();
									startDMXVisual();
								}
								else {
									currentControlIndex = 0;
									startRainbow();
								}
								break;
							}
							break;
						case Connection.B1:
							switch(currentControlIndex)
							{
							case -1:
								redrumDMXVisual();
								revertScene();
								currentChannelIndex = 0;
								currentState = Modes.DMX;
								break;
							case 0:
								redrumRainbow();
								currentState = Modes.COLOR_WHEEL;

								break;
							case 1:
								redrumPresetVisual();
								currentState = Modes.PRESET;

								break;
							}
							
							sendHeartbeat = true;
							break;
						case Connection.B2:
							currentState = Modes.LIGHT_SELECTION;
							switch(currentControlIndex)
							{
							case -1:
								redrumDMXVisual();
								break;
							case 0:
								redrumRainbow();
								break;
							case 1:
								redrumPresetVisual();
								break;
							}
							revertScene();
							
							startHighlight();

							sendHeartbeat = true;

							break;
						}	
					break;
					case Modes.COLOR_WHEEL: 
						if(currCommand.getDataType() == Connection.USER_ACTION_DATA){
							switch(currCommand.getUserActionData()){
							case Connection.B1:
								sceneManager.setCurrentScene(dmx.getDmx());
								
								currentState = Modes.LIGHT_SELECTION;
								
								startHighlight();
								sendHeartbeat = true;
								break;
							case Connection.B2:
								currentState = Modes.CONTROL_SELECTION;
								
								startRainbow();
								
								sendHeartbeat = true;
								break;
							}
						}
						else if(currCommand.getDataType() == Connection.JOYSTIC_DATA)
						{
							System.out.println(Arrays.toString(currCommand.getJoystickData()));
							xVal = currCommand.getJoystickData()[0];
							yVal = currCommand.getJoystickData()[1];
							
							for (FixtureProfile light : selectedLights) {
								try {
									light.setColor(colorConverter.getColor(xVal, yVal));
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
						else
						{
							System.out.println("Panic");
							sendHeartbeat = true;
							break;
						}
					break;
					case Modes.DMX:
						if(currCommand.getDataType() != Connection.USER_ACTION_DATA){
							sendHeartbeat = true;
							break;
						}
						switch(currCommand.getUserActionData()){
						case Connection.LEFT:
							if(--currentChannelIndex < 0)
								currentChannelIndex = selectedLights.get(0).getNumChannels() - 1;
							
							break;
						case Connection.RIGHT:
							if(++currentChannelIndex >= selectedLights.get(0).getNumChannels())
								currentChannelIndex = 0;
							
							break;
						case Connection.UP:
							for (FixtureProfile light : selectedLights) {
								
								currentChannelVal = light.getDMXVals()[currentChannelIndex];
								currentChannelVal += LightingDefines.DMX_STEP;
								if(currentChannelVal > 255)
									continue;
								try {
									light.setChannelManual(currentChannelIndex, currentChannelVal);
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								
							}
							break;
						case Connection.DOWN:
							for (FixtureProfile light : selectedLights) {
								
								currentChannelVal = light.getDMXVals()[currentChannelIndex];
								currentChannelVal -= LightingDefines.DMX_STEP;
								if(currentChannelVal < 0)
									continue;
								try {
									light.setChannelManual(currentChannelIndex, currentChannelVal);
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								
							}
							break;
						case Connection.B1:
							sceneManager.setCurrentScene(dmx.getDmx());
							
							currentState = Modes.LIGHT_SELECTION;
							
							startHighlight();
							sendHeartbeat = true;
							break;
						case Connection.B2:
							currentState = Modes.CONTROL_SELECTION;
							
							revertScene();
							
							startDMXVisual();
							
							sendHeartbeat = true;
							break;
							
						case Connection.PS2:
							for (FixtureProfile light : selectedLights) {
								try {
									light.clearLight();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
					
						}
					break;
					case Modes.PRESET:
						switch(currCommand.getUserActionData()){
						case Connection.LEFT:
							if(--currentPresetIndex < 0){
								currentPresetIndex = LightingDefines.PRESETS.length - 1;
							}
							for (FixtureProfile light : selectedLights) {
								try {
									light.setColor(LightingDefines.PRESETS[currentPresetIndex]);
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
									System.exit(-1);
								}
							}
							break;
						case Connection.RIGHT:
							if(++currentPresetIndex >= LightingDefines.PRESETS.length){
								currentPresetIndex = 0;
							}
							for (FixtureProfile light : selectedLights) {
								try {
									light.setColor(LightingDefines.PRESETS[currentPresetIndex]);
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
									System.exit(-1);
								}
							}
							break;
						case Connection.B1:
							//saves scene
							sceneManager.setCurrentScene(dmx.getDmx());
							
							currentState = Modes.LIGHT_SELECTION;
							
							startHighlight();
							
							sendHeartbeat = true;
							break;
						case Connection.B2:
							currentState = Modes.CONTROL_SELECTION;
							
							startPresetVisual();
							
							sendHeartbeat = true;
							break;
						}
					break;
					case Modes.PARTY:
						if(currCommand.getDataType() == Connection.USER_ACTION_DATA){
							switch(currCommand.getUserActionData()){
							case Connection.PS2_LONG:
								System.out.println(min + " " + max );
								currentState = Modes.IDLE;
								sendHeartbeat = true;
								break;
							}
						}
						else if(currCommand.getDataType() == Connection.GYRO_DATA)
						{
							int gyroData = currCommand.getGyroData();
							int dv = 0;
							int rR;
							int rG;
							int rB;
							
							if(gyroData > 300)
								dv = 10;
							if(gyroData > 350)
								dv = 30;
							if(gyroData > 400)
								dv = 45;
							if(gyroData > 600)
								dv = 90;
							if(gyroData > 670)
								dv = 100;
							if(gyroData > 900)
								dv = 120;
							if(gyroData > 950)
								dv = 175;
							if(gyroData > 1000)
								dv = 200;
							if(gyroData > 1100)
								dv = 225;
							if(gyroData > 1400)
								dv = 240;
							if(gyroData > 1700)
								dv = 250;
							if(gyroData > 2000)
								dv = 255;

							
							System.out.println(dv);
							System.out.println(gyroData);
							
							for (int i = 0; i < profiles.getLightCount(); i++) {
								try {
									rR = (int) (Math.random()*256);
									rG = (int) (Math.random()*256);
									rB = (int) (Math.random()*256);
									profiles.getLight(i).setDimmerValue(dv);
									profiles.getLight(i).setColor(new Color(rR, rG, rB));
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							
						}
						else
						{
							
							sendHeartbeat = true;
							break;
						}
					break;
					case Modes.SCARY:
						if(currCommand.getDataType() == Connection.USER_ACTION_DATA){
							switch(currCommand.getUserActionData()){
							case Connection.PS2_LONG:
								currentState = Modes.IDLE;
								sendHeartbeat = true;
								break;
							}
						}
						else if(currCommand.getDataType() == Connection.GYRO_DATA)
						{
							int gyroData = currCommand.getGyroData();
							int dv = 247;
							
							if(gyroData > 300)
								dv = 230;
							if(gyroData > 320)
								dv = 220;
							if(gyroData > 340)
								dv = 200;
							if(gyroData > 360)
								dv = 170;
							if(gyroData > 370)
								dv = 130;
							if(gyroData > 390)
								dv = 110;
							if(gyroData > 410)
								dv = 100;
							if(gyroData > 420)
								dv = 70;
							if(gyroData > 450)
								dv = 50;
							if(gyroData > 460)
								dv = 33;
							if(gyroData > 465)
								dv = 20;
							if(gyroData > 477)
								dv = 3;

							
							System.out.println(dv);
							System.out.println(gyroData);
							
							for (int i = 0; i < profiles.getLightCount(); i++) {
								try {

									profiles.getLight(i).setDimmerValue(dv);
									profiles.getLight(i).setColor(Color.WHITE);
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
						else
						{
							System.out.println("Panic");
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
	
	private void revertScene() {
		try {
			dmx.setDMX(sceneManager.getCurrentScene().getDmxVals());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(selectedLights != null) {
			for (FixtureProfile light : selectedLights) {
				light.syncLight();
			}
		}

	}
	
	private void clearSelected() {
		for (int i = 0; i < profiles.getLightCount(); i++) {
			profiles.getLight(i).setSelected(false);
		}
	}
	
	private void startRainbow() {
		rainbowVisual = new RainbowThread(selectedLights);
		rainbowVisual.start();
	}
	
	private void redrumRainbow() {
		try {
			rainbowVisual.redrum();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			// gonna die anyway
		}
		rainbowVisual = null;
	}
	
	private void startDMXVisual() {
		dmxVisual = new DMXVisualThread(selectedLights);
		dmxVisual.start();
	}
	
	private void redrumDMXVisual() {
		try {
			dmxVisual.redrum();
		} catch (InterruptedException e) {
			// gonna die anyway
		}
		
		dmxVisual = null;
	}
	
	private void startChase() {
		chase = new ChaseThread(chaseSceneDelay, sceneManager.getSceneArray(), dmx);
		chase.start();
	}
	
	private void redrumChase() {
		chase.redrum();
		try {
			chase.join();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.exit(-1);
		}
		chase = null;
	}
	
	private void startHighlight() {
		highlight = new HighlightThread();
		highlight.addLight(profiles.getLight(currentLightIndex));
		highlight.start();
	}
	
	private ArrayList<FixtureProfile> redrumHighlight() {
		ArrayList<FixtureProfile> temp;
		temp = highlight.redrum();
		highlight.interrupt();
		try {
			highlight.join();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		highlight = null;
		return temp;
	}
	
	private void startPresetVisual() {
		presetVisual = new PresetVisualThread(selectedLights);
		presetVisual.start();
	}
	
	private void redrumPresetVisual() {
		presetVisual.redrum();
		presetVisual.interrupt();
		try {
			presetVisual.join();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		presetVisual = null;
	}
	
	public void stopServer(){
		if(highlight != null){
			redrumHighlight();
		}
		
		if(presetVisual != null){
			redrumPresetVisual();
		}
		
		if(rainbowVisual != null) {
			redrumRainbow();
		}
		
		if(dmxVisual != null) {
			redrumDMXVisual();
		}
		
		if(udpListen != null)
			try {
				udpListen.redrum();
				udpListen.interrupt();
				udpListen.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		if(heartbeat != null)
			try {
				heartbeat.redrum();
				heartbeat.interrupt();
				heartbeat.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		if(serverSocket != null)
			serverSocket.close();


		
		/*try {
			dmx.clearDMX();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		serverRunning = false;
	}
	
	public FixtureManager getProfileManager(){
		return profiles;
	}
	
	public boolean isServerRunning()
	{
		return serverRunning;
		
	}
	
}
