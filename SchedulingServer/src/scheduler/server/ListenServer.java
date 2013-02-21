package scheduler.server;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;

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
