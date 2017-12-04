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
import java.util.HashMap;

public class DataCounter {
	private ArrayList<Comment> comments;
	private int[] iSelected;

	public DataCounter(ArrayList<Comment> comments, int[] iSelected) {
		this.comments = comments;
		this.iSelected = iSelected;
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
//					System.err.println("i:" + data[i]);
				}
				data[iSelected.length] = 1;
				map.put(keyStr, data);
//				System.err.println("k:" + key);
			} else {
				Object[] data = map.get(keyStr);
				data[iSelected.length] = (int)data[iSelected.length] + 1;
			}
			key.setLength(0);
		}
		
		return new ArrayList<Object[]>(map.values());
	}

}
