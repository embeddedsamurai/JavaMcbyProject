package calendar.renderer;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import calendar.CalendarManager;
import calendar.util.CalendarColor;
import calendar.util.CalendarUtil;

import util.DojaFont;
import util.DojaGraphics;

public class MonthListRenderer {

	/** 小フォント */
	private static final DojaFont sFONT = DojaFont.getFont(DojaFont.SIZE_SMALL);
	/** 大フォント */
	private static final DojaFont mFONT = DojaFont.getFont(DojaFont.SIZE_MEDIUM);	
	
	/** 選択モード */
	private static final int STATE_SELECT_MONTH = 0;
	private static final int STATE_SELECT_YEAR = 1;	
	
	/** 現在の選択モード */
	private int state = STATE_SELECT_MONTH;
	
	/** 月リストの画像 */
	private Image[] monthListImg = new Image[12];
	
	/** 矢印画像 */
	private static Image LEFT_ARROW_IMG = null;
	/** 矢印画像 */
	private static Image RIGHT_ARROW_IMG = null;
	/** 矢印画像 */
	private static Image RIGHT_ARROW_IMG_2 = null;
	/** 月フォーカス用画像 */
	private static Image MONTH_FOCUS_IMG = null;
	
	/** 画面幅 */
	private int width = 0;
	/** 画面高 */
	private int height = 0;
	
	/** 現在描画中の年 */
	private int curYear = 0;

	
	/** 現在選択中の月 */
	private int monthIndex = 0;
	/** フォーカス中であるかどうか */
	private boolean focus = false;
	/** フォーカスのON/OFFを切り替えるかどうか */
	private boolean changeFocus = false;
	
	/** 月選択ボックスの一辺の長さ */
	private int boxWidth;
	/** 画面横端の余白 */
	private int marginWidth;		
	/** 画面下端の余白 */
	private int bottomMargin;		
	/** 月選択ボックス同士の間隔 */
	private int boxInterval;
	/** 月選択ボックスの描画開始Y座標 */
	private int startY;
	
	/**
	 * コンストラクタ	 
	 * @param width 幅
	 * @param height　高さ
	 */
	public MonthListRenderer(int width,int height){
		//モニタのサイズを取得
		this.width  = width; 
		this.height = height;
		
		// 月選択ボックスの一辺の長さ
		boxWidth = width/9;
		// 画面横端の余白
		marginWidth = (width-(boxWidth*7))>>1;		
		// 画面下端の余白
		bottomMargin = (marginWidth>>1);
		// 月選択ボックス同士の間隔
		boxInterval = (boxWidth>>1);
		// 月選択ボックスの描画開始Y座標 
		startY = height - (boxWidth*7) - bottomMargin;
		
		// イメージの作成
		createImages();
		// 現在の時間を取得
		int[] info = CalendarUtil.calcTimeInfo(System.currentTimeMillis());
		curYear = info[0];
	}
	
	/**
	 * 現在の年の月リストを描画	 
	 * @param g 描画対象のグラフィックスクラス	 
	 */
	public void draw(DojaGraphics g){
		drawMonthList(g);
		drawTop(g,curYear);
		drawSelector(g);
	}
	
	
	/**
	 * 上へ 
	 */
	public void up(){
		if(state == STATE_SELECT_MONTH){
			if(monthIndex < 4){
				state = STATE_SELECT_YEAR;
				monthIndex = -1;
			} else{
				monthIndex -= 4;
			}
		} else if(state == STATE_SELECT_YEAR){
			changeFocus = true;
		}
	}
	
	/**
	 * 下へ
	 */
	public void down(){		
		if(state == STATE_SELECT_MONTH){			
			if(monthIndex > 7){
				changeFocus = true;
			} else{
				monthIndex += 4;
			}			
		} else if(state == STATE_SELECT_YEAR){			
			state = STATE_SELECT_MONTH;
			monthIndex = 1;			
		}		
	}
	
	/**
	 * 左へ
	 */
	public void left(){
		if(state == STATE_SELECT_MONTH){
			monthIndex--;
			if((monthIndex+1)%4 == 0){
				monthIndex += 4;
			}
		}else if(state == STATE_SELECT_YEAR){
			curYear--;
		}
	
	}
	
	/**
	 * 右へ
	 */
	public void right(){
		if(state == STATE_SELECT_MONTH){
			monthIndex++;
			if(monthIndex%4 == 0){
				monthIndex -= 4;
			}			
		}else if(state == STATE_SELECT_YEAR){			
			int[] info = CalendarUtil.calcTimeInfo(System.currentTimeMillis());
			if(curYear >= info[0]){
				return;
			}
			curYear++;			
		}		
	}
	
	/**
	 * フォーカスの状態を設定する	 
	 * @param focus フォーカスの状態
	 * @param key 状態繊維前に押したキー	 
	 */
	public void setFocus(boolean focus, int key){
		this.focus = focus;
		if(key == CalendarManager.UP_REQ){
			monthIndex = 9;
			state = STATE_SELECT_MONTH;
		} else if(key == CalendarManager.DOWN_REQ){
			monthIndex = -1;
			state = STATE_SELECT_YEAR;
		}
	}
	
	/**
	 * フォーカスが変わったかどうか
	 * @return フォーカスが変わったかどうか
	 */
	public boolean isChangeFocus(){
		if(changeFocus){
			changeFocus = false;
			return true;
		}
		return false;
	}
	
	/**
	 * 月リスト描画用イメージの作成
	 */
	private void createImages(){
		createMonthListImg();
		createArrowImg();
	}
	
	/**
	 * 月選択部分のイメージを作成
	 */
	private void createMonthListImg(){
		
		final int margin = 2;
		
		// 12ヵ月分ループする
		for(int i=0;i<12;i++){
			
			// 描画用イメージの作成
			monthListImg[i] = Image.createImage(boxWidth+margin, boxWidth+margin);
			Graphics mg = monthListImg[i].getGraphics();
						
			// 外枠の描画
			int x = 0;
			int y = 0;
			int w = boxWidth;
			int h = boxWidth;
			final int curve = 15;
			mg.setColor(CalendarColor.COLOR_LIGHT_GRAY);			
			mg.drawRoundRect(x  ,y  ,w, h, curve, curve);
			mg.drawRoundRect(x  ,y+1,w, h, curve, curve);
			mg.drawRoundRect(x+1,y  ,w, h, curve, curve);
			mg.drawRoundRect(x+1,y+1,w, h, curve, curve);
			
			// 月番号の描画
			String dstr = Integer.toString(i+1);
			final Font font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_LARGE);
			mg.setFont(font);
			mg.setColor(CalendarColor.COLOR_MOTH_GREEN);
			x = ((boxWidth - font.stringWidth(dstr))>>1);
			y = ((boxWidth - font.getBaselinePosition())>>1)+1;
			
			int position = Graphics.TOP|Graphics.LEFT;
			mg.drawString(dstr, x  , y  , position);
			mg.drawString(dstr, x  , y+1, position);
			mg.drawString(dstr, x+1, y  , position);
			mg.drawString(dstr, x+1, y+1, position);
		}
		
		//月用フォーカス画像の生成
		Image tmpImg = Image.createImage(boxWidth+margin, boxWidth+margin);
		Graphics g = tmpImg.getGraphics();
		int x = 0;
		int y = 0;
		int w = boxWidth;
		int h = boxWidth;
		final int curve = 15;
		g.setColor(CalendarColor.COLOR_LIGHT_RED);
		g.drawRoundRect(x  ,y  ,w, h, curve, curve);
		g.drawRoundRect(x  ,y+1,w, h, curve, curve);
		g.drawRoundRect(x+1,y  ,w, h, curve, curve);
		g.drawRoundRect(x+1,y+1,w, h, curve, curve);
		
		// 透過イメージの作成
		w = (w+margin);
		h = (h+margin);
		int[] rgb1 = new int[w*h];
		tmpImg.getRGB(rgb1, 0, w, 0, 0,w,h);
		int ndColor1 = rgb1[0];
		for (int i = 0; i < rgb1.length; i++) {
			if (rgb1[i] == ndColor1)rgb1[i] = ((rgb1[i] & 0xFFFFFF) | 0x00 << 24);
		}
		MONTH_FOCUS_IMG = Image.createRGBImage(rgb1,w,h,true);
	}
	
	
	/**
	 * 矢印の描画
	 */
	private void createArrowImg() {
		// 升目の間隔
		int sep = width/9;
		
		// 一時描画用イメージの作成
		Image naImg1 = Image.createImage(sep, sep);
		Graphics nag1 = naImg1.getGraphics();		
		Image naImg2 = Image.createImage(sep, sep);
		Graphics nag2 = naImg2.getGraphics();
		Image naImg3 = Image.createImage(sep, sep);
		Graphics nag3 = naImg3.getGraphics();
		
		// 三角形の頂点
		int x1 = 0;
		int y1 = sep/2;
		int x2 = sep;
		int y2 = 0;
		int x3 = sep;
		int y3 = sep;
		
		// 三角形の描画
		nag1.setColor(CalendarColor.COLOR_MOTH_GREEN);
		nag1.fillTriangle(x1, y1, x2, y2, x3, y3);		
		// 透過イメージの作成
		int[] rgb1 = new int[sep * sep];
		naImg1.getRGB(rgb1, 0, sep, 0, 0, sep, sep);
		int ndColor1 = rgb1[0];
		for (int i = 0; i < rgb1.length; i++) {
			if (rgb1[i] == ndColor1)rgb1[i] = ((rgb1[i] & 0xFFFFFF) | 0x00 << 24);
		}
		LEFT_ARROW_IMG = Image.createRGBImage(rgb1, sep, sep, true);
		
		// 三角形の頂点
		x1 = sep;
		y1 = sep/2;
		x2 = 0;
		y2 = 0;		
		x3 = 0;
		y3 = sep;
		
		nag2.setColor(CalendarColor.COLOR_MOTH_GREEN);
		nag2.fillTriangle(x1, y1, x2, y2, x3, y3);
		int[] rgb2 = new int[sep * sep];
		naImg2.getRGB(rgb2, 0, sep, 0, 0, sep, sep);
		int ndColor2 = rgb2[rgb2.length-1];
		for (int i = 0; i < rgb2.length; i++) {
			if (rgb2[i] == ndColor2)
				rgb2[i] = ((rgb2[i] & 0xFFFFFF) | 0x00 << 24);
		}
		RIGHT_ARROW_IMG = Image.createRGBImage(rgb2, sep, sep, true);
		
		nag3.setColor(CalendarColor.COLOR_LIGHT_GRAY);
		nag3.fillTriangle(x1, y1, x2, y2, x3, y3);
		int[] rgb3 = new int[sep * sep];
		naImg3.getRGB(rgb3, 0, sep, 0, 0, sep, sep);
		int ndColor3 = rgb3[rgb3.length-1];
		for (int i = 0; i < rgb3.length; i++) {
			if (rgb3[i] == ndColor3)
				rgb3[i] = ((rgb3[i] & 0xFFFFFF) | 0x00 << 24);
		}
		RIGHT_ARROW_IMG_2 = Image.createRGBImage(rgb3, sep, sep, true);
	}
	
	/**
	 * 対象の年の月リストの月選択部分を描画
	 *
	 * @param g 描画対象のグラフィックスクラス
	 *
	 */
	private void drawMonthList(DojaGraphics g){
		// 月リストを描画
		int count = 0;
		int margin = (width - ((boxWidth*4)+(boxInterval)*3))>>1;
		for(int i=0; i < 3; i++){
			for(int j=0; j < 4; j++){
				int y = startY + 10 + i*(boxWidth+(boxInterval));
				int x = margin+j*(boxWidth+(boxInterval));
				g.drawImage(monthListImg[count],x,y);
				count++;
			}
		}
	}
	
	/**
	 * 上部を描画
	 * @param g     Graphicsオブジェクト
	 * @param year  年 
	 */
	private void drawTop(DojaGraphics g, int year) {

		// 升目の間隔
		// 横幅を9分割  (空白 日 月 火 水 木 金 土 空白)
		int sep = width/9;
		//　枠の大きさ
		final int size = sep*7;
		// 枠外側の余白 (横幅から、日~土までの幅を引いた残り)	
		final int outmargin = (width-size)>>1;
		// 文字間の余白（横)
		final int strMargin = 5;
		// 月描画枠上部のY座標
		final int weekY = height - (size+(sep>>1)) - sep - 10;
		
		
		// 外枠の描画
		g.setColor(CalendarColor.COLOR_LIGHT_GRAY);
		int x = outmargin + sep;
		int y = weekY;
		g.drawRect(x  , y  ,sep*5, sep);
		g.drawRect(x+1, y  ,sep*5, sep);
		g.drawRect(x  , y+1,sep*5, sep);
		g.drawRect(x+1, y+1,sep*5, sep);
		
		// 単位の描画,年月の描画
		final String ystr = Integer.toString(year);
		final String ychar = "年";
		
		//年の描画		
		y = y + (sep>>1) + (sFONT.getAscent()*2/3);
		x = x + ((mFONT.stringWidth(ystr))>>1) + strMargin;
		g.setFont(mFONT);		
		g.setColor(CalendarColor.COLOR_WATER_BLUE);
		g.drawString(ystr,x,y);		
		
		//年そのものの描画
		x = x + ((mFONT.stringWidth(ystr) + sFONT.stringWidth(ychar))>>1) + strMargin;
		g.setFont(sFONT);
		g.setColor(CalendarColor.COLOR_GRAY);		
		g.drawString(ychar,x,y);

	    // 現在の年月日を取得
	    int[] info = CalendarUtil.calcTimeInfo(System.currentTimeMillis());
	    
		// 矢印の描画(左)
		x = outmargin + sep - LEFT_ARROW_IMG.getWidth() - strMargin;
		y = weekY;
		g.drawImage(LEFT_ARROW_IMG,x,y);			
		
		// 矢印の描画(右)
		x = outmargin + size - RIGHT_ARROW_IMG.getWidth() + strMargin;
		if (curYear >= info[0]) {
			// 次の年いがあるとき
			g.drawImage(RIGHT_ARROW_IMG_2,x,y);			
		}else{
			// 次の年がないとき
			g.drawImage(RIGHT_ARROW_IMG,x,y);
		}
	}
	
	/**
	 * 選択枠を描画
	 *
	 * @param g 描画対象のグラフィックスクラス
	 *
	 */
	private void drawSelector(DojaGraphics g){
		
		// フォーカスがない場合描画しない
		if(!focus) return;
		final int monthMargin = (width - ((boxWidth*4)+(boxWidth>>1)*3))>>1;
		final int margin = 10;
		
		// 選択枠の描画
		g.setColor(CalendarColor.COLOR_LIGHT_RED);
		int x = 0,y = 0,w = 0,h = 0;
		if(state == STATE_SELECT_MONTH){	// 月選択時
			x = monthMargin + (monthIndex%4)*(boxWidth+boxInterval);
			y = startY + margin + (monthIndex/4)*(boxWidth+boxInterval);
			//フォーカス用の画像を描画
			g.drawImage(MONTH_FOCUS_IMG, x, y);
		} else if(state == STATE_SELECT_YEAR){  // 年選択時		
			x = marginWidth + boxWidth;
			
			// 月描画枠上部のY座標						
			y = height - (boxWidth*8 + (boxWidth>>1)) - margin;
			w = boxWidth*5;
			h = boxWidth;	
			// 枠の描画
			g.drawRect(x,  y  ,w,h);
			g.drawRect(x+1,y  ,w,h);
			g.drawRect(x,  y+1,w,h);
			g.drawRect(x+1,y+1,w,h);
		}		
	}
	
}
