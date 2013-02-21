package scheduler.algorithmServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.zip.GZIPOutputStream;

import scheduler.events.Event;
import scheduler.geneticAlgorithm.Individual;
import scheduler.utils.DatabaseUtils;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;

public class AlgorithmServer extends Thread
{
	
	private String sqsURL;
	private AmazonSQSClient sqsClient;
	
	private ExecutorService threadPool;
	private int threads;
	private boolean running = true;
	private HashMap<Future<ArrayList<Individual>>, String> futures = new HashMap<Future<ArrayList<Individual>>, String>();
	
	private void initSQS()
	{
		AWSCredentials credentials = new ClasspathPropertiesFileCredentialsProvider("awsAccess.properties").getCredentials();
		sqsClient = new AmazonSQSClient( credentials );
		GetQueueUrlRequest urlRequest = new GetQueueUrlRequest("schedulerQueue");
		GetQueueUrlResult res =  sqsClient.getQueueUrl(urlRequest);
		sqsURL = res.getQueueUrl();
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
					int uid = Integer.parseInt(msg.getBody());
					
					//Create and submit the task
					Callable<ArrayList<Individual>> thr = new GeneticAlgorithmThread(uid);
					Future<ArrayList<Individual>> future = threadPool.submit(thr);
					futures.put(future, msg.getReceiptHandle());
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
						
						ConvertPopulationToSchedule(population);
						
						//Remove from SQS
						DeleteMessageRequest delReq = new DeleteMessageRequest(sqsURL, futures.get(callback));
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
	private void ConvertPopulationToSchedule(ArrayList<Individual> population)
	{
		try
		{
			ByteArrayOutputStream binFile = new ByteArrayOutputStream();
			GZIPOutputStream os = new GZIPOutputStream(binFile);
			
			//Write each event from the best set of events
			for(Event ev : population.get(0).getEvents())
			{
				os.write(Event.WriteToBuffer(ev));
			}
		} catch(IOException e){}
	}
	
	public AlgorithmServer(int threads)
	{
		this.threads = threads;
		
		initSQS();
		threadPool = Executors.newFixedThreadPool(threads);
	}
	
	public static void main(String[] args)
	{
		int threads = Integer.parseInt(args[0]);
		
		DatabaseUtils.init();
		AlgorithmServer srv = new AlgorithmServer(threads);
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
