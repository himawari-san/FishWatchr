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

import javax.swing.JPanel;


public class SoundPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	public static final int BAR_WIDTH = 2; // グラフの棒の幅(dot)
	private static int Y_ORIGIN = 75; // y原点(y=75のとき原点になる)
	
	private SoundPlayer soundPlayer;
	private short[] soundBuf;
	private int p;
	

	public SoundPanel(SoundPlayer soundPlayer){
		super();
		this.soundPlayer = soundPlayer;
		soundBuf = soundPlayer.getSoundGraphBuffer().getBuf();
	}

	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
//		int width = getSize().width; // window 幅
		int x0 = getSize().width / 2; // 原点座標
		int r = x0 / BAR_WIDTH; // 原点左右の描画数
		p = soundPlayer.getCurrentFrame();
		int pStart = p - r;
		int pEnd = p + r;
		
		// 原点の目盛り
		g.drawLine(0, Y_ORIGIN+4, x0*2, Y_ORIGIN+4);
		g.fillRect(x0, Y_ORIGIN, BAR_WIDTH, 4);


		int v;
		for(int i = pStart; i < pEnd; i++){
			if(i >= 0){
				v = soundBuf[i];
//				v = soundBuf[i] / 150;
//				System.err.println("v:" + v + ", " + i + ", " + soundBuf.length);
				if(v >= 0){
					g.fillRect((i-pStart) * BAR_WIDTH, Y_ORIGIN - v, BAR_WIDTH, v);
				} else {
					g.fillRect((i-pStart) * BAR_WIDTH, -v, BAR_WIDTH, Y_ORIGIN - v);
				}
			}
		}
	}
}
