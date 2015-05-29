package org.teachothers.fishwatchr;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.EtchedBorder;

public class AnnotationGlobalViewer extends JPanel {
	private static final long serialVersionUID = 1L;
	private CommentList commentList;
	private int x0 = 0;
	private int y0 = 0;
	private HashMap keys = new HashMap();
	private int mediaLength = 0;

	private int markWidth = 3;
	private int markHeight = 20;
	
	private int xMax = 0;
	
	private boolean flag = false;
	private int currentTime = 0;

	private JPanel namePanel;
	private JPanel displayPanel;
	private DisplayPanel annotationViewerPanel;
	private JProgressBar timeBar;

	
	
	public AnnotationGlobalViewer(CommentList commentList, int mediaLength) {
//		super();
		this.commentList = commentList;
		this.mediaLength = mediaLength;
		ginit();
		init();
	}


	private void ginit(){
		x0 = 0;
		y0 = this.getSize().height;
		
		setLayout(new BorderLayout());
		namePanel = new JPanel();
		namePanel.setPreferredSize(new Dimension(50, Short.MAX_VALUE));
		namePanel.setBorder(new EtchedBorder());
		displayPanel = new JPanel();
		displayPanel.setLayout(new BorderLayout());
		annotationViewerPanel = new DisplayPanel();
		annotationViewerPanel.setBorder(new EtchedBorder());
		timeBar = new JProgressBar();
		
		displayPanel.add(annotationViewerPanel, BorderLayout.CENTER);
		displayPanel.add(timeBar, BorderLayout.SOUTH);
		
		add(namePanel, BorderLayout.LINE_START);
		add(displayPanel, BorderLayout.CENTER);
	}
	
	
	private void init(){
		
	}
	
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		displayPanel.repaint();
	}
	
	class DisplayPanel extends JPanel {

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			int max = 0;
			xMax = getSize().width;
			
			System.err.println("aa");
			
			for(Comment comment : commentList){
				int time = commentList.unifiedCommentTime(comment) / 1000;
				if(max < time){
					max = time;
				}
			}
			
			
			int n = 0;
			for(Comment comment : commentList){
				int time = commentList.unifiedCommentTime(comment) / 1000;
				String discusserName = comment.getDiscusser().getName();
				String commenterName = comment.getCommenter().getName();
			
				int x = time/4;
				int y0 = 100;
				if(x > xMax){
					x -= xMax;
					y0 = 250;
				}
				
				if(!keys.containsKey(discusserName)){
					keys.put(discusserName, n++);
				}
//				g.setColor(Color.black);
//				g.drawString(discusserName, 0, y0 - (int)keys.get(discusserName)*25);
				g.setColor(comment.getCommentType().getColor());
				g.fillRect(x, y0 - (int)keys.get(discusserName)*25 , 2, 20);
				

			}
		}
		
	}
	
	
	public void drawTime(int time){
		revalidate();
		currentTime = time;
		Graphics g = getGraphics();
		g.clearRect(0, 125, 100, 5);
		g.fillRect(currentTime/1000/2, 125, 5, 5);
	}

	public void drawTime(int time, boolean flag){
		this.flag = flag;
		currentTime = time;
		repaint();
	}
}
