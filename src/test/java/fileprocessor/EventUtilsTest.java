package fileprocessor;

import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Test;

import pojo.Event;
import pojo.LogEvent;

public class EventUtilsTest {

	@Test
	public void testAddOrUpdateEvent() {
		Map<String, Event> idToEventMap = new ConcurrentHashMap<String, Event>();
		EventUtils.addOrUpdateEvent(new LogEvent(), idToEventMap);
	}

	@Test
	public void testConvertLogLineToEvent() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetStartOrEndTimestamp() {
		fail("Not yet implemented");
	}

}
