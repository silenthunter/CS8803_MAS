package com.appvengers.Jarvis;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.TimeZone;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

public class GoogleCalendarActivity extends Activity {

	  
	  
	  static final int REQUEST_GOOGLE_PLAY_SERVICES = 0;
	  static final int REQUEST_AUTHORIZATION = 1;
	  static final int REQUEST_ACCOUNT_PICKER = 2;	  
	  static final int ADD_EVENT_REQUEST = 3;	  
	  static final int SELECT_CALENDAR = 4;
	  static final int SHOW_SUCCESS = 5;
	  static final int SHOW_EVENTS = 6;	  	  
	  HashMap<String,String> calIdSummary = new HashMap<String,String>();
	  public final static String SUMMARY_TEXT = "com.appvengers.Jarvis.SUMMARY";
	  public final static String START_TIME_TEXT = "com.appvengers.Jarvis.START_TIME";
	  public final static String END_TIME_TEXT = "com.appvengers.Jarvis.END_TIME";
	  public final static String DESC_TEXT = "com.appvengers.Jarvis.DESC";
	  public final static String CALENDAR_THING = "com.appvengers.Jarvis.CALENDAR";
	  public final static String CALENDAR_LIST_STRING = "com.appvengers.Jarvis.CALENDAR_LIST";
	  public final static String SELECTED_CALENDAR_STRING = "com.appvengers.Jarvis.SELECTED_CALENDAR";
	  public static final String SELECTED_EVENT_STRING = "com.appvengers.Jarvis.SELECTED_EVENT";
	  public static final String PREF_ACCOUNT_NAME = "com.appvengers.Jarvis.accountName";
	  public static final String PREF_CALENDAR = "com.appvengers.Jarvis.accountName";
	  boolean edit = false;
	  Event eventToEdit = null;

	  @Override
	  public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    // view and menu
	    setContentView(R.layout.activity_main);
	    // Google Accounts
	    SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
	    GoogleCalendar.getInstance().setupClient(this, settings);
	    if (checkGooglePlayServicesAvailable()) 
	    {
	    	haveGooglePlayServices();
		}
	    
	  }
	  protected void onActivityResult(final int requestCode, final int resultCode,
		         final Intent data) {
		  super.onActivityResult(requestCode, resultCode, data);
		    switch (requestCode) {
		      case REQUEST_GOOGLE_PLAY_SERVICES:
		        if (resultCode == Activity.RESULT_OK) {
		          haveGooglePlayServices();
		        } else {
		          checkGooglePlayServicesAvailable();
		        }
		        break;
		      case REQUEST_AUTHORIZATION:
		        if (resultCode == Activity.RESULT_OK) {
		          if(GoogleCalendar.getInstance().calendarId==null)
		        	AsyncLoadCalendarList.run(this);
		          
		        } else {
		          chooseAccount();
		        }
		        break;
		      case REQUEST_ACCOUNT_PICKER:
		        if (resultCode == Activity.RESULT_OK && data != null && data.getExtras() != null) {
		          String accountName = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
		          if (accountName != null) {
		            GoogleCalendar.getInstance().credential.setSelectedAccountName(accountName);
		            SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
		            SharedPreferences.Editor editor = settings.edit();
		            editor.putString(PREF_ACCOUNT_NAME, accountName);
		            editor.commit();
		            if(GoogleCalendar.getInstance().calendarId==null)
		            	AsyncLoadCalendarList.run(this);
		          }
		        }
		        break;
		      case SELECT_CALENDAR:
		    	  if(resultCode== Activity.RESULT_OK && data != null && data.getExtras() != null)
		    	  {
		    		  String calendarSummary = data.getStringExtra(SELECTED_CALENDAR_STRING);
		    		  String calId = calIdSummary.get(calendarSummary);
		    		  SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
			          SharedPreferences.Editor editor = settings.edit();
			          editor.putString(PREF_CALENDAR, calId);
			          editor.commit();
			          GoogleCalendar.getInstance().calendarId = calId;
		    	  }
		    	  break;
		    	  
		      case SHOW_SUCCESS:
		    	  break;
		      case SHOW_EVENTS:
		    	  if(resultCode== Activity.RESULT_OK && data != null && data.getExtras() != null)
		    	  {
		    		  String eventSummary = data.getStringExtra(SELECTED_EVENT_STRING);
		    		  Event toEdit = null;
		    		  for(Event e : GoogleCalendar.getInstance().requestedEvents)
		    		  {
		    			  if(e.getSummary().equals(eventSummary))
		    				  toEdit=e;
		    		  }
		    		  if(toEdit!=null)
		    		  {
		    			  edit=true;
		    			  eventToEdit = toEdit;
		    		  }
		    	  }
		    	  break;
		        
		    }
		    
		      
		 }
	  
	  private void haveGooglePlayServices() {
		    // check if there is already an account selected
		    if (GoogleCalendar.getInstance().credential.getSelectedAccountName() == null) {
		      // ask user to choose account
		      chooseAccount();
		    } else {
		    	if(GoogleCalendar.getInstance().calendarId==null)
		    		AsyncLoadCalendarList.run(this);
		    }
		  }
	  private void chooseAccount() {
		    startActivityForResult(GoogleCalendar.getInstance().credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
		  }
	  
	  
	  /** Check that Google Play services APK is installed and up to date. */
	  private boolean checkGooglePlayServicesAvailable() {
	    final int connectionStatusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
	    if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
	      showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
	      return false;
	    }
	    return true;
	  }
	  
	  void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
		    runOnUiThread(new Runnable() {
		      public void run() {
		        Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
		            connectionStatusCode, GoogleCalendarActivity.this, REQUEST_GOOGLE_PLAY_SERVICES);
		        dialog.show();
		      }
		    });
		  }
	  
	  public void selectACalendar()
	  {
		  ArrayList<String> names = new ArrayList<String>();
	  	    names.addAll(calIdSummary.keySet());
	  	    Intent intent = new Intent(this, Choose_Calendar.class);
	  	    intent.putStringArrayListExtra(CALENDAR_LIST_STRING, names);
	  	    startActivityForResult(intent, SELECT_CALENDAR );
	  	    
	  }

  public void addCalendarEvent(View view) throws Exception { 
	 
	 //summary 
  	EditText summary = (EditText) findViewById(R.id.summary);
  	String summaryText = summary.getText().toString();
  	
  	//Start date/time
  	DatePicker startDatePick = (DatePicker) findViewById(R.id.startDate);
  	TimePicker startTimePick = (TimePicker) findViewById(R.id.startTime);
  	GregorianCalendar gc = new GregorianCalendar(startDatePick.getYear(), startDatePick.getMonth(),startDatePick.getDayOfMonth(),startTimePick.getCurrentHour(),startTimePick.getCurrentMinute());
  	DateTime tempStart = new DateTime(gc.getTime(), TimeZone.getTimeZone("UTC"));
  	EventDateTime startDateTime = new EventDateTime();
  	startDateTime.setDateTime(tempStart);
  	
  	//End date/time
  	DatePicker endDatePick = (DatePicker) findViewById(R.id.endDate);
  	TimePicker endTimePick = (TimePicker) findViewById(R.id.endTime);
  	gc = new GregorianCalendar(endDatePick.getYear(), endDatePick.getMonth(),endDatePick.getDayOfMonth(),endTimePick.getCurrentHour(),endTimePick.getCurrentMinute());
  	DateTime tempEnd = new DateTime(gc.getTime(), TimeZone.getTimeZone("UTC"));
  	EventDateTime endDateTime = new EventDateTime();
  	endDateTime.setDateTime(tempEnd);

  	EditText desc = (EditText) findViewById(R.id.desc);
  	String descText = desc.getText().toString();
  	if(!edit)
  	{
  		new AsyncNewEventTask(this, summaryText, descText, startDateTime, endDateTime).execute();
  	}
  	else
  	{
  		eventToEdit.setSummary(summaryText);
  		eventToEdit.setDescription(descText);
  		eventToEdit.setStart(startDateTime);
  		eventToEdit.setEnd(endDateTime);
  		new AsyncUpdateEventTask(this,eventToEdit).execute();
  	}
  	
  }
  public void showSuccess()
  {
	  Intent intent = new Intent(this, ShowSuccess.class);
	  startActivityForResult(intent, SHOW_SUCCESS );
  }
  public void getTodaysEvents(View view)
  {
	  
	  GregorianCalendar startGC = new GregorianCalendar();
	  startGC = new GregorianCalendar(startGC.get(GregorianCalendar.YEAR),startGC.get(GregorianCalendar.MONTH),startGC.get(GregorianCalendar.DAY_OF_MONTH),0,0,0);
	  GregorianCalendar endGC = new GregorianCalendar();
	  endGC = new GregorianCalendar(endGC.get(GregorianCalendar.YEAR),endGC.get(GregorianCalendar.MONTH),endGC.get(GregorianCalendar.DAY_OF_MONTH),23,59,59);
	  new AsyncLoadEventsTask(this,GoogleCalendar.getInstance().requestedEvents,new DateTime(startGC.getTime(),TimeZone.getTimeZone("UTC")),new DateTime(endGC.getTime(),TimeZone.getTimeZone("UTC"))).execute();
  }
  public void showEvents()
  {
	  Intent intent = new Intent(this, ShowEvents.class);
	  startActivityForResult(intent, SHOW_EVENTS);
  }
  public void deleteSuccess()
  {
	  Intent intent = new Intent(this, ShowSuccess.class);
	  startActivityForResult(intent, SHOW_SUCCESS );
  }

}
