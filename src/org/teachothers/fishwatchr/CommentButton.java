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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JOptionPane;

public class CommentButton extends JButton implements ActionListener {
	private static final long serialVersionUID = 1L;
	// 話者優先
	public static final int BUTTON_TYPE_DISCUSSER = 0;
	// コメント優先
	public static final int BUTTON_TYPE_COMMENT = 1;
	
	private CommentTableModel ctm;
	private SoundPlayer soundPlayer;
	private User commenter;
	private ArrayList<User> discussers;
	private ArrayList<CommentType> commentTypes;
	private User discusser = null;
	private CommentType commentType = null;
	private int buttonType = BUTTON_TYPE_DISCUSSER;
	private boolean isMultiAnnotation = false;
//	private String setName = "";
	
	// コメント優先
	public CommentButton(CommentTableModel ctm, SoundPlayer soundPlayer, boolean isMultiAnnotation, CommentType commentType, ArrayList<User> discussers, User commenter) {
		super(commentType.getType());
		this.ctm = ctm;
		this.commenter = commenter;
		this.commentType = commentType;
		this.soundPlayer = soundPlayer;
		this.discussers = discussers;
		this.isMultiAnnotation = isMultiAnnotation;

		buttonType = BUTTON_TYPE_COMMENT;
		addActionListener(this);
	}

	// 話者優先
	public CommentButton(CommentTableModel ctm, SoundPlayer soundPlayer, boolean isMultiAnnotation, User discusser, ArrayList<CommentType> commentTypes, User commenter) {
		super(discusser.getName());
		this.ctm = ctm;
		this.commenter = commenter;
		this.commentTypes = commentTypes;
		this.soundPlayer = soundPlayer;
		this.discussers = new ArrayList<User>();
		this.discusser = discusser;
		this.isMultiAnnotation = isMultiAnnotation;
		
		buttonType = BUTTON_TYPE_DISCUSSER;
		addActionListener(this);
	}


	public void setMultiAnnotation(boolean flag){
		isMultiAnnotation = flag;
	}
	

	public void actionPerformed(ActionEvent arg0) {
		Date now = new Date(); // 現在日時
		int currentTime = soundPlayer.getElapsedTime(); // 開始からの経過時間（msec）
		
		if(buttonType == BUTTON_TYPE_COMMENT){
			ArrayList<Object> items = new ArrayList<Object>();
			for(User d : discussers){
				if(!d.getName().isEmpty()){
					items.add(d);
				}
			}
			User selectedDiscusser = new User("");
			if (isMultiAnnotation) {
				int selectedValue = JOptionPane.showOptionDialog(this, "キャンセルする場合は，ESC",
						"対象者の選択", JOptionPane.YES_NO_CANCEL_OPTION,
						JOptionPane.PLAIN_MESSAGE, null, items.toArray(), items.get(0));
				if (selectedValue == JOptionPane.CLOSED_OPTION) {
					return;
				}
				selectedDiscusser = discussers.get(selectedValue);
			}
			ctm.addComment("", commentType, commenter, selectedDiscusser, now, currentTime);
		} else if(buttonType == BUTTON_TYPE_DISCUSSER){
			CommentType commentType = new CommentType("", Color.BLACK);
			if (isMultiAnnotation) {
				ArrayList<Object> items = new ArrayList<Object>();
				for(CommentType ct : commentTypes){
					if(!ct.getType().isEmpty()){
						items.add(ct);
					}
				}
				int selectedValue = JOptionPane.showOptionDialog(this, "キャンセルする場合は，ESC",
						"ラベルの選択", JOptionPane.YES_NO_CANCEL_OPTION,
						JOptionPane.PLAIN_MESSAGE, null, items.toArray(),
						items.get(0));
				if (selectedValue == JOptionPane.CLOSED_OPTION) {
					return;
				}
				commentType = commentTypes.get(selectedValue);
			}
			ctm.addComment("", commentType, commenter, discusser, now, currentTime);
		}
	}
}
