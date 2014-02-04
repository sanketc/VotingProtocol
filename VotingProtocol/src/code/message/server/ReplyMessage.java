package code.message.server;

import java.io.Serializable;

import code.common.ConfigInfo;
import code.message.Message;

@SuppressWarnings("serial")
public abstract class ReplyMessage extends Message implements Serializable {

	private int requestId;
	
	private int dataObjectId;
	
	public ReplyMessage() {
	}

	public ReplyMessage(ConfigInfo config, int requestId, int dataObjectId) {
		super(config);
		this.requestId = requestId;
		this.dataObjectId = dataObjectId;
	}
	
	public int getRequestId() {
		return this.requestId;
	}
	
	public int getDataObjectId() {
		return dataObjectId;
	}
	
	@Override 
	public String toString() {
		return "[ REPLY_MESSAGE | FROM=" + getConfig().getFullId() + 
				" | REQ_ID=" + getRequestId() + 
				" | DATA_OBJ_ID=" + getDataObjectId() + " ]";
	}
}