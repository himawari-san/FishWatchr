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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.text.StringEscapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class CommentList extends ArrayList<Comment> {

	private static final long serialVersionUID = 1L;
	private static final String NOT_DEFINED = Messages.getString("CommentList.0"); //$NON-NLS-1$
	private static final int maxElapsedTime = 1000 * 60 * 60 * 2; // 2 hours
	
	public static final String FILE_SUFFIX = ".xml"; //$NON-NLS-1$
	public static final String ANNOTATION_FILE_BASENAME_FOR_STREAM = "stream"; //$NON-NLS-1$
	public static final String MERGED_FILE_SUFFIX = ".merged.xml"; //$NON-NLS-1$
	public static final String BACKUP_DIR = "BAK"; //$NON-NLS-1$
	public static final String BASE_TIME_FILE_PREFIX = "_sys_basetime"; //$NON-NLS-1$
	public static final String LOCKFILE_SUFFIX =".lock"; //$NON-NLS-1$
	
	private HashMap<String, OverallEvaluation> evaluations = new HashMap<String, OverallEvaluation>();
	
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");  //$NON-NLS-1$
	private Date startTime = null;

	private boolean isModified = false;
	private File lockFile =  null;
//	private HashMap<String, Integer> mapStartTimeOffset = new HashMap<String, Integer>();
	private HashMap<String, Integer> mapCommentTimeOffset = new HashMap<String, Integer>();
	private HashMap<String, String> mapStartTime = new HashMap<String, String>();
	// a relative path from a observation file to a media file, or an absolute path to a media file 
	private String mediaFilename = ""; //$NON-NLS-1$
	// /comment_list/@media_file
	private String mediaFilenameOriginal = ""; //$NON-NLS-1$
	private String setName = ""; //$NON-NLS-1$
	private User annotator = new User("");

	
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
			System.err.println("Warning(CommentList): no set name."); //$NON-NLS-1$
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
			return xmlFilename + Messages.getString("CommentList.2"); //$NON-NLS-1$
		}
		String startTimeStr = startTime != null ? dateFormat.format(startTime)
				: NOT_DEFINED;
		HashMap<String, String> setStr = new HashMap<String, String>();
		String message = ""; //$NON-NLS-1$

		File xmlFile = new File(xmlFilename);
		if (xmlFile.exists()) {
			String parentPath = xmlFile.getParentFile().getCanonicalPath();
			String filename = xmlFile.getName();
			
			String backupFilename =
					getUniqueFilename(
							parentPath + File.separatorChar
							+ BACKUP_DIR + File.separatorChar
							+ filename + ".bak"); //$NON-NLS-1$
			File backupDir = new File(parentPath + File.separatorChar + BACKUP_DIR);
			if(!backupDir.exists()){
				backupDir.mkdir();
			}
			File backupFile = new File(backupFilename);
			Files.copy(xmlFile.toPath(), backupFile.toPath());
			message += xmlFile.toPath().getFileName() + "\n" + Messages.getString("CommentList.3") //$NON-NLS-1$ //$NON-NLS-2$
					+ "\n" + backupFile.getAbsolutePath() + "\n" + Messages.getString("CommentList.4") + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}

		OutputStreamWriter ow = new OutputStreamWriter(new FileOutputStream(
				xmlFilename), "utf-8"); //$NON-NLS-1$

		// header
		ow.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); //$NON-NLS-1$
		String newMediaFilename = mediaFilename;
		if(mediaFilename.matches("^https?://.+")){ //$NON-NLS-1$
			newMediaFilename = URLEncoder.encode(mediaFilename, "utf-8"); //$NON-NLS-1$
		} else if(!mediaFilenameOriginal.isEmpty()){
			newMediaFilename = mediaFilenameOriginal;
		}
		
		ow.write("<comment_list" + " start_time=\"" + startTimeStr + "\" media_file=\"" + newMediaFilename + "\" group_name=\"" + annotator.getGroupName() + "\">\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

		// commentTypes settings
		ow.write("  <comment_types>\n"); //$NON-NLS-1$
		for (int i = 0; i < commentTypes.size(); i++) {
			ow.write("    <li name=\"" + commentTypes.get(i).getType() //$NON-NLS-1$
					+ "\" color=\"" + commentTypes.get(i).getColor().getRGB() //$NON-NLS-1$
					+ "\" />\n"); //$NON-NLS-1$
		}
		ow.write("  </comment_types>\n"); //$NON-NLS-1$

		// discussers settings
		ow.write("  <discussers>\n"); //$NON-NLS-1$
		for (int i = 0; i < discussers.size(); i++) {
			ow.write("    <li name=\"" + discussers.get(i).getUserName() //$NON-NLS-1$
					+ "\" />\n"); //$NON-NLS-1$
		}
		ow.write("  </discussers>\n"); //$NON-NLS-1$

		for (Comment comment : this) {
			String setName = comment.getSetName();
			String setValue = ""; //$NON-NLS-1$
			if (setStr.containsKey(setName)) {
				setValue = setStr.get(setName);
			}
			setStr.put(setName, setValue + "    <comment" + " date=\"" //$NON-NLS-1$ //$NON-NLS-2$
					+ dateFormat.format(comment.getDate()) + "\"" //$NON-NLS-1$
					+ " commenter=\"" + comment.getCommenter().getUserName() + "\"" //$NON-NLS-1$ //$NON-NLS-2$
					+ " discusser=\"" + comment.getDiscusser().getUserName() + "\"" //$NON-NLS-1$ //$NON-NLS-2$
					+ " comment_type=\"" //$NON-NLS-1$
					+ comment.getCommentType().getType() + "\"" //$NON-NLS-1$
					+ " comment_time=\"" + comment.getCommentTime() + "\"" //$NON-NLS-1$ //$NON-NLS-2$
					+ " comment_time_end=\"" + comment.getCommentTimeEnd() + "\"" //$NON-NLS-1$ //$NON-NLS-2$
					+ " aux=\"" + StringEscapeUtils.escapeXml11(comment.getAux()) //$NON-NLS-1$
					+ "\"" + ">" //$NON-NLS-1$ //$NON-NLS-2$
					+ StringEscapeUtils.escapeXml11(comment.getCommentBody())
					+ "</comment>\n"); //$NON-NLS-1$
		}

		for (Map.Entry<String, String> item : setStr.entrySet()) {
			String name = item.getKey();
			int offset = getCommentTimeOffset(item.getKey());
			ow.write("  <set name=\"" + name + "\" original_start_time=\"" //$NON-NLS-1$ //$NON-NLS-2$
					+ startTimeStr + "\" correction_time=\"" + offset + "\">\n"); //$NON-NLS-1$ //$NON-NLS-2$
			ow.write(item.getValue());
			ow.write("  </set>\n"); //$NON-NLS-1$
		}

		
		// comment がないセットだけが出力される場合（念のため）
		if(setStr.isEmpty()){
			for (Map.Entry<String, String> item : mapStartTime.entrySet()) {
				String name = item.getKey();
				if (!setStr.containsKey(name)) {
					int offset = getCommentTimeOffset(item.getKey());
					ow.write("  <set name=\"" + name //$NON-NLS-1$
							+ "\" original_start_time=\"" + startTimeStr //$NON-NLS-1$
							+ "\" correction_time=\"" + offset + "\">\n"); //$NON-NLS-1$ //$NON-NLS-2$
					ow.write("  </set>\n"); //$NON-NLS-1$
				}
			}
		}

		ow.write("</comment_list>\n"); //$NON-NLS-1$
		ow.close();
		setModified(false);
		message += "\n" + Messages.getString("CommentList.5") //$NON-NLS-1$ //$NON-NLS-2$
				+ "\n" + xmlFile.getCanonicalPath(); //$NON-NLS-1$ //$NON-NLS-2$

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
				commentType.set("", Color.gray); //$NON-NLS-1$
			}

			// discussers 初期化
			for (User discusser : discussers) {
				discusser.setUserName(""); //$NON-NLS-1$
			}
			
			evaluations.clear();
		}
		
		DocumentBuilderFactory factory = Util.getSimpleDocumentBuilderFactory();

		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(new File(targetFilename));
		

		XPathFactory xPathFactory = XPathFactory.newInstance();
		XPath xpath = xPathFactory.newXPath();

		// start_time
		XPathExpression expr = xpath.compile("/comment_list/@start_time"); //$NON-NLS-1$
		String strStartTime = (String) expr.evaluate(doc,
				XPathConstants.STRING);

		Date newStartTime = dateFormat.parse(strStartTime);
		if(newStartTime == null){
			throw new XPathExpressionException(Messages.getString("CommentList.6") + targetFilename); //$NON-NLS-1$
		} else if(startTime == null){
			startTime = newStartTime;
		} else {
			startTime = newStartTime;
		}

		// media_file
		expr = xpath.compile("/comment_list/@media_file"); //$NON-NLS-1$
		mediaFilename = (String) expr.evaluate(doc, XPathConstants.STRING);
		mediaFilenameOriginal = mediaFilename;
		if(mediaFilename == null){
			System.err.println("Warning:(MainFrame.java): " + targetFilename + Messages.getString("CommentList.8")); //$NON-NLS-1$ //$NON-NLS-2$
			mediaFilename = ""; //$NON-NLS-1$
		} else if(URLDecoder.decode(mediaFilename, "utf-8").matches("^https?://.+")){ //$NON-NLS-1$ //$NON-NLS-2$
			mediaFilename = URLDecoder.decode(mediaFilename, "utf-8"); //$NON-NLS-1$
		} else if(!mediaFilename.isEmpty()){
			if(new File(mediaFilename).isAbsolute()){
				// do nothing
			} else {
				mediaFilename = new File(targetFilename).getParent() + "/" + mediaFilename; //$NON-NLS-1$
			}
		}
		
		
		// コメント読み込み
		expr = xpath.compile("/comment_list"); //$NON-NLS-1$
		NodeList commentListNodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		if(commentListNodes.getLength() == 0){
			throw new XPathExpressionException(Messages.getString("CommentList.9")); //$NON-NLS-1$
		}

		// comment_types 要素
		expr = xpath.compile("/comment_list/comment_types/li"); //$NON-NLS-1$
		NodeList commentTypesNodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		boolean flagRegister = false;
		for (int i = 0; i < commentTypesNodes.getLength(); i++) {
			String commentTypeName = ((Element)commentTypesNodes.item(i)).getAttribute("name"); //$NON-NLS-1$
			String commentTypeColor = ((Element)commentTypesNodes.item(i)).getAttribute("color"); //$NON-NLS-1$

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
				System.err.println("Warning(CommentList): " + CommentTableModel.ITEM_LABEL + " " + commentTypeName + Messages.getString("CommentList.10")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		}
		
		// discussers 要素
		expr = xpath.compile("/comment_list/discussers/li"); //$NON-NLS-1$
		NodeList discussersNodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		for (int i = 0; i < discussersNodes.getLength(); i++) {
			String discusserName = ((Element)discussersNodes.item(i)).getAttribute("name"); //$NON-NLS-1$

			flagRegister = false;
			for (int j = 0; j < discussers.size(); j++) {
				User discusser = discussers.get(j);
				if (discusser.getUserName().equals(discusserName)) {
					flagRegister = true;
					break;
				} else if (discusser.getUserName().isEmpty()) {
					discusser.setUserName(discusserName);
					flagRegister = true;
					break;
				}
			}
			if(!flagRegister){
				System.err.println("Warning(CommentList): " + CommentTableModel.ITEM_LABEL + " " + discusserName + Messages.getString("CommentList.11")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		}
		
		// evaluation
		expr = xpath.compile("/comment_list/evaluations/evaluation"); //$NON-NLS-1$
		NodeList evaluationNodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		for (int i = 0; i < evaluationNodes.getLength(); i++) {
			OverallEvaluation evaluation = new OverallEvaluation((Element)evaluationNodes.item(i));
			if(evaluation != null) {
				evaluations.put(evaluation.getEvaluatorName(), evaluation);
			}
		}

		
		Element commentListElement = (Element) commentListNodes.item(0);
		// set 要素
		NodeList setNodes = commentListElement.getElementsByTagName("set"); //$NON-NLS-1$
		for (int i = 0; i < setNodes.getLength(); i++) {
			Element setNode = (Element) setNodes.item(i);
			String setName = setNode.getAttribute("name"); //$NON-NLS-1$
			String originalStartTime = setNode.getAttribute("original_start_time"); //$NON-NLS-1$
			int value = Integer.valueOf(setNode.getAttribute("correction_time")); //$NON-NLS-1$
			setCommentTimeOffset(setName, value);
			mapStartTime.put(setName, originalStartTime);

			NodeList commentNodes = setNode.getElementsByTagName("comment"); //$NON-NLS-1$
			for (int j = 0; j < commentNodes.getLength(); j++) {
				Element commentNode = (Element) commentNodes.item(j);
				String contentBody = commentNode.getTextContent();
				User discusser = new User(commentNode.getAttribute("discusser")); //$NON-NLS-1$
				User commenter = new User(commentNode.getAttribute("commenter")); //$NON-NLS-1$
				String strCommentType = commentNode.getAttribute("comment_type"); //$NON-NLS-1$
				String aux = commentNode.getAttribute("aux"); //$NON-NLS-1$

				// コメントタイプ登録
				CommentType commentType = new CommentType("", Color.gray); // default //$NON-NLS-1$
				for (CommentType ct : commentTypes) {
					if (ct.getType().equals(strCommentType)) {
						commentType = ct;
						break;
					}
				}


				Date commentDate;
				commentDate = dateFormat.parse(commentNode.getAttribute("date")); //$NON-NLS-1$
				int commentTime = Integer.parseInt(commentNode
						.getAttribute("comment_time")); //$NON-NLS-1$
				int commentTimeEnd = Integer.parseInt(commentNode
						.getAttribute("comment_time_end")); //$NON-NLS-1$

				Comment comment = new Comment();
				comment.set(contentBody, commentType, commenter, discusser,
						commentDate, commentTime, setName, aux);

				if (commentTimeEnd != Comment.COMMENT_TIME_END_UNDEFINED)
					comment.setCommentTimeEnd(commentTimeEnd);
				add(comment);
				
				OverallEvaluation evaluation = getEvaluation(commenter.getUserName());
				evaluation = evaluation == null ? new OverallEvaluation(commenter.getUserName()) : evaluation;
				
			}
		}

		// group name
		expr = xpath.compile("/comment_list/@group_name"); //$NON-NLS-1$
		String groupName = (String) expr.evaluate(doc, XPathConstants.STRING);
		if(groupName == null){
			annotator.setGroupName("");
		} else {
			annotator.setGroupName(groupName);
		}
		
		System.err.println("load end"); //$NON-NLS-1$

		sortByTime();
		refreshID();
		setModified(false);

		return mediaFilename;
	}
	

	public HashMap<String, OverallEvaluation> getEvaluations() {
		return evaluations;
	}

	
	public OverallEvaluation getEvaluation(String evaluatorName) {
		return evaluations.get(evaluatorName);
	}
	
	
	public void setEvaluation(String evaluatorName, OverallEvaluation evaluation) {
		evaluations.put(evaluatorName, evaluation);
	}
	
	
	public File lock(String filename){
		if(lockFile == null){
			if(new File(filename + LOCKFILE_SUFFIX).exists()){
				return null;
			}
		} else {
			lockFile.delete();
		}

		lockFile = new File(filename + LOCKFILE_SUFFIX);
		lockFile.deleteOnExit();

		try {
			FileWriter fw = new FileWriter(lockFile);
			fw.write(""); //$NON-NLS-1$
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return lockFile;
	}

	
	public void unlock(){
		if(lockFile != null){
			lockFile.delete();
			lockFile = null;
		}
	}

	
	public void sortByTime(){
		Collections.sort(this, new Comparator<Comment>() {
			public int compare(Comment c1, Comment c2) {
				return unifiedCommentTime(c1) - unifiedCommentTime(c2);
			}
		});
	}

	
	public ArrayList<String> merge(String dirName,
			ArrayList<CommentType> commentTypes, ArrayList<User> discussers) throws IOException, XPathExpressionException, ParseException, ParserConfigurationException, SAXException {

		File dir = new File(dirName);
		boolean flagAdd = false;
		String candMediafilename = ""; //$NON-NLS-1$
		File[] files = dir.listFiles();
		ArrayList<String> results = new ArrayList<String>();
		String mergedFileSuffix = MERGED_FILE_SUFFIX;
		String backupFileSuffix = mergedFileSuffix.replaceFirst(FILE_SUFFIX, ""); //$NON-NLS-1$
		ArrayList<File> baseTimeFileCandidates = new ArrayList<File>();
		
		for (File file : files) {
			String filename = file.getCanonicalPath();
			String separator = File.separator.equals("\\") ? File.separator + File.separator : File.separator;  //$NON-NLS-1$

			if (filename.endsWith(FILE_SUFFIX)){
				if(file.getName().startsWith(".")) { //$NON-NLS-1$
					continue;
				} else if(filename.matches(".*" + separator + //$NON-NLS-1$
						BASE_TIME_FILE_PREFIX +
						"_[^" + separator + "]*"+ //$NON-NLS-1$ //$NON-NLS-2$
						FILE_SUFFIX + "$")){ // basetime //$NON-NLS-1$
					baseTimeFileCandidates.add(file);
					System.err.println("Message(CommentList): found a basetime file " + filename); //$NON-NLS-1$
					continue;
				} else if(filename.endsWith(mergedFileSuffix) // merge file
						|| filename.matches(".*" + backupFileSuffix + "\\.\\d\\d\\d" + FILE_SUFFIX + "$") // backup				 //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						){
					System.err.println("Warning(CommentList): exclude " + filename); //$NON-NLS-1$
					continue;
				}
				
				load(filename, commentTypes, discussers, flagAdd);
				results.add(file.getName());
				
				// update candMediafilename
				if(!SoundPlayer.isStream(mediaFilename)) {
					// do nothing
				} else if(candMediafilename.isEmpty()){
					candMediafilename = mediaFilename;
				} else if(!candMediafilename.equals(mediaFilename)){
					throw new IllegalStateException(
							Messages.getString("CommentList.1") //$NON-NLS-1$
							+ "\n" //$NON-NLS-1$
							+ candMediafilename + ", " //$NON-NLS-1$
							+ mediaFilename);
				}
				
				
				// 初回だけ，false にする
				if (!flagAdd) {
					flagAdd = true;
				}
			} else if(SoundPlayer.isPlayableFile(filename)){
				if(candMediafilename.isEmpty()){
					candMediafilename = filename;
				} else if(!candMediafilename.equals(filename)){
					throw new IllegalStateException(Messages.getString("CommentList.12") //$NON-NLS-1$
							+ new File(candMediafilename).getName()
							+ ",\n" //$NON-NLS-1$
							+ new File(filename).getName()
							+ Messages.getString("CommentList.13")); //$NON-NLS-1$
				}
			}
		}
		
		if(!flagAdd){
			throw new IllegalStateException(Messages.getString("CommentList.14")); //$NON-NLS-1$
		}
		

		// load a basetime file
		// the basetime file must be loaded at the end for setting startTime
		if(baseTimeFileCandidates.size() > 0){
			String filename = baseTimeFileCandidates.get(0).getCanonicalPath();
			if(baseTimeFileCandidates.size() != 1){
				String[] timeCandidates = new String[baseTimeFileCandidates.size()];
				int i = 0;
				for(File timeFile : baseTimeFileCandidates) {
					timeCandidates[i++] = timeFile.getName().
							replaceFirst("^" + BASE_TIME_FILE_PREFIX + "_?", ""). //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							replaceFirst(FILE_SUFFIX + "$", ""); //$NON-NLS-1$ //$NON-NLS-2$
				}
				Object selectedValue = JOptionPane.showInputDialog(
						null, Messages.getString("CommentList.19"), Messages.getString("CommentList.20"), //$NON-NLS-1$ //$NON-NLS-2$
						JOptionPane.PLAIN_MESSAGE, null, timeCandidates, timeCandidates[0]);
				if (selectedValue != null) {
					i = 0;
					for(String timeCandidate : timeCandidates) {
						if(timeCandidate.equals((String)selectedValue)) {
							filename = baseTimeFileCandidates.get(i).getCanonicalPath();
						}
						i++;
					}
				} else {
					throw new IllegalStateException(Messages.getString("CommentList.21")); //$NON-NLS-1$
				}

				System.err.println("Warning(CommentList): " + filename + "will be used, although more than 2 basetime files were found."); //$NON-NLS-1$ //$NON-NLS-2$
			}
			results.add(0, new File(filename).getName());
			load(filename, commentTypes, discussers, true);
			syncByStartTime();
			sortByTime();
			refreshID();

		}

		mediaFilename = candMediafilename;
		mediaFilenameOriginal = SoundPlayer.isStream(mediaFilename) ? mediaFilename : new File(mediaFilename).getName();
		results.add(0, mediaFilename);
		
		return results;
	}
	
	
	public boolean isDuplicated(){
		HashSet<String> set = new HashSet<String>();
		
		for(Comment comment : this){
			String key = comment.catCommentInfo();
			if(set.contains(key)){
				return true;
			}
			set.add(key);
		}
		
		return false;
	}
	
	
	public ArrayList<String> mergeComments(){
		HashMap<String, Comment> keyMap = new HashMap<String, Comment>();
		ArrayList<String> deletedKeys = new ArrayList<String>();
		
		Iterator<Comment> it = iterator();
		
		while(it.hasNext()){
			Comment comment = it.next();
			
			String key = comment.catCommentInfo();
			if(keyMap.containsKey(key)){
				Comment storedComment = keyMap.get(key);
				
				// merge
				storedComment.setCommentBody(storedComment.mergeText(storedComment.getCommentBody(), comment.getCommentBody()));
				storedComment.setAux(storedComment.mergeText(storedComment.getAux(), comment.getAux()));

				it.remove();
				deletedKeys.add(key);
			} else {
				keyMap.put(key, comment);
			}
		}
		
		refreshID();
		return deletedKeys;
	}

	
	public boolean syncByStartTime(){
		boolean flagSyncCondition = true;
		long startTimeLong = startTime.getTime();
		
		for(Comment comment : this){
			long commentTime = comment.getDate();
			long elasedTime = commentTime - startTimeLong;
			if(elasedTime < 0){
				System.err.println("Warning(CommentList): this comment was made before recording the video:" + dateFormat.format(commentTime)); //$NON-NLS-1$
				flagSyncCondition = false;
			} else if(elasedTime > maxElapsedTime){ // 2 hours
				System.err.println("Warning(CommentList): The elapsed time of this comment exceeds 2hours:" + dateFormat.format(commentTime)); //$NON-NLS-1$
				flagSyncCondition = false;
			}
			comment.setCommentTimeBegin((int)elasedTime);
		}
		
		return flagSyncCondition;
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
				sb.append(value + "\t"); //$NON-NLS-1$
			}
			pw.println(sb.toString().replaceFirst("\t$", "")); //$NON-NLS-1$ //$NON-NLS-2$
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

		return String.format("%02d:%02d:%02d", hour, minute, sec); //$NON-NLS-1$
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
			return ""; //$NON-NLS-1$
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
		System.err.println("sn:" + setName); //$NON-NLS-1$
		if(setName.isEmpty()){
			SimpleDateFormat today = new SimpleDateFormat("yyyyMMdd"); //$NON-NLS-1$
			setName = today.format(new Date());
		} else {
			if(setName.endsWith(CommentList.FILE_SUFFIX)){
				// 末尾の .xml を削除
				setName = setName.replaceFirst("\\.xml$", ""); //$NON-NLS-1$ //$NON-NLS-2$
			}
			if(SoundPlayer.isPlayableFile(setName)){
				// 末尾の拡張子を削除
				setName = setName.replaceFirst("\\.[^\\.]+$", ""); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		if(!setName.endsWith("_" + commenter.getUserName())){ //$NON-NLS-1$
			// 末尾にユーザ名がついていなければ，追加
			setName += "_" + commenter.getUserName(); //$NON-NLS-1$
		}
		// ファイル名冒頭のシステム名を削除
		setName = setName.replaceFirst("^" + FishWatchr.SYSTEM_NAME.toLowerCase(), ""); //$NON-NLS-1$ //$NON-NLS-2$
		
		return;
	}
	
	
	public String getSetName(){
		return setName;
	}
	
	
	public Set<String> getSetNames(){
		return mapStartTime.keySet();
	}
	
	
	public User getAnnotator() {
		return annotator;
	}
	
	
	public void setAnnotator(User annotator) {
		this.annotator = annotator;
	}


	public int getSetSize(){
		return mapStartTime.size();
	}
		

	public void setMediaFilename(String filename){
		if(filename.matches("^https?://.+")){ //$NON-NLS-1$
			mediaFilename = filename;
			mediaFilenameOriginal = mediaFilename;
		} else {
			mediaFilename = new File(filename).getName();
			mediaFilenameOriginal = mediaFilename;
		}
	}

	
	public String getMediaFilename(){
		return mediaFilename;
	}
		
	
	// すでにファイル yyy.xml が存在する場合は，yyy.001.xml を返す
	static public String getUniqueFilename(String filename){
		String nameBody;
		String suffix;
		int p = filename.lastIndexOf("."); //$NON-NLS-1$
		int c = 1;
		
		if(p != -1){
			nameBody = filename.substring(0, p);
			suffix = filename.substring(p, filename.length());
		} else {
			nameBody = filename;
			suffix = ""; //$NON-NLS-1$
		}

		if(new File(filename).exists()){
			while(true){
				String newFilename = nameBody + String.format(".%03d", c++) + suffix; //$NON-NLS-1$
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
	    	if(a.getUserName().isEmpty()) return 1;
	    	if(b.getUserName().isEmpty()) return -1;
	    	return a.getUserName().compareTo(b.getUserName());
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
