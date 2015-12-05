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

import com.sun.jna.NativeLibrary;

import uk.co.caprica.vlcj.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;



public class FishWatchr {
	public final static String SYSTEM_NAME = "FishWatchr";
	public final static int SOUND_VIEWER_HEIGHT = 80;
	public final static int WINDOW_WIDTH = 1024;
	public final static int WINDOW_HEIGHT = 650;
	
	
	public static void main(final String[] arg){
		String libVlcDir = System.getProperty("libvlcdir");
		if(libVlcDir == null || libVlcDir.isEmpty()){
			boolean isDiscovered = new NativeDiscovery().discover();
			if(!isDiscovered){
				NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), "vlc");
				NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), "VLC.app/Contents/MacOS/lib");
				NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), "/Applications/VLC.app/Contents/MacOS/lib");
				NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), "C:\\Program Files\\VideoLAN\\VLC");
				NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), "C:\\Program Files (x86)\\VideoLAN\\VLC");
			}
		} else {
			NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), libVlcDir);
		}
		
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
		mainFrame.setMinimumSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
		mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); 
		mainFrame.init();
		mainFrame.setVisible(true);
		mainFrame.revalidate();
	}
}

