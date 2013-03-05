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
		
		//Don't try to copy a null array
		if(data != null)
		{
			this.data = data.clone();
			this.dataLength = data.length;
		}
		else
			this.dataLength = 0;
	}
	
	public static Message readFromBuffer(byte[] arr)
	{
		return readFromBuffer(arr, 0, arr.length);
	}
	
	public byte[] getData()
	{
		return data;
	}
	
	public MessageType getType()
	{
		return type;
	}
	
	public int getUserID()
	{
		return userID;
	}
	
	public int getDataLength()
	{
		return dataLength;
	}
	
	public static Message readFromBuffer(byte[] arr, int startIndex, int length)
	{
		ByteBuffer byteBuffer = ByteBuffer.wrap(arr, startIndex, length);
		
		Message retn = new Message();
		retn.type = MessageType.values()[byteBuffer.getInt()];
		retn.userID = byteBuffer.getInt();
		retn.dataLength = byteBuffer.getInt();
		if(retn.dataLength > 0)
		{
			retn.data = new byte[retn.dataLength];
			byteBuffer.get(retn.data, 0, retn.dataLength);
		}
		else
			retn.data = null;
		
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
		
		if(msg.data != null)
			byteBuffer.put(msg.data);
		
		return retn;
	}
}
