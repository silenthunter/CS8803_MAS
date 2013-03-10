package com.appvengers.Jarvis;

import java.util.ArrayList;
import android.content.SharedPreferences;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;

public class GoogleCalendar 
{
	final HttpTransport transport = AndroidHttp.newCompatibleTransport();
	final JsonFactory jsonFactory = new GsonFactory();
	GoogleAccountCredential credential;
	com.google.api.services.calendar.Calendar client;
	private static GoogleCalendar instance;
	public String calendarId;
	public ArrayList<Event> requestedEvents = new ArrayList<Event>();
	
	public static GoogleCalendar getInstance()
	{
		if(instance == null)
		{
			instance=new GoogleCalendar();
		}
		return instance;
	}

	public void setupClient(GoogleCalendarActivity activity, SharedPreferences settings) {
		credential = GoogleAccountCredential.usingOAuth2(activity, CalendarScopes.CALENDAR);	    
	    credential.setSelectedAccountName(settings.getString(GoogleCalendarActivity.PREF_ACCOUNT_NAME, null));
	    // Calendar client
	    client = new com.google.api.services.calendar.Calendar.Builder(
	        transport, jsonFactory, credential).setApplicationName("Jarvis/1.0")
	        .build();
	    calendarId = settings.getString(GoogleCalendarActivity.PREF_CALENDAR, null);
	}

}
