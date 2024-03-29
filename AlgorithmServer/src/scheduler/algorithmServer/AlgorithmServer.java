package scheduler.algorithmServer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.zip.GZIPOutputStream;

import scheduler.events.Event;
import scheduler.geneticAlgorithm.Individual;
import scheduler.geneticAlgorithm.IndividualComparer;
import scheduler.utils.ArrayListUtils;
import scheduler.utils.DatabaseUtils;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.google.android.gcm.server.Sender;

public class AlgorithmServer extends Thread
{
	private static final String BUCKET_NAME = "completedSchedules";
	
	private String sqsURL;
	private AmazonSQSClient sqsClient;
	private AmazonS3 s3;
	Sender gcmSender;
	
	private ExecutorService threadPool;
	private int threads;
	private boolean running = true;
	private HashMap<Future<ArrayList<Individual>>, Message> futures = new HashMap<Future<ArrayList<Individual>>, Message>();
	private final HashMap<Integer, ArrayList<Individual>> writeQueue = new HashMap<Integer, ArrayList<Individual>>();
	private HashMap<Integer, String> gcmMap = new HashMap<Integer, String>();
	
	private void initSQS()
	{
		AWSCredentials credentials = new ClasspathPropertiesFileCredentialsProvider("awsAccess.properties").getCredentials();
		sqsClient = new AmazonSQSClient( credentials );
		GetQueueUrlRequest urlRequest = new GetQueueUrlRequest("schedulerQueue");
		GetQueueUrlResult res =  sqsClient.getQueueUrl(urlRequest);
		sqsURL = res.getQueueUrl();
	}
	
	private void initS3()
	{
		AWSCredentials credentials = new ClasspathPropertiesFileCredentialsProvider("awsAccess.properties").getCredentials();
		s3 = new AmazonS3Client(credentials);
	}
	
	@Override
	public void run()
	{
		//Get messages until told to shutdown
		while(running)
		{
			//Get a new message
			if(futures.size() < threads)
			{
				ReceiveMessageRequest msgReq = new ReceiveMessageRequest(sqsURL);
				ReceiveMessageResult res = sqsClient.receiveMessage(msgReq);
				
				//Process each message for user UIDs
				for(Message msg : res.getMessages())
				{
					System.out.println("Message Received");
					
					String msgBody = msg.getBody();
					int idx = msgBody.indexOf('\n');
					String userID = msgBody.substring(0, idx);
					String regID = msgBody.substring(idx + 1);
					
					int uid = Integer.parseInt(userID);
					
					gcmMap.put(uid, regID);
					
					//Create and submit the task
					Callable<ArrayList<Individual>> thr = new GeneticAlgorithmThread(uid);
					Future<ArrayList<Individual>> future = threadPool.submit(thr);
					futures.put(future, msg);
				}
				
			}
			else
			{
				//Make sure the listener thread doesn't spin
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			//Check the futures for returns
			ArrayList<Future<ArrayList<Individual>>> removed = new ArrayList<Future<ArrayList<Individual>>>();
			for(Future<ArrayList<Individual>> callback : futures.keySet())
			{
				if(callback.isDone())
				{
					removed.add(callback);
					
					//Skip if the callback was cancelled instead of completed
					if(callback.isCancelled()) continue;
					
					ArrayList<Individual> population;
					try
					{
						population = callback.get();
						
						String msgBody = futures.get(callback).getBody();
						int idx = msgBody.indexOf('\n');
						String userID = msgBody.substring(0, idx);
						String regID = msgBody.substring(idx + 1);
						
						int uid = Integer.parseInt(userID);
						
						ConvertPopulationToSchedule(uid, population);
						
						//Remove from SQS
						DeleteMessageRequest delReq = new DeleteMessageRequest(sqsURL, futures.get(callback).getReceiptHandle());
						sqsClient.deleteMessage(delReq);
						
					} catch (InterruptedException | ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}//end for
			
			//Remove the completed callbacks
			for(Future<ArrayList<Individual>> callback : removed)
			{
				futures.remove(callback);
			}
			
		}
	}
	
	/**
	 * Takes the population and saves the events into a compressed file
	 * @param population The population provided by the genetic algorithm
	 */
	private void ConvertPopulationToSchedule(int UID, ArrayList<Individual> population)
	{
		synchronized(writeQueue)
		{
			Collections.sort(population, new IndividualComparer());
			ArrayList<Event> events = population.get(0).getEvents();
			for(Event ev : events)
			{
				Date d = new Date(ev.getStartTime() * 1000);
				System.out.println("CPS: " + d.toString());
			}
			
			writeQueue.put(UID, population);
			writeQueue.notify();
		}
	}
	
	private void writeToS3(String key, byte[] data)
	{
		PutObjectResult result;
		
		try
		{
			ObjectMetadata metaData = new ObjectMetadata();
			
			metaData.setContentLength(data.length);
			ByteArrayInputStream inFile = new ByteArrayInputStream(data);
			
			PutObjectRequest req = new PutObjectRequest(BUCKET_NAME, key, inFile, metaData);
			result = s3.putObject(req);
			
			result.toString();
		}catch(Exception e){e.printStackTrace();}//LAZY
	}
	
	private void startS3Thread()
	{
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				while(running)
				{
					synchronized(writeQueue)
					{
						//Wait for schedules to write
						while(writeQueue.size() == 0)
						{
							try
							{
								writeQueue.wait();
							}catch(InterruptedException e)
							{
								System.err.println("S3 thread 'wait' interrupted");
							}
						}
						
						//Get the population
						int uid = writeQueue.keySet().iterator().next();
						ArrayList<Individual> population = writeQueue.remove(uid); 
						
						try
						{
							//Compress list of events
							ByteArrayOutputStream binFile = new ByteArrayOutputStream();
							GZIPOutputStream os = new GZIPOutputStream(binFile);
							
							//Holds an int for ArrayList lengths
							byte[] lengthArr = new byte[4];
							
							ByteBuffer byteBuffer = ByteBuffer.wrap(lengthArr);
							byteBuffer.putInt(population.size());
							os.write(lengthArr);
							
							//Write each event from each population
							for(int i = 0; i < population.size(); i++)
							{
								//write the size
								byteBuffer.rewind();
								byteBuffer.putInt(population.get(i).getEvents().size());
								
								os.write(lengthArr);
								for(Event ev : population.get(i).getEvents())
								{
									if(i == 0)
									{
										Date d = new Date(ev.getStartTime());
										System.out.println("CPS: " + d.toString());
									}
									os.write(Event.writeToBuffer(ev));
								}
							}
							
							os.close();
							
							//Write to S3			
							writeToS3(Integer.toString(uid), binFile.toByteArray());
							
							//Alert the user
							com.google.android.gcm.server.Message message = 
									new com.google.android.gcm.server.Message.Builder().build();
							gcmSender.send(message, gcmMap.get(uid), 5);
							
						} catch(IOException e){e.printStackTrace();}
					}
				}
				
			}
		}).start();
	}
	
	public AlgorithmServer(int threads)
	{
		this.threads = threads;
		
		initSQS();
		initS3();
		gcmSender = new Sender("AIzaSyDe2-Ars_8EGVwkLU2xDzRWHvB046tKUkw");
		
		startS3Thread();
		threadPool = Executors.newFixedThreadPool(threads);
	}
	
	public static void main(String[] args)
	{
		
		int threads = Integer.parseInt(args[0]);
		
		System.out.println("Setting up database");
		DatabaseUtils.init();
		
		System.out.println("Creating Server");
		AlgorithmServer srv = new AlgorithmServer(threads);
		
		System.out.println("Starting Server");
		srv.start();
		
		//Wait as long as the server is alive
		while(srv.isAlive())
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
}
