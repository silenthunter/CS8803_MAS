package scheduler.algorithmServer;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import scheduler.events.Event;
import scheduler.geneticAlgorithm.GeneticAlgorithm;
import scheduler.geneticAlgorithm.Individual;
import scheduler.utils.DatabaseUtils;

public class GeneticAlgorithmThread implements Callable<ArrayList<Individual>>
{
	final static int POPULATION_SIZE = 100;
	final static int MIN_GENERATIONS = 200;
	final static int MAX_GENERATIONS = 50000;
	final static int GOAL_FITNESS = 0;
	
	private int userUID;
	private GeneticAlgorithm algorithm;
	
	public GeneticAlgorithmThread(int userUID)
	{
		this.userUID = userUID;
		
		ArrayList<Event> events = getUserEventsFromDB();
		algorithm = new GeneticAlgorithm(events);
	}
	
	private ArrayList<Event> getUserEventsFromDB()
	{
		ArrayList<Event> retn = DatabaseUtils.getEventsForUser(userUID);
		return retn;
	}
	
	@Override
	public ArrayList<Individual> call() throws Exception
	{
		algorithm.compute(POPULATION_SIZE, MIN_GENERATIONS, MAX_GENERATIONS, GOAL_FITNESS);
		
		return algorithm.getPopulation();
	}
}
