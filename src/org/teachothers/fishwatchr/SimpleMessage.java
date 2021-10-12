package org.teachothers.fishwatchr;

import java.util.HashMap;

public class SimpleMessage extends HashMap<String, String> {

	private static final long serialVersionUID = 1L;
	private String id = "";
	
	public SimpleMessage() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public String put(String key, String value) {
		if(key.equals(SimpleTextMap.DATA_ID_KEY)) {
			id = value;
		}
		return super.put(key, value);
	}
	
	public String getID() {
		return id;
	}
}
