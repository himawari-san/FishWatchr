/*
    Copyright (C) 2014-2016 Masaya YAMAGUCHI

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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;

import javax.swing.AbstractButton;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;


public class TimeCorrectionSettingPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final int OFFSET_COLUMN_WIDTH = 120;
	private static final int COLUMN_WIDTH_SETNAME = 120;
	private static final String COLUMN_NAME_BASE_SET = "基準";
	private static final String COLUMN_NAME_METHOD = "同期方法";
	
	private CommentList commentList;
	private HashMap<String, JTextField> mapField = new HashMap<String, JTextField>();
	private String columnNames[] = {COLUMN_NAME_BASE_SET, "データ名", "オフセット", "開始時間", COLUMN_NAME_METHOD};
	private String methods[] = {"経過時間", "実時間", "マーク"};
	private Object settings[][];

	public TimeCorrectionSettingPanel(CommentList commentList){
		this.commentList = commentList;
		ginit();
	}

	private void ginit(){
		if(commentList.getSetSize() == 0){
			setLayout(new GridLayout(1, 1));
			add(new JLabel("データが読み込まれていません"));
			return;
		}

		settings = new Object[commentList.getSetNames().size()][];

		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(500,100));
		MySettingTableModel tableModel = new MySettingTableModel();
		ButtonGroup buttonGroup = new ButtonGroup();
		int i = 0;
		for(String setName : commentList.getSetNames()){
			Object data[] = new Object[columnNames.length];
			JRadioButton jRadioButtonBase = new JRadioButton();
			JCheckBox jCheckBoxSyncByTag = new JCheckBox();
			JComboBox<String> jComboMethod = new JComboBox<String>(methods);
			jComboMethod.setBorder(BorderFactory.createEmptyBorder());

			if(i==0){
				jRadioButtonBase.setSelected(true);
			}
			data[0] = jRadioButtonBase;
			data[1] = setName;
			data[2] = jCheckBoxSyncByTag;
			data[3] = commentList.getStartTime(setName).replaceFirst("\\.\\d+$", "");
			data[4] = jComboMethod;
			settings[i] = data;
			buttonGroup.add(jRadioButtonBase);
			i++;
		}
		JTable settingTable = new JTable(tableModel);
		JScrollPane scrollPane = new JScrollPane(settingTable);
		add(scrollPane, BorderLayout.CENTER);
		
		settingTable.getColumn(COLUMN_NAME_BASE_SET).setCellEditor(new ButtonEditor());
		settingTable.getColumn(COLUMN_NAME_BASE_SET).setCellRenderer(new SettingTableCellRenderer());
		settingTable.getColumn(COLUMN_NAME_BASE_SET).setPreferredWidth(50);
		settingTable.getColumn(COLUMN_NAME_METHOD).setCellEditor(new ComboEditor());
		settingTable.getColumn(COLUMN_NAME_METHOD).setCellRenderer(new SettingTableCellRenderer());
		
		settingTable.setCellSelectionEnabled(true);
	}

	
	public void applyData(){
		for(String setName : commentList.getSetNames()){
			commentList.setCommentTimeOffset(setName, Integer.parseInt(mapField.get(setName).getText()));
		}
	}
	
	
	private class MySettingTableModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		public Class<? extends Object> getColumnClass(int column) {
			return getValueAt(0, column).getClass();
		}
		
		public Object getValueAt(int row, int column) {
			return settings[row][column];
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public int getRowCount() {
			return settings.length;
		}

		public String getColumnName(int column){
			return columnNames[column];
		}
		
		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			String columnName = getColumnName(columnIndex);
			if(columnName.equals(COLUMN_NAME_BASE_SET) || columnName.equals(COLUMN_NAME_METHOD)){
				return true;
			} else {
				return false;
			}
		}

		@Override
		public void setValueAt(Object aValue, int row, int column) {
			super.setValueAt(aValue, row, column);
			fireTableDataChanged(); // GroupButton の時に必要
		}

		
	}	
	
	// based on http://www.programming.mesexemples.com/java/swing-components/java-how-to-add-jradiobutton-to-jtable/
	class ButtonEditor extends AbstractCellEditor implements TableCellEditor, ItemListener {
		
		private static final long serialVersionUID = 1L;

		private AbstractButton button;

		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {
			if (value == null)
				return null;
			button = (JRadioButton) value;
			button.addItemListener(this);
			return (Component) value;
		}

		public Object getCellEditorValue() {
			button.removeItemListener(this);
			return button;
		}

		public void itemStateChanged(ItemEvent e) {
			super.fireEditingStopped();
		}
	}

	
	class ComboEditor extends AbstractCellEditor implements TableCellEditor, ItemListener {
		
		private static final long serialVersionUID = 1L;

		private JComboBox<String> box;

		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {
			if (value == null){
				return null;
			}
			box = (JComboBox<String>) value;
			box.addItemListener(this);
			return (Component) value;
		}

		public Object getCellEditorValue() {
			box.removeItemListener(this);
			return box;
		}

		public void itemStateChanged(ItemEvent e) {
			super.fireEditingStopped();
		}
	}

	
	
	public class SettingTableCellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;
	    @Override
	    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
	    	if(value == null){
	    		return null;
	    	} else if(value instanceof JComboBox){
	    		return new JLabel(((JComboBox<?>)value).getSelectedItem().toString());
	    	} else {
	    		return (Component)value;
	    	}
	    }
	}
}
