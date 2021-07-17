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

import uk.co.caprica.vlcj.binding.RuntimeUtil;
import uk.co.caprica.vlcj.factory.discovery.provider.WellKnownDirectoryProvider;

public class MyOsxDirectoryProvider extends WellKnownDirectoryProvider {

	@Override
	public int priority() {
		// higher than OsxWellKnownDirectoryProvider
		return super.priority() + 1;
	}

	@Override
	public String[] directories() {
		return new String[] {
		    	// Add the directory that includes fishwatchr.jar
				"./VLC.app/Contents/MacOS/lib",
				// The following entry is removed, because it causes a kind of violation on macOS (when used as App) and .
				// The removal means that fishwatchr.jar can't be executed from different directories like "java -jar xxx/fishwatchr.jar
//				Paths.get("./" + System.getProperty("java.class.path")).getParent().resolve("VLC.app/Contents/MacOS/lib").toString(),
	            "/Applications/VLC.app/Contents/Frameworks",
	            "/Applications/VLC.app/Contents/MacOS/lib"
	    };
	}

	@Override
	public boolean supported() {
		return RuntimeUtil.isMac();
	}

}
