package org.teachothers.fishwatchr;

import java.io.File;
import java.io.IOException;

public class Util {
	
	public static String catStrings(String str1, String str2, String delimiter){
		if(str1.isEmpty()){
			return str2;
		} else if(str2.isEmpty()){
			return str1;
		} else {
			return str1 + delimiter + str2;
		}
	}
	
	public static String getCurrentDir(){
		try {
			return new File(new File(System.getProperty("java.class.path")).getCanonicalPath()).getParent(); //$NON-NLS-1$
		} catch (IOException e) {
			System.err.println("Warning(Util.getCurrentDir: can't get the current directory."); //$NON-NLS-1$
			return ""; //$NON-NLS-1$
		}
	}
}
