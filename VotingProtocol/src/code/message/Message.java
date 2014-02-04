package code.message;

import java.io.Serializable;

import code.common.ConfigInfo;

@SuppressWarnings("serial")
public abstract class Message implements Serializable {

	/**
	 * Message sender config.
	 */
	private ConfigInfo config;
	
	public Message() {
	}

	public Message(ConfigInfo config) {
		this.config = config;
	}

	public ConfigInfo getConfig() {
		return config;
	}
}