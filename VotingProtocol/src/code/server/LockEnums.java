package code.server;

/**
 * Different types of locks possible on a DataObject.
 */
public enum LockEnums {
	
	NOT_LOCKED,
	
	READ_LOCKED,
	
	WRITE_LOCKED;
	
	@Override
	public String toString(){
		return this.name();
	}
}