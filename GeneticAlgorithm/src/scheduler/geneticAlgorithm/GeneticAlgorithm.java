package scheduler.geneticAlgorithm;

import java.util.ArrayList;

import scheduler.events.Event;

public class GeneticAlgorithm
{
	private ArrayList<Event> events = new ArrayList<Event>();
	private ArrayList<Individual> population = new ArrayList<Individual>();
	
	public GeneticAlgorithm(ArrayList<Event> events)
	{
		//Copy events
		for(Event ev : events)
		{
			Event myEv = ev.copy();
			this.events.add(myEv);
		}
	}
	
	public void compute(int populationSize, int generations)
	{
		initPopulation(populationSize);
	}
	
	private void initPopulation(int populationSize)
	{
		for(int i = 0; i < populationSize; i++)
		{
			Individual ind = new Individual(events);
			population.add(ind);
		}
	}
}
