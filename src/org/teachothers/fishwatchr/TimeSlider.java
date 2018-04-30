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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;

import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicSliderUI;

public class TimeSlider extends JSlider {

	private static final long serialVersionUID = 1L;
	private int marginLow = 7;
	private int marginHigh = 5;
	
	private boolean flagClick = false;
	public int vv = 0;

	public TimeSlider() {

		// SliderUI sui = getUI();
		// Class.forName(MetalSliderUI);
		// setUI(new SynthSliderUI(this) {
//		 setUI(new MetalSliderUI() {
		setUI(new CustomSliderUI(this) {
			// setUI(new BasicSliderUI(this) {
			protected void scrollDueToClickInTrack(int direction) {
				// this is the default behaviour, let's comment that out
				// scrollByBlock(direction);

				int value = getValue();
				if (getOrientation() == JSlider.HORIZONTAL) {
					value = this.valueForXPosition(getMousePosition().x);
				} else if (getOrientation() == JSlider.VERTICAL) {
					value = this.valueForYPosition(getMousePosition().y);
				}
				setValue(value);
			}
		});
	}


	public boolean getFlag() {
		return flagClick;
	}

	
	public void clearFlag() {
		flagClick = false;
	}

	
	public void setTipTime(int time, int x){
		// time (sec)
		int maxX = getWidth() - marginLow - marginHigh;
		
		x -= marginLow;
		if(x < 0){
			x = 0;
		}
		if(x > maxX){
			x = maxX;
		}
		
		time =  time * x / maxX;
		int hour = time / 3600;
		time -= hour * 3600;
		int minute = time / 60;
		int sec = time - minute * 60;
		
		setToolTipText(String.format("%02d:%02d:%02d", hour, minute, sec)); //$NON-NLS-1$
	}

	
// quoted from http://stackoverflow.com/questions/12293982/change-the-jslider-look-and-feel
	public class CustomSliderUI extends BasicSliderUI {

		private BasicStroke stroke = new BasicStroke(1f, BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_ROUND, 0f, new float[] { 1f, 2f }, 0f);

		public CustomSliderUI(JSlider b) {
			super(b);
		}

		@Override
		public void paint(Graphics g, JComponent c) {
			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			super.paint(g, c);
		}

		@Override
		protected Dimension getThumbSize() {
			return new Dimension(12, 16);
		}

		@Override
		public void paintTrack(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;
			Stroke old = g2d.getStroke();
			g2d.setStroke(stroke);
			g2d.setPaint(Color.BLACK);
			if (slider.getOrientation() == SwingConstants.HORIZONTAL) {
				g2d.drawLine(trackRect.x, trackRect.y + trackRect.height / 2,
						trackRect.x + trackRect.width, trackRect.y
								+ trackRect.height / 2);
			} else {
				g2d.drawLine(trackRect.x + trackRect.width / 2, trackRect.y,
						trackRect.x + trackRect.width / 2, trackRect.y
								+ trackRect.height);
			}
			g2d.setStroke(old);
		}

		@Override
		public void paintThumb(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;
			int x1 = thumbRect.x + 2;
			int x2 = thumbRect.x + thumbRect.width - 2;
			int width = thumbRect.width - 4;
			int topY = thumbRect.y + thumbRect.height / 2 - thumbRect.width / 3;
			GeneralPath shape = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
			shape.moveTo(x1, topY);
			shape.lineTo(x2, topY);
			shape.lineTo((x1 + x2) / 2, topY + width);
			shape.closePath();
			g2d.setPaint(new Color(81, 83, 186));
			g2d.fill(shape);
			Stroke old = g2d.getStroke();
			g2d.setStroke(new BasicStroke(2f));
			g2d.setPaint(new Color(131, 127, 211));
			g2d.draw(shape);
			g2d.setStroke(old);
		}
	}
}
