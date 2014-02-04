package code.common;

import java.io.Serializable;

/**
 * Host Config information.
 */
@SuppressWarnings("serial")
public class ConfigInfo implements Serializable {

	private String id;

	private String address;

	private int port;
	
	/**
	 * Server or Client
	 */
	private String type;

	public ConfigInfo(String id, String address, int port, String type) {
		this.id = id;
		this.address = address;
		this.port = port;
		this.type = type;
	}

	public String getId() {
		return id;
	}

	public String getFullId() {
		return type + id;
	}

	public String getAddress() {
		return address;
	}

	public int getPort() {
		return port;
	}
	
	public int getIntId() {
		return Integer.parseInt(id);
	}
	
	@Override
	public String toString() {
		return "[ ID= " + id + " | ADDRESS=" + address + " | PORT=" + port + " | TYPE=" + type + " ]"; 
	}
}