package code.client;

import java.util.HashMap;
import java.util.HashSet;

import code.common.CommonAPI;
import code.common.ConfigInfo;
import code.common.Globals;
import code.common.MessageManager;
import code.message.Message;
import code.message.MessageEnums;
import code.message.MessageFactory;
import code.message.client.ReadRequestMessage;
import code.message.client.RequestMessage;
import code.message.client.TimeOutMessage;
import code.message.client.WriteRequestMessage;
import code.message.control.EndOfComputationMessage;
import code.message.control.StartOfComputationMessage;
import code.message.control.TerminateMessage;
import code.message.server.DataMessage;
import code.message.server.GrantMessage;
import code.net.Connection;
import code.server.OperationTypeEnums;

/**
 * Services all types of messages.
 */
public class ClientService extends Thread {
	
	private ConfigInfo config;
	
	private MessageManager msgManager;
	
	private MessageFactory factory;
	
	private HashMap<String, Connection> connectionPoolTable;
	
	private CommonAPI commonAPI;
	
	private int unsuccessfulRead = 0 ; 
	private int unsuccessfulWrite = 0 ;
	
	private int numberOfReadOperations = 0;
	private int numberOfWriteOperations = 0;
	
	private int totalMessageCount = 0;
	
	private int iterationCount = 1;
	private int noOfEocMessagesRcv = 0;
	
	private Message lastRequestMsg = null;
	private Timer lastRequestTimer = null;
	private ConfigInfo lastRequestServerConfig = null;
	private HashSet<Integer> grantSet = null;
	
	private long startTime = 0;

	private long minReadAccessTime = 0;
	private long maxReadAccessTime = 0;
	
	private long minWriteAccessTime = 0;
	private long maxWriteAccessTime = 0;

	private long totalReadTime = 0;
	private long totalWriteTime = 0;

	public ClientService(ConfigInfo config, MessageManager msgManager,
			HashMap<String, Connection> connectionPoolTable) {
		this.config = config;
		this.msgManager = msgManager;
		this.factory = new MessageFactory();
		this.connectionPoolTable = connectionPoolTable;
		commonAPI = new CommonAPI();
	}
	
	@Override
	public void run() {
		Message message;
		while (true) {
			try {

				/* Check for new message */
				synchronized (msgManager) {
					while (msgManager.isEmptyQueue()) {
						msgManager.wait();
					}
					message = msgManager.getNextMessage();
				}
				/* Handle message */
				handleMessage(message);
			
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void handleMessage(Message message) throws Exception {
		Globals.logMsg("PROCESSING MESSAGE: " + message.getClass().getSimpleName() + " | " + message.getConfig());
		totalMessageCount++;
		if(message instanceof StartOfComputationMessage) {
			performOperation();
		} else if(message instanceof EndOfComputationMessage) {
			handleEndOfComputationMessage((EndOfComputationMessage)message);
		} else if(message instanceof TerminateMessage) {
			System.out.println("Exiting program Normally !!");
			Globals.logMsg("Exiting program Normally !!");
			System.exit(Globals.SYS_SUCCESS);
		} else if(message instanceof DataMessage) {
			handleDataMessage((DataMessage)message);
		} else if(message instanceof TimeOutMessage) {
			handleTimeOutMessage((TimeOutMessage)message);
		} else if(message instanceof GrantMessage) {
			handleGrantMessage((GrantMessage)message);
		} else {
			System.out.println(" Unknown Message Received : "  + message.getClass().getSimpleName());
		}
	}
	
	private void handleGrantMessage(GrantMessage message) throws Exception {
		
		/* Check if grant message if valid */
		if(lastRequestMsg == null) 
			return;
		if(((RequestMessage)lastRequestMsg).getRequestId() != message.getRequestId() ) {
			/* This means that the request has timeout before */
			Globals.logMsg("SKIPPING : " + message);
			return;		
		}
		
		/* Add current grant in grant set */
		grantSet.add(message.getConfig().getIntId());
		
		if(!hasWriteQuorumFormed())
			return;
		
		/* Update timing details */
		updateTimingDetails();
		
		/* Stop timer */
		lastRequestTimer.stopTimer();
		/* CS */
		sleep(Globals.getHoldTime());

		/* Send write commit to all server */
		Message commitMessage = factory.generateMessage(MessageEnums.WRITE_COMMIT, config, 
				message.getRequestId(), message.getDataObjectId(), Globals.INCREMENT_VALUE);
		for(ConfigInfo serverConfig: Globals.getServerList()) {
			sendMessageWrapper(commitMessage, serverConfig);
		}
		
		/* Log value locally */
		Globals.logMsg("WRITE GRANT RECEIVED FROM SERVER_IDs = " + grantSet + 
				" | DATA VALUE INCREMENTED_BY = " + Globals.INCREMENT_VALUE);
		
		/* Reset values */
		resetValues();
		
		/* loop */
		performOperation();
	}

	private boolean hasWriteQuorumFormed() {
		for(HashSet<Integer> qSet: Globals.getQuorumList()) {
			if(qSet.size() == grantSet.size()) {
				if(qSet.containsAll(grantSet))
					return true;
			}
		}
		return false;
	}

	private void handleTimeOutMessage(TimeOutMessage message) throws Exception {
		
		/* Check if timeout message if valid */
		if(lastRequestMsg == null) 
			return;
		if(((RequestMessage)lastRequestMsg).getRequestId() != message.getRequestId() ) {
			/* This means that the request has been processed before */
			Globals.logMsg("SKIPPING : " + message);
			return;		
		}
		
		Message withdrawMessage = factory.generateMessage(MessageEnums.WITHDRAW, config, 
				message.getRequestId(), message.getDataObjectId());
		
		if(lastRequestMsg instanceof ReadRequestMessage) {
			unsuccessfulRead++;
			
			/* Send withdraw message */
			sendMessageWrapper(withdrawMessage, lastRequestServerConfig);
			
			/* Log timeout */
			Globals.logMsg("TIMEOUT FOR READ REQUEST : " + (ReadRequestMessage)lastRequestMsg);
			
		} else if(lastRequestMsg instanceof WriteRequestMessage) {
			unsuccessfulWrite++;
			/* Send withdraw message */
			for(ConfigInfo serverConfig: Globals.getServerList()) {
				sendMessageWrapper(withdrawMessage, serverConfig);
			}

			/* Log timeout */
			Globals.logMsg("TIMEOUT FOR WRITE REQUEST : " + (WriteRequestMessage)lastRequestMsg);
		}
		
		/* Reset values */
		resetValues();
		
		/* loop */
		performOperation();
	}
	
	private void resetValues() {
		lastRequestMsg = null;
		lastRequestTimer = null;
		lastRequestServerConfig = null;
		grantSet = null;
	}

	private void handleDataMessage(DataMessage message) throws Exception {
		
		/* Check if DataMessage is valid. i.e Data reply is for current request */
		if(lastRequestMsg == null)
			return;
		if(((RequestMessage)lastRequestMsg).getRequestId() != message.getRequestId() ) {
			/* This means that the request has timed out before it could get a data reply */
			Globals.logMsg("SKIPPING : " + message);
			return;		
		}
		
		/* Update timing details */
		updateTimingDetails();
		
		/* Stop timer */
		lastRequestTimer.stopTimer();
		/* CS */
		sleep(Globals.getHoldTime());
		
		/* Send read commit to server */
		Message commitMessage = factory.generateMessage(MessageEnums.READ_COMMIT, config, 
				message.getRequestId(), message.getDataObjectId());
		sendMessageWrapper(commitMessage, message.getConfig());
		
		/* Log value locally */
		Globals.logMsg("READ REPLY FROM SERVER_ID = " + message.getConfig().getId() + 
				" | DATA VALUE RECEIVED = " + message.getDataValue());
		
		/* Reset values */
		resetValues();
		
		/* loop */
		performOperation();
	}

	/**
	 * Perform one operation.
	 */
	void performOperation() throws Exception {
		if (iterationCount <= Globals.ITERATION_COUNT) {
			/* Sleep */
			sleep(commonAPI.getRandomDelay());
			/* Perform Operation */
			OperationTypeEnums type = commonAPI.getRandomOperation();
			if (type == OperationTypeEnums.READ) {
				executeReadOperation();
			} else { /* For write operation */
				executeWriteOperation();
			}
			iterationCount++;
		} else {
			/* Send end of computation message */
			Message eocMessage = factory.generateMessage(MessageEnums.C_END, config);
			ConfigInfo controllerClientConfig = Globals.getClientLookUpTable().get(Globals.getClientCount());
			sendMessageWrapper(eocMessage, controllerClientConfig);
			/* Print summary */
			printSummary();
		}
	}

	private void printSummary() {
		Globals.logMsg("*************** SUMMARY *****************");
		Globals.logMsg("TOTAL OPERATIONS				= " + Globals.ITERATION_COUNT);
		Globals.logMsg("HOLD_TIME						= " + (double)Globals.getHoldTime()/(double)Globals.END_CORRECTION);
		Globals.logMsg("TOTAL READ OPERATIONS			= " + numberOfReadOperations);
		Globals.logMsg("TOTAL WRITE OPERATIONS			= " + numberOfWriteOperations);
		Globals.logMsg("TOTAL UNSUCESSFUL READ			= " + unsuccessfulRead);
		Globals.logMsg("TOTAL UNSUCESSFUL WRITE		= " + unsuccessfulWrite);
		Globals.logMsg("TOTAL MESSAGE EXCHANGED		= " + totalMessageCount);
		
		int successfulRead = (numberOfReadOperations - unsuccessfulRead);
		double avgReadAccessTime = 0;
		if(successfulRead != 0)
			avgReadAccessTime = totalReadTime / successfulRead;
		Globals.logMsg("MIN READ ACCESS TIME			= " + (double)minReadAccessTime/(double)Globals.END_CORRECTION);
		Globals.logMsg("AVG READ ACCESS TIME			= " + (double)avgReadAccessTime/(double)Globals.END_CORRECTION);
		Globals.logMsg("MAX READ ACCESS TIME			= " + (double)maxReadAccessTime/(double)Globals.END_CORRECTION);

		int successfulWrite = (numberOfWriteOperations - unsuccessfulWrite);
		double avgWriteAccessTime = 0;
		if(successfulWrite != 0)
			avgWriteAccessTime = totalWriteTime / successfulWrite;
		Globals.logMsg("MIN WRITE ACCESS TIME			= " + (double)minWriteAccessTime/(double)Globals.END_CORRECTION);
		Globals.logMsg("AVG WRITE ACCESS TIME			= " + (double)avgWriteAccessTime/(double)Globals.END_CORRECTION);
		Globals.logMsg("MAX WRITE ACCESS TIME			= " + (double)maxWriteAccessTime/(double)Globals.END_CORRECTION);
		Globals.logMsg("*****************************************");
	}

	private void handleEndOfComputationMessage(EndOfComputationMessage message) throws Exception {
		noOfEocMessagesRcv++;
		/* Send Termination messages to all */
		if ( Globals.getClientCount() == noOfEocMessagesRcv ) {
			Message terMessage = factory.generateMessage(MessageEnums.C_TERMINATE, config);
			for(ConfigInfo serverConfig: Globals.getServerList()) {
				sendMessageWrapper(terMessage, serverConfig);
			}
			for(ConfigInfo clientConfig: Globals.getClientList()) {
				sendMessageWrapper(terMessage, clientConfig);
			}
		}
	}

	/* --------------------- Read Operation -----------------------  */

	private void executeReadOperation() throws Exception {
		numberOfReadOperations++;
		startTime = System.currentTimeMillis();
		int dataObjectId = commonAPI.getRandomDataObjectId();
		
		Message readRequestMessage = factory.generateMessage(MessageEnums.READ_REQUEST, 
				config, iterationCount, dataObjectId);
		lastRequestMsg = readRequestMessage;
		
		ConfigInfo serverConfig = commonAPI.getRandomServer();
		lastRequestServerConfig = serverConfig;
		
		/* Send request */
		sendMessageWrapper(readRequestMessage, serverConfig);
		
		/* Start awaiting timer */
		Timer timer = new Timer(config, msgManager, iterationCount, dataObjectId);
		lastRequestTimer = timer;
		timer.start();
	}

	/* --------------------- Write Operation -----------------------  */
	
	private void executeWriteOperation() throws Exception {
		numberOfWriteOperations++;
		startTime = System.currentTimeMillis();
		int dataObjectId = commonAPI.getRandomDataObjectId();
		
		Message writeRequestMessage = factory.generateMessage(MessageEnums.WRITE_REQUEST,
				config, iterationCount, dataObjectId, Globals.INCREMENT_VALUE);
		lastRequestMsg = writeRequestMessage;
		lastRequestServerConfig = null;
		
		/* Send request to all servers */
		for(ConfigInfo serverConfig: Globals.getServerList()) {
			sendMessageWrapper(writeRequestMessage, serverConfig);
		}
		
		/* Start awaiting timer */
		Timer timer = new Timer(config, msgManager, iterationCount, dataObjectId);
		lastRequestTimer = timer;
		timer.start();
		
		grantSet = new HashSet<Integer>();
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
			msgManager.addLastMessage(message);
		} else {
			/* Message for others */
			Connection con = connectionPoolTable.get(receiverId);
			con.sendMessage(message);
		}
		
		/* Update message count */
		totalMessageCount++;
	}
	
	@SuppressWarnings("unused")
	private void debug(String msg) {
		String logMsg = "DEBUG: " + msg + " | " + Thread.currentThread().getStackTrace()[2];
		System.out.println(logMsg);
		Globals.logMsg(logMsg);
	}
	
	private void updateTimingDetails() {
		long endTime = System.currentTimeMillis();
		long timeDiff = endTime - startTime;
		/* To remove noise as the time may go beyond max limit due to log writing and slow thread. */
		timeDiff = (timeDiff > Globals.AWATING_GRANT_TIME + Globals.getHoldTime() ?
				(Globals.AWATING_GRANT_TIME + Globals.getHoldTime()) : timeDiff);
		
		if(lastRequestMsg instanceof ReadRequestMessage) {
			minReadAccessTime = minReadAccessTime == 0 ? timeDiff : Math.min(timeDiff, minReadAccessTime); 
			maxReadAccessTime = maxReadAccessTime == 0 ? timeDiff : Math.max(timeDiff, maxReadAccessTime); 
			totalReadTime += timeDiff;
		} else if(lastRequestMsg instanceof WriteRequestMessage) {
			minWriteAccessTime = minWriteAccessTime == 0 ? timeDiff : Math.min(timeDiff, minWriteAccessTime); 
			maxWriteAccessTime = maxWriteAccessTime == 0 ? timeDiff : Math.max(timeDiff, maxWriteAccessTime); 
			totalWriteTime += timeDiff;
		}
	}
}