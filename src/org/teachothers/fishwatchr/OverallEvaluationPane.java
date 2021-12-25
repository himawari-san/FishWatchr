package org.teachothers.fishwatchr;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import javax.swing.BoxLayout;
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
		setPreferredSize(new Dimension(600, 800));
	}

	class InputPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		private final Integer[] EVALUATION_SCALES = {2, 3, 4, 5, 6, 7, 8, 9, 10};
		private static final int EVALUATION_SCALE_POINT = 3; // 5-points-scale
		
		private ArrayList<NPointScaleComboBox> evalComboBoxes = new ArrayList<NPointScaleComboBox>();
		private JComboBox<Integer> evalScaleCB = new JComboBox<Integer>(EVALUATION_SCALES);

		public InputPanel() {
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			
			JPanel optionPanel = new JPanel();
			JLabel optionLabel = new JLabel("評価方法");
			evalScaleCB.setSelectedIndex(EVALUATION_SCALE_POINT);
			evalScaleCB.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					updateComboBoxes();
				}
			});
			optionPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			optionPanel.add(optionLabel);
			optionPanel.add(evalScaleCB);
			
			JPanel evalLabelPanel = new JPanel();
			JLabel evalLabel = new JLabel("評価項目");
			evalLabelPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			evalLabelPanel.add(evalLabel);
			JPanel evalFormPanel = new JPanel();
			int nItemsCommentType = commentTypes.size();
			int nItemsDiscussor = discussers.size();
			int nItems = nItemsCommentType > nItemsDiscussor ? nItemsCommentType : nItemsDiscussor;

			JComponent[][] evalItems = new JComponent[nItems][7];
			for(int i = 0; i < nItemsCommentType; i++) {
				evalItems[i][0] = new JLabel(commentTypes.get(i).getType()); 
				evalItems[i][1] = new NPointScaleComboBox((Integer)evalScaleCB.getSelectedItem());
				evalItems[i][2] = new CommentButton("コメント");
				evalItems[i][3] = new JLabel();
				
				evalComboBoxes.add((NPointScaleComboBox)evalItems[i][1]);
			}
			
			for(int i = 0; i < nItemsDiscussor; i++) {
				evalItems[i][4] = new JLabel(discussers.get(i).getName()); 
				evalItems[i][5] = new NPointScaleComboBox((Integer)evalScaleCB.getSelectedItem());
				evalItems[i][6] = new CommentButton("コメント");

				evalComboBoxes.add((NPointScaleComboBox)evalItems[i][5]);
			}
			
			GroupLayout gl2 = Util.getGroupLayout(evalItems, evalFormPanel);
			evalFormPanel.setLayout(gl2);

			JPanel commentLabelPanel = new JPanel();
			commentLabelPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			commentLabelPanel.add(new JLabel("総合コメント"));
			JScrollPane commentScrollPane = new JScrollPane();
			commentScrollPane.setViewportView(new JTextArea(10, 30));
			
			JPanel buttonPanel = new JPanel();
			buttonPanel.add(new JButton("保存"));
			
			add(optionPanel);
			add(evalLabelPanel);
			add(evalFormPanel);
			add(commentLabelPanel);
			add(commentScrollPane);
			add(buttonPanel);
		}
		
		public void updateComboBoxes() {
			evalComboBoxes.forEach((comboBox)->{
				comboBox.setValues((Integer)evalScaleCB.getSelectedItem());
			});
		}
		
	}

	
	class ResultPanel extends JPanel {
		private static final long serialVersionUID = 1L;

		public ResultPanel(OverallEvaluation evaluation) {
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			
			JPanel evalLabelPanel = new JPanel();
			JLabel evalLabel = new JLabel("評価項目");
			evalLabelPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			evalLabelPanel.add(evalLabel);
			JPanel evalFormPanel = new JPanel();
			int nItemsCommentType = commentTypes.size();
			int nItemsDiscussor = discussers.size();
			int nItems = nItemsCommentType > nItemsDiscussor ? nItemsCommentType : nItemsDiscussor;
			
			Random r = new Random();
			JComponent[][] evalItems = new JComponent[nItems][7];
			for(int i = 0; i < nItemsCommentType; i++) {
				String item = commentTypes.get(i).getType();
				evalItems[i][0] = new JLabel(item); 
				evalItems[i][0].setMaximumSize(new Dimension(Short.MAX_VALUE, evalItems[i][0].getMaximumSize().height));
				evalItems[i][1] = new JLabel(evaluation.getTarget1(item));
				evalItems[i][1].setMaximumSize(new Dimension(Short.MAX_VALUE, evalItems[i][1].getMaximumSize().height));
				evalItems[i][2] = new CommentButton("コメント");
				evalItems[i][3] = new JLabel();
			}
			
			for(int i = 0; i < nItemsDiscussor; i++) {
				String item = discussers.get(i).getName();
				evalItems[i][4] = new JLabel(item);
				evalItems[i][4].setMaximumSize(new Dimension(Short.MAX_VALUE, evalItems[i][3].getMaximumSize().height));
				evalItems[i][5] = new JLabel(String.valueOf(r.nextInt(5)+1));
				evalItems[i][5].setMaximumSize(new Dimension(Short.MAX_VALUE, evalItems[i][4].getMaximumSize().height));
				evalItems[i][6] = new CommentButton("コメント");
			}
			
			GroupLayout gl2 = Util.getGroupLayout(evalItems, evalFormPanel);
			evalFormPanel.setLayout(gl2);

			JPanel commentLabelPanel = new JPanel();
			commentLabelPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			JLabel commentLabel = new JLabel("コメント");
			commentLabelPanel.add(commentLabel);
			JScrollPane commentScrollPane = new JScrollPane();
			JTextArea coomentArea = new JTextArea(evaluation.getComment(), 10, 30);
			commentScrollPane.setViewportView(coomentArea);
			JPanel dummy = new JPanel();
			JPanel grue = new JPanel();
			grue.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
			dummy.add(grue);
			
			add(commentLabelPanel);
			add(commentScrollPane);
			add(evalLabelPanel);
			add(evalFormPanel);
			add(dummy);
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
		
		public String getComment() {
			return textArea.getText();
		}
	}
	
	
	class NPointScaleComboBox extends JComboBox<String> {

		private static final long serialVersionUID = 1L;
		private static final String UNENTERED_LABEL = "(未入力)";

		public NPointScaleComboBox(int n) {
			setValues(n);
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

	}

}
