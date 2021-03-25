package fileprocessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
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

	public static void main(String[] args) throws SQLException {

		final File file = new File(
				"C:\\Users\\sunny\\workspace\\fileprocessor\\src\\main\\java\\fileprocessor\\logfile.txt");
		// promptUserForExistingFilePath();

		Connection connection = DbConnection.getInstance().getConnection();
		eventsDao = new EventsDao(connection);
		eventsDao.createTableIfNotExist();

		Thread fileProcessor = new Thread(new Runnable() {

			public void run() {
				try {
					readFileUsingBufferedReader(file);
				} catch (IOException e) {
					Logger.error(e.getMessage());
					e.printStackTrace();
				} catch (InterruptedException e) {
					Logger.error(e.getMessage());
					e.printStackTrace();
				}

			}
		});
		fileProcessor.start();

		Thread eventsMapProcessor = new Thread(new Runnable() {

			public void run() {
				try {
					while (true) {
						Thread.sleep(1000);
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
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
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
		eventsMapProcessor.start();

		// readFromDatabase();

	}

	public static LogEvent convertLineToEvent(String logLine) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		LogEvent logEvent = mapper.readValue(logLine, LogEvent.class);

		return logEvent;

	}

	public static void readFileUsingBufferedReader(File file) throws IOException, InterruptedException {

		FileInputStream fis = new FileInputStream(file);
		InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
		BufferedReader br = new BufferedReader(isr);

		String line;
		while ((line = br.readLine()) != null) {
			Thread.sleep(1000);
			// process the line
			LogEvent logEvent = convertLineToEvent(line);
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
			Logger.debug("Log " + logEvent.getId() + " " + logEvent.getState() + " updated on map");
		}
		Logger.debug("All records read from the file");
		allLogsReadFromFile = true;
		br.close();
	}

	private static void setStartOrEndTimestamp(Event event, LogEvent logEvent) {
		if (logEvent.getState().equalsIgnoreCase("STARTED")) {
			event.setStartTimestamp(logEvent.getTimestamp());
		} else {
			event.setEndTimestamp(logEvent.getTimestamp());
		}
	}

	public static void processEvents(final Map<String, Event> idToEventMap) throws InterruptedException {

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
}
