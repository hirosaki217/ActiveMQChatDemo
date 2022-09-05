package data;

public class User {
	private String name;
	private String message;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public User(String name, String message) {
		super();
		this.name = name;
		this.message = message;
	}
	public User() {
		super();
	}
	@Override
	public String toString() {
		return "User [name=" + name + ", message=" + message + "]";
	}
	
}
