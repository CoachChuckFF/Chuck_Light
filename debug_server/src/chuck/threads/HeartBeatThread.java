package chuck.threads;

import java.io.*;
import java.net.*;

import chuck.defines.*;

public class HeartBeatThread extends Thread {
	private DatagramSocket server;
	private byte currentState;
	private boolean running;

	/**
	 * Constructor. Set UDP socket for sending heartbeat packets.
	 * 
	 * @param server	Shared UDP server socket for sending to controller.
	 */
	public HeartBeatThread(DatagramSocket server, byte currentState) {
		this.server = server;
		this.currentState = currentState;
	}

	/**
	 * Broadcast a UDP packet every seven seconds to determine if controller is connected.
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		DatagramPacket heartbeat;
		running = true;
		InetAddress address = null;
		try {
			address = InetAddress.getByName("255.255.255.255");
		} catch (UnknownHostException ex) {
			// TODO: configure a specific IP address, maybe with some kind of handshake
			// sending to broadcast, so unknown host is fatal error
			ex.printStackTrace();
			System.exit(-1);
		}

		byte[] data = new byte[10];
		System.arraycopy(Connection.ID, 0, data, 0, 8);
		data[8] = Connection.POLL_PACKET_ID;

		while (running) {
			data[9] = currentState;
			heartbeat = new DatagramPacket(data, data.length, address, Connection.DMX_PORT);
			try {
				server.send(heartbeat);
			} catch (IOException e) {
				// io exception treated as fatal error
				e.printStackTrace();
				System.exit(-1);
			}
			
			try {
				Thread.sleep(7000);
			} catch (InterruptedException e) {
				// treating interrupted exception as fatal error
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}
	
	public void redrum()
	{
		running = false;
	}
}
