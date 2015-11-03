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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;

public class DiscussersPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final String USER_NOT_UNDEFINED = "(未指定)";
	private static final int DISCUSSION_PANEL_MAX_HEIGHT = 35;
	private static final int USERNAME_LABEL_MAX_WIDTH = 80;
	
	private List<User> discussers;
	private int maxDiscussers;
	private JLabel[] userNameLabels;
	private MarkPanel[] markPanels;
	private CommentTableModel ctm;
	private SoundPlayer soundPlayer;
	
	public DiscussersPanel(List<User> discussers, int maxDiscussers, CommentTableModel ctm, SoundPlayer soundPlayer){
		this.discussers = discussers;
		this.maxDiscussers = maxDiscussers;
		this.ctm = ctm;
		this.soundPlayer = soundPlayer;
		markPanels = new MarkPanel[maxDiscussers];
		userNameLabels = new JLabel[maxDiscussers];
		ginit();
	}
	
	public void repaintComponents(){
		for (int i = 0; i < markPanels.length; i++) {
			markPanels[i].repaint();
		}
	}

	
	public void updateCompoments(){
		for (int i = 0; i < markPanels.length; i++) {
			if(i < discussers.size()){
				userNameLabels[i].setText(discussers.get(i).getName());
				markPanels[i].setUserName(discussers.get(i).getName());
			} else {
				userNameLabels[i].setText(USER_NOT_UNDEFINED);
				markPanels[i].setUserName("");
				
			}
		}
	}

	
	private void ginit(){
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		for (int i = 0; i < discussers.size() || i < maxDiscussers; i++) {
			JPanel discusserPanel = new JPanel();
			discusserPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, DISCUSSION_PANEL_MAX_HEIGHT));
			discusserPanel.setLayout(new BorderLayout());
			String discusserName = (i < discussers.size() && !discussers.get(i).getName().isEmpty())
					? discussers.get(i).getName() : USER_NOT_UNDEFINED;
			userNameLabels[i] = new JLabel(discusserName);
			// why min_value?
			userNameLabels[i].setPreferredSize(new Dimension(USERNAME_LABEL_MAX_WIDTH, Integer.MIN_VALUE));
			userNameLabels[i].setBorder(new EtchedBorder(BevelBorder.RAISED));
			userNameLabels[i].setHorizontalAlignment(JLabel.CENTER);
			discusserPanel.add(userNameLabels[i], BorderLayout.WEST);
			discusserPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
			markPanels[i] = new MarkPanel(ctm, discusserName, soundPlayer);
			discusserPanel.add(markPanels[i], BorderLayout.CENTER);
			add(discusserPanel);
		}
	}
}
