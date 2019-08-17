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

import java.util.Date;


public interface MediaPlayer {
	public final static int STATUS_STOP = 0;
	public final static int STATUS_RECORD = 1;
	public final static int STATUS_PAUSE = 2;
	public final static int STATUS_RESUME = 3;
	public final static int STATUS_INITALIZED = 0;
	public final static int STATUS_PLAY = 4;
			
	public void init();
	public void setFile(String targetFilename);
	public float getSoundLength();
	public long getCurrentRecordingPosition();
	public int getStatus();
	public void myPlay();
	public void myStop();
	public void myPause();
	public void myResume();
	public void myForward(int sec);
	public SoundGraphBuffer getSoundGraphBuffer();
	public Date getStartTime();
	public String getTargetFilename();
	public void setTargetFilename(String targetFilename);
	public void forward(int sec);
	public void backward(int sec);
	public void setFrame(int frame);
	public double getFrameLength();
}
