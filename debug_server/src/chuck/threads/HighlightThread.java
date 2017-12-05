package chuck.threads;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Semaphore;

import chuck.LightingProfile;
import chuck.defines.*;
import chuck.drivers.DMXDriver;
import chuck.lighting.Scene;

public class HighlightThread extends Thread {
	private boolean running = false;
	Semaphore semaphore = null;
	private ArrayList<LightingProfile> lights;
	private ArrayList<int[]> savedValues;
	private DMXDriver dmx;

	public HighlightThread(DMXDriver dmx) {
		this.lights = new ArrayList<LightingProfile>();
		this.savedValues = new ArrayList<int[]>();
		this.dmx = dmx;
		this.semaphore = new Semaphore(1);

	}


	public void run() {
		running = true;
		int direction = -1;
		int dimmerValue = 255;
		
		while(running){
			try {
				semaphore.acquire();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			for (LightingProfile light: lights) {
				if(!running)
					break;

				if(direction == -1 && dimmerValue < 100){
					direction = 1;
				} else if (direction == 1 && dimmerValue >= 255){
					direction = -1;
				}
				
				try {
					light.setDimmerValue(dimmerValue +=(direction));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				try {
					Thread.sleep(LightingDefines.HIGHLIGHT_VISUAL_DELAY);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			}
			
			semaphore.release();
			
		}
		
	}
	
	
	public void addLight(LightingProfile light){
		
		if(!lights.contains(light)){
			try {
				semaphore.acquire();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			lights.add(light);
			savedValues.add(light.getDMXVals());
			System.out.println(light.hasColor());
			System.out.println(light.getDefaultColorOffest());
			if(!light.hasColor()) {
				try {
					light.setChannelManual(light.getDefaultColorOffest(), 255);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.exit(-1);
				}	
			}

			semaphore.release();
		}
	}
	
	//restore prevous value
	public void removeLight(LightingProfile light){
		if(lights.contains(light))
		{
			try {
				semaphore.acquire();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			light.setDMXVals(savedValues.remove(lights.indexOf(light)));
			lights.remove(light);
			semaphore.release();
		}
	}
	
	public ArrayList<LightingProfile> redrum()
	{
		running = false;
		ArrayList<LightingProfile> temp = (ArrayList<LightingProfile>) lights.clone();
		while(lights.size() != 0)
		{
			removeLight(lights.get(0));
		}
		//return (ArrayList<LightingProfile>) lights.clone();
		return temp;
	}
	

}