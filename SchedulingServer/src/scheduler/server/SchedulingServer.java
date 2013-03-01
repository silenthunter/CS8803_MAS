package scheduler.server;

import scheduler.utils.DatabaseUtils;

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
		System.out.println("Initializing SQS");
		SchedulingServer.initSQS();
		
		System.out.println("Initializing Database Connection");
		DatabaseUtils.init();
		
		System.out.println("Creating Server");
		ListenServer srv = new ListenServer(8000);
		System.out.println("Starting Server");
		srv.start();
		
	}

}
