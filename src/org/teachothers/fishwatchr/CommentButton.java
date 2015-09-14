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
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class CommentButton extends JButton implements ActionListener {
	private static final long serialVersionUID = 1L;
	private static boolean isWorking = false;
	
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
	

	public void showMnemonic(){
		String label = getText();
		char mnemonic = (char) getMnemonic();
		if(mnemonic != 0 && mnemonic - '0' < 10){
			setText("<html><p style=\"text-align:center\">" + label + "<br />[" + Integer.toString(mnemonic -'0') + "]</div></html>");
//			setText("<html><p style=\"margin:0px; padding:0px; text-align:center\">" + label + "</p><p style=\"margin:0px; padding: 0px; text-align:center\">[" + Integer.toString(mnemonic -'0') + "]</p></html>");
		}
	}
	
	
	public void actionPerformed(ActionEvent arg0) {
		if(isWorking){
			return;
		} else {
			isWorking = true; // 同時に複数の処理が行われるのを防止
		}
		
		Date now = new Date(); // 現在日時
		int currentTime = soundPlayer.getElapsedTime(); // 開始からの経過時間（msec）
		
		if(buttonType == BUTTON_TYPE_COMMENT){
			User selectedDiscusser = new User("");
			if (isMultiAnnotation) {
				ButtonDialog dialog = new ButtonDialog("ラベルの選択(" + commentType.getType() + ")", discussers);
				dialog.setModal(true);
				dialog.setLocationRelativeTo(this);
				dialog.setVisible(true);
				int iSelectedValue = dialog.getSelectedValue();
				if (iSelectedValue == -1) {
					isWorking = false;
					return;
				}
				selectedDiscusser = discussers.get(iSelectedValue);
			}
			ctm.addComment("", commentType, commenter, selectedDiscusser, now, currentTime);
		} else if(buttonType == BUTTON_TYPE_DISCUSSER){
			CommentType commentType = new CommentType("", Color.BLACK);
			if (isMultiAnnotation) {
				ButtonDialog dialog = new ButtonDialog("ラベルの選択(" + discusser.getName() + ")", commentTypes);
				dialog.setModal(true);
				dialog.setLocationRelativeTo(this);
				dialog.setVisible(true);
				int iSelectedValue = dialog.getSelectedValue();
				if (iSelectedValue == -1) {
					isWorking = false;
					return;
				}
				commentType = commentTypes.get(iSelectedValue);
			}
			ctm.addComment("", commentType, commenter, discusser, now, currentTime);
		}
		isWorking = false;
	}


	class ButtonDialog extends JDialog {
		private static final long serialVersionUID = 1L;
		ArrayList<?> objects;
		int iSelectedItem = -1;
		
		public ButtonDialog(String title, ArrayList<?> objects){
			super();
			setTitle(title);
			this.objects = objects;
			ginit();
		}
		
		
		void ginit(){
			Container pane = getContentPane();
			JPanel messagePanel = new JPanel();
			JPanel buttonPanel = new JPanel();

			pane.setLayout(new BorderLayout());
			pane.add(messagePanel, BorderLayout.NORTH);
			pane.add(buttonPanel, BorderLayout.CENTER);
			buttonPanel.setLayout(new FlowLayout());
			messagePanel.setLayout(new BorderLayout());
			
			messagePanel.add(new JLabel(" キャンセルする場合は，ESC"), BorderLayout.WEST);
			
			
			int c = 0;
			for(Object object: objects){
				final String label = object.toString();
				if(label.isEmpty()){
					continue;
				}
				final MyJButton button = new MyJButton(c++, label);
				button.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						ButtonDialog.this.setVisible(false);
						iSelectedItem = button.getNumber();
					}
				});
				
				button.addKeyListener(new KeyListener() {
					@Override
					public void keyPressed(KeyEvent e) {
						if(e.getKeyCode() == KeyEvent.VK_ESCAPE){
							ButtonDialog.this.setVisible(false);
							iSelectedItem = -1;
						}
					}

					@Override
					public void keyTyped(KeyEvent e) {}

					@Override
					public void keyReleased(KeyEvent e) {}
				});

				buttonPanel.add(button);
			}
			pack();
		}

		public int getSelectedValue(){
			return iSelectedItem;
		}
	}

	
	class MyJButton extends JButton {
		private static final long serialVersionUID = 1L;
		int no = 0;
		
		public MyJButton(int no, String label){
			super("<html><p style=\"text-align:center\">" + label + "<br />[" + Integer.toString(no+1) + "]</div></html>");
			this.no = no;
			setMnemonic('0' + no + 1);
		}
		
		public int getNumber(){
			return no;
		}
		
	}
}
