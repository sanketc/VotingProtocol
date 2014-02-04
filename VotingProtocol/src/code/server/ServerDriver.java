package code.server;

import java.util.HashMap;

import code.common.ConfigInfo;
import code.common.Globals;
import code.common.MessageManager;
import code.net.Connection;
import code.net.ConnectionAcceptor;

/**
 * Starting point for server.
 */
public class ServerDriver {

	private ConfigInfo config;
	
	private MessageManager msgManager;
	
	private HashMap<String, Connection> connectionPoolTable;
	
	public ServerDriver(String id) throws Exception {
		Globals.initialize(id, Globals.SERVER_STR);
		this.config = Globals.getMyConfig();
		this.msgManager = new MessageManager();
		this.connectionPoolTable = new HashMap<String, Connection>();
	}
	
	public void start() throws Exception {
		
		ConnectionAcceptor connectionAcceptor = new ConnectionAcceptor(config, connectionPoolTable, msgManager);
		connectionAcceptor.start();
		
		/* start service */
		ServerService service = new ServerService(config, msgManager, connectionPoolTable);
		service.start();
	}
	
	public static void main(String[] args) throws Exception {

		if(args.length != 1) {
			System.out.println("Missing argument !!");
			System.out.println("Usage:");
			System.out.println("        java code.server.ServerDriver <ServerId>");
			System.out.println("Exiting program now !!");
			System.exit(Globals.SYS_FAILURE);
		} 
		
		String id = args[0];

		ServerDriver driver = new ServerDriver(id);
		System.out.println(" << Server >> :: ID= '" + id + "' started !!");
		driver.start();
	}
}