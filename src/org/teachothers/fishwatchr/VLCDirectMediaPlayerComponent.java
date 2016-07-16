/*
    Copyright (C) 2014-2016 Masaya YAMAGUCHI

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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.openimaj.audio.AudioDevice;
import org.openimaj.audio.util.AudioUtils;
import org.openimaj.video.capture.Device;
import org.openimaj.video.capture.VideoCapture;

import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.direct.BufferFormat;
import uk.co.caprica.vlcj.player.direct.BufferFormatCallback;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;
import uk.co.caprica.vlcj.player.direct.RenderCallbackAdapter;
import uk.co.caprica.vlcj.player.direct.format.RV32BufferFormat;

public class VLCDirectMediaPlayerComponent extends JPanel {

	private static final long serialVersionUID = 1L;

	private BufferedImage image;

	// このコンポーネントのサイズ
	private int componentWidth = 512;
	private int componentHeight = 360;

	// 動画のサイズ
	private int imageWidth = componentWidth;
	private int imageHeight = componentHeight;
	
	private MediaPlayerFactory factory;
    private DirectMediaPlayer mediaPlayer;
    
    private String overlayStyles[] = {"なし", "上部", "中央", "下部"};
    private int iOverlaidTextStyle = 0;
    private Font overlaidTextFont = new Font(Font.SANS_SERIF, Font.PLAIN, 16);
    private int textHeight = 0;
    private int textDescent = 0;
    private FontMetrics fm;
    private String overlaidText = "";
    private Color bgColor;
    
    public VLCDirectMediaPlayerComponent() throws InterruptedException, InvocationTargetException {
//    	factory = new MediaPlayerFactory();
    	factory = new MediaPlayerFactory(new String[]{
//    			"--verbose=15",
//    			"--intf", "dummy",
//    			"--vout", "dummy",
//    			"--no-audio",
//    			"--no-stat",
//    			"--no-snapshot-preview",
//    			"--no-video-title-show",
//    			"--quiet",
//    			"--live-caching=50"
    	});
    	init();
    }

    
    public void init(){
    	System.err.println("imageWidth, imageHeight: " + imageWidth + "," + imageHeight);
    	image = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(imageWidth, imageHeight);
        image.setAccelerationPriority(1.0f);
        imageWidth = image.getWidth();
        imageHeight = image.getHeight();

        componentWidth = getWidth();
        componentHeight = getHeight();

    	if(mediaPlayer != null)	mediaPlayer.release();
    	
        mediaPlayer = factory.newDirectMediaPlayer(new VLCBufferFormatCallback(), new VLCRenderCallback());
        mediaPlayer.setPlaySubItems(true); // <--- This is very important for YouTube
        
        clearDisplay();
    }

    
    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        // パネルの中央に動画を表示
        int x0 = (componentWidth - imageWidth) / 2;
        int y0 = (componentHeight - imageHeight) / 2;
        g2.drawImage(image, null, x0, y0);
        
		if (iOverlaidTextStyle != 0) {
			g2.setFont(overlaidTextFont);

			if(fm == null){
				fm = g2.getFontMetrics();
				textHeight = fm.getHeight();
				textDescent = fm.getDescent();
				bgColor = Color.WHITE;
			}
			

			switch(iOverlaidTextStyle){
			case 0:
				return;
			case 1: // upside
				g2.setColor(bgColor);
				g2.fillRect(x0, 0, fm.stringWidth(overlaidText), textHeight);
				g2.setColor(Color.DARK_GRAY);
				g2.drawString(overlaidText, x0, textHeight-textDescent);
				break;
			case 2: // center
				g2.setColor(bgColor);
				g2.fillRect(x0, componentHeight/2, fm.stringWidth(overlaidText), textHeight);
				g2.setColor(Color.DARK_GRAY);
				g2.drawString(overlaidText, x0, componentHeight/2+textHeight-textDescent);
				break;
			case 3: // downside
				g2.setColor(bgColor);
				g2.fillRect(x0, componentHeight-textHeight-4, fm.stringWidth(overlaidText), textHeight);
				g2.setColor(Color.DARK_GRAY);
				g2.drawString(overlaidText, x0, componentHeight-textDescent-4);
				break;
			}
		}
    }

    
    public DirectMediaPlayer getMediaPlayer(){
    	return mediaPlayer;
    }
    

    public DirectMediaPlayer getMediaPlayer(float videoAspectRatio){
    	componentWidth = getWidth();
    	componentHeight = getHeight();
    	
    	float componentRatio = (float)componentWidth / (float)componentHeight;
    	
    	if(componentRatio < videoAspectRatio){
    		imageWidth = componentWidth;
    		imageHeight = (int)((float)componentWidth / videoAspectRatio);
    	} else {
    		imageWidth = (int)((float)componentHeight * videoAspectRatio);
    		imageHeight = componentHeight;
    	}
    	init();
    	return mediaPlayer;
    }
    
    
    public void clearDisplay(){
        Graphics2D g2 = (Graphics2D)image.getGraphics();
    	if(g2 != null){
    		g2.setColor(getBackground());
    		g2.fillRect(0, 0, imageWidth, imageHeight);
    		repaint();
    	}
    }


    public void setMarquee(String text){
    	overlaidText = text;
    }
    
    
    public String[] getAvailableTextOverlayStyles(){
    	return overlayStyles; 
    }
    
    public void setTextOverlayStyle(int iOverlayStyle){
    	this.iOverlaidTextStyle = iOverlayStyle;
    }
    

    public List<CaptureDevice> getVideoDeviceList(){
        List<Device> videoDevices = VideoCapture.getVideoDevices();
        ArrayList<CaptureDevice> captureDevices = new ArrayList<CaptureDevice>();
        
        for(Device videoDevice: videoDevices){
        	CaptureDevice captureDevice = new CaptureDevice(videoDevice.getIdentifierStr(), videoDevice.getNameStr(), CaptureDevice.TYPE_VIDEO);
        	if(!captureDevice.validate()){
        		continue;
        	}
        	captureDevices.add(captureDevice);
        	System.err.println("vd(str): " + videoDevice.getNameStr());
        	System.err.println("vd(id): " + videoDevice.getIdentifierStr());
        }
    	captureDevices.add(new CaptureDevice(CaptureDevice.LABEL_NONE, CaptureDevice.LABEL_NONE, CaptureDevice.TYPE_NONE));

        return captureDevices;
    }


    public List<CaptureDevice> getAudioDeviceList(){
        List<AudioDevice> audioDevices = AudioUtils.getDevices();
        ArrayList<CaptureDevice> captureDevices = new ArrayList<CaptureDevice>();

        for(AudioDevice audioDevice: audioDevices){
        	CaptureDevice captureDevice = new CaptureDevice(audioDevice.deviceName, audioDevice.displayName, CaptureDevice.TYPE_AUDIO);
        	if(!captureDevice.validate()){
        		continue;
        	}
        	captureDevices.add(captureDevice);
        	System.err.println("ad(id): " + audioDevice.deviceName);
        	System.err.println("ad(dsp): " + audioDevice.displayName);
        	System.err.println("ad(string): " + audioDevice.toString());
        }
    	captureDevices.add(new CaptureDevice(CaptureDevice.LABEL_NONE, CaptureDevice.LABEL_NONE, CaptureDevice.TYPE_NONE));

        return captureDevices;
    }


    class VLCRenderCallback extends RenderCallbackAdapter {
        public VLCRenderCallback() {
            super(((DataBufferInt) image.getRaster().getDataBuffer()).getData());
        }

        @Override
        public void onDisplay(DirectMediaPlayer mediaPlayer, int[] data) {
            VLCDirectMediaPlayerComponent.this.repaint();
        }
    }

    class VLCBufferFormatCallback implements BufferFormatCallback {
        @Override
        public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
            return new RV32BufferFormat(imageWidth, imageHeight);
        }
    }
}
