package com.appvengers.Jarvis;

import java.io.IOException;

import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

abstract class AsyncCalendarTask extends AsyncTask<Void, Void, Boolean> {
	final MainActivity activity;
	final com.google.api.services.calendar.Calendar client;
	
	AsyncCalendarTask(MainActivity activity)
	{
		this.activity = activity;
		this.client = activity.client;
	}

	@Override
	protected final Boolean doInBackground(Void... arg0) {
		try {
		      doInBackground();
		      return true;
		    } catch (final GooglePlayServicesAvailabilityIOException availabilityException) {
		      activity.showGooglePlayServicesAvailabilityErrorDialog(
		          availabilityException.getConnectionStatusCode());
		    } catch (UserRecoverableAuthIOException userRecoverableException) {
		      activity.startActivityForResult(
		          userRecoverableException.getIntent(), MainActivity.REQUEST_AUTHORIZATION);
		    } catch (IOException e) {
		    	Log.d("AsyncCalendarTask",e.getMessage());
		    }
		    return false;
	}
	
	abstract protected void doInBackground() throws IOException;

}
