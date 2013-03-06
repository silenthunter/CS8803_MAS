package com.appvengers.Jarvis;

import java.io.IOException;

import com.google.api.services.calendar.model.Event;

public class AsyncUpdateEventTask extends AsyncCalendarTask {
	Event event;
	AsyncUpdateEventTask(GoogleCalendar activity, Event event) {
		super(activity);
		this.event = event;
	}

	@Override
	protected void doInBackground() throws IOException {
		Event updated = client.events().update(activity.calendarId, event.getId(), event).execute();
		if(updated!=null)
		{
			activity.showSuccess();
		}

	}

}
