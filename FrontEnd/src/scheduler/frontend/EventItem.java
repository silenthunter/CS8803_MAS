package scheduler.frontend;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class EventItem extends TableRow {

	String eventName, eventLocation;
	long eventTime;
	
	public EventItem(String eventName, String eventLocation, long eventTime, Context context)
	{
		super(context);
		this.eventLocation = eventLocation;
		this.eventName = eventName;
		this.eventTime = eventTime;
		
		onCreate(context);
	}
	
	public void setName(String name)
	{
		TextView txtEventName = (TextView)findViewById(R.id.eventName);
		txtEventName.setText(name);
	}
	
	public void setLocation(String location)
	{
		TextView txtEventLocation = (TextView)findViewById(R.id.eventLocation);
		txtEventLocation.setText(location);
	}
	
	public void setTime(long time)
	{
		TextView txtEventTime = (TextView)findViewById(R.id.eventTime);
		
		//Convert the time into a string
		Date date = new Date(time);
		SimpleDateFormat format = new SimpleDateFormat("MM/dd HH:mm:ss");
		String txtTime = format.format(date);
		txtEventTime.setText(txtTime);
	}
	
	private void onCreate(Context context) {
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.event_item, this);
		
		setName(this.eventName);
		setLocation(this.eventLocation);
		setTime(this.eventTime);
	}

}
