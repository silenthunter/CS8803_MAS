package scheduler.frontend;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.view.Menu;
import android.view.MenuItem;

public class Schedule extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_schedule);
		
		AlertDialog.Builder bld = new AlertDialog.Builder(this);
		bld.setMessage("Group Meeting");
		bld.setTitle("Time for an Event!");
		bld.setCancelable(true);
		bld.setPositiveButton("Ok", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				
			}
		});
		bld.show();
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
