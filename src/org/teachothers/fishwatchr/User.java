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

public class User {
	private String userName;
	private String groupName = "";
	
	
	public User(String name){
		this.userName = name;
	}
	
	public String getUserName(){
		return userName;
	}
	
	public void setUserName(String name){
		this.userName = name;
	}

	
	public void setGroupName(String name) {
		groupName = name;
	}

	public String getGroupName() {
		return groupName;
	}
	
	public String toString(){
		return userName;
	}
}
