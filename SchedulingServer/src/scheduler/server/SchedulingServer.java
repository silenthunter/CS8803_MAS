package scheduler.server;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;

import scheduler.comms.MessageSender;
import scheduler.events.Event;
import scheduler.utils.DatabaseUtils;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
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
		SchedulingServer.initSQS();
		DatabaseUtils.init();
		
		ListenServer srv = new ListenServer(8000);
		srv.start();
		
	}

}
