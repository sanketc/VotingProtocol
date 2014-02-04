package code.message.control;

import java.io.Serializable;

import code.common.ConfigInfo;
import code.message.Message;

@SuppressWarnings("serial")
public class StartOfComputationMessage extends Message implements Serializable {

	public StartOfComputationMessage() {
	}

	public StartOfComputationMessage(ConfigInfo config) {
		super(config);
	}
}