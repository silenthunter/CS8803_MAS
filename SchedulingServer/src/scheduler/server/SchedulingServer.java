package scheduler.server;

import java.util.ArrayList;

import scheduler.events.Event;
import scheduler.utils.DatabaseUtils;

public class SchedulingServer {

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		DatabaseUtils.init();
		
		ArrayList<Event> ev = DatabaseUtils.getEventsForUser(1);

	}

}
