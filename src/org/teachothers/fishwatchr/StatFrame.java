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

import java.awt.BorderLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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

	public static final String LABEL_STYLE_UNIQ = Messages.getString("StatFrame.0"); //$NON-NLS-1$
//	public static final String LABEL_STYLE_TARGET = Comment.ITEM_TARGET;
//	public static final String LABEL_STYLE_LABEL = Comment.ITEM_LABEL;
	public static final String LABEL_STYLE_EVAL = Messages.getString("StatFrame.1"); //$NON-NLS-1$
	
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
		figureTabbedPane.addTab(Messages.getString("StatFrame.2"), chartPanel); //$NON-NLS-1$
		figureTabbedPane.addTab(Messages.getString("StatFrame.3"), tablePanel); //$NON-NLS-1$

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

	
	public boolean showChart(int style){
	    DefaultCategoryDataset dataSet = new DefaultCategoryDataset();
	    boolean flagLegend = false;
	    boolean flagSuccess = true;
	    
	    if(style == CHART_STYLE_UNIQ){
	    	String categoryName = StringUtils.join(headers, "/"); //$NON-NLS-1$
	    	
	    	Collections.sort(data, new Comparator<Object[]>() {
	    	    public int compare(Object[] a, Object[] b) {
	    	    	String key1 = StringUtils.join(a, "/").replaceFirst("/[^/]*$", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	    	    	String key2 = StringUtils.join(b, "/").replaceFirst("/[^/]*$", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	    	    	return key1.compareToIgnoreCase(key2);
	    	    }
			});
	    	
		    for(Object[] record : data){
		    	String categoryValue = StringUtils.join(record, "/").replaceFirst("/[^/]*$", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		    	dataSet.addValue(Double.parseDouble(record[iFreq].toString()), categoryName, categoryValue);
		    }
	    } else if(style == CHART_STYLE_TARGET || style == CHART_STYLE_LABEL){
	    	flagLegend = true;
	    	Collections.sort(data, new Comparator<Object[]>() {
	    	    public int compare(Object[] a, Object[] b) {
	    	    	String key1 = a[1].toString() + "\t" + a[0].toString(); //$NON-NLS-1$
	    	    	String key2 = b[1].toString() + "\t" + b[0].toString(); //$NON-NLS-1$
	    	    	return key1.compareToIgnoreCase(key2);
	    	    }
			});
	    	
		    for(Object[] record : data){
		    	dataSet.addValue(Double.parseDouble(record[iFreq].toString()), record[0].toString(), record[1].toString());
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
	    		// is numeric?
	    		try{
		    		Double.parseDouble(record[2].toString());
	    		} catch(NumberFormatException e){
	    			flagSuccess = false;
	    			continue;
	    		}
	    		
		    	String key = record[0] + "\t" + record[1]; //$NON-NLS-1$
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
	    	// use TreeSet to sort keys
	    	TreeSet<String> keys = new TreeSet<String>(new Comparator<String>() {
				@Override
				public int compare(String o1, String o2) {
					return o1.compareToIgnoreCase(o2);
				}
			});
	    	for(String target : setTargets){
		    	for(String label : setLabels){
		    		keys.add(target + "\t" + label); //$NON-NLS-1$
		    	}
	    	}
	    	
		    for(String key : keys){
		    	String[] keyArray = key.split("\t"); //$NON-NLS-1$
		    	if(mapEvalSum.containsKey(key)){
			    	dataSet.addValue(mapEvalSum.get(key) / mapFreqSum.get(key), keyArray[0].toString(), keyArray[1].toString());
		    	} else {
			    	dataSet.addValue(0, keyArray[0].toString(), keyArray[1].toString());
		    	}
		    }
	    }
	    
	    JFreeChart chart = 
	    	      ChartFactory.createBarChart("", // title //$NON-NLS-1$
	    	                                  "", //$NON-NLS-1$
	    	                                  Messages.getString("StatFrame.4"), //$NON-NLS-1$
	    	                                  dataSet,
	    	                                  PlotOrientation.VERTICAL,
	    	                                  true,
	    	                                  true,
	    	                                  false);

	    // font settings
	    chart.setAntiAlias(true);
	    chart.getLegend().setItemFont(new Font(Font.DIALOG, Font.PLAIN, FishWatchr.DEFAULT_FONT_SIZE));
	    chart.getCategoryPlot().getDomainAxis().setLabelFont(new Font(Font.DIALOG, Font.PLAIN, FishWatchr.DEFAULT_FONT_SIZE));
	    chart.getCategoryPlot().getDomainAxis().setTickLabelFont(new Font(Font.DIALOG, Font.PLAIN, FishWatchr.DEFAULT_FONT_SIZE));
	    chart.getCategoryPlot().getRangeAxis().setLabelFont(new Font(Font.DIALOG, Font.PLAIN, FishWatchr.DEFAULT_FONT_SIZE));
	    chart.getCategoryPlot().getRangeAxis().setTickLabelFont(new Font(Font.DIALOG, Font.PLAIN, FishWatchr.DEFAULT_FONT_SIZE));

	    // rotate category labels
	    chart.getCategoryPlot().getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);
	    
	    if(!flagLegend){
		    chart.removeLegend();
	    }
	    
	    chartPanel.add(new ChartPanel(chart));
	    
	    return flagSuccess;
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
				return Messages.getString("StatFrame.5"); //$NON-NLS-1$
			} else {
				return headers[i];
			}
		}
	}
}
