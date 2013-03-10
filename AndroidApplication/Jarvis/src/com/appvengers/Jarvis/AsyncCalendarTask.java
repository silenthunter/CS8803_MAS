package com.appvengers.Jarvis;

import java.io.IOException;

import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

abstract class AsyncCalendarTask extends AsyncTask<Void, Void, Boolean> {
	final GoogleCalendarActivity activity;
	
	AsyncCalendarTask(GoogleCalendarActivity activity)
	{
		this.activity = activity;
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
		          userRecoverableException.getIntent(), GoogleCalendarActivity.REQUEST_AUTHORIZATION);
		    } catch (IOException e) {
		    	Log.d("AsyncCalendarTask",e.getMessage());
		    }
		    return false;
	}
	
	abstract protected void doInBackground() throws IOException;

}
