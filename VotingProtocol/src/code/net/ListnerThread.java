package code.net;

import java.io.ObjectInputStream;

import code.common.Globals;
import code.common.MessageManager;
import code.message.Message;

/**
 * Listens for messages on a port.
 */
public class ListnerThread extends Thread {

	private Connection connection;
	
	private MessageManager msgManager;
	
	public ListnerThread(Connection connection, MessageManager msgManager) {
		this.connection = connection;
		this.msgManager = msgManager;
	}
	
	@Override
	public void run() {
		try {
			ObjectInputStream inputStream = connection.getInputStream();
			while(true) {
				Message request = (Message) inputStream.readObject();
				Globals.logReceivedMsg(request);
				msgManager.addLastMessage(request);
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			System.exit(Globals.SYS_FAILURE);
		}
	}
}