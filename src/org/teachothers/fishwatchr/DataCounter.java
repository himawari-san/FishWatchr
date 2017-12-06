/*
    Copyright (C) 2014-2017 Masaya YAMAGUCHI

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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

public class DataCounter {
	public final static String SUMMARY_MODE_ALL = "全体";
	public final static String SUMMARY_MODE_SELF = "本人";
	public final static String SUMMARY_MODE_ALL_COMPARE = "全体(比較)";
	public final static String SUMMARY_MODE_SELF_COMPARE = "本人(比較)";
	
	private ArrayList<Comment> comments;
	private int[] iSelected;
	private String username;

	public DataCounter(ArrayList<Comment> comments, int[] iSelected, String username) {
		this.comments = comments;
		this.iSelected = iSelected;
		this.username = username;
	}

	
	public void setUsername(String username){
		this.username = username;
	}

	
	public ArrayList<Object[]> getSummary(){
		HashMap<String, Object[]> map = new HashMap<String, Object[]>();
		
		StringBuffer key = new StringBuffer();
		for(Comment comment : comments){
			for(int i : iSelected){
				key.append(comment.getAt(i));
				key.append("\t");
			}
			
			String keyStr = key.toString();
			if(!map.containsKey(keyStr)){
				Object[] data = new Object[iSelected.length+1];
				for(int i = 0; i < iSelected.length; i++){
					data[i] = comment.getAt(iSelected[i]);
				}
				data[iSelected.length] = 1;
				map.put(keyStr, data);
			} else {
				Object[] data = map.get(keyStr);
				data[iSelected.length] = (int)data[iSelected.length] + 1;
			}
			key.setLength(0);
		}
		
		return new ArrayList<Object[]>(map.values());
	}


	public ArrayList<Object[]> getSummary(String mode){
		HashMap<String, Object[]> map = new HashMap<String, Object[]>();
		HashSet<String> nameSet = new HashSet<String>();
		
		StringBuffer key = new StringBuffer();
		for(Comment comment : comments){
			
			if(mode == SUMMARY_MODE_SELF){
				if(!comment.getCommenter().getName().equals(username)){
					continue;
				}
			} else if(mode == SUMMARY_MODE_ALL){
				Comment newComment = new Comment();
				newComment.set(
						comment.getContentBody(),
						comment.getCommentType(),
						new User(SUMMARY_MODE_ALL),
						comment.getDiscusser(),
						new Date(comment.getDate()),
						comment.getCommentTime(),
						comment.getSetName());
				comment = newComment;
			} else if(mode == SUMMARY_MODE_SELF_COMPARE){
				Comment newComment = new Comment();
				User newUser;
				if(comment.getCommenter().getName().equals(username)){
					newUser = comment.getCommenter();
				} else {
					newUser = new User("他人");
					nameSet.add(comment.getCommenter().getName());
				}
					
				newComment.set(
						comment.getContentBody(),
						comment.getCommentType(),
						newUser,
						comment.getDiscusser(),
						new Date(comment.getDate()),
						comment.getCommentTime(),
						comment.getSetName());
				comment = newComment;
			} else if(mode == SUMMARY_MODE_ALL_COMPARE){
			}
			
			for(int i : iSelected){
				key.append(comment.getAt(i));
				key.append("\t");
			}

			String keyStr = key.toString();
			if(!map.containsKey(keyStr)){
				Object[] data = new Object[iSelected.length+1];
				for(int i = 0; i < iSelected.length; i++){
					data[i] = comment.getAt(iSelected[i]);
				}
				data[iSelected.length] = (double)1.0;
				map.put(keyStr, data);
			} else {
				Object[] data = map.get(keyStr);
				data[iSelected.length] = (double)data[iSelected.length] + 1;
			}
			key.setLength(0);
		}

		if(mode == SUMMARY_MODE_SELF_COMPARE){
			for(String keyStr : map.keySet()){
				if(keyStr.startsWith("他人")){
					Object[] data = map.get(keyStr);
					data[iSelected.length] = Double.parseDouble(data[iSelected.length].toString()) / (double)nameSet.size();
				}
			}
		}

		return new ArrayList<Object[]>(map.values());
	}
}
