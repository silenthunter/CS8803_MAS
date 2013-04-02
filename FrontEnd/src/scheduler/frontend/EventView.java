package scheduler.frontend;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

public class EventView extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_view);
		
		onInit();
	}
	
	private void onInit()
	{
		Button addEvent = (Button)findViewById(R.id.btnAddEvent);
		addEvent.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0)
			{
				//Check if it's valid data
				if(checkValidation())
				{

				}
				
			}
		});
		
		final FragmentManager fragMan = getSupportFragmentManager();
		
		//Listen for the start time click
		final EditText startTime = (EditText)findViewById(R.id.addEventStartDate);
		startTime.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View arg0, boolean arg1) {
				
				if(arg1)
				{
					DialogFragment timePicker = new TimePickerFragment();
					Bundle args = new Bundle();
					args.putInt("editText", startTime.getId());
					timePicker.setArguments(args);
					timePicker.show(fragMan, "SelectTime");
				}
				
			}
		});
		
		startTime.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				//DialogFragment timePicker = new TimePickerFragment();
				//timePicker.show(fragMan, "SelectTime");
				
			}
		});
	}
	
	private boolean checkValidation()
	{
		//Check date
		TextView txtDate = (TextView)findViewById(R.id.addEventDate);
		String dateStr = txtDate.getText().toString();
		if(!checkDateTime(dateStr, "mm/dd/yy", "Invalid date format")) return false;
		
		//Check start time
		TextView txtStartDate = (TextView)findViewById(R.id.addEventStartDate);
		String startDateStr = txtStartDate.getText().toString();
		if(!checkDateTime(startDateStr, "HH:mmaa", "Invalid start time")) return false;
		
		//Check end time
		TextView txtEndDate = (TextView)findViewById(R.id.addEventEndDate);
		String endDateStr = txtEndDate.getText().toString();
		if(!checkDateTime(endDateStr, "HH:mmaa", "Invalid end time")) return false;
		
		return true;
	}
	
	private boolean checkDateTime(String input, String dateTimeFormat, String errorMessage)
	{
		SimpleDateFormat format = new SimpleDateFormat(dateTimeFormat);
		
		//Check date
		try {
			format.parse(input);
		} catch (ParseException e) {
			//Invalid date
			createErrorPopup(errorMessage);
			return false;
		}
		
		return true;
	}
	
	private void createErrorPopup(String text)
	{
		AlertDialog.Builder bld = new AlertDialog.Builder(this);
		bld.setTitle("Invalid Input");
		bld.setMessage(text);
		bld.setPositiveButton("OK", null);
		bld.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.schedule, menu);
		return true;
	}
	
	public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener
	{
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			
		     // Use the current time as the default values for the picker
	        final Calendar c = Calendar.getInstance();
	        int hour = c.get(Calendar.HOUR_OF_DAY);
	        int minute = c.get(Calendar.MINUTE);

			return new TimePickerDialog(getActivity(), this, hour, minute, false);
		}
		
		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute)
		{
			Bundle args = this.getArguments();
			
			if(args != null && args.containsKey("editText"))
			{
				int editTextId = args.getInt("editText");
				EditText editText = (EditText)view.findViewById(editTextId);
				editText.setText(hourOfDay + ":" + minute);
			}
			
		}
	}

}
