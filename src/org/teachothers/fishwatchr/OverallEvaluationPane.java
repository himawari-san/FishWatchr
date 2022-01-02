package org.teachothers.fishwatchr;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JTextArea;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;



public class OverallEvaluationPane extends JOptionPane {
	private static final long serialVersionUID = 1L;
	private ArrayList<CommentType> commentTypes;
	private ArrayList<User> discussers;
	private HashMap<String, OverallEvaluation> evaluations;


	public OverallEvaluationPane(HashMap<String, OverallEvaluation> evaluations, ArrayList<CommentType> commentTypes, ArrayList<User> discussers) {
		this.commentTypes = commentTypes;
		this.discussers = discussers;
		this.evaluations = evaluations;
		ginit();
	}

	
	private void ginit(){
		setOptions(new Object[0]); // remove the default OK button

		int nMainTab = 0;
		JTabbedPane mainTabPane = new JTabbedPane();

		mainTabPane.add(new InputPanel());
		mainTabPane.setTitleAt(nMainTab++, "評価入力");

		
		JTabbedPane resultTabPane = new JTabbedPane();
		mainTabPane.add(resultTabPane);
		mainTabPane.setTitleAt(nMainTab++, "評価結果");
		
		int nResultTabs = 0;
		for(OverallEvaluation evaluation : evaluations.values()) {
			resultTabPane.add(new ResultPanel(evaluation));
			resultTabPane.setTitleAt(nResultTabs++, evaluation.getEvaluatorName());
		}
		resultTabPane.add(new JPanel()); // dummy
		resultTabPane.setTitleAt(nResultTabs++, "Summary");
		
		setMessage(mainTabPane);
//		setPreferredSize(new Dimension(600, 800));
		
	}

	class InputPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		private final Integer[] EVALUATION_SCALES = {2, 3, 4, 5, 6, 7, 8, 9, 10};
		private static final int EVALUATION_SCALE_POINT = 3; // 5-points-scale
		
		private JComboBox<Integer> evalScaleCB = new JComboBox<Integer>(EVALUATION_SCALES);
		private UIEvalElements evalElementsLeft = new UIEvalElements();
		private UIEvalElements evalElementsRight = new UIEvalElements();

		public InputPanel() {
			setLayout(new BorderLayout(5, 5));
			
			JPanel optionPanel = new JPanel();
			JPanel evalPanel = new JPanel();
			JPanel buttonPanel = new JPanel();
			add(optionPanel, BorderLayout.NORTH);
			add(evalPanel, BorderLayout.CENTER);
			add(buttonPanel, BorderLayout.SOUTH);

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
			JPanel evalCommentPanel = new JPanel();
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

			int nItemsCommentType = commentTypes.size();
			for(int i = 0; i < nItemsCommentType; i++) {
				UIEvalElement uie = new UIEvalElement(commentTypes.get(i).getType(), (Integer)evalScaleCB.getSelectedItem());
				evalElementsLeft.add(uie);
			}
			GroupLayout gl2 = Util.getGroupLayout(evalElementsLeft.buildUIArray(), evalFormPanelLeft);
			evalFormPanelLeft.setLayout(gl2);

			int nItemsDiscussor = discussers.size();
			for(int i = 0; i < nItemsDiscussor; i++) {
				UIEvalElement uie = new UIEvalElement(discussers.get(i).getName(), (Integer)evalScaleCB.getSelectedItem());
				evalElementsRight.add(uie);
			}
			GroupLayout gl3 = Util.getGroupLayout(evalElementsRight.buildUIArray(), evalFormPanelRight);
			evalFormPanelRight.setLayout(gl3);
			
			//// evalCommentPanel
			JScrollPane commentScrollPane = new JScrollPane();
			evalCommentPanel.add(new JLabel("総合コメント"), BorderLayout.NORTH);
			evalCommentPanel.add(commentScrollPane, BorderLayout.CENTER);
			commentScrollPane.setViewportView(new JTextArea(10, 30));


			// buttonPanel
			JButton saveButton = new JButton("保存");
			saveButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					// TODO Auto-generated method stub
				}
			});
			buttonPanel.add(saveButton);
		}
		
	}

	
	class UIEvalElement {
		public static final int N_ELEMENTS = 3;
		
		String name;
		JLabel label;
		NPointScaleComboBox comboBox;
		CommentButton button;
		
		public UIEvalElement(String name, int nScalePoints){
			this.name = name;
			this.label = new JLabel(name);
			this.comboBox = new NPointScaleComboBox(nScalePoints);
			this.button = new CommentButton("コメント");
		}
		
		public String getName() {
			return name;
		}
		
		public void setEvaluation(OverallEvaluation.Evaluation evaluation) {
			comboBox.setFixedValue(evaluation.getScore());
			button.setComment(evaluation.getComment());
		}
		
		
		public void setScalePoints(int n) {
			comboBox.setValues(n);
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
	}
	
	class ResultPanel extends JPanel {
		private static final long serialVersionUID = 1L;

		private UIEvalElements evalElementsLeft = new UIEvalElements();
		private UIEvalElements evalElementsRight = new UIEvalElements();

		public ResultPanel(OverallEvaluation overallEvaluation) {
			GridBagLayout gbl = new GridBagLayout();
			GridBagConstraints gbc = new GridBagConstraints();
			setLayout(gbl);
			
			JPanel commentPanel = new JPanel();
			commentPanel.setLayout(new BorderLayout(2, 4));
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.weightx = 0d;
			gbc.weighty = 0d;
			gbc.anchor = GridBagConstraints.NORTHWEST;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbl.setConstraints(commentPanel, gbc);
			add(commentPanel);
			JLabel commentLabel = new JLabel("総合コメント");
			commentPanel.add(commentLabel, BorderLayout.NORTH);

			JScrollPane commentScrollPane = new JScrollPane();
			JTextArea commentArea = new JTextArea(10, 30);
			commentArea.setText(overallEvaluation.getComment());
			commentScrollPane.setViewportView(commentArea);
			commentPanel.add(commentScrollPane, BorderLayout.CENTER);
			commentPanel.setBorder(new BevelBorder(BevelBorder.RAISED));

			JPanel evalPanel = new JPanel();
			evalPanel.setLayout(new BorderLayout());
			evalPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED));
			gbc.gridx = 0;
			gbc.gridy = 1;
			gbc.weightx = 1.0d;
			gbc.weighty = 1.0d;
			gbc.anchor = GridBagConstraints.SOUTHWEST;
			gbc.fill = GridBagConstraints.BOTH;
			gbl.setConstraints(evalPanel, gbc);
			add(evalPanel);
			
			JLabel evalLabel = new JLabel("評価項目");
			evalPanel.add(evalLabel, BorderLayout.NORTH);

			JPanel evalFormPanel = new JPanel();
			evalFormPanel.setLayout(new GridLayout(1, 2));
			evalPanel.add(evalFormPanel, BorderLayout.CENTER);
			
			JPanel evalFormPanelLeft = new JPanel();
			JPanel evalFormPanelRight = new JPanel();
			evalFormPanel.add(evalFormPanelLeft);
			evalFormPanel.add(evalFormPanelRight);
			
			for(String evaluationName : overallEvaluation.getEvaluationNames(OverallEvaluation.TAG_CATEGORY1)) {
				UIEvalElement uie = new UIEvalElement(evaluationName, 0);
				uie.setEvaluation(overallEvaluation.getEvaluation(OverallEvaluation.TAG_CATEGORY1, evaluationName));
				evalElementsLeft.add(uie);
			}
			GroupLayout gl2 = Util.getGroupLayout(evalElementsLeft.buildUIArray(), evalFormPanelLeft);
			evalFormPanelLeft.setLayout(gl2);

			for(String evaluationName : overallEvaluation.getEvaluationNames(OverallEvaluation.TAG_CATEGORY2)) {
				UIEvalElement uie = new UIEvalElement(evaluationName, 0);
				uie.setEvaluation(overallEvaluation.getEvaluation(OverallEvaluation.TAG_CATEGORY2, evaluationName));
				evalElementsRight.add(uie);
			}
			GroupLayout gl3 = Util.getGroupLayout(evalElementsRight.buildUIArray(), evalFormPanelRight);
			evalFormPanelRight.setLayout(gl3);
		}
	}

	
	class CommentButton extends JButton {

		private static final long serialVersionUID = 1L;
		private static final int TEXT_AREA_ROWS = 8;
		private static final int TEXT_AREA_COLUMNS = 36;
		private JTextArea textArea = new JTextArea(TEXT_AREA_ROWS, TEXT_AREA_COLUMNS);

		public CommentButton(String text) {
			super(text);
			addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					JPanel p = new JPanel();
					p.setLayout(new BorderLayout());
					p.add(new JScrollPane(textArea), BorderLayout.CENTER);
					JOptionPane.showMessageDialog(null, p);
				}
			});
		}

		
		public void setComment(String comment) {
			textArea.setText(comment);
		}
		
		public String getComment() {
			return textArea.getText();
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
		
		public void setFixedValue(String n) {
			if(getItemCount() == 1) {
				DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>)getModel();
				model.removeAllElements();
				model.addElement(n);
			}
			// TODO
		}
	}
}
