package code.message.control;

import java.io.Serializable;

import code.common.ConfigInfo;
import code.message.Message;

@SuppressWarnings("serial")
public class ConnectMessage extends Message implements Serializable {

	public ConnectMessage() {
	}

	public ConnectMessage(ConfigInfo config) {
		super(config);
	}
}