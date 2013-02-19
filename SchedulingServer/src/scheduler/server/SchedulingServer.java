package scheduler.server;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import scheduler.events.Event;
import scheduler.geneticAlgorithm.GeneticAlgorithm;
import scheduler.utils.DatabaseUtils;

public class SchedulingServer {

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		//DatabaseUtils.init();
		
		ArrayList<Event> events = new ArrayList<Event>();//DatabaseUtils.getEventsForUser(1);
		Date d = new Date();
		
		for(int i = 0; i < 10; i++)
		{
			Event ev = new Event(d.getTime() / 1000, 60);
			events.add(ev);
		}
		
		GeneticAlgorithm alg = new GeneticAlgorithm(events);
		alg.compute(100, 100);
	}

}
