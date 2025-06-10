package org.teachothers.fishwatchr;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;

public class FTextField extends JTextField {
	private static final long serialVersionUID = 1L;
	private JPopupMenu popupMenu = new JPopupMenu();
	
	public FTextField() {
		init();
	}

	public FTextField(String text) {
		super(text);
		init();
	}

	public FTextField(int columns) {
		super(columns);
		init();
	}

	public FTextField(String text, int columns) {
		super(text, columns);
		init();
	}

	public FTextField(Document doc, String text, int columns) {
		super(doc, text, columns);
		init();
	}

	private void init() {
		JMenuItem menuItemPaste = new JMenuItem("Paste");
		menuItemPaste.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				FTextField.this.paste();
			}
		});
		JMenuItem menuItemCopy = new JMenuItem("Copy");
		menuItemCopy.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				FTextField.this.copy();
			}
		});
		
		popupMenu.add(menuItemCopy);
		popupMenu.add(menuItemPaste);
		
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(SwingUtilities.isRightMouseButton(e)) {
					popupMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});
	}
}
