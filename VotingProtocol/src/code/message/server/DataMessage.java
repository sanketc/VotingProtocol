package code.message.server;

import java.io.Serializable;

import code.common.ConfigInfo;

@SuppressWarnings("serial")
public class DataMessage extends ReplyMessage implements Serializable {

	private int dataValue;

	public DataMessage() {
	}

	public DataMessage(ConfigInfo config, int requestId, int dataObjectId, int dataValue) {
		super(config, requestId, dataObjectId);
		this.dataValue = dataValue;
	}

	public int getDataValue() {
		return dataValue;
	}
	
	@Override 
	public String toString() {
		return "[ DATA_MESSAGE | FROM=" + getConfig().getFullId() + 
				" | REQ_ID=" + getRequestId() + 
				" | DATA_OBJ_ID=" + getDataObjectId() +
				" | VALUE=" + getDataValue() + " ]";
	}
}