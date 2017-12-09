package chuck.lighting;

public class Scene {
	
	private int[] dmxVals;
	
	public Scene(int[] dmxVals){
		this.dmxVals = dmxVals.clone();
	}
	
	public int[] getDmxVals(){
		return this.dmxVals;
	}
	
	public void setDmxVals(int[] dmxVals){
		this.dmxVals = dmxVals.clone();
	}
}
