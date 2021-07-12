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

import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import uk.co.caprica.vlcj.player.base.Marquee;
import uk.co.caprica.vlcj.player.base.MarqueePosition;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventListener;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaListPlayerComponent;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;


public class SoundPlayer {
	public static final int LIMIT_RECODING_TIME = 60 * 60 * 2; // 7200sec = 2hours
	public static final String DEFAULT_VIDEO_ASPECT_RATIO = "default"; //$NON-NLS-1$
	public static String SOUNDFILE_EXTENSION = ".wav"; //$NON-NLS-1$
	private static String[] videoAspectRatios = {DEFAULT_VIDEO_ASPECT_RATIO, "16:9", "4:3", "1:1", "16:10", "2.21:1", "2.35:1", "2.39:1", "5:4"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
	private static String[] MEDIA_FILE_EXTENSIONS = { "asf", "avi", "flv", "mov", "mp3", "mp4", "mpg", "mts", "oga", "ogg", "ogv", "ogx", "wav", "wma", "wmv"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$ //$NON-NLS-12$ //$NON-NLS-13$ //$NON-NLS-14$ //$NON-NLS-15$
	private static String[] SOUND_FILE_EXTENSIONS = { "mp3", "oga", "wav", "wma"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	private final static int MAX_RETRY_REFERRING_DATA = 100;  
	private final static int RETRY_INTERVAL = 100; // msec  
    private final String textOverlayLabels[] = {Messages.getString("VLCDirectMediaPlayerComponent.0"), Messages.getString("VLCDirectMediaPlayerComponent.1"), Messages.getString("VLCDirectMediaPlayerComponent.2"), Messages.getString("VLCDirectMediaPlayerComponent.3")}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    private final MarqueePosition textOverlayPositions[] = {null, MarqueePosition.TOP_LEFT, MarqueePosition.CENTRE, MarqueePosition.BOTTOM_LEFT};
	
	public final static int PLAYER_STATE_STOP = 0;
	public final static int PLAYER_STATE_RECORD = 1;
	public final static int PLAYER_STATE_PAUSE = 2;
	public final static int PLAYER_STATE_RESUME = 3;
	public final static int PLAYER_STATE_PLAY = 4;
	public final static int PLAYER_STATE_FINISH_RECORDING = 5;
	public final static int PLAYER_STATE_READ_VIDEO_INFO = 6;

	public final static double defaultFrameLength = 0.25; // sec
	public final static float defaultSampleRate = 10000; // Hz
	public final static int defaultSampleSizeInBits = 16; // bits
	public final static int defaultChannels = 1; // モノラル
	public final static boolean defaultIsSigned = true;
	public final static boolean defaultIsBigEndian = false;

	public static double frameLength = defaultFrameLength;
	public static float sampleRate = defaultSampleRate;
	public static int sampleSizeInBits = defaultSampleSizeInBits;
	public static int channels = defaultChannels;
	public static boolean isSigned = defaultIsSigned;
	public static boolean isBigEndian = defaultIsBigEndian;
	
	public static boolean isSoundBufferEnable = false;
	
	private AudioFormat linearFormat;

	private byte[] buf; 
	private int maxDataSize;
			
	private Date startTime;
	
	private int state = PLAYER_STATE_STOP;
	private TargetDataLine targetDataLine;
	private SoundGraphBuffer soundGraphBuf;
	
	private EmbeddedMediaListPlayerComponent mediaPlayerComponent;
	private EmbeddedMediaPlayer mp;

	
	private String targetFilename;
	private float soundLength; // 収録時間(sec)
	
	private boolean saveFlag = false;

	private String defaultVideoAspectRatio = ""; // default ratio of the video file
	
	private boolean isStreaming = false;

	private MarqueePosition currentOverlayPosition = null;
    private TextOverlayInfo textOverlayInfo;

	
	public SoundPlayer(MediaPlayerEventListener mediaPlayerEventListener) {
		if(mediaPlayerEventListener==null)System.err.println("null");
		textOverlayInfo = new TextOverlayInfo(textOverlayPositions, textOverlayLabels);
		soundGraphBuf = new SoundGraphBuffer((int) Math.ceil(LIMIT_RECODING_TIME / frameLength));
		init();
		mediaPlayerComponent = new EmbeddedMediaListPlayerComponent();
		mp = mediaPlayerComponent.mediaPlayer();
		mp.events().addMediaPlayerEventListener(mediaPlayerEventListener);
	}
	
	public void release() {
		mediaPlayerComponent.release();
	}
	
	
	public void setSize(int width, int height) {
		mediaPlayerComponent.setSize(width, height);
	}
	

	public void setVideoAspectRatio(String videoAspectRatio){
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(videoAspectRatio.equals(DEFAULT_VIDEO_ASPECT_RATIO)) {
					mp.video().setAspectRatio(defaultVideoAspectRatio);
				} else {
					mp.video().setAspectRatio(videoAspectRatio);
				}
				mediaPlayerComponent.setSize(mediaPlayerComponent.getSize());
				
			}
		});
	}
	
	public String[] getAvailableVideoAspectRatio(){
		return videoAspectRatios;
	}

	
	public void setTextOverlayPosition(MarqueePosition position){
		if(position == null) {
			mp.marquee().enable(false);
			return;
		} 
		
		currentOverlayPosition = position;
		mp.marquee().enable(true);
	}
	
	public TextOverlayInfo getTextOverlayInfo(){
		return textOverlayInfo;
	}

	
	// すべての設定を初期化
	public void init(){
		targetFilename = ""; //$NON-NLS-1$
		startTime = null;
		state = PLAYER_STATE_STOP;
		soundGraphBuf.clear();
		soundLength = -1;
		saveFlag = false;
		defaultVideoAspectRatio = "";
	}

	
	public void initState(){
		soundGraphBuf.setPosition(0);
	}
	

	public void setDefaultRecordingParameters(){
		frameLength = defaultFrameLength;
		sampleRate = defaultSampleRate;
		sampleSizeInBits = defaultSampleSizeInBits;
		channels = defaultChannels;
		isSigned = defaultIsSigned;
		isBigEndian = defaultIsBigEndian;
		// リニアPCM 8000Hz 16bit モノラル 符号付き リトルエンディアン
		linearFormat = new AudioFormat(sampleRate, sampleSizeInBits, channels, isSigned, isBigEndian);
		maxDataSize = (int)(sampleRate * sampleSizeInBits / 8 * channels * frameLength);
		buf = new byte[maxDataSize];
//		soundGraphBuf.setFrameLength(frameLength);
	}
	
	public boolean setFile(String filename, boolean flagWaveform){
		init();

		isStreaming = false; // default
		setSoundBufferEnable(false);
		targetFilename = filename;
		if(targetFilename.toLowerCase().endsWith(".xml")){ //$NON-NLS-1$
			// ファイル名だけセットするということでいいか？
			return true;
		} else if(targetFilename.toLowerCase().endsWith(".wav")){ //$NON-NLS-1$
			readWavInfo(targetFilename);
			buf = new byte[maxDataSize]; 
			readWav(targetFilename, buf);
			setSoundBufferEnable(true);
			mp.media().start(targetFilename);
		} else if(targetFilename.startsWith("http://") || targetFilename.startsWith("file://") || targetFilename.startsWith("https://")){ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			isStreaming = true;
			if(!readVideoInfo(targetFilename)) {
				return false;
			}
		} else {
			// wav データの読み込み
			String wavFilename = targetFilename + ".wav"; //$NON-NLS-1$
			if(flagWaveform){
				if (new File(wavFilename).exists()) {
					readWavInfo(wavFilename);
					buf = new byte[maxDataSize];
					readWav(wavFilename, buf);
					setSoundBufferEnable(true);
				} else {
					JOptionPane.showMessageDialog(mediaPlayerComponent.getParent(),
							Messages.getString("SoundPlayer.0") + //$NON-NLS-1$
							"(" + wavFilename + ")" + //$NON-NLS-1$ //$NON-NLS-2$
							Messages.getString("SoundPlayer.3")); //$NON-NLS-1$
//					mp.events().removeMediaPlayerEventListener(mpEventListener);
					mp.media().start(
							targetFilename,
							":sout=#transcode{acodec=s16l,channels=2,samplerate=44100,ab=128}:standard{access=file,mux=wav,dst=" //$NON-NLS-1$
									+ wavFilename + "}"); //$NON-NLS-1$
					while (true) {
						if (!mp.status().isPlaying()) {
							break;
						}
						try {
							Thread.sleep(RETRY_INTERVAL);
						} catch (InterruptedException e) {
							e.printStackTrace();
							return false;
						}
					}
					JOptionPane.showMessageDialog(mediaPlayerComponent.getParent(), wavFilename
							+ Messages.getString("SoundPlayer.4")); //$NON-NLS-1$
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					soundGraphBuf.setPosition(0);
					readWavInfo(wavFilename);
					buf = new byte[maxDataSize];
					readWav(wavFilename, buf);
					setSoundBufferEnable(true);
				}
			}

			if (isSoundFile(targetFilename)) {
				mp.media().start(targetFilename);
				soundLength = (float) (mp.status().length() / 1000);
				mp.controls().stop();
//				// hey vlcj4
//				mp.release();
//				mp = mediaPlayerComponent.getMediaPlayer(videoAspectRatio);
//				mp.events().addMediaPlayerEventListener(mpEventListener);
				mp.media().prepare(targetFilename);
			} else {
				if(!readVideoInfo(targetFilename)) {
					return false;
				}
			}
		}
		soundGraphBuf.setPosition(0);
		return true;
	}

	
	
	public int readWavInfo(String filename){
		try {
			File mediaFile = new File(filename);
			linearFormat = AudioSystem.getAudioFileFormat(mediaFile).getFormat();
			sampleRate = linearFormat.getSampleRate();
			sampleSizeInBits = linearFormat.getSampleSizeInBits();
			channels = linearFormat.getChannels();
			isBigEndian = linearFormat.isBigEndian();
			isSigned = true;
			soundLength = AudioSystem.getAudioFileFormat(mediaFile).getFrameLength() / sampleRate;
			maxDataSize = (int)Math.ceil(sampleRate * sampleSizeInBits / 8 * channels * frameLength);
			System.err.println("rate: " + sampleRate + ", " + "bits: " + sampleSizeInBits + ", " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					"channels: " + channels + ", " + "maxDataSize: " + maxDataSize); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
			return -1;
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
		return maxDataSize;
	}


	
	public void setSoundBufferEnable(boolean flag){
		isSoundBufferEnable = flag;
	}

	public boolean getSoundBufferEnable(){
		return isSoundBufferEnable;
	}
	
	
	public void readWav(String filename, byte[] buf){
		try {
	        AudioInputStream ais = AudioSystem.getAudioInputStream(new File(filename));

	        int nRead;
	        while((nRead = ais.read(buf)) > 0){
	        	if(soundGraphBuf.add(buf, nRead, channels) < 0){
	        		System.err.println("Warning(SoundPlayer): exceeded SoundGraphBuffer size"); //$NON-NLS-1$
	        		break;
	        	}
	        	if(nRead != buf.length){
	        		System.err.println("nRead: " + nRead); //$NON-NLS-1$
	        	}
	        }
	        ais.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		}
	}
	
	
	public float getSoundLength(){
		return soundLength;
	}
	

	public boolean isSaved(){
		return saveFlag;
	}


	public long getCurrentRecordingPosition(){
		return targetDataLine.getLongFramePosition();
	}
	
	public int getPlayerState(){
		return state;
	}

	public void setPlayerState(int state){
		this.state = state;
	}

	public void myRecord(String filename, boolean withSoundFile, CaptureDevice videoDevice, CaptureDevice audioDevice){
		state = PLAYER_STATE_RECORD;
		startTime = new Date();
		targetFilename = filename;
		if(withSoundFile){
	        String mrl = CaptureDevice.getMRL(videoDevice, audioDevice);
	        String[] options = CaptureDevice.getOption(videoDevice, audioDevice, targetFilename);

	        mp.media().start(mrl, options);
		}
	}
	
	
	public void myPlay(){
		state = PLAYER_STATE_PLAY;

		if(isStreaming){
			mp.media().start(targetFilename);
			for (int i = 0; i < MAX_RETRY_REFERRING_DATA; i++) {
				if (mp.status().isSeekable()) {
					return;
				} else {
					try {
						Thread.sleep(RETRY_INTERVAL);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			myStop();
		} else {
			mp.controls().play();
		}
	}

	
	public void myStop(){
		if(getPlayerState() == PLAYER_STATE_RECORD) {
			setPlayerState(PLAYER_STATE_FINISH_RECORDING);
		} else {
			setPlayerState(PLAYER_STATE_STOP);
		}
		
		if(mp != null && mp.status().isPlayable()){
	        mp.controls().stop();
	        initState();
		}
	}

	public void myPause(){
		state = PLAYER_STATE_PAUSE;

		if(mp.status().isPlaying()){
			mp.controls().pause();
		}
	}

	public void myResume(){
		state = PLAYER_STATE_PLAY;
		myPlay();
	}
	

	public SoundGraphBuffer getSoundGraphBuffer(){
		return soundGraphBuf;
	}
	
	
	public Date getStartTime(){
		return startTime;
	}
	
	// 再生時の経過時間（msec）
	public int getElapsedTime(){
		if(state == PLAYER_STATE_RECORD){
			return getCurrentRecordingTime();
		} else {
			return (int)(soundGraphBuf.getPosition() * frameLength * 1000);
		}
	}
	
	// startTime からの経過時間録音オフ時のアノテーション経過時間（msec）
	public int getCurrentRecordingTime(){
		if(startTime == null){
			return 0;
		}
		Date now = new Date();
		return (int)(now.getTime() - startTime.getTime());
	}
	

	// 経過フレーム数
	public int getCurrentFrame(){
		if(state == PLAYER_STATE_RECORD){
			return (int)(getCurrentRecordingTime()/frameLength/1000);
		} else {
			return soundGraphBuf.getPosition();
		}
	}
	
	
	public String getTargetFilename(){
		return targetFilename;
	}


	public static String[] getPlayableFileExtensions(){
		return MEDIA_FILE_EXTENSIONS;
	}

	
	public static boolean isPlayable(String filename){
		if(filename.startsWith("http://") || filename.startsWith("file://") || filename.startsWith("https://")){ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return false;
		}

		for(String extension: MEDIA_FILE_EXTENSIONS){
			if(filename.toLowerCase().endsWith(extension.toLowerCase())){
				return true;
			}
		}
		return false;
	}

	
	public static boolean isSoundFile(String filename){
		if(filename.startsWith("http://") || filename.startsWith("https://")){ //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		}

		for(String extension: SOUND_FILE_EXTENSIONS){
			if(filename.toLowerCase().endsWith(extension.toLowerCase())){
				return true;
			}
		}
		return false;
	}

	
	
	public void forward(int msec){
		mp.controls().skipTime(msec);
	}

	public void backward(int msec){
		mp.controls().skipTime(-msec);
	}
	
	public void setPlayPoint(long msec){
		mp.controls().setTime(msec);
	}
	
	
	public void setPlayRate(float rate){
		if(mp.status().isPlaying()){
			mp.controls().setRate(rate);
		}
	}
	
	public double getFrameLength(){
		return frameLength;
	}
	
	public EmbeddedMediaListPlayerComponent getMediaplayerComponent(){
		if(mediaPlayerComponent == null) {System.err.println("null");}
		return mediaPlayerComponent;
	}

	
	public void updateVlcInfo(){
//		if(mp == null) return;
		if(mp == null || !mp.status().isPlayable()) return;
		soundGraphBuf.setPosition((int)(mp.status().time() /1000 / frameLength));
	}

	
	public void setOverlayText(String text){
		if(currentOverlayPosition == null) {
			return;
		}
		
		mp.marquee().enable(true);
		Marquee.marquee()
		.opacity(255)
	     .position(currentOverlayPosition)
	     .colour(Color.WHITE)
	     .text(text)
	     .size(40)
	     .apply(mp);
	}
	
	
    public List<CaptureDevice> getVideoDeviceList(){
        ArrayList<CaptureDevice> captureDevices = new ArrayList<CaptureDevice>();
    	captureDevices.add(new CaptureDevice(CaptureDevice.LABEL_NONE, CaptureDevice.LABEL_NONE, CaptureDevice.TYPE_NONE));
        return captureDevices;
    }

    public List<CaptureDevice> getAudioDeviceList(){
        ArrayList<CaptureDevice> captureDevices = new ArrayList<CaptureDevice>();
    	CaptureDevice captureDevice = new CaptureDevice("", "Default", CaptureDevice.TYPE_AUDIO); //$NON-NLS-1$ //$NON-NLS-2$
    	captureDevices.add(captureDevice);
    	captureDevices.add(new CaptureDevice(CaptureDevice.LABEL_NONE, CaptureDevice.LABEL_NONE, CaptureDevice.TYPE_NONE));

        return captureDevices;
    }
	

	private boolean readVideoInfo(String targetFilename) {
		Dimension videoDimension = null;
		setPlayerState(PLAYER_STATE_READ_VIDEO_INFO);
		mp.media().start(targetFilename);
		for (int i = 0; i < MAX_RETRY_REFERRING_DATA; i++) {
			videoDimension = mp.video().videoDimension();
			if (videoDimension != null && videoDimension.height != 0
					&& videoDimension.width != 0) {
				soundLength = (float) (mp.status().length() / 1000);
				defaultVideoAspectRatio = mp.video().aspectRatio();
				break;
			} else {
				try {
					Thread.sleep(RETRY_INTERVAL);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		if(videoDimension == null) {
			myStop();
			return false;
		} else {
			return true;
		}
	}
	
	class TextOverlayInfo {
		MarqueePosition[] positions;
		String[] labels;

		public TextOverlayInfo (MarqueePosition[] positions, String[] labels){
			this.positions = positions;
			this.labels = labels;
		}
		
		public String getLabel(int i) {
			return labels[i];
		}
		
		public MarqueePosition getPosition(int i) {
			return positions[i];
		}
		
		public int count() {
			return positions.length;
		}
	}
}	
