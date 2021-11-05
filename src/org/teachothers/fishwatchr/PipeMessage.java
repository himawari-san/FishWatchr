package org.teachothers.fishwatchr;

import java.util.HashMap;

public class PipeMessage extends HashMap<String, String> {

	private static final long serialVersionUID = 1L;
	private static final String KEY_VALUE_SEPARATOR = "\t";
	private String id = "";
	

	public PipeMessage(String id) {
		this.id = id;
	}

	
	public String getID() {
		return id;
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
					System.err.println("kv:" + key + "," + lines[i].substring(p + 1));
					continue;
				}
			}
		}
		
		return message;
	}
}
