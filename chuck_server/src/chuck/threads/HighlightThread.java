package chuck.threads;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import chuck.defines.*;
import chuck.lighting.FixtureProfile;

public class HighlightThread extends Thread {
	private boolean running = false;
	Semaphore semaphore = null;
	private ArrayList<FixtureProfile> lights;
	private ArrayList<int[]> savedValues;
	private boolean updateDefaultColor = false;

	public HighlightThread() {
		this.lights = new ArrayList<FixtureProfile>();
		this.savedValues = new ArrayList<int[]>();
		this.semaphore = new Semaphore(1);

	}

	public void run() {
		running = true;
		int direction = -1 * LightingDefines.HIGHLIGHT_DIMMER_STEP;
		int dimmerValue = 255;
		
		while(running){
			semaphore.acquireUninterruptibly();
			for (FixtureProfile light: lights) {
				if(!running)
					break;

				if(updateDefaultColor) {
					light.syncLight();
					if(!light.hasColor()) {
						try {
							light.setChannelManual(light.getDefaultColorOffest(), 255);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							System.exit(-1);
						}	
					}
				}
				
				if(direction == -1 * LightingDefines.HIGHLIGHT_DIMMER_STEP && dimmerValue < LightingDefines.HIGHLIGHT_DIMMER_LOW_VAL){
					direction = LightingDefines.HIGHLIGHT_DIMMER_STEP;
				} else if (direction == LightingDefines.HIGHLIGHT_DIMMER_STEP && dimmerValue >= LightingDefines.HIGHLIGHT_DIMMER_HIGH_VAL){
					direction = -1 * LightingDefines.HIGHLIGHT_DIMMER_STEP;
				}
				
				try {
					light.setDimmerValue(dimmerValue +=(direction));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			}
			
			updateDefaultColor = false;
			semaphore.release();
			
			try {
				Thread.sleep(LightingDefines.HIGHLIGHT_VISUAL_DELAY);
			} catch (InterruptedException e) {
				continue;
			}
			
		}
		
	}
	
	
	public void addLight(FixtureProfile light){
		
		if(!lights.contains(light)){
			semaphore.acquireUninterruptibly();
			lights.add(light);
			savedValues.add(light.getDMXVals());
			updateDefaultColor = true;
			semaphore.release();

		}
	}
	
	public void updateDefaultColor() {
		semaphore.acquireUninterruptibly();
		updateDefaultColor = true;
		semaphore.release();
	}
	
	//restore prevous value
	public void removeLight(FixtureProfile light){
		if(lights.contains(light))
		{
			semaphore.acquireUninterruptibly();
			try {
				light.setDMXVals(savedValues.remove(lights.indexOf(light)));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(-1);
			}
			lights.remove(light);
			semaphore.release();

		}
	}
	
	public ArrayList<FixtureProfile> redrum()
	{
		running = false;
		@SuppressWarnings("unchecked")
		ArrayList<FixtureProfile> temp = (ArrayList<FixtureProfile>) lights.clone();
		while(lights.size() != 0)
		{
			removeLight(lights.get(0));
		}
		//return (ArrayList<LightingProfile>) lights.clone();
		return temp;
	}
	
	public void clearHighlighted()
	{
		semaphore.acquireUninterruptibly();
			lights.clear();
			savedValues.clear();
		semaphore.release();
	}
	

}