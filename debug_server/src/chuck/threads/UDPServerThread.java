package chuck.threads;

import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import chuck.WirelessCommand;
import chuck.defines.*;

public class UDPServerThread extends Thread {
	
	private DatagramSocket server;
	private ConcurrentLinkedQueue<WirelessCommand> commandQ;
	
	public UDPServerThread(DatagramSocket server, ConcurrentLinkedQueue<WirelessCommand> commandQ) {
		this.server = server;
		this.commandQ = commandQ;
	}
	
	public void run() {
		byte[] receiveData = new byte[1024];
		// byte[] sendData = new byte[1024];

		System.out.println("HERE");

		while (true) {
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			try {
				server.receive(receivePacket);
			} catch (IOException ex) {
				//throw new InterruptedException("Error receiving data packet");
				ex.printStackTrace();
				System.exit(-1);
			}
			
			if (verifyPacket(receivePacket.getData())) {
				// process user information
				switch (receivePacket.getData()[8]) {
				case Connection.DATA_PACKET_ID:
					System.out.println(receivePacket.getData()[11]);
					break;
				case Connection.POLL_PACKET_ID:
					System.out.println("Poll Packet");
					break;
				case Connection.POLL_REPLY_PACKET_ID:
					System.out.println("Poll Reply Packet");
					break;
				default:
					System.out.println("Not a valid packet type");
					break;
				}
				commandQ.offer(new WirelessCommand());
			} else {
				System.out.println("Invalid Packet");
			}
			/*
			 * receivePacket.getData(); String sentence = new String(
			 * receivePacket.getData()); System.out.println("RECEIVED: " + sentence);
			 * InetAddress IPAddress = receivePacket.getAddress(); int port =
			 * receivePacket.getPort(); String capitalizedSentence = sentence.toUpperCase();
			 * sendData = capitalizedSentence.getBytes(); DatagramPacket sendPacket = new
			 * DatagramPacket(sendData, sendData.length, IPAddress, port);
			 * serverSocket.send(sendPacket);
			 */
		}
	}

	private static boolean verifyPacket(byte[] buffer) // saftey
	{
		/*
		 * for (int i = 0; i < Connection.ID.length; i++) { if(buffer[i] !=
		 * Connection.ID[i]) return false; }
		 */

		return true;
	}
}