package scheduler.android;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TableRow;
import android.widget.TextView;

public class EventRow extends TableRow
{
	String eventName;
	String time;
	
	public EventRow(Context context, AttributeSet attrs, String eventName, String time)
	{
		super(context, attrs);
		
		this.eventName = eventName;
		this.time = time;
		
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.event_row, this);
		
		final TextView strEventName = (TextView)findViewById(R.id.eventName);
		strEventName.setText(eventName);
		
		final TextView strTime = (TextView)findViewById(R.id.time);
		strTime.setText(time);
	}
}
