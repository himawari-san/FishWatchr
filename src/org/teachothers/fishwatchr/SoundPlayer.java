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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JOptionPane;

import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;


public class SoundPlayer extends Thread {
	public static final int LIMIT_RECODING_TIME = 60 * 60 * 2; // 7200sec = 2hours
	public static String SOUNDFILE_EXTENSION = ".wav";
	private static String[] videoAspectRates = {"16:9", "4:3", "1:1", "16:10", "2.21:1", "2.35:1", "2.39:1", "5:4"};
	private static String[] MEDIA_FILE_EXTENSIONS = { "asf", "avi", "flv", "mov", "mp3", "mp4", "mts", "oga", "ogg", "ogv", "ogx", "wav", "wma", "wmv"};
	private final static int MAX_RETRY_REFERRING_DATA = 100;  
	private final static int RETRY_INTERVAL = 50; // msec  
	
	public final static int PLAYER_STATE_STOP = 0;
	public final static int PLAYER_STATE_RECORD = 1;
	public final static int PLAYER_STATE_PAUSE = 2;
	public final static int PLAYER_STATE_RESUME = 3;
	public final static int PLAYER_STATE_INITALIZED = 0;
	public final static int PLAYER_STATE_PLAY = 4;
	public final static int PLAYER_STATE_STOPPING = 5;

	public final static int PLAYER_TYPE_DEFAULT = 0; // Java Sound 
	public final static int PLAYER_TYPE_VLC = 1; // VLC
	
	
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
	
//	public static int playerType = PLAYER_TYPE_VLC;
	public static int playerType = PLAYER_TYPE_DEFAULT;
	public static boolean isSoundBufferEnable = false;
	
	private AudioFormat linearFormat;
	private DataLine.Info info;

	private byte[] buf; 
	private int maxDataSize;
			
	private Date startTime;
	
	private int state = PLAYER_STATE_STOP;
	private TargetDataLine targetDataLine;
	private SoundGraphBuffer soundGraphBuf;
	
	private VLCDirectMediaPlayerComponent mediaPlayerComponent;
	private MediaPlayer mp;

	
	private String targetFilename;
	private float soundLength; // 収録時間(sec)
	
	private MainFrame mainFrame;

	private int skippedFrame;

	private Thread myThread;
	
	private boolean saveFlag = false;

	private MyMediaPlayerEventListener mpEventListener;
	
//	private float videoAspectRate = 4f/3f;
	private float videoAspectRate = 16f/9f;
	
	private boolean isStreaming = false;
	
	public SoundPlayer(MainFrame mainFrame) {
		this.mainFrame = mainFrame;
		soundGraphBuf = new SoundGraphBuffer((int) Math.ceil(LIMIT_RECODING_TIME / frameLength));
		try {
			mediaPlayerComponent = new VLCDirectMediaPlayerComponent();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		init();
	}
	
	
	public void resizeMediaPlayer(int width, int height){
		long now = 0;
		int currentState = getPlayerState();
		if(mp != null){
			now = mp.getTime();
		}
		mediaPlayerComponent.setSize(new Dimension(width, height));

		if(mp != null) mp.release();
		
		mp = mediaPlayerComponent.getMediaPlayer(videoAspectRate);
        mp.addMediaPlayerEventListener(mpEventListener);

        if(playerType == PLAYER_TYPE_DEFAULT){
    		mainFrame.changeState(state);
        	System.err.println("return: " + state);
			return;
		}
		
        
		state = currentState;
		if(currentState == PLAYER_STATE_PAUSE){
			mp.startMedia(targetFilename);
			mp.setTime(now);
			mp.pause();
		} else if(currentState == PLAYER_STATE_PLAY){
			mp.startMedia(targetFilename);
			if (isStreaming) {
				for (int i = 0; i < MAX_RETRY_REFERRING_DATA; i++) {
					if (mp.isSeekable()) {
						break;
					} else {
						try {
							Thread.sleep(RETRY_INTERVAL);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
			mp.setTime(now);
		}
		mainFrame.changeState(state);
	}

	
	public void setVideoAspectRatio(float videoAspectRatio){
		Dimension componentSize = mediaPlayerComponent.getSize();
		this.videoAspectRate = videoAspectRatio;
		resizeMediaPlayer(componentSize.width, componentSize.height);
	}
	
	public String[] getAvailableVideoAspectRatio(){
		return videoAspectRates;
	}
	
	
	// すべての設定を初期化
	public void init(){
		targetFilename = "";
		startTime = null;
		state = PLAYER_STATE_STOP;
		skippedFrame = 0;
		soundGraphBuf.clear();
		soundLength = -1;
		saveFlag = false;
//		if(mp != null) mp.release();
		mp = mediaPlayerComponent.getMediaPlayer();
		mediaPlayerComponent.clearDisplay();
		mpEventListener = new MyMediaPlayerEventListener();
	}

	
	public void initState(){
		skippedFrame = 0;
		soundGraphBuf.setPosition(0);
		if(playerType == PLAYER_TYPE_VLC){
//			setP
		}
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
	
	public boolean setFile(String filename){
		init();

		isStreaming = false; // default
		setSoundBufferEnable(false);
		targetFilename = filename;
		if(targetFilename.toLowerCase().endsWith(".xml")){
			// ファイル名だけセットするということでいいか？
			return true;
		} else if(targetFilename.toLowerCase().endsWith(".wav")){
			playerType = PLAYER_TYPE_DEFAULT;
			readWavInfo(targetFilename);
			buf = new byte[maxDataSize]; 
			readWav(targetFilename, buf);
			setSoundBufferEnable(true);
		} else if(targetFilename.startsWith("http://") || targetFilename.startsWith("file://") || targetFilename.startsWith("https://")){
			int aaaaa;
			isStreaming = true;
			playerType = PLAYER_TYPE_VLC;
			mp.startMedia(targetFilename);
			Dimension videoDimension = null;
			for(int i = 0; i < MAX_RETRY_REFERRING_DATA; i++){
				videoDimension = mp.getVideoDimension();
				if(videoDimension != null && videoDimension.height != 0 && videoDimension.width != 0){
					soundLength = (float)(mp.getLength()/1000);
					mp.release();
					mp = mediaPlayerComponent.getMediaPlayer(videoAspectRate);
			        mp.addMediaPlayerEventListener(mpEventListener);
					mp.startMedia(targetFilename);
					break;
				} else {
					try {
						Thread.sleep(RETRY_INTERVAL);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			if(videoDimension == null){
				mp.stop();
				return false;
			}
		} else {
			playerType = PLAYER_TYPE_VLC;

			// wav データの読み込み
			String wavFilename = targetFilename + ".wav";
			if(new File(wavFilename).exists()){
				readWavInfo(wavFilename);
				buf = new byte[maxDataSize];
				readWav(wavFilename, buf);
				setSoundBufferEnable(true);
			} else {
				int selectedValue = JOptionPane.showConfirmDialog(mainFrame, "音声波形の表示を行うために，wavファイルを生成しますか？");
				if(selectedValue == JOptionPane.CANCEL_OPTION){
					return false;
				} else if(selectedValue == JOptionPane.YES_OPTION){
					JOptionPane.showMessageDialog(mainFrame, "wav ファイル（" + wavFilename  + "）を生成します。\n生成には数分かかる場合があります。");
					mp.removeMediaPlayerEventListener(mpEventListener);
					mp.startMedia(targetFilename, ":sout=#transcode{acodec=s16l,channels=2,samplerate=44100,ab=128}:standard{access=file,mux=wav,dst=" + wavFilename + "}");
					while(true){
						if(!mp.isPlaying()){
							break;
						}
						try {
							Thread.sleep(RETRY_INTERVAL);
						} catch (InterruptedException e) {
							e.printStackTrace();
							return false;
						}
					}
					JOptionPane.showMessageDialog(mainFrame, wavFilename + "が生成されました。");
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
			
//			try {
//				join();
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}

			mp.startMedia(targetFilename);
			Dimension videoDimension = null;
			for(int i = 0; i < MAX_RETRY_REFERRING_DATA; i++){
				videoDimension = mp.getVideoDimension();
				if(videoDimension != null && videoDimension.height != 0 && videoDimension.width != 0){
					mp.release();
					mp = mediaPlayerComponent.getMediaPlayer(videoAspectRate);
			        mp.addMediaPlayerEventListener(mpEventListener);
					mp.startMedia(targetFilename);
					soundLength = (float)(mp.getLength()/1000);
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
				mp.stop();
				return false;
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
			System.err.println("rate: " + sampleRate + ", " + "bits: " + sampleSizeInBits + ", " + 
					"channels: " + channels + ", " + "maxDataSize: " + maxDataSize);
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
	        	soundGraphBuf.add(buf, nRead, channels);
	        	if(nRead != buf.length){
	        		System.err.println("nRead: " + nRead);
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
	
	
	public void start(){
		myThread = new Thread(this);
		myThread.start();
	}

	public void run() {
//	public synchronized void run() {
		if(state == PLAYER_STATE_RECORD){
			System.err.println("record!");
			record();
		} else if(state == PLAYER_STATE_PLAY){
			play();
		}
	}


	public void record(){
		saveFlag = false;

		String tmpTargetFilename = targetFilename + ".tmp";
		File tmpFile;
		
		try {
			// ターゲットデータラインを取得する
			info = new DataLine.Info(TargetDataLine.class, linearFormat);

			targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
			
			// ターゲットデータラインをオープンする
			targetDataLine.open(linearFormat);
			System.err.println(targetDataLine.getBufferSize());
			// マイク入力開始
			targetDataLine.start();

			// ターゲットデータラインから入力ストリームを取得する
			AudioInputStream audioStream = new AudioInputStream(targetDataLine);

			// 音声出力用テンポラリファイル
			tmpFile = new File(tmpTargetFilename);
			FileOutputStream fos = new FileOutputStream(tmpFile);

			int nRead;
			int sumRead = 0;
			while (state == PLAYER_STATE_RECORD) {
				nRead = audioStream.read(buf, 0, buf.length);
				fos.write(buf, 0, nRead);
				soundGraphBuf.add(buf, nRead, channels);
				sumRead += nRead;
			}

			fos.close();
			
			// マイク入力停止
			// ターゲットデータラインをクローズする
			targetDataLine.close();
			System.out.println("マイク入力停止");
			
			
			// wav ファイルへ書き出す
			File audioFile = new File(targetFilename);
			FileInputStream fis = new FileInputStream(tmpTargetFilename);
			AudioInputStream audioStream2 = new AudioInputStream(fis, linearFormat, sumRead/linearFormat.getFrameSize());
			AudioSystem.write(audioStream2, AudioFileFormat.Type.WAVE, audioFile);
			fis.close();
			audioStream.close();
			System.out.println("ファイル書き込み終了");
			tmpFile.delete();
			System.out.println("一時ファイル削除");
			saveFlag = true;
			initCallback();
		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean isSaved(){
		return saveFlag;
	}


	public synchronized void play(){

		try {
	        AudioInputStream ais = AudioSystem.getAudioInputStream(new File(targetFilename));
	        
			// ターゲットデータラインを取得する
			info = new DataLine.Info(SourceDataLine.class, linearFormat);
			SourceDataLine source = (SourceDataLine)AudioSystem.getLine(info);
	        // ソースデータラインを開く
	        source.open(linearFormat);
	        // スピーカー出力開始
	        source.start();

	        while(state != PLAYER_STATE_STOPPING){
	        	if(state == PLAYER_STATE_PAUSE){
	        		wait();
	        	}
	        	
	        	if(skippedFrame < 0){
	        		source.flush();
	        		ais.close();
	    	        ais = AudioSystem.getAudioInputStream(new File(targetFilename));
	    	        int newFrame = soundGraphBuf.getPosition() + skippedFrame;
	    	        if(newFrame < 0){
	    	        	newFrame = 0;
	    	        }

	    	        ais.skip(newFrame * buf.length);
	    	        soundGraphBuf.setPosition(newFrame);

	    	        skippedFrame = 0;
	    	        continue;
	        	}

				int nRead = ais.read(buf);
				if(nRead == -1){
					break;
				}

				soundGraphBuf.add(buf, nRead, channels);

				// skip forwards
				if(skippedFrame > 0){
					skippedFrame--;
					continue;
				}

				source.write(buf, 0, nRead);
	        }
	        ais.close();
    		source.flush();
	        source.close();
	        initCallback();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}


	// 再生終了時に制御を mainFrame に戻す
	private void initCallback(){
		skippedFrame = 0;
		soundGraphBuf.setPosition(0);
		if(playerType == PLAYER_TYPE_VLC){
			mp.setPosition(0);
		}
        mainFrame.changeState(PLAYER_STATE_STOP);
        System.err.println("initcall state stop");
    	state = PLAYER_STATE_STOP;
	}
	
	public void playVlc(){
//		System.err.println("sub:" + mp.subItemCount() + ", " + mp.subItemIndex());
		if(isStreaming){
			mp.playSubItem(0, "");
			for (int i = 0; i < MAX_RETRY_REFERRING_DATA; i++) {
				if (mp.isSeekable()) {
					break;
				} else {
					try {
						Thread.sleep(RETRY_INTERVAL);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		} else {
			mp.play();
//			mp.start();
		}
	}

	public void stopVlc(){
		if(mp != null && mp.isPlayable()){
	        mp.stop();
			mediaPlayerComponent.clearDisplay();
		}
	}

	public void pauseVlc(){
		if(mp.isPlaying()){
			mp.pause();
		}
	}

	public long getCurrentRecordingPosition(){
		return targetDataLine.getLongFramePosition();
	}
	
	public int getPlayerState(){
		return state;
	}

	public void myRecord(String filename, boolean withSoundFile){
		state = PLAYER_STATE_RECORD;
		playerType = PLAYER_TYPE_DEFAULT;
		startTime = new Date();
		targetFilename = filename;
		if(withSoundFile){
			start();
		}
	}
	

	public void myPlay(){
		state = PLAYER_STATE_PLAY;
		if(playerType == PLAYER_TYPE_DEFAULT){
			start();
		} else {
			playVlc();
		}
	}

	
	public void myStop(){
		if(playerType == PLAYER_TYPE_VLC){
			stopVlc();
		} else if(state == PLAYER_STATE_PLAY){
			state = PLAYER_STATE_STOPPING;
		} else {
			state = PLAYER_STATE_STOP;
		}

		// STOP になるまで待つ
		int c = 0;
		while(state == PLAYER_STATE_STOPPING){
			System.err.println("stopping");
			try {
				Thread.sleep(RETRY_INTERVAL);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(c++ > 30) break; // 永久ループ防止（3sec）
		}
	}

	public void myPause(){
		state = PLAYER_STATE_PAUSE;
		if(playerType == PLAYER_TYPE_VLC){
			pauseVlc();
		}
	}

	public synchronized void myResume(){
		state = PLAYER_STATE_PLAY;
		if(playerType == PLAYER_TYPE_VLC){
			mp.play();
		} else {
			notify();
		}
	}
	
	public void myJoin(){
		if(myThread == null) return;
		
		try {
			myThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

//	public void setTargetFilename(String targetFilename){
//		this.targetFilename = targetFilename;
//	}


	public static String[] getPlayableFileExtensions(){
		return MEDIA_FILE_EXTENSIONS;
	}

	
	public static boolean isPlayable(String filename){
		for(String extension: MEDIA_FILE_EXTENSIONS){
			if(filename.toLowerCase().endsWith(extension.toLowerCase())){
				return true;
			}
		}
		return false;
	}
	
	
	public void forward(int msec){
		skippedFrame = (int)(msec / frameLength /1000);
		if(playerType == PLAYER_TYPE_VLC){		
			mp.skip(msec);
		}
	}

	public void backward(int msec){
		skippedFrame = - (int)(msec / frameLength / 1000);
		if(playerType == PLAYER_TYPE_VLC){
			mp.skip(-msec);
		}
	}
	
	public void setPlayPoint(long msec){
		if(playerType == PLAYER_TYPE_VLC){
			mp.setTime(msec);
		} else {
			skippedFrame = (int)(msec/frameLength/1000) - soundGraphBuf.getPosition();
		}
	}
	
	
	public void setPlayRate(float rate){
		if(playerType == PLAYER_TYPE_VLC && mp.isPlaying()){
			mp.setRate(rate);
		}
	}
	
	public double getFrameLength(){
		return frameLength;
	}
	
	public int getPlayerType(){
		return playerType;
	}
	
	public VLCDirectMediaPlayerComponent getMediaplayerComponent(){
		return mediaPlayerComponent;
	}

	
	public void updateVlcInfo(){
//		if(mp == null) return;
		if(mp == null || !mp.isPlayable()) return;
		soundGraphBuf.setPosition((int)(mp.getTime() /1000 / frameLength));
	}

	
    private class MyMediaPlayerEventListener extends MediaPlayerEventAdapter {
        public void finished(MediaPlayer mediaPlayer) {
        	if(playerType == PLAYER_TYPE_VLC){
        		System.err.println("vlc finish!");
        		if(mp.isSeekable()){
            		initCallback();
        		}
        	}
        }
        
        public void stopped(MediaPlayer mediaPlayer){
        	if(playerType == PLAYER_TYPE_VLC){
        		System.err.println("vlc stop");
        		initCallback();
        	}
        }
    }
}	
