package code.message.client;

import java.io.Serializable;

import code.common.ConfigInfo;

@SuppressWarnings("serial")
public class WriteCommitMessage extends RequestMessage implements Serializable {

	private int incrementValue;

	public WriteCommitMessage() {
	}

	public WriteCommitMessage(ConfigInfo config, int requestId, int dataObjectId, int incrementValue) {
		super(config, requestId, dataObjectId);
		this.incrementValue = incrementValue;
	}
	
	public int getIncrementValue() {
		return incrementValue;
	}
	
	@Override 
	public String toString() {
		return "[ WRITE_COMMIT_MESSAGE | FROM=" + getConfig().getFullId() + 
				" | REQ_ID=" + getRequestId() + 
				" | DATA_OBJ_ID=" + getDataObjectId() + " ]";
	}
}