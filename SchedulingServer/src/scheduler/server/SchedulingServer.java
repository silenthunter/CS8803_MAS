package scheduler.server;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;

import scheduler.events.Event;
import scheduler.geneticAlgorithm.GeneticAlgorithm;
import scheduler.utils.DatabaseUtils;

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
		//DatabaseUtils.init();
		
		/*ArrayList<Event> events = new ArrayList<Event>();//DatabaseUtils.getEventsForUser(1);
		Date d = new Date();
		
		for(int i = 0; i < 50; i++)
		{
			Event ev = new Event(d.getTime() / 1000, 60);
			events.add(ev);
		}
		
		GeneticAlgorithm alg = new GeneticAlgorithm(events);
		alg.compute(100, 0, 50000, 0);*/
		
		//ListenServer srv = new ListenServer(3000);
		
		SchedulingServer.initSQS();
		SchedulingServer.writeToSQS("Test Message");
	}

}
