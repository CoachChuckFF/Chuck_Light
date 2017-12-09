package chuck.threads;

import java.io.*;
import java.util.ArrayList;

import chuck.defines.*;
import chuck.lighting.FixtureProfile;

public class PresetVisualThread extends Thread {
	private boolean running = false;
	private ArrayList<FixtureProfile> lights;
	
	public PresetVisualThread(ArrayList<FixtureProfile> lights) {
		this.lights = lights;
	}


	public void run() {
		int i = 0;

		running = true;
		while(running){

			for (FixtureProfile light: lights) {
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
