package chuck.threads;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import chuck.LightingProfile;
import chuck.defines.*;

public class PresetVisualThread extends Thread {
	private boolean running = false;
	Semaphore semaphore = null;
	private ArrayList<LightingProfile> lights;
	
	public PresetVisualThread(ArrayList<LightingProfile> lights) {
		this.lights = lights;
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
			}
			
			try {
				Thread.sleep(LightingDefines.PRESET_VISUAL_DELAY);
			} catch (InterruptedException e) {
				continue;
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
