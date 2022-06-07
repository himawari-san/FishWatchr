/*
    Copyright (C) 2014-2021 Masaya YAMAGUCHI

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
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.lang3.StringUtils;

public class AnnotationSettingPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final int MAX_COMMENT_TYPE = 12;
	public static final String USER_NOT_DEFINED = ""; //$NON-NLS-1$
	
	private List<CommentType> commentTypes;
	private FTextField annotationNames[];
	private JButton markColors[];
	private JCheckBox discusserSelections[];
	
	public AnnotationSettingPanel(List<CommentType> commentTypes){
		this.commentTypes = commentTypes;
		annotationNames = new FTextField[MAX_COMMENT_TYPE];
		markColors = new ColorButton[MAX_COMMENT_TYPE];
		discusserSelections = new JCheckBox[MAX_COMMENT_TYPE];
		ginit();
	}
	
	private void ginit(){
		setLayout(new GridLayout(MAX_COMMENT_TYPE + 1, 3));
		JLabel a1 = new JLabel(Messages.getString("AnnotationSettingPanel.1")); //$NON-NLS-1$
		a1.setHorizontalAlignment(JLabel.CENTER);
		JLabel a2 = new JLabel(Messages.getString("AnnotationSettingPanel.2")); //$NON-NLS-1$
		a2.setHorizontalAlignment(JLabel.CENTER);
		add(a1);
		add(a2);
		
		for(int i = 0; i < MAX_COMMENT_TYPE; i++){
			annotationNames[i] = new FTextField();
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
				annotationNames[i].setText(USER_NOT_DEFINED);
			}
			
			add(annotationNames[i]);
			add(markColors[i]);
		}
	}
	
	public String updateNewValue() {
		ArrayList<String> invalidItems = new ArrayList<String>();
		
		for (int i = 0; i < MAX_COMMENT_TYPE; i++) {
		    if(annotationNames[i].getText().matches(".*\\s.*")){ //$NON-NLS-1$
		    	continue;
		    } else if(annotationNames[i].getText().matches(".*[<>/&'\"\\s].*")){ //$NON-NLS-1$
				invalidItems.add(annotationNames[i].getText());
				continue;
			} else if (i < commentTypes.size()) {
				commentTypes.get(i).set(annotationNames[i].getText(),
						markColors[i].getBackground());
			} else {
				commentTypes.add(new CommentType(annotationNames[i]
						.getText(), markColors[i].getBackground()));
			}
		}
		
		return StringUtils.join(invalidItems, ", "); //$NON-NLS-1$
	}
	
	
	class ColorButton extends JButton {
		private static final long serialVersionUID = 1L;

		public ColorButton(){
			addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Color selectedColor = JColorChooser.showDialog(new JFrame(), Messages.getString("AnnotationSettingPanel.6"), ColorButton.this.getBackground()); //$NON-NLS-1$
					if(selectedColor != null){
						ColorButton.this.setBackground(selectedColor);
					}
				}
			});
		}
	}
}
