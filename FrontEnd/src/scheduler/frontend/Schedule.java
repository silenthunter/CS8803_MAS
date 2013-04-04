package scheduler.frontend;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import com.google.android.gcm.GCMRegistrar;

import scheduler.comms.MessageSender;
import scheduler.events.Event;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
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
	Context scheduleContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_schedule);
		
		initGCM();
		initButtons();
		loadEvents();
	}
	
	private void initButtons()
	{
		final TableLayout scroll = (TableLayout)findViewById(R.id.event_table);
		
		final Button refreshBtn = (Button)findViewById(R.id.btnAddTask);
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
								saveEvents();
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
							EventItem newEvent = new EventItem(event.getName(), event.getLocation(), event.getStartTime(), event, scroll.getContext());
							scroll.addView(newEvent);
						}
					}
				};
				
				//Call the async task
				new AsyncGetEvents().execute(updateEvents);
				
			}
		});
	}

	private void initGCM()
	{
		GCMRegistrar.checkDevice(this);
		GCMRegistrar.checkManifest(this);
		
		final String regID = GCMRegistrar.getRegistrationId(this);
		if(regID.equals(""))
			GCMRegistrar.register(this, FrontendConstants.SENDER_ID);
		else
			FrontendConstants.GCM_Reg_ID = regID;
		
		GCMIntentService.setCallback(this);
		
		scheduleContext = this;
	}

	public void spawnPopup(final String title, final String message)
	{
	
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				AlertDialog.Builder bld = new AlertDialog.Builder(scheduleContext);
				bld.setTitle(title);
				bld.setMessage(message);
				bld.setCancelable(false);
				bld.setPositiveButton("Ok", null);
				bld.show();
				
			}
		});
	}
	
	private void saveEvents()
	{
		try {
			File saveData = new File(getApplicationContext().getFilesDir(), FrontendConstants.EVENTS_FILE_NAME);
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

	private void loadEvents()
	{
		try
		{
			File saveData = new File(getApplicationContext().getFilesDir(), FrontendConstants.EVENTS_FILE_NAME);
			FileInputStream inStream = new FileInputStream(saveData);
			
			byte[] buffer = new byte[1000000];
			int read = inStream.read(buffer);
			
			ArrayList<Event> readEvents = new ArrayList<Event>();
			for(int i = 0; i < read; i += Event.MAX_SIZE)
			{
				Event newEvent = Event.readFromBuffer(buffer, i, Event.MAX_SIZE);
				readEvents.add(newEvent);
			}
			
			events = readEvents;
			
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					final TableLayout scroll = (TableLayout)findViewById(R.id.event_table);
					
					final Button refreshBtn = (Button)findViewById(R.id.btnAddTask);
					
					//Clear the scrollView
					refreshBtn.setEnabled(true);
					scroll.removeAllViews();
					for(Event event : events)
					{
						 //add the new events
						EventItem newEvent = new EventItem(event.getName(), event.getLocation(), event.getStartTime(), event, scroll.getContext());
						scroll.addView(newEvent);
					}
					
				}
			});
			
			inStream.close();
		}catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void computeEvents()
	{
		class ComputeAsync extends AsyncTask<Integer, Integer, Integer>
		{
			@Override
			protected Integer doInBackground(Integer... params)
			{
				MessageSender snd = new MessageSender(FrontendConstants.SERVER_ADDR, FrontendConstants.SERVER_PORT);
				snd.connect();
				snd.createSchedule(FrontendConstants.USER_ID, FrontendConstants.GCM_Reg_ID);
				snd.disconnect();
				return null;
			}
		}
		
		ComputeAsync async = new ComputeAsync();
		async.execute();
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
		else if(item.getItemId() == R.id.computeEvents)
		{
			computeEvents();
		}
		
		// TODO Auto-generated method stub
		return super.onOptionsItemSelected(item);
	}

}
