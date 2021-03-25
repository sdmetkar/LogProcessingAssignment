package fileprocessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import dao.DbConnection;
import dao.EventsDao;
import pojo.Event;
import pojo.LogEvent;

public class FileProcessor {
	private static final Logger Logger = LoggerFactory.getLogger(FileProcessor.class);

	private static Map<String, Event> idToEventMap = new ConcurrentHashMap<String, Event>();
	private static boolean allLogsReadFromFile = false;
	private static EventsDao eventsDao;
	// private static File file;

	public static void main(String[] args) throws SQLException, FileNotFoundException {

		final File file = new File(
				"C:\\Users\\sunny\\workspace\\fileprocessor\\src\\main\\java\\fileprocessor\\logfile.txt");
		// promptUserForExistingFilePath();
		FileInputStream fis = new FileInputStream(file);
		InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
		final BufferedReader br = new BufferedReader(isr);

		Connection connection = DbConnection.getInstance().getConnection();
		eventsDao = new EventsDao(connection);
		eventsDao.createTableIfNotExist();
		
		
		//Producer Thread: loops through lines, if id of current line is not yet present on map, put it on the map
		//otherwise update the existing object on map corresponding to the id
		Thread readLinesUpdateMapThread = new Thread(new Runnable() {

			public void run() {
				try {
					readLinesUpdateMap(br, idToEventMap);
				} catch (Exception e) {
					Logger.error(e.getMessage());
					e.printStackTrace();
				}
			}
		});
		readLinesUpdateMapThread.start();

		//Consumer Thread: loops through map entries, checks for objects for which duration is calculated , 
		//save such objects to database and remove them from map
		Thread readMapUpdateDatabaseThread = new Thread(new Runnable() {

			public void run() {
				try {
					while (true) {
						Thread.sleep(500);
						if (idToEventMap.size() > 0) {
							processEvents(idToEventMap);
						} else {

							if (allLogsReadFromFile) {
								Logger.debug("All events processed");
								break;
							} else {
								Logger.debug("Waiting for map to be filled");
							}

						}
					}
				} catch (Exception e) {
					Logger.error(e.getMessage());
					e.printStackTrace();
				} finally {
					try {
						DbConnection.getInstance().getConnection().close();
					} catch (SQLException e) {
						Logger.error(e.getMessage());
						e.printStackTrace();
					}
				}

			}
		});
		readMapUpdateDatabaseThread.start();

	}

	private static void readLinesUpdateMap(BufferedReader br, Map<String, Event> idToEventMap)
			throws IOException, InterruptedException {
		if (br != null) {
			String line;
			while ((line = br.readLine()) != null) {
				Thread.sleep(500);
				LogEvent logEvent = convertLogLineToEvent(line);
				addOrUpdateEvent(logEvent, idToEventMap);
				Logger.debug("Log \"" + logEvent.getId() + "-" + logEvent.getState() + "\" updated on map");
			}
			allLogsReadFromFile = true;
			Logger.info("All lines read and map updated");
			br.close();
		}
	}

	private static void setStartOrEndTimestamp(Event event, LogEvent logEvent) {
		if (logEvent.getState().equalsIgnoreCase("STARTED")) {
			event.setStartTimestamp(logEvent.getTimestamp());
		} else {
			event.setEndTimestamp(logEvent.getTimestamp());
		}
	}

	private static void processEvents(final Map<String, Event> idToEventMap) throws InterruptedException {

		Set<String> ids = idToEventMap.keySet();
		for (String id : ids) {

			Event event = idToEventMap.get(id);
			if (event.getDuration() != -1) {
				Logger.debug("Event ready to be logged to database: " + id);
				if (event.getDuration() >= 4) {
					Logger.warn("Event " + id + " " + "took more than 4 ms");
					event.setAlert(true);
				}

				eventsDao.create(event);
				idToEventMap.remove(id);
				Logger.debug("Events currently on the map: " + idToEventMap.size());
			}
		}

	}

	private static File promptUserForExistingFilePath() {
		File file = null;
		Scanner scanner = new Scanner(System.in);
		System.out.println("Enter path of log file which you want to process.");
		while (true) {
			String filePath = scanner.nextLine();
			Logger.debug("File path is: " + filePath);
			if (filePath == null || filePath.equalsIgnoreCase("")) {
				System.out.println("Enter path of log file which you want to process.");
			} else {
				file = new File(filePath);
				if (file.exists()) {
					break;
				} else {
					System.out.println(
							"File " + filePath + " does not exist. Enter path of log file which you want to process.");
				}
			}
		}
		scanner.close();
		return file;
	}

	private static LogEvent convertLogLineToEvent(String logLine) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		LogEvent logEvent = mapper.readValue(logLine, LogEvent.class);

		return logEvent;

	}

	private static Map<String, Event> addOrUpdateEvent(LogEvent logEvent, Map<String, Event> idToEventMap) {
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
}
