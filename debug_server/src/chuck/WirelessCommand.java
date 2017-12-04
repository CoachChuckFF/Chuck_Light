package chuck;

import java.net.InetAddress;
import java.util.Arrays;

import chuck.defines.Connection;
import chuck.defines.Modes;

public class WirelessCommand {
	private byte[] receiveData;
	private byte packetType;
	private byte mode;
	private boolean parsed = false;
	
	//general
	private InetAddress sender_ip;
	
	//Poll Reply Packet Specific
	private float batteryLevel;
	private byte errorCode;
	private byte messageLength;
	private String message;
	
	//Data Packet Specific
	private byte dataType;
	private byte userActionData; //LRUD, Buttons
	private int joystickData[];
	private int gyroData[];
	
	
	public WirelessCommand(byte[] rawData, InetAddress sender_ip) {
		receiveData = rawData.clone();
		this.sender_ip = sender_ip;
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
		
		//System.out.println(Arrays.toString(receiveData));
		
		switch(packetType){
		case Connection.DATA_PACKET_ID:
			dataType = receiveData[10];
			switch(dataType)
			{
			case Connection.USER_ACTION_DATA:
				userActionData = receiveData[11];
				break;
			case Connection.JOYSTIC_DATA:
				
				joystickData = new int[2];
				joystickData[0] = (receiveData[15] & 0xFF) << 24 | 
									(receiveData[14] & 0xFF) << 16 |
									(receiveData[13] & 0xFF) << 8 |
									(receiveData[12] & 0xFF); //X
				joystickData[1] = (receiveData[19] & 0xFF) << 24 | 
									(receiveData[18] & 0xFF) << 16 |
									(receiveData[17] & 0xFF) << 8 |
									(receiveData[16] & 0xFF); //Y
				break;
			case Connection.GYRO_DATA:
				gyroData = new int[3];
				//or just a byte of movement percentage?
				gyroData[0] = receiveData[14]; //X
				gyroData[1] = receiveData[15]; //Y
				gyroData[2] = receiveData[16]; //Z
				break;
			default:
				return parsed = false; //not a valid input packet
			}
			break;
		case Connection.POLL_REPLY_PACKET_ID:
			break;
		case Connection.POLL_PACKET_ID:
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
	public int[] getJoystickData(){
		return joystickData;
	}
	
	//returns null on error
	public int[] getGyroData(){
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
		if(receiveData[i] != Connection.ID[i]){
			return false; 
			}
		}

		return true;
	}

	public InetAddress getSender_ip() {
		return sender_ip;
	}
}
