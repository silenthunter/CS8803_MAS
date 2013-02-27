package scheduler.comms;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import scheduler.events.Event;
import scheduler.messaging.Message;
import scheduler.messaging.MessageType;

public class MessageSender
{
	private int port;
	private String address;
	
	private Socket socket;
	private OutputStream outStream;
	
	public MessageSender(String address, int port)
	{
		this.address = address;
		this.port = port;
	}
	
	public void connect()
	{
		try {
			socket = new Socket(address, port);
			outStream = socket.getOutputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void disconnect()
	{
		try {
			if(!socket.isClosed())
				socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void addEvents(List<Event> events, int userID)
	{
		int numEvents = events.size();
		int eventSize = 4;//An integer for the number of events + the size of each event
		for(int i = 0; i < numEvents; i++)
			eventSize += events.get(i).getSize();
		
		byte[] data = new byte[eventSize];
		ByteBuffer byteBuffer = ByteBuffer.wrap(data);
		
		byteBuffer.putInt(numEvents);
		for(Event event : events)
			byteBuffer.put(Event.writeToBuffer(event));
		
		Message msg = new Message(MessageType.ADD_EVENT, userID, data);
		writeMessage(msg);
	}
	
	/**
	 * Finds the latest schedule the server has produced for a user, if one exists
	 * @param userID The unique identifier of the user
	 * @return The possible schedules for a user if any are found, Null otherwise
	 */
	public ArrayList<ArrayList<Event>> getSchedules(int userID)
	{
		ArrayList<ArrayList<Event>> retn = new ArrayList<ArrayList<Event>>();
		
		//TODO: Retrieve and parse schedule
		
		return retn;
	}
	
	public void writeMessage(Message msg)
	{
		byte[] lengthArr = new byte[4];
		byte[] arr = Message.writeToBuffer(msg);
		
		ByteBuffer byteBuffer = ByteBuffer.wrap(lengthArr);
		byteBuffer.putInt(arr.length);
		
		try {
			outStream.write(lengthArr);
			outStream.write(arr);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
