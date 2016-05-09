/*
 * 作成日: 2005/9/10
 *
 * Copyright 2001 - 2008 Embedded.Samurai, Inc. All rights reserved.
 *
 * $Id: DojaGraphics.java,v 1.1 2005/9/10 06:11:11 esamurai Exp $
 */

package util;

import javax.microedition.lcdui.*;
/**
 * Doja風にMIDPで描画を行うクラス。
 * <br>
 *
 * @version $Revision: 1.1 $
 * @copyright 2007 Embedded.Samurai, Inc.
 * @reference furukawa eiiti
 * @author embedded.samurai <embedded.samurai@gmail.com>
 */
public final class DojaGraphics{

	public static final int BLACK  = (   0<<16) + ( 0  <<8) +   0;
	public static final int BLUE   = (   0<<16) + ( 0  <<8) + 255;
	public static final int GREEN  = (   0<<16) + ( 128<<8) +   0;
	public static final int RED    = ( 255<<16) + ( 0  <<8) +   0;
	public static final int WHITE  = ( 255<<16) + ( 255<<8) + 255;
	public static final int YELLOW = ( 255<<16) + ( 255<<8) +   0;

	private Graphics bufferGraphics;
	private Canvas canvas;
	private int origX=0;        //原点X座標
	private int origY=0;        //原点Y座標

	// コンストラクタ
	public DojaGraphics(Graphics bufferGraphics,Canvas canvas){
		this.bufferGraphics = bufferGraphics;
		this.canvas = canvas;
		this.bufferGraphics.setFont(Font.getFont(Font.FACE_MONOSPACE,
		                          Font.STYLE_PLAIN,Font.SIZE_SMALL));
	}

	// ロック
	public void lock(){
	}


	// アンロック
	public void unlock(boolean repaintflag){
		if(repaintflag){
			canvas.repaint();
			canvas.serviceRepaints();
		}
	}

	// 原点の指定
	public void setOrigin(int origX,int origY){
		this.origX = origX;
		this.origY = origY;
	}

	// 文字列の描画
	public void drawString(String str,int x,int y){
		bufferGraphics.drawString(str,x+origX,y+origY,bufferGraphics.HCENTER|bufferGraphics.BASELINE);
	}

	// イメージの描画
	public void drawImage(Image image,int x,int y){
		bufferGraphics.drawImage(image,x+origX,y+origY,bufferGraphics.LEFT|bufferGraphics.TOP);
	}

	// イメージの描画
	public void drawImage(Image image,int dx,int dy,int sx,int sy,int width,int height){

		int cx=bufferGraphics.getClipX();
		int cy=bufferGraphics.getClipY();
		int cw=bufferGraphics.getClipWidth();
		int ch=bufferGraphics.getClipHeight();

		dx += origX;
		dy += origY;
		sx += origX;
		sy += origY;

		bufferGraphics.setClip(dx,dy,width,height);
		bufferGraphics.drawImage(image,dx+origX-sx,dy+origY-sy,bufferGraphics.LEFT|bufferGraphics.TOP);
		bufferGraphics.setClip(cx,cy,cw,ch);
	}

	// 線の描画
	public void drawLine(int x1,int y1,int x2,int y2){
		bufferGraphics.drawLine(x1+origX,y1+origY,x2,y2);
	}

	public void clearRect(int x,int y,int width,int height){
		bufferGraphics.fillRect(x+origX,y+origY,width,height);
	}
	
	// 矩形の描画
	public void drawRect(int x,int y,int width,int height){
		bufferGraphics.drawRect(x+origX,y+origY,width,height);
	}
	
	//引数として渡された矩形をクリッピング領域として設定
	public void setClip(int x,int y,int width,int height){
		bufferGraphics.setClip(x, y, width, height);
	}

	// フォントの設定
	public void setFont(DojaFont font){
		bufferGraphics.setFont(font.font);
	}

	// 色の取得
	public  static int getColorOfName(int name){
		return name;
	}

	// 色の取得
	public static int getColorOfRGB(int red,int green,int blue){
		return (red<<16)+(green<<8)+blue;
	}

	// 色の設定
	public void setColor(int color){
		bufferGraphics.setColor(color);
	}

}
