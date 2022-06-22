package org.teachothers.fishwatchr;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class PipeMessage extends ConcurrentHashMap<String, String> {

	private static final long serialVersionUID = 1L;
	private static final String KEY_VALUE_SEPARATOR = "\t";
	private static final String SYSTEM_KEY_PREFIX = "__pipe_message_";
	private static final String SYSTEM_SENDER_NAME = "_system_pipe_message_";

	public static final int STATUS_INIT = 0;
	public static final int STATUS_NORMAL = 1;
	public static final int STATUS_CONTINUED = 2;
	public static final int STATUS_DUPLICATED = 3;
	public static final int STATUS_NAME_INQUIRY = 4;
	public static final int STATUS_CANCELED = 5;
	public static final int STATUS_ERROR = -1;
	
	private static final String MESSAGE_KEY_PATH = SYSTEM_KEY_PREFIX + "path";
	private static final String MESSAGE_KEY_ID = SYSTEM_KEY_PREFIX + "id";
	private static final String MESSAGE_KEY_SENDER_NAME = SYSTEM_KEY_PREFIX + "username";
	private static final String MESSAGE_KEY_DATASIZE = SYSTEM_KEY_PREFIX + "datasize";
	private static final String MESSAGE_KEY_TYPE = SYSTEM_KEY_PREFIX + "type";

	
	
	private String id = String.format("%s/%d", LocalDateTime.now().toString(), ThreadLocalRandom.current().nextLong());
	private int status = STATUS_INIT;
	private String senderName = SYSTEM_SENDER_NAME;
	private String path = "";
	private long dataSize = 0;
	private int errorCode = 0;
	

	public PipeMessage() {
	}

	public PipeMessage(String path) {
		this(SYSTEM_SENDER_NAME, path);
	}
	
	public PipeMessage(String senderName, String path) {
		setSenderName(senderName);
		setPath(path);
	}

	
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSenderName() {
		return senderName;
	}

	public void setSenderName(String senderName) {
		this.senderName = senderName;
	}
	
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public long getDataSize() {
		return dataSize;
	}

	public void setDataSize(long dataSize) {
		this.dataSize = dataSize;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public int getStatus() {
		return status;
	}


	public void setStatus(int type) {
		this.status = type;
	}


	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		
		result.append(String.format("%s%s%s", MESSAGE_KEY_ID, KEY_VALUE_SEPARATOR, getId()));
		result.append(String.format("\n%s%s%s", MESSAGE_KEY_SENDER_NAME, KEY_VALUE_SEPARATOR, getSenderName()));
		result.append(String.format("\n%s%s%s", MESSAGE_KEY_PATH, KEY_VALUE_SEPARATOR, getPath()));
		result.append(String.format("\n%s%s%s", MESSAGE_KEY_DATASIZE, KEY_VALUE_SEPARATOR, getDataSize()));
		result.append(String.format("\n%s%s%s", MESSAGE_KEY_TYPE, KEY_VALUE_SEPARATOR, getStatus()));

		this.forEach((key, value)->{
			result.append(String.format("\n%s%s%s", key, KEY_VALUE_SEPARATOR, value));
		});

		return result.toString();
	}
	
	
	public static PipeMessage encode(String str) {
		String lines[] = str.split("\n");
		PipeMessage message = new PipeMessage();
		
		for(int i = 1; i < lines.length; i++) {
			int p = lines[i].indexOf(KEY_VALUE_SEPARATOR);

			if (p != -1) {
				String key = lines[i].substring(0, p);
				String value = lines[i].substring(p + 1);
				
				switch(key) {
				case MESSAGE_KEY_ID: message.setId(value); break;
				case MESSAGE_KEY_SENDER_NAME: message.setSenderName(value); break;
				case MESSAGE_KEY_PATH: message.setPath(value); break;
				case MESSAGE_KEY_DATASIZE: message.setDataSize(Long.parseLong(value)); break;
				case MESSAGE_KEY_TYPE: message.setStatus(Integer.parseInt(value)); break;
				default:
					if (!key.isEmpty()) {
						message.put(key, value);
						continue;
					}
				}
			}
		}

		return message;
	}
}
