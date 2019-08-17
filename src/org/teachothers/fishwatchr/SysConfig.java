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

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class SysConfig {
	public final static String CONFIG_FILENAME = "config.xml"; //$NON-NLS-1$
	public final static String COLUMN_ID_BASE = "column"; //$NON-NLS-1$
	
	private Document doc = null;
	private XPath xpath = null;
	private Pattern patternColumnID = Pattern.compile("^" + COLUMN_ID_BASE + "(\\d+)$"); //$NON-NLS-1$ //$NON-NLS-2$
	
	public SysConfig(){
		
	}
	
	
	public void load(ArrayList<CommentType> commentTypes, ArrayList<User> discussers) {
		File configFile = new File(Util.getCurrentDir() + "/" + CONFIG_FILENAME); //$NON-NLS-1$

		if(!configFile.exists()){
			try {
				Files.copy(getClass().getResourceAsStream("resources/config/" + CONFIG_FILENAME), //$NON-NLS-1$
						configFile.toPath());
				System.err.println("Warning(SysConfig): Generate the default " + CONFIG_FILENAME + ", because " +  CONFIG_FILENAME + " is not found."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			} catch (IOException e) {
				System.err.println("Error(SysConfig): Can not copy the default config file."); //$NON-NLS-1$
				e.printStackTrace();
			}
		} else {
			System.err.println("loaded config:" + Util.getCurrentDir() + "/" + CONFIG_FILENAME); //$NON-NLS-1$ //$NON-NLS-2$
		}

		setDefault(commentTypes, discussers);
		
		if (configFile.exists()) {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();

			DocumentBuilder builder;
			try {
				builder = factory.newDocumentBuilder();
				doc = builder.parse(configFile);

				XPathFactory xPathFactory = XPathFactory.newInstance();
				xpath = xPathFactory.newXPath();

				// comment_types 要素
				XPathExpression expr = xpath
						.compile("/settings/comment_types/li"); //$NON-NLS-1$
				NodeList commentTypesNodes = (NodeList) expr.evaluate(doc,
						XPathConstants.NODESET);
				for (int i = 0; i < commentTypes.size(); i++) {
					CommentType ct = commentTypes.get(i);
					if(i < commentTypesNodes.getLength()){
						String commentTypeName = ((Element) commentTypesNodes
								.item(i)).getAttribute("name"); //$NON-NLS-1$
						String commentTypeColor = ((Element) commentTypesNodes
								.item(i)).getAttribute("color"); //$NON-NLS-1$
						ct.set(commentTypeName,
								new Color(Integer.parseInt(commentTypeColor)));
					} else if(i == 0){
						ct.set(Messages.getString("SysConfig.0"), Color.RED); //$NON-NLS-1$
					} else {
						ct.set("", Color.LIGHT_GRAY); //$NON-NLS-1$
					}
				}
				if(commentTypesNodes.getLength() == 0){
					commentTypes.get(0).set(Messages.getString("SysConfig.1"), Color.RED); //$NON-NLS-1$
				} else if(commentTypesNodes.getLength() > commentTypes.size()){
					for(int i = commentTypes.size(); i < commentTypesNodes.getLength(); i++){
						String commentTypeName = ((Element) commentTypesNodes
								.item(i)).getAttribute("name"); //$NON-NLS-1$
						System.err.println(Messages.getString("SysConfig.2") + CommentTableModel.ITEM_LABEL + " "  //$NON-NLS-1$//$NON-NLS-2$
										+ commentTypeName + Messages.getString("SysConfig.3")); //$NON-NLS-1$
					}
				}

				// discussers 要素
				expr = xpath.compile("/settings/discussers/li"); //$NON-NLS-1$
				NodeList discussersNodes = (NodeList) expr.evaluate(doc,
						XPathConstants.NODESET);
				for (int i = 0; i < discussers.size(); i++) {
					User discusser = discussers.get(i);
					if(i < discussersNodes.getLength()){
						String discusserName = ((Element) discussersNodes.item(i))
								.getAttribute("name"); //$NON-NLS-1$
						discusser.setName(discusserName);
					} else if(i == 0){
						discusser.setName(Messages.getString("SysConfig.4")); //$NON-NLS-1$
					} else {
						discusser.setName(""); //$NON-NLS-1$
					}
				}
				
				if(discussersNodes.getLength() > discussers.size()){
					for(int i = discussers.size(); i < discussersNodes.getLength(); i++){
						String discusserName = ((Element) discussersNodes.item(i))
								.getAttribute("name"); //$NON-NLS-1$
						System.err.println(Messages.getString("SysConfig.5") + CommentTableModel.ITEM_TARGET + " "  //$NON-NLS-1$//$NON-NLS-2$
								+ discusserName + Messages.getString("SysConfig.6")); //$NON-NLS-1$
					}
				}
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null,
						Messages.getString("SysConfig.7") //$NON-NLS-1$
						+ CONFIG_FILENAME
						+ Messages.getString("SysConfig.8") //$NON-NLS-1$
						+ Messages.getString("SysConfig.9") + e.getLocalizedMessage()); //$NON-NLS-1$
				e.printStackTrace();
				setDefault(commentTypes, discussers);
			}

		} else {
			System.err.println("Warning(SysConfig): " + CONFIG_FILENAME + Messages.getString("SysConfig.11")); //$NON-NLS-1$ //$NON-NLS-2$
			setDefault(commentTypes, discussers);
		}
	}

	
	public void save() throws IOException, TransformerException {
		
		File configFile = new File(Util.getCurrentDir() + "/" + CONFIG_FILENAME); //$NON-NLS-1$
		if (configFile.exists()) {
			String filename = configFile.getName();
			
			String backupFilename = filename + ".bak"; //$NON-NLS-1$
			File backupFile = new File(backupFilename);
			Files.copy(configFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        
        transformer.setOutputProperty("indent", "yes"); //$NON-NLS-1$ //$NON-NLS-2$
        transformer.setOutputProperty("encoding", "utf-8"); //$NON-NLS-1$ //$NON-NLS-2$

        // XMLファイルの作成
        transformer.transform(new DOMSource(doc), new StreamResult(configFile));
	}

	
	public void setDefault(ArrayList<CommentType> commentTypes, ArrayList<User> discussers){
		discussers.clear();
		discussers.add(new User(CommentTableModel.ITEM_TARGET + "1")); //$NON-NLS-1$
		discussers.add(new User(CommentTableModel.ITEM_TARGET + "2")); //$NON-NLS-1$
		discussers.add(new User(CommentTableModel.ITEM_TARGET + "3")); //$NON-NLS-1$
		discussers.add(new User(CommentTableModel.ITEM_TARGET + "4")); //$NON-NLS-1$
		discussers.add(new User(Messages.getString("SysConfig.12"))); //$NON-NLS-1$
		discussers.add(new User(Messages.getString("SysConfig.13"))); //$NON-NLS-1$
		discussers.add(new User("")); //$NON-NLS-1$
		discussers.add(new User("")); //$NON-NLS-1$

		commentTypes.clear();
		commentTypes.add(new CommentType(Messages.getString("SysConfig.14"), Color.red)); //$NON-NLS-1$
		commentTypes.add(new CommentType(Messages.getString("SysConfig.15"), Color.ORANGE)); //$NON-NLS-1$
		commentTypes.add(new CommentType(Messages.getString("SysConfig.16"), Color.blue)); //$NON-NLS-1$
		commentTypes.add(new CommentType(Messages.getString("SysConfig.17"), Color.green)); //$NON-NLS-1$
		commentTypes.add(new CommentType(Messages.getString("SysConfig.18"), Color.cyan)); //$NON-NLS-1$
		commentTypes.add(new CommentType(Messages.getString("SysConfig.19"), Color.yellow)); //$NON-NLS-1$
		commentTypes.add(new CommentType(Messages.getString("SysConfig.20"), Color.magenta)); //$NON-NLS-1$
		commentTypes.add(new CommentType("", new Color(10, 10, 10))); //$NON-NLS-1$
		commentTypes.add(new CommentType("", new Color(60, 60, 60))); //$NON-NLS-1$
		commentTypes.add(new CommentType("", new Color(110, 110, 110))); //$NON-NLS-1$
		commentTypes.add(new CommentType("", new Color(160, 160, 160))); //$NON-NLS-1$
		commentTypes.add(new CommentType("", new Color(210, 210, 210))); //$NON-NLS-1$
	}
	
	
	public String getFirstNodeAsString(String path){
		if(doc == null || xpath == null){
			return null;
		}
		
		String nodeValue = null;

		try {
			XPathExpression expr = xpath.compile(path);
			NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
			if(nodes.getLength() > 0){
				nodeValue = nodes.item(0).getTextContent();
			} else {
				nodeValue = ""; //$NON-NLS-1$
			}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		
		return nodeValue;
	}
	
	
	public String[] getColumnNames(int nColumns){
		if(doc == null || xpath == null){
			return null;
		}

		String[] columnNames = new String[nColumns];
		
		try {
			XPathExpression expr = xpath.compile("/settings/column_names/li"); //$NON-NLS-1$
			NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
			if(nColumns == 0 || nodes.getLength() == 0){
				return null;
			} else if(nodes.getLength() != nColumns){
				System.err.println("Error(SysConfig): The number of columns of /settings/column_names must be " + nColumns + ", but it is " + nodes.getLength() +"."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				return null;
			}
			
			
			for (int i = 0; i < nodes.getLength(); i++) {
				Element element = (Element)nodes.item(i);
				String id = element.getAttribute("id"); //$NON-NLS-1$
				String name = element.getAttribute("name"); //$NON-NLS-1$

				Matcher m = patternColumnID.matcher(id);
				if(m.find()){
					int n = new Integer(m.group(1));
					if(n <= nColumns && columnNames[n-1] == null && !name.isEmpty()){
						columnNames[n-1] = name;
						continue;
					}
				}
				System.err.println("Error(SysConfig): invalid node ->  " + Util.prettyPrintXML(element)); //$NON-NLS-1$
				return null;
			}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		return columnNames;
	}

	
	public boolean[] getColumnReadOnlyFlags(int nColumns){
		if(doc == null || xpath == null){
			return null;
		}

		boolean[] flags = new boolean[nColumns];
		HashSet<String> idSet = new HashSet<String>();
		
		try {
			XPathExpression expr = xpath.compile("/settings/column_names/li"); //$NON-NLS-1$
			NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
			if(nColumns == 0 || nodes.getLength() == 0){
				return null;
			} else if(nodes.getLength() != nColumns){
				System.err.println("Error(SysConfig): The number of columns of /settings/column_names must be " + nColumns + ", but it is " + nodes.getLength() +"."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				return null;
			}
			
			for (int i = 0; i < nodes.getLength(); i++) {
				Element element = (Element)nodes.item(i);
				String id = element.getAttribute("id"); //$NON-NLS-1$
				String readonly = element.getAttribute("readonly"); //$NON-NLS-1$

				Matcher m = patternColumnID.matcher(id);
				if(m.find() && !idSet.contains(id)){
					int n = new Integer(m.group(1));
					if(n <= nColumns  && !readonly.isEmpty()){
						// always true if n == 1 or 2 
						if(n > 2){
							if(readonly.equalsIgnoreCase("true")){ //$NON-NLS-1$
								flags[n-1] = true;
							} else {
								flags[n-1] = false;
							}
						} else {
							flags[n-1] = true;
						}
						continue;
					}
				}
				System.err.println("Error(SysConfig): invalid node ->  " + Util.prettyPrintXML(element)); //$NON-NLS-1$
				return null;
			}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		return flags;
	}

	
	public String[] getColumnConstraints(int nColumns){
		if(doc == null || xpath == null){
			return null;
		}
		String[] constraints = new String[nColumns];
		
		try {
			XPathExpression expr = xpath.compile("/settings/column_names/li"); //$NON-NLS-1$
			NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
			if(nColumns == 0 || nodes.getLength() == 0){
				return null;
			} else if(nodes.getLength() != nColumns){
				System.err.println("Error(SysConfig): The number of columns of /settings/column_names must be " + nColumns + ", but it is " + nodes.getLength() +"."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				return null;
			}
			
			for (int i = 0; i < nodes.getLength(); i++) {
				Element element = (Element)nodes.item(i);
				constraints[i] = element.getAttribute("constraint"); //$NON-NLS-1$
			}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		return constraints;
	}

	
	public void setCommentTypes(String path, String nodeName, List<CommentType> commentTypes){

		XPathExpression expr;
		try {
			expr = xpath.compile(path);
			Node nodeCommentTypes = (Node) expr.evaluate(doc, XPathConstants.NODE);
			
			while(nodeCommentTypes.hasChildNodes()){
				nodeCommentTypes.removeChild(nodeCommentTypes.getFirstChild());
			}
			
			for(CommentType commentType: commentTypes){
				if(commentType.getType().isEmpty()){
					continue;
				}
				Element newElement = doc.createElement(nodeName);
				newElement.setAttribute("name", commentType.getType()); //$NON-NLS-1$
				newElement.setAttribute("color", String.valueOf(commentType.getColor().getRGB())); //$NON-NLS-1$
				nodeCommentTypes.appendChild(newElement);
			}
			
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	
	public void setDiscussers(String path, String nodeName, List<User> discussers){

		XPathExpression expr;
		try {
			expr = xpath.compile(path);
			Node nodeCommentTypes = (Node) expr.evaluate(doc, XPathConstants.NODE);
			
			while(nodeCommentTypes.hasChildNodes()){
				nodeCommentTypes.removeChild(nodeCommentTypes.getFirstChild());
			}
			
			for(User discusser: discussers){
				if(discusser.getName().isEmpty()){
					continue;
				}
				Element newElement = doc.createElement(nodeName);
				newElement.setAttribute("name", discusser.getName()); //$NON-NLS-1$
				nodeCommentTypes.appendChild(newElement);
			}
			
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	public void setValue(String pathToElement, String attributeName, String value) throws XPathExpressionException{
		XPathExpression expr = xpath.compile(pathToElement);
		Element element = (Element) expr.evaluate(doc, XPathConstants.NODE);
		element.setAttribute(attributeName, value);
	}

}
