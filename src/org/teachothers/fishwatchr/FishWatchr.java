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

import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;


public class FishWatchr {
	public final static String SYSTEM_NAME = "FishWatchr"; //$NON-NLS-1$
	public final static int SOUND_VIEWER_HEIGHT = 80;
	public final static int WINDOW_WIDTH = 1024;
	public final static int WINDOW_HEIGHT = 650;
	public final static int DEFAULT_FONT_SIZE = 12;
		
	
	public static void main(final String[] arg){
		long startupTime = System.currentTimeMillis();
		
		UIManager.put("Button.font",new Font(Font.DIALOG, Font.PLAIN, DEFAULT_FONT_SIZE)); //$NON-NLS-1$
		UIManager.put("Label.font",new Font(Font.DIALOG, Font.PLAIN, DEFAULT_FONT_SIZE)); //$NON-NLS-1$
		UIManager.put("List.font",new Font(Font.DIALOG, Font.PLAIN, DEFAULT_FONT_SIZE)); //$NON-NLS-1$
		UIManager.put("ComboBox.font",new Font(Font.DIALOG, Font.PLAIN, DEFAULT_FONT_SIZE)); //$NON-NLS-1$
		UIManager.put("Menu.font",new Font(Font.DIALOG, Font.PLAIN, DEFAULT_FONT_SIZE)); //$NON-NLS-1$
		UIManager.put("MenuItem.font",new Font(Font.DIALOG, Font.PLAIN, DEFAULT_FONT_SIZE)); //$NON-NLS-1$
		UIManager.put("CheckBoxMenuItem.font",new Font(Font.DIALOG, Font.PLAIN, DEFAULT_FONT_SIZE)); //$NON-NLS-1$
		UIManager.put("RadioButtonMenuItem.font",new Font(Font.DIALOG, Font.PLAIN, DEFAULT_FONT_SIZE)); //$NON-NLS-1$
		UIManager.put("RadioButtonMenuItem.acceleratorFont",new Font(Font.DIALOG, Font.PLAIN, DEFAULT_FONT_SIZE)); //$NON-NLS-1$
		
		System.setProperty("awt.useSystemAAFontSettings", "on"); //$NON-NLS-1$ //$NON-NLS-2$
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				final MainFrame mainFrame = new MainFrame(SYSTEM_NAME);
				mainFrame.setMinimumSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
				mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); 
				mainFrame.init();
				mainFrame.setVisible(true);
				mainFrame.revalidate();
				
				if(arg.length == 2){
					try{
						mainFrame.play(arg[0], (long)(Double.parseDouble(arg[1])*1000));
					} catch(NullPointerException e) {
						System.err.println("Error(FishWatchr): invalid number format => " + arg[1]); //$NON-NLS-1$
					}
				} else if(arg.length == 1){
					mainFrame.play(arg[0], 0);
				}
				System.err.println("startup time:" + (System.currentTimeMillis()-startupTime)); //$NON-NLS-1$
			}
		});
	}
}

