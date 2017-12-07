package org.teachothers.fishwatchr;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
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
	public static final int CHART_STYLE_TARGET = 1;
	public static final int CHART_STYLE_LABEL = 2;
	public static final int CHART_STYLE_EVAL = 3;

	public static final String LABEL_STYLE_UNIQ = "頻度(選択項目)";
	public static final String LABEL_STYLE_TARGET = "観察対象";
	public static final String LABEL_STYLE_LABEL = "ラベル";
	public static final String LABEL_STYLE_EVAL = "評価";
	
	private JTabbedPane	figureTabbedPane;
	private JPanel chartPanel;
	private JPanel tablePanel;

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
		chartPanel = new JPanel();
		chartPanel.setLayout(new BorderLayout());
		tablePanel = new JPanel();
		tablePanel.setLayout(new BorderLayout());
		
		figureTabbedPane = new JTabbedPane(JTabbedPane.TOP);
		figureTabbedPane.addTab("グラフ", chartPanel);
		figureTabbedPane.addTab("データ表", tablePanel);

		table = new JTable(new StatTableModel());
		table.getTableHeader().setReorderingAllowed(false);
		table.setAutoCreateRowSorter(true);
		// sort by freq (descending order)
		table.getRowSorter().toggleSortOrder(iFreq);
		table.getRowSorter().toggleSortOrder(iFreq);
		
		// text align of Freq field
		DefaultTableCellRenderer rightAlignCellRenderer = new DefaultTableCellRenderer();
		rightAlignCellRenderer.setHorizontalAlignment(SwingUtilities.RIGHT);
		table.getColumnModel().getColumn(iFreq).setCellRenderer(rightAlignCellRenderer);
		scrollTablePane = new JScrollPane(table);
		tablePanel.add(scrollTablePane);
		
		getContentPane().add(figureTabbedPane);
	}

	
	public void showChart(int style){
	    DefaultCategoryDataset dataSet = new DefaultCategoryDataset();
	    boolean flagLegend = false;
	    
	    if(style == CHART_STYLE_UNIQ){
	    	String categoryName = StringUtils.join(headers, "/");
	    	
		    for(Object[] record : data){
		    	String categoryValue = StringUtils.join(record, "/").replaceFirst("/[^/]*$", "");
		    	dataSet.addValue(Double.parseDouble(record[iFreq].toString()), categoryName, categoryValue);
		    }
	    } else if(style == CHART_STYLE_TARGET || style == CHART_STYLE_LABEL){
	    	flagLegend = true;
		    for(Object[] record : data){
		    	dataSet.addValue(Double.parseDouble(record[iFreq].toString()), record[0].toString(), record[1].toString());
//		    	dataSet.addValue(((Integer)record[iFreq]).doubleValue(), record[0].toString(), record[1].toString());
		    }
	    } else if(style == CHART_STYLE_EVAL){
	    	flagLegend = true;
	    	// calc evaluation of each category
	    	// record[0]:annotator, [1]:target, [2]:label, [3]:freq
	    	HashMap<String, Double> mapEvalSum = new HashMap<String, Double>();
	    	HashMap<String, Double> mapFreqSum = new HashMap<String, Double>();
	    	TreeSet<String> setTargets = new TreeSet<String>();
	    	TreeSet<String> setLabels = new TreeSet<String>();

	    	for(Object[] record : data){
		    	String key = record[0] + "\t" + record[1];
		    	double addedValue = Double.parseDouble(record[2].toString()) * Double.parseDouble(record[3].toString());
		    	setTargets.add(record[0].toString());
		    	setLabels.add(record[1].toString());
		    	
		    	if(mapEvalSum.containsKey(key)){
		    		mapEvalSum.put(key, mapEvalSum.get(key) + addedValue);
		    		mapFreqSum.put(key, mapFreqSum.get(key) + Double.parseDouble(record[3].toString()));
		    	} else {
		    		mapEvalSum.put(key, addedValue);
		    		mapFreqSum.put(key, Double.parseDouble(record[3].toString()));
		    	}
		    }
		    
	    	// generate all target/label combinations  
	    	TreeSet<String> keys = new TreeSet<String>(); // for sorting keys
	    	for(String target : setTargets){
		    	for(String label : setLabels){
		    		keys.add(target + "\t" + label);
		    	}
	    	}
	    	
		    for(String key : keys){
		    	String[] keyArray = key.split("\t");
		    	if(mapEvalSum.containsKey(key)){
			    	dataSet.addValue(mapEvalSum.get(key) / mapFreqSum.get(key), keyArray[0].toString(), keyArray[1].toString());
		    	} else {
			    	dataSet.addValue(0, keyArray[0].toString(), keyArray[1].toString());
		    	}
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
	    
	    chartPanel.add(new ChartPanel(chart));
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
				return Double.class;
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
