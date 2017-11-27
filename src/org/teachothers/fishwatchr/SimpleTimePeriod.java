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
