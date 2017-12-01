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

import java.awt.Color;

public class CommentType {
	private String type;
	private Color color;
	
	public CommentType(String type, Color color){
		this.type = type;
		this.color = color;
	}

	
	public String getType(){
		return type;
	}

	
	public Color getColor(){
		return color;
	}
	
	
	public void set(String type, Color color){
		this.type = type;
		this.color = color;
	}

	
	public String toString(){
		return type;
	}
}
