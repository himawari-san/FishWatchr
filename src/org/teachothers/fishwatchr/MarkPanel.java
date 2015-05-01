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

import java.awt.Graphics;

import javax.swing.JPanel;


public class MarkPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private CommentList commentList;
	private SoundPlayer soundPlayer;
	private String userName;
	
	public MarkPanel(CommentList commentList, String userName, SoundPlayer soundPlayer){
		super();
		this.commentList = commentList;
		this.userName = userName;
		this.soundPlayer = soundPlayer;
//		setPreferredSize(new Dimension(1024,20));
	}

	
	public void setUserName(String userName){
		this.userName = userName;
	}
			
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		int pwidth = getParent().getSize().width;
		int maxX = getSize().width;
		int d = pwidth - maxX;
		int y0 = getSize().height / 2;
		int p = commentList.size();
		int bp = soundPlayer.getCurrentFrame();
		double frameLength = soundPlayer.getFrameLength() * 1000;

		while (p > 0) {
			Comment comment = commentList.get(--p);
			User discusser = comment.getDiscusser();
			CommentType commentType = comment.getCommentType();
			if (discusser == null || !userName.equals(comment.getDiscusser().getName())) {
				continue;
			}
			int frame = (int)(commentList.unifiedCommentTime(comment) / frameLength);
			if(commentType != null){
				g.setColor(comment.getCommentType().getColor());
			}

			g.fillOval((frame - (bp - (pwidth/2/SoundPanel.BAR_WIDTH))) * SoundPanel.BAR_WIDTH - d, y0 - 4, 8, 8);
		}
	}
}
