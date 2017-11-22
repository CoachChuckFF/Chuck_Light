package chuck.threads;

import java.io.*;
import java.net.*;
import java.util.concurrent.BlockingQueue;

import chuck.WirelessCommand;

/**
 * Listens for UDP packets from the wireless controller and inserts them into queue for processing.
 * 
 * @author Christian Krueger and Joseph Eichenhofer
 *
 */
public class UDPServerThread extends Thread {

	private DatagramSocket server;
	private BlockingQueue<WirelessCommand> commandQ;
	private boolean running = false;

	/**
	 * Constructor. Specify the shared socket and shared queue.
	 * 
	 * @param server	shared UDP socket for server application
	 * @param commandQ	shared synchronized queue for communicating commands to processing thread
	 */
	public UDPServerThread(DatagramSocket server, BlockingQueue<WirelessCommand> commandQ) {
		this.server = server;
		this.commandQ = commandQ;
	}

	/**
	 * Waits for UDP packets on the port specified by the datagramsocket from constructor.
	 * 
	 * Creates a WirelessCommand object (without parsing the data) into the shared queue
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		byte[] receiveData = new byte[1024];

		while (running) {
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			try {
				server.receive(receivePacket);
			} catch (IOException ex) {
				// treat ioexception as fatal for now;
				ex.printStackTrace();
				System.exit(-1);
			}

			try {
				commandQ.put(new WirelessCommand(receivePacket.getData()));
			} catch (InterruptedException ex) {
				ex.printStackTrace();
				System.exit(-1);
			}

			/*
			 * if (verifyPacket(receivePacket.getData())) { // process user information
			 * switch (receivePacket.getData()[8]) { case Connection.DATA_PACKET_ID:
			 * System.out.println(receivePacket.getData()[11]); break; case
			 * Connection.POLL_PACKET_ID: System.out.println("Poll Packet"); break; case
			 * Connection.POLL_REPLY_PACKET_ID: System.out.println("Poll Reply Packet");
			 * break; default: System.out.println("Not a valid packet type"); break; }
			 * 
			 * } else { System.out.println("Invalid Packet"); }
			 */

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
	
	public void redrum()
	{
		running = false;
	}

}