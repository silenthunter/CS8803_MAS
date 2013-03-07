package scheduler.utils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import scheduler.events.Event;

public class DatabaseUtils
{
	final static String RemoteDB = "schedulerdb.cpzywqusv1wc.us-east-1.rds.amazonaws.com";
	final static String UserName = "schUser";
	final static String Password = "8803MAS";
	
	static Connection conn = null;
	
	/**
	 * Set up the database connection
	 */
	public static void init()
	{
		try
		{
			conn = DriverManager.getConnection("jdbc:mysql://" + RemoteDB + "/schedDB?" +
			        "user=" + UserName + "&password=" + Password);
		} 
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Adds an event to the database
	 * @param event The event being added
	 * @return The unique identifier of the event that was just added. Returns -1 if there is an error.
	 */
	public static int addEvent(Event event)
	{
		upkeepConnection();
		
		Statement stmt = null;
		int UID = -1;
		
		try
		{
			stmt = conn.createStatement();
			UID = stmt.executeUpdate("INSERT INTO Events (StartTime, Duration, Name, Location) " +
					"VALUES ('" + event.getStartTime() + "', " +
							"'" + event.getDuration() + "', '" +
							event.getName() + "', '" + event.getLocation() + "')", Statement.RETURN_GENERATED_KEYS);
			
		} 
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			//Cleanup
			try 
			{
				if(stmt != null) stmt.close();
			} catch (SQLException e) {}
		}
		
		return UID;
	}
	
	/**
	 * Deletes events with a certain UID
	 * @param eventIDs A list containing the UIDs of the events
	 */
	public static void deleteEvents(List<Integer> eventIDs)
	{
		upkeepConnection();
		
		Statement stmt = null;
		String cmd = "DELETE FROM Events WHERE uid=" + eventIDs.get(0);
		String cmdInter = "DELETE FROM Inter WHERE eventUid=" + eventIDs.get(0);
		
		//Add each id to the query
		for(int i = 0; i < eventIDs.size(); i++)
		{
			cmd += " OR uid=" + eventIDs.get(i);
			cmdInter += " OR uid=" + eventIDs.get(i);
		}
		
		try
		{
			stmt = conn.createStatement();
			stmt.execute(cmd);
			stmt.execute(cmdInter);
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Modifies existing events
	 * @param events The events to overwrite
	 */
	public static void modifyEvents(List<Event> events)
	{
		upkeepConnection();
		
		try
		{
			Statement stmt = null;
			stmt = conn.createStatement();
			
			//Update each event
			for(Event event : events)
			{
				String cmd = "UPDATE Events SET " +
						"Name=" + event.getName() +
						" StartTime=" + event.getStartTime() +
						" Duration=" + event.getDuration() +
						" Location=" + event.getLocation() +
						" WHERE uid=" + event.getUID();
				
				stmt.execute(cmd);
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Add a new user to the database
	 * @param name Name of the user to add
	 * @return The unique identifier for the added user. Returns -1 if there is an error.
	 */
	public static int addUser(String name)
	{
		upkeepConnection();
		
		Statement stmt = null;
		int UID = -1;
		
		try
		{
			stmt = conn.createStatement();
			UID = stmt.executeUpdate("INSERT INTO Users (Name) VALUES ('" + name + "')", Statement.RETURN_GENERATED_KEYS);
		} 
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			//Cleanup
			try 
			{
				if(stmt != null) stmt.close();
			} catch (SQLException e) {}
		}
		
		return UID;
	}
	
	/**
	 * Connects a user to an event
	 * @param userUID The user's unique identifier in the database
	 * @param eventUID The event's unique identifier in the database
	 * @param priority The user's priority for this event
	 * @return The unique identifier for the interconnect entry. Returns -1 if there is an error.
	 */
	public static int addUserToEvent(int userUID, int eventUID, int priority)
	{
		upkeepConnection();
		
		Statement stmt = null;
		int UID = -1;
		
		try
		{
			stmt = conn.createStatement();
			UID = stmt.executeUpdate("INSERT INTO Inter(EventUID, UserUID, Priority) VALUES('" + eventUID +
					"', '" + userUID + "', '" + priority + "')", Statement.RETURN_GENERATED_KEYS);
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			//Cleanup
			try 
			{
				if(stmt != null) stmt.close();
			} catch (SQLException e) {}
		}
		
		return UID;
	}
	
	public static ArrayList<Event> getEventsForUser(int userUID)
	{
		upkeepConnection();
		
		Statement stmt = null;
		ArrayList<Event> retn = new ArrayList<Event>();
		
		try
		{
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT ev.UID, ev.Name, ev.Location, ev.StartTime, ev.Duration, intr.Priority FROM Inter intr, Events ev, Users usr WHERE " +
					"intr.EventUID = ev.UID AND " +
					"intr.UserUID = usr.UID AND " +
					"usr.UID = " +userUID);
			
			//Read event information
			while(rs.next())
			{
				//SQL returns start at 1...
				int UID = rs.getInt(1);
				String name = rs.getString(2);
				String location = rs.getString(3);
				long startTime = rs.getLong(4);
				int duration = rs.getInt(5);
				short priority = rs.getShort(6);
				
				Event ev = new Event(startTime, duration, priority);
				ev.setName(name);
				ev.setLocation(location);
				ev.setUID(UID);
				
				retn.add(ev);
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			//Cleanup
			try 
			{
				if(stmt != null) stmt.close();
			} catch (SQLException e) {}
		}
		
		return retn;
	}
	
	private static void upkeepConnection()
	{
		try
		{
			if(!conn.isValid(1))
			{
				//Close the old connection and open a new one
				conn.close();
				init();
			}
		} catch(SQLException e)
		{
			e.printStackTrace();
		}
	}
	
}
