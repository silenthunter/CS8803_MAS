package scheduler.utils;

import java.sql.*;

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
	 * Add a new user to the database
	 * @param name Name of the user to add
	 * @return The unique identifier for the added user. Returns -1 if there is an error.
	 */
	public static int addUser(String name)
	{
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
	
}
