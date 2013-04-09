package scheduler.frontend;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import scheduler.comms.MessageSender;
import scheduler.events.Event;

import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings.Global;
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
		
		boolean enabled = true;
		
		Bundle bundle = getIntent().getExtras();
		if(bundle != null && bundle.containsKey("event"))
		{
			Event event = (Event)bundle.getSerializable("event");
			initValues(event);
			enabled = false;
		}
		
		onInit(enabled);
	}
	
	private void initValues(Event event)
	{
		//Set title
		EditText txtTitle = (EditText)findViewById(R.id.addTaskTitle);
		txtTitle.setText(event.getName());
		
		//Set location
		EditText txtLocation = (EditText)findViewById(R.id.addTaskLocation);
		txtLocation.setText(event.getLocation());
		
		//Set date and time
		long startTime = event.getStartTime() * 1000;
		long endTime = startTime + event.getDuration() * 60000;
		SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yy");
		SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:mmaa");
		
		String dateStr = dateFormatter.format(new Date(startTime));
		String startStr = timeFormatter.format(new Date(startTime));
		String endStr = timeFormatter.format(new Date(endTime));
		
		EditText txtDate = (EditText)findViewById(R.id.addTaskDate);
		txtDate.setText(dateStr);
		
		EditText txtStart = (EditText)findViewById(R.id.addTaskStartDate);
		txtStart.setText(startStr);
		
		EditText txtEnd = (EditText)findViewById(R.id.addTaskEndDate);
		txtEnd.setText(endStr);
	}
	
	private void onInit(boolean enabled)
	{
		Button addEvent = (Button)findViewById(R.id.btnAddTask);
		if(!enabled)
			addEvent.setVisibility(View.INVISIBLE);
		else
			addEvent.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View arg0)
				{
					//Check if it's valid data
					if(checkValidation())
					{
						writeEvent();
					}
					
				}
			});
		
		final FragmentManager fragMan = getSupportFragmentManager();
		
		//Listen for the start time click
		final EditText startTime = (EditText)findViewById(R.id.addTaskStartDate);
		if(!enabled)
			startTime.setEnabled(false);
		else
		startTime.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View arg0, boolean hasFocus) {
				
				if(hasFocus)
				{
					DialogFragment timePicker = new TimePickerFragment();
					Bundle args = new Bundle();
					args.putInt("editText", startTime.getId());
					timePicker.setArguments(args);
					timePicker.show(fragMan, "SelectTime");
				}
				
			}
		});
		

		//Listen for the end time click
		final EditText endTime = (EditText)findViewById(R.id.addTaskEndDate);
		if(!enabled)
			endTime.setEnabled(false);
		else
		endTime.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				
				if(hasFocus)
				{
					DialogFragment timePicker = new TimePickerFragment();
					Bundle args = new Bundle();
					args.putInt("editText", endTime.getId());
					timePicker.setArguments(args);
					timePicker.show(fragMan, "SelectTime");
				}
				
			}
		});
		
		if(!enabled)
		{
			EditText dateTxt = (EditText)findViewById(R.id.addTaskDate);
			dateTxt.setEnabled(false);
			
			EditText txtLocation = (EditText)findViewById(R.id.addTaskLocation);
			txtLocation.setEnabled(false);
			
			EditText txtTitle = (EditText)findViewById(R.id.addTaskTitle);
			txtTitle.setEnabled(false);
		}
	}
	
	private boolean checkValidation()
	{
		//Check date
		TextView txtDate = (TextView)findViewById(R.id.addTaskDate);
		String dateStr = txtDate.getText().toString();
		if(!checkDateTime(dateStr, "mm/dd/yy", "Invalid date format")) return false;
		
		//Check start time
		TextView txtStartDate = (TextView)findViewById(R.id.addTaskStartDate);
		String startDateStr = txtStartDate.getText().toString();
		if(!checkDateTime(startDateStr, "hh:mmaa", "Invalid start time")) return false;
		
		//Check end time
		TextView txtEndDate = (TextView)findViewById(R.id.addTaskEndDate);
		String endDateStr = txtEndDate.getText().toString();
		if(!checkDateTime(endDateStr, "hh:mmaa", "Invalid end time")) return false;
		
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
	
	private void writeEvent()
	{
		try
		{
			SimpleDateFormat dayFormat = new SimpleDateFormat("mm/dd/yy");
			EditText txtDate = (EditText)findViewById(R.id.addTaskDate);
			String strDate = txtDate.getText().toString();
			Date dayOf = dayFormat.parse(strDate);
			
			EditText txtStartTime = (EditText)findViewById(R.id.addTaskStartDate);
			SimpleDateFormat format = new SimpleDateFormat("hh:mmaa");
			String strStartTime = txtStartTime.getText().toString();
			Date startTimeDate = format.parse(strStartTime);
			
			EditText txtEndTime = (EditText)findViewById(R.id.addTaskEndDate);
			String strEndTime = txtEndTime.getText().toString();
			Date endTimeDate = format.parse(strEndTime);
			
			long startTime = dayOf.getTime() + startTimeDate.getTime();
			
			//Get the difference in minutes
			short duration = (short)((endTimeDate.getTime() - startTimeDate.getTime()) / 60000);
			
			Event event = new Event(startTime / 1000, duration, Event.DEFAULT_PRIORITY);
			
			//Set name
			EditText txtName = (EditText)findViewById(R.id.addTaskTitle);
			event.setName(txtName.getText().toString());
			
			//Set location
			EditText txtLocation = (EditText)findViewById(R.id.addTaskLocation);
			event.setLocation(txtLocation.getText().toString());
			
			//Events are locked
			event.lock();
			
			class MessageAsync extends AsyncTask<Event, Integer, Long>
			{
				@Override
				protected Long doInBackground(Event... params)
				{
					//Write event to the server
					MessageSender snd = new MessageSender(FrontendConstants.SERVER_ADDR, FrontendConstants.SERVER_PORT);
					snd.connect();
					ArrayList<Event> events = new ArrayList<Event>();
					
					boolean modify = true;
					for(Event ev : params)
					{
						events.add(ev);
						if(ev.getUID() == 0) modify = false;
					}
					snd.addEvents(events, FrontendConstants.USER_ID);
					snd.disconnect();
					
					return null;
				}
			}
			
			//Call the server asynchronously
			MessageAsync async = new MessageAsync();
			async.execute(event);
			
			//Return to old screen
			finish();
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void onTimeSelected(int id, int hour, int minute)
	{
		EditText timeText = (EditText)findViewById(id);
		
		int convertedHour = hour % 12;
		if(convertedHour == 0) convertedHour = 12;
		
		String ampm = hour / 12 == 0 ? "AM" : "PM";
		
		String strMin = Integer.toString(minute);
		if(strMin.length() == 1) strMin = "0" + strMin; //Prepend a 0 if needed
		
		String convertedHourStr = Integer.toString(convertedHour);
		if(convertedHourStr.length() == 1) convertedHourStr = "0" + convertedHourStr; //Prepend a 0 if needed
		
		timeText.setText(convertedHourStr + ":" + strMin + ampm);
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
				
				EventView parentActivity = (EventView)getActivity();
				parentActivity.onTimeSelected(editTextId, hourOfDay, minute);
			}
			
		}
	}

}
