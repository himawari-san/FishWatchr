/*
    Copyright (C) 2014-2019 Masaya YAMAGUCHI

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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventObject;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;


public class CommentTable extends JTable {

	private static final long serialVersionUID = 1L;
	private static final String LABEL_FILTER_CANCEL = Messages.getString("CommentTable.0"); //$NON-NLS-1$
	private static final String LABEL_KEY_SET = Messages.getString("CommentTable.1"); //$NON-NLS-1$
	private static final int MAX_POPUP_ITEMS = 15;
	private static final String ANNOTATOR_MARKER_OPEN = "<<"; //$NON-NLS-1$
	private static final String ANNOTATOR_MARKER_CLOSE = ">>"; //$NON-NLS-1$
	public static final int UNDEFINED = -1;

	public CommentTableModel ctm;
	private JPopupMenu popupMenu = new JPopupMenu();
	private int iCurrentComment = UNDEFINED;
	private StringBuffer currentCommentBuffer = new StringBuffer();
	
	private JMenuItem menuItemStat = new JMenuItem(Messages.getString("CommentTable.2")); //$NON-NLS-1$
	private JMenuItem menuItemDelete = new JMenuItem(Messages.getString("CommentTable.3")); //$NON-NLS-1$
	private JMenuItem menuItemCellDelete = new JMenuItem(Messages.getString("CommentTable.4")); //$NON-NLS-1$
	
	private String currentAnnotator = ""; //$NON-NLS-1$
	private boolean enableAutoFillAnnotatorName = false;

	public CommentTable(CommentTableModel ctm){
		super(ctm);
		this.ctm = ctm;
		ginit();
		setSurrendersFocusOnKeystroke(true);
	}
	

	public void initState(){
		iCurrentComment = UNDEFINED;
		ctm.clearFilter();
		ctm.selectTimePeriod(null);
		ctm.refreshFilter();
	}
	
	public void ginit(){
		setOpaque(false);
		setDefaultRenderer(Object.class, new CellRenderer());
		setDefaultRenderer(Number.class, new CellRenderer(SwingConstants.RIGHT));
		getColumn(ctm.getColumnName(Comment.F_COMMENT_TARGET)).setCellEditor(new ListCellEditor<User>(ctm.discussers));
		getColumn(ctm.getColumnName(Comment.F_COMMENT_LABEL)).setCellEditor(new ListCellEditor<CommentType>(ctm.commentTypes));
		getColumn(ctm.getColumnName(Comment.F_COMMENT)).setCellEditor(new TextCellEditor());
		getColumn(ctm.getColumnName(Comment.F_AUX)).setCellEditor(new TextCellEditor());
		setCellSelectionEnabled(true);
		
		
		menuItemStat.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int[] iSelectedColumns = getSelectedColumns();
				if(iSelectedColumns.length == 0){
					JOptionPane.showMessageDialog(null,  Messages.getString("CommentTable.5")); //$NON-NLS-1$
					return;
				}
				if(ctm.getFilteredCommentList().size() == 0){
					JOptionPane.showMessageDialog(null,  Messages.getString("CommentTable.6")); //$NON-NLS-1$
					return;
				}
				
				String headers[] = new String[iSelectedColumns.length];
				for(int i = 0; i < iSelectedColumns.length; i++){
					headers[i] = ctm.getColumnName(iSelectedColumns[i]);
				}

				DataCounter dc = new DataCounter(ctm.getFilteredCommentList(), iSelectedColumns, ""); //$NON-NLS-1$
				StatFrame sf = new StatFrame(dc.getSummary(DataCounter.SUMMARY_MODE_ALL_COMPARE), headers);
				if(!sf.showChart(StatFrame.CHART_STYLE_UNIQ)){
					JOptionPane.showMessageDialog(null,  Messages.getString("CommentTable.8")); //$NON-NLS-1$
					return;
				}
				sf.pack();
				sf.setTitle(Messages.getString("CommentTable.9") + StatFrame.LABEL_STYLE_UNIQ + "/" + DataCounter.SUMMARY_MODE_ALL); //$NON-NLS-1$ //$NON-NLS-2$
				sf.setVisible(true);
			}
		});
		
		menuItemDelete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int selectedRow = CommentTable.this.getSelectedRow();
				int selectedValue = JOptionPane.showConfirmDialog(null,
						Messages.getString("CommentTable.16") + (selectedRow+1) + Messages.getString("CommentTable.11"), //$NON-NLS-1$ //$NON-NLS-2$
						Messages.getString("CommentTable.12"), JOptionPane.OK_CANCEL_OPTION); //$NON-NLS-1$ //$NON-NLS-2$
				if(selectedValue == JOptionPane.OK_OPTION){
					deleteComment(selectedRow);
				}
			}
		});
		menuItemCellDelete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int selectedRow = CommentTable.this.getSelectedRow();
				int selectedColumn = CommentTable.this.getSelectedColumn();
				
				if(!ctm.isCellEditable(selectedRow, selectedColumn)){
					JOptionPane.showMessageDialog(CommentTable.this, Messages.getString("CommentTable.13")); //$NON-NLS-1$
					return;
				}
				int selectedValue = JOptionPane.showConfirmDialog(null, Messages.getString("CommentTable.14"), Messages.getString("CommentTable.15"), JOptionPane.OK_CANCEL_OPTION); //$NON-NLS-1$ //$NON-NLS-2$
				if(selectedValue == JOptionPane.OK_OPTION){
					Object o = getValueAt(selectedRow, selectedColumn);
					if(o instanceof User){
						setValueAt(new User(""),  selectedRow, selectedColumn); //$NON-NLS-1$
					} else if(o instanceof CommentType){
						setValueAt(new CommentType("", Color.BLACK),  selectedRow, selectedColumn); //$NON-NLS-1$
					} else if(o instanceof String){
						setValueAt("",  selectedRow, selectedColumn); //$NON-NLS-1$
					}
				}
			}
		});

		
		popupMenu.add(menuItemStat);
		popupMenu.addSeparator();
		popupMenu.add(menuItemDelete);
		popupMenu.add(menuItemCellDelete);
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				// コメントリストから行の削除
				if(e.getButton() == MouseEvent.BUTTON3 && CommentTable.this.getSelectedColumnCount() > 0){
					popupMenu.show(CommentTable.this, e.getX(), e.getY());
				}
			}
		});
		
		getTableHeader().addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e) {
				// 左ボタンクリック(ソート)
				int columnIndex = getTableHeader().columnAtPoint(e.getPoint());
				// ポイントしているヘッダの名前を取得
				String headerName = (String) getTableHeader().getColumnModel()
						.getColumn(columnIndex).getHeaderValue();

				invokeDialogForFiltering(headerName, e.getComponent(), e.getX(), e.getY());
			}
		});
	}
	
	
	public JPopupMenu getPopupMenu(){
		return popupMenu;
	}
	
	
	public void setPopupMenuEnable(boolean flag){
		popupMenu.getComponent(1).setEnabled(flag);
		// 2: separator
		popupMenu.getComponent(3).setEnabled(flag);
		popupMenu.getComponent(4).setEnabled(flag);
	}

	
	public StatFrame invokeStatFrame(int style){
		int[] iSelectedColumns = getSelectedColumns();
		String headers[] = new String[iSelectedColumns.length];
		for(int i = 0; i < iSelectedColumns.length; i++){
			headers[i] = ctm.getColumnName(iSelectedColumns[i]);
		}

		DataCounter dc = new DataCounter(ctm.getFilteredCommentList(), iSelectedColumns, ""); //$NON-NLS-1$
		StatFrame sf = new StatFrame(dc.getSummary(), headers);
		sf.showChart(style);
		sf.setSize(400,  200);
		sf.setTitle(Messages.getString("CommentTable.20")); //$NON-NLS-1$
		sf.setVisible(true);
		
		return sf;
	}

	
	void invokeDialogForFiltering(String headerName, Component component,
			int x, int y) {
		JPopupMenu popupMenu = new JPopupMenu();
		CommentList commentList = ctm.getCommentList();
		
		if(commentList.size() == 0){
			return;
		}

		ArrayList<String> itemList = new ArrayList<String>();

		itemList.addAll(ctm.getItemList(headerName));

		// フィルター解除用item追加
		int fromIndexForSorting = 0;
		if(ctm.isFiltered()){
			itemList.add(0, LABEL_FILTER_CANCEL);
			fromIndexForSorting++;
		}
		// 文字列検索用item追加
		if (itemList.size() != 0) {
			itemList.add(fromIndexForSorting, LABEL_KEY_SET);
			fromIndexForSorting++;
		}

		// item のソート
		Object[] itemObjects = itemList.toArray();
		Arrays.sort(itemObjects, fromIndexForSorting, itemObjects.length);

		String selectedValue = null;
		if (itemObjects.length <= MAX_POPUP_ITEMS) {
			// ポップアップメニューの場合
			for (int i = 0; i < itemList.size(); i++) {
				JMenuItem menuItem = new JMenuItem((String) itemObjects[i]);
				menuItem.addMouseListener(new CommentTableHeaderPopupMouseAdapter(ctm, headerName));
				popupMenu.add(menuItem);
			}
			
			popupMenu.setPreferredSize(new Dimension(Math.min(400, popupMenu.getPreferredSize().width), popupMenu.getPreferredSize().height));
			popupMenu.show(component, x, y);
		} else {
			JList<Object> list = new JList<Object>(itemObjects);
			JOptionPane op = new JOptionPane(new JScrollPane(list));
			op.setMessageType(JOptionPane.INFORMATION_MESSAGE);
			op.setOptionType(JOptionPane.OK_CANCEL_OPTION);
			op.setIcon(null);
			final JDialog jd = op.createDialog(component, Messages.getString("CommentTable.21")); //$NON-NLS-1$
			list.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if(e.getClickCount() == 2){
						jd.setVisible(false);
					}
				}
			});
			jd.setLocation(x, (int)component.getLocationOnScreen().getY());
			jd.setPreferredSize(new Dimension(Math.min(400, jd.getPreferredSize().width), Math.min(400, jd.getPreferredSize().height)));
			jd.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			jd.setVisible(true);
			selectedValue = (String) list.getSelectedValue();
			if(selectedValue == null || selectedValue.equals(JOptionPane.UNINITIALIZED_VALUE)){
				return;
			} else if(selectedValue.equals(LABEL_FILTER_CANCEL)){
				ctm.clearFilter();
			} else if(selectedValue.equals(LABEL_KEY_SET)){
				String inputValue = JOptionPane.showInputDialog(
						null, Messages.getString("CommentTable.22"), Messages.getString("CommentTable.23"), //$NON-NLS-1$ //$NON-NLS-2$
						JOptionPane.PLAIN_MESSAGE);
				if (inputValue != null) {
					ctm.addFilter(headerName, inputValue);
				}
			} else {
				ctm.addFilter(headerName, selectedValue);
			}
			resetPosition();
			ctm.refreshFilter();
		}
	}

	
	public void deleteComment(int row){
		ctm.deleteCommentAt(row);
		resetPosition();
	}
	
	
	public void resetPosition(){
		iCurrentComment = UNDEFINED;
	}

	
	public void indicateCurrentComment(long msec, long range){
		CommentList commentList = ctm.getCommentList();
		ArrayList<Comment> filteredCommentList = ctm.getFilteredCommentList();
		int commentTime = 0;
		
		iCurrentComment = UNDEFINED;
		
		for(int i = 0; i < filteredCommentList.size(); i++){
			Comment comment = filteredCommentList.get(i);
			commentTime = commentList.unifiedCommentTime(comment);
			comment.setFocused(false);
			if(commentTime <= msec){
				iCurrentComment = i;
			}
		}
		if(iCurrentComment != UNDEFINED){
			int currentCommentTime = commentList.unifiedCommentTime(filteredCommentList.get(iCurrentComment));
			for(int i = iCurrentComment+1; i < filteredCommentList.size(); i++){
				Comment comment = filteredCommentList.get(i);
				if(commentList.unifiedCommentTime(comment) - currentCommentTime < range){
					comment.setFocused(true);
				} else {
					break;
				}
			}
			for(int i = iCurrentComment-1; i >= 0; i--){
				Comment comment = filteredCommentList.get(i);
				if(currentCommentTime - commentList.unifiedCommentTime(comment) < range){
					comment.setFocused(true);
				} else {
					break;
				}
			}
			repaint();
		}
	}
	
	
	
	public int getCurrentCommentPosition(){
		return iCurrentComment;
	}

	
	public String getCurrentComment(){
		if(iCurrentComment != -1 && ctm.getFilteredCommentList().size() > iCurrentComment){
			currentCommentBuffer.setLength(0);
			Comment currentComment = ctm.getFilteredCommentList().get(iCurrentComment);
			currentCommentBuffer.append(currentComment.getDiscusser().getName() + "("); //$NON-NLS-1$
			currentCommentBuffer.append(currentComment.getCommentType().getType() + "), "); //$NON-NLS-1$
			currentCommentBuffer.append(currentComment.getCommenter().getName() + ": "); //$NON-NLS-1$
			currentCommentBuffer.append(currentComment.getCommentBody());
			return currentCommentBuffer.toString();
		} else {
			return ""; //$NON-NLS-1$
		}
	}
	
	public int getNearCommentPosition(long msec, boolean isForward){
		CommentList commentList = ctm.getCommentList();
		ArrayList<Comment> filteredCommentList = ctm.getFilteredCommentList();
		int nComment = filteredCommentList.size();
		
		for(int i = 0; i < nComment; i++){
			Comment comment = filteredCommentList.get(i);
			int commentTime = commentList.unifiedCommentTime(comment);
			if(commentTime > msec){
				if(isForward){
					return i;
				} else if(i != 0){
					return i-1;
				} else {
					return UNDEFINED;
				}
			}
		}
		if(isForward){
			return UNDEFINED;
		} else {
			return nComment-1;
		}
		
	}


	public void setViewCenteraByIndex(int commentIndex){		
		Rectangle targetRect = getCellRect(commentIndex, 0, true);
		Rectangle viewRect = getVisibleRect();
		int dy = viewRect.height / 2;
		viewRect.setLocation(targetRect.x, targetRect.y	- dy);
		scrollRectToVisible(viewRect);
	}

	
	public void setViewCenterByTime(long msec){
		// 現在の行が表の真ん中になるように調整
		int targetCommentPosition = getCurrentCommentPosition();
		if(targetCommentPosition == UNDEFINED){
			targetCommentPosition = getNearCommentPosition(msec, false);
			if(targetCommentPosition == UNDEFINED){
				return;
			}
		}
		
		setViewCenteraByIndex(targetCommentPosition);
	}
	
	
	public void setAnnotator(String annotator) {
		currentAnnotator = annotator;
	}
	
	
	public void setAutoFillAnnotatorName(boolean flag) {
		enableAutoFillAnnotatorName = flag;
	}

	
	public class CellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;
		private final Color colorCurrent = new Color(0xE0, 0xE0, 0xFF);
		private final Color colorNeighbors = new Color(0xFF, 0xF8, 0xDC);
		
		public CellRenderer(){
			super();
			setBorder(BorderFactory.createEmptyBorder());
		}

		public CellRenderer(int align){
			super();
			setBorder(BorderFactory.createEmptyBorder());
			setHorizontalAlignment(align);
		}

		
		public Component getTableCellRendererComponent(JTable table, Object value,
	            boolean isSelected, boolean hasFocus, int row, int column) {

			setForeground(Color.BLACK); // MacOS
			if(row == iCurrentComment){
				setBackground(colorCurrent);
			} else {
				Comment comment = ctm.getFilteredCommentList().get(row);
				if(comment.isFocused()){
					setBackground(colorNeighbors);
				} else {
					setBackground(CommentTable.this.getBackground());
				}
			}
			
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

			return this;
		}
	}

	
	public class TextCellEditor extends AbstractCellEditor implements TableCellEditor {

		private static final long serialVersionUID = 1L;
		private Object value;
		JTextField textField;
		
		public TextCellEditor(){
			textField = new JTextField();
			textField.setBorder(BorderFactory.createEmptyBorder());
			textField.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if(e.getClickCount() == 2){
						showTextAreaDialog();
					}
				}
				
			});
			textField.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if(e.isControlDown() && e.getKeyCode() == KeyEvent.VK_ENTER){
						showTextAreaDialog();
					}
				}
			});
		}
		
		public void showTextAreaDialog(){
			if(currentAnnotator.isEmpty() && enableAutoFillAnnotatorName) {
				JOptionPane.showMessageDialog(CommentTable.this, Messages.getString("CommentTable.10")); //$NON-NLS-1$
				currentAnnotator = MainFrame.USER_NOT_SPECIFIED;
			}
			
			JScrollPane scrollPane = new JScrollPane();
			String cellText = textField.getText();
			
			if(enableAutoFillAnnotatorName) {
				if(!cellText.isEmpty()) {
					cellText += Comment.LINEBREAK;
				}
				cellText += ANNOTATOR_MARKER_OPEN + currentAnnotator + ANNOTATOR_MARKER_CLOSE;
			}
			
			final JTextArea textArea = new JTextArea(cellText.replaceAll(Comment.LINEBREAK, "\n"), 20, 50); //$NON-NLS-1$
			textArea.setLineWrap(true);
			scrollPane.add(textArea);
			textArea.setCaretPosition(textArea.getText().length());

			JOptionPane op = new JOptionPane(new JScrollPane(textArea), JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION){
				private static final long serialVersionUID = 1L;

				@Override
				public void selectInitialValue() {
					// cancel super.selectInitialValue() to focus on the textArea
					// see http://www.ne.jp/asahi/hishidama/home/tech/java/swing/JOptionPane.html
				}
			};
			JDialog d = op.createDialog(Messages.getString("CommentTable.29")); //$NON-NLS-1$
			d.setVisible(true);
			Object selectedValue = op.getValue();
			
			if(selectedValue != null && ((Integer)selectedValue) == JOptionPane.OK_OPTION){
				textField.setText(textArea.getText().replaceAll("\n", Comment.LINEBREAK)); //$NON-NLS-1$
			}
		}
		
		@Override
		public Object getCellEditorValue() {
			return value;
		}

		@Override
		public boolean isCellEditable(EventObject e) {
			if(!(e instanceof MouseEvent)|| ((MouseEvent)e).getClickCount() >= 2){
				return true;
			}
			return false;
		}

		@Override
		public boolean stopCellEditing() {
			value = textField.getText();
			return super.stopCellEditing();
		}

		@Override
		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {

			textField.setText((String) value);
			textField.setFont(table.getFont());
			
			return textField;
		}
	}

	
	public class ListCellEditor<T> extends AbstractCellEditor implements TableCellEditor, ActionListener {


		private static final long serialVersionUID = 1L;
		private static final int clickCount = 2;
		private Object value;
		private List<T> list;
 
		public ListCellEditor(List<T> list) {
			this.list = list;
		}
 
		@Override
		public Object getCellEditorValue() {
			return value;
		}

		
		@Override
		public boolean isCellEditable(EventObject e) {
			if(!(e instanceof MouseEvent)|| ((MouseEvent)e).getClickCount() >= clickCount){
				return true;
			}
			return false;
		}
		
		@Override
		public Component getTableCellEditorComponent(JTable table, Object value,
				boolean isSelected, int row, int column) {

			this.value = value;
     
			JComboBox<T> comboBox = new JComboBox<T>();
			comboBox.setBorder(BorderFactory.createEmptyBorder());
			comboBox.setFont(table.getFont());
			
			int iSelectedItem = 0;
			int i = 0;
			for (T element : list) {
				if(element == null || element.toString().isEmpty()){
					continue;
				} else if(value != null && value.toString() != null && element.toString().equalsIgnoreCase(value.toString())){
					iSelectedItem = i;
				}
				comboBox.addItem(element);
				i++;
			}
     
			if(comboBox.getItemCount() > 0){
				comboBox.setSelectedIndex(iSelectedItem);
				comboBox.addActionListener(this);
			}
     
			if (isSelected) {
				comboBox.setBackground(table.getSelectionBackground());
			} else {
				comboBox.setBackground(Color.white);
			}
			
			return comboBox;
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			event.getModifiers();
			Object source = event.getSource();
			if(source == null){
				return;
			}
			if(source instanceof JComboBox){
				value = ((JComboBox<?>)source).getSelectedItem();
			} else {
				value = new Object();
			}
		}

		@Override
		public boolean stopCellEditing() {
			return super.stopCellEditing();
		}

	}	

	
	class CommentTableHeaderPopupMouseAdapter extends java.awt.event.MouseAdapter {
		String headerName;
		CommentTableModel ctm;

		CommentTableHeaderPopupMouseAdapter(CommentTableModel ctm, String headerName) {
			this.headerName = headerName;
			this.ctm = ctm;
		}

		public void mouseReleased(MouseEvent e) {
			String selectedValue = ((JMenuItem) e.getSource()).getText();
			if(selectedValue.equals(LABEL_FILTER_CANCEL)){
				ctm.clearFilter();
			} else if(selectedValue.equals(LABEL_KEY_SET)){
				String inputValue = JOptionPane.showInputDialog(
						null, Messages.getString("CommentTable.31"), Messages.getString("CommentTable.32"), //$NON-NLS-1$ //$NON-NLS-2$
						JOptionPane.PLAIN_MESSAGE);
				if (inputValue != null) {
					ctm.addFilter(headerName, inputValue);
				}
				
			} else {
				ctm.addFilter(headerName, "^" + selectedValue + "$");
			}
			
			resetPosition();
			ctm.refreshFilter();
		}
	}
}
