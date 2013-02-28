package com.appvengers.Jarvis;

import java.io.IOException;
import java.util.ArrayList;

import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;

public class AsyncNewEventTask extends AsyncCalendarTask {
	
	 String summary;
	 String description; 
	 EventDateTime startTime; 
	 EventDateTime endTime;
	
	AsyncNewEventTask(MainActivity activity, String summary, String description, EventDateTime startTime, EventDateTime endTime)
	{
		super(activity);
		this.summary = summary;
		this.description = description;
		this.startTime = startTime;
		this.endTime = endTime;
	}

	@Override
	protected void doInBackground() throws IOException {
		Event event = new Event();
		event.setDescription(description);
		event.setSummary(summary);
		event.setStart(startTime);
		event.setEnd(endTime);
		event.setAttendees(null);
		event.setReminders(null);
		Event createdEvent = client.events().insert(activity.calendarId, event).execute();
		if(createdEvent!=null)
			activity.showSuccess();

	}

}
