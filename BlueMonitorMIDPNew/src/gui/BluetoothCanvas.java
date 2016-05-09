package gui;


import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.midlet.MIDlet;

import request.Key;

import util.DojaFont;
import util.DojaGraphics;

public class BluetoothCanvas extends Canvas {
	
	//NOTICE
	//DojaFontクラスのの絡みで、DojaFont.drawString(string,x,y)
	//するときに引数として指定するx座標は描画開始の座標ではなくて、文字列の中央が来る座標である。

	//================================定数====================================//
	//------- 色関連  --------//
	/** 背景色 */
	private static final int BG_COLOR = 0x0000FFFF;
	/** 文字色 */
	private static final int STR_COLOR = 0x00000000;
	/** ウィンドウ色 */
	private static final int WND_COLOR = 0x00FFFFFF;
	/** フォーカス色 */
	private static final int FCS_COLOR = 0x00FF66B4;
	
	//------- フォント関連   --------//
	/** フォント */
	private static final DojaFont FONT = DojaFont.getFont(DojaFont.SIZE_TINY);
	/** フォントのディセント */
	private static final int FONT_DECENT = FONT.getDescent();
	/** フォントのアセント */
	private static final int FONT_ASCENT = FONT.getAscent();
	/** フォントの高さ */
	private static final int FONT_HEIGHT = FONT_ASCENT + FONT_DECENT;
	
	private static final int BLINK_IINTERVAL = 12;
		
	//------- 共通の描画用変数 --------//
	/** オフグラフィックス */
	protected DojaGraphics offGra;
	/** オフイメージ */
	protected Image offImg;

	/** ウィンドウ幅 */
	private int width ;
	/** ウィンドウ高さ */
	private int height;
	
	/** MIDLETオブジェクトへの参照 */
	private MIDlet midlet;
	
	/** ウィンドウ枠 */
	private FrmWnd frmWnd;
	
	/**　▲を点滅させるためのカウンタ */
	private int blinkCnter = 0;
	/**　▲を点滅させるためのフラグ */
	private boolean blinkflag = true;

	/**
	 * コンストラクタ
	 * @param midlet midletオブジェクトへの参照
	 */
	public BluetoothCanvas(MIDlet midlet) {
		this.midlet = midlet;
		//幅
		width = getWidth();
		//高さ
		height = getHeight();

		//ダブルバッファリングのためのオフグラフィックスイメージの生成
		if (offImg == null) {
			offImg   =Image.createImage(width,height);
			offGra =  new DojaGraphics(offImg.getGraphics(),this);
		}
		//フォントをグラフィックスオブジェクトに適用
		offGra.setFont(FONT);
		
		//ウィンドウ枠
		frmWnd = new FrmWnd(true);
	}
	
	/**
	 * 描画する
	 * @param g グラフィックスオブジェクト
	 */
	protected void paint(Graphics g) {
		g.drawImage(offImg,0,0,Graphics.LEFT|Graphics.TOP);
	}
	
	/**
	 * 表示するメッセージを描画する
	 * 
	 * @param message 描画するメッセージ
	 */
	public void drawMessage(String[] message){
		offGra.lock();
   		
		//--------------背景の描画---------------//
   		//背景塗りつぶし
   		offGra.setColor(BG_COLOR);
   		offGra.clearRect(0,0,width,height);
   		
   		//--------------メインの描画---------------//
   		//メッセージの描画
   		offGra.setColor(STR_COLOR);

   		
		//ウィンドウの高さ
   		final int margin = 3;
		final int windowHeight = (FONT_HEIGHT + margin) * message.length + margin;
		
		// メニューウィンドウを表示
		frmWnd.setBGColor(WND_COLOR);
		Image img = frmWnd.getWindow(width-(margin<<1),windowHeight);
		offGra.drawImage(img,(width-img.getWidth())>>1,(height-img.getHeight())>>1);
		
		//描画開始座標
		int y = ((height-img.getHeight())>>1) + FONT_HEIGHT;
		
		
		for (int i = 0; i < message.length ; i++) {

				// メニュー項目を描画
				offGra.setColor(STR_COLOR);
				int x = (width >> 1);
				offGra.drawString(message[i], x, y);

				y += margin + FONT_HEIGHT;
		}	

   		offGra.unlock(true);
	}
	
	/**
	 * デバイスもしくはサービスの一覧表示を描画する
	 * 
	 * @param message 表示するメッセージ
	 * @param menu 描画するデバイス/サービスをもつ配列
	 * @param メニューのインデックス
	 */
	public void drawViewList(String message,String[] menu,int menuIndex){
		offGra.lock();
		
		//--------------背景の描画---------------//
   		//背景塗りつぶし
   		offGra.setColor(BG_COLOR);
   		offGra.clearRect(0,0,width,height);
   		
   		//--------------メインの描画---------------//
   		//一覧表示するリストの描画
   		offGra.setColor(STR_COLOR);
   		//点滅カウンタを更新
   		blinkCnter++;
   		   		
   		//文字の間隔
		final int margin = 3;
		//ウィンドウの高さ
		int windowHeight = (FONT_HEIGHT + margin) + margin;
		
		//メッセージを表示
		frmWnd.setBGColor(WND_COLOR);
		Image img = frmWnd.getWindow(width-(margin<<1),windowHeight);
		offGra.drawImage(img,(width-img.getWidth())>>1,((FONT_HEIGHT-img.getHeight())>>1) +10);
		int y = ((FONT_HEIGHT-img.getHeight())>>1) + FONT_HEIGHT + 10;
		offGra.drawString(message,width>>1, y);
   		   		
		//最大表示数
		final int maxNum = 4;
		//描画調整用
		final int charWidth = FONT.stringWidth("▲");
		
		//ウィンドウの高さ
		
		if(menu.length > maxNum){
			//項目が多いときは	//一度に全部を表示しない
			windowHeight = (FONT_HEIGHT + margin) * (maxNum) + margin;					
		}else{
			windowHeight = (FONT_HEIGHT + margin) * menu.length + margin;
		}
		
		// メニューウィンドウを表示
		frmWnd.setBGColor(WND_COLOR);
		img = frmWnd.getWindow(width-(margin<<1),windowHeight);
		offGra.drawImage(img,(width-img.getWidth())>>1,(height-img.getHeight())>>1);
		
		//描画開始座標
		y = ((height-img.getHeight())>>1) + FONT_HEIGHT;
		
		// メニュー項目を描画		
		if(menu.length <= maxNum){
			//表示可能な最大数を超えていないとき
			
			for (int i = 0; i < menu.length ; i++) {
				
				// フォーカスがあるときは、バックグラウンドを違う色で描画				
				if (i == menuIndex) {
					frmWnd.setBGColor(FCS_COLOR);
					Image fImg = frmWnd.getWindow(width-(margin<<1),FONT_HEIGHT);
					offGra.drawImage(fImg,margin,y - FONT_ASCENT);
				}
				
				// メニュー項目を描画
				offGra.setColor(STR_COLOR);
				int x = (width >> 1);
				offGra.drawString(menu[i], x, y);

				y += margin + FONT_HEIGHT;
			}
		}else{
			//表示可能な最大数を超えているとき
			
			offGra.setColor(STR_COLOR);
			//▲マークの表示
			if(menuIndex > (maxNum >> 1) && blinkflag ){
				//▲マークで上にまだ表示する項目があることを示す
				offGra.drawString("▲",width - (margin << 1) - charWidth, y );				
			}
						
			int offset = menuIndex - (maxNum >> 1);
			
			if(offset < 0) {
				offset = 0;
			}else if(menuIndex + (maxNum >> 1) > menu.length - 1){
				offset = menu.length - maxNum ;					
			}
			
			for (int i = offset; i < offset + maxNum ; i++) {
				
				// フォーカスがあるときは、バックグラウンドを違う色で描画				
				if (i == menuIndex) {
					frmWnd.setBGColor(FCS_COLOR);
					Image fImg = frmWnd.getWindow(width-(margin<<1),FONT_HEIGHT);
					offGra.drawImage(fImg,margin,y - FONT_ASCENT);
				}

				// メニュー項目を描画
				offGra.setColor(STR_COLOR);
				int x = (width >> 1);
				offGra.drawString(menu[i], x, y);

				y += margin + FONT_HEIGHT;
			}
			
						
			//▼マークの表示
			if( offset < menu.length - maxNum && blinkflag){			
				//▼マークで下にまだ表示する項目があることを示す
				offGra.drawString("▼",width - (margin << 1 ) - charWidth , y - (margin + FONT_HEIGHT));
			}
		}
		
		//点滅用カウンタをリセット
		if(blinkCnter >= BLINK_IINTERVAL){
			blinkCnter = 0;
			blinkflag = !blinkflag;
		}

		offGra.unlock(true);
	}
	
	//===============================キー処理==================================//

	/**
	 * キーがはされたときの処理
	 * @param keyCode キーコード
	 */
	protected void keyReleased(int keyCode) {
		int param = getGameAction(keyCode);
		Key.keyFlag[2] &= ~(1L << param);
	}

	/**
	 * キーが押されたときの処理
	 * @param keycode キーコード
	 */
	protected void keyPressed(int keyCode) {
		int param = getGameAction(keyCode);
		Key.keyFlag[0] |= (1L << param);
		Key.keyFlag[2] |= (1L << param);
	}
	
	/**
	 * このキャンバスを表示する
	 */
	public void setDisplay(){
		Display.getDisplay(midlet).setCurrent(this);
	}
	
}
