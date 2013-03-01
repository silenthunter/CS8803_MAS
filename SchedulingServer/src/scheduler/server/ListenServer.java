package scheduler.server;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

public class ListenServer extends Thread
{
	
	private boolean isRunning = true;
	private int port;
	private ServerSocket serverSocket;
	private ArrayList<ClientHandler> clients = new ArrayList<ClientHandler>();
	
	/**
	 * Starts the listen server on the specified port
	 * @param port The port this server will listen on
	 */
	public ListenServer(int port)
	{
		this.port = port;
	}
	
	@Override
	public void run()
	{
		try {
			serverSocket = new ServerSocket(port);
			
			//Accept clients as long as the server is running
			while(isRunning)
			{
				Socket clientConn = serverSocket.accept();
				
				System.out.println("Client Connected");
				ClientHandler handler = new ClientHandler(clientConn);
				handler.start();
				
				clients.add(handler);
				
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
