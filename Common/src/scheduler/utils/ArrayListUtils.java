package scheduler.utils;

import java.util.ArrayList;

import scheduler.events.Event;

public class ArrayListUtils
{
	public static ArrayList<Event> copyList(ArrayList<Event> eventList)
	{
		ArrayList<Event> retn = new ArrayList<Event>();
		
		//Copy events
		for(Event ev : eventList)
		{
			Event myEv = ev.copy();
			retn.add(myEv);
		}
		
		return retn;
	}
}
