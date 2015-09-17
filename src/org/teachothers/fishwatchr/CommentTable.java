/*
    Copyright (C) 2014-2015 Masaya YAMAGUCHI

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
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;


public class CommentTable extends JTable {
	private static final long serialVersionUID = 1L;
	private static final String LABEL_FILTER_CANCEL = "[フィルタの解除]";
	private static final String LABEL_KEY_SET = "[検索文字列の指定]";
	private static final int MAX_POPUP_ITEMS = 15;
	public static final int UNDEFINED = -1;


	public CommentTableModel ctm;
	private JPopupMenu popupMenu = new JPopupMenu();
	private int currentCommentID = -1; // 未指定 -1
	private int prevCommentID = 0; // 未指定 -1


	public CommentTable(CommentTableModel ctm){
		super(ctm);
		this.ctm = ctm;
		ginit();
		setSurrendersFocusOnKeystroke(true);
	}
	

	public void initState(){
		currentCommentID = UNDEFINED;
		prevCommentID = 0;
	}
	
	public void ginit(){
//		setOpaque(false);
		setColumnSelectionAllowed(true);
		getColumn("番号").setCellRenderer(new CellRenderer());
		getColumn("話者").setCellEditor(new ListCellEditor<User>(ctm.discussers));
		getColumn("ラベル").setCellEditor(new ListCellEditor<CommentType>(ctm.commentTypes));
		
//		getColumn("話者").setCellEditor(new JComboBoxCellEditor(ctm.discussers));
//		getColumn("ラベル").setCellEditor(new JComboBox(ctm.commentTypes));
//		ListCellEditor a = new ListCellEditor<User>(ctm.discussers);
//		JComboBox jcb = new JComboBox(ctm.discussers.toArray(new User[0]));
//		getColumn("話者").setCellEditor(new DefaultCellEditor(jcb));
		
		
		JMenuItem menuItemDelete = new JMenuItem("行の削除");
		menuItemDelete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int selectedRow = CommentTable.this.getSelectedRow();
				int selectedValue = JOptionPane.showConfirmDialog(null, "行を削除してもよいですか？", "削除の確認", JOptionPane.OK_CANCEL_OPTION);
				if(selectedValue == JOptionPane.OK_OPTION){
					deleteComment(selectedRow);
				}
			}
		});
		JMenuItem menuItemCellDelete = new JMenuItem("セル値の削除");
		menuItemCellDelete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int selectedRow = CommentTable.this.getSelectedRow();
				int selectedColumn = CommentTable.this.getSelectedColumn();
				
				if(!ctm.isCellEditable(selectedRow, selectedColumn)){
					JOptionPane.showMessageDialog(CommentTable.this, "このセルは編集できません。");
					return;
				}
				int selectedValue = JOptionPane.showConfirmDialog(null, "セルの値を削除してもよいですか？", "削除の確認", JOptionPane.OK_CANCEL_OPTION);
				if(selectedValue == JOptionPane.OK_OPTION){
					Object o = getValueAt(selectedRow, selectedColumn);
					if(o instanceof User){
						setValueAt(new User(""),  selectedRow, selectedColumn);
					} else if(o instanceof CommentType){
						setValueAt(new CommentType("", Color.BLACK),  selectedRow, selectedColumn);
					} else if(o instanceof String){
						setValueAt("",  selectedRow, selectedColumn);
					}
				}
			}
		});

		
		popupMenu.add(menuItemDelete);
		popupMenu.add(menuItemCellDelete);
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				// コメントリストから行の削除
				if(e.getButton() == MouseEvent.BUTTON3){
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
	
	void invokeDialogForFiltering(String headerName, Component component,
			int x, int y) {
		JPopupMenu popupMenu = new JPopupMenu();
		CommentList commentList = ctm.getCommentList();
		
//		popupMenu.setVisible(true);
		if(commentList.size() == 0){
			return;
		}

		ArrayList<String> itemList = new ArrayList<String>();

		itemList.addAll(ctm.getItemList(headerName));

		// フィルター解除用item追加
		int fromIndexForSorting = 0;
		if(commentList.size() != ctm.getFilteredCommentList().size()){
			itemList.add(0, LABEL_FILTER_CANCEL);
			fromIndexForSorting++;
		}
		// 文字列検索用item追加
		if (itemList.size() != 0) {
			itemList.add(fromIndexForSorting, LABEL_KEY_SET);
			fromIndexForSorting++;
		}

//		// item のソート
		Object[] itemObjects = itemList.toArray();
		Arrays.sort(itemObjects, fromIndexForSorting, itemObjects.length);

		String selectedValue = null;
//		System.err.println("ka:" + itemObjects.length);
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
			JOptionPane op = new JOptionPane();
			op.setMessageType(JOptionPane.INFORMATION_MESSAGE);
			op.setOptionType(JOptionPane.OK_CANCEL_OPTION);
			op.setSelectionValues(itemObjects);
			op.setInitialSelectionValue(itemObjects[0]);
			op.setWantsInput(true);
			op.setIcon(null);
			JDialog jd = op.createDialog(null, "test");
			jd.setSize(new Dimension(Math.min(400, jd.getPreferredSize().width), jd.getPreferredSize().height));
			jd.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			jd.setVisible(true);
			selectedValue = (String) op.getInputValue();
			if(selectedValue.equals(LABEL_FILTER_CANCEL)){
				ctm.removeFilter(headerName);
			} else if(selectedValue.equals(LABEL_KEY_SET)){
				String inputValue = JOptionPane.showInputDialog(
						null, "正規表現: ", "検索文字列の入力",
						JOptionPane.PLAIN_MESSAGE);
				if (inputValue != null) {
					ctm.addFilter(headerName, inputValue);
				}
				
			} else if(selectedValue == null || selectedValue.equals(JOptionPane.UNINITIALIZED_VALUE)){
				return;
			} else {
				ctm.addFilter(headerName, selectedValue);
			}
//			setRowSelectionInterval(prevCommentID, prevCommentID);
			resetPosition();
			ctm.refreshFilter();
		}
	}

	
	public void deleteComment(int row){
		ctm.deleteCommentAt(row);
	}
	
	
	public void resetPosition(){
		currentCommentID = UNDEFINED;
		prevCommentID = 0;
	}

	
	public void indicateCurrentComment(long msec){
		CommentList commentList = ctm.getCommentList();
		ArrayList<Comment> filteredCommentList = ctm.getFilteredCommentList();

		int candidate = UNDEFINED; // 初期値は候補なし
		
		for(int i = 0; i < filteredCommentList.size(); i++){
			Comment comment = filteredCommentList.get(i);
			int commentTime = commentList.unifiedCommentTime(comment);
			int p = ctm.findComment(comment);
			int commentTime2 = Integer.MAX_VALUE; // フィルタ時のnext comment
			if(commentList.size() > p+1){
				Comment comment2 = commentList.get(p+1);
				commentTime2 = commentList.unifiedCommentTime(comment2);
			}
//			System.err.println("time:" + commentTime + ", " + commentTime2 + ", " + msec);
			if(commentTime <= msec && commentTime2 > msec){
				candidate = i;
//				System.err.println("cand:" + prevCommentID + ", " + candidate);
			} else if(candidate != UNDEFINED){
				if(candidate == currentCommentID){
					return;
				} else {
					int currentSelectedRow = getSelectedRow();
					currentCommentID = candidate;
					if(prevCommentID != UNDEFINED && prevCommentID < getRowCount()){
						// 一つ前の再生位置のマークを消去
						setRowSelectionInterval(prevCommentID, prevCommentID);
					}

//					System.err.println("k300:" + prevCommentID + ", " + candidate);
					// 現在の再生位置のマークを描画
					setRowSelectionInterval(currentCommentID, currentCommentID);
					// カーソルを選択中の位置に戻す
					if(currentSelectedRow != UNDEFINED){
						setRowSelectionInterval(currentSelectedRow, currentSelectedRow);
					} else {
						setRowSelectionInterval(0, 0);
					}
					prevCommentID = currentCommentID;
					revalidate();
					repaint();
					return;
				}
			}
		}
		
		if(candidate == UNDEFINED){
			if(prevCommentID != UNDEFINED && getRowCount() > 0 && prevCommentID < getRowCount()){
//				System.err.println("k3a:" + prevCommentID + ", " + candidate);
				currentCommentID = UNDEFINED;
				// 一つ前の再生位置のマークを消去
				setRowSelectionInterval(prevCommentID, prevCommentID);
				revalidate();
				repaint();
				
				prevCommentID = UNDEFINED;
			}
		} else if(candidate == currentCommentID){
			return;
		} else if(candidate != UNDEFINED){
			currentCommentID = candidate;
			// 行を削除した場合の対策
			if(ctm.getRowCount() <= prevCommentID){
				prevCommentID = 0;
				candidate = UNDEFINED;
				return;
			}

			if(prevCommentID != UNDEFINED && prevCommentID < getRowCount()){
				setRowSelectionInterval(prevCommentID, prevCommentID);
			}
			setRowSelectionInterval(currentCommentID, currentCommentID);
			prevCommentID = currentCommentID;
//			System.err.println("k2:" + prevCommentID + ", " + candidate);
			revalidate();
			repaint();
		}
	}
	
	
	
	public int getCurrentCommentPosition(){
		return currentCommentID;
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


	public void setViewCenter(long msec){
		// 現在の行が表の真ん中になるように調整
		int targetCommentPosition = getCurrentCommentPosition();
		if(targetCommentPosition == UNDEFINED){
			targetCommentPosition = getNearCommentPosition(msec, false);
			if(targetCommentPosition == UNDEFINED){
				return;
			}
		}
		
		Rectangle targetRect = getCellRect(targetCommentPosition, 0, true);
		Rectangle viewRect = getVisibleRect();
		if(targetRect == null||viewRect == null){
//			System.err.println("hey you!");
		}
		int dy = viewRect.height / 2;
		viewRect.setLocation(targetRect.x, targetRect.y	- dy);
		scrollRectToVisible(viewRect);
	}
	
	
	public class CellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;

		public CellRenderer(){
			super();
			setBorder(BorderFactory.createEmptyBorder());
			setHorizontalAlignment(RIGHT);
		}

		
		public Component getTableCellRendererComponent(JTable table, Object value,
	            boolean isSelected, boolean hasFocus, int row, int column) {

			setText(value.toString());
	        if (isSelected) {
	        	setBackground(table.getSelectionBackground());
	        } else {
	            setBackground(Color.white);
	        }

	        if(row == currentCommentID){
	            setBackground(Color.LIGHT_GRAY);
	        } else {
	            setBackground(Color.white);
	        }
	        
	        return this;
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
//				if(e == null || !(e instanceof MouseEvent)|| ((MouseEvent)e).getClickCount() >= clickCount){
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
	

//	public class JComboBoxCellEditor extends DefaultCellEditor {
//		private static final long serialVersionUID = 1L;
////		private Object value;
//		private JComboBox<User> comboBox;
// 
//		
//		
//		public JComboBoxCellEditor(List<User> list) {
//
//			super(new JComboBox<User>(list.toArray(new User[0])));
//			this.comboBox = (JComboBox<User>)getComponent();
//			setClickCountToStart(2);
//
//			
//			//			DefaultComboBoxModel model = new DefaultComboBoxModel(list.toArray());
//		}
// 
////		public JComboBox getComponent(){
////			return (JComboBox) super.getComponent();
////		}
//		
////		@Override
////		public Object getCellEditorValue() {
////			return value;
////		}
//
//		@Override
//		public Component getTableCellEditorComponent(JTable table, Object value,
//				boolean isSelected, int row, int column) {
//
//			if (isSelected) {
//				comboBox.setBackground(table.getSelectionBackground());
//			} else {
//				comboBox.setBackground(Color.white);
//			}
//			return comboBox;
//		}
//
//
////		@Override
////		public boolean stopCellEditing() {
////			return super.stopCellEditing();
////		}
//
//	}

	
	
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
				ctm.removeFilter(headerName);
			} else if(selectedValue.equals(LABEL_KEY_SET)){
				String inputValue = JOptionPane.showInputDialog(
						null, "正規表現: ", "検索文字列の入力",
//						CommentTable.this, "test: ", "注釈者名",
						JOptionPane.PLAIN_MESSAGE);
				if (inputValue != null) {
					ctm.addFilter(headerName, inputValue);
				}
				
			} else {
				ctm.addFilter(headerName, selectedValue);
			}
			
//			setRowSelectionInterval(prevCommentID, prevCommentID);
			resetPosition();
			ctm.refreshFilter();
//			System.err.println("sv" + selectedValue);
		}
	}

}
