package scheduler.android;

import java.util.ArrayList;

import scheduler.comms.MessageSender;
import android.accounts.Account;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.QuickContactBadge;

import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

public class MainActivity extends Activity {

	String authToken = "";
	Account account = null;
	
	final static int RESULT_CODE = 7;
	final static int AUTHORIZE = 8;
	final static int GET_ALL_EVENTS = 9;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		startActivityForResult(new Intent(this,GoogleCalendarActivity.class),AUTHORIZE);
	}
	
	private void addAllEventsToServer() {
		class EventAdder extends AsyncTask<Integer, Integer, Long>
		{
			@Override
			protected Long doInBackground(Integer... arg0) {
				MessageSender sender = new MessageSender("ec2-50-19-65-128.compute-1.amazonaws.com", 8000);
				sender.connect();
				ArrayList<scheduler.events.Event> events = new ArrayList<scheduler.events.Event>();
				ArrayList<Event> es = GoogleCalendar.getInstance().requestedEvents;
				for(Event e :es)
				{
					EventDateTime getStart = e.getStart();
					EventDateTime getEnd = e.getEnd();
					if(getStart!=null&&getEnd!=null)
					{
						DateTime getDTStart = getStart.getDateTime();
						DateTime getDTEnd = getEnd.getDateTime();
						if(getDTStart!=null&&getDTEnd!=null)
						{
							scheduler.events.Event event = new scheduler.events.Event(getDTStart.getValue(), (int)(getDTEnd.getValue()-getDTStart.getValue()));
							event.setName(e.getSummary());
							event.lock();
							events.add(event);
						}
					}
				}
				sender.addEvents(events, 5);
				sender.disconnect();
				return null;
			}
		}
		new EventAdder().execute();
		
	}

	@Override
	public void startActivityForResult(Intent intent, int requestCode)
	{
		intent.putExtra("requestCode", requestCode);
        super.startActivityForResult(intent, requestCode);
	}
	
	private void registerButtons()
	{
		//TODO: Move these to real classes
		
		final Button viewSchedule = (Button)findViewById(R.id.BtnViewSchedule);
		viewSchedule.setOnClickListener(new View.OnClickListener()
		{
			
			@Override
			public void onClick(View arg0)
			{
				Intent newIntent = new Intent(getApplicationContext(), Schedule.class);
				startActivity(newIntent);
			}
		});
		
		final Button addEvent = (Button)findViewById(R.id.BtnNewEvent);
		addEvent.setOnClickListener(new View.OnClickListener()
		{
			
			@Override
			public void onClick(View arg0)
			{
				Intent newIntent = new Intent(getApplicationContext(), AddEvent.class);
				startActivity(newIntent);
			}
		});
	}
	
	private void initQuickContact()
	{
		final QuickContactBadge badge = (QuickContactBadge)findViewById(R.id.profileImage);
		badge.assignContactFromEmail(account.name, false);
		//badge.setMode(ContactsContract.QuickContact.MODE_SMALL);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(requestCode == RESULT_CODE)
		{
			Bundle bnd = data.getExtras();
			for(String key : bnd.keySet())
				System.out.println(key + " : " + bnd.get(key));
		}
		if(requestCode == AUTHORIZE)
		{
			startActivityForResult(new Intent(this,GoogleCalendarActivity.class),GET_ALL_EVENTS);
		}
		if(requestCode == GET_ALL_EVENTS)
		{
			addAllEventsToServer();
			class AuthGetter extends AsyncTask<Activity, Integer, Long>
			{
				
				@Override
				protected Long doInBackground(Activity... arg0) {
					try
					{
						authToken = GoogleCalendar.getInstance().credential.getToken();
						
						System.out.println(authToken);
					}
					catch(UserRecoverableAuthException e)
					{
						startActivityForResult(e.getIntent(), RESULT_CODE);
					}
					catch(Exception e){e.printStackTrace();}
					return null;
				}
			}			
			new AuthGetter().execute(this);
			account = GoogleCalendar.getInstance().credential.getSelectedAccount();
			registerButtons();
			initQuickContact();
		}
		
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
