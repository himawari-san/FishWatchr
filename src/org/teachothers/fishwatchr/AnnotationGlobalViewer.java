package org.teachothers.fishwatchr;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.EtchedBorder;

public class AnnotationGlobalViewer extends JPanel {
	private static final long serialVersionUID = 1L;
	private CommentList commentList;
	private int x0 = 0;
	private int y0 = 0;
//	private HashMap<String, Integer> keys = new HashMap<String, Integer>();
//	private int mediaLength = 0;

	private int markWidth = 3;
	private int markHeight = 20;
	
	private int xMax = 0;
	
	private boolean flag = false;
	private int currentTime = 0;

	private JPanel namePanel;
	private JPanel displayPanel;
	private JPanel annotationViewerPanel;
	private JProgressBar timeBar;
	
	private ArrayList<User> discussers;
	private ArrayList<CommentType> commentTypes;
	private ArrayList<String> discusserNames = new ArrayList<String>();
	private ArrayList<String> types = new ArrayList<String>();
	private ArrayList<String> commenterNames = new ArrayList<String>();
	
	private SoundPlayer soundPlayer;
	
	public AnnotationGlobalViewer(CommentList commentList, SoundPlayer soundPlayer, ArrayList<User> discussers, ArrayList<CommentType> commentTypes) {
//		super();
		this.commentList = commentList;
//		this.mediaLength = mediaLength;
		this.discussers = discussers;
		this.commentTypes = commentTypes;
		this.soundPlayer = soundPlayer;
		
		ginit();
		init();
	}

	private void init(){
		updatePanel();
	}
	

	private void ginit(){
		x0 = 0;
		y0 = this.getSize().height;

		setLayout(new BorderLayout());
		JPanel p1 = new JPanel();
		p1.setLayout(new BorderLayout());
		JPanel p2 = new JPanel();
		p2.add(new JButton("test"));
		namePanel = getNamePanel();
		namePanel.setPreferredSize(new Dimension(50, Short.MAX_VALUE));
		namePanel.setBorder(new EtchedBorder());
		displayPanel = new JPanel();
		displayPanel.setLayout(new BorderLayout());
		annotationViewerPanel = getAnnotationViewerPanel();
		annotationViewerPanel.setBorder(new EtchedBorder());
		timeBar = new JProgressBar();
		
		displayPanel.add(annotationViewerPanel, BorderLayout.CENTER);
		displayPanel.add(timeBar, BorderLayout.SOUTH);
		
		p1.add(namePanel, BorderLayout.LINE_START);
		p1.add(displayPanel, BorderLayout.CENTER);
		add(p1, BorderLayout.CENTER);
		add(p2, BorderLayout.NORTH);
	}

	
	private JPanel getAnnotationViewerPanel(){
		if(annotationViewerPanel == null){
			annotationViewerPanel = new JPanel(){
				private static final long serialVersionUID = 1L;

				@Override
				protected void paintComponent(Graphics g) {
					super.paintComponent(g);
					int max = 0;
					xMax = getSize().width;
					
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
						g.setColor(comment.getCommentType().getColor());
						g.fillRect(x, discusserNames.indexOf(discusserName)*25 , 2, 20);
//						System.err.println(discusserName + ":" + discusserNames.indexOf(discusserName));
					}
					int x1 = soundPlayer.getElapsedTime()/4000;
					g.drawLine(x1, 0, x1, 300);
				}
				
			};
			
			annotationViewerPanel.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
//					super.mouseClicked(e);
					Point point = e.getPoint();
					soundPlayer.setPlayPoint((long)(point.x * 4 * 1000));
//					System.err.println("x:" + point.x + "," + (long)(point.x * 4));
//					System.err.println("x:" + point.x + "," + (long)(point.x * 4.0f / soundPlayer.getSoundLength()));
				}
			});
		}
		
		return annotationViewerPanel;
	}

	private JPanel getNamePanel(){
		if(namePanel == null){
			namePanel = new JPanel(){
				private static final long serialVersionUID = 1L;

				public void paintComponent(Graphics g) {
					int i = 0;
					for(String name: discusserNames){
						g.setColor(Color.black);
						g.drawString(name, 4, i*25+getFontMetrics(getFont()).getHeight());
						i++;
					}
				}
			};
			
		}
		
		return namePanel;
	}
	
    class UserComparator implements Comparator<User> {  
        public int compare(User arg0, User arg1) {
        	return arg0.getName().compareTo(arg1.getName());
        }
    }  

    
    class CommentTypeComparator implements Comparator<CommentType> {  
        public int compare(CommentType arg0, CommentType arg1) {
        	return arg0.getType().compareTo(arg1.getType());
        }
    }  

    
	public void updatePanel(){
		discusserNames.clear();
		for(User discusser: discussers){
			discusserNames.add(discusser.getName());
		}
		Collections.sort(discusserNames);

		types.clear();
		for(CommentType commentType: commentTypes){
			types.add(commentType.getType());
		}
		Collections.sort(types);
		
		commenterNames.clear();
		for(Comment comment : commentList){
			commenterNames.add(comment.getCommenter().getName());
		}
		Collections.sort(commenterNames);
		repaint();
	}
	

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		displayPanel.repaint();
		namePanel.repaint();
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
