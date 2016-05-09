/*
 * 作成日: 2005/9/10
 *
 * Copyright 2001 - 2008 Embedded.Samurai, Inc. All rights reserved.
 *
 * $Id: DojaFont.java,v 1.1 2005/9/10 06:11:11 esamurai Exp $
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
public final class DojaFont{

	// Font定数
	public final static int SIZE_TINY   = Font.SIZE_SMALL;
	public final static int SIZE_SMALL  = Font.SIZE_MEDIUM;
	public final static int SIZE_MEDIUM = Font.SIZE_LARGE;

	// 変数
	Font font;

	// フォントの取得
	public static DojaFont getFont(int size){
		DojaFont dojaFont = new DojaFont();
		dojaFont.font = Font.getFont(Font.FACE_MONOSPACE,Font.STYLE_PLAIN,size);
		return dojaFont;
	}

	// 文字幅の取得
	public int stringWidth(String str){
		return font.stringWidth(str);
	}

	// ラインブレイクの取得
	public int getLineBreak(String str,int offset,int len,int width){
		for (int l=0;l<=len-offset;l++){
			if(font.substringWidth(str,offset,l) > width){
				return offset + l -1;
			}
		}
		return str.length();
	}

	// アセントの取得
	public int getAscent() {
		return font.getBaselinePosition();
	}

	// デセントの取得
	public int getDescent(){
		return font.getHeight() - font.getBaselinePosition();
	}
}
