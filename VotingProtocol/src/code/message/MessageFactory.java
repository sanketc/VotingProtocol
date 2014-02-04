package code.message;

import code.common.ConfigInfo;
import code.message.client.ReadCommitMessage;
import code.message.client.ReadRequestMessage;
import code.message.client.TimeOutMessage;
import code.message.client.WithdrawMessage;
import code.message.client.WriteCommitMessage;
import code.message.client.WriteRequestMessage;
import code.message.control.ConnectMessage;
import code.message.control.EndOfComputationMessage;
import code.message.control.StartOfComputationMessage;
import code.message.control.TerminateMessage;
import code.message.server.DataMessage;
import code.message.server.GrantMessage;

/**
 * Factory for message generation.
 */
public class MessageFactory {

	/**
	 * Generates message
	 * @param type Message.enum
	 * @param config Message Sender config
	 * @param object Can be int or int,int
	 * @return
	 */
	public Message generateMessage(MessageEnums type, ConfigInfo config, Object ... object) {
		int requestId = -1;
		int dataObjectId = -1;
		int incrementValue = 0;
		int dataValue = 0;
		
		if(object.length == 2) {
			requestId = (Integer) object[0];
			dataObjectId = (Integer) object[1];
		} else if(object.length == 3) {
			requestId = (Integer) object[0];
			dataObjectId = (Integer) object[1];
			incrementValue = (Integer) object[2];
			dataValue = (Integer) object[2];
		}
		
		switch(type) {
			
			case READ_REQUEST: return new ReadRequestMessage(config, requestId, dataObjectId);
			case READ_COMMIT: return new ReadCommitMessage(config, requestId, dataObjectId);
			
			case WRITE_REQUEST: return new WriteRequestMessage(config, requestId, dataObjectId, incrementValue);
			case WRITE_COMMIT: return new WriteCommitMessage(config, requestId, dataObjectId, incrementValue);
			
			case DATA: return new DataMessage(config, requestId, dataObjectId, dataValue);
			case GRANT: return new GrantMessage(config, requestId, dataObjectId);
			
			case TIMEOUT: return new TimeOutMessage(config, requestId, dataObjectId);
			case WITHDRAW: return new WithdrawMessage(config, requestId, dataObjectId);
			
			case C_START: return new StartOfComputationMessage(config);
			case C_END: return new EndOfComputationMessage(config);
			case C_TERMINATE: return new TerminateMessage(config);
			case C_CONNECT: return new ConnectMessage(config);
			
			default: return null;
		}
	}
	
}