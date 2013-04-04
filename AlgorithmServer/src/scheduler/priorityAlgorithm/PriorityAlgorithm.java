package scheduler.priorityAlgorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Random;

import scheduler.events.Event;
import scheduler.events.EventComparer;
import scheduler.geneticAlgorithm.Individual;
import scheduler.geneticAlgorithm.IndividualComparer;

public class PriorityAlgorithm {
	
	static Random rand = new Random();
	private ArrayList<Event> taskList = new ArrayList<Event>();
	private ArrayList<Event> lockedList = new ArrayList<Event>();
	private ArrayList<Individual> population = new ArrayList<Individual>();
	private long time = ((System.currentTimeMillis()/(60000))+2)*(60000);
	private int totalPriority;
	
	private static int NUMBER_OF_SCHEDULES = 1;
	
	public PriorityAlgorithm(ArrayList<Event> events)
	{
		//get unchangeable events and calculate total priority (for random generation)
		totalPriority = 0;
		for (Event e:events){
			if (e.isLocked()){
				lockedList.add(e);
			} else {
				taskList.add(e);
				totalPriority+=e.getPriority();
			}
		}
		Collections.sort(lockedList,new EventComparer());
	}
	
	public void compute()
	{	
		//generate a schedule using priority. split event if it doesn't fit before locked event.
		for (int x=0;x<NUMBER_OF_SCHEDULES;x++){
			ArrayList<Event> schedule = new ArrayList<Event>();
			
			//make copies of info local to this schedule
			LinkedList<Event> remainingTaskList = new LinkedList<Event>();
			for (Event e:taskList){
				remainingTaskList.add(e.copy());
			}
			int currPriority = totalPriority; //remaining priority in remainingTaskList
			long currTime = time; //current time for which we're scheduling
			
			//keep track of next lock event
			Event nextLocked = null;
			if (!lockedList.isEmpty())
				nextLocked = lockedList.get(0);
			int nextLockNum = 1;
			
			while(!remainingTaskList.isEmpty()){
				int randEventNum = rand.nextInt(currPriority);
				Iterator<Event> i = remainingTaskList.iterator();
				
				Event currEvent = null;
				while (randEventNum>=0){
					try {
						currEvent = i.next();
					} catch (NoSuchElementException e){
						e.printStackTrace(); //should never happen
					}
					randEventNum -= currEvent.getPriority();
				}
				
				long endTime = currTime+(currEvent.getDuration()*60000);
				if (nextLocked!=null && endTime>nextLocked.getStartTime()){
					// next locked event starts before currEvent will finish.
					
					int freeTime = (int)((nextLocked.getStartTime()-currTime)/60000);
					if (freeTime>=5){
						//add as much of currEvent as we can.
						Event firstEvent = currEvent.copy();
						firstEvent.setDuration(freeTime);
						firstEvent.setStartTime(currTime);
						currEvent.setDuration(currEvent.getDuration()-freeTime);
						schedule.add(firstEvent);
					}//dont bother if its <5 minutes
					
					//add next locked	
					schedule.add(nextLocked);
					currTime = nextLocked.getStartTime()+nextLocked.getDuration()*60000; //end time
					if (nextLockNum<lockedList.size()){
						nextLocked = lockedList.get(nextLockNum);
						nextLockNum++;
					} else {
						nextLocked = null;
					}
				} else {
					currEvent.setStartTime(currTime);
					schedule.add(currEvent);
					i.remove();
					currPriority -= currEvent.getPriority();
					currTime = endTime;
				}				
			}			
			population.add(new Individual(schedule));
		}
		
	}
	
	/**
	 * Returns the population of this genetic algorithm.
	 * @return ArrayList of Individuals forming a population
	 * @remark The population will be sorted from best fitness to worst if <b>compute</b> has been called beforehand.
	 */
	public ArrayList<Individual> getPopulation()
	{
		return population;
	}

}
