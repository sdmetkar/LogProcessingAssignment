package dao;

import java.sql.Connection;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pojo.Event;

public class EventsDao implements IGenericDao<Event> {

	private static final Logger Logger = LoggerFactory.getLogger(EventsDao.class);
	private Connection connection;
	private static final String TABLE_NAME = "events_tbl";

	public EventsDao(Connection connection) {
		this.connection = connection;
	}



	public int create(Event event) {
		int result=0;
		try {

			Statement stmt = connection.createStatement();
			String query = "insert into " + TABLE_NAME + " values(" + "\'" + event.getId() + "\'" + ","
					+ event.getDuration() + "," + "\'" + event.getType() + "\'" + "," + "\'" + event.getHost() + "\'"
					+ "," + event.isAlert() + ");";
			Logger.debug("Query " + query);

			result=stmt.executeUpdate(query);
			stmt.close();
			Logger.info("Event " + event.getId() + " saved to db successfully ");
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
		}
		return result;

	}

	public Event update(Event event) {
		// TODO Auto-generated method stub
		return null;
	}

	public void delete(Event event) {
		// TODO Auto-generated method stub

	}

	public void deleteById(long eventId) {
		// TODO Auto-generated method stub

	}

	public void createTableIfNotExist() {

		try {

			Statement stmt = connection.createStatement();
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + TABLE_NAME
					+ " (id VARCHAR(25) NOT NULL, duration LONG NOT NULL,type VARCHAR(20),host VARCHAR(20),alert boolean,PRIMARY KEY (id));");
			//Logger.debug("Table created successfully "+count);
			stmt.close();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
		}

	}

}
