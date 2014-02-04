package code.message.server;

import java.io.Serializable;

import code.common.ConfigInfo;

@SuppressWarnings("serial")
public class GrantMessage extends ReplyMessage implements Serializable {

	public GrantMessage() {
	}

	public GrantMessage(ConfigInfo config, int requestId, int dataObjectId) {
		super(config, requestId, dataObjectId);
	}
	
	@Override 
	public String toString() {
		return "[ GRANT_MESSAGE | FROM=" + getConfig().getFullId() + 
				" | REQ_ID=" + getRequestId() + 
				" | DATA_OBJ_ID=" + getDataObjectId() + " ]";
	}
}