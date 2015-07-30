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


public class SoundGraphBuffer {
	short[] buf;
	int pc;
	
	public SoundGraphBuffer(int limit){
		buf = new short[limit];
		pc = 0;
	}
	
	public int add(byte b[], int length, int channels){
		int c = 0;
		long sum = 0;
		short p = 0;
		int step = channels * 2;
		
		for(int i = 0; i < b.length-1 && pc < buf.length && i < length-1; i += step){
			p = (short)(((b[i+1] & 0xFF) << 8) | (b[i] & 0xFF));
			sum += Math.abs(p*p);
//			short p2 = (short)((b[i+1] << 8) | b[i]);
//			short p3 = (short)(((b[i+1]) << 8)  + (b[i] & 0xff));
//			if(p != p2 || p != p3){
//				System.err.println("p, p2, p3: " + p + "," + p2 + "," + p3);
//			}
//
			//			p = (short)(((b[i+1]) << 8)  | (b[i] & 0xff));
//			p = (short)(((b[i+1]) << 8)  + (b[i] & 0xff));
			c++;
		}
//		System.err.println("c: " + c);
		short res = (short)(Math.log10(sum/c) * 10);
//		short res = (short)(Math.log10(sum / c) * 10);
//		short res = (short)(sum / c);
		buf[pc++] = res;
//		System.err.println("pc: " + pc + ", " + res);
		
		return res;
	}
	
	public short[] getBuf(){
		return buf;
	}
	
	public int getPosition(){
		return pc;
	}
	
	public void setPosition(int pc){
		this.pc = pc;
		
	}
	
	public void clear(){
		for(int i = 0; i < buf.length; i++){
			buf[i] = 0;
		}
		setPosition(0);
	}	
}
