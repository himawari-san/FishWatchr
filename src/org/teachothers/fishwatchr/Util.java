/*
    Copyright (C) 2014-2019 Masaya YAMAGUCHI

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.teachothers.fishwatchr;

import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Node;

public class Util {

	static DocumentBuilderFactory documentBuilderFactory = null;

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
		return System.getProperty("user.dir"); // the directory where fishwatchr.jar is placed
	}
	

	public static String prettyPrintXML(Node doc){
    	// https://stackoverflow.com/questions/139076/how-to-pretty-print-xml-from-java
    	// answered by Lorenzo Boccaccia
		Transformer transformer;
		try {
			transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2"); //$NON-NLS-1$ //$NON-NLS-2$
			//initialize StreamResult with File object to save to file
			StreamResult result = new StreamResult(new StringWriter());
			DOMSource source = new DOMSource(doc);
			transformer.transform(source, result);

			return result.getWriter().toString();
		} catch (TransformerFactoryConfigurationError | TransformerException e1) {
			e1.printStackTrace();
			return ""; //$NON-NLS-1$
		}
    }
	
	
	public static DocumentBuilderFactory getSimpleDocumentBuilderFactory() {
		if(documentBuilderFactory == null) {
			documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setNamespaceAware(false);
			documentBuilderFactory.setValidating(false);
			try {
				documentBuilderFactory.setFeature("http://xml.org/sax/features/namespaces", false);
				documentBuilderFactory.setFeature("http://xml.org/sax/features/validation", false);
				documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
				documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}
			
		}
		
		return documentBuilderFactory;
	}
}
