package com.appvengers.Jarvis;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.api.services.calendar.model.Event;

public class ShowEvents extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_events);
		
		RadioGroup list = (RadioGroup)findViewById(R.id.event_list);
		list.removeAllViews();
		for(Event e : GoogleCalendar.currentEvents)
		{
			RadioButton b = new RadioButton(this);
			b.setText(e.getSummary());
			list.addView(b);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_show_events, menu);
		return true;
	}
	
	public void updateEvent(View view)
	{
		RadioGroup group = (RadioGroup)findViewById(R.id.event_list);
		int checkedId = group.getCheckedRadioButtonId();
		Intent toRet = new Intent();
		if(checkedId!=-1)
		{
			RadioButton selected = (RadioButton)group.findViewById(checkedId);
			String selectedText = selected.getText().toString();
			toRet.putExtra(GoogleCalendar.SELECTED_EVENT_STRING, selectedText);
			setResult(Activity.RESULT_OK,toRet);
		}
		else
		{
			setResult(Activity.RESULT_CANCELED);
		}
		finish();
	}

}
