package scheduler.frontend;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

public class Schedule extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_schedule);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.schedule, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		if(item.getItemId() == R.id.toDo)
		{
			Intent intent = new Intent(this, ToDoView.class);
			startActivity(intent);
		}
		else if(item.getItemId() == R.id.new_event)
		{
			Intent intent = new Intent(this, EventView.class);
			startActivity(intent);
		}
		else if(item.getItemId() == R.id.new_task)
		{
			Intent intent = new Intent(this, ItemView.class);
			startActivity(intent);
		}
		
		// TODO Auto-generated method stub
		return super.onOptionsItemSelected(item);
	}

}
