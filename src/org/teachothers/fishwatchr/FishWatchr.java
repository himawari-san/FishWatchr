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
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
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
	
	private final static String LOCAL_VLC_DIR_WINDOWS = "vlc";  //$NON-NLS-1$
	private final static String LOCAL_VLC_DIR_MACOS = "VLC.app/Contents/MacOS";  //$NON-NLS-1$
//	private final static String SYSTEM_VLC_DIR_UBUNTU = "/usr/lib/x86_64-linux-gnu";  //$NON-NLS-1$
	
	
	public static void main(final String[] arg){
		long startupTime = System.currentTimeMillis();
		String osName = System.getProperty("os.name"); //$NON-NLS-1$

		File jarPath = new File(System.getProperty("java.class.path")); //$NON-NLS-1$
		// the directory that includes fishwatchr.jar
		String jarParent = ""; //$NON-NLS-1$
		try {
			jarParent = new File(jarPath.getCanonicalPath()).getParent();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		// find vlc libs
		String vlcLibraryName = RuntimeUtil.getLibVlcLibraryName();
		if(osName.toLowerCase().startsWith("windows") && new File(jarParent + "/" + LOCAL_VLC_DIR_WINDOWS).exists()){ //$NON-NLS-1$ //$NON-NLS-2$
			NativeLibrary.addSearchPath(vlcLibraryName, jarParent + "/" + LOCAL_VLC_DIR_WINDOWS); //$NON-NLS-1$
			LibC.INSTANCE._putenv("VLC_PLUGIN_PATH=" + jarParent + "/" + LOCAL_VLC_DIR_WINDOWS); //$NON-NLS-1$ //$NON-NLS-2$
			System.err.println("Warning(FishWatchr): using the local vlc library, " + jarParent + "/" + LOCAL_VLC_DIR_WINDOWS); //$NON-NLS-1$ //$NON-NLS-2$
		} else if(osName.toLowerCase().startsWith("mac") && new File(jarParent + "/" + LOCAL_VLC_DIR_MACOS).exists()){ //$NON-NLS-1$ //$NON-NLS-2$
			NativeLibrary.addSearchPath(vlcLibraryName, jarParent + "/" + LOCAL_VLC_DIR_MACOS + "/lib"); //$NON-NLS-1$ //$NON-NLS-2$

			// https://github.com/caprica/vlcj/issues/643
			NativeLibrary.addSearchPath(vlcLibraryName+"core", jarParent + "/" + LOCAL_VLC_DIR_MACOS + "/lib"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			Map<String,?> options = new HashMap<>();
			NativeLibrary.getInstance(vlcLibraryName+"core", options ); //$NON-NLS-1$

			LibC.INSTANCE.setenv("VLC_PLUGIN_PATH", jarParent + "/" + LOCAL_VLC_DIR_MACOS + "/plugins", 1); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			// for Youtube videos
			LibC.INSTANCE.setenv("VLC_DATA_PATH", jarParent + "/" + LOCAL_VLC_DIR_MACOS + "/share", 1); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

			System.err.println("Warning(FishWatchr): using the local vlc library, " + jarParent + "/" + LOCAL_VLC_DIR_MACOS); //$NON-NLS-1$ //$NON-NLS-2$
		} else if(osName.toLowerCase().startsWith("linux")){ //$NON-NLS-1$
			// skip NativeDiscovery().discover() because it returns wrong answers. 
			
		} else {
			boolean isDiscovered = new NativeDiscovery().discover();
			if(!isDiscovered){
				JOptionPane.showMessageDialog(null, Messages.getString("FishWatchr.0")); //$NON-NLS-1$
				System.exit(-1);
			}

			if(osName.toLowerCase().startsWith("mac")){ //$NON-NLS-1$
				// for Youtube videos (bug?)
				LibC.INSTANCE.setenv("VLC_DATA_PATH", "/Applications/VLC.app/Contents/MacOS/share", 1); //$NON-NLS-1$ //$NON-NLS-2$
			}
			System.err.println("Warning(FishWatchr): using the system vlc library, " + isDiscovered); //$NON-NLS-1$
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

