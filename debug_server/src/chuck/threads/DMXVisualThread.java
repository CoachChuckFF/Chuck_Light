package chuck.threads;

import java.awt.Color;
import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import chuck.LightingProfile;
import chuck.defines.*;

public class DMXVisualThread extends Thread {
	private boolean running = false;
	private ArrayList<LightingProfile> lights;
	
	public DMXVisualThread(ArrayList<LightingProfile> lights) {
		this.lights = lights;
	}


	public void run() {
		int i = 0;
		int direction = -1 * LightingDefines.HIGHLIGHT_DIMMER_STEP;
		int dimmerValue = 255;

		running = true;
		while(running){

			for (LightingProfile light: lights) {
				if(!running)
					break;
				try {
					if(light.getWhite() != -1)
					{
						light.setChannelManual(light.getWhite(), 255);;
					}
					else
					{
						light.setColor(Color.WHITE);
					}

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
