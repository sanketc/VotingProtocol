package code.common;

import java.util.LinkedList;

import code.message.Message;

/**
 * Manages access to request queue for socket received messages. 
 */
public class MessageManager {

	public LinkedList<Message> messageQueue;
	
	public MessageManager() {
		messageQueue = new LinkedList<Message>();
	}
	
	synchronized public boolean isEmptyQueue() {
		return messageQueue.isEmpty();
	}
	
	synchronized public void addLastMessage(Message message) {
		messageQueue.addLast(message);
		notifyAll();
	}

	synchronized public void addFirstMessage(Message message) {
		messageQueue.addFirst(message);
		notifyAll();
	}
	
	synchronized public Message getNextMessage() {
		return messageQueue.pollFirst();
	}	
}