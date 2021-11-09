/*
    Copyright (C) 2014-2021 Masaya YAMAGUCHI

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

import java.awt.Font;
import java.io.File;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.nio.file.Path;

import javax.swing.JLabel;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;
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

	
	public static String getJarDir() throws URISyntaxException{
		// the directory where fishwatchr.jar is placed
		return new File(FishWatchr.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
	}

	
	public static String getCurrentDir(){
		// the directory where jvm is executed
		return System.getProperty("user.dir");  //$NON-NLS-1$
	}
	
	
	public static Path getUniquePath(Path basePath, String filename){

		Path newPath = basePath.resolve(filename);
		int c = 1;
		
		while(newPath.toFile().exists()){
			String num = String.format(".%03d", c++);
			
			if(filename.lastIndexOf(".") != -1) { // has a suffix
				// e.g. test.xml -> test.001.xml	
				newPath = basePath.resolve(filename.replaceFirst("(\\.[^\\.]*)$", num+"$1"));
			} else {
				// append a number
				newPath = basePath.resolve(filename + num);
			}
		}
		return newPath;
	}

	
	public static long getTotalFilesize(Path[] paths) {
		long total = 0;
		
		for(Path path : paths) {
			total += path.toFile().length();
		}
		
		return total;
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
				documentBuilderFactory.setFeature("http://xml.org/sax/features/namespaces", false); //$NON-NLS-1$
				documentBuilderFactory.setFeature("http://xml.org/sax/features/validation", false); //$NON-NLS-1$
				documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false); //$NON-NLS-1$
				documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false); //$NON-NLS-1$
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}
			
		}
		
		return documentBuilderFactory;
	}
	

	public static int getTextHeight(String html, Font font, int componentWidth) {
	    JLabel renderer = new JLabel(html);
	    renderer.setFont(font);
	    View view = (View) renderer.getClientProperty(BasicHTML.propertyKey);
	    view.setSize(componentWidth, 0.0f);
	    
	    return (int) Math.ceil(view.getPreferredSpan(View.Y_AXIS));
	}
}
