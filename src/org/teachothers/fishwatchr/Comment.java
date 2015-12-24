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

import java.util.Date;
import java.util.HashMap;

public class Comment {
	public static final int N_Field = 9;
	public static final int F_ID = 0; // コメントid
	public static final int F_COMMENT_TIME = 1; // 開始からの経過時間(msec)
	public static final int F_COMMENTER = 2;
	public static final int F_DISCUSSER = 3;
	public static final int F_COMMENT_TYPE = 4;
	public static final int F_COMMENT = 6;
	public static final int F_DATE = 7; // コメントした日時
	public static final int F_SET_NAME = 5; // コメントセット名
	public static final int F_COMMENT_TIME_END = 8; // 範囲型のコメントの終了時間
//	public static final int F_COMMENT_TIME_OFFSET = 9; // 経過時間に対するオフセット

	public static final String headers[] = {"番号", "時間", "注釈者", "話者", "ラベル", "セット", "コメント"};
	public static final int COMMENT_TIME_END_UNDEFINED = -1; // 範囲型でない場合，終了時間は-1とする
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
			Date commentDate, int commentTime, String setName){
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
	
	public String getContentBody(){
		return (String)data[F_COMMENT];
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
	
}
