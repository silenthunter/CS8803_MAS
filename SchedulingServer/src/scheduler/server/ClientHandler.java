package scheduler.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

import scheduler.events.Event;
import scheduler.messaging.Message;
import scheduler.messaging.MessageType;
import scheduler.utils.DatabaseUtils;

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
		
		int messagesRead = 0;
		
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
				
				//Read data until the buffer is full
				int offset = 0;
				while(msgSize > 0)
				{
					int bytesRead = istream.read(buff, offset, msgSize);
					msgSize -= bytesRead;
					offset += bytesRead;
				}
				
				messagesRead++;
				
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
			
			//TODO: Batch these
			//Add events to the database
			for(Event event : events)
			{
				int eventID = DatabaseUtils.addEvent(event);
				DatabaseUtils.addUserToEvent(msg.getUserID(), eventID, 0);
			}
		}
		else if(msg.getType() == MessageType.CREATE_SCHEDULE)
		{
			SchedulingServer.writeToSQS(Integer.toString(msg.getUserID()));
		}
		else if(msg.getType() == MessageType.REQUEST_SCHEDULE)
		{
			byte[] fileData = getFileFromS3(msg.getUserID());			
			Message retnMsg = createMessageFromData(fileData);
			sendMessage(retnMsg);
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
	
	private byte[] getFileFromS3(int userID)
	{
		AWSCredentials credentials = new ClasspathPropertiesFileCredentialsProvider("awsAccess.properties").getCredentials();
		AmazonS3 s3 = new AmazonS3Client(credentials);
		
		GetObjectRequest req = new GetObjectRequest("completedSchedules", Integer.toString(userID));
		S3Object obj = s3.getObject(req);
		int size = (int)obj.getObjectMetadata().getContentLength();
		
		byte[] retn = new byte[size];
		
		//Read from the object
		S3ObjectInputStream stream = obj.getObjectContent();
		
		try {
			int totalRead = 0;
			int read = 0;
			do
			{
				read = stream.read(retn, totalRead, 10000);
				totalRead += read;
			} while(read > 0);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return retn;
		
	}
	
	private Message createMessageFromData(byte[] arr)
	{
		Message msg = new Message(MessageType.REQUEST_SCHEDULE, 0, arr);
		return msg;
	}
	
	private void sendMessage(Message msg)
	{
		try
		{
			OutputStream outStream = clientConn.getOutputStream();
			
			byte[] lengthArr = new byte[4];
			byte[] arr = Message.writeToBuffer(msg);
			
			ByteBuffer byteBuffer = ByteBuffer.wrap(lengthArr);
			byteBuffer.putInt(arr.length);
			
				outStream.write(lengthArr);
				outStream.write(arr);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
