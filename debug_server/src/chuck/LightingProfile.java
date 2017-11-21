package chuck;

/**
 * Chuck Lighting Profile Class
 *
 * @author Christian Krueger
 */
public class LightingProfile implements Comparable<LightingProfile>{

	  /* fields */
	  private String name;
	  private int address; //what address the fixture is set at
	  private int channels; //how many channels the fixture has

	  /*What channel these functions are on, on the fixture
	   * 0 if the fixture does not have the color
	   * 1 indexed
	   * */
	  private int dimmer;
	  private int red;
	  private int green;
	  private int blue;
	  private int amber;
	  private int white;
	  private int strobe;
	  private int zoom;
	  private int pan;
	  private int pan_fine;
	  private int tilt;
	  private int tilt_fine;

	  /* constructor */
	  public LightingProfile() {

		  }

	  public LightingProfile(String name, int address, int channels) {
	    this.name = name;
	    this.address = address;
	    this.channels = channels;
	  }

	  /* accessors */
	  public String getName() { return this.name; }
	  public void setName(String name) { this.name = name;}

	  public int getAddress() { return this.address; }
	  public void setAddress(int address) { this.address = address;}

	  public int getChannels() { return this.channels; }
	  public void setChannels(int channels) { this.channels = channels;}

	  public int getDimmer() { return this.dimmer; }
	  public void setDimmer(int dimmer) { this.dimmer = dimmer;}

	  public int getRed() { return this.red; }
	  public void setRed(int red) { this.red = red;}

	  public int getGreen() { return this.green; }
	  public void setGreen(int green) { this.green = green;}

	  public int getBlue() { return this.blue; }
	  public void setBlue(int blue) { this.blue = blue;}

	  public int getAmber() { return this.amber; }
	  public void setAmber(int amber) { this.amber = amber;}

	  public int getWhite() { return this.white; }
	  public void setWhite(int white) { this.white = white;}

	  public int getStrobe() { return this.strobe; }
	  public void setStrobe(int strobe) { this.strobe = strobe;}

	  public int getZoom() { return this.zoom; }
	  public void setZoom(int zoom) { this.zoom = zoom;}

	  public int getPan() { return this.pan; }
	  public void setPan(int pan) { this.pan = pan;}

	  public int getPanFine() { return this.pan_fine; }
	  public void setPanFine(int pan_fine) { this.pan_fine = pan_fine;}

	  public int getTilt() { return this.tilt; }
	  public void setTilt(int tilt) { this.tilt = tilt;}

	  public int getTiltFine() { return this.tilt_fine; }
	  public void setTiltFine(int tilt_fine) { this.tilt_fine = tilt_fine;}

	public int compareTo(LightingProfile light) {

		return this.address - light.getAddress();
	}

	public String toString() {
		String light = "";
		light +="----- " + this.name + " -----\n";
		light +="1. Address: " + this.address + "\n";
		light +="2. Channels: " + this.channels + "\n";
		light +="3. Dimmer: " + this.dimmer + "\n";
		light +="4. Red: " + this.red + "\n";
		light +="5. Green: " + this.green + "\n";
		light +="6. Blue: " + this.blue + "\n";
		light +="7. Amber: " + this.amber + "\n";
		light +="8. White: " + this.white + "\n";
		light +="9. Strobe: " + this.strobe + "\n";
		light +="10. Zoom: " + this.zoom + "\n";
		light +="11. Pan: " + this.pan + "\n";
		light +="12. Pan Fine: " + this.pan_fine + "\n";
		light +="13. Tilt: " + this.tilt + "\n";
		light +="14. Tilt Fine: " + this.tilt_fine + "\n";
		light +="-------------" + "\n";
		return light;
	}

}
