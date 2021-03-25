package fileprocessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.ObjectMapper;

import dao.DbConnection;
import pojo.Event;
import pojo.LogEvent;

public class FileProcessor {
	// private static final Logger logger =
	// LoggerFactory.getLogger(FileProcessor.class);

	private static Map<String, Event> idToEventMap = new ConcurrentHashMap<String, Event>();
	private static boolean allLogsReadFromFile=false;

	public static void main(String[] args) throws IOException {
		// System.out.println(convertLineToEvent("{\"id\":\"abc\",\"host\":\"google\"}").toString());
		 createTable();

		Thread fileProcessor = new Thread(new Runnable() {

			public void run() {
				try {
					readFileUsingBufferedReader();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
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
						if(idToEventMap.size()>0){
						processEvents(idToEventMap);
						}
						else{
							if(allLogsReadFromFile){
								
								break;
							}
							System.out.println("Waiting for map to be filled");
	
						}
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				finally{
					try {
						DbConnection.getInstance().getConnection().close();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
		});
		eventsMapProcessor.start();
		
		//readFromDatabase();

	}

	public static LogEvent convertLineToEvent(String logLine) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		LogEvent logEvent = mapper.readValue(logLine, LogEvent.class);

		return logEvent;

	}

	static void writeToDatabase(Event event) {
		try {

			Connection con = DbConnection.getInstance().getConnection();
			Statement stmt = con.createStatement();
			String query="insert into events_tbl5 values ("
					+ "\'"+event.getId() + "\'" + ","
					+ event.getDuration() + ","
					+ "\'"+"test"+ "\'"+","
					+ "\'"+ event.getHost() + "\'"+ ","
					+ event.isAlert()+");";
			System.out.println("Query "+query);
					
			
			int result = stmt.executeUpdate(query);
			System.out.println("Event "+event.getId() +"saved to db successfully ");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	static void readFromDatabase() {
		try {

			Connection con = DbConnection.getInstance().getConnection();
			Statement stmt = con.createStatement();
			String query="select * from events_tbl5;";
			System.out.println("Query "+query);
					
			
			ResultSet result = stmt.executeQuery(query);
			while(result.next()){
	            System.out.println(result.getString("id")+" | "+
	               result.getInt("duration")+" | "+
	               result.getString("type"));
	         }
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void createTable() {

		try {

			Connection con = DbConnection.getInstance().getConnection();
			Statement stmt = con.createStatement();
			// result = stmt.executeUpdate("DROP TABLE events_tbl");
			int result = stmt.executeUpdate(
					"CREATE TABLE IF NOT EXISTS events_tbl5 (id VARCHAR(25) NOT NULL, duration INTEGER NOT NULL,type VARCHAR(20),host VARCHAR(20),alert boolean,PRIMARY KEY (id));");
			System.out.println("Table created successfully");
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}

	}

	public static void readFileUsingBufferedReader() throws IOException, InterruptedException {
		String fileName = "C:\\Users\\sunny\\workspace\\fileprocessor\\src\\main\\java\\fileprocessor\\logfile.txt";
		File file = new File(fileName);
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
				event.setType(logEvent.getHost());

				idToEventMap.put(event.getId(), event);
			}
			System.out.println(idToEventMap.size());
		}
		System.out.println("All records read from the file");
		allLogsReadFromFile=true;
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

		System.out.println("Processing map ");

		Set<String> ids = idToEventMap.keySet();
		for (String id : ids) {
			Event event = idToEventMap.get(id);
			if (event.getDuration() != -1) {
				if (event.getDuration() >= 4) {
					event.setAlert(true);
				}

				writeToDatabase(event);
				idToEventMap.remove(id);
				System.out.println("Objects left to be processed" + idToEventMap.size());
			}
		}

	}

}
