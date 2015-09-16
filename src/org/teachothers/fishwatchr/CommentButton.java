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

import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

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
	AbstractAction actNormal = new AbstractAction() {
		private static final long serialVersionUID = 1L;
		@Override
		  public void actionPerformed(ActionEvent e) {
			  doClick();
		  }
	};
	AbstractAction actReverse = new AbstractAction() {
		private static final long serialVersionUID = 1L;
		  @Override
		  public void actionPerformed(ActionEvent e) {
			  isMultiAnnotation = isMultiAnnotation == true ? false : true; 
			  doClick();
			  isMultiAnnotation = isMultiAnnotation == true ? false : true; 
		  }
	};

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
	
	
	public void setActionKey(int c){
		String label = getText();
//		char mnemonic = (char) getMnemonic();
		if(c < 0 || c > 9){
			return;
		} else if(c == 9){
			c = 0;
		} else {
			c++;
		}
		setText("<html><p style=\"text-align:center\">" + label + "<br />[" + Integer.toString(c) + "]</div></html>");
		InputMap imap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_0 + c, KeyEvent.ALT_DOWN_MASK), "normal");
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_0 + c, KeyEvent.CTRL_DOWN_MASK), "reverse");
		getActionMap().put("normal", actNormal);
		getActionMap().put("reverse", actReverse);
	}


	public void setMultiAnnotation(boolean flag){
		isMultiAnnotation = flag;
	}
	

	public void actionPerformed(ActionEvent arg0) {
		if(isWorking){
			return;
		} else {
			isWorking = true; // 同時に複数の処理が行われるのを防止
		}
		
		boolean isTempMultiAnnotation = isMultiAnnotation;
		
		Date now = new Date(); // 現在日時
		int currentTime = soundPlayer.getElapsedTime(); // 開始からの経過時間（msec）
		
		// shift キーを押してクリックした場合，isMultiAnnotation を反転
		if((arg0.getModifiers() & ActionEvent.SHIFT_MASK) != 0){
			isTempMultiAnnotation ^= true; // reverse
		}
		
		if(buttonType == BUTTON_TYPE_COMMENT){
			User selectedDiscusser = new User("");
			if (isTempMultiAnnotation) {
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
			if (isTempMultiAnnotation) {
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
		AbstractAction act = new AbstractAction() {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				doClick(100);
			}
		};
		
		public MyJButton(int no, String label){
			super();
			this.no = no;
			int c = no;
			if(c == 9){
				c = 0;
			} else {
				c++;
			}
			setText("<html><p style=\"text-align:center\">" + label + "<br />[" + Integer.toString(c) + "]</div></html>");
			InputMap imap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
			imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_0 + c, 0), "key0");
			imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_0 + c, KeyEvent.ALT_DOWN_MASK), "keyALT");
			imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_0 + c, KeyEvent.CTRL_DOWN_MASK), "keyCTRL");
			getActionMap().put("key0", act);
			getActionMap().put("keyALT", act);
			getActionMap().put("keyCTRL", act);
		}
		
		public int getNumber(){
			return no;
		}
	}
}
