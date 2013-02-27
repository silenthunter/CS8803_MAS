package scheduler.server;

import java.util.ArrayList;
import java.util.Date;

import scheduler.comms.MessageSender;
import scheduler.events.Event;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.GetQueueUrlRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;

public class SchedulingServer
{
	
	static String sqsURL;
	static AmazonSQSClient sqsClient;
	final static Object sqsLock = new Object();
	
	public static void initSQS()
	{
		AWSCredentials credentials = new ClasspathPropertiesFileCredentialsProvider("awsAccess.properties").getCredentials();
		sqsClient = new AmazonSQSClient( credentials );
		GetQueueUrlRequest urlRequest = new GetQueueUrlRequest("schedulerQueue");
		GetQueueUrlResult res =  sqsClient.getQueueUrl(urlRequest);
		sqsURL = res.getQueueUrl();
	}
	
	public static void writeToSQS(String message)
	{
		synchronized(sqsLock)
		{
			SendMessageRequest msgReq = new SendMessageRequest(sqsURL, message);
			sqsClient.sendMessage(msgReq);
		}
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		ListenServer srv = new ListenServer(8000);
		srv.start();
		
		MessageSender sender = new MessageSender("localhost", 8000);
		sender.connect();
		
		//DatabaseUtils.init();
		
		ArrayList<Event> events = new ArrayList<Event>();//DatabaseUtils.getEventsForUser(1);
		Date d = new Date();
		
		for(int i = 0; i < 50; i++)
		{
			Event ev = new Event(d.getTime() / 1000, 60);
			events.add(ev);
		}
		
		sender.addEvents(events, 1);
		
	}

}
