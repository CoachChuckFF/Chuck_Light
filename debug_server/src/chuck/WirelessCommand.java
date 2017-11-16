package chuck;

public class WirelessCommand {
	private byte[] receiveData;
	private boolean parsed = false;
	
	private byte id;
	
	public WirelessCommand(byte[] rawData) {
		receiveData = rawData.clone();
	}

	/*
	 * populate variables based on byte data
	 * 
	 * @returns true if the command is already parsed, or if the parsing completed successfully
	 *          false if the command could not be parsed
	 */
	public boolean parse() {
		if (parsed)
			return true;
		if (!verifyPacket())
			return false;
		
		id = receiveData[8];
		return parsed = true;
	}
	
	public byte getID() {
		return id;
	}

	private boolean verifyPacket() {
		/*
		 * for (int i = 0; i < Connection.ID.length; i++) { if(buffer[i] !=
		 * Connection.ID[i]) return false; }
		 */

		return true;
	}
}
