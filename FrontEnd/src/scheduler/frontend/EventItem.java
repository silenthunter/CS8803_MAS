package scheduler.frontend;

import java.text.SimpleDateFormat;
import java.util.Date;

import scheduler.events.Event;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class EventItem extends TableRow {

	String eventName, eventLocation;
	long eventTime;
	Event event;
	
	public EventItem(String eventName, String eventLocation, long eventTime, Event event, Context context)
	{
		super(context);
		this.eventLocation = eventLocation;
		this.eventName = eventName;
		this.eventTime = eventTime;
		this.event = event;
		
		onCreate(context);
		initClickListener(event);
	}
	
	private void initClickListener(final Event event)
	{
		final Context context = getContext();
		
		setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(context, EventView.class);
				Bundle args = new Bundle();
				args.putSerializable("event", event);
				intent.putExtras(args);
				context.startActivity(intent);
			}
		});
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
