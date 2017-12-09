package chuck.threads;

import java.io.*;
import java.util.ArrayList;
import chuck.lighting.FixtureProfile;
import chuck.defines.*;

public class DMXVisualThread extends Thread {
	private boolean running = false;
	private ArrayList<FixtureProfile> lights;
	
	public DMXVisualThread(ArrayList<FixtureProfile> lights) {
		this.lights = lights;
	}


	public void run() {
		int direction = -1 * LightingDefines.HIGHLIGHT_DIMMER_STEP;
		int dimmerValue = 255;

		running = true;
		while(running){

			for (FixtureProfile light: lights) {
				if(!running)
					break;
				try {
					light.setWhite(255);

					light.setDimmerValue(dimmerValue +=(direction));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if(direction == -1 * LightingDefines.HIGHLIGHT_DIMMER_STEP && dimmerValue < LightingDefines.HIGHLIGHT_DIMMER_LOW_VAL){
				direction = LightingDefines.HIGHLIGHT_DIMMER_STEP;
			} else if (direction == LightingDefines.HIGHLIGHT_DIMMER_STEP && dimmerValue >= LightingDefines.HIGHLIGHT_DIMMER_HIGH_VAL){
				direction = -1 * LightingDefines.HIGHLIGHT_DIMMER_STEP;
			}
			
			try {
				Thread.sleep(LightingDefines.DMX_VISUAL_DELAY);
			} catch (InterruptedException e) {
				continue;
			}
		}

	}

	public void redrum() throws InterruptedException
	{
		running = false;
		this.interrupt();
		this.join();
	}


}
