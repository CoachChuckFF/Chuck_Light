package chuck.threads;

import java.io.*;
import java.net.*;
import java.util.Arrays;

import chuck.defines.*;

class UDPServer
{
   public static void main(String args[]) throws Exception
  {
 	DatagramSocket serverSocket = new DatagramSocket(Connection.DMX_PORT);
    byte[] receiveData = new byte[1024];
    byte[] sendData = new byte[1024];
    
    HeartBeatThread heartbeat = new HeartBeatThread(serverSocket); //starts heartbeat thread
    heartbeat.start();
    
    System.out.println("HERE");
    
    while(true)
    {
      DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
      serverSocket.receive(receivePacket);
      if(verifyPacket(receivePacket.getData()))
      {
    	  //process user information
    	  switch(receivePacket.getData()[8])
    	  {
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
      }
      else
      {
    	  System.out.println("Invalid Packet");
      }
      /*receivePacket.getData();
      String sentence = new String( receivePacket.getData());
      System.out.println("RECEIVED: " + sentence);
      InetAddress IPAddress = receivePacket.getAddress();
      int port = receivePacket.getPort();
      String capitalizedSentence = sentence.toUpperCase();
      sendData = capitalizedSentence.getBytes();
      DatagramPacket sendPacket =
      new DatagramPacket(sendData, sendData.length, IPAddress, port);
      serverSocket.send(sendPacket);*/
    }
  }
   
   public static boolean verifyPacket(byte[] buffer) //saftey
   {/*
	   for (int i = 0; i < Connection.ID.length; i++) {
		   if(buffer[i] != Connection.ID[i])
			   return false;
	   }*/
	   
	return true;
   }
}