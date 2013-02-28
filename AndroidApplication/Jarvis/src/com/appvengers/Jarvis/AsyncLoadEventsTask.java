package com.appvengers.Jarvis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

public class AsyncLoadEventsTask extends AsyncCalendarTask {
	DateTime timeMin;
	DateTime timeMax;
	ArrayList<Event> events;
	

	AsyncLoadEventsTask(MainActivity activity,ArrayList<Event> events, DateTime timeMin, DateTime timeMax) {
		super(activity);
		this.timeMax = timeMax;
		this.timeMin = timeMin;
		this.events = events;
	}

	@Override
	protected void doInBackground() throws IOException {
		String pageToken = null;
		do{
			Events eventsObj = client.events().list(activity.calendarId).setPageToken(pageToken).setSingleEvents(true).setTimeMin(timeMin).setTimeMax(timeMax).execute();
			List<Event> items = eventsObj.getItems();
			events.addAll(items);
			
		}while (pageToken !=null);

	}

}
