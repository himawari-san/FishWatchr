package org.teachothers.fishwatchr;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Node;

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
	

	public static String prettyPrintXML(Node doc){
    	// https://stackoverflow.com/questions/139076/how-to-pretty-print-xml-from-java
    	// answered by Lorenzo Boccaccia
		Transformer transformer;
		try {
			transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			//initialize StreamResult with File object to save to file
			StreamResult result = new StreamResult(new StringWriter());
			DOMSource source = new DOMSource(doc);
			transformer.transform(source, result);

			return result.getWriter().toString();
		} catch (TransformerFactoryConfigurationError | TransformerException e1) {
			e1.printStackTrace();
			return "";
		}
    }
}
