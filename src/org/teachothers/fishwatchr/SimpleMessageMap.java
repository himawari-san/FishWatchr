package org.teachothers.fishwatchr;

import java.util.ArrayList;
import java.util.HashMap;

public class SimpleMessageMap extends HashMap<String, SimpleMessage> {

	private static final long serialVersionUID = 1L;
	private ArrayList<String> mapIDs = new ArrayList<String>();
	public static final String DATA_ID_KEY = "data_id";
	public static final String DATA_GLUE = "\t";
	
	public SimpleMessageMap() {
		super();
		
	}
	
	// SimpleMessage s1
	//   data_id: name1
	//   comment: fw1.xml
	//   media: fw1.mp4
	//
	// updateMap(s1)
	// ->
	//   name1 + DATA_GLUE data_id: name1
	//   name1 + DATA_GLUE comment: fw1.xml
	//   name1 + DATA_GLUE media: fw1.mp4
    //   mapIDs.add(name1)	

	
	// overwrite an existing map
//	public String updateMap(SimpleMessage message) {
//		String mapID = message.getID();
//		if(mapID.isEmpty()) {
//			return ""; 
//		}
//
//		if(!mapContains(mapID)) {
//			mapIDs.add(mapID);
//		}
//		
//		message.forEach((key, value)->addEntry(key, value, mapID));
//
//		return mapID;
//	}
	
//	public void addEntry(String key, String value, String mapID) {
//		StringBuffer newKey = new StringBuffer(mapID);
//		newKey.append(DATA_GLUE).append(key);
//		put(newKey.toString(), value);
//	}
//
//	public String getMapID() {
//		if(containsKey(DATA_ID_KEY)) {
//			return get(DATA_ID_KEY);
//		} else {
//			return "";
//		}
//	}
	
	
	public boolean mapContains(String mapID) {
		return mapIDs.contains(mapID) ? true : false;	
	}
	
	
	public boolean validate() {
		return containsKey(DATA_ID_KEY) ? true : false;
	}

}
