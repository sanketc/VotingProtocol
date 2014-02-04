package code.message.client;

import java.io.Serializable;

import code.common.ConfigInfo;

@SuppressWarnings("serial")
public class TimeOutMessage extends RequestMessage implements Serializable {

	public TimeOutMessage() {
	}

	public TimeOutMessage(ConfigInfo config, int requestId, int dataObjectId) {
		super(config, requestId, dataObjectId);
	}
	
	@Override 
	public String toString() {
		return "[ TIMEOUT_MESSAGE | FROM=" + getConfig().getFullId() + 
				" | REQ_ID=" + getRequestId() + 
				" | DATA_OBJ_ID=" + getDataObjectId() + " ]";
	}
}