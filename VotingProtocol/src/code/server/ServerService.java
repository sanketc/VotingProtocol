package code.server;

import java.util.ArrayList;
import java.util.HashMap;

import code.common.ConfigInfo;
import code.common.Globals;
import code.common.MessageManager;
import code.message.Message;
import code.message.client.RequestMessage;
import code.message.control.TerminateMessage;
import code.net.Connection;

/**
 * Server request handler.
 */
public class ServerService extends Thread {

	@SuppressWarnings("unused")
	private ConfigInfo config;

	private MessageManager msgManager;
	
	@SuppressWarnings("unused")
	private HashMap<String, Connection> connectionPoolTable;
	
	private HashMap<Integer, MessageManager> dataRequestQueueLookUpTable;
	
	private ArrayList<DataObjectService> dataObjectServiceList;
	
	public ServerService(ConfigInfo config, MessageManager msgManager, 
			HashMap<String, Connection> connectionPoolTable) {
		this.config = config;
		this.msgManager = msgManager;
		this.connectionPoolTable = connectionPoolTable;
		this.dataRequestQueueLookUpTable = new HashMap<Integer, MessageManager>();
		this.dataObjectServiceList = new ArrayList<DataObjectService>();
	
		for(int id = 0; id < Globals.DATA_OBJECT_COUNT; id++) {
			MessageManager dataMsgMgr = new MessageManager();
			dataRequestQueueLookUpTable.put(id, dataMsgMgr);
			DataObjectService dataServiceThread = new DataObjectService(config, id, dataMsgMgr, connectionPoolTable);
			dataObjectServiceList.add(dataServiceThread);
			dataServiceThread.setPriority(MAX_PRIORITY);
			dataServiceThread.start();			
		}
	}
	
	@Override
	public void run() {
		Message message;
		while(true){
			try {
				
				synchronized (msgManager) {
					while(msgManager.isEmptyQueue()) {
						msgManager.wait();
					}
					message = msgManager.getNextMessage();
				}
				/* Check for new message to dispatch to respective DataObjectServcie */
				handleMessage(message);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void handleMessage(Message message) throws Exception {
		Globals.logMsg("PROCESSING MESSAGE: " + message.getClass().getSimpleName() + " | " + message.getConfig());
		if(message instanceof TerminateMessage) {
			/* Print summary */
			for(DataObjectService obj: dataObjectServiceList)
				obj.printSummary();
			/* Exit program */
			System.out.println("Exiting program Normally !!");
			Globals.logMsg("Exiting program Normally !!");
			System.exit(Globals.SYS_SUCCESS);
		} else if(message instanceof RequestMessage) {
			handleRequestMessage((RequestMessage)message);
		} else {
			System.out.println(" Unknown Message Received : "  + message.getClass().getSimpleName());
		}
	}

	private void handleRequestMessage(RequestMessage message) {
		MessageManager dataMgr = dataRequestQueueLookUpTable.get(message.getDataObjectId());
		dataMgr.addLastMessage(message);
	}
}