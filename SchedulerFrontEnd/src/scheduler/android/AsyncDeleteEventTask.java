package scheduler.android;

import java.io.IOException;

public class AsyncDeleteEventTask extends AsyncCalendarTask {
	String id;
	AsyncDeleteEventTask(GoogleCalendarActivity activity, String id) {
		super(activity);
		this.id = id;
	}

	@Override
	protected void doInBackground() throws IOException {
		GoogleCalendar.getInstance().client.events().delete(GoogleCalendar.getInstance().calendarId, id).execute();
		activity.deleteSuccess();

	}

}
