package fileprocessor;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import pojo.Event;
import pojo.LogEvent;

public class EventUtils {
	
	private static final Logger Logger = LoggerFactory.getLogger(EventUtils.class);
	
	static Map<String, Event> addOrUpdateEvent(LogEvent logEvent, Map<String, Event> idToEventMap) {
		if (idToEventMap.containsKey(logEvent.getId())) {
			Event event = idToEventMap.get(logEvent.getId());
			setStartOrEndTimestamp(event, logEvent);
			event.setDuration(event.getEndTimestamp() - event.getStartTimestamp());

			idToEventMap.put(event.getId(), event);
		} else {
			Event event = new Event();
			event.setId(logEvent.getId());

			setStartOrEndTimestamp(event, logEvent);

			event.setHost(logEvent.getHost());
			event.setType(logEvent.getType());

			idToEventMap.put(event.getId(), event);
		}
		return idToEventMap;
	}
	

	
	static LogEvent convertLogLineToEvent(String logLine) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		LogEvent logEvent = mapper.readValue(logLine, LogEvent.class);

		return logEvent;

	}

	static void setStartOrEndTimestamp(Event event, LogEvent logEvent) {
		if (logEvent.getState().equalsIgnoreCase("STARTED")) {
			event.setStartTimestamp(logEvent.getTimestamp());
		} else {
			event.setEndTimestamp(logEvent.getTimestamp());
		}
	}
}
