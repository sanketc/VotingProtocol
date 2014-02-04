package code.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

import code.common.ConfigInfo;
import code.common.Globals;
import code.common.MessageManager;
import code.message.Message;
import code.message.MessageEnums;
import code.message.MessageFactory;
import code.net.Connection;
import code.net.ConnectionAcceptor;
import code.net.ListnerThread;

/**
 * Starting point for client.
 */
public class ClientDriver {

	private ConfigInfo config;
	
	private MessageManager msgManager;
	
	private MessageFactory factory;
	
	/**
	 * <ID, Connection>
	 */
	private HashMap<String, Connection> connectionPoolTable;

	public ClientDriver(String id) throws Exception {
		Globals.initialize(id, Globals.CLIENT_STR);
		config = Globals.getMyConfig();
		msgManager = new MessageManager();
		factory = new MessageFactory();
		connectionPoolTable = new HashMap<String, Connection>();
	}

	public void start() throws Exception {
		
		ConnectionAcceptor connectionAcceptor = new ConnectionAcceptor(config, connectionPoolTable, msgManager);
		connectionAcceptor.start();
		
		/* Send connection messages to all servers */
		for(ConfigInfo serverConfig: Globals.getServerList()) {
			connect(serverConfig);
		}
		
		/* Start service */
		ClientService service = new ClientService(config, msgManager, connectionPoolTable);
		service.start();

		/* If last clientId then start send start of computation message 
		 * to all clients including self. */
		if(config.getIntId() != Globals.getClientCount())
			return;

		/* Send connection messages to all client ids */
		for(ConfigInfo clientConfig: Globals.getClientList()) {
			if(config.getIntId() != clientConfig.getIntId()) {
				connect(clientConfig);
			}
		}
		
		/* Send SOC message */
		Message socMessage = factory.generateMessage(MessageEnums.C_START, config);
		for(ConfigInfo clientConfig: Globals.getClientList()) {
			sendMessageWrapper(socMessage, clientConfig);
		}
	}
	
	public void connect(ConfigInfo receiverConfig) throws Exception {

		Socket socket = null;
		ObjectOutputStream outStream = null;
		ObjectInputStream inputStream = null;
		
		try {
			socket = new Socket(receiverConfig.getAddress(), receiverConfig.getPort());
			outStream = new ObjectOutputStream(socket.getOutputStream());
			inputStream = new ObjectInputStream(socket.getInputStream());
		} catch (UnknownHostException e) {
			String errMsg = "Don't know about host: " + receiverConfig.getAddress();
			Globals.logErrorMsg(errMsg);
			System.out.println(errMsg);
			System.exit(Globals.SYS_FAILURE);
		} catch (IOException e) {
			String errMsg = "Couldn't get I/O for the connection to: " + receiverConfig.getAddress();
			Globals.logErrorMsg(errMsg);
			System.out.println(errMsg);
			System.exit(Globals.SYS_FAILURE);
		}

		Message connectMessage = factory.generateMessage(MessageEnums.C_CONNECT, config);
		outStream.writeObject(connectMessage);
		outStream.flush();

		Globals.logSentMsg(connectMessage, receiverConfig);
		
		System.out.println("Connected to: " + receiverConfig);
		
		/* Update connection pool table */
		Connection con = new Connection(receiverConfig, socket, inputStream, outStream);
		connectionPoolTable.put(receiverConfig.getFullId(), con);
		
		/* Start listener thread */
		ListnerThread thread = new ListnerThread(con, msgManager);
		thread.start();
		System.out.println("Started listneing for : " + receiverConfig);
		
	}
	
	public static void main(String[] args) throws Exception {
		
		if(args.length != 1) {
			System.out.println("Missing argument !!");
			System.out.println("Usage:");
			System.out.println("        java code.server.ClientDriver <ClientId>");
			System.out.println("Exiting program now !!");
			System.exit(Globals.SYS_FAILURE);
		}
		
		String id = args[0];

		ClientDriver driver = new ClientDriver(id);
		System.out.println(" << CLIENT >> :: ID= '" + id + "' started !!");
		driver.start();
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
	}
}