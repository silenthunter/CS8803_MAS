package com.appvengers.Jarvis;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class ShowEvents extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_events);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_show_events, menu);
		return true;
	}

}
