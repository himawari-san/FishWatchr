package org.teachothers.fishwatchr;

import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

public class ExportDialog {

    private String selectedFormat;
    private File exportFile;
    private JCheckBox checkLabelOutput = new JCheckBox("「ラベル」を出力");
    private JCheckBox checkTargetNodeOutput = new JCheckBox("「観察対象」ノードを出力");


    public void showDialog(Component parent) {
        JRadioButton tsvOption = new JRadioButton(CommentList.FORMAT_TSV);
        JRadioButton kmOption = new JRadioButton(CommentList.FORMAT_KM);
        ButtonGroup formatGroup = new ButtonGroup();
        formatGroup.add(tsvOption);
        formatGroup.add(kmOption);
        tsvOption.setSelected(true);
        
        JPanel formatPanel = new JPanel();
        formatPanel.setLayout(new BoxLayout(formatPanel, BoxLayout.X_AXIS));
        formatPanel.add(tsvOption);
        formatPanel.add(Box.createHorizontalStrut(10));
        formatPanel.add(kmOption);
        formatPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JPanel checkboxPanel = new JPanel();
        checkboxPanel.setLayout(new BoxLayout(checkboxPanel, BoxLayout.Y_AXIS));
        checkboxPanel.add(checkLabelOutput);
        checkboxPanel.add(checkTargetNodeOutput);
        checkboxPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        checkboxPanel.setEnabled(false);
        

        JLabel fileLabel = new JLabel("エクスポート先: 未選択");
        JButton fileButton = new JButton("保存先の指定");

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel("出力形式："));
        panel.add(formatPanel);
        panel.add(checkboxPanel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(fileButton);
        panel.add(fileLabel);
        panel.add(Box.createVerticalStrut(10));

        JButton okButton = new JButton("OK");
        okButton.setEnabled(false);
        JButton cancelButton = new JButton("Cancel");
        Object[] options = {okButton, cancelButton};
        JOptionPane optionPane = new JOptionPane(
        		panel,
        		JOptionPane.PLAIN_MESSAGE,
        		JOptionPane.OK_CANCEL_OPTION,
        		null,
        		options,
        		okButton);

        JDialog dialog = optionPane.createDialog(parent, "Export Settings");
        exportFile = null;

        kmOption.addActionListener(e -> {
            for (Component c : checkboxPanel.getComponents()) {
                c.setEnabled(true);
            }
            exportFile = null;
            fileLabel.setText("エクスポート先: 未選択");
            okButton.setEnabled(false);
        });
        tsvOption.addActionListener(e -> {
            for (Component c : checkboxPanel.getComponents()) {
                c.setEnabled(false);
            }
            exportFile = null;
            fileLabel.setText("エクスポート先: 未選択");
            okButton.setEnabled(false);
        });
        tsvOption.doClick();
                
        // disable ok button
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
            	okButton.setEnabled(false);
            }
        });
        
        okButton.addActionListener(e -> {
            optionPane.setValue(okButton);
            dialog.dispose();	
        });
        cancelButton.addActionListener(e -> {
            optionPane.setValue(cancelButton);
            dialog.dispose();
        });
        
        fileButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("エクスポート先のファイルを指定");
            int ret = chooser.showSaveDialog(parent);
            if (ret == JFileChooser.APPROVE_OPTION) {
                exportFile = chooser.getSelectedFile();
                selectedFormat = tsvOption.isSelected() ? CommentList.FORMAT_TSV : CommentList.FORMAT_KM;
                String suffix = "." + selectedFormat.toLowerCase();
                if (!exportFile.getName().endsWith(suffix)) {
                	exportFile = new File(exportFile.getAbsolutePath() + suffix);
                }
                fileLabel.setText("エクスポート先: " + exportFile.getAbsolutePath());
                okButton.setEnabled(true);

            }
        });

        dialog.setVisible(true);

        Object selectedValue = optionPane.getValue();
        if (selectedValue != okButton || exportFile == null) {
        	selectedFormat = null;
            exportFile = null;
        }
    }
    
    public String getSelectedFormat() {
        return selectedFormat;
    }

    public File getExportFile() {
        return exportFile;
    }
    
    public boolean isKmOptionLabelOutput() {
        return checkLabelOutput.isSelected();
    }

    public boolean isKmOptionTargetNodeOutput() {
        return checkTargetNodeOutput.isSelected();
    }    
}