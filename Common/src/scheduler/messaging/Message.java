package scheduler.messaging;

import java.nio.ByteBuffer;

public class Message
{
	private MessageType type;
	private int userID;
	
	private int dataLength;
	private byte[] data;
	
	public Message() {}
	
	public Message(MessageType type, int userID, byte[] data)
	{
		this.type = type;
		this.userID = userID;
		this.data = data.clone();
		this.dataLength = data.length;
	}
	
	public static Message readFromBuffer(byte[] arr)
	{
		ByteBuffer byteBuffer = ByteBuffer.wrap(arr);
		
		Message retn = new Message();
		retn.type = MessageType.values()[byteBuffer.getInt()];
		retn.userID = byteBuffer.getInt();
		retn.dataLength = byteBuffer.getInt();
		byteBuffer.get(retn.data, 0, retn.dataLength);
		
		return retn;
	}
	
	/**
	 * Writes a message to a byte array
	 * @param msg The message to convert
	 * @return A byte array representing the message passed in
	 */
	public static byte[] writeToBuffer(Message msg)
	{
		//The size of the data buffer + the size of the 3 fields
		int totalLength = 12 + msg.dataLength;
		
		byte[] retn = new byte[totalLength];
		ByteBuffer byteBuffer = ByteBuffer.wrap(retn);
		
		byteBuffer.putInt(msg.type.ordinal());
		byteBuffer.putInt(msg.userID);
		byteBuffer.putInt(msg.dataLength);
		
		byteBuffer.put(msg.data);
		
		return retn;
	}
}
