package code.net;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import code.common.ConfigInfo;
import code.common.Globals;
import code.common.MessageManager;
import code.message.Message;
import code.message.control.ConnectMessage;

/**
 * Accepts incoming connections and invokes separate listener threads.
 */
public class ConnectionAcceptor extends Thread {

	private ServerSocket serverSocket = null;
	
	private HashMap<String, Connection> connectionPoolTable;
	
	private MessageManager msgManager;
	
	public ConnectionAcceptor(ConfigInfo config, 
			HashMap<String, Connection> connectionPoolTable, MessageManager msgManager) {
		try {
			serverSocket = new ServerSocket(config.getPort());
		} catch (IOException e) {
			String msg = "Could not listen on port: " + config.getPort();
			Globals.logErrorMsg(msg);
			System.out.println(msg);
			return;
		}
		this.connectionPoolTable = connectionPoolTable;
		this.msgManager = msgManager;
	}
	
	public void Finalize() throws Exception {
		serverSocket.close();
	}
	
	@Override
	public void run() {
		try {
			while(true) {
				
				Socket socket = serverSocket.accept();
				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
				
				Message request = (Message) in.readObject();
				Globals.logReceivedMsg(request);
				
				if(request instanceof ConnectMessage) {
					Connection con = new Connection(request.getConfig(), socket, in, out);
					System.out.println("Connected to : " + request.getConfig());
					connectionPoolTable.put(request.getConfig().getFullId(), con);
					
					/* Start listener thread */
					System.out.println("Started listening for : " + request.getConfig());
					ListnerThread t = new ListnerThread(con, msgManager);
					t.start();
					
				} else {
					String msg = "Connection Manager: Unknown Message";
					System.out.println(msg);
					Globals.logErrorMsg(msg);
					System.exit(Globals.SYS_FAILURE);
				}

			}
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			System.exit(Globals.SYS_FAILURE);
		}

	}
}