package scheduler.android;

import java.io.IOException;

import com.google.api.services.calendar.model.Event;

public class AsyncUpdateEventTask extends AsyncCalendarTask {
	Event event;
	AsyncUpdateEventTask(GoogleCalendarActivity activity, Event event) {
		super(activity);
		this.event = event;
	}

	@Override
	protected void doInBackground() throws IOException {
		Event updated = GoogleCalendar.getInstance().client.events().update(GoogleCalendar.getInstance().calendarId, event.getId(), event).execute();
		if(updated!=null)
		{
			activity.updateEventSuccess();
		}

	}

}
