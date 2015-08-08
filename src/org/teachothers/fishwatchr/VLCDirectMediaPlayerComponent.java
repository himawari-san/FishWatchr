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

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.swing.JPanel;

import uk.co.caprica.vlcj.binding.internal.libvlc_media_t;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
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
    
    public VLCDirectMediaPlayerComponent() throws InterruptedException, InvocationTargetException {
        factory = new MediaPlayerFactory();
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

//    	factory2 = new MediaPlayerFactory("--no-video");
    	if(mediaPlayer != null)	mediaPlayer.release();
        
    	
        mediaPlayer = factory.newDirectMediaPlayer(new VLCBufferFormatCallback(), new VLCRenderCallback());
        mediaPlayer.addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
            @Override
            public void buffering(MediaPlayer mediaPlayer, float newCache) {
                System.out.println("Buffering " + newCache);
            }

            @Override
            public void mediaSubItemAdded(MediaPlayer mediaPlayer, libvlc_media_t subItem) {
                List<String> items = mediaPlayer.subItems();
                System.out.println(items);
            }
        });

        mediaPlayer.setPlaySubItems(true); // <--- This is very important for YouTube
//        audioPlayer = factory2.newDirectAudioPlayer("S16N", 16000, 2, new VLCAudioCallbackAdapter());        
        clearDisplay();
    }

    
    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        // パネルの中央に動画を表示
        int x0 = (componentWidth - imageWidth) / 2;
        int y0 = (componentHeight - imageHeight) / 2;
        g2.drawImage(image, null, x0, y0);
    }

    
    public DirectMediaPlayer getMediaPlayer(){
    	return mediaPlayer;
    }
    

    public DirectMediaPlayer getMediaPlayer(float videoAspectRatio){
    	int componentWidth = getWidth();
    	int componentHeight = getHeight();

    	float componentRatio = (float)componentWidth / (float)componentHeight;
//		System.err.println("r: " + componentRatio + ", " + videoAspectRatio + ", " + componentHeight + ", " + componentWidth);
    	
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
