package chuck.threads;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;

import chuck.defines.*;
import chuck.drivers.DMXDriver;
import chuck.lighting.Scene;

public class ChaseThread extends Thread {
	private int sceneDelay; 
	private boolean running = false;
	private ArrayList<Scene> scenes;
	private DMXDriver dmx;

	public ChaseThread(int sceneDelay, ArrayList<Scene> scenes, DMXDriver dmx) {
		this.sceneDelay = sceneDelay;
		this.scenes = scenes;
		this.dmx = dmx;
	}


	public void run() {
		running = true;
		
		if(scenes.size() <= 1){
			System.out.println("Can't play a chase with " + scenes.size() + "scenes");
			return;
		}
		
		while(running){
			for (Scene scene : scenes) {
				if(!running)
					break;
				System.out.println(Arrays.toString(scene.getDmxVals()));
				//dmx.setDMX(scene.getDmxVals());
				try {
					Thread.sleep(sceneDelay);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
	
	public void redrum()
	{
		running = false;
	}
	
	public void setSceneDelay(int sceneDelay){
		this.sceneDelay = sceneDelay;
	}

}