package code.message.client;

import java.io.Serializable;

import code.common.ConfigInfo;

@SuppressWarnings("serial")
public class WithdrawMessage extends RequestMessage implements Serializable {

	public WithdrawMessage() {
	}

	public WithdrawMessage(ConfigInfo config, int requestId, int dataObjectId) {
		super(config, requestId, dataObjectId);
	}
	
	@Override 
	public String toString() {
		return "[ WITHDRAW_MESSAGE | FROM=" + getConfig().getFullId() + 
				" | REQ_ID=" + getRequestId() + 
				" | DATA_OBJ_ID=" + getDataObjectId() + " ]";
	}
}