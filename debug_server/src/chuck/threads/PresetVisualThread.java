package chuck.threads;

import java.awt.Color;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Semaphore;

import chuck.LightingProfile;
import chuck.defines.*;
import chuck.drivers.DMXDriver;
import chuck.lighting.Scene;

public class PresetVisualThread extends Thread {
	private boolean running = false;
	Semaphore semaphore = null;
	private ArrayList<LightingProfile> lights;
	private DMXDriver dmx;

	public PresetVisualThread(DMXDriver dmx, ArrayList<LightingProfile> lights) {
		this.lights = lights;
		this.dmx = dmx;

	}


	public void run() {
		int i = 0;

		running = true;
		while(running){

			for (LightingProfile light: lights) {
				if(!running)
					break;
				try {
					light.setColor(LightingDefines.PRESETS[i]);
					light.setDimmerValue(255);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				try {
					Thread.sleep(LightingDefines.PRESET_VISUAL_DELAY);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

			if(++i >= LightingDefines.PRESETS.length){
				i = 0;
			}

		}

	}

	public void redrum()
	{
		running = false;
	}


}
