/*
    Copyright (C) 2014-2015 Masaya YAMAGUCHI

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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringEscapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class CommentList extends LinkedList<Comment> {

	private static final long serialVersionUID = 1L;
	private static final String NOT_DEFINED = "(未定義)";

	public static final String FILE_SUFFIX = ".xml";
	public static final String MERGED_FILE_SUFFIX = ".merged.xml";
	public static final String BACKUP_DIR = "BAK";
	
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"); 
	private Date startTime = null;

	private boolean isModified = false;
//	private HashMap<String, Integer> mapStartTimeOffset = new HashMap<String, Integer>();
	private HashMap<String, Integer> mapCommentTimeOffset = new HashMap<String, Integer>();
	private HashMap<String, String> mapStartTime = new HashMap<String, String>();
	private String mediaFilename = "";
	private String setName = "";

	
	@Override
	public boolean add(Comment comment){
		super.add(comment);
		isModified = true;
		return true;
	}
	
	@Override
	public void add(int index, Comment comment) {
		super.add(index, comment);
		isModified = true;
	}
	
	
	@Override
	public Comment remove(int i) {
		isModified = true;
		return super.remove(i);
	}
	
	public void clear(){
		super.clear();
		startTime = null;
		isModified = false;
		Comment.resetID(); // id を 0 に戻す
		mapCommentTimeOffset.clear();
		mapStartTime.clear();
	}
	
	
	public void setModified(boolean flag){
		isModified = flag;
		if(!isModified){
			for(Comment comment: this){
				comment.setModified(false);
			}
		}
	}

	public boolean isModified(){
		if(isModified){
			return true;
		}
		for(Comment comment: this){
			if(comment.isModified()){
				return true;
			}
		}
		return false;
	}
	
	
	public void setStartTime(Date startTime){
		this.startTime = startTime;
		if(setName.isEmpty()){
			System.err.println("Error(CommentList): セット名がつけられていません！！！");
		}
		mapStartTime.put(setName, dateFormat.format(startTime));
	}
	

	public boolean isStartTimeSet(){
		if(startTime == null){
			return false;
		} else {
			return true;
		}
	}
	
	// 返り値：メッセージ
	public String save(String xmlFilename,
			ArrayList<CommentType> commentTypes, ArrayList<User> discussers)
			throws IOException {

		if (!xmlFilename.endsWith(FILE_SUFFIX)) {
			return xmlFilename + "は拡張子が .xml ではないため，保存の処理を中止します。";
		}
		String startTimeStr = startTime != null ? dateFormat.format(startTime)
				: NOT_DEFINED;
		HashMap<String, String> setStr = new HashMap<String, String>();
		String message = "";

		File xmlFile = new File(xmlFilename);
		if (xmlFile.exists()) {
			String parentPath = xmlFile.getParentFile().getCanonicalPath();
			String filename = xmlFile.getName();
			
			String backupFilename =
					getUniqueFilename(
							parentPath + File.separatorChar
							+ BACKUP_DIR + File.separatorChar
							+ filename + ".bak");
			File backupDir = new File(parentPath + File.separatorChar + BACKUP_DIR);
			if(!backupDir.exists()){
				backupDir.mkdir();
			}
			File backupFile = new File(backupFilename);
			Files.copy(xmlFile.toPath(), backupFile.toPath());
			message += xmlFile.toPath().getFileName() + " は，すでに存在するため，"
					+ backupFile.getAbsolutePath() + "へバックアップしました。";
		}

		OutputStreamWriter ow = new OutputStreamWriter(new FileOutputStream(
				xmlFilename), "utf-8");

		// header
		ow.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		String newMediaFilename = mediaFilename;
		if(mediaFilename.matches("^https?://.+")){
			newMediaFilename = URLEncoder.encode(mediaFilename, "utf-8");
		} else {
			newMediaFilename = new File(mediaFilename).getName();
		}
		ow.write("<comment_list" + " start_time=\"" + startTimeStr + "\" media_file=\"" + newMediaFilename + "\">\n");

		// commentTypes settings
		ow.write("  <comment_types>\n");
		for (int i = 0; i < commentTypes.size(); i++) {
			// System.err.println("aa:" + commentTypes.get(i).getType());
			ow.write("    <li name=\"" + commentTypes.get(i).getType()
					+ "\" color=\"" + commentTypes.get(i).getColor().getRGB()
					+ "\" />\n");
		}
		ow.write("  </comment_types>\n");

		// discussers settings
		ow.write("  <discussers>\n");
		for (int i = 0; i < discussers.size(); i++) {
			ow.write("    <li name=\"" + discussers.get(i).getName()
					+ "\" />\n");
		}
		ow.write("  </discussers>\n");

		for (Comment comment : this) {
			String setName = comment.getSetName();
			String setValue = "";
			if (setStr.containsKey(setName)) {
				setValue = setStr.get(setName);
			}
			setStr.put(setName, setValue + "    <comment" + " date=\""
					+ dateFormat.format(comment.getDate()) + "\""
					+ " commenter=\"" + comment.getCommenter().getName() + "\""
					+ " discusser=\"" + comment.getDiscusser().getName() + "\""
					+ " comment_type=\""
					+ comment.getCommentType().getType() + "\""
					+ " comment_time=\"" + comment.getCommentTime() + "\""
					+ " comment_time_end=\"" + comment.getCommentTimeEnd()
					+ "\"" + ">"
					+ StringEscapeUtils.escapeXml11(comment.getContentBody())
					+ "</comment>\n");
		}

		for (Map.Entry<String, String> item : setStr.entrySet()) {
			String name = item.getKey();
			int offset = getCommentTimeOffset(item.getKey());
			ow.write("  <set name=\"" + name + "\" original_start_time=\""
					+ startTimeStr + "\" correction_time=\"" + offset + "\">\n");
			ow.write(item.getValue());
			ow.write("  </set>\n");
		}

		
		// comment がないセットだけが出力される場合（念のため）
		if(setStr.isEmpty()){
			for (Map.Entry<String, String> item : mapStartTime.entrySet()) {
				String name = item.getKey();
				if (!setStr.containsKey(name)) {
					int offset = getCommentTimeOffset(item.getKey());
					ow.write("  <set name=\"" + name
							+ "\" original_start_time=\"" + startTimeStr
							+ "\" correction_time=\"" + offset + "\">\n");
					ow.write("  </set>\n");
				}
			}
		}

		ow.write("</comment_list>\n");
		ow.close();
		setModified(false);
		message += "\n" + xmlFile.getCanonicalPath() + " に結果を保存しました。";

		return message;
	}
	
	// 返り値：media filename
	public String load(String targetFilename,
			ArrayList<CommentType> commentTypes, ArrayList<User> discussers, boolean flagAdd) throws ParseException, ParserConfigurationException, SAXException, IOException, XPathExpressionException {

		if(!flagAdd){
			// リストの初期化(override)
			clear();
			
			// commentTypes 初期化
			for (CommentType commentType : commentTypes) {
				commentType.set("", Color.gray);
			}

			// discussers 初期化
			for (User discusser : discussers) {
				discusser.setName("");
			}
		}
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(new File(targetFilename));

		XPathFactory xPathFactory = XPathFactory.newInstance();
		XPath xpath = xPathFactory.newXPath();

		// start_time
		XPathExpression expr = xpath.compile("/comment_list/@start_time");
		String strStartTime = (String) expr.evaluate(doc,
				XPathConstants.STRING);

		Date newStartTime = dateFormat.parse(strStartTime);
		if(newStartTime == null){
			throw new XPathExpressionException(targetFilename + "はファイルの形式が不正です。\n" +
					"/comment_list/@start_time が正しく読み込めませんでした。");
		} else if(startTime == null){
			startTime = newStartTime;
		} else if(newStartTime.compareTo(startTime) < 0){
			startTime = newStartTime;
		}

		// media_file
		expr = xpath.compile("/comment_list/@media_file");
		mediaFilename = (String) expr.evaluate(doc, XPathConstants.STRING);
		if(mediaFilename == null){
			System.err.println("warning:(MainFrame.java): " + targetFilename + " には，/comment_list/@media_file がありません。");
			mediaFilename = "";
		} else if(URLDecoder.decode(mediaFilename, "utf-8").matches("^https?://.+")){
			mediaFilename = URLDecoder.decode(mediaFilename, "utf-8");
		} else if(!mediaFilename.isEmpty()){
			mediaFilename = new File(targetFilename).getParent() + "/" + mediaFilename;
		}
		
		
		// コメント読み込み
		expr = xpath.compile("/comment_list");
		NodeList commentListNodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		if(commentListNodes.getLength() == 0){
			throw new XPathExpressionException("ファイルの形式が不正です。comment_listがありません。");
		}

		// comment_types 要素
		expr = xpath.compile("/comment_list/comment_types/li");
		NodeList commentTypesNodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		boolean flagRegister = false;
		for (int i = 0; i < commentTypesNodes.getLength(); i++) {
			String commentTypeName = ((Element)commentTypesNodes.item(i)).getAttribute("name");
			String commentTypeColor = ((Element)commentTypesNodes.item(i)).getAttribute("color");

			flagRegister = false;
			for (int j = 0; j < commentTypes.size(); j++) {
				CommentType ct = commentTypes.get(j);
				if (ct.getType().equals(commentTypeName)) {
					flagRegister = true;
					break;
				} else if (ct.getType().isEmpty()) {
					ct.set(commentTypeName, new Color(Integer.parseInt(commentTypeColor)));
					flagRegister = true;
					break;
				}
			}
			
			if(!flagRegister){
				System.err.println("Warning(CommentList): ラベル " + commentTypeName + " が登録できませんでした。");
			}
		}
		
		// discussers 要素
		expr = xpath.compile("/comment_list/discussers/li");
		NodeList discussersNodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		for (int i = 0; i < discussersNodes.getLength(); i++) {
			String discusserName = ((Element)discussersNodes.item(i)).getAttribute("name");

			flagRegister = false;
			for (int j = 0; j < discussers.size(); j++) {
				User discusser = discussers.get(j);
				if (discusser.getName().equals(discusserName)) {
					flagRegister = true;
					break;
				} else if (discusser.getName().isEmpty()) {
					discusser.setName(discusserName);
					flagRegister = true;
					break;
				}
			}
			if(!flagRegister){
				System.err.println("Warning(CommentList): ラベル " + discusserName + " が登録できませんでした。");
			}
		}
		
		
		Element commentListElement = (Element) commentListNodes.item(0);
		// set 要素
		NodeList setNodes = commentListElement.getElementsByTagName("set");
		for (int i = 0; i < setNodes.getLength(); i++) {
			Element setNode = (Element) setNodes.item(i);
			String setName = setNode.getAttribute("name");
			String originalStartTime = setNode.getAttribute("original_start_time");
			int value = Integer.valueOf(setNode.getAttribute("correction_time"));
			setCommentTimeOffset(setName, value);
			mapStartTime.put(setName, originalStartTime);

			NodeList commentNodes = setNode.getElementsByTagName("comment");
			for (int j = 0; j < commentNodes.getLength(); j++) {
				Element commentNode = (Element) commentNodes.item(j);
				String contentBody = commentNode.getTextContent();
				User discusser = new User(commentNode.getAttribute("discusser"));
				User commenter = new User(commentNode.getAttribute("commenter"));
				String strCommentType = commentNode.getAttribute("comment_type");

				// コメントタイプ登録
				CommentType commentType = new CommentType("", Color.gray); // default
				for (CommentType ct : commentTypes) {
					if (ct.getType().equals(strCommentType)) {
						commentType = ct;
						break;
					}
				}


				Date commentDate;
				commentDate = dateFormat.parse(commentNode.getAttribute("date"));
				int commentTime = Integer.parseInt(commentNode
						.getAttribute("comment_time"));
				int commentTimeEnd = Integer.parseInt(commentNode
						.getAttribute("comment_time_end"));

				Comment comment = new Comment();
				comment.set(contentBody, commentType, commenter, discusser,
						commentDate, commentTime, setName);

				if (commentTimeEnd != Comment.COMMENT_TIME_END_UNDEFINED)
					comment.setCommentTimeEnd(commentTimeEnd);
				add(comment);
			}
		}

		System.err.println("load end");

		Collections.sort(this, new Comparator<Comment>() {
			public int compare(Comment c1, Comment c2) {
				return unifiedCommentTime(c1) - unifiedCommentTime(c2);
			}
		});
		
		refreshID();
		setModified(false);

		
		return mediaFilename;
	}
	

	public ArrayList<String> merge(String dirName,
			ArrayList<CommentType> commentTypes, ArrayList<User> discussers) throws IOException, XPathExpressionException, ParseException, ParserConfigurationException, SAXException {

		File dir = new File(dirName);
		boolean flagAdd = false;
		String candMediafilename = "";
		File[] files = dir.listFiles();
		ArrayList<String> results = new ArrayList<String>();
		String mergedFileSuffix = MERGED_FILE_SUFFIX;
		String backupFileSuffix = mergedFileSuffix.replaceFirst(FILE_SUFFIX, "");
		
		for (File file : files) {
			String filename = file.getCanonicalPath();
			
			if (filename.endsWith(FILE_SUFFIX)){
				if(filename.endsWith(mergedFileSuffix)
						|| filename.matches(".*" + backupFileSuffix + "\\.\\d\\d\\d" + FILE_SUFFIX + "$")){
					System.err.println("Warning(CommentList): exclude " + filename);
					continue;
				}
				
				load(filename, commentTypes, discussers, flagAdd);
				results.add(file.getName());
				// 初回だけ，false にする
				if (!flagAdd) {
					flagAdd = true;
				}
			} else if(SoundPlayer.isPlayable(filename)){
				if(candMediafilename.isEmpty()){
					candMediafilename = filename;
				} else if(!candMediafilename.equals(filename)){
					throw new IllegalStateException("複数のメディアファイル（"
							+ new File(candMediafilename).getName()
							+ ",\n"
							+ new File(filename).getName()
							+ "）があります。\nフォルダに含めるメディアファイルは一つにしてください。");
				}
			}
		}
		mediaFilename = candMediafilename;
		results.add(0, mediaFilename);
		
		return results;
	}
	
	
	public void export(String filename) throws IOException{

		PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
		
		for(Comment comment: this){
			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < Comment.N_Field; i++){
				String value;
				
				if(i == Comment.F_COMMENT_TIME){
					value = formatTime(unifiedCommentTime(comment));
				} else {
					value = comment.getAt(i).toString();
				}
				sb.append(value + "\t");
			}
			pw.println(sb.toString().replaceFirst("\t$", ""));
		}
		pw.close();
	}
	
	// time ミリ秒
	private String formatTime(int msec){
		int time = (int) (msec / 1000);
		int hour = time / 3600;
		time -= hour * 3600;
		int minute = time / 60;
		int sec = time - minute * 60;

		return String.format("%02d:%02d:%02d", hour, minute, sec);
	}

	
	
	public void refreshID(){
		int i = 1;
		for(Comment comment: this){
			comment.setID(i++);
		}
	}

//	public void unityTime() throws ParseException{
//
//		// 最も最初の startTime を探す
//		String earliestStartTimeStr = "";
//		for(Map.Entry<String, String> item : mapStartTime.entrySet()){
//			String itemTime = item.getValue();
//			if(earliestStartTimeStr.isEmpty() || earliestStartTimeStr.compareTo(itemTime) > 0){
//				earliestStartTimeStr = itemTime;;
//			}
//		}
//		
//		// 各セットの開始時間との差分を計算しておく
////		long earliestStartTime = dateFormat.parse(earliestStartTimeStr).getTime();
////		for(Map.Entry<String, String> item : mapStartTime.entrySet()){
////			long itemTime = dateFormat.parse(item.getValue()).getTime();
////			mapStartTimeOffset.put(item.getKey(), (int)(itemTime - earliestStartTime));
////		}
//	}
	
	
	public void setCommentTimeOffset(String setName, int offset){
		mapCommentTimeOffset.put(setName,  offset);
	}

	
	public int getCommentTimeOffset(String setName){
		if(mapCommentTimeOffset.containsKey(setName)){
			return mapCommentTimeOffset.get(setName);
		} else {
			return 0;
		}
	}

	
	public String getStartTime(String setName){
		if(mapStartTime.containsKey(setName)){
			return mapStartTime.get(setName);
		} else {
			return "";
		}
	}
	

	// 補正値を合わせた経過時間
	public int unifiedCommentTime(Comment comment){
		String setName = comment.getSetName();
		int timeCorrection = getCommentTimeOffset(setName);
		return comment.getCommentTime() + timeCorrection;
	}

	
	public void setSetName(String filename, User commenter){
		setName = new File(filename).getName();
		System.err.println("sn:" + setName);
		if(setName.isEmpty()){
			SimpleDateFormat today = new SimpleDateFormat("yyyyMMdd");
			setName = today.format(new Date());
		} else {
			if(setName.endsWith(CommentList.FILE_SUFFIX)){
				// 末尾の .xml を削除
				setName = setName.replaceFirst("\\.xml$", "");
			}
			if(SoundPlayer.isPlayable(setName)){
				// 末尾の拡張子を削除
				setName = setName.replaceFirst("\\.[^\\.]+$", "");
			}
		}
		if(!setName.endsWith("_" + commenter.getName())){
			// 末尾にユーザ名がついていなければ，追加
			setName += "_" + commenter.getName();
		}
		// ファイル名冒頭のシステム名を削除
		setName = setName.replaceFirst("^" + FishWatchr.SYSTEM_NAME.toLowerCase(), "");
		
		return;
	}
	
	
	public String getSetName(){
		return setName;
	}
	
	
	public Set<String> getSetNames(){
		return mapStartTime.keySet();
	}
	
	
	public int getSetSize(){
		return mapStartTime.size();
	}
		

	public void setMediaFilename(String filename){
		if(filename.matches("^https?://.+")){
			mediaFilename = filename;
		} else {
			mediaFilename = new File(filename).getName();
		}
	}

	
	public String getMediaFilename(){
		return mediaFilename;
	}
		
	
	// すでにファイル yyy.xml が存在する場合は，yyy.001.xml を返す
	static public String getUniqueFilename(String filename){
		String nameBody;
		String suffix;
		int p = filename.lastIndexOf(".");
		int c = 1;
		
		if(p != -1){
			nameBody = filename.substring(0, p);
			suffix = filename.substring(p, filename.length());
		} else {
			nameBody = filename;
			suffix = "";
		}

		if(new File(filename).exists()){
			while(true){
				String newFilename = nameBody + String.format(".%03d", c++) + suffix;
				if(new File(newFilename).exists()){
					continue;
				} else {
					return newFilename;
				}
			}
		} else {
			return filename;
		}
	}

	
	class UserComparator implements Comparator<User> {

	    public int compare(User a, User b) {
	    	if(a.getName().isEmpty()) return 1;
	    	if(b.getName().isEmpty()) return -1;
	    	return a.getName().compareTo(b.getName());
	    }
	}
	
	class CommentTypeComparator implements Comparator<CommentType> {

	    public int compare(CommentType a, CommentType b) {
	    	if(a.getType().isEmpty()) return 1;
	    	if(b.getType().isEmpty()) return -1;
	    	return a.getType().compareTo(b.getType());
	    }
	}
}
