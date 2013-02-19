package scheduler.geneticAlgorithm;

import java.util.ArrayList;

import scheduler.events.Event;

public class Individual
{
	ArrayList<Event> events = new ArrayList<Event>();
	int fitness = 0;
	
	public Individual(ArrayList<Event> events)
	{
		//Copy events
		for(Event ev : events)
		{
			Event myEv = ev.copy();
			this.events.add(myEv);
		}
	}
	
	public Individual(Individual parent1, Individual parent2)
	{
		//TODO: Crossover constructor
	}
	
	public void computeFitness()
	{
		//TODO: compute fitness
	}
	
	
}
