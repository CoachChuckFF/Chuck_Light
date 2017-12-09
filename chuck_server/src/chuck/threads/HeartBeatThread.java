package chuck.threads;

import java.io.*;
import java.net.*;

import chuck.defines.*;

public class HeartBeatThread extends Thread {
	private DatagramSocket server;
	private byte currentState;
	private byte[] data;
	private InetAddress address;
	private boolean running;
	private boolean connected;

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
		running = true;
		address = null;
		try {
			//change to not broadcast
			address = InetAddress.getByName("255.255.255.255");
		} catch (UnknownHostException ex) {
			// sending to broadcast, so unknown host is fatal error
			ex.printStackTrace();
			System.exit(-1);
		}

		data = new byte[10];
		System.arraycopy(Connection.ID, 0, data, 0, 8);
		data[8] = Connection.POLL_PACKET_ID;

		while (running) {

			sendHeartbeat();
			
			try {
				Thread.sleep(Connection.HEARTBEAT_INTERVAL);
			} catch (InterruptedException e) {
				continue;
			}
		}
	}
	
	public synchronized void sendHeartbeat(){
		
		if(running){
			data[9] = currentState;
			DatagramPacket heartbeat = new DatagramPacket(data, data.length, address, Connection.DMX_PORT);
		
			try {
				server.send(heartbeat);
			} catch (IOException e) {
				// io exception treated as fatal error
				e.printStackTrace();
				System.exit(-1);
			}
		}
		else
		{
			System.out.println("Server not Running");
		}
	}
	
	public void redrum()
	{
		running = false;
		connected = false;
	}
	
	public void setAddress(InetAddress address)
	{	
		this.address = address;
	}
	
	public void setConnected(boolean connected)
	{
		this.connected = connected;
	}
	
	public boolean getConnected()
	{
		return this.connected;
	}
	
	public void setCurrentState(byte currentState)
	{
		this.currentState = currentState;
	}
}
