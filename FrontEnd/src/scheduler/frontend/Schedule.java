package scheduler.frontend;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import scheduler.comms.MessageSender;
import scheduler.events.Event;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TableLayout;

public class Schedule extends Activity {
	
	ArrayList<Event> events = new ArrayList<Event>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_schedule);
		
		initButtons();
	}
	
	private void initButtons()
	{
		final TableLayout scroll = (TableLayout)findViewById(R.id.event_table);
		
		final Button refreshBtn = (Button)findViewById(R.id.btnRefershEvents);
		refreshBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) 
			{
				//disable the button
				refreshBtn.setEnabled(false);
				
				//Set the asynchronous task
				class AsyncGetEvents extends AsyncTask<Handler, Integer, Long>
				{
					@Override
					protected Long doInBackground(Handler... params) {
						
						//Get the list of events
						try
						{
							MessageSender snd = new MessageSender(FrontendConstants.SERVER_ADDR, FrontendConstants.SERVER_PORT);
							snd.connect();
							ArrayList<ArrayList<Event>> schedules = snd.getSchedules(FrontendConstants.USER_ID);
							
							if(schedules.size() > 0)
							{
								ArrayList<Event> schedule = schedules.get(0);
								events = schedule;
							}
							
							//Inform the callback that the operation is done
							Handler callback = params[0];
							callback.sendEmptyMessage(RESULT_OK);
						}catch(Exception e)
						{
							e.printStackTrace();
						}
						
						return null;
					}
				}
				
				//Create a handler to update the GUI
				Handler updateEvents = new Handler()
				{
					@Override
					public void handleMessage(Message msg)
					{
						//Clear the scrollView
						refreshBtn.setEnabled(true);
						scroll.removeAllViews();
						for(Event event : events)
						{
							 //add the new events
							EventItem newEvent = new EventItem(event.getName(), event.getLocation(), event.getStartTime(), scroll.getContext());
							scroll.addView(newEvent);
						}
					}
				};
				
				//Call the async task
				new AsyncGetEvents().execute(updateEvents);
				
			}
		});
	}
	
	private void saveEvents()
	{
		try {
			File saveData = new File(getApplicationContext().getFilesDir(), "storedEvents");
			FileOutputStream outStream = new FileOutputStream(saveData);
			
			for(Event event : events)
			{
				outStream.write(Event.writeToBuffer(event));
			}
			
			outStream.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.schedule, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		if(item.getItemId() == R.id.toDo)
		{
			Intent intent = new Intent(this, ToDoView.class);
			startActivity(intent);
		}
		else if(item.getItemId() == R.id.new_event)
		{
			Intent intent = new Intent(this, EventView.class);
			startActivity(intent);
		}
		else if(item.getItemId() == R.id.new_task)
		{
			Intent intent = new Intent(this, ItemView.class);
			startActivity(intent);
		}
		
		// TODO Auto-generated method stub
		return super.onOptionsItemSelected(item);
	}

}
