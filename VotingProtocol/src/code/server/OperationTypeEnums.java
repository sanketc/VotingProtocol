package code.server;

/**
 * Operation Enums.
 */
public enum OperationTypeEnums {
	
	READ,
	
	WRITE;
	
	@Override
	public String toString(){
		return this.name();
	}
}