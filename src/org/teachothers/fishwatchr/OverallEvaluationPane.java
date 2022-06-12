package org.teachothers.fishwatchr;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.teachothers.fishwatchr.OverallEvaluation.Evaluation;
import org.teachothers.fishwatchr.OverallEvaluation.EvaluationCategory;



public class OverallEvaluationPane extends JOptionPane {
	private static final long serialVersionUID = 1L;
	private static final int WIDTH = 480;
	private static final int HEIGHT = 550;

	private User evaluator;
	private ArrayList<CommentType> commentTypes;
	private ArrayList<User> discussers;
	private HashMap<String, OverallEvaluation> evaluations;
	private InputPanel inputPanel;
	private ResultPanel resultPanel;


	public OverallEvaluationPane(HashMap<String, OverallEvaluation> evaluations, User evaluator, ArrayList<CommentType> commentTypes, ArrayList<User> discussers) {
		this.evaluator = evaluator;
		this.commentTypes = commentTypes;
		this.discussers = discussers;
		this.evaluations = evaluations;
		ginit();
	}

	
	private void ginit(){
//		setOptions(new Object[0]); // remove the default OK button
		setSize(WIDTH, HEIGHT);
		setMinimumSize(new Dimension(WIDTH, HEIGHT));
		setPreferredSize(new Dimension(WIDTH, HEIGHT));


		int nMainTab = 0;
		JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
		JTabbedPane mainTabPane = new JTabbedPane() {
			private static final long serialVersionUID = 1L;

			@Override
			public void setSelectedIndex(int index) {
				if(index == 1) {
					inputPanel.storeData();
					resultPanel.updateData();
				}
				super.setSelectedIndex(index);
			}
		};
		mainPanel.add(mainTabPane, BorderLayout.CENTER);

		inputPanel = new InputPanel();
		mainTabPane.add(inputPanel);
		mainTabPane.setTitleAt(nMainTab++, "評価入力");

		
		resultPanel = new ResultPanel(evaluations);
		mainTabPane.add(resultPanel);
		mainTabPane.setTitleAt(nMainTab++, "評価結果");
		
		for(OverallEvaluation evaluation : evaluations.values()) {
			if(evaluation.getEvaluator().getUserName().equals(evaluator.getUserName())) {
				inputPanel.setEvaluation(evaluation);
			}
		}

		setMessage(mainPanel);
	}
	
	
	public void storeData() {
		inputPanel.storeData();
	}
	
	
	public boolean isDirty() {
		return inputPanel.isDirty();
	}
	

	class InputPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		private final Integer[] EVALUATION_SCALES = {2, 3, 4, 5, 6, 7, 8, 9, 10};
		private static final int EVALUATION_SCALE_POINT = 3; // 5-points-scale
		
		private JComboBox<Integer> evalScaleCB = new JComboBox<Integer>(EVALUATION_SCALES);
		private UIEvalElements evalElementsLeft = new UIEvalElements();
		private UIEvalElements evalElementsRight = new UIEvalElements();
		private JTextArea commentTextArea = new JTextArea(10, 30);
		private boolean isCommentDirty = false;

		public InputPanel() {
			setLayout(new BorderLayout(5, 5));
			
			JPanel optionPanel = new JPanel();
			JPanel evalPanel = new JPanel();
			add(optionPanel, BorderLayout.NORTH);
			add(evalPanel, BorderLayout.CENTER);

			optionPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			evalPanel.setLayout(new BorderLayout());
			
			evalPanel.setBorder(new BevelBorder(BevelBorder.RAISED));

			
			// optionPanel
			JLabel optionLabel = new JLabel("評価方法");
			optionPanel.add(optionLabel);
			optionPanel.add(evalScaleCB);
			
			evalScaleCB.setSelectedIndex(EVALUATION_SCALE_POINT);
			evalScaleCB.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					int nScalePoints = (Integer)evalScaleCB.getSelectedItem();
					evalElementsLeft.updateComboBox(nScalePoints);
					evalElementsRight.updateComboBox(nScalePoints);
				}
			});
			
		
			// evalPanel
			JPanel evalScorePanel = new JPanel();
			evalScorePanel.setBorder(new EmptyBorder(4, 4, 2, 2));
			JPanel evalCommentPanel = new JPanel();
			evalCommentPanel.setBorder(new EmptyBorder(2, 2, 2, 2));
			evalPanel.add(evalScorePanel, BorderLayout.CENTER);
			evalPanel.add(evalCommentPanel, BorderLayout.SOUTH);

			evalScorePanel.setLayout(new BorderLayout());
			evalCommentPanel.setLayout(new BorderLayout());
			
			//// evalScorePanel
			JLabel evalScoreLabel = new JLabel("評価項目");
			JPanel evalFormPanel = new JPanel();
			evalScorePanel.add(evalScoreLabel, BorderLayout.NORTH);
			evalScorePanel.add(evalFormPanel, BorderLayout.CENTER);
			
			JPanel evalFormPanelLeft = new JPanel();
			JPanel evalFormPanelRight = new JPanel();
			evalFormPanel.add(evalFormPanelLeft);
			evalFormPanel.add(evalFormPanelRight);
			evalFormPanel.setLayout(new GridLayout(1, 2));

			int selectedScale = (Integer)evalScaleCB.getSelectedItem();
			for(CommentType commentType : commentTypes) {
				String type = commentType.getType();
				if(!type.isBlank()) {
					UIEvalElement uie = new UIEvalElement(type, selectedScale);
					evalElementsLeft.add(uie);
				}
			}
			GroupLayout gl2 = Util.getGroupLayout(evalElementsLeft.buildUIArray(), evalFormPanelLeft);
			evalFormPanelLeft.setLayout(gl2);

			for(User discusser : discussers) {
				String name = discusser.getUserName();
				if(!name.isBlank()) {
					UIEvalElement uie = new UIEvalElement(name, selectedScale);
					evalElementsRight.add(uie);
				}
			}
			GroupLayout gl3 = Util.getGroupLayout(evalElementsRight.buildUIArray(), evalFormPanelRight);
			evalFormPanelRight.setLayout(gl3);
			
			//// evalCommentPanel
			JScrollPane commentScrollPane = new JScrollPane();
			evalCommentPanel.add(new JLabel("総合コメント"), BorderLayout.NORTH);
			evalCommentPanel.add(commentScrollPane, BorderLayout.CENTER);
			commentScrollPane.setViewportView(commentTextArea);
			commentTextArea.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void removeUpdate(DocumentEvent arg0) {
					isCommentDirty = true;
				}
				
				@Override
				public void insertUpdate(DocumentEvent arg0) {
					isCommentDirty = true;
				}
				
				@Override
				public void changedUpdate(DocumentEvent arg0) {
					isCommentDirty = true;
				}
			});
		}

		
		private String getComment() {
			return commentTextArea.getText();
		}
		
		private void setComment(String text) {
			boolean tempIsDirty = isCommentDirty;
			commentTextArea.setText(text);
			isCommentDirty = tempIsDirty;
		}

		
		private void setEvaluation(OverallEvaluation overallEvaluation) {
			for(UIEvalElement element : evalElementsLeft.values()) {
				Evaluation e = overallEvaluation.getEvaluation(OverallEvaluation.TAG_CATEGORY1, element.getName());
				if(e != null) {
					element.setEvaluation(e);
				}
			}

			for(UIEvalElement element : evalElementsRight.values()) {
				Evaluation e = overallEvaluation.getEvaluation(OverallEvaluation.TAG_CATEGORY2, element.getName());
				if(e != null) {
					element.setEvaluation(e);
				}
			}
			
			setComment(overallEvaluation.getComment());
		}
		
		
		public void storeData() {
			OverallEvaluation newOverallEvaluation = evaluations.containsKey(evaluator.getUserName()) ? evaluations.get(evaluator.getUserName()) : new OverallEvaluation(evaluator);
			newOverallEvaluation.setTimestamp();
			newOverallEvaluation.setComment(getComment());
			for(UIEvalElement element : evalElementsLeft.values()) {
				newOverallEvaluation.setEvaluation(OverallEvaluation.TAG_CATEGORY1, element.getName(), element.getScore(), element.getComment());
			}
			for(UIEvalElement element : evalElementsRight.values()) {
				newOverallEvaluation.setEvaluation(OverallEvaluation.TAG_CATEGORY2, element.getName(), element.getScore(), element.getComment());
			}
			
			evaluations.put(evaluator.getUserName(), newOverallEvaluation);
		}
		
		
		public boolean isDirty() {
			if(isCommentDirty) {
				return true;
			}
			
			for(UIEvalElement element : evalElementsLeft.values()) {
				if(element.isDirty()) {
					return true;
				}
			}

			for(UIEvalElement element : evalElementsRight.values()) {
				if(element.isDirty()) {
					return true;
				}
			}

			return false;
		}
	}

	
	class UIEvalElement {
		public static final int N_ELEMENTS = 3;
		
		private String name;
		private JLabel label;
		private NPointScaleComboBox comboBox;
		private CommentButton button;
		private boolean isDirty = false;
		
		public UIEvalElement(String name, int nScalePoints){
			this.name = name;
			this.label = new JLabel(name);
			this.comboBox = new NPointScaleComboBox(nScalePoints);
			this.button = new CommentButton("コメント");
			
			comboBox.addItemListener(new ItemListener() {
				
				@Override
				public void itemStateChanged(ItemEvent arg0) {
					isDirty = true;
				}
			});
		}
		
		public boolean isDirty() {
			return button.isDirty() ? true : isDirty;
		}
		
		public String getName() {
			return name;
		}
		

		public void setEvaluation(OverallEvaluation.Evaluation evaluation) {
			boolean tempIsDirty = isDirty;
			comboBox.setSelectedValue(evaluation.getScore());
			button.setComment(evaluation.getComment());
			isDirty = tempIsDirty;
		}

		
		
		public void setScalePoints(int n) {
			boolean tempIsDirty = isDirty;
			comboBox.setValues(n);
			isDirty = tempIsDirty;
		}
		
		
		public String getScore() {
			return (String) comboBox.getSelectedItem();
		}
		
		
		public String getComment() {
			return button.getComment();
		}
		
		public JComponent[] buildUIArray() {
			JComponent[] components = new JComponent[N_ELEMENTS];
			components[0] = label;
			components[1] = comboBox;
			components[2] = button;

			return components;
		}
	}
	
	
	class UIEvalElements extends HashMap<String, UIEvalElement> {
		private static final long serialVersionUID = 1L;


		public void add(UIEvalElement uiEvalElement) {
			put(uiEvalElement.getName(), uiEvalElement);
		}
		
		
		public JComponent[][] buildUIArray() {
			int n = size();

			JComponent[][] components = new JComponent[n][];

			int i = 0;
			for(UIEvalElement element : values()) {
				components[i++] = element.buildUIArray(); 
			}

			return components;
		}
		
		public void updateComboBox(int nScalePoints) {
			values().forEach((element)->{
				element.setScalePoints(nScalePoints);
			});
		}
		
		
		public boolean isDirty() {
			for(UIEvalElement uiEvalElement : values()) {
				if(uiEvalElement.isDirty()) {
					return true;
				}
			}
			
			return false;
		}
	}
	
	class ResultPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		private final String[] COLUMN_NAMES = {"評価者名", "観点", "値", "コメント"};
		
		private String[][] data = new String[0][COLUMN_NAMES.length];
		private HashMap<String, OverallEvaluation> overallEvaluations;
		private JTable table;

		public ResultPanel(HashMap<String, OverallEvaluation> overallEvaluations) {
			this.overallEvaluations = overallEvaluations;

			JPanel infoPanel = new JPanel(new BorderLayout());
			infoPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
			JTextArea info = new JTextArea(10, 30);
			infoPanel.add(info, BorderLayout.CENTER);
			
			table = new JTable() {
				private static final long serialVersionUID = 1L;
				public boolean isCellEditable(int row, int column) {                
					return false;
				}
			};
			table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent e) {
					if(e.getValueIsAdjusting()) {
						return;
					}
					
					int iSelectedRow = table.getSelectedRow();
					if(iSelectedRow > 0) {
					info.setText(
							"評価者: " + 	data[iSelectedRow][0] + "\n" +
							"観点: " + 	data[iSelectedRow][1] + "\n" +
							"値: " + data[iSelectedRow][2] + "\n" +
							"コメント:\n" + data[iSelectedRow][3]
							);
					}
				}
			});
			
			
			setLayout(new BorderLayout(5, 5));
			add(new JScrollPane(table), BorderLayout.CENTER);
			add(infoPanel, BorderLayout.SOUTH);
		
		}

		public void updateData() {
			int nEvaluation = 0;

			// count the number of evaluations
			for(OverallEvaluation overallEvaluation : overallEvaluations.values()) {
				for(String categoryName : OverallEvaluation.TAG_CATEGORIES) {
					EvaluationCategory category = overallEvaluation.getCategory(categoryName);
					nEvaluation += category.size();
				}
				nEvaluation++; // overall comment
			}

			data = new String[nEvaluation][COLUMN_NAMES.length];
			int i = 0;
			for(OverallEvaluation overallEvaluation : overallEvaluations.values()) {
				String evaluator = overallEvaluation.getEvaluator().getUserName();
				for(String categoryName : OverallEvaluation.TAG_CATEGORIES) {
					EvaluationCategory category = overallEvaluation.getCategory(categoryName);
					for(Evaluation evaluation : category.values()) {
						data[i][0] = evaluator;
						data[i][1] = evaluation.getName();
						data[i][2] = evaluation.getScore();
						data[i][3] = evaluation.getComment();
						i++;
					}
				}
				data[i][0] = evaluator;
				data[i][1] = "総合評価";
				data[i][2] = "";
				data[i][3] = overallEvaluation.getComment();
				i++;
			}
			table.setModel(new DefaultTableModel(data, COLUMN_NAMES));
		}
	}

	
	class CommentButton extends JButton {

		private static final long serialVersionUID = 1L;
		private static final int TEXT_AREA_ROWS = 8;
		private static final int TEXT_AREA_COLUMNS = 36;
		private JTextArea textArea = new JTextArea(TEXT_AREA_ROWS, TEXT_AREA_COLUMNS);
		private boolean isDirty = false;

		public CommentButton(String text) {
			super(text);
			addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					String tempText = getComment();
					JPanel p = new JPanel();
					p.setLayout(new BorderLayout());
					p.add(new JScrollPane(textArea), BorderLayout.CENTER);
					JOptionPane.showMessageDialog(null, p);
					if(!getComment().contentEquals(tempText)) {
						isDirty = true;
					}
				}
			});
		}

		
		public void setComment(String comment) {
			textArea.setText(comment);
		}
		
		public String getComment() {
			return Util.cleanText(textArea.getText());
		}
		
		public boolean isDirty() {
			return isDirty;
		}
	}
	

	
	class NPointScaleComboBox extends JComboBox<String> {

		private static final long serialVersionUID = 1L;
		private static final String UNENTERED_LABEL = "(未入力)";

		public NPointScaleComboBox(int n) {
			setValues(n);
			if(n < 2) {
				setEditable(false);
			}
		}
		
		public void setValues(int n) {
			DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>)getModel();
			ArrayList<String> scale = new ArrayList<String>();
			for(int i = 1; i <= n; i++) {
				scale.add(Integer.toString(i));
			}
			model.removeAllElements();
			model.addElement(UNENTERED_LABEL);
			model.addAll(scale);
		}
		
		public void setSelectedValue(String n) {
			for(int i = 0; i < getItemCount(); i++) {
				if(getItemAt(i).equals(n)) {
					setSelectedIndex(i);
				}
			}
		}
	}
}
