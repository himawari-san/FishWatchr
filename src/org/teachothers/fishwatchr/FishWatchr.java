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

import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.UIManager;

import uk.co.caprica.vlcj.discovery.NativeDiscovery;
//import uk.co.caprica.vlcj.runtime.RuntimeUtil;
//
//import com.sun.jna.NativeLibrary;



public class FishWatchr {
	public final static String SYSTEM_NAME = "FishWatchr";
	public final static int VIEWER_HEIGHT = 180;
	public final static int WINDOW_WIDTH = 1024;
	public final static int OTHER_WINDOW_HEIGHT = 270;
	public static int nUsers = 2;
	
	
	public static void main(final String[] arg){
		new NativeDiscovery().discover();
//		NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), "vlc");
////		NativeLibrary.addSearchPath("vlc", "vlc");
//		NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), "/Applications/VLC.app/Contents/MacOS/lib");
//		NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), "C:\\Program Files\\VideoLAN\\VLC");
//		NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), "C:\\Program Files (x86)\\VideoLAN\\VLC");
		
//		try {
////			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
////			UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
//			UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
////			UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
//		} catch (ClassNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InstantiationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (UnsupportedLookAndFeelException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		UIManager.put("Button.font",new Font(Font.DIALOG, Font.PLAIN, 12));
		UIManager.put("Label.font",new Font(Font.DIALOG, Font.PLAIN, 12));
		UIManager.put("List.font",new Font(Font.DIALOG, Font.PLAIN, 12));
		UIManager.put("ComboBox.font",new Font(Font.DIALOG, Font.PLAIN, 12));
		UIManager.put("Menu.font",new Font(Font.DIALOG, Font.PLAIN, 12));
		UIManager.put("MenuItem.font",new Font(Font.DIALOG, Font.PLAIN, 12));
		UIManager.put("CheckBoxMenuItem.font",new Font(Font.DIALOG, Font.PLAIN, 12));
		UIManager.put("RadioButtonMenuItem.font",new Font(Font.DIALOG, Font.PLAIN, 12));
		UIManager.put("RadioButtonMenuItem.acceleratorFont",new Font(Font.DIALOG, Font.PLAIN, 12));
		
		MainFrame mainFrame = new MainFrame(SYSTEM_NAME);
		mainFrame.setSize(WINDOW_WIDTH, VIEWER_HEIGHT * nUsers + OTHER_WINDOW_HEIGHT);
		mainFrame.setMinimumSize(new Dimension(WINDOW_WIDTH, VIEWER_HEIGHT * nUsers + OTHER_WINDOW_HEIGHT));
		mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); 
		mainFrame.setVisible(true);
		mainFrame.revalidate();
		mainFrame.init();
	}
}

