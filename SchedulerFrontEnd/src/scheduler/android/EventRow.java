package scheduler.android;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TableRow;

public class EventRow extends TableRow
{
	public EventRow(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.event_row, this);
	}
}
