package fileprocessor;

import static org.junit.Assert.fail;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Test;

import pojo.Event;
import pojo.LogEvent;

public class EventUtilsTest {

	@Test
	public void testAddOrUpdateEvent() {
		Map<String, Event> idToEventMap = new ConcurrentHashMap<String, Event>();
		EventUtils.addOrUpdateEvent(getTestLogEvent(), idToEventMap);
		assert(idToEventMap.size()==1);
	}

	@Test
	public void testConvertLogLineToEvent() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetStartOrEndTimestamp() {
		fail("Not yet implemented");
	}

	private Event getTestEvent(){
		Event event=new Event();
		event.setId("sun");
		event.setDuration(3);
		event.setHost("fads");
		event.setType("application_log");
		return event;
	}
	
	private LogEvent getTestLogEvent(){
		LogEvent logEvent=new LogEvent();
		logEvent.setId("sun");
		logEvent.setState("started");
		logEvent.setTimestamp(5);
		logEvent.setHost("fads");
		logEvent.setType("application_log");
		return logEvent;
	}
}
