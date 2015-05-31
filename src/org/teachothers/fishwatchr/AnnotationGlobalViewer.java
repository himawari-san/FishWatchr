package org.teachothers.fishwatchr;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

public class AnnotationGlobalViewer extends JPanel {
	private static final long serialVersionUID = 1L;
	private final int x0NamePanel = 4; // x origin of namePanel
	private final int y0NamePanel = 4; // y origin of namePanel
	private final int widthNamePanel = 50;

	private final int x0AnnotationViewerPanel = 2;
	private final int y0AnnotationViewerPanel = 4;	
	
	private CommentList commentList;

	private int itemHeight = 25;
	
	private int markWidth = 2;
	private int markHeight = 20;
	
	private int scaleFactor = 4; // x scaleFactor
	
	private int currentTime = 0;

	private JPanel namePanel;
	private JPanel displayPanel;
	private JPanel annotationViewerPanel;
	private JComboBox<String> targetSelector;
	private String[] targets = {"話者", "ラベル", "注釈者"};
	
	private ArrayList<User> discussers;
	private ArrayList<CommentType> commentTypes;
	private ArrayList<String> discusserNames = new ArrayList<String>();
	private ArrayList<String> types = new ArrayList<String>();
	private ArrayList<String> commenterNames = new ArrayList<String>();
	
	private SoundPlayer soundPlayer;
	
	public AnnotationGlobalViewer(CommentList commentList, SoundPlayer soundPlayer, ArrayList<User> discussers, ArrayList<CommentType> commentTypes) {
//		super();
		this.commentList = commentList;
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
		p2.add(targetSelector);

		displayPanel.add(annotationViewerPanel, BorderLayout.CENTER);
//		displayPanel.add(timeBar, BorderLayout.SOUTH);
		
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
					int x;
					String discusserName;
					String commenterName;
					String commentType;
					
					for(Comment comment : commentList){
						x = x0AnnotationViewerPanel +
								commentList.unifiedCommentTime(comment) / 1000 / scaleFactor;
						g.setColor(comment.getCommentType().getColor());

						switch (targetSelector.getSelectedIndex()){
						case 0:
							discusserName = comment.getDiscusser().getName();
							g.fillRect(x, y0AnnotationViewerPanel + discusserNames.indexOf(discusserName)*itemHeight , markWidth, markHeight);
							break;
						case 1:
							commentType = comment.getCommentType().getType();
							g.fillRect(x, y0AnnotationViewerPanel + types.indexOf(commentType)*itemHeight , markWidth, markHeight);
							break;
						case 2:
							commenterName = comment.getCommenter().getName();
							g.fillRect(x, y0AnnotationViewerPanel + commenterNames.indexOf(commenterName)*itemHeight , markWidth, markHeight);
							break;
						}
					}
					int xTime = x0AnnotationViewerPanel + soundPlayer.getElapsedTime() / scaleFactor /1000;
					g.setColor(Color.LIGHT_GRAY);
					g.drawLine(xTime, 0, xTime, 300);
				}
			};
			
			annotationViewerPanel.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
//					super.mouseClicked(e);
					if(e.getClickCount() < 2) {
						return;
					}
					Point point = e.getPoint();
					soundPlayer.setPlayPoint((long)(point.x * scaleFactor * 1000));
				}
			});
		}
		
		return annotationViewerPanel;
	}

	private JPanel getNamePanel(){
		if(namePanel == null){
			namePanel = new JPanel(){
				private static final long serialVersionUID = 1L;
				private final int fontHeight = getFontMetrics(getFont()).getHeight();

				public void paintComponent(Graphics g) {
					int i = 0;
					switch (targetSelector.getSelectedIndex()){
					case 0:
						for(String name: discusserNames){
							g.setColor(Color.black);
							g.drawString(name, x0NamePanel, y0NamePanel + i * itemHeight + fontHeight);
							i++;
						}
						break;
					case 1:
						for(String type: types){
							g.setColor(Color.black);
							g.drawString(type, x0NamePanel, y0NamePanel + i * itemHeight + fontHeight);
							i++;
						}
						break;
					case 2:
						for(String commenterName: commenterNames){
							g.setColor(Color.black);
							g.drawString(commenterName, x0NamePanel, y0NamePanel + i * itemHeight + fontHeight);
							i++;
						}
						break;
					default:
						for(String name: discusserNames){
							g.setColor(Color.black);
							g.drawString(name, x0NamePanel, y0NamePanel + i * itemHeight + fontHeight);
							i++;
						}
					}
				}
			};
			
		}
		
		return namePanel;
	}
	
    
	public void updatePanel(){
		discusserNames.clear();
		for(User discusser: discussers){
			String name = discusser.getName();
			if(!discusserNames.contains(name)){
				discusserNames.add(name);
			}
		}
//		Collections.sort(discusserNames);

		types.clear();
		for(CommentType commentType: commentTypes){
			String type = commentType.getType();
			if(!types.contains(type)){
				types.add(type);
			}
		}
//		Collections.sort(types);
		
		commenterNames.clear();
		for(Comment comment : commentList){
			String name = comment.getCommenter().getName();
			if(!commenterNames.contains(name)){
				commenterNames.add(comment.getCommenter().getName());
			}
		}
		Collections.sort(commenterNames);
		repaint();
	}
	

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		displayPanel.repaint();
		namePanel.repaint();
	}
}
