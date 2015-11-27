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
import java.util.HashMap;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

public class AnnotationGlobalViewer extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final int SCALE_FACTOR_DEFAULT = 4;
	private static final int VIEW_TYPE_SPEAKER = 0; 
	private static final int VIEW_TYPE_LABEL = 1; 
	private static final int VIEW_TYPE_COMMENTER = 2; 

	private static final int COMPARISON_NOTHING = 0; 
	private static final int COMPARISON_COMMENTER = 1; 
	private static final int COMPARISON_LABEL = 2; 
	private static final int COMPARISON_DISCUSSER = 3; 

	private static final int scaleFactorHistogram = 4;
	
	private final int x0NamePanel = 4; // x origin of namePanel
	private final int y0NamePanel = 4; // y origin of namePanel
	private final int widthNamePanel = 50;

	private final int x0AnnotationViewerPanel = 2;
	private final int y0AnnotationViewerPanel = 4;	

	private final int xTimeMaxTickHeight = 5;	

	private final int y0MarginHistogram = 2;
	private int y0Histogram = 0;
	
	private int itemHeight = 25;
	
	private int markWidth = 2;
	private int markHeight = 20;
	
	private int scaleFactor = SCALE_FACTOR_DEFAULT; // x scaleFactor (1/x)
	
	private JPanel namePanel;
	private JPanel displayPanel;
	private JPanel annotationViewerPanel;
	private JComboBox<String> targetSelector;
	private JComboBox<String> displayTypeSelector;
	private String[] targets = {"話者", "ラベル", "注釈者"};
	private String[] displayTypes = {"なし", "注釈者", "ラベル", "話者"};
	private ArrayList<User> discussers;
	private ArrayList<CommentType> commentTypes;
	private ArrayList<String> discusserNames = new ArrayList<String>();
	private ArrayList<String> types = new ArrayList<String>();
	private ArrayList<String> commenterNames = new ArrayList<String>();
	
	private CommentTableModel ctm;
	private SoundPlayer soundPlayer;
	private float totalTime = 0; // sec
	private int xTimeMax = 0;
	private int focusedRange = 10000;
	
	public AnnotationGlobalViewer(CommentTableModel ctm, SoundPlayer soundPlayer, ArrayList<User> discussers, ArrayList<CommentType> commentTypes) {
		this.ctm = ctm;
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
		displayTypeSelector = new JComboBox<String>(displayTypes);
		p2.add(new JLabel("表示"));
		p2.add(targetSelector);
		p2.add(new JLabel("比較"));
		p2.add(displayTypeSelector);

		displayPanel.add(annotationViewerPanel, BorderLayout.CENTER);
		
		p1.add(namePanel, BorderLayout.LINE_START);
		p1.add(displayPanel, BorderLayout.CENTER);
		add(p1, BorderLayout.CENTER);
		add(p2, BorderLayout.NORTH);
	}

	
	private JPanel getAnnotationViewerPanel(){
		if(annotationViewerPanel == null){
			annotationViewerPanel = new JPanel(){
				private static final long serialVersionUID = 1L;
				@SuppressWarnings("unchecked")
				private HashMap<String, Integer>[] results = (HashMap<String, Integer>[]) new HashMap[2000];
				
				@Override
				protected void paintComponent(Graphics g) {
					super.paintComponent(g);
					
					ArrayList<Comment> filteredCommentList = ctm.getFilteredCommentList();
					CommentList commentList = ctm.getCommentList();

					plotData(g, filteredCommentList, commentList);
					drawHistogram(g, filteredCommentList, commentList);
				}

				
				private void drawHistogram(Graphics g, ArrayList<Comment> filteredCommentList, CommentList commentList) {
					int n = filteredCommentList.size();
					int freq;
					Comment comment;
					String targetCond = null;
					String cond = null;
					int selector = displayTypeSelector.getSelectedIndex();
					
					g.setColor(Color.darkGray);
					for(int i = 0; i < n; i++){
						Comment targetComment = filteredCommentList.get(i);
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
							comment = filteredCommentList.get(j);

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
							comment = filteredCommentList.get(j);

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
						int x = x0AnnotationViewerPanel +
								commentList.unifiedCommentTime(targetComment) / 1000 / scaleFactor;
//						g.fillRect(x, y0Histogram-(50), markWidth, 50);
						g.fillRect(x, y0Histogram-(freq*scaleFactorHistogram), markWidth, freq*scaleFactorHistogram);
						this.getHeight();
					}
				}

				
				protected void plotData(Graphics g, ArrayList<Comment> filteredCommentList, CommentList commentList) {
					int x;
					String discusserName;
					String commenterName;
					String commentType;
					
					for(Comment comment : filteredCommentList){
						x = x0AnnotationViewerPanel +
								commentList.unifiedCommentTime(comment) / 1000 / scaleFactor;
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
							commenterName = comment.getCommenter().getName();
							g.fillRect(x, y0AnnotationViewerPanel + commenterNames.indexOf(commenterName)*itemHeight , markWidth, markHeight);
							break;
						}
					}
					int xTime = x0AnnotationViewerPanel + soundPlayer.getElapsedTime() / scaleFactor /1000;
					g.setColor(Color.BLACK);
					g.drawLine(xTime, 0, xTime, getSize().height);
					g.drawLine(xTimeMax, 0, xTimeMax, xTimeMaxTickHeight);
				}
				
				protected void displayTypeEntropy(Graphics g, ArrayList<Comment> filteredCommentList, CommentList commentList) {
					int x;
					String discusserName;
					String commenterName;
					String commentType;
					
					for(int i = 0; i < results.length; i++){
						results[i] = null;
					}

					String key;
					for(Comment comment : filteredCommentList){
						int i = commentList.unifiedCommentTime(comment) / 1000 / 15;
						HashMap<String, Integer> data = results[i];
						if(results[i] == null){
							data = new HashMap<String, Integer>();
							results[i] = data;
						}

						switch (targetSelector.getSelectedIndex()){
						case VIEW_TYPE_SPEAKER:
							key = comment.getDiscusser().getName();
							break;
						case VIEW_TYPE_LABEL:
							key = comment.getCommentType().getType();
							break;
						case VIEW_TYPE_COMMENTER:
							key = comment.getCommenter().getName();
							break;
						default:
							key = "";
						}

						if(data.containsKey(key)){
							data.put(key, data.get(key) + 1);
						} else {
							data.put(key, 1);
						}
					}

					for(int i = 0; i < results.length; i++){
						if(results[i] == null) continue;
						x = x0AnnotationViewerPanel + i * 15 / scaleFactor;
						int entropy = (int)(calEntropy(results[i])*180);
						if(entropy > 255) entropy = 255;
						System.err.println("ent: " + entropy);
						g.setColor(new Color(entropy, 0, 0));
						g.fillRect(x, y0AnnotationViewerPanel, markWidth*(15 / scaleFactor), markHeight);
					}
					
					int xTime = x0AnnotationViewerPanel + soundPlayer.getElapsedTime() / scaleFactor /1000;
					g.setColor(Color.BLACK);
					g.drawLine(xTime, 0, xTime, getSize().height);
					g.drawLine(xTimeMax, 0, xTimeMax, xTimeMaxTickHeight);
				}
				
				
				
				protected double calEntropy(HashMap<String, Integer> data){
					int freqSum = 0;
					double res = 0;
					
					for(int v : data.values()){
						freqSum += v;
					}

					for(int v : data.values()){
						res += (double)v * Math.log10((double)v / freqSum);
//						System.err.println("v: " + v + ", " +  freqSum + ", " + res);
					}
					res = -1 * res / freqSum / Math.log10(2);
					System.err.println("res: " + res);
//					System.exit(0);
					return res;
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
					float newTime = point.x * scaleFactor;
					if(newTime < totalTime){
						soundPlayer.setPlayPoint((long)(newTime * 1000));
					}
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
					super.paintComponent(g);
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

		
		totalTime = soundPlayer.getSoundLength();
		
		// 1800 = 30min 
		int t = (int) Math.ceil(totalTime / 1800f);
		if(t > 1){
			scaleFactor = SCALE_FACTOR_DEFAULT * t;
		} else {
			scaleFactor = SCALE_FACTOR_DEFAULT;
		}
		
		xTimeMax = x0AnnotationViewerPanel + (int)totalTime / scaleFactor;

		repaint();
		y0Histogram = annotationViewerPanel.getHeight() - y0MarginHistogram;
	}
	

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		displayPanel.repaint();
		namePanel.repaint();
	}
	
	
	public void setFocusRange(int msec){
		focusedRange = msec;
	}
}
