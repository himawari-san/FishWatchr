package org.teachothers.fishwatchr;

import java.io.UnsupportedEncodingException;
import java.util.Locale;

public class CaptureDevice {
	private String deviceID;
	private String name;
	private int type;
    private static String os = System.getProperty("os.name").toLowerCase();
    public static final int TYPE_VIDEO = 0;
    public static final int TYPE_AUDIO = 1;
    public static final int TYPE_NONE = -1;
    public static final String LABEL_NONE = "なし";

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
			return true;
		} else if(type == TYPE_AUDIO){
        	if(os.contains("windows")){
    			if(Locale.getDefault().toString().equals(Locale.JAPAN.toString())){
    				try {
    					deviceID = new String(deviceID.getBytes("ISO-8859-1"), "MS932");
    				} catch (UnsupportedEncodingException e) {
    					e.printStackTrace();
    				}
    			} else {
    				System.err.println("Warning(CaptureDevice): This device name may be garbled: " +  deviceID);
    			}
				name = deviceID; // attention;
        		
        		if(deviceID.startsWith("マイク") || deviceID.toLowerCase().startsWith("mic")){
        			return true;
        		} else {
        			return false;
        		}
        	} else {
    			String normalizedDeviceID = deviceID.toLowerCase();
				if (normalizedDeviceID.startsWith("hdmi ")
						|| normalizedDeviceID.startsWith("port ")
						|| normalizedDeviceID.startsWith("default ")
						|| normalizedDeviceID.startsWith("built-in output")) {
        			return false;
        		}
    			return true;
        	}
		} else {
			return false;
		}
	}
	
	
	public static String getMRL(CaptureDevice videoDevice, CaptureDevice audioDevice){
		String mrl = "";

		if(videoDevice.type == TYPE_NONE && audioDevice.type == TYPE_NONE){
			return mrl;
		}
		
		
		if(os.contains("windows")){
			mrl = "dshow:// ";
		} else if(os.contains("mac")){
			mrl = "qtcapture://" + videoDevice.getDeviceID();
		} else if(os.contains("nux")){
        	if(videoDevice.type == TYPE_NONE){
        		// audio only
        		mrl = "alsa://" + audioDevice.getDeviceID().replaceFirst(".*\\[(.+?)\\].*", "$1");
        	} else {
        		// video(and audio)
        		mrl = "v4l2://" + videoDevice.getDeviceID();
        	}
        }
		
		return mrl;
	}

	
	public static String[] getOption(CaptureDevice videoDevice, CaptureDevice audioDevice, String filename){
		String[] options = new String[3];
		
		if(videoDevice.type == TYPE_NONE && audioDevice.type == TYPE_NONE){
			return null;
		}
		
		
		if(os.contains("windows")){
        	if(videoDevice.type == TYPE_NONE){
        		// audio only
        		options[0] = " :sout=#transcode{vcodec=none,acodec=s16l,ab=128,channels=2,samplerate=44100}:duplicate{dst=file{dst=" + filename  + "}}";
        		options[1] = " :dshow-vdev=None :dshow-adev=" + audioDevice.getDeviceID() + " :live-caching=300";
        	} else if(audioDevice.type == TYPE_NONE){
        		// video only
        		options[0] = " :sout=#transcode{vcodec=mp2v,acodec=none,ab=128,scale=1,channels=2,deinterlace,audio-sync,samplerate=44100}:duplicate{dst=file{dst=" + filename  + "},dst=display}";
        		options[1] = " :dshow-vdev=" + videoDevice.getName() + " :dshow-adev=None :live-caching=300";
        	} else {
        		// video and audio
//        		options[0] = ":sout=#transcode{vcodec=mp2v,acodec=mp2a,ab=128,scale=1,channels=2,deinterlace,audio-sync}:duplicate{dst=standard{access=file,mux=ps,dst=\"" + "test"  + ".mpg\"},dst=display{noaudio}}";
        		options[0] = ":sout=#transcode{vcodec=h264,venc=h264,chrome=h264,acodec=mp2a,ab=128,channels=2}:duplicate{dst=standard{access=file,mux=ps,dst=\"" + filename  + ".mpg\"},dst=display{noaudio}}";
//        		options[0] = ":sout=#transcode{vcodec=mp2v,vb=1024,fps=15,width=320,acodec=mp2a,ab=128,scale=1,channels=2,deinterlace,audio-sync}:duplicate{dst=standard{access=file,mux=ps,dst=\"" + "test"  + ".mpg\"},dst=display{noaudio}}";
//        		+ " :dshow-vdev=" + videoDevice.getName() + " :dshow-adev= :live-caching=300";
//        		options[0] = " :sout=#transcode{vcodec=mp2v,vb=4096,scale=1,acodec=mpga,channels=2,samplerate=44100}:standard{access=file,dst=" + filename  + ".mpg}";
//        		options[1] = " :dshow-vdev=" + videoDevice.getName() + " :dshow-adev= :live-caching=300";
//        		options[0] = " :sout=#transcode{vcodec=mp4v,acodec=mpga,ab=128,channels=2,samplerate=44100}:duplicate{dst=file{dst=" + filename  + ".mp4},dst=display{noaudio}}";
//        		options[1] = " :dshow-vdev=" + videoDevice.getName() + " :dshow-adev=" + audioDevice.getName() + " :live-caching=300";
        		options[1] = " :dshow-vdev=" + videoDevice.getName();
//        		options[0] = " :sout=#transcode{vcodec=mp4v,acodec=mpga,ab=128,channels=2,samplerate=44100}:duplicate{dst=file{dst=" + filename  + ".mp4}}";
        	}
		} else if(os.contains("mac")){
        	if(videoDevice.type == TYPE_NONE){
        		// audio only
        		options[0] = " :sout=#transcode{vcodec=none,acodec=s16l,ab=128,channels=2,samplerate=44100}:duplicate{dst=file{dst=" + filename  + "}}";
        	} else if(audioDevice.type == TYPE_NONE){
        		// video only
        		options[0] = " :sout=#transcode{vcodec=mp2v,acodec=none,ab=128,channels=2,samplerate=44100}:duplicate{dst=file{dst=" + filename  + "},dst=display}";
        	} else {
        		// video and audio
        		options[0] = " :sout=#transcode{vcodec=mp2v,acodec=mpga,ab=128,channels=2,samplerate=44100}:duplicate{dst=file{dst=" + filename  + "},dst=display{noaudio}}";
        	}
		} else if(os.contains("nux")){
        	if(videoDevice.type == TYPE_NONE){
        		// audio only
        		options[0] = ":sout=#transcode{vcodec=none,acodec=s16l,ab=128,channels=2,samplerate=44100}:duplicate{dst=file{dst=" + filename  + "}}";
        	} else if(audioDevice.type == TYPE_NONE){
        		// video only
        		options[0] = ":sout=#transcode{vcodec=mp2v,acodec=none,ab=128,channels=2,samplerate=44100}:duplicate{dst=file{dst=" + filename  + "},dst=display}";
        	} else {
        		// video and audio
        		options[0] = ":sout=#transcode{vcodec=mp2v,acodec=mpga,ab=128,channels=2,samplerate=44100}:duplicate{dst=file{dst=" + filename  + "},dst=display{noaudio}}";
        		options[1] = ":input-slave=alsa://" + audioDevice.getDeviceID().replaceFirst(".*\\[(.+?)\\].*", "$1");
        	}
        }

		return options;
	}
	
	
	public static String getMediadataSuffix(CaptureDevice videoDevice, CaptureDevice audioDevice){
		if(videoDevice.type != CaptureDevice.TYPE_NONE){
			return ".mpg";
		} else if(audioDevice.type != CaptureDevice.TYPE_NONE){
			return ".wav";
		} else {
			return "";
		}
	}

	
}
