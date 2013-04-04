package scheduler.algorithmServer;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import scheduler.events.Event;
import scheduler.geneticAlgorithm.Individual;
import scheduler.priorityAlgorithm.PriorityAlgorithm;
import scheduler.utils.DatabaseUtils;

public class PriorityAlgorithmThread implements Callable<ArrayList<Individual>>{

	private int userUID;
	private PriorityAlgorithm algorithm;
	
	public PriorityAlgorithmThread(int userUID)
	{
		this.userUID = userUID;
		
		ArrayList<Event> events = getUserEventsFromDB();
		algorithm = new PriorityAlgorithm(events);
	}
	
	private ArrayList<Event> getUserEventsFromDB()
	{
		ArrayList<Event> retn = DatabaseUtils.getEventsForUser(userUID);
		return retn;
	}
	
	@Override
	public ArrayList<Individual> call() throws Exception {
		algorithm.compute();
		return algorithm.getPopulation();
	}

}
