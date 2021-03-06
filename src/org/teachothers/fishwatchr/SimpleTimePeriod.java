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

public class SimpleTimePeriod {
	int startTime;
	int endTime;
	
	public SimpleTimePeriod(int startTime, int endTime) {
		this.startTime = startTime;
		this.endTime = endTime;
	}
	
	public boolean includes(int time){
		if(time >= startTime && time <= endTime){
			return true;
		} else {
			return false;
		}
	}
}
