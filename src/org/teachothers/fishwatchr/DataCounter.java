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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

public class DataCounter {
	public final static String SUMMARY_MODE_ALL = Messages.getString("DataCounter.0"); //$NON-NLS-1$
	public final static String SUMMARY_MODE_SELF = Messages.getString("DataCounter.1"); //$NON-NLS-1$
	public final static String SUMMARY_MODE_ALL_COMPARE = Messages.getString("DataCounter.2"); //$NON-NLS-1$
	public final static String SUMMARY_MODE_SELF_COMPARE = Messages.getString("DataCounter.3"); //$NON-NLS-1$
	
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
				key.append("\t"); //$NON-NLS-1$
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
		HashSet<String> annotatorNameSet = new HashSet<String>();
		
		StringBuffer key = new StringBuffer();
		for(Comment comment : comments){
			
			if(mode == SUMMARY_MODE_SELF){
				if(!comment.getCommenter().getUserName().equals(username)){
					continue;
				}
			} else if(mode == SUMMARY_MODE_ALL){
				Comment newComment = new Comment();
				newComment.set(
						comment.getCommentBody(),
						comment.getCommentType(),
						new User(SUMMARY_MODE_ALL),
						comment.getDiscusser(),
						new Date(comment.getDate()),
						comment.getCommentTime(),
						comment.getSetName(),
						comment.getAux());
				comment = newComment;
			} else if(mode == SUMMARY_MODE_SELF_COMPARE){
				Comment newComment = new Comment();
				User newUser;
				if(comment.getCommenter().getUserName().equals(username)){
					newUser = comment.getCommenter();
				} else {
					newUser = new User(Messages.getString("DataCounter.5")); //$NON-NLS-1$
					annotatorNameSet.add(comment.getCommenter().getUserName());
				}
					
				newComment.set(
						comment.getCommentBody(),
						comment.getCommentType(),
						newUser,
						comment.getDiscusser(),
						new Date(comment.getDate()),
						comment.getCommentTime(),
						comment.getSetName(),
						comment.getAux());
				comment = newComment;
			} else if(mode == SUMMARY_MODE_ALL_COMPARE){
			}
			
			for(int i : iSelected){
				key.append(comment.getAt(i));
				key.append("\t"); //$NON-NLS-1$
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
				if(keyStr.startsWith(Messages.getString("DataCounter.5"))){ //$NON-NLS-1$
					Object[] data = map.get(keyStr);
					data[iSelected.length] = Double.parseDouble(data[iSelected.length].toString()) / (double)annotatorNameSet.size();
				}
			}
		}

		return new ArrayList<Object[]>(map.values());
	}
}
