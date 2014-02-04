package code.message.client;

import java.io.Serializable;

import code.common.ConfigInfo;

@SuppressWarnings("serial")
public class ReadCommitMessage extends RequestMessage implements Serializable {

	public ReadCommitMessage() {
	}

	public ReadCommitMessage(ConfigInfo config, int requestId, int dataObjectId) {
		super(config, requestId, dataObjectId);
	}
	
	@Override 
	public String toString() {
		return "[ READ_COMMIT_MESSAGE | FROM=" + getConfig().getFullId() + 
				" | REQ_ID=" + getRequestId() + 
				" | DATA_OBJ_ID=" + getDataObjectId() + " ]";
	}
}