package org.teachothers.fishwatchr;

import java.awt.Container;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.apache.commons.lang3.StringUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;


public class StatFrame extends JFrame {
	private static final long serialVersionUID = 1L;

	public static final int CHART_STYLE_UNIQ = 0;
	public static final int CHART_STYLE_ANNOTATOR_TARGET = 1;
	public static final int CHART_STYLE_ANNOTATOR_LABEL = 2;
	public static final int CHART_STYLE_EVAL = 3;

	private JScrollPane scrollTablePane;
	private JTable table;
	private String headers[];
	private int iFreq;
	private ArrayList<Object[]> data;

	public StatFrame(ArrayList<Object[]> data, String headers[]) {
		this.data = data;
		this.headers = headers;
		iFreq = headers.length;
		ginit();
	}
	
	
	public void ginit(){
		table = new JTable(new StatTableModel());
		table.getTableHeader().setReorderingAllowed(false);
		table.setAutoCreateRowSorter(true);
		
		// text align of Freq field
		DefaultTableCellRenderer rightAlignCellRenderer = new DefaultTableCellRenderer();
		rightAlignCellRenderer.setHorizontalAlignment(SwingUtilities.RIGHT);
		table.getColumnModel().getColumn(iFreq).setCellRenderer(rightAlignCellRenderer);

		scrollTablePane = new JScrollPane(table);
		
//		getContentPane().add(showChart(1));
//		getContentPane().add(showChart(1));
//		getContentPane().re
////		add(scrollTablePane);
	}

	
	public void showChart(int style){
	    DefaultCategoryDataset dataSet = new DefaultCategoryDataset();
	    boolean flagLegend = false;
	    Container con = getContentPane();
	    con.removeAll();
	    
	    if(style == CHART_STYLE_UNIQ){
	    	String categoryName = StringUtils.join(headers, "/");
	    	
		    for(Object[] record : data){
		    	String categoryValue = StringUtils.join(record, "/").replaceFirst("/[^/]*$", "");
		    	dataSet.addValue(((Integer)record[iFreq]).doubleValue(), categoryName, categoryValue);
		    }
	    } else if(style == CHART_STYLE_ANNOTATOR_TARGET){
	    	flagLegend = true;
		    for(Object[] record : data){
		    	dataSet.addValue(((Integer)record[iFreq]).doubleValue(), record[0].toString(), record[1].toString());
//		    	dataSet.addValue(((Integer)record[iFreq]).doubleValue(), record[1].toString(), record[0].toString());
		    }
	    }
	    
	    
	    JFreeChart chart = 
	    	      ChartFactory.createBarChart("", // title
	    	                                  "",
	    	                                  "頻度",
	    	                                  dataSet,
	    	                                  PlotOrientation.VERTICAL,
	    	                                  true,
	    	                                  true,
	    	                                  false);

	    chart.getCategoryPlot().getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);
	    if(!flagLegend){
		    chart.removeLegend();
	    }
	    
	    con.add(new ChartPanel(chart));
	}
	
	
	
	public class StatTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;

		@Override
		public int getRowCount() {
			return data.size();
		}

		@Override
		public int getColumnCount() {
			return data.get(0).length;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			Object[] record = data.get(rowIndex);
			return record[columnIndex];
		}
		
		public Class<?> getColumnClass(int column) {
			if (column == iFreq) {
				return Integer.class;
			} else {
				return Object.class;
			}
		}
		
		public String getColumnName(int i){
			if(i == iFreq){
				return "頻度";
			} else {
				return headers[i];
			}
		}
	}
}
