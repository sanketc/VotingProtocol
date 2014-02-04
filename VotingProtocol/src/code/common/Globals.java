package code.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.StringTokenizer;

import code.message.Message;

/**
 * Global API, Variables and Constants.
 */
public class Globals {

	public static final int SYS_SUCCESS = 0;
	public static final int SYS_FAILURE = -1;

	public static final String CONFIG = "config";
	public static final String LOG = "log";
	
	public static final String SERVER_STR = "SERVER";
	public static final String CLIENT_STR = "CLIENT";
	
	private static PrintWriter lowWriter;
	
	public static final String configurationFile = CONFIG + File.separator + "ConfigurationFile.txt";
	public static final String settingsFile = CONFIG + File.separator + "Settings.txt";

	/**
	 * List of servers.
	 */
	private static ArrayList<ConfigInfo> serverList = null;

	public static ArrayList<ConfigInfo> getServerList() {
		return serverList;
	}
	
	private static Hashtable<Integer, ConfigInfo> serverLookUpTable = null;
	
	public static Hashtable<Integer, ConfigInfo> getServerLookUpTable() {
		return serverLookUpTable;
	}
	
	/**
	 * List of clients. 
	 */
	private static ArrayList<ConfigInfo> clientList = null;
	
	public static ArrayList<ConfigInfo> getClientList() {
		return clientList;
	}

	private static Hashtable<Integer, ConfigInfo> clientLookUpTable = null;
	
	public static Hashtable<Integer, ConfigInfo> getClientLookUpTable() {
		return clientLookUpTable;
	}

	/**
	 * List of quorum nodes for this client.
	 */
	private static ArrayList<HashSet<Integer>> quorumList = null;
	
	public static ArrayList<HashSet<Integer>> getQuorumList() {
		return quorumList;
	}

	/**
	 * Max number of operations.
	 */
	public static final int ITERATION_COUNT = 50;

	/**
	 * Hold time for a give DataObject Access.
	 * milli Sec
	 */
	private static int holdTime = 1;	/* Default value = 1 */
	
	public static int getHoldTime() {
		return holdTime;
	}
	
	/**
	 * Value by which increments are performed in each write.
	 */
	public static final int INCREMENT_VALUE = 1;

	/**
	* Used to increase execution time.(Scaling factor)
	*/
	public static final int END_CORRECTION = 10;

	/**
	 * Waiting/Timeout time for each request. 
	 */
	public static final int AWATING_GRANT_TIME = 20 * END_CORRECTION;

	
	/**
	 * Total number of data objects in the system.(per server)
	 */
	public static final int DATA_OBJECT_COUNT = 4;
	
	/**
	 * Total number of servers in the system.
	 */
	public static int getServerCount() {
		return serverList.size();
	}

	/**
	 * Total number of clients in the system.
	 */
	public static int getClientCount() {
		return clientList.size();
	}

	/**
	 * Config information holder for current/this node.
	 */
	private static ConfigInfo myConfig;
	
	public static ConfigInfo getMyConfig() {
		return myConfig;
	}
	
	public static void initialize(String id, String nodeType) throws Exception {
		initializeLog(id, nodeType);
		readConfigFile(id, nodeType);
		readSettingsFile();
		populateQuorums();
	}
	
	public static void Finalize(){
		closeLog();
	}

	private static void populateQuorums() {
		quorumList = new ArrayList<HashSet<Integer>>();
		HashSet<Integer> s;
		
		// ----------

		s = new HashSet<Integer>();
		s.add(1); s.add(2); s.add(4);
		quorumList.add(s);

		s = new HashSet<Integer>();
		s.add(1); s.add(2); s.add(5);
		quorumList.add(s);

		s = new HashSet<Integer>();
		s.add(1); s.add(4); s.add(5);
		quorumList.add(s);

		// ----------
		
		s = new HashSet<Integer>();
		s.add(1); s.add(3); s.add(6);
		quorumList.add(s);
		
		s = new HashSet<Integer>();
		s.add(1); s.add(3); s.add(7);
		quorumList.add(s);
		
		s = new HashSet<Integer>();
		s.add(1); s.add(6); s.add(7);
		quorumList.add(s);

		// ----------
		
		s = new HashSet<Integer>();
		s.add(2); s.add(4); s.add(3); s.add(6);
		quorumList.add(s);

		s = new HashSet<Integer>();
		s.add(2); s.add(4); s.add(3); s.add(7);
		quorumList.add(s);
		
		s = new HashSet<Integer>();
		s.add(2); s.add(4); s.add(6); s.add(7);
		quorumList.add(s);

		// ----------
		
		s = new HashSet<Integer>();
		s.add(2); s.add(5); s.add(3); s.add(6);
		quorumList.add(s);
		
		s = new HashSet<Integer>();
		s.add(2); s.add(5); s.add(3); s.add(7);
		quorumList.add(s);
		
		s = new HashSet<Integer>();
		s.add(2); s.add(5); s.add(6); s.add(7);
		quorumList.add(s);

		// ----------
		
		s = new HashSet<Integer>();
		s.add(4); s.add(5); s.add(3); s.add(6);
		quorumList.add(s);
		
		s = new HashSet<Integer>();
		s.add(4); s.add(5); s.add(3); s.add(7);
		quorumList.add(s);
		
		s = new HashSet<Integer>();
		s.add(4); s.add(5); s.add(6); s.add(7);
		quorumList.add(s);
	}
	
	private static void initializeLog(String id, String type){
		String logFileName = LOG + File.separator + type + "_" + id + "_log.txt";
		try {
			lowWriter = new PrintWriter(new FileWriter(new File(logFileName)));
		} catch (IOException e) {
			System.out.println("Error: While opening log file : " + logFileName);
			System.out.println("Exiting application now !!");
			System.exit(SYS_FAILURE);
		}
	}
	
	synchronized private static void closeLog(){
		lowWriter.close();
	}
	
	synchronized public static void logMsg(String msg){
		lowWriter.println("LOG: " + msg);
		lowWriter.flush();
	}

	synchronized public static void logReceivedMsg(Message message) {
		ConfigInfo config = message.getConfig();
		lowWriter.println("MESSAGE_RECEIVED: " + message.getClass().getSimpleName() +
				" from " + config.getId() + " @ " + config.getAddress());
		lowWriter.flush();
	}
	
	synchronized public static void logSentMsg(Message message, ConfigInfo config){
		lowWriter.println("MESSAGE_SENT: " + message.getClass().getSimpleName() + 
				" to "  + config.getId() + " @ " + config.getAddress());
		lowWriter.flush();
	}
	
	synchronized public static void logErrorMsg(String msg) {
		lowWriter.println("ERROR: " + msg);
		lowWriter.flush();
	}
	
	private static void readConfigFile(String currId, String nodeType) throws Exception {
		File file = new File(configurationFile);
		if(!file.exists()) {
			System.out.println("Error: Config file does not exits : " + configurationFile);
			System.out.println("Exiting application now !!");
			System.exit(SYS_FAILURE);
		}
		
		clientList = new ArrayList<ConfigInfo>();
		serverList = new ArrayList<ConfigInfo>();
		clientLookUpTable = new Hashtable<Integer, ConfigInfo>();
		serverLookUpTable = new Hashtable<Integer, ConfigInfo>();
		BufferedReader br = new BufferedReader(new FileReader(file));
		
		/* Skip header */
		br.readLine();
		
		String line;
		StringTokenizer stk;
		while ((line = br.readLine()) != null) {
			stk = new StringTokenizer(line);
			if (stk.countTokens() != 4) {
				System.out.println("Error: Config file not in correct format !!");
				System.out.println("Error: Config file Name: " + configurationFile);
				System.out.println("Exiting application now !!");
				System.exit(SYS_FAILURE);
			}
			
			String type = stk.nextToken();
			String id = stk.nextToken();
			String address = stk.nextToken();
			int port = Integer.parseInt(stk.nextToken());
			ConfigInfo config = new ConfigInfo(id, address, port, type);
			if(type.equalsIgnoreCase(SERVER_STR)) {
				if (id.equals(currId) && nodeType.equals(Globals.SERVER_STR))
					myConfig = config;
				serverList.add(config);
				serverLookUpTable.put(new Integer(id), config);
			} else if(type.equalsIgnoreCase(CLIENT_STR)) {
				if (id.equals(currId) && nodeType.equals(Globals.CLIENT_STR))
					myConfig = config;
				clientList.add(config);
				clientLookUpTable.put(new Integer(id), config);
			} else {
				br.close();
				throw new Exception("Configuration file in incorrect format !!");
			}
		}
		
		br.close();
	}
	
	/**
	 * Current node is not added. 
	 **/
	private static void readSettingsFile() throws Exception {
		String fileName = Globals.settingsFile;
		File file = new File(fileName);
		if(!file.exists()) {
			System.out.println("Error: Setting file does not exits : " + fileName);
			System.out.println("Exiting application now !!");
			System.exit(SYS_FAILURE);
		}
		
		/* Read list */
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = br.readLine();
		String[] array = line.split("=");
		
		if (array.length != 2 && !array[0].equals("HOLD_TIME")) {
			System.out.println("Error: Setting file not in correct format !!");
			System.out.println("Error: File Name: " + fileName);
			System.out.println("Exiting application now !!");
			System.exit(SYS_FAILURE);
		}

		holdTime = (int) (Double.parseDouble(array[1]) * END_CORRECTION);
		
		br.close();
	}
	
}