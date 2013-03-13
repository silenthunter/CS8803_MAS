package scheduler.android;

import java.io.IOException;
import java.util.List;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;

public class AsyncLoadCalendarList extends AsyncCalendarTask {
	
	AsyncLoadCalendarList(GoogleCalendarActivity activity)
	{
		super(activity);
	}

	@Override
	protected void doInBackground() throws IOException {
		CalendarList calList = GoogleCalendar.getInstance().client.calendarList().list().setFields("items(id,summary)").execute();
    	List<CalendarListEntry> list = calList.getItems();
    	//ArrayList<String> names = new ArrayList<String>();
    	for(CalendarListEntry entry : list)
    	{
    		//names.add(entry.getSummary());
    		activity.calIdSummary.put(entry.getSummary(),entry.getId());
    	}
    	activity.selectACalendar();

	}
	static void run(GoogleCalendarActivity activity)
	{
		new AsyncLoadCalendarList(activity).execute();
	}

}
