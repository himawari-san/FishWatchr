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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;


public class CommentTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	private CommentList commentList;
	private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss"); //$NON-NLS-1$
	public ArrayList<User> discussers;
	public ArrayList<CommentType> commentTypes;
	private ArrayList<Comment> filteredCommentList = new ArrayList<Comment>();
	private HashMap<String, Pattern> filters = new HashMap<String, Pattern>();
	private SimpleTimePeriod selectedPeriod = null;
	
	public static final String ITEM_NUMBER = Messages.getString("Comment.0"); //$NON-NLS-1$
	public static final String ITEM_ANNOTATOR = Messages.getString("Comment.1"); //$NON-NLS-1$
	public static final String ITEM_TIME = Messages.getString("Comment.2"); //$NON-NLS-1$
	public static final String ITEM_TARGET = Messages.getString("Comment.3"); //$NON-NLS-1$
	public static final String ITEM_LABEL = Messages.getString("Comment.4"); //$NON-NLS-1$
	public static final String ITEM_SET = Messages.getString("Comment.5"); //$NON-NLS-1$
	public static final String ITEM_COMMENT = Messages.getString("Comment.6"); //$NON-NLS-1$
	public static final String ITEM_AUX = Messages.getString("Comment.7"); //$NON-NLS-1$

	private String columnNames[] = {ITEM_NUMBER, ITEM_TIME, ITEM_ANNOTATOR, ITEM_TARGET, ITEM_LABEL, ITEM_SET, ITEM_COMMENT, ITEM_AUX};
	private boolean readonlyFlags[] = {true, true, false, false, false, false, false, false, false};
	private String constraints[] = {"\\d+", null, null, null, null, null, null, null}; //$NON-NLS-1$
	private HashMap<String, Integer> columnNameMap = new HashMap<String, Integer>();

	
	public CommentTableModel(CommentList commentList, ArrayList<User> discussers, ArrayList<CommentType> commentTypes){
		this.commentList = commentList;
		this.discussers = discussers;
		this.commentTypes = commentTypes;
		refreshFilter();

		// initialize columnNameMap
		for(int i = 0; i < columnNames.length; i++){
			columnNameMap.put(columnNames[i], i);
		}
	}
	
	
	public String getColumnName(int i){
		return columnNames[i];
	}
	
	
	public int getColumnCount() {
		return columnNames.length;
	}

	
	public int getRowCount() {
		return filteredCommentList.size();
	}

	
    public Class<? extends Object> getColumnClass(int column) {
    	Object obj = getValueAt(0, column);
    	if(obj != null){
    		return getValueAt(0, column).getClass();
    	} else {
    		return null;
    	}
    }
	
	public Object getValueAt(int row, int column) {
		if(filteredCommentList.size() <= row) {
			return "";  //$NON-NLS-1$
		}
		Comment comment = filteredCommentList.get(row);
		
		if(comment == null) return null;
		if(column == Comment.F_ANNOTATOR || column == Comment.F_COMMENT_TARGET){
			return comment.getAt(column);
		} else if(column == Comment.F_COMMENT_TIME){
			return formatTime(commentList.unifiedCommentTime(comment));
		} else if(column == Comment.F_DATE){
			return timeFormat.format((Date)comment.getAt(column));
//		} else if(column == Comment.F_COMMENT_TYPE){
//			return comment.getCommentType().getType();
		} else {
			return comment.getAt(column);
		}
	}
	
	// time (millisec)
	private String formatTime(int msec){
		int time = (int) (msec / 1000);
		int hour = time / 3600;
		time -= hour * 3600;
		int minute = time / 60;
		int sec = time - minute * 60;

		return String.format("%02d:%02d:%02d", hour, minute, sec); //$NON-NLS-1$
	}
	
	
	
	public void setValueAt(Object obj, int row, int column){
		Comment comment = filteredCommentList.get(row);
		comment.setAt(column, obj);
	}
	
	
	public boolean isCellEditable(int row, int column){
		return !readonlyFlags[column];
	}
	
	
	public Comment getCommentAt(int row){
		return filteredCommentList.get(row);
	}

	
	public ArrayList<Comment> getFilteredCommentList(){
		return filteredCommentList;
	}

	
	public ArrayList<String> getItemList(String headerName){
		ArrayList<String> itemList = new ArrayList<String>();
		for(Comment comment: filteredCommentList){
			String item = comment.getAt(columnNameMap.get(headerName)).toString();
			if(!itemList.contains(item)){
				itemList.add(item);
			}
		}
		
		return itemList;
	}

	
	public Comment addComment(String contentBody, CommentType commentType, User commenter, User discusser,
			Date commentDate, int commentTime, String aux){
		Comment comment = new Comment();
		String setName = commentList.getSetName();
		
		int originalTime = commentTime - commentList.getCommentTimeOffset(setName);
		comment.set("", commentType, commenter, discusser, commentDate, originalTime, setName, aux); //$NON-NLS-1$

		if(commentList.size() == 0) {
			commentList.add(comment);
		} else {
			Comment lastComment = commentList.get(commentList.size()-1);
			int frame = commentList.unifiedCommentTime(comment);
			if (commentList.unifiedCommentTime(lastComment) <= frame) {
				commentList.add(comment);
			} else {
				boolean fragInsert = false;
				for (int i = 0; i < commentList.size(); i++) {
					if (commentList.unifiedCommentTime(commentList.get(i)) >= frame) {
						commentList.add(i, comment);
						fragInsert = true;
						break;
					}
				}
				if (!fragInsert) {
					commentList.add(comment);
				}
			}
		}
		
		// なぜか２回実行しないと，CommentTable の scrollRectToVisible が正常に動作しない
//		fireTableRowsInserted(getRowCount()-1, getRowCount()-1);
//		fireTableRowsInserted(getRowCount()-1, getRowCount()-1);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				refreshFilter();
				fireTableDataChanged();
			}});

		return comment;
	}

	
	void addFilter(String header, String value){
		Pattern p = Pattern.compile(value);
		filters.put(header, p);
	}
	
	
	void removeFilter(String header){
		if(filters.remove(header) != null){
			refreshFilter();
		}
	}
	
	
	boolean isFiltered(){
		if(filters.isEmpty()){
			return false;
		} else {
			return true;
		}
	}
	
	
	void clearFilter(){
		filters.clear();
	}
	
	
	void refreshFilter(){
		filteredCommentList.clear();
		
		for(Comment comment: commentList){
			boolean flag = true;
			for(Map.Entry<String, Pattern> filter : filters.entrySet()){
				String headerName = filter.getKey();
				String value = comment.getAt(columnNameMap.get(headerName)).toString();
				Pattern filterCond = filter.getValue();
				if(value != null && !filterCond.matcher(value).find()){
					flag = false;
					break;
				} else if(value == null && !filterCond.matcher("").find()){ //$NON-NLS-1$
					flag = false;
					break;
				}
			}
			
			if(flag){
				filteredCommentList.add(comment);
				comment.setExcluded(false);
			} else {
				comment.setExcluded(true);
			}
		}

		if(selectedPeriod != null){
			selectTimePeriod(selectedPeriod);
		}
		
		fireTableDataChanged();
	}
	
	
	void deleteCommentAt(int row){
		Comment comment = getCommentAt(row);
		int p = findComment(comment);
		commentList.remove(p);
		refreshFilter();
	}

	
	public void selectTimePeriod(SimpleTimePeriod period){
		ArrayList<Comment> tempCommentList = new ArrayList<Comment>();
		String timeFieldName = getColumnName(Comment.F_COMMENT_TIME);
		this.selectedPeriod = period;
		
		for(Comment comment: filteredCommentList){
			int commentTime = (int)comment.getAt(columnNameMap.get(timeFieldName));
			if(period == null || period.includes(commentTime)){
				tempCommentList.add(comment);
			}
		}
		filteredCommentList.clear();
		filteredCommentList.addAll(tempCommentList);
		
		fireTableDataChanged();
	}
	
	
	public CommentList getCommentList(){
		return commentList;
	}
	
	
	// search comment from all data
	public int findComment(Comment comment){
		for(int i = 0; i < commentList.size(); i++){
			if(commentList.get(i).equals(comment)){
				return i;
			}
		}
		return -1;
	}

	
	public int findFilteredComment(Comment comment){
		for(int i = 0; i < filteredCommentList.size(); i++){
			if(filteredCommentList.get(i).equals(comment)){
				return i;
			}
		}
		return -1;
	}
	
	
	public void setColumnNames(String[] newColumnNames){
		columnNames = newColumnNames;

		// update columnNameMap
		for(int i = 0; i < columnNames.length; i++){
			columnNameMap.put(columnNames[i], i);
		}
	}

	
	public void setColumnReadOnlyFlags(boolean[] flags){
		readonlyFlags = flags;
	}
	
	
	public void setColumnConstraints(String[] constraints){
		this.constraints = constraints;
	}
	
	
	public HashMap<Integer, Integer> validateAnnotations() {
		
		HashMap<Integer, Integer> invalidCoumnSummary = new HashMap<Integer, Integer>();

		if(constraints != null) {
			for(Comment comment : commentList) {
				int[] invalidColumns = comment.validate(constraints);
				if(invalidColumns == null) {
					continue;
				} else {
					for(int i : invalidColumns) {
						if(invalidCoumnSummary.containsKey(i)) {
							invalidCoumnSummary.put(i, invalidCoumnSummary.get(i) + 1);
						} else {
							invalidCoumnSummary.put(i,  1);
						}
					}
				}
			}
		}

		return invalidCoumnSummary;
	}
}
