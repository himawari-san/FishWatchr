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
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class AnnotationSettingPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final int MAX_COMMENT_TYPE = 12;
	public static final String NOT_SPECIFIED = "(未指定)";
	
	private List<CommentType> commentTypes;
	private JTextField annotationNames[];
	private JButton markColors[];
	private JCheckBox discusserSelections[];
	
	public AnnotationSettingPanel(List<CommentType> commentTypes){
		this.commentTypes = commentTypes;
		annotationNames = new JTextField[MAX_COMMENT_TYPE];
		markColors = new ColorButton[MAX_COMMENT_TYPE];
		discusserSelections = new JCheckBox[MAX_COMMENT_TYPE];
		ginit();
	}
	
	private void ginit(){
		setLayout(new GridLayout(MAX_COMMENT_TYPE + 1, 3));
		JLabel a1 = new JLabel("注記名");
		a1.setHorizontalAlignment(JLabel.CENTER);
		JLabel a2 = new JLabel("マーク色");
		a2.setHorizontalAlignment(JLabel.CENTER);
		add(a1);
		add(a2);
		
		for(int i = 0; i < MAX_COMMENT_TYPE; i++){
			annotationNames[i] = new JTextField();
			markColors[i] = new ColorButton();
			discusserSelections[i] = new JCheckBox();
			annotationNames[i].setPreferredSize(new Dimension(100, 25));
			discusserSelections[i].setHorizontalAlignment(JCheckBox.CENTER);

			if(commentTypes.size() > i){
				annotationNames[i].setText(commentTypes.get(i).getType());
				markColors[i].setBackground(commentTypes.get(i).getColor());
				markColors[i].setOpaque(true);
				markColors[i].setBorderPainted(false);
			} else {
				annotationNames[i].setText(NOT_SPECIFIED);
			}
			
			add(annotationNames[i]);
			add(markColors[i]);
		}
	}
	
	public void updateNewValue() {
		int nCommentTypes = 0;

		for (int i = 0; i < MAX_COMMENT_TYPE; i++) {
			if (!annotationNames[i].getText().equals(NOT_SPECIFIED)) {
				nCommentTypes++;
				if (nCommentTypes > commentTypes.size()) {
					commentTypes.add(new CommentType(annotationNames[i]
							.getText(), markColors[i].getBackground()));
				} else {
//					System.err.println("hey!3" + markColors[i].getBackground());
					commentTypes.get(i).set(annotationNames[i].getText(),
							markColors[i].getBackground());
				}
			}
		}
	}
	
	
	class ColorButton extends JButton {
		private static final long serialVersionUID = 1L;

		public ColorButton(){
			addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Color selectedColor = JColorChooser.showDialog(new JFrame(), "色を選択してください", ColorButton.this.getBackground());
					if(selectedColor != null){
						ColorButton.this.setBackground(selectedColor);
					}
				}
			});
		}
	}
	
	
}
