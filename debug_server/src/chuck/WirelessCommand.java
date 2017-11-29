package chuck;

import chuck.defines.Connection;
import chuck.defines.Modes;

public class WirelessCommand {
	private byte[] receiveData;
	private byte packetType;
	private byte mode;
	private boolean parsed = false;
	
	//Poll Reply Packet Specific
	private float batteryLevel;
	private byte errorCode;
	private byte messageLength;
	private String message;
	
	//Data Packet Specific
	private byte dataType;
	private byte userActionData; //LRUD, Buttons
	private byte joystickData[];
	private byte gyroData[];
	
	
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
		
		packetType = receiveData[8];
		mode = receiveData[9];
		
		switch(packetType){
		case Connection.POLL_REPLY_PACKET_ID:
			switch(dataType = receiveData[10])
			{
			case Connection.USER_ACTION_DATA:
				userActionData = receiveData[11];
				break;
			case Connection.JOYSTIC_DATA:
				joystickData = new byte[2];
				joystickData[0] = receiveData[12]; //X
				joystickData[1] = receiveData[13]; //Y
				break;
			case Connection.GYRO_DATA:
				gyroData = new byte[2];
				//or just a byte of movement percentage?
				gyroData[0] = receiveData[14]; //X
				gyroData[1] = receiveData[15]; //Y
				gyroData[2] = receiveData[16]; //Z
				break;
			default:
				return parsed = false; //not a valid input packet
			}
			break;
		case Connection.DATA_PACKET_ID:
			break;
		default:
			return parsed = false; //not a valid input packet
		}
			
		
		return parsed = true;
	}
	
	public byte getPacketType() {
		return this.packetType;
	}
	
	public byte getDataType(){
		return this.dataType;
	}
	
	public byte getUserActionData(){
		return this.userActionData;
	}
	
	//returns null on error
	public byte[] getJoystickData(){
		return joystickData;
	}
	
	//returns null on error
	public byte[] getGyroData(){
		return gyroData;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("command type:");
		sb.append(String.format("%02x", packetType));
		
		return sb.toString();
	}
	
	private boolean verifyPacket() {
		for (int i = 0; i < Connection.ID.length; i++) {
		if(receiveData[i] != Connection.ID[i]) return false; }


		return true;
	}
}
