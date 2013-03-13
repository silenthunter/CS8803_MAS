package scheduler.android;

import java.io.IOException;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

public class AsyncNewEventTask extends AsyncCalendarTask {
	
	 String summary;
	 String description; 
	 EventDateTime startTime; 
	 EventDateTime endTime;
	
	AsyncNewEventTask(GoogleCalendarActivity activity, String summary, String description, EventDateTime startTime, EventDateTime endTime)
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
		Event createdEvent = GoogleCalendar.getInstance().client.events().insert(GoogleCalendar.getInstance().calendarId, event).execute();
		if(createdEvent!=null)
			activity.addEventSuccess();

	}

}
