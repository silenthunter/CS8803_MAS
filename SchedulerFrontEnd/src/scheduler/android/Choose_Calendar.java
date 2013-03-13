package scheduler.android;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class Choose_Calendar extends Activity {

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choose__calendar);
		RadioGroup group = (RadioGroup)findViewById(R.id.CalendarSelect);
		Intent intent = getIntent();
		ArrayList<String> calendarStrings = intent.getStringArrayListExtra(GoogleCalendarActivity.CALENDAR_LIST_STRING);
		for(String title:calendarStrings)
		{
			RadioButton button = new RadioButton(this);
			button.setText(title);
			
			group.addView(button);
			System.out.println(title);
		}
	}
	
	public void selectCalendarEvent(View view)
	{
		RadioGroup group = (RadioGroup)findViewById(R.id.CalendarSelect);
		int checkedId = group.getCheckedRadioButtonId();
		Intent toRet = new Intent();
		if(checkedId!=-1)
		{
			RadioButton selected = (RadioButton)group.findViewById(checkedId);
			String selectedText = selected.getText().toString();
			toRet.putExtra(GoogleCalendarActivity.SELECTED_CALENDAR_STRING, selectedText);
			setResult(Activity.RESULT_OK,toRet);
		}
		else
		{
			setResult(Activity.RESULT_CANCELED);
		}
		finish();
	}	

}
