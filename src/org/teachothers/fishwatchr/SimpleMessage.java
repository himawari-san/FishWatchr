package org.teachothers.fishwatchr;

import java.util.Date;
import java.util.HashMap;

public class SimpleMessage extends HashMap<String, String> {

	private static final long serialVersionUID = 1L;
	private static final String KEY_GLUE = "/_/";
	private static final String KEY_VALUE_SEPARATOR = "\t";
	private String id = "";
	
	public SimpleMessage() {
	}

	public SimpleMessage(String name) {
		Date d = new Date();
		setID(String.format("%s%s%tQ", name, KEY_GLUE, d.getTime())); 
	}

	public void setID(String id) {
		this.id = id;
	}
	
	public String getID() {
		return id;
	}
	
	
	@Override
	public String toString() {
		StringBuffer result = new StringBuffer(id);
		this.forEach((key, value)->{
			result.append("\n");
			result.append(key);
			result.append(KEY_VALUE_SEPARATOR);
			result.append(value);
		});

		return result.toString();
	}
	
	
	public static SimpleMessage encode(String str) {
		String lines[] = str.split("\n");
		SimpleMessage message = new SimpleMessage();
		message.setID(lines[0]);
		
		for(int i = 1; i < lines.length; i++) {
			int p = lines[i].indexOf(KEY_VALUE_SEPARATOR);

			if (p != -1) {
				String key = lines[i].substring(0, p);
				if (!key.isEmpty()) {
					message.put(key, lines[i].substring(p + 1));
					System.err.println("kv:" + key + "," + lines[i].substring(p + 1));
					continue;
				}
			}
		}
		
		return message;
	}
}
