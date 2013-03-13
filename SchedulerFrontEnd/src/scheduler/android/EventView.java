package scheduler.android;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import scheduler.events.Event;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.widget.TextView;

public class EventView extends Activity {
	
	Event event;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_event_view);
		
		Intent intent = getIntent();
		event = (Event)intent.getExtras().get("Event");
		
		final TextView lblEventName = (TextView)findViewById(R.id.lblViewEventName);
		final TextView lblEventLocation = (TextView)findViewById(R.id.lblViewLocation);
		final TextView lblDate = (TextView)findViewById(R.id.lblViewDateTime);
		
		//set name
		lblEventName.setText(event.getName());
		
		//set location
		lblEventLocation.setText(event.getLocation());
		
		//Set Time
		Date cal = new Date(event.getStartTime());
		SimpleDateFormat format = new SimpleDateFormat("HH:mm - dd MMM, yy");
		String strDate = format.format(cal);
		lblDate.setText(strDate);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.event_view, menu);
		return true;
	}

}
