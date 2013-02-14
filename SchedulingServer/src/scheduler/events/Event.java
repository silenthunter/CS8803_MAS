package scheduler.events;

import java.nio.ByteBuffer;

public class Event
{
	final static short DEFAULT_PRIORITY = 3;
	
	private long startTime;
	private int duration;
	private short priority;
	
	public Event(int duration)
	{
		
	}
	
	public Event(long startTime, int duration)
	{
		this(startTime, duration, DEFAULT_PRIORITY);
	}
	
	public Event(int duration, short priority)
	{
		this(0, duration, priority);
	}
	
	public Event(long startTime, int duration, short priority)
	{
		this.startTime = startTime;
		this.duration = duration;
		this.priority = priority;
	}
	
	/**
	 * 
	 * @param buffer
	 * @return
	 */
	public static Event ReadFromBuffer(byte[] buffer)
	{
		ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
		
		long startTimeTmp = byteBuffer.getLong();
		int durationTmp = byteBuffer.getInt();
		short priorityTmp = byteBuffer.getShort();
		
		Event retn = new Event(startTimeTmp, durationTmp, priorityTmp);
		return retn;
	}
}
