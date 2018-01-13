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

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;


public class Comment {
	public static final int N_Field = 10;
	public static final int F_ID = 0; // コメントid
	public static final int F_COMMENT_TIME = 1; // 開始からの経過時間(msec)
	public static final int F_COMMENTER = 2;
	public static final int F_DISCUSSER = 3;
	public static final int F_COMMENT_TYPE = 4;
	public static final int F_COMMENT = 6;
	public static final int F_DATE = 8; // コメントした日時
	public static final int F_SET_NAME = 5; // コメントセット名
	public static final int F_AUX = 7; // コメント補助情報
	public static final int F_COMMENT_TIME_END = 9; // 範囲型のコメントの終了時間
//	public static final int F_COMMENT_TIME_OFFSET = 9; // 経過時間に対するオフセット
	
	public static final String ITEM_NUMBER = "番号";
	public static final String ITEM_ANNOTATOR = "注釈者";
	public static final String ITEM_TIME = "時間";
	public static final String ITEM_TARGET = "観察対象";
	public static final String ITEM_LABEL = "ラベル";
	public static final String ITEM_SET = "セット";
	public static final String ITEM_COMMENT = "コメント";
	public static final String ITEM_AUX = "補助情報";

	public static final String headers[] = {ITEM_NUMBER, ITEM_TIME, ITEM_ANNOTATOR, ITEM_TARGET, ITEM_LABEL, ITEM_SET, ITEM_COMMENT, ITEM_AUX};
	public static final int COMMENT_TIME_END_UNDEFINED = -1; // 範囲型でない場合，終了時間は-1とする

	public static final String COMMENT_DELIMITER = " || ";
	public static final String LINEBREAK = " // ";
	
	private static String defaultDiscusserName = "不特定";

	private static int currentID = 1;
	private Object[] data = new Object[N_Field];
	private boolean isModified = false;
	private int id;
	private static HashMap<String, Integer> headerMap = new HashMap<String, Integer>();
	private boolean isExcluded = false;
	private boolean isFocused = false;
	
	
	public Comment(){
		data = new Object[N_Field];
		id = currentID++;
	}

	static {
		for(int i = 0; i < headers.length; i++){
			headerMap.put(headers[i], i);
		}
	}

	public static void resetID(){
		currentID = 1;
	}
	
	
	public static void setDefaultDiscusserName(String name){
		defaultDiscusserName = name;
	}
	
	
	public void set(String contentBody, CommentType commentType, User commenter, User discusser,
			Date commentDate, int commentTime, String setName, String aux){
		// for testing
		if(discusser.getName().isEmpty()){
			discusser.setName(defaultDiscusserName);
		}
		data[F_COMMENTER] = commenter;
		data[F_COMMENT] = contentBody;
		data[F_COMMENT_TYPE] = commentType;
		data[F_DISCUSSER] = discusser;
		data[F_DATE] = commentDate;
		data[F_COMMENT_TIME] = commentTime;
		data[F_SET_NAME] = setName;
		data[F_COMMENT_TIME_END] = COMMENT_TIME_END_UNDEFINED;
		data[F_ID] = id;
		data[F_AUX] = aux;
//		data[F_COMMENT_TIME_OFFSET] = offset;
	}
	
	
	public void setAt(int i, Object obj){
		// 同じ値なら変更しない
		if(data[i].equals(obj)){
			return;
		} else if(data[i] != null && obj != null && data[i].toString().equals(obj.toString())){
			return;
		}
		data[i] = obj;
		isModified = true;
	}
	
	public void setCommentTimeBegin(int msec){
		data[F_COMMENT_TIME] = msec;
		isModified = true;
	}

	public void setCommentTimeEnd(int msec){
		data[F_COMMENT_TIME_END] = msec;
		isModified = true;
	}
	
	
	public void setID(int i){
		data[F_ID] = i;
	}
	
//	public void setCommentTimeOffset(int msec){
//		data[F_COMMENT_TIME_OFFSET] = msec;
//		isModified = true;
//	}
	
	public boolean isModified(){
		return isModified;
	}

	public void setModified(boolean flag){
		isModified = flag;
	}

	
	
	public int getID(){
		return (int)data[F_ID];
	}
	
	public String getCommentBody(){
		return (String)data[F_COMMENT];
	}
	
	public void setCommentBody(String body){
		data[F_COMMENT] = body;
	}
	
	public User getDiscusser(){
		return (User)data[F_DISCUSSER];
	}

	public User getCommenter(){
		return (User)data[F_COMMENTER];
	}
	
	// コメントした日時（非補正）
	public long getDate(){
		return ((Date)data[F_DATE]).getTime();
	}

	
	public int getCommentTime(){
		return ((int)data[F_COMMENT_TIME]);
	}

	public int getCommentTimeEnd(){
		return ((int)data[F_COMMENT_TIME_END]);
	}
	
	public CommentType getCommentType(){
		return (CommentType)data[F_COMMENT_TYPE];
	}
	
	public String getSetName(){
		return (String)data[F_SET_NAME];
	}
	
	public String getAux(){
		return (String)data[F_AUX];
	}

	public void setAux(String aux){
		data[F_AUX] = aux;
	}

	public Object getAt(int i){
		return data[i];
	}
	
	
	public Object getValueByHeaderName(String headerName){
		if(!headerMap.containsKey(headerName)){
			return null;
		}
		
		return data[headerMap.get(headerName)];
	}


	public boolean isExcluded(){
		return isExcluded;
	}
	
	public boolean setExcluded(boolean flag){
		isExcluded = flag;
		return isExcluded;
	}

	public boolean isFocused(){
		return isFocused;
	}

	public boolean setFocused(boolean flag){
		isFocused = flag;
		return isFocused;
	}
	
	
	public String catCommentInfo(){
		StringBuffer str = new StringBuffer();
		
		str.append(getCommenter());
		str.append("\t");
		str.append(getDate());
		str.append("\t");
		str.append(getDiscusser());
		str.append("\t");
		str.append(getCommentType());
		str.append("\t");
		str.append(getSetName());
		
		return str.toString();
	}

	
	public boolean mergeContents(Comment comment){

		String commentBody = getCommentBody();
		String targetCommentBody = comment.getCommentBody();
		String commentAux = getAux();
		String targetCommentAux = comment.getAux();
		
		if(!catCommentInfo().equals(comment.catCommentInfo())){
			return false;
		}
		
		if(!commentBody.contains(COMMENT_DELIMITER)
				|| !Arrays.asList(commentBody.split(COMMENT_DELIMITER)).contains(targetCommentBody)){
			setCommentBody(Util.catStrings(commentBody, targetCommentBody, COMMENT_DELIMITER));
		}

		if(!commentAux.contains(COMMENT_DELIMITER)
				|| !Arrays.asList(commentAux.split(COMMENT_DELIMITER)).contains(targetCommentAux)){
			setAux(Util.catStrings(commentAux, targetCommentAux, COMMENT_DELIMITER));
		}

		return true;
	}
}
