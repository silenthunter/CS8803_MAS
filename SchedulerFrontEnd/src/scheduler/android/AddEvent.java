package scheduler.android;

import java.util.ArrayList;

import scheduler.comms.MessageSender;
import scheduler.events.Event;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

public class AddEvent extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_event);
		
		//Suppress the large calendar view
		final DatePicker datePicker = (DatePicker)findViewById(R.id.eventDate);
		datePicker.setCalendarViewShown(false);
		
		final Button btnSubmit = (Button)findViewById(R.id.btnSubmit);
		btnSubmit.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0)
			{
				class EventAdder extends AsyncTask<Integer, Integer, Long>
				{
					@Override
					protected Long doInBackground(Integer... arg0) {
						long startTime = datePicker.getCalendarView().getDate();
						TextView txtDuration = (TextView)findViewById(R.id.txtEventDuration);
						String strDuration = txtDuration.getText().toString();
						int duration = Integer.parseInt(strDuration);
						
						TextView txtPriority = (TextView)findViewById(R.id.txtPriority);
						String strPriority = txtPriority.getText().toString();
						
						short priority = Short.parseShort(strPriority);
						
						Event event = new Event(startTime, duration, priority);
						
						TextView txtName = (TextView)findViewById(R.id.txtEventName);
						event.setName(txtName.getText().toString());
						
						TextView txtLocation = (TextView)findViewById(R.id.txtEventLocation);
						event.setLocation(txtLocation.getText().toString());
						
						//Connect to the server and send the event up
						MessageSender sender = new MessageSender("ec2-50-19-65-128.compute-1.amazonaws.com", 8000);
						sender.connect();
						
						ArrayList<Event> events = new ArrayList<Event>();
						events.add(event);
						sender.addEvents(events, 5);
						
						sender.disconnect();
						
						return null;
					}
				}
				
				new EventAdder().execute();
				
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_event, menu);
		return true;
	}

}
