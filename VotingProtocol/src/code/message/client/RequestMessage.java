package code.message.client;

import java.io.Serializable;

import code.common.ConfigInfo;
import code.message.Message;

@SuppressWarnings("serial")
public abstract class RequestMessage extends Message implements Serializable {

	private int requestId;
	
	private int dataObjectId;

	public RequestMessage() {
	}

	public RequestMessage(ConfigInfo config, int requestId, int dataObjectId) {
		super(config);
		this.requestId = requestId;
		this.dataObjectId = dataObjectId;
	}

	public int getRequestId() {
		return requestId;
	}

	public int getDataObjectId() {
		return dataObjectId;
	}
	
	@Override 
	public String toString() {
		return "[ REQUEST_MESSAGE | FROM=" + getConfig().getFullId() + 
				" | REQ_ID=" + getRequestId() + 
				" | DATA_OBJ_ID=" + getDataObjectId() + " ]";
	}
	
	public String getUniqueString() {
		return "[" +getConfig().getFullId() + 
				"|" + getRequestId() + 
				"|" + getDataObjectId() + "]";
	}
}