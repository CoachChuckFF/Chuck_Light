package chuck.threads;

import java.io.*;
import java.net.*;
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
                  String sentence = new String( receivePacket.getData());
                  System.out.println("RECEIVED: " + sentence);
                  InetAddress IPAddress = receivePacket.getAddress();
                  int port = receivePacket.getPort();
                  String capitalizedSentence = sentence.toUpperCase();
                  sendData = capitalizedSentence.getBytes();
                  /*DatagramPacket sendPacket =
                  new DatagramPacket(sendData, sendData.length, IPAddress, port);
                  serverSocket.send(sendPacket);*/
               }
      }
}