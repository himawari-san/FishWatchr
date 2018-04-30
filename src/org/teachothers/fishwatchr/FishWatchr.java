/*
    Copyright (C) 2014-2018 Masaya YAMAGUCHI

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
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.UIManager;

import com.sun.jna.NativeLibrary;

import uk.co.caprica.vlcj.binding.LibC;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;



public class FishWatchr {
	public final static String SYSTEM_NAME = "FishWatchr"; //$NON-NLS-1$
	public final static int SOUND_VIEWER_HEIGHT = 80;
	public final static int WINDOW_WIDTH = 1024;
	public final static int WINDOW_HEIGHT = 650;
	public final static int DEFAULT_FONT_SIZE = 12;
	
	
	public static void main(final String[] arg){
		String libVlcDir = System.getProperty("libvlcdir"); //$NON-NLS-1$
		if(libVlcDir == null || libVlcDir.isEmpty()){
			boolean isDiscovered = new NativeDiscovery().discover();
			String osName = System.getProperty("os.name"); //$NON-NLS-1$
			if(!isDiscovered){
				if(osName.toLowerCase().startsWith("windows")){ //$NON-NLS-1$
					NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), "vlc"); //$NON-NLS-1$
					NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), "/Applications/VLC.app/Contents/MacOS/lib"); //$NON-NLS-1$
					NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), "C:\\Program Files\\VideoLAN\\VLC"); //$NON-NLS-1$
					NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), "C:\\Program Files (x86)\\VideoLAN\\VLC"); //$NON-NLS-1$
				} else if(osName.toLowerCase().startsWith("mac")){ //$NON-NLS-1$
					File jarPath = new File(System.getProperty("java.class.path")); //$NON-NLS-1$
					String jarParent = ""; //$NON-NLS-1$
					try {
						jarParent = new File(jarPath.getCanonicalPath()).getParent();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					
					if(new File(jarParent + "/VLC.app/Contents/MacOS/lib").exists()){ //$NON-NLS-1$
						LibC.INSTANCE.setenv("VLC_PLUGIN_PATH", jarParent + "/VLC.app/Contents/MacOS/plugins", 1); //$NON-NLS-1$ //$NON-NLS-2$
						// for Youtube videos
						LibC.INSTANCE.setenv("VLC_DATA_PATH", jarParent + "/VLC.app/Contents/MacOS/share", 1); //$NON-NLS-1$ //$NON-NLS-2$
						NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), jarParent + "/VLC.app/Contents/MacOS/lib/"); //$NON-NLS-1$
					} else {
						NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), "/Applications/VLC.app/Contents/MacOS/lib"); //$NON-NLS-1$
					}
				}
			} else if(osName.toLowerCase().startsWith("mac")){ //$NON-NLS-1$
				// vlcj bug?
				// can not play Youtube videos when using VLC in /Application 
				LibC.INSTANCE.setenv("VLC_DATA_PATH", "/Applications/VLC.app/Contents/MacOS/share", 1); //$NON-NLS-1$ //$NON-NLS-2$
			}
		} else {
			NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), libVlcDir);
		}
		
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
		
		final MainFrame mainFrame = new MainFrame(SYSTEM_NAME);
		mainFrame.setMinimumSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
		mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); 
		mainFrame.init();
		mainFrame.setVisible(true);
		mainFrame.revalidate();
		
		if(arg.length != 0){
			if(arg.length == 2){
				try{
					try {
						// wait for generating soundplayer
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					mainFrame.play(arg[0], (long)(Double.parseDouble(arg[1])*1000));
				} catch(NullPointerException e) {
					System.err.println("Error(FishWatchr): invalid number format => " + arg[1]); //$NON-NLS-1$
				}
			}
		}
	}
}

