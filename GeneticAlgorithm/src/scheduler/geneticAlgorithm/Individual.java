package scheduler.geneticAlgorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Random;

import scheduler.events.Event;
import scheduler.events.EventComparer;
import scheduler.utils.ArrayListUtils;

public class Individual
{
	static Random rand = new Random();
	
	ArrayList<Event> events = new ArrayList<Event>();
	int fitness = 0;
	
	public Individual(ArrayList<Event> events)
	{
		this.events = ArrayListUtils.copyList(events);
	}
	
	/**
	 * Creates a new individual by doing a crossover between the two parents provide
	 * @param parent1 The first parent to cross
	 * @param parent2 The second parent to cross
	 */
	public Individual(Individual parent1, Individual parent2)
	{
		if(parent1.events.size() != parent2.events.size())
		{
			System.err.println("Parents have arrays of different lengths!");
		}
		
		int num = rand.nextInt(100);
		
		if(num > 30)
		{
			//Select event from random parent
			for(int i = 0; i < parent1.events.size(); i++)
			{
				Event myEv = rand.nextInt(2) == 0 ? parent1.events.get(i).copy() : parent2.events.get(i).copy();
				this.events.add(myEv);
			}
		}
		else
		{
			//Random split point
			int idx = rand.nextInt(parent1.events.size());
			for(int i = 0; i < parent1.events.size(); i++)
			{
				Event myEv = i < idx ? parent1.events.get(i).copy() : parent2.events.get(i).copy();
				this.events.add(myEv);
			}
		}
	}
	
	public void mutate()
	{
		int choice = rand.nextInt(100);
		
		if(choice > 60)
		{
			//Move a random number and events between 30 minutes and 2 hours
			int mutCount = rand.nextInt(events.size());
			for(int i = 0; i < mutCount; i++)
			{
				Event ev = events.get(rand.nextInt(events.size()));
				long startTime = ev.getStartTime();
				ev.setStartTime(startTime + 1800 * (rand.nextInt(24) + 1) * (rand.nextInt(2) % 2 == 0 ? 1 : -1));
				
			}
		}
		else if(choice > 30)
		{
			//Move all other events towards or away from an event
			int idx = rand.nextInt(events.size());
			int shiftMult = rand.nextInt(24) + 1;
			
			//Changes whether the schedule expands or contracts
			int direction = rand.nextInt(2);
			if(direction == 0) direction = -1;
			
			Event pivot = events.get(idx);
			
			for(Event ev :events)
			{
				if(ev.getStartTime() < pivot.getStartTime())
					ev.shiftStartTime(30 * shiftMult * direction);
				else if(ev.getStartTime() > pivot.getStartTime())
					ev.shiftStartTime(-30 * shiftMult * direction);
			}
		}
		else if(choice > 10)
		{
			//Shift away from an event in one direction
			int idx = rand.nextInt(events.size());
			int shiftMult = rand.nextInt(8) + 1;
			
			Event pivot = events.get(idx);
			
			//Chooses direction of expansion
			int direction = rand.nextInt(2);
			
			for(Event ev :events)
			{
				if(ev.getStartTime() < pivot.getStartTime() && direction == 0)
					ev.shiftStartTime(30 * shiftMult);
				else if(ev.getStartTime() > pivot.getStartTime() && direction == 1)
					ev.shiftStartTime(-30 * shiftMult);
			}
		}
		else
		{
			//Swap two event times
			int idx1 = rand.nextInt(events.size());
			int idx2 = rand.nextInt(events.size());
			
			Event ev1 = events.get(idx1);
			Event ev2 = events.get(idx2);
			
			long time1 = ev1.getStartTime();
			ev1.setStartTime(ev2.getStartTime());
			ev2.setStartTime(time1);
		}
	}
	
	/**
	 * Compute the fitness of this individual
	 * @remark Skipped if the fitness is non-zero
	 */
	public void computeFitness()
	{
		if(fitness != 0)return;
		//fitness = 0;
		
		//Sort a copy of the events
		ArrayList<Event> sorted = ArrayListUtils.copyList(events);
		Collections.sort(sorted, new EventComparer());
		
		for(int i = 1; i < sorted.size(); i++)
		{
			long startTime1 = sorted.get(i - 1).getStartTime();
			long startTime2 = sorted.get(i).getStartTime();
			long endTime1 = startTime1 + sorted.get(i - 1).getDuration() * 60;
			
			//Large penalty for overlapping events
			if(startTime2 < endTime1) fitness -= 10000;
			
			//Penalty for time between events. (Waiting is boring)
			fitness -= (startTime2 - endTime1) / 60;
		}
	}
	
	/**
	 * Print the schedule of events in a readable format
	 */
	public void printSchedule()
	{
		ArrayList<Event> sorted = ArrayListUtils.copyList(events);
		Collections.sort(sorted, new EventComparer());
		
		for(Event ev : sorted)
		{
			Date dt = new Date(ev.getStartTime() * 1000);
			System.out.println(dt.toString());
		}
		
		System.out.println("Score: " + fitness);
	}
	
}
