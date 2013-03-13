package scheduler.android;

import java.io.Serializable;
import java.text.AttributedString;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import scheduler.comms.MessageSender;
import scheduler.events.Event;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TableLayout;

public class Schedule extends Activity {
	
	final private ArrayList<Event> userEvents = new ArrayList<Event>();
	final Context schedContext = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_schedule);
		
		final Button btnUpdate = (Button)findViewById(R.id.BtnUpdateSchedule);
		final ProgressBar lblProgress = (ProgressBar)findViewById(R.id.updateProgress);
		lblProgress.setEnabled(false);
		
		final TableLayout eventTable = (TableLayout)findViewById(R.id.tblEvents);
		
		btnUpdate.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0)
			{
				btnUpdate.setEnabled(false);
				lblProgress.setEnabled(true);
				
				class EventFetch extends AsyncTask<Handler, Integer, Long>
				{
					
					@Override
					protected Long doInBackground(Handler... arg0) 
					{						
						//TODO: Don't hardcode
						MessageSender sender = new MessageSender("ec2-50-19-65-128.compute-1.amazonaws.com", 8000);
						sender.connect();
						ArrayList<ArrayList<Event>> schedules = sender.getSchedules(3);
						
						ArrayList<Event> schedule = schedules.get(0);
						
						//Load the schedules into the UI
						for(Event ev : schedule)
						{
							userEvents.add(ev);
						}
						
						sender.disconnect();
						
						Handler callback = arg0[0];
						Message msg = callback.obtainMessage();
						
						callback.sendEmptyMessage(RESULT_OK);
						
						return null;
					}
				}
				
				Handler updateHandler = new Handler()
				{
					@Override
					public void handleMessage(Message msg)
					{
						int idx = 0;
						for(Event ev : userEvents)
						{
							long time = ev.getStartTime();
							Date eventTime = new Date(time);
							eventTime.toString();
							SimpleDateFormat format = new SimpleDateFormat("MM/dd HH:mm:ss");
							String strTime = format.format(eventTime);
							EventRow row = new EventRow(eventTable.getContext(), null, ev.getName(), strTime, idx++, new MyHandler());
						
							eventTable.addView(row);
						}
						
						lblProgress.setEnabled(false);
						btnUpdate.setEnabled(true);
					}
				};
				
				new EventFetch().execute(updateHandler);
			}
		});
		
	}
	
	class MyHandler extends Handler
	{
		@Override
		public void handleMessage(Message msg)
		{
			int idx = msg.what;
			super.handleMessage(msg);
			
			Intent intent = new Intent(getApplicationContext(), EventView.class);
			Event ev = userEvents.get(idx);
			intent.putExtra("Event", (Serializable)ev);
			
			startActivity(intent);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.schedule, menu);
		return true;
	}

}
