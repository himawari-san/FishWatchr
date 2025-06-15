/*
    Copyright (C) 2014-2021 Masaya YAMAGUCHI

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

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import uk.co.caprica.vlcj.factory.discovery.provider.WindowsInstallDirectoryProvider;


public class MyWindowsInstallDirectoryProvider extends WindowsInstallDirectoryProvider {
	private final static String LOCAL_VLC_DIR_WINDOWS = "vlc";  //$NON-NLS-1$

    @Override
    public int priority() {
        return super.priority() + 1;
    }

    @Override
    public String[] directories() {
		ArrayList<String> dirs = new ArrayList<String>();

		Path jarPath = null;
		try {
			jarPath = Paths.get(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Add VLC directory in the FishWatchr directory
    	dirs.add(jarPath.getParent().resolve(LOCAL_VLC_DIR_WINDOWS).toString());
    	dirs.addAll(Arrays.asList(super.directories()));
    	
    	return dirs.toArray(new String[0]);
    }
}