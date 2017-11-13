package chuck.threads;

import java.io.*;
import java.net.*;

import chuck.defines.*;

public class HeartBeatThread extends Thread {
	protected DatagramSocket server = null;
	
	public HeartBeatThread(DatagramSocket server)
	{
		this.server = server;
	}
	
	public void run() {
		
		DatagramPacket heartbeat;
		
		InetAddress address = null;
		try {
			address = InetAddress.getByName("255.255.255.255");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} //broadcast address
		
		byte[] data = new byte[10];
		System.arraycopy(Connection.ID, 0, data, 0, 8);
		data[8] = Connection.POLL_PACKET_ID;
		
		
		while(true)
		{
			//data[9] = getCurrentState();
			heartbeat = new DatagramPacket(data, data.length, address, Connection.DMX_PORT);
			try {
				server.send(heartbeat);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("Problem with sending");
			}
			System.out.println("Sent");
	        
	        try {
				Thread.sleep(7000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
