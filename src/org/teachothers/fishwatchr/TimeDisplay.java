/*
    Copyright (C) 2014-2016 Masaya YAMAGUCHI

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

import javax.swing.JLabel;

public class TimeDisplay extends JLabel {
	private static final long serialVersionUID = 1L;

	public void setTime(int time){
		int hour = time / 3600;
		time -= hour * 3600;
		int minute = time / 60;
		int sec = time - minute * 60;
		
		setText(String.format("%02d:%02d:%02d", hour, minute, sec));
	}
}
