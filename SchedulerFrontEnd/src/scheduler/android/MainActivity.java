package scheduler.android;

import java.util.ArrayList;

import scheduler.comms.MessageSender;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.services.calendar.model.Event;

import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.QuickContactBadge;

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
		
		class AuthGetter extends AsyncTask<Activity, Integer, Long>
		{
			
			@Override
			protected Long doInBackground(Activity... arg0) {
				try
				{
					authToken = GoogleCalendar.getInstance().credential.getToken();
					account = GoogleCalendar.getInstance().credential.getSelectedAccount();
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
		
		startActivityForResult(new Intent(this,GoogleCalendarActivity.class),GET_ALL_EVENTS);
		addAllEventsToServer();
		
		new AuthGetter().execute(this);
		
		registerButtons();
		initQuickContact();
	}
	
	private void addAllEventsToServer() {
		MessageSender sender = new MessageSender("ec2-50-19-65-128.compute-1.amazonaws.com", 8000);
		sender.connect();
		ArrayList<scheduler.events.Event> events = new ArrayList<scheduler.events.Event>();
		for(Event e :GoogleCalendar.getInstance().requestedEvents)
		{
			scheduler.events.Event event = new scheduler.events.Event(e.getStart().getDateTime().getValue(), (int)(e.getEnd().getDateTime().getValue()-e.getStart().getDateTime().getValue()));
			event.setName(e.getSummary());
			event.lock();
			events.add(event);
		}
		sender.addEvents(events, 3);
		
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
