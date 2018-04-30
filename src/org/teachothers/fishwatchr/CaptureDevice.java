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

import java.io.UnsupportedEncodingException;
import java.util.Locale;

public class CaptureDevice {
	private String deviceID;
	private String name;
	private int type;
    private static String os = System.getProperty("os.name").toLowerCase(); //$NON-NLS-1$
    public static final int TYPE_VIDEO = 0;
    public static final int TYPE_AUDIO = 1;
    public static final int TYPE_NONE = -1;
    public static final String LABEL_NONE = Messages.getString("CaptureDevice.1"); //$NON-NLS-1$

	public CaptureDevice(String deviceID, String name, int type){
		this.deviceID = deviceID;
		this.name = name;
		this.type = type;
	}
	
	public String getName(){
		return name;
	}
	
	public String getDeviceID(){
		return deviceID;
	}
	
	public int getType(){
		return type;
	}
	
	
	
	public boolean validate(){
		if(type == TYPE_VIDEO){
	    	if(os.contains("windows")){ //$NON-NLS-1$
	    		deviceID = name; // attention (Windows's deviceIDs are useless);
	    		return false;
	    	}
			return true;
		} else if(type == TYPE_AUDIO){
        	if(os.contains("windows")){ //$NON-NLS-1$
        		// temporary codes to deal with Java Sound bugs
        		if(Locale.getDefault().toString().equals(Locale.JAPAN.toString())){
    				try {
    					deviceID = new String(deviceID.getBytes("ISO-8859-1"), "MS932"); //$NON-NLS-1$ //$NON-NLS-2$
    				} catch (UnsupportedEncodingException e) {
    					e.printStackTrace();
    				}
    			} else {
    				System.err.println("Warning(CaptureDevice): This device name may be garbled: " +  deviceID); //$NON-NLS-1$
    			}
				name = deviceID; // attention (Windows's names are useless)
        		
        		if(deviceID.startsWith("マイク") || deviceID.toLowerCase().startsWith("mic")){ //$NON-NLS-1$ //$NON-NLS-2$
        			name = "Default"; // temporary codes to deal with Java Sound bugs   //$NON-NLS-1$
        			deviceID = ""; // temporary codes to deal with Java Sound bugs //$NON-NLS-1$
        			return true;
        		} else {
        			return false;
        		}
        	} else {
    			String normalizedDeviceID = deviceID.toLowerCase();
				if (normalizedDeviceID.startsWith("hdmi ") //$NON-NLS-1$
						|| normalizedDeviceID.startsWith("port ") //$NON-NLS-1$
						|| normalizedDeviceID.startsWith("default ") //$NON-NLS-1$
						|| normalizedDeviceID.startsWith("built-in output")) { //$NON-NLS-1$
        			return false;
        		}
	        	if(os.contains("mac")){ //$NON-NLS-1$
        			name = "Default"; // temporary codes to deal with Java Sound bugs   //$NON-NLS-1$
        			deviceID = ""; // temporary codes to deal with Java Sound bugs //$NON-NLS-1$
	        	}
    			return true;
        	}
		} else {
			return false;
		}
	}
	
	
	public static String getMRL(CaptureDevice videoDevice, CaptureDevice audioDevice){
		String mrl = ""; //$NON-NLS-1$

		if(videoDevice.type == TYPE_NONE && audioDevice.type == TYPE_NONE){
			return mrl;
		}
		
		
		if(os.contains("windows")){ //$NON-NLS-1$
			mrl = "dshow://"; //$NON-NLS-1$
		} else if(os.contains("mac")){ //$NON-NLS-1$
        	if(videoDevice.type == TYPE_NONE){
        		// audio only
//    			mrl = "qtsound://" + audioDevice.getDeviceID(); // this code does not work.
    			mrl = "qtsound://"; // use a default device //$NON-NLS-1$
        	} else {
        		// video(and audio)
    			mrl = "qtcapture://" + videoDevice.getDeviceID(); //$NON-NLS-1$
        	}
		} else if(os.contains("nux")){ //$NON-NLS-1$
        	if(videoDevice.type == TYPE_NONE){
        		// audio only
        		mrl = "alsa://" + audioDevice.getDeviceID().replaceFirst(".*\\[(.+?)\\].*", "$1"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        	} else {
        		// video(and audio)
        		mrl = "v4l2://" + videoDevice.getDeviceID(); //$NON-NLS-1$
        	}
        }
		
		return mrl;
	}

	
	public static String[] getOption(CaptureDevice videoDevice, CaptureDevice audioDevice, String filename){
		String[] options = null;
		
		if(videoDevice.type == TYPE_NONE && audioDevice.type == TYPE_NONE){
			return null;
		}
		
		
		if(os.contains("windows")){ //$NON-NLS-1$
        	if(videoDevice.type == TYPE_NONE){
        		// audio only
        		options = new String[]{
        				":sout=#transcode{vcodec=none,acodec=s16l,ab=128,channels=2,samplerate=44100}:standard{access=file,mux=wav,dst=" + filename  + "}}", //$NON-NLS-1$ //$NON-NLS-2$
        				":dshow-vdev=none", //$NON-NLS-1$
        				":dshow-adev=" + audioDevice.getDeviceID() //$NON-NLS-1$
        		};
        	} else if(audioDevice.type == TYPE_NONE){
        		// video only
        		options = new String[]{
        				":sout=#transcode{vcodec=mp2v,acodec=none,scale=1,deinterlace}:duplicate{dst=file{dst=" + filename  + "},dst=display}", //$NON-NLS-1$ //$NON-NLS-2$
        				":dshow-vdev=" + videoDevice.getName(), //$NON-NLS-1$
                		":dshow-adev=none", //$NON-NLS-1$
                		":live-caching=300" //$NON-NLS-1$
                		};
        	} else {
        		// video and audio
        		options = new String[]{
        				":sout=#transcode{vcodec=mp2v,acodec=mpga,samplerate=44100,ab=128,channels=2,deinterlace}:duplicate{dst=std{access=file,dst=" + filename + "},dst=display{noaudio}}", //$NON-NLS-1$ //$NON-NLS-2$
        				":dshow-vdev=" + videoDevice.getDeviceID(), //$NON-NLS-1$
        				":dshow-adev=" + audioDevice.getDeviceID(), //$NON-NLS-1$
        				":live-caching=300" //$NON-NLS-1$
        		};
        		//":sout=#transcode{vcodec=theo,acodec=vorbis}:duplicate{dst=std{access=file,dst=" + filename + "},dst=display{noaudio}}";
        	}
		} else if(os.contains("mac")){ //$NON-NLS-1$
        	if(videoDevice.type == TYPE_NONE){
        		// audio only
        		options = new String[]{
        				":sout=#transcode{vcodec=none,acodec=s16l}:standard{mux=wav,access=file,dst=" + filename  + "}" //$NON-NLS-1$ //$NON-NLS-2$
        		};
        	} else if(audioDevice.type == TYPE_NONE){
        		// video only
        		options = new String[]{
        				":sout=#transcode{vcodec=mp2v,acodec=none,ab=128,channels=2,samplerate=44100}:duplicate{dst=file{dst=" + filename  + "},dst=display}" //$NON-NLS-1$ //$NON-NLS-2$
        		};
        	} else {
        		// video and audio
        		options = new String[]{
        				":sout=#transcode{vcodec=mp2v,acodec=mpga,ab=128,channels=2,samplerate=44100,audio-sync}:duplicate{dst=file{dst=" + filename  + "},dst=display{noaudio}}", //$NON-NLS-1$ //$NON-NLS-2$
        				":input-slave=qtsound://"  // use a default device //$NON-NLS-1$
//        				":input-slave=qtsound://" + audioDevice.getDeviceID()
        		};
//        		options[1] = ":input-slave=qtsound://";
//        		options[1] = ":input-slave=qtsound://AppleHDAEngineInput:1B,0,1,0:1";
//        		options[1] = ":input-slave=qtcapture://" + videoDevice.getDeviceID();
//        		options[1] = ":input-slave=qtsound://" + audioDevice.getDeviceID();
        	}
		} else if(os.contains("nux")){ //$NON-NLS-1$
        	if(videoDevice.type == TYPE_NONE){
        		// audio only
        		options = new String[]{
        				":sout=#transcode{vcodec=none,acodec=s16l,ab=128,channels=2,samplerate=44100}:duplicate{dst=file{dst=" + filename  + "}}" //$NON-NLS-1$ //$NON-NLS-2$
        		};
        	} else if(audioDevice.type == TYPE_NONE){
        		// video only
        		options = new String[]{
        				":sout=#transcode{vcodec=mp2v,acodec=none,ab=128,channels=2,samplerate=44100}:duplicate{dst=file{dst=" + filename  + "},dst=display}" //$NON-NLS-1$ //$NON-NLS-2$
        		};
        	} else {
        		// video and audio
        		options = new String[]{
        				":sout=#transcode{vcodec=mp2v,acodec=mpga,ab=128,channels=2,samplerate=44100}:duplicate{dst=file{dst=" + filename  + "},dst=display{noaudio}}", //$NON-NLS-1$ //$NON-NLS-2$
        				":input-slave=alsa://" + audioDevice.getDeviceID().replaceFirst(".*\\[(.+?)\\].*", "$1") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        		};
        	}
        }

		return options;
	}
	
	
	public static String getMediadataSuffix(CaptureDevice videoDevice, CaptureDevice audioDevice){
		if(videoDevice.type != CaptureDevice.TYPE_NONE){
			return ".mpg"; //$NON-NLS-1$
		} else if(audioDevice.type != CaptureDevice.TYPE_NONE){
			return ".wav"; //$NON-NLS-1$
		} else {
			return ""; //$NON-NLS-1$
		}
	}
}
