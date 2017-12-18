/*
    Copyright (C) 2014-2017 Masaya YAMAGUCHI

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

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ToolTipManager;
import javax.swing.border.EtchedBorder;


public class AnnotationGlobalViewer extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final float SCALE_FACTOR_DEFAULT = 2f;
	private static final float Y_SCALE_FACTOR_DEFAULT = 10f;
	private static final float Y_SCALE_FACTOR_MIN = 0.2f;
	private static final int VIEW_TYPE_LABEL = 0; 
	private static final int VIEW_TYPE_SPEAKER = 1; 
	private static final int VIEW_TYPE_COMMENTER = 2; 

	private static final int COMPARISON_NOTHING = 0; 
	private static final int COMPARISON_COMMENTER = 1; 
	private static final int COMPARISON_LABEL = 2; 
	private static final int COMPARISON_DISCUSSER = 3; 

	
	private final int xTimeTickHeight = 5;	

	private final int x0NamePanel = 4; // x origin of namePanel
	private final int y0NamePanel = xTimeTickHeight + 1; // y origin of namePanel
	private final int widthNamePanel = 50;

	private final int x0AnnotationViewerPanel = 2;
	private final int y0AnnotationViewerPanel = y0NamePanel;	


	private final int y0MarginHistogram = 2;
	private int y0Histogram = 0;
	
	private int itemHeight = 25;
	
	private int markWidth = 1;
	private int markHeight = 20;
	
	private float scaleFactor = SCALE_FACTOR_DEFAULT; // x scaleFactor (1/x)
	private float yScaleFactorHistogram = Y_SCALE_FACTOR_DEFAULT;
	private float ratioPlotArea = 0.9f;
	private float yScaleFactorHistogramNext = yScaleFactorHistogram;
	
	private JPanel namePanel;
	private JPanel displayPanel;
	private JPanel annotationViewerPanel;
	private JComboBox<String> targetSelector;
	private JComboBox<String> displayTypeSelector;
	private JButton resetScaleButton;
	private String[] targets = {Comment.ITEM_LABEL, Comment.ITEM_TARGET, Comment.ITEM_ANNOTATOR};
	private String[] displayTypes = {"なし", Comment.ITEM_ANNOTATOR, Comment.ITEM_LABEL, Comment.ITEM_TARGET};
	private ArrayList<User> discussers;
	private ArrayList<CommentType> commentTypes;
	private ArrayList<String> discusserNames = new ArrayList<String>();
	private ArrayList<String> types = new ArrayList<String>();
	private ArrayList<String> commenterNames = new ArrayList<String>();
	
	private CommentTableModel ctm;
	private SoundPlayer soundPlayer;
	private int focusedRange = 10000;
	private int focusedRangeTick = 2;
	
	private int selectionStartTime = 0;
	private int selectionEndTime = 0;
	
	private boolean isFiltered = true;
	
	public AnnotationGlobalViewer(CommentTableModel ctm, SoundPlayer soundPlayer, ArrayList<User> discussers, ArrayList<CommentType> commentTypes) {
		this.ctm = ctm;
		this.discussers = discussers;
		this.commentTypes = commentTypes;
		this.soundPlayer = soundPlayer;
		
		ginit();
		init();
	}

	public void init(){
		initScaleFactor();
		updatePanel();
	}
	

	private void ginit(){
		setLayout(new BorderLayout());
		JPanel p1 = new JPanel();
		p1.setLayout(new BorderLayout());
		JPanel p2 = new JPanel();
		p2.setLayout(new FlowLayout(FlowLayout.LEFT));
		namePanel = getNamePanel();
		namePanel.setPreferredSize(new Dimension(widthNamePanel, Short.MAX_VALUE));
		namePanel.setBorder(new EtchedBorder());
		displayPanel = new JPanel();
		displayPanel.setLayout(new BorderLayout());
		annotationViewerPanel = getAnnotationViewerPanel();
		annotationViewerPanel.setBorder(new EtchedBorder());
		
		targetSelector = new JComboBox<String>(targets);
		displayTypeSelector = new JComboBox<String>(displayTypes);
		resetScaleButton = new JButton("リセット");
		resetScaleButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				initScaleFactor();
				ctm.selectTimePeriod(null); // clear
				ctm.refreshFilter();
			}
		});
		p2.add(new JLabel("表示"));
		p2.add(targetSelector);
//		p2.add(new JLabel("比較"));
//		p2.add(displayTypeSelector);
		p2.add(resetScaleButton);

		displayPanel.add(annotationViewerPanel, BorderLayout.CENTER);
		
		p1.add(namePanel, BorderLayout.LINE_START);
		p1.add(displayPanel, BorderLayout.CENTER);
		add(p1, BorderLayout.CENTER);
		add(p2, BorderLayout.NORTH);
	}

	
	private JPanel getAnnotationViewerPanel(){
		if(annotationViewerPanel == null){
			annotationViewerPanel = new AnnotationViewerPanel();
		}
		return annotationViewerPanel;
	}

	private JPanel getNamePanel(){
		if(namePanel == null){
			namePanel = new AnnotationNamePanel();
		}
		
		return namePanel;
	}
	

	private int getItemHeight(Graphics g, int panelHeight){
		int nItemP1 = commenterNames.size()+1;
		int newItemHeight = g.getFontMetrics(g.getFont()).getHeight();
		int fontSize = g.getFont().getSize();
		int yButtom = y0NamePanel + nItemP1 * newItemHeight;
		while(yButtom > panelHeight * ratioPlotArea){
			fontSize--;
			if(fontSize < 1){
				break;
			}
			g.setFont(new Font(Font.DIALOG, Font.PLAIN, fontSize));
			newItemHeight = g.getFontMetrics(g.getFont()).getHeight();
			yButtom = y0NamePanel + nItemP1 * newItemHeight;
		}
		if(fontSize == FishWatchr.DEFAULT_FONT_SIZE){
			newItemHeight = itemHeight;
			g.setFont(new Font(Font.DIALOG, Font.PLAIN, FishWatchr.DEFAULT_FONT_SIZE));
		}

		return newItemHeight;
	}
	
	
	public void updatePanel(){
		discusserNames.clear();
		for(User discusser: discussers){
			String name = discusser.getName();
			if(!discusserNames.contains(name)){
				discusserNames.add(name);
			}
		}

		types.clear();
		for(CommentType commentType: commentTypes){
			String type = commentType.getType();
			if(!types.contains(type)){
				types.add(type);
			}
		}
		
		ArrayList<Comment> filteredCommentList = ctm.getFilteredCommentList();
		commenterNames.clear();
		for(Comment comment : filteredCommentList){
			String name = comment.getCommenter().getName();
			if(!commenterNames.contains(name)){
				commenterNames.add(comment.getCommenter().getName());
			}
		}
		Collections.sort(commenterNames);

		// update scaleFactor
		updateScaleFactor();
		focusedRangeTick = (int)(focusedRange/scaleFactor/1000);
		yScaleFactorHistogram = Y_SCALE_FACTOR_DEFAULT;
		yScaleFactorHistogramNext = yScaleFactorHistogram;

		repaint();
		y0Histogram = annotationViewerPanel.getHeight() - y0MarginHistogram;
	}
	
	
	public void initScaleFactor(){
		float totalTime = 0;
		
		if(soundPlayer.getPlayerState() == SoundPlayer.PLAYER_STATE_RECORD){
			totalTime = SoundPlayer.LIMIT_RECODING_TIME;
		} else {
			totalTime = soundPlayer.getSoundLength();
		}
		
		selectionStartTime = 0;
		selectionEndTime = (int)(totalTime * 1000);
		updateScaleFactor();
	}
	
	
	public void updateScaleFactor(){
		scaleFactor = ((float)(selectionEndTime - selectionStartTime)) / 1000 / (annotationViewerPanel.getWidth() - x0AnnotationViewerPanel*2 - 1);
		setFocusRange(focusedRange); // update focusedRangeTick
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		displayPanel.repaint();
		namePanel.repaint();
	}
	
	
	public void setFocusRange(int msec){
		focusedRange = msec;
		focusedRangeTick = (int)(focusedRange/scaleFactor/1000);
	}
	
	
	public void setExternalMouseListener(MouseListener l){
		annotationViewerPanel.addMouseListener(l);
	}
	
	
	public void setPlayPoint(int x){
		float newTime = (x - x0AnnotationViewerPanel) * scaleFactor * 1000 + selectionStartTime;
		if(newTime >= 0 && newTime < selectionEndTime){
			soundPlayer.setPlayPoint((long)newTime);
		}
	}
	
	
	public void applyFilter(boolean flag){
		isFiltered = flag;
	}
	
	class AnnotationViewerPanel extends JPanel implements MouseMotionListener, MouseListener {
		private static final long serialVersionUID = 1L;
		private ToolTipManager ttm = ToolTipManager.sharedInstance();
		private int defaultDelay = ttm.getInitialDelay();
		private int selectionStartX = -1;
		private int selectionEndX = -1;
		private int cursorX = -1;
		private float cursorLineDash[] = {3.0f, 3.0f};
		private BasicStroke dashStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, cursorLineDash, 0.0f); 
		boolean isDragged = false;
		
		public AnnotationViewerPanel() {
			addMouseListener(this);
			addMouseMotionListener(this);
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			ArrayList<Comment> filteredCommentList = ctm.getFilteredCommentList();
			CommentList commentList = ctm.getCommentList();

			plotData(g, filteredCommentList, commentList);
			drawHistogram(g, filteredCommentList, commentList);
			drawCursorLine(g);

			if(isDragged){
				drawSelection(g);
			}
		}

		
		private void drawSelection(Graphics g){
			g.setColor(Color.black);
			
			int tempSelectionStartX;
			int tempSelectionEndX;
			
			if(selectionEndX - selectionStartX > 0){
				tempSelectionStartX = selectionStartX;
				tempSelectionEndX = selectionEndX;
			} else {
				tempSelectionStartX = selectionEndX;
				tempSelectionEndX = selectionStartX;
			}
			g.drawRect(tempSelectionStartX, 0, tempSelectionEndX - tempSelectionStartX, getHeight());
		}
		
		
		private void drawHistogram(Graphics g, ArrayList<Comment> filteredCommentList, CommentList commentList) {
			int n;
			int freq;
			Comment comment;
			String targetCond = null;
			String cond = null;
			int selector = displayTypeSelector.getSelectedIndex();
			int heightMax = (int)(getHeight() * (1 - ratioPlotArea));
			List<Comment> targetList = commentList;
			
			if(isFiltered){
				targetList = filteredCommentList;
			} else {
				targetList = commentList;
			}
			n = targetList.size();
			
			yScaleFactorHistogram = yScaleFactorHistogramNext;
			g.setColor(Color.darkGray);
			for(int i = 0; i < n; i++){
				Comment targetComment = targetList.get(i);
				int targetCommentTime = commentList.unifiedCommentTime(targetComment);
				freq = 0;
				
				switch(selector){
				case COMPARISON_NOTHING:
					targetCond = null;
					break;
				case COMPARISON_COMMENTER:
					targetCond = targetComment.getCommenter().getName();
					break;
				case COMPARISON_LABEL:
					targetCond = targetComment.getCommentType().getType();
					break;
				case COMPARISON_DISCUSSER:
					targetCond = targetComment.getDiscusser().getName();
				}


				for(int j = i+1; j < n; j++){
					comment = targetList.get(j);

					switch(selector){
					case COMPARISON_NOTHING:
						cond = null;
						break;
					case COMPARISON_COMMENTER:
						cond = comment.getCommenter().getName();
						break;
					case COMPARISON_LABEL:
						cond = comment.getCommentType().getType();
						break;
					case COMPARISON_DISCUSSER:
						cond = comment.getDiscusser().getName();
					}

					if(commentList.unifiedCommentTime(comment) - targetCommentTime < focusedRange){
						if(targetCond == null || !targetCond.equals(cond)){
							freq++;
						}
					} else {
						break;
					}
				}
				for(int j = i-1; j >= 0; j--){
					comment = targetList.get(j);

					switch(selector){
					case COMPARISON_NOTHING:
						cond = null;
						break;
					case COMPARISON_COMMENTER:
						cond = comment.getCommenter().getName();
						break;
					case COMPARISON_LABEL:
						cond = comment.getCommentType().getType();
						break;
					case COMPARISON_DISCUSSER:
						cond = comment.getDiscusser().getName();
					}

					if(targetCommentTime - commentList.unifiedCommentTime(comment) < focusedRange){
						if(targetCond == null || !targetCond.equals(cond)){
							freq++;
						}
					} else {
						break;
					}
				}
				
				int y = (int)(freq * yScaleFactorHistogram);
				if(heightMax < y && yScaleFactorHistogram > Y_SCALE_FACTOR_MIN){
					i = 0;
					yScaleFactorHistogramNext = (float)heightMax / (freq+1);
					yScaleFactorHistogram = yScaleFactorHistogramNext;
					continue;
				}
				
				int x = (int)
						(x0AnnotationViewerPanel +
						(commentList.unifiedCommentTime(targetComment) - selectionStartTime) / 1000 / scaleFactor);
				g.fillRect(x, y0Histogram-y, markWidth, y);
			}
		}

		
		protected void plotData(Graphics g, ArrayList<Comment> filteredCommentList, CommentList commentList) {
			int x;
			String discusserName;
			String commenterName;
			String commentType;
			List<Comment> targetList;

			if(isFiltered){
				targetList = filteredCommentList;
			} else {
				targetList = commentList;
			}
			
			for(Comment comment : targetList){
				x = (int)
						(x0AnnotationViewerPanel +
								((float)(commentList.unifiedCommentTime(comment)- selectionStartTime) / 1000 ) / scaleFactor);
				g.setColor(comment.getCommentType().getColor());

				switch (targetSelector.getSelectedIndex()){
				case VIEW_TYPE_SPEAKER:
					discusserName = comment.getDiscusser().getName();
					g.fillRect(x, y0AnnotationViewerPanel + discusserNames.indexOf(discusserName)*itemHeight , markWidth, markHeight);
					break;
				case VIEW_TYPE_LABEL:
					commentType = comment.getCommentType().getType();
					g.fillRect(x, y0AnnotationViewerPanel + types.indexOf(commentType)*itemHeight , markWidth, markHeight);
					break;
				case VIEW_TYPE_COMMENTER:
					int newItemHeight = getItemHeight(g, getHeight());
					int newMarkHeight = newItemHeight < 2 ? newItemHeight : newItemHeight - 1;
					commenterName = comment.getCommenter().getName();
					g.fillRect(x, y0AnnotationViewerPanel + commenterNames.indexOf(commenterName)*newItemHeight , markWidth, newMarkHeight);
					break;
				}
			}
			int xTime = (int)
					(x0AnnotationViewerPanel + (soundPlayer.getElapsedTime() - selectionStartTime) / scaleFactor /1000);
			g.setColor(Color.BLACK);
			g.drawLine(xTime, 0, xTime, getSize().height);
			g.drawLine(xTime-focusedRangeTick, 0, xTime-focusedRangeTick, xTimeTickHeight);
			g.drawLine(xTime+focusedRangeTick, 0, xTime+focusedRangeTick, xTimeTickHeight);
		}
		
		
		private void drawCursorLine(Graphics g){
			Stroke defaultStroke = ((Graphics2D)g).getStroke();
			g.setColor(Color.LIGHT_GRAY);
			((Graphics2D)g).setStroke(dashStroke);
			g.drawLine(cursorX, 0, cursorX, getSize().height);
			((Graphics2D)g).setStroke(defaultStroke);
		}
		
		
		@Override
		public void mouseEntered(MouseEvent e) {
			ttm.setInitialDelay(0);
		}
				
		@Override
		public void mouseExited(MouseEvent e) {
			ttm.setInitialDelay(defaultDelay);
			cursorX = -1;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			// use an external listener now
		}
		
		@Override
		public void mouseDragged(MouseEvent e) {
			selectionEndX = e.getX();
			isDragged = true;
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			int time = (int)((e.getX() - x0AnnotationViewerPanel) * scaleFactor + ((float)selectionStartTime/1000));
			int hour = time / 3600;
			time -= hour * 3600;
			int minute = time / 60;
			int sec = time - minute * 60;
				
			annotationViewerPanel.setToolTipText(String.format("%02d:%02d:%02d", hour, minute, sec));
			cursorX = e.getX();
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if(selectionStartX == -1){
				selectionStartX = e.getX();
				selectionEndX = -1;
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if(isDragged){
				if(selectionEndX - selectionStartX < 0){ // swap
					int t = selectionStartX;
					selectionStartX = selectionEndX;
					selectionEndX = t;
				}
				int oldSelectionStartTime = selectionStartTime; 
				selectionStartTime = (int)((selectionStartX - x0AnnotationViewerPanel) * scaleFactor) * 1000 + oldSelectionStartTime;
				selectionEndTime = (int)((selectionEndX - x0AnnotationViewerPanel) * scaleFactor) * 1000 + oldSelectionStartTime;
				updateScaleFactor();
				ctm.selectTimePeriod(new SimpleTimePeriod(selectionStartTime, selectionEndTime));
				cursorX = e.getX();
			}
			
			selectionStartX = -1;
			isDragged = false;
		}
	}
	
	
	class AnnotationNamePanel extends JPanel {
		private static final long serialVersionUID = 1L;
		private final int fontHeight = getFontMetrics(getFont()).getHeight();

		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			
		    ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
                    RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

			int i = 0;
			switch (targetSelector.getSelectedIndex()){
			case VIEW_TYPE_SPEAKER:
				for(String name: discusserNames){
					g.setColor(Color.black);
					g.drawString(name, x0NamePanel, y0NamePanel + i * itemHeight + fontHeight);
					i++;
				}
				break;
			case VIEW_TYPE_LABEL:
				for(String type: types){
					g.setColor(Color.black);
					g.drawString(type, x0NamePanel, y0NamePanel + i * itemHeight + fontHeight);
					i++;
				}
				break;
			case VIEW_TYPE_COMMENTER:
				int newItemHeight = getItemHeight(g, getHeight());
				int newFontHeight = g.getFontMetrics(g.getFont()).getHeight();

				for(String commenterName: commenterNames){
					g.setColor(Color.black);
					g.drawString(commenterName , x0NamePanel, y0NamePanel + i * newItemHeight + newFontHeight);
					i++;
				}
				g.setFont(new Font(Font.DIALOG, Font.PLAIN, FishWatchr.DEFAULT_FONT_SIZE));
				break;
			default:
				for(String name: discusserNames){
					g.setColor(Color.black);
					g.drawString(name, x0NamePanel, y0NamePanel + i * itemHeight + fontHeight);
					i++;
				}
			}
		}
	}
}
