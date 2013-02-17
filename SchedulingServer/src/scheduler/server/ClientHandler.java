package scheduler.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

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
		
		byte[] buff = new byte[2048];
		
		//Read while the client is connected
		while(clientConn.isConnected())
		{
			try
			{
				int msgSize = istream.read();
				istream.read(buff, 0, msgSize);
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
