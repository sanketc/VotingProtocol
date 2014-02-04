package code.server;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import code.common.ConfigInfo;
import code.common.Globals;
import code.common.MessageManager;
import code.message.Message;
import code.message.MessageEnums;
import code.message.MessageFactory;
import code.message.client.ReadCommitMessage;
import code.message.client.ReadRequestMessage;
import code.message.client.RequestMessage;
import code.message.client.WithdrawMessage;
import code.message.client.WriteCommitMessage;
import code.message.client.WriteRequestMessage;
import code.net.Connection;

/**
 * This service listens and services for request for access
 * for the data object it holds.
 */
public class DataObjectService extends Thread {

	private ConfigInfo config;
	
	private int dataObjectId;
	
	private int dataValue;
	
	private LockEnums lock;
	
	private int read_lock_count = 0;
	
	private MessageManager dataMsgManager;
	
	private MessageFactory factory;
	
	private LinkedList<RequestMessage> requestWaitingQueue;
	
	private HashMap<String, Connection> connectionPoolTable;
	
	private int successfulRead = 0 ; 
	private int successfulWrite = 0 ;
	
	private int numberOfReadOperations = 0;
	private int numberOfWriteOperations = 0;
	
	/**
	 * Set of those requests that have been granted/locked.
	 */
	private HashSet<String> requestGrantSet;
	
	public DataObjectService(ConfigInfo config, int dataObjectId, 
			MessageManager dataMsgManager, HashMap<String, Connection> connectionPoolTable) {
		this.config = config;
		this.dataObjectId = dataObjectId;
		this.dataValue = dataObjectId;
		this.lock = LockEnums.NOT_LOCKED;
		this.dataMsgManager = dataMsgManager;
		this.factory = new MessageFactory();
		this.requestWaitingQueue = new LinkedList<RequestMessage>();
		this.connectionPoolTable = connectionPoolTable;
		this.requestGrantSet = new HashSet<String>();
	}
	
	@Override
	public void run() {
		Message message;
		while(true) {
			try {
				/* check for new message */
				synchronized (dataMsgManager) {
					while(dataMsgManager.isEmptyQueue()) {
						dataMsgManager.wait();
					}
					message = dataMsgManager.getNextMessage();
				}
				handleMessage(message, true);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void handleMessage(Message message, boolean isNewMessage) throws Exception {
		if(message instanceof ReadRequestMessage) {
			handleReadRequestMessage((ReadRequestMessage)message, isNewMessage);
		} else if(message instanceof ReadCommitMessage) {
			handleReadCommitMessage((ReadCommitMessage)message);
		} else if(message instanceof WriteRequestMessage) {
			handleWriteRequestMessage((WriteRequestMessage)message, isNewMessage);
		} else if(message instanceof WriteCommitMessage) {
			handleWriteCommitMessage((WriteCommitMessage)message);
		} else if(message instanceof WithdrawMessage) {
			handleWithdrawMessage((WithdrawMessage)message);
		} else {
			System.out.println(" Unknown Message : "  + message.getClass().getSimpleName());
		}
	}

	private void handleReadRequestMessage(ReadRequestMessage message, boolean isNewMessage) throws Exception {
		if(isNewMessage)
			numberOfReadOperations++;
		
		if(lock == LockEnums.NOT_LOCKED || lock == LockEnums.READ_LOCKED) {
			/* Lock */
			read_lock_count++;
			
			lock = LockEnums.READ_LOCKED;
			requestGrantSet.add(message.getUniqueString());
			
			/* Send DataMessage */
			Message dataMessage = factory.generateMessage(MessageEnums.DATA, config, 
					message.getRequestId(), message.getDataObjectId(), dataValue);
			sendMessageWrapper(dataMessage, message.getConfig());
		} else { /* WRITE_LOCKED */
			/* Queue the request */
			if(isNewMessage)
				requestWaitingQueue.addLast(message);
			else
				requestWaitingQueue.addFirst(message);
		}
	}
	
	private void handleReadCommitMessage(ReadCommitMessage message) throws Exception {
		/* Update lock */
		read_lock_count--;
		successfulRead++;
		
		if(read_lock_count == 0)
			lock = LockEnums.NOT_LOCKED;
		requestGrantSet.remove(message.getUniqueString());
		
		/* Handle pending messages */
		if(requestWaitingQueue.isEmpty())
			return;
		handleMessage(requestWaitingQueue.poll(), false);
	}
	
	private void handleWriteRequestMessage(WriteRequestMessage message, boolean isNewMessage) throws Exception {
		if(isNewMessage)
			numberOfWriteOperations++;
		
		if(lock == LockEnums.NOT_LOCKED) {
			/* Lock */
			lock = LockEnums.WRITE_LOCKED;
			requestGrantSet.add(message.getUniqueString());
			
			/* Send DataMessage */
			Message grantMessage = factory.generateMessage(MessageEnums.GRANT, config,
					message.getRequestId(), message.getDataObjectId());
			sendMessageWrapper(grantMessage, message.getConfig());
		} else { /* READ_LOCKED || WRITE_LOCKED*/
			/* Queue the request */
			if(isNewMessage)
				requestWaitingQueue.addLast(message);
			else
				requestWaitingQueue.addFirst(message);
		}
	}

	private void handleWriteCommitMessage(WriteCommitMessage message) throws Exception {
		// TODO: Why don't we check the status of the current lock ?
		/* Update value */
		dataValue += message.getIncrementValue();
		successfulWrite++;
		
		/* Check if this server had granted the request */
		if(requestGrantSet.contains(message.getUniqueString())) {
			/* Unlock */
			lock = LockEnums.NOT_LOCKED;
			requestGrantSet.remove(message.getUniqueString());
		} else {
			/* Delete message (if) from the pending request list */
			deleteElementFromQueue(message);
		}
		
		/* Handle pending messages */
		if(requestWaitingQueue.isEmpty())
			return;
		handleMessage(requestWaitingQueue.poll(), false);
	}

	private void handleWithdrawMessage(WithdrawMessage message) throws Exception {
		/* Check if this server had granted the request */
		if(requestGrantSet.contains(message.getUniqueString())) {
			/* Update lock */
			if(lock == LockEnums.WRITE_LOCKED) {
				lock = LockEnums.NOT_LOCKED;
			} else if(lock == LockEnums.READ_LOCKED) {
				read_lock_count--;
				if(read_lock_count == 0)
					lock = LockEnums.NOT_LOCKED;
			}
			requestGrantSet.remove(message.getUniqueString());
		} else {
			/* Delete message (if) from the pending request list */
			deleteElementFromQueue(message);
		}

		/* Handle pending messages */
		if(requestWaitingQueue.isEmpty())
			return;
		handleMessage(requestWaitingQueue.poll(), false);
	}
	
	/**
	 * Assumption : There can be only one message of given client id in the queue
	 * at a time.
	 */
	private void deleteElementFromQueue(RequestMessage requestMessage) {
		Message messageToDelete = null;
		for(RequestMessage m: requestWaitingQueue) {
			if(m.getUniqueString().equals(requestMessage.getUniqueString())) {
				messageToDelete = m;
			}
		}
		requestWaitingQueue.remove(messageToDelete);
	}
	
	/**
	 * Send message to other receiverConfig.
	 * @param message Message to send
	 * @param receiverConfig Message receiver configuration.
	 */
	private void sendMessageWrapper(Message message, ConfigInfo receiverConfig) throws Exception {
		String receiverId = receiverConfig.getFullId();
		if(config.getFullId().equals(receiverId)) {
			/* If message send to self then directly add to message queue */
			dataMsgManager.addLastMessage(message);
		} else {
			/* Message for others */
			Connection con = connectionPoolTable.get(receiverId);
			con.sendMessage(message);
		}
	}
	
	public void printSummary() {
		Globals.logMsg("*************** SUMMARY *****************");
		Globals.logMsg("DATA_OBJECT			= " + dataObjectId);
		Globals.logMsg("FINAL DATA_VALUE		= " + dataValue);
		Globals.logMsg("HOLD_TIME				= " + (double)Globals.getHoldTime()/(double)Globals.END_CORRECTION);
		Globals.logMsg("TOTAL READ OPERATIONS	= " + numberOfReadOperations);
		Globals.logMsg("TOTAL WRITE OPERATIONS	= " + numberOfWriteOperations);
		Globals.logMsg("TOTAL SUCESSFUL READ	= " + successfulRead);
		Globals.logMsg("TOTAL SUCESSFUL WRITE	= " + successfulWrite);
		Globals.logMsg("*****************************************");
	}
}