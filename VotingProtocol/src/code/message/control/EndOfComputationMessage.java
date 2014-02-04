package code.message.control;

import java.io.Serializable;

import code.common.ConfigInfo;
import code.message.Message;

@SuppressWarnings("serial")
public class EndOfComputationMessage extends Message implements Serializable {

	public EndOfComputationMessage() {
	}

	public EndOfComputationMessage(ConfigInfo config) {
		super(config);
	}
}