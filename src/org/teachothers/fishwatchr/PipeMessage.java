package org.teachothers.fishwatchr;

import java.util.HashMap;

public class PipeMessage extends HashMap<String, String> {

	private static final long serialVersionUID = 1L;
	private static final String KEY_VALUE_SEPARATOR = "\t";
	public static final int TYPE_INIT = 0;
	public static final int TYPE_NORMAL = 1;
	public static final int TYPE_ERROR = -1;
	
	private String id = "";
	private int type = TYPE_INIT;
	

	public PipeMessage(String id) {
		this.id = id;
	}

	
	public String getID() {
		return id;
	}
	
	
	public int getType() {
		return type;
	}


	public void setType(int type) {
		this.type = type;
	}


	@Override
	public String toString() {
		StringBuffer result = new StringBuffer(id); // show id at the beginning
		this.forEach((key, value)->{
			result.append("\n");
			result.append(key);
			result.append(KEY_VALUE_SEPARATOR);
			result.append(value);
		});

		return result.toString();
	}
	
	
	public static PipeMessage encode(String str) {
		String lines[] = str.split("\n");
		PipeMessage message = new PipeMessage(lines[0]);
		
		for(int i = 1; i < lines.length; i++) {
			int p = lines[i].indexOf(KEY_VALUE_SEPARATOR);

			if (p != -1) {
				String key = lines[i].substring(0, p);
				if (!key.isEmpty()) {
					message.put(key, lines[i].substring(p + 1));
					continue;
				}
			}
		}

		message.setType(TYPE_NORMAL);

		return message;
	}
}
