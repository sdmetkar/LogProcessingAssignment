package dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

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

	public Event findOne(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Event> findAll() {
		List<Event> events = new ArrayList<Event>();
		try {

			Statement stmt = connection.createStatement();
			String query = "select * from events_tbl5;";
			Logger.debug("Query " + query);

			ResultSet result = stmt.executeQuery(query);
			while (result.next()) {
				Event event = new Event();
				event.setId(result.getString("id"));
				event.setDuration(result.getInt("duration"));
				event.setType(result.getString("type"));
				event.setHost(result.getString("host"));
				event.setAlert(result.getBoolean("alert"));
			}
			stmt.close();

		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
		}
		return events;

	}

	public void create(Event event) {
		try {

			Statement stmt = connection.createStatement();
			String query = "insert into " + TABLE_NAME + " values(" + "\'" + event.getId() + "\'" + ","
					+ event.getDuration() + "," + "\'" + event.getType() + "\'" + "," + "\'" + event.getHost() + "\'"
					+ "," + event.isAlert() + ");";
			Logger.debug("Query " + query);

			stmt.executeUpdate(query);
			stmt.close();
			Logger.info("Event " + event.getId() + " saved to db successfully ");
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
		}

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
					+ " (id VARCHAR(25) NOT NULL, duration INTEGER NOT NULL,type VARCHAR(20),host VARCHAR(20),alert boolean,PRIMARY KEY (id));");
			//Logger.debug("Table created successfully "+count);
			stmt.close();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
		}

	}

}
