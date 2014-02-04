package code.message;

/**
 * Message enums.
 */
public enum MessageEnums {

	/* Protocol Messages */
	READ_REQUEST,
	WRITE_REQUEST,
	READ_COMMIT,
	WRITE_COMMIT,
	DATA,	/* Read Reply Message */
	GRANT,	/* Write Reply Message */
	ERROR,
	TIMEOUT,	/* Awaiting timer timeout message */
	WITHDRAW,	/* Read/Write Withdrawal Message */
	
	/* Controlling messages */
	C_START,
	C_END,
	C_TERMINATE,
	C_CONNECT;
	
	@Override
	public String toString(){
		return this.name();
	}
}