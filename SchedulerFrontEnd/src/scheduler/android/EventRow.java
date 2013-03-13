package scheduler.android;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TableRow;
import android.widget.TextView;

public class EventRow extends TableRow
{
	String eventName;
	String time;
	Handler eventViewerCallback = null;
	final int uid;
	
	public EventRow(Context context, AttributeSet attrs, String eventName, String time, int id, Handler callback)
	{
		super(context, attrs);
		
		this.eventName = eventName;
		this.time = time;
		this.eventViewerCallback = callback;
		this.uid = id;
		
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.event_row, this);
		
		final TextView strEventName = (TextView)findViewById(R.id.eventName);
		strEventName.setText(eventName);
		
		final TextView strTime = (TextView)findViewById(R.id.time);
		strTime.setText(time);
		
		final String evName = eventName;
		final Context ctx = this.getContext();
		//Give more information when clicked
		this.setOnClickListener(new View.OnClickListener() 
		{
			
			@Override
			public void onClick(View arg0)
			{
				System.out.println("Event Selected: " + evName);
				eventViewerCallback.sendEmptyMessage(uid);
			}
		});
		
	}
	
}
