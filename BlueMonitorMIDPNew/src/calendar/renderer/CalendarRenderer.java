package calendar.renderer;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import calendar.util.CalendarColor;
import calendar.util.CalendarUtil;

import util.DojaFont;
import util.DojaGraphics;

public class CalendarRenderer {

	//================================定数====================================//
		
	//フォント
	/** 小フォント */
	private static final DojaFont sFONT = DojaFont.getFont(DojaFont.SIZE_SMALL);
	/** 大フォント */
	private static final DojaFont mFONT = DojaFont.getFont(DojaFont.SIZE_MEDIUM);

	//状態
	private static final int STATE_SELECT_DAY = 0;
	private static final int STATE_SELECT_MONTH = 1;
	
	/** カレンダー枠の上部のY座標 */
	private final int calendarY;
	/** 週描画枠上部のY座標 */
	private final int weekY;
	/** 横幅を9分割  (空白 日 月 火 水 木 金 土 空白) */
	private final int sep;
	/** 枠の大きさ */
	private final int size;
	/** 枠外側の余白(横幅から、日~土までの幅を引いた残り) */
	private final int outmargin;
	/** カレンダー枠と外枠の余白 */
	private final int margin = 10;


	//================================変数====================================//
	
	//画像
	private static Image CHECK_IMG = null;
	private static Image LEFT_ARROW_IMG = null;
	private static Image RIGHT_ARROW_IMG = null;
	private static Image RIGHT_ARROW_IMG_2 = null;

	/** 状態 */
	private int state = STATE_SELECT_DAY;
	/** 画像 */
	private Image storeImg = null;
	/** フォーカスがあるかどうか */
	private boolean focus = true;
	
	/** カレンダー上の日のインデックス */
	private int curDay = 0;

	/** 幅 */
	private int width = 0;
	/** 高さ */
	private int height = 0;

	/**現在の年 */
	private int curYear = 0;
	/**現在の月 */
	private int curMonth = 0;

	/**描画済みの年 */
	private int drawnYear = 0;
	/**描画済みの月 */
	private int drawnMonth = 0;
	
	//================================関数====================================//
	/**
	 * コンストラクタ
	 * @param width  幅
	 * @param height 高さ
	 */
	public CalendarRenderer(int width, int height) {
		// モニタのサイズを設定
		this.width = width;
		this.height = height;
				
 
		// 横幅を9分割  (空白 日 月 火 水 木 金 土 空白)
		sep  = width/9; 
		// 枠の大きさ
		size = sep*7;
		// カレンダー枠の上部のY座標
		calendarY = height - (size+(sep>>1));
		// 枠外側の余白(横幅から、日~土までの幅を引いた残り)
		outmargin = (width-size)>>1;
		// 週描画枠上部のY座標
		weekY = calendarY - sep - margin;

		createCheckImg();
		createArrowImg();

		// 現在の年月日を取得
		int[] info = CalendarUtil.calcTimeInfo(System.currentTimeMillis());
		curYear = info[0];
		curMonth = info[1];
		curDay = info[2] - 1;

		//画像の読み込み
		storeImg = Image.createImage(width, height);
	}

	//==============================画像の生成==================================//

	/**
	 * '済'画像の生成 
	 */
	private void createCheckImg() {
		// 描画する文字
		final char c = '済';

		final Font font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
		// 文字の縦幅の余白
		final int hcharmargin = ((sep - font.getHeight())>>1) + 1;
		// 文字の横幅の余白
		final int wcharmargin = ((sep - font.stringWidth(c+""))>>1);

		// 一時描画用イメージの作成
		Image naImg = Image.createImage(sep + 2, sep + 2);
		Graphics nag = naImg.getGraphics();

		// フォントの設定
		nag.setFont(font);

		// 色の設定
		nag.setColor(CalendarColor.COLOR_RED);

		// 円の描画
		int x = 0;
		int y = 0;
		int w = sep;
		int h = sep;
		nag.drawArc(x  ,y  , w, h, 0, 360);
		nag.drawArc(x  ,y+1, w, h, 0, 360);
		nag.drawArc(x+1,y  , w, h, 0, 360);
		nag.drawArc(x+1,y+1, w, h, 0, 360);

		// 文字の描画
		x = wcharmargin;
		y = hcharmargin;
		int position = (Graphics.TOP | Graphics.LEFT);
		nag.drawChar(c, x  , y  , position);
		nag.drawChar(c, x+1, y  , position);
		nag.drawChar(c, x  , y+1, position);
		nag.drawChar(c, x+1, y+1, position);

		// 透過イメージの作成
		int[] rgb = new int[(sep + 2) * (sep + 2)];
		naImg.getRGB(rgb, 0, sep + 2, 0, 0, sep + 2, sep + 2);
		int ndColor = rgb[0];
		for (int i = 0; i < rgb.length; i++) {
			if (rgb[i] == ndColor)
				rgb[i] = ((rgb[i] & 0xFFFFFF) | 0x00 << 24);
		}
		CHECK_IMG = Image.createRGBImage(rgb, sep + 2, sep + 2, true);
	}

	/**
	 * 矢印の描画
	 */
	private void createArrowImg() {

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

	//==============================描画処理==================================//
	

	/**
	 * 描画処理
	 * @param g Graphicsオブジェクト
	 */
	public void draw(DojaGraphics g) {

		// カレンダーを描画
		if (drawnYear == curYear && drawnMonth == curMonth) {
			//描画中の年、月と現在の年、月が一致したとき
			g.drawImage(storeImg, 0, 0);
		} else {
			//描画中の年、月と現在の年、月が一致しないとき
			DojaGraphics sg = new DojaGraphics(storeImg.getGraphics(),null);
			//画像を作成しなおす
			sg.setColor(CalendarColor.COLOR_WHITE);
			sg.clearRect(0,0, width, height);
			drawCalendar(sg, curYear, curMonth);
			//画像を描画
			g.drawImage(storeImg, 0, 0);
			//描画中の年、月を更新
			drawnYear = curYear;
			drawnMonth = curMonth;
		}

		// 計測日に印をつける
		boolean[] mesurementDays = CalendarUtil.getMesurementDays(curYear, curMonth);
		drawMesurementDays(g, mesurementDays, curYear, curMonth);

		// 選択枠を描画
		drawSelector(g, curYear, curMonth);
	}
		
	
	/**
	 * カレンダーの描画
	 * @param g Graphicsオブジェクト
	 * @param year  年
	 * @param month 月
	 */
	private void drawCalendar(DojaGraphics g, int year, int month) {
		// 現在の月かどうかチェック
		int today = -1;
		int[] info = CalendarUtil.calcTimeInfo(System.currentTimeMillis());
		if (info[0] == year && info[1] == month) {
			today = info[2];
		}
		// 背景を描画
		drawBGLine(g);
		// 曜日を描画
		drawWeeks(g);
		// 日を描画
		drawDays(g, year, month, today);
		// 月を描画
		drawTop(g, year, month);
	}

	/**
	 * 背景の描画
	 * @param g
	 */
	private void drawBGLine(DojaGraphics g) {

		// 色の設定
		g.setColor(CalendarColor.COLOR_LIGHT_GRAY);

		// 外枠の描画		
		int x = outmargin;
		int y = calendarY;
		g.drawRect(x  , y, size, size);
		g.drawRect(x+1, y, size, size);
		g.drawRect(x  , y, size, size);
		g.drawRect(x+1, y, size, size);

		// 升目の描画
		int x1 = 0;
		int y1 = 0;
		for (int i = 1; i < 7; i++) {
			x  = outmargin;
			y  = (calendarY + sep*i);
			x1 = (outmargin + size);
			y1 = (calendarY + sep*i);
			
			g.drawLine(x,y,x1,y1);
			
			x  = (outmargin + i*sep);
			y  = calendarY;
			x1 = (outmargin + i*sep);
			y1 = calendarY + size;
			
			g.drawLine(x,y,x1,y1);
		}

		// 二重線の描画
		g.drawLine(outmargin        , (calendarY + sep - 2),
				  (outmargin + size), (calendarY + sep - 2));
	}

	/**
	 * 週の描画
	 * @param g Graphicsオブジェクト
	 */
	private void drawWeeks(DojaGraphics g) {
		// 曜日
		final char[] weeks = {'日','月','火','水','木','金','土'};

		// フォントの設定
		g.setFont(sFONT);
		
		// 曜日の描画
		for (int i = 0; i < 7; i++) {			
			//日曜は赤色
			if (i == 0)g.setColor(CalendarColor.COLOR_LIGHT_RED);
			//平日は灰色
			if (i == 1)g.setColor(CalendarColor.COLOR_GRAY);			
			//土曜は青色
			if (i == 6)g.setColor(CalendarColor.COLOR_LIGHT_BLUE);
			
			//文字の描画
			int x = (outmargin + i*sep    + (sep>>1));
			int y = (calendarY + (sep>>1) + (sFONT.getAscent()>>1));
			
			g.drawString(weeks[i]+"",x,y);					    			
			g.drawString(weeks[i]+"",x+1,y);								
			g.drawString(weeks[i]+"",x,y+1);			
			g.drawString(weeks[i]+"",x+1,y+1);
		}
	}

	/**
	 * 日を描画
	 * @param g     Graphics
	 * @param year  年
	 * @param month 月
	 * @param today 今日の日
	 */
	private void drawDays(DojaGraphics g, int year, int month, int today) {
		
		// フォントの設定
		g.setFont(sFONT);
		// 1日の曜日を取得
		int start = CalendarUtil.getWeek(year, month, 1);
		// 月の最大日数を取得
		int lastday = CalendarUtil.getLastDay(year, month);
		
		// 日の描画
		for (int i = 0; i < lastday; i++) {
			//日
			String day = Integer.toString(i + 1);
			//横のインデックス
			int xIndex = (i+start)%7;
			//縦のインデックス
			int yIndex = (i+start)/7+1;
			
			if ((i+1) == today){
				//今日は緑で描画
				g.setColor(CalendarColor.COLOR_LIGHT_GREEN);
			}else if (xIndex == 0){
				//日曜は赤で描画
				g.setColor(CalendarColor.COLOR_LIGHT_RED);
			}else if (xIndex == 6){
				//土曜は青で描画
				g.setColor(CalendarColor.COLOR_LIGHT_BLUE);
			}else{
				//その他は黒で描画
				g.setColor(CalendarColor.COLOR_BLACK);
			}
			
			int x = (outmargin + xIndex*sep + (sep>>1));
			int y = (calendarY + (sep>>1)+ yIndex*sep + (sFONT.getAscent()>>1));
			
			//文字を描画			
			g.drawString(day,x,y);
			g.drawString(day,x+1,y);
			g.drawString(day,x,y+1);
			g.drawString(day,x+1,y+1);
		}
	}

	/**
	 * 上部を描画
	 * @param g     Graphicsオブジェクト
	 * @param year  年 
	 * @param month 月
	 */
	private void drawTop(DojaGraphics g, int year, int month) {

		// 文字間の余白（横)
		final int strMargin = 5;
		
		// 外枠の描画
		g.setColor(CalendarColor.COLOR_LIGHT_GRAY);
		int x = outmargin + sep;
		int y = weekY;
		g.drawRect(x  , y  ,sep*5, sep);
		g.drawRect(x+1, y  ,sep*5, sep);
		g.drawRect(x  , y+1,sep*5, sep);
		g.drawRect(x+1, y+1,sep*5, sep);
		
		// 単位の描画,年月の描画
		final String mstr = Integer.toString(month);
		final String ystr = Integer.toString(year);
		final String mchar = "月";
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
		
		//月の描画
		x = x + ((sFONT.stringWidth(ychar) + mFONT.stringWidth(mstr))>>1) + strMargin;
		g.setFont(mFONT);
		g.setColor(CalendarColor.COLOR_DARK_RED);
		g.drawString(mstr,x,y);
		
		//月そのものの描画
		x = x + ((mFONT.stringWidth(mstr) + sFONT.stringWidth(mchar))>>1) + strMargin;
		g.setFont(sFONT);
		g.setColor(CalendarColor.COLOR_GRAY);
		g.drawString(mchar,x,y);		

	    // 現在の年月日を取得
	    int[] info = CalendarUtil.calcTimeInfo(System.currentTimeMillis());
	    
		// 矢印の描画(左)
		x = outmargin + sep - LEFT_ARROW_IMG.getWidth() - strMargin;
		y = weekY;
		g.drawImage(LEFT_ARROW_IMG,x,y);			
		
		// 矢印の描画(右)
		x = outmargin + size - RIGHT_ARROW_IMG.getWidth() + strMargin;
		if (curYear >= info[0] && curMonth >= info[1]) {
			// 次の月があるとき
			g.drawImage(RIGHT_ARROW_IMG_2,x,y);			
		}else{
			// 次の月がないとき
			g.drawImage(RIGHT_ARROW_IMG,x,y);
		}
	}

	/**
	 * セレクタの描画
	 * @param g    Graphicsオブジェクト
	 * @param year  年
	 * @param month 月
	 */
	private void drawSelector(DojaGraphics g, int year, int month) {
		//フォーカスがなければ描画しない
		if (!focus)	return;

		int x = 0,y = 0,w = 0,h = 0;
		g.setColor(CalendarColor.COLOR_RED);
		if (state == STATE_SELECT_DAY) {
			// 1日の曜日を取得
			int start = CalendarUtil.getWeek(year, month, 1);

			// 描画位置を決定
			int windex = (start + curDay)%7;
			int hindex = (start + curDay)/7+1;
			
			x = outmargin + windex * sep;
			y = calendarY + hindex * sep;
			w = h = sep;
			
		} else if (state == STATE_SELECT_MONTH) {
			// 描画位置を決定
			x = outmargin + sep;
			y = weekY;
			w = sep*5;
			h = sep;							
		}
		
		// 枠の描画
		g.drawRect(x,  y  ,w,h);
		g.drawRect(x+1,y  ,w,h);
		g.drawRect(x,  y+1,w,h);
		g.drawRect(x+1,y+1,w,h);
	}
	
	/**
	 * 計測日に印をつける
	 * @param g Graphicsオブジェクト
	 * @param mesurementDays すでに計測したかどうかのフラグの配列
	 * @param year  年
	 * @param month 月
	 */
	private void drawMesurementDays(DojaGraphics g, boolean[] mesurementDays,int year, int month) {
	
		// 1日の曜日を取得
		int start = CalendarUtil.getWeek(year, month, 1);

		for (int i = 0; i < mesurementDays.length; i++) {
			if (mesurementDays[i]) {
				//枠の横のインデックス
				int xIndex = (i + start) % 7;
				//枠の縦ののインデックス
				int yIndex = (i + start)/7+1;	
				//描画開始x座標
				int x = (outmargin + xIndex*sep);
				//描画開始y座標
				int y = (calendarY + yIndex*sep);
				//描画
				g.drawImage(CHECK_IMG,x,y);						  
			}
		}
	}

	//================================その他===================================//
	
	public int getCurrentYear() {
		return curYear;
	}
	public int getCurrentMonth() {
		return curMonth;
	}
	public int getDayIndex() {
		return curDay;
	}
	
	public long getCurrentDate(){
		System.out.println(curYear+ " " + curMonth + " " + curDay);
		return CalendarUtil.getCurrentTime(curYear,curMonth,curDay);
	}
	
	public boolean isSelectDay() {
		return (state == STATE_SELECT_DAY);
	}
	
	public void setDayIndex(int index) {
		curDay = index;
	}

	public void setLastDayIndex() {
		curDay = CalendarUtil.getLastDay(curYear, curMonth) - 1;
	}

	public void setStateDay() {
		state = STATE_SELECT_DAY;
	}

	public void setStateMonth() {
		state = STATE_SELECT_MONTH;
	}

	public void setCurrentDay(int y, int m, int d) {
		curYear = y;
		curMonth = m;
		curDay = d;
	}		

	public void addMesurementDay(long time) {
		int[] info = CalendarUtil.calcTimeInfo(time);
		CalendarUtil.addMesurementDay(info[0], info[1], info[2]);
	}

	public void removeMesurementDay(long time) {
		int[] info = CalendarUtil.calcTimeInfo(time);
		CalendarUtil.removeMesurementDay(info[0], info[1], info[2]);
	}

	public void setFocus(boolean focus) {
		this.focus = focus;
	}
	

	/**
	 * 引数で渡した日がチェックされているかどうか
	 * @param index 日
	 * @return
	 */
	public boolean isCheckedDay(int index) {
		boolean[] mesurementDays = CalendarUtil.getMesurementDays(curYear, curMonth);
		return mesurementDays[index];
	}
	
	/**
	 * 選択されている枠を上へ
	 */
	public void up() {
		if (state == STATE_SELECT_DAY) {
			if ((curDay - 7) < 0) {
				state = STATE_SELECT_MONTH;
			} else {
				curDay -= 7;
			}
		} else if (state == STATE_SELECT_MONTH) {
			state = STATE_SELECT_DAY;
			curDay = CalendarUtil.getLastDay(curYear, curMonth) - 1;
		}
	}

	/**
	 * 選択されている枠を下へ 
	 */
	public void down() {
		if (state == STATE_SELECT_DAY) {
			int lastday = CalendarUtil.getLastDay(curYear, curMonth);
			if ((curDay + 7) >= lastday) {
				state = STATE_SELECT_MONTH;
			} else {
				curDay += 7;
			}
		} else if (state == STATE_SELECT_MONTH) {
			state = STATE_SELECT_DAY;
			curDay = 0;
		}
	}

	/**
	 * 選択されている枠を右へ
	 */
	public void right() {
		if (state == STATE_SELECT_DAY) {
			int lastday = CalendarUtil.getLastDay(curYear, curMonth);
			curDay++;
			if (curDay >= lastday) {
				int[] info = CalendarUtil.calcTimeInfo(System.currentTimeMillis());
				if (curYear >= info[0] && curMonth >= info[1]) {
					curDay--;
					return;
				}
				curDay = 0;
				curMonth += 1;
				if (curMonth > 12) {
					curYear++;
					curMonth = 1;
				}
			}
		} else if (state == STATE_SELECT_MONTH) {

			int[] info = CalendarUtil.calcTimeInfo(System.currentTimeMillis());
			if (curYear >= info[0] && curMonth >= info[1]) {
				return;
			}
			curMonth += 1;
			if (curMonth > 12) {
				curYear++;
				curMonth = 1;
			}
		}
	}

	/**
	 * 選択されている枠を左へ
	 */
	public void left() {
		if (state == STATE_SELECT_DAY) {
			curDay--;
			if (curDay < 0) {
				curMonth -= 1;
				if (curMonth < 1) {
					curYear--;
					curMonth = 12;
				}
				curDay = CalendarUtil.getLastDay(curYear, curMonth) - 1;
			}
		} else if (state == STATE_SELECT_MONTH) {
			curMonth -= 1;
			if (curMonth < 1) {
				curYear--;
				curMonth = 12;
			}
		}
	}

}
