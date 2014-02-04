package code.common;

import java.util.ArrayList;
import java.util.Random;

import code.server.OperationTypeEnums;

/**
 * Contains common APIs used by both client and server.
 */
public class CommonAPI {

	/**
	 * Returns random server from the list of servers.
	 */
	public ConfigInfo getRandomServer() {
		ArrayList<ConfigInfo> serverList = Globals.getServerList();
		if(serverList == null)
			return null;
		Random rand = new Random();
		int  index = rand.nextInt(100) % serverList.size() ;
		return serverList.get(index);
	}
	
	public int getRandomDelay() {
		Random rand = new Random();
		return (5 + rand.nextInt(5 + 1 /* For 10 inclusive */)) * Globals.END_CORRECTION;
	}

	public int getRandomDataObjectId() {
		Random rand = new Random();
		return rand.nextInt(Globals.DATA_OBJECT_COUNT);
	}
	
	public OperationTypeEnums getRandomOperation() {
		Random rand = new Random();
		int randomNo = rand.nextInt(10) + 1; 
		if( randomNo == 1 )	 
			return OperationTypeEnums.WRITE;
		else
			return OperationTypeEnums.READ;
	}
	
}