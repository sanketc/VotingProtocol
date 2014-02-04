package code.message.control;

import java.io.Serializable;

import code.common.ConfigInfo;
import code.message.Message;

@SuppressWarnings("serial")
public class TerminateMessage extends Message implements Serializable {

	public TerminateMessage() {
	}

	public TerminateMessage(ConfigInfo config) {
		super(config);
	}
}