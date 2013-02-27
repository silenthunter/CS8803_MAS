package scheduler.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import scheduler.events.Event;
import scheduler.messaging.Message;
import scheduler.messaging.MessageType;

public class ClientHandler extends Thread
{
	private Socket clientConn;
	
	public ClientHandler(Socket clientConn)
	{
		this.clientConn = clientConn;
	}
	
	@Override
	public void run()
	{
		InputStream istream = null;
		
		try
		{
			istream = clientConn.getInputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//This really shouldn't happen
		if(istream == null)
		{
			System.err.println("Client InputStream not initialized");
			return;
		}
		
		byte[] buff;
		
		//Read while the client is connected
		while(clientConn.isConnected())
		{
			try
			{
				byte[] sizeArr = new byte[4];
				istream.read(sizeArr);
				ByteBuffer byteBuffer = ByteBuffer.wrap(sizeArr);
				
				int msgSize = byteBuffer.getInt();
				buff = new byte[msgSize];
				istream.read(buff, 0, msgSize);
				
				processMessage(buff);
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void processMessage(byte buff[])
	{
		Message msg = Message.readFromBuffer(buff);
		
		if(msg.getType() == MessageType.ADD_EVENT)
		{
			ArrayList<Event> events = getEventsFromMessage(msg);
		}
	}
	
	private ArrayList<Event> getEventsFromMessage(Message msg)
	{
		ArrayList<Event> events = new ArrayList<Event>();
		ByteBuffer byteBuffer = ByteBuffer.wrap(msg.getData());
		
		int offset = 4;
		int numEvents = byteBuffer.getInt();
		for(int i = 0; i < numEvents; i++)
		{
			//TODO: Make variable size
			
			Event newEvent = Event.readFromBuffer(msg.getData(), offset, Event.MAX_SIZE);
			events.add(newEvent);
			
			offset += Event.MAX_SIZE;
		}
		
		return events;
	}
}
