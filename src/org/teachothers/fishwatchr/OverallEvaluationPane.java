package org.teachothers.fishwatchr;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;




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
	}

	class InputPanel extends JPanel {
		private static final long serialVersionUID = 1L;

		public InputPanel() {
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			
			JPanel optionPanel = new JPanel();
			JLabel optionLabel = new JLabel("評価方法");
			JComboBox<String> evalMethodCB = new JComboBox<String>(new String[] {"自由記述", "5段階"});
			optionPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			optionPanel.add(optionLabel);
			optionPanel.add(evalMethodCB);
			
			JPanel evalLabelPanel = new JPanel();
			JLabel evalLabel = new JLabel("評価項目");
			evalLabelPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			evalLabelPanel.add(evalLabel);
			JPanel evalFormPanel = new JPanel();
			int nItemsCommentType = commentTypes.size();
			int nItemsDiscussor = discussers.size();
			int nItems = nItemsCommentType > nItemsDiscussor ? nItemsCommentType : nItemsDiscussor;

			JComponent[][] evalItems = new JComponent[nItems][5];
			for(int i = 0; i < nItemsCommentType; i++) {
				evalItems[i][0] = new JLabel(commentTypes.get(i).getType()); 
				evalItems[i][1] = new JTextField();
				evalItems[i][2] = new JLabel();
			}
			
			for(int i = 0; i < nItemsDiscussor; i++) {
				evalItems[i][3] = new JLabel(discussers.get(i).getName()); 
				evalItems[i][4] = new JTextField();
			}
			
			GroupLayout gl2 = Util.getGroupLayout(evalItems, evalFormPanel);
			evalFormPanel.setLayout(gl2);

			JPanel commentLabelPanel = new JPanel();
			commentLabelPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			commentLabelPanel.add(new JLabel("コメント"));
			JScrollPane commentScrollPane = new JScrollPane();
			commentScrollPane.setViewportView(new JTextArea(10, 30));
			
			add(optionPanel);
			add(evalLabelPanel);
			add(evalFormPanel);
			add(commentLabelPanel);
			add(commentScrollPane);
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
//			evalFormPanel.setPreferredSize(new Dimension(Short.MAX_VALUE, 100));
			int nItemsCommentType = commentTypes.size();
			int nItemsDiscussor = discussers.size();
			int nItems = nItemsCommentType > nItemsDiscussor ? nItemsCommentType : nItemsDiscussor;
			
//			JPanel targetsPanel = new JPanel();
//			targetsPanel.setLayout(new BoxLayout(targetsPanel, BoxLayout.X_AXIS));
//
//			JPanel target1Panel = new JPanel();
//			target1Panel.setLayout(new BoxLayout(target1Panel, BoxLayout.Y_AXIS));
//			for(int i = 0; i < nItemsCommentType; i++) {
//				String item = commentTypes.get(i).getType();
//				target1Panel.add(new JLabel(item)); 
//			}
			
			JComponent[][] evalItems = new JComponent[nItems][5];
			for(int i = 0; i < nItemsCommentType; i++) {
				String item = commentTypes.get(i).getType();
				evalItems[i][0] = new JLabel(item); 
				evalItems[i][0].setMaximumSize(new Dimension(Short.MAX_VALUE, evalItems[i][0].getMaximumSize().height));
				evalItems[i][1] = new JLabel(evaluation.getTarget1(item));
				evalItems[i][1].setMaximumSize(new Dimension(Short.MAX_VALUE, evalItems[i][1].getMaximumSize().height));
				evalItems[i][2] = new JLabel();
			}
			
			for(int i = 0; i < nItemsDiscussor; i++) {
				String item = discussers.get(i).getName();
				evalItems[i][3] = new JLabel(item);
				evalItems[i][3].setMaximumSize(new Dimension(Short.MAX_VALUE, evalItems[i][3].getMaximumSize().height));
				evalItems[i][4] = new JLabel(evaluation.getTarget2(item));
				evalItems[i][4].setMaximumSize(new Dimension(Short.MAX_VALUE, evalItems[i][4].getMaximumSize().height));
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

}
