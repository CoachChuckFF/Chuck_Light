package chuck;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.ConcurrentLinkedQueue;

import chuck.defines.Connection;
import chuck.drivers.DMXDriver;
import chuck.threads.HeartBeatThread;
import chuck.threads.UDPServerThread;

public class ServerApp {

	private DMXDriver dmx;
	private DatagramSocket serverSocket;
	
	private HeartBeatThread heartbeat;
	private UDPServerThread udpListen;
	
	private ConcurrentLinkedQueue<WirelessCommand> commandQ;
	
	public static void main(String[] args) {
		ServerApp serv = new ServerApp();
		serv.startServer();
	}

	public void startServer() {
		// instantiate dmx driver
		try {
			dmx = new DMXDriver();
		} catch (IOException ex) {
			ex.printStackTrace();
			System.exit(-1);
		}
		// instantiate server socket
		try {
			serverSocket = new DatagramSocket(Connection.DMX_PORT);
		} catch (SocketException ex) {
			ex.printStackTrace();
			System.exit(-1);
		}
		
		// start heartbeat thread
		heartbeat = new HeartBeatThread(serverSocket);
		heartbeat.start();
		// start udp listener thread
		udpListen = new UDPServerThread(serverSocket, commandQ);
		udpListen.start();
		
		// for testing, set to some random color
		try {
			dmx.setDMX(2, 128, 125);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.exit(-1);
		}
		
		WirelessCommand currCommand = null;
		boolean on = false;
		while (true) {
			currCommand = commandQ.poll();
			if (currCommand == null) {
				continue;
			}
			
			try {
				if (on) {
					dmx.setDMX(1, 0);
				} else {
					dmx.setDMX(1, 255);
				}
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}
}
