package scheduler.events;

import java.nio.ByteBuffer;

public class Event
{
	final static short DEFAULT_PRIORITY = 3;
	
	private long startTime;
	private int duration;
	private short priority;
	private boolean locked = false;
	
	private String name;
	private String location;
	
	public Event(int duration)
	{
		this(0, duration, DEFAULT_PRIORITY);
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
	
	public long getStartTime()
	{
		return startTime;
	}
	
	public void setStartTime(long startTime)
	{
		this.startTime = startTime;
	}
	
	public int getDuration()
	{
		return duration;
	}
	
	public short getPriority()
	{
		return priority;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public String getLocation()
	{
		return location;
	}
	
	public void setLocation(String location)
	{
		this.location = location;
	}
	
	public boolean isLocked()
	{
		return locked;
	}
	
	public void lock()
	{
		locked = true;
	}
	
	public void unlock()
	{
		locked = false;
	}
	
	/**
	 * Shifts the start time of the event
	 * @param shiftAmount Amount of time in minutes to shift the event
	 */
	public void shiftStartTime(int shiftAmount)
	{
		startTime += shiftAmount * 60;
	}
	
	/**
	 * 
	 * @param buffer The buffer that contains the event
	 * @return The parsed Event
	 */
	public static Event ReadFromBuffer(byte[] buffer)
	{
		ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
		
		long startTimeTmp = byteBuffer.getLong();
		int durationTmp = byteBuffer.getInt();
		short priorityTmp = byteBuffer.getShort();
		
		Event retn = new Event(startTimeTmp, durationTmp, priorityTmp);
		
		//Read the Name
		short strLen = byteBuffer.getShort();
		while(strLen-- > 0)
		{
			retn.name += byteBuffer.getChar();
		}
		
		//Read the Location
		strLen = byteBuffer.getShort();
		while(strLen-- > 0)
		{
			retn.location += byteBuffer.getChar();
		}
		
		return retn;
	}
	
	/**
	 * Turns an event into a byte array
	 * @param event Event to parse
	 * @return byte array representing the given event
	 */
	public static byte[] WriteToBuffer(Event event)
	{
		byte[] retn = new byte[2048];
		ByteBuffer byteBuffer = ByteBuffer.wrap(retn);
		
		byteBuffer.putLong(event.startTime);
		byteBuffer.putInt(event.duration);
		byteBuffer.putShort(event.priority);
		
		//Write Name
		byteBuffer.putShort((short)event.name.length());
		for(int i = 0; i < event.name.length(); i++)
			byteBuffer.putChar(event.name.charAt(i));//TODO: Think of a better way to do this if performance issues arise
		
		//Write Location
		byteBuffer.putShort((short)event.location.length());
		for(int i = 0; i < event.location.length(); i++)
			byteBuffer.putChar(event.location.charAt(i));
		
		return retn;
	}
	
	public Event copy()
	{
		Event retn = new Event(startTime, duration, priority);
		retn.name = name;
		retn.location = location;
		
		return retn;
	}
}
