package scheduler.comms;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import scheduler.events.Event;
import scheduler.messaging.Message;
import scheduler.messaging.MessageType;

public class MessageSender
{
	private int port;
	private String address;
	
	private Socket socket;
	private InputStream inStream;
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
			inStream = socket.getInputStream();
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
		
		//Retrieve schedule
		requestSchedule(userID);
		
		try
		{
			byte[] msgLength = new byte[4];
			inStream.read(msgLength);
			ByteBuffer byteBuffer = ByteBuffer.wrap(msgLength);
			
			int msgSize = byteBuffer.getInt();
			
			byte[] msgBuff = new byte[msgSize];
			
			int totalRead = 0;
			int read = 0;
			do
			{
				read = inStream.read(msgBuff, totalRead, msgSize - totalRead);
				totalRead += read;
			} while(read > 0);
			Message msg = Message.readFromBuffer(msgBuff);
			
			retn = getEventsFromMessage(msg);
			
		}
		catch(IOException e)
		{
			//TODO: Actual error catching
			e.printStackTrace();
		}
		
		return retn;
	}
	
	private ArrayList<ArrayList<Event>> getEventsFromMessage(Message msg)
	{
		ArrayList<ArrayList<Event>> schedule = new ArrayList<ArrayList<Event>>();
		
		try
		{
		ByteArrayInputStream bin = new ByteArrayInputStream(msg.getData());
		GZIPInputStream inputStream = new GZIPInputStream(bin);
		
		//TODO: Dynamic array
		byte[] buffer = new byte[100000000];
		
		int readSoFar = 0;
		int read = 0;
		
		do
		{
			read = inputStream.read(buffer, readSoFar, 10000);
			readSoFar += read;
		} while(read > 0);
		
		ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
		
		int offset = 4;
		int numSchedule = byteBuffer.getInt(0);
		for(int i = 0; i < numSchedule; i++)
		{
			int numEvents = byteBuffer.getInt(offset);
			offset += 4;
			ArrayList<Event> events = new ArrayList<Event>();
			
			for(int j = 0; j < numEvents; j++)
			{
				//TODO: Make variable size
				
				Event newEvent = Event.readFromBuffer(buffer, offset, Event.MAX_SIZE);
				events.add(newEvent);
				
				offset += Event.MAX_SIZE;
			}
			
			schedule.add(events);
		}
		}
		catch(IOException e)
		{
			//TODO: Real error handling
			e.printStackTrace();
		}
		
		return schedule;
	}
	
	private void requestSchedule(int userID)
	{
		Message msg = new Message(MessageType.REQUEST_SCHEDULE, userID, new byte[]{});
		writeMessage(msg);
	}
	
	public void createSchedule(int userID)
	{
		Message msg = new Message(MessageType.CREATE_SCHEDULE, userID, new byte[]{});
		writeMessage(msg);
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
