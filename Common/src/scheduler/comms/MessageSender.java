package scheduler.comms;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import scheduler.messaging.Message;

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
	
	public void writeMessage(Message msg)
	{
		byte[] arr = Message.writeToBuffer(msg);
		
		try {
			outStream.write(arr);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
