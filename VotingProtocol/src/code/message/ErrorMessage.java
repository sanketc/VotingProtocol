package code.message;

import java.io.Serializable;

import code.common.ConfigInfo;

@SuppressWarnings("serial")
public class ErrorMessage extends Message implements Serializable{

	private String msg;
	
	public ErrorMessage(){
	}

	public ErrorMessage(ConfigInfo config, String msg) {
		super(config);
		this.msg = msg;
	}

	public String getMsg() {
		return msg;
	}
}