package fileprocessor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import pojo.Event;
import pojo.LogEvent;

public class EventUtilsTest {

	@Test
	public void testAddOrUpdateEvent() {
		Map<String, Event> idToEventMap = new ConcurrentHashMap<String, Event>();
		EventUtils.addOrUpdateEvent(getTestLogEvent(), idToEventMap);
		assert (idToEventMap.size() == 1);
	}

	@Test
	public void testConvertLogLineToEvent() {
		try {
			LogEvent logEvent = EventUtils.convertLogLineToLogEvent(
					"{\"id\":\"1\",\"timestamp\":21,\"state\":\"finished\",\"type\":\"APPLICATION_LOG\",\"host\":\"google1\"}");
			assert (logEvent.getTimestamp() == 21);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testSetStartOrEndTimestamp() {
		Event event = getTestEvent();
		LogEvent logEvent = getTestLogEvent();
		EventUtils.setStartOrEndTimestamp(event, logEvent);
		assert (event.getEndTimestamp() == 5);
	}

	private Event getTestEvent() {
		Event event = new Event();
		event.setId("sun");
		event.setDuration(3);
		event.setHost("fads");
		event.setType("application_log");
		return event;
	}

	private LogEvent getTestLogEvent() {
		LogEvent logEvent = new LogEvent();
		logEvent.setId("sun");
		logEvent.setState("finished");
		logEvent.setTimestamp(5);
		logEvent.setHost("fads");
		logEvent.setType("application_log");
		return logEvent;
	}

	@Test
	public void createTestData() throws IOException {
		File file = new File("C:\\Users\\sunny\\logfile_1.txt");
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		OutputStreamWriter osr = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
		final BufferedWriter bw = new BufferedWriter(osr);
		for (int i = 0; i < 10000; i++) {
			LogEvent startedLogEvent=getRandomLogEventStarted();


			ObjectMapper objectMapper = new ObjectMapper();
			
			String json=objectMapper.writeValueAsString(startedLogEvent);
			bw.write(json);
			bw.newLine();
			LogEvent finishedLogEvent=getRandomLogEventFinished(startedLogEvent);
			json=objectMapper.writeValueAsString(finishedLogEvent);
			bw.write(json);
			bw.newLine();
		}
		bw.close();
	}

	private LogEvent getRandomLogEventStarted() {
		LogEvent logEvent = new LogEvent();
		logEvent.setId(""+Math.abs(new Random().nextInt()));
		logEvent.setState("started");
		logEvent.setTimestamp(Math.abs(new Random().nextLong()));
		logEvent.setHost("fads");
		logEvent.setType("application_log");
		return logEvent;
	}
	
	private LogEvent getRandomLogEventFinished(LogEvent startLogEvent) {
		LogEvent logEvent = startLogEvent;
		logEvent.setState("finished");
		logEvent.setTimestamp(startLogEvent.getTimestamp()+new Random().nextInt(10 - 1 + 1) + 1);
		return logEvent;
	}
}
