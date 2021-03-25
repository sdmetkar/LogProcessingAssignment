package pojo;

public class Event {
	private String id;
	private int duration=-1;
	private String type;
	private String host;
	private boolean alert;
	private int startTimestamp;
	private int endTimestamp;


	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public int getDuration() {
		return duration;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public boolean isAlert() {
		return alert;
	}
	public void setAlert(boolean alert) {
		this.alert = alert;
	}
	
	public int getStartTimestamp() {
		return startTimestamp;
	}
	public void setStartTimestamp(int startTimestamp) {
		this.startTimestamp = startTimestamp;
	}
	public int getEndTimestamp() {
		return endTimestamp;
	}
	public void setEndTimestamp(int endTimestamp) {
		this.endTimestamp = endTimestamp;
	}

	
	@Override
	public String toString() {
		return id+" "+host+ " " +type;
	}

	
}
