package code.message.client;

import java.io.Serializable;

import code.common.ConfigInfo;

@SuppressWarnings("serial")
public class ReadRequestMessage extends RequestMessage implements Serializable {

	public ReadRequestMessage() {
	}

	public ReadRequestMessage(ConfigInfo config, int requestId, int dataObjectId) {
		super(config, requestId, dataObjectId);
	}
	
	@Override 
	public String toString() {
		return "[ READ_REQUEST_MESSAGE | FROM=" + getConfig().getFullId() + 
				" | REQ_ID=" + getRequestId() + 
				" | DATA_OBJ_ID=" + getDataObjectId() + " ]";
	}
}