package scheduler.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
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
		
		//Read while the client is connected
		while(clientConn.isConnected())
		{
			try
			{
				byte[] sizeArr = new byte[4];
				istream.read(sizeArr);
				ByteBuffer byteBuffer = ByteBuffer.wrap(sizeArr);
				
				int msgSize = byteBuffer.getInt();
				boolean nonZero = msgSize != 0;
				buff = new byte[msgSize];
				
				//Read data until the buffer is full
				int offset = 0;
				while(msgSize > 0)
				{
					int bytesRead = istream.read(buff, offset, msgSize);
					msgSize -= bytesRead;
					offset += bytesRead;
				}
				
				//Don't process blank messages
				if(nonZero)
					processMessage(buff);
				
				
			} 
			catch (SocketException e)
			{
				System.out.println("Socket Exception: Leaving Loop");
				break;
			}catch (IOException e) {
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
				DatabaseUtils.addUserToEvent(msg.getUserID(), eventID, event.getPriority());
			}
		}
		else if(msg.getType() == MessageType.CREATE_SCHEDULE)
		{
			String userID =Integer.toString(msg.getUserID());
			ByteBuffer buffer = ByteBuffer.wrap(msg.getData());
			
			int strLen = buffer.getInt();
			String retnID = "";
			
			for(int i = 0; i < strLen; i++)
				retnID += buffer.getChar();
			
			SchedulingServer.writeToSQS(userID, retnID);
		}
		else if(msg.getType() == MessageType.REQUEST_SCHEDULE)
		{
			byte[] fileData = getFileFromS3(msg.getUserID());			
			Message retnMsg = createMessageFromData(fileData);
			sendMessage(retnMsg);
		}
		else if(msg.getType() == MessageType.REMOVE_EVENT)
		{
			ArrayList<Integer> uids = new ArrayList<Integer>();
			ByteBuffer buffer = ByteBuffer.wrap(msg.getData());
			
			//Read the array from the msg data array
			for(int i = 0; i < msg.getDataLength(); i+=4)
			{
				int newInt = buffer.getInt();
				uids.add(newInt);
			}
			
			DatabaseUtils.deleteEvents(uids);
		}
		else if(msg.getType() == MessageType.MODIFY_EVENT)
		{
			ArrayList<Event> events = getEventsFromMessage(msg);
			DatabaseUtils.modifyEvents(events);
		}
	}
	
	private ArrayList<Event> getEventsFromMessage(Message msg)
	{
		ArrayList<Event> events = new ArrayList<Event>();
		
		//Don't continue on null events
		if(msg.getData() == null)
			return events;
		
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
	
	/**
	 * Takes a user's ID and attempts to retrieve a schedule from Amazon S3
	 * @param userID The user's ID
	 * @return An array of bytes for a schedule, NULL if an error occurs
	 */
	private byte[] getFileFromS3(int userID)
	{
		byte[] retn = null;
		
		try {
			AWSCredentials credentials = new ClasspathPropertiesFileCredentialsProvider("awsAccess.properties").getCredentials();
			AmazonS3 s3 = new AmazonS3Client(credentials);
			
			GetObjectRequest req = new GetObjectRequest("completedSchedules", Integer.toString(userID));
			S3Object obj = s3.getObject(req);
			int size = (int)obj.getObjectMetadata().getContentLength();
			
			retn = new byte[size];
			
			//Read from the object
			S3ObjectInputStream stream = obj.getObjectContent();
		
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
		} catch (AmazonS3Exception e)
		{
			System.err.println(e.getErrorCode() + " (Key: " + userID + ")");
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
