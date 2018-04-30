/*
    Copyright (C) 2014-2018 Masaya YAMAGUCHI

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
import java.util.List;

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
	private Document doc = null;
	private XPath xpath = null;
	
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
						ct.set("汎用", Color.RED);
					} else {
						ct.set("", Color.LIGHT_GRAY); //$NON-NLS-1$
					}
				}
				if(commentTypesNodes.getLength() == 0){
					commentTypes.get(0).set("汎用", Color.RED);
				} else if(commentTypesNodes.getLength() > commentTypes.size()){
					for(int i = commentTypes.size(); i < commentTypesNodes.getLength(); i++){
						String commentTypeName = ((Element) commentTypesNodes
								.item(i)).getAttribute("name"); //$NON-NLS-1$
						System.err.println("Warning(SysConfig): 登録数を越えたため，" + Comment.ITEM_LABEL + " " //$NON-NLS-2$
										+ commentTypeName + " が登録できませんでした。");
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
						discusser.setName("不特定");
					} else {
						discusser.setName(""); //$NON-NLS-1$
					}
				}
				
				if(discussersNodes.getLength() > discussers.size()){
					for(int i = discussers.size(); i < discussersNodes.getLength(); i++){
						String discusserName = ((Element) discussersNodes.item(i))
								.getAttribute("name"); //$NON-NLS-1$
						System.err.println("Warning(SysConfig): 登録数を越えたため，" + Comment.ITEM_TARGET + " " //$NON-NLS-2$
								+ discusserName + " が登録できませんでした。");
					}
				}
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null,
						"設定ファイル("
						+ CONFIG_FILENAME
						+ ")の読み込み中にエラーが発生したため，デフォルトの設定が読み込まれました。"
						+ "\nエラーメッセージ：\n" + e.getLocalizedMessage());
				System.err.println("Error(SysConfig): " + "設定ファイルの読み込み中にエラーが発生したため，デフォルトの設定が読み込まれました。"); //$NON-NLS-1$
				e.printStackTrace();
				setDefault(commentTypes, discussers);
			}

		} else {
			System.err.println("Warning(SysConfig): " + CONFIG_FILENAME + "が見つからなかったため，デフォルトの設定を使用します。"); //$NON-NLS-1$
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
		discussers.add(new User(Comment.ITEM_TARGET + "１")); //$NON-NLS-1$
		discussers.add(new User(Comment.ITEM_TARGET + "２")); //$NON-NLS-1$
		discussers.add(new User(Comment.ITEM_TARGET + "３")); //$NON-NLS-1$
		discussers.add(new User(Comment.ITEM_TARGET + "４")); //$NON-NLS-1$
		discussers.add(new User("不特定"));
		discussers.add(new User("誤り"));
		discussers.add(new User("")); //$NON-NLS-1$
		discussers.add(new User("")); //$NON-NLS-1$

		commentTypes.clear();
		commentTypes.add(new CommentType("意見", Color.red));
		commentTypes.add(new CommentType("質問", Color.ORANGE));
		commentTypes.add(new CommentType("管理", Color.blue));
		commentTypes.add(new CommentType("相づち", Color.green));
		commentTypes.add(new CommentType("確認", Color.cyan));
		commentTypes.add(new CommentType("その他", Color.yellow));
		commentTypes.add(new CommentType("誤り", Color.magenta));
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
