package dao;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;

import pojo.Event;

public class EventsDaoTest {
	
	private EventsDao eventsDao;
	
	@Before
	public void setUp(){
		Connection connection = null;
		try {
			connection = DbConnection.getInstance().getConnection();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		eventsDao=new EventsDao(connection);
	}

	@Test
	public void testCreate() {
		int result=eventsDao.create(getTestEvent());
		assert(result==1);
	}


	
	private Event getTestEvent(){
		Event event=new Event();
		event.setId("sun");
		event.setDuration(3);
		event.setHost("fads");
		event.setType("application_log");
		return event;
	}

}
