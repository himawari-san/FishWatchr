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

import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class DiscusserSettingPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	public static final String USER_NOT_DEFINED = "";
	public static final String USER_NOT_SPECIFIED = "(未指定)";

	private List<User> discussers;
	private String newDiscussers[];
	private int maxDiscussers;
	private JTextField discusserNames[];
	
	public DiscusserSettingPanel(List<User> discussers, int maxDiscussers){
		super();
		this.discussers = discussers;
		this.maxDiscussers = maxDiscussers;
		discusserNames = new JTextField[maxDiscussers];
		newDiscussers = new String[maxDiscussers];
		for(int i = 0; i < maxDiscussers; i++){
			newDiscussers[i] = i < discussers.size() ? discussers.get(i).getName() : USER_NOT_DEFINED;
		}
		ginit();
	}
	
	private void ginit(){
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		for(int i = 0; i < maxDiscussers; i++){
			discusserNames[i] = new JTextField();
			if(discussers.size() > i){
				discusserNames[i].setText(discussers.get(i).getName());
			} else {
				discusserNames[i].setText(USER_NOT_DEFINED);
			}			
			add(discusserNames[i]);
		}
	}
	
	public void updateNewValue(){
		ArrayList<Integer> deleteItems = new ArrayList<Integer>();
		
		for(int i = 0; i < discusserNames.length; i++){
			if(discusserNames[i].getText().equals(USER_NOT_DEFINED)){
				deleteItems.add(i);
			} else {
				if(i > discussers.size()){
					discussers.add(new User(discusserNames[i].getText()));
				} else {
					discussers.get(i).setName(discusserNames[i].getText());
				}
			} 
		}

		for(int i : deleteItems){
			discussers.remove(i);
		}
	}
}
