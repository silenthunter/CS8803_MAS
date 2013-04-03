package scheduler.frontend;


import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;

import com.google.android.gcm.GCMBaseIntentService;


public class GCMIntentService extends GCMBaseIntentService
{
	
	static Schedule sched;

	@Override
	protected void onError(Context arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onMessage(Context arg0, Intent arg1)
	{
		//Tell the user they have a new schedule
		String title = "Schedule Ready";
		String message = "A new schedule is ready for you to download! Hit Refresh on the main screen to retrieve it.";
		
		sched.spawnPopup(title, message);
	}

	@Override
	protected void onRegistered(Context arg0, String arg1)
	{
		FrontendConstants.GCM_Reg_ID = arg1;
		
	}

	@Override
	protected void onUnregistered(Context arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}
	
	static void setCallback(Schedule sched)
	{
		GCMIntentService.sched = sched;
	}


}
