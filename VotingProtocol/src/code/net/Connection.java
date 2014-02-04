package code.net;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import code.common.ConfigInfo;
import code.common.Globals;
import code.message.Message;

/**
 * Class contains connection information.
 */
public class Connection {
	
	private Socket socket;
	
	private ObjectOutputStream outStream;
	
	private ObjectInputStream inputStream;
	
	private ConfigInfo receiverConfig;

	public Connection(ConfigInfo config, Socket socket, ObjectInputStream inputStream, 
			ObjectOutputStream outStream) {
		this.receiverConfig = config;
		this.socket = socket;
		this.outStream = outStream;
		this.inputStream = inputStream;
	}

	public Socket getSocket() {
		return socket;
	}

	public ObjectOutputStream getOutStream() {
		return outStream;
	}
	
	public void sendMessage(Message message) throws Exception {
		Globals.logSentMsg(message, receiverConfig);
		synchronized (outStream) {
			outStream.writeObject(message);
			outStream.flush();
			outStream.notifyAll();
		}		
	}

	public Message receiveMessage() throws Exception {
		Message message = null;
		synchronized (inputStream) {
			message = (Message) inputStream.readObject();
		}		
		Globals.logReceivedMsg(message);
		return message;
	}
	
	public ObjectInputStream getInputStream() {
		return inputStream;
	}

	public ConfigInfo getConfig() {
		return receiverConfig;
	}
	
	public void closeConnection() throws Exception {
		outStream.close();
		inputStream.close();
		socket.close();
	}
}