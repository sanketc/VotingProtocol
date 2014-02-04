package code.client;

import code.common.ConfigInfo;
import code.common.Globals;
import code.common.MessageManager;
import code.message.Message;
import code.message.MessageEnums;
import code.message.MessageFactory;

/**
 * This class runs a timer for 20 time units 
 * and at the end adds TIMEOUT message in the MessageQueue 
 * of the client, indicating that the timeout for resource 
 * access has occurred.
 */
public class Timer extends Thread {

	private ConfigInfo config;
	
	private MessageManager msgManager;
	
	private MessageFactory factory;
	
	private Boolean stop = false;
	
	private int requestId;
	
	private int dataObjectId;
	
	public Timer(ConfigInfo config, MessageManager msgManager, int requestId, int dataObjectId) {
		this.config = config;
		this.msgManager = msgManager;
		factory = new MessageFactory();
		this.requestId = requestId;
		this.dataObjectId = dataObjectId;
	}
	
	@Override
	public void run() {
		/* Sleep */
		try {
			sleep(Globals.AWATING_GRANT_TIME);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		/* Check if thread has been stopped from outside */
		synchronized (stop) {
			if(stop == true) {
				return;
			}
		}
		
		/* Send timeout message */
		Message message = factory.generateMessage(MessageEnums.TIMEOUT, config, requestId, dataObjectId);
		msgManager.addLastMessage(message);
	}
	
	public void stopTimer() {
		synchronized (stop) {
			this.stop = true;
		}
	}
}