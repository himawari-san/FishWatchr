package org.teachothers.fishwatchr;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class SysConfig {
	private final String configFilename = "fishwatchr.config";
	private Document doc = null;
	private XPath xpath = null;
	
	public SysConfig(){
		
	}
	
	
	public void load(ArrayList<CommentType> commentTypes, ArrayList<User> discussers) {
		File configFile = new File(configFilename);

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
						.compile("/settings/comment_types/li");
				NodeList commentTypesNodes = (NodeList) expr.evaluate(doc,
						XPathConstants.NODESET);
				System.err.println("co:" + commentTypes.size());
				for (int i = 0; i < commentTypes.size(); i++) {
					CommentType ct = commentTypes.get(i);
					if(i < commentTypesNodes.getLength()){
						String commentTypeName = ((Element) commentTypesNodes
								.item(i)).getAttribute("name");
						String commentTypeColor = ((Element) commentTypesNodes
								.item(i)).getAttribute("color");
						ct.set(commentTypeName,
								new Color(Integer.parseInt(commentTypeColor)));
					} else if(i == 0){
						ct.set("汎用", Color.RED);
					} else {
						ct.set("", Color.LIGHT_GRAY);
					}
				}
				if(commentTypesNodes.getLength() == 0){
					commentTypes.get(0).set("汎用", Color.RED);
				} else if(commentTypesNodes.getLength() > commentTypes.size()){
					for(int i = commentTypes.size(); i < commentTypesNodes.getLength(); i++){
						String commentTypeName = ((Element) commentTypesNodes
								.item(i)).getAttribute("name");
						System.err.println("Warning(SysConfig): 登録数を越えたため，ラベル "
										+ commentTypeName + " が登録できませんでした。");
					}
				}

				// discussers 要素
				expr = xpath.compile("/settings/discussers/li");
				NodeList discussersNodes = (NodeList) expr.evaluate(doc,
						XPathConstants.NODESET);
				for (int i = 0; i < discussers.size(); i++) {
					User discusser = discussers.get(i);
					if(i < discussersNodes.getLength()){
						String discusserName = ((Element) discussersNodes.item(i))
								.getAttribute("name");
						discusser.setName(discusserName);
					} else if(i == 0){
						discusser.setName("不特定");
					} else {
						discusser.setName("");
					}
				}
				
				if(discussersNodes.getLength() > discussers.size()){
					for(int i = discussers.size(); i < discussersNodes.getLength(); i++){
						String discusserName = ((Element) discussersNodes.item(i))
								.getAttribute("name");
						System.err.println("Warning(SysConfig): 登録数を越えたため，話者 "
								+ discusserName + " が登録できませんでした。");
					}
				}
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null,
						"設定ファイル("
						+ configFilename
						+ ")の読み込み中にエラーが発生したため，デフォルトの設定が読み込まれました。"
						+ "\nエラーメッセージ：\n" + e.getLocalizedMessage());
				System.err.println("Error(SysConfig): " + "設定ファイルの読み込み中にエラーが発生したため，デフォルトの設定が読み込まれました。");
				e.printStackTrace();
				setDefault(commentTypes, discussers);
			}

		} else {
			System.err.println("Warning(SysConfig): fishwatchr.config が見つからなかったため，デフォルトの設定を使用します。");
			setDefault(commentTypes, discussers);
		}
	}
	
	public void setDefault(ArrayList<CommentType> commentTypes, ArrayList<User> discussers){
		discussers.clear();
		discussers.add(new User("話者１"));
		discussers.add(new User("話者２"));
		discussers.add(new User("話者３"));
		discussers.add(new User("話者４"));
		discussers.add(new User("不特定"));
		discussers.add(new User("誤り"));
		discussers.add(new User(""));
		discussers.add(new User(""));

		commentTypes.clear();
		commentTypes.add(new CommentType("意見", Color.red));
		commentTypes.add(new CommentType("質問", Color.ORANGE));
		commentTypes.add(new CommentType("管理", Color.blue));
		commentTypes.add(new CommentType("相づち", Color.green));
		commentTypes.add(new CommentType("確認", Color.cyan));
		commentTypes.add(new CommentType("その他", Color.yellow));
		commentTypes.add(new CommentType("誤り", Color.magenta));
		commentTypes.add(new CommentType("", new Color(10, 10, 10)));
		commentTypes.add(new CommentType("", new Color(60, 60, 60)));
		commentTypes.add(new CommentType("", new Color(110, 110, 110)));
		commentTypes.add(new CommentType("", new Color(160, 160, 160)));
		commentTypes.add(new CommentType("", new Color(210, 210, 210)));
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
				nodeValue = "";
			}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		
		return nodeValue;
	}
	
}
