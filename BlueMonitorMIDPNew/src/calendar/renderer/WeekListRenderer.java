package calendar.renderer;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import calendar.CalendarManager;
import calendar.util.CalendarColor;
import calendar.util.CalendarUtil;

import util.DojaFont;
import util.DojaGraphics;

public class WeekListRenderer {
	
	//====================================定数====================================//
	//フォント
	/** 小フォント */
	private static final DojaFont sFONT = DojaFont.getFont(DojaFont.SIZE_SMALL);
	/** 大フォント */
	private static final DojaFont mFONT = DojaFont.getFont(DojaFont.SIZE_MEDIUM);
	
	//状態
	/** 週選択状態 */
	private static final int STATE_SELECT_WEEK = 0;
	/** 月選択状態 */ 
	private static final int STATE_SELECT_MONTH = 1;
	
	/** 1週間の日数*/
	private static final int NUM_OF_DAY_IN_WEEK = 7;
	/** 1年の月数*/
	private static final int NUM_OF_MONTH_IN_YEAR = 12;
	
	/** 横幅を9分割  (空白 日 月 火 水 木 金 土 空白) */
	private final int sep;
	/** 枠の大きさ */
	private final int size;
	/** 週の箱一つの幅 */
	private final int boxWidth;
	/** BOXの高さ (+3は上下の余白) */
	private final int boxHeight;
	/** 月描画枠上部のY座標 */
	private final int monthY;		

	//===================================変数====================================//
	/** 現在の状態 */
	private int state = STATE_SELECT_WEEK;
		
	/** 週表示ボックスを格納する配列 */
	private Image[] weekBox = new Image[6];
	
	/** 矢印画像 */
	private static Image LEFT_ARROW_IMG = null;
	private static Image RIGHT_ARROW_IMG = null;
	private static Image RIGHT_ARROW_IMG_2 = null;
	
	/** 画面幅 */
	private int width = 0;
	/** 画面高さ */
	private int height = 0;
	
	/** 現在描画中の年 */
	private int curYear = 0;
	/** 現在描画中の月 */
	private int curMonth = 0;
	/** 現在描画中の日 */
    private int curDay = 0;
	
	/** 選択中の週 */
	private int weekIndex = 0;
	
	/** フォーカス中であるか */
	private boolean focus = false;
	
	/** フォーカスのON/OFFを切り替えるかどうか */
	private boolean changeFocus = false;
	
	//====================================関数====================================//
	/**
	 * コンストラクタ
	 */
	public WeekListRenderer(int width,int height){
		
		// モニタのサイズを取得
		this.width = width;
		this.height = height;
		
		// 横幅を9分割  (空白 日 月 火 水 木 金 土 空白)
		sep  = width/9; 
		// 枠の大きさ
		size = sep*7;		
		//BOXのサイズ 
		boxWidth = sep*5;		
		//BOXの高さ (+3は上下の余白)
		boxHeight = sep;
		// 月描画枠上部のY座標
		monthY = height - (size+(sep>>1)) - sep - 10;
		
		// 描画イメージの作成
		createWeekBoxImg();
		createArrowImg();
		
		// 現在の時間を取得
		int[] info = CalendarUtil.calcTimeInfo(System.currentTimeMillis());
		curYear  = info[0];
		curMonth = info[1];
        curDay   = info[2];
		weekIndex = CalendarUtil.getCurrentWeekNum(info[0], info[1], info[2]);		
	}
	
	/**
	 * 描画処理を行う
	 * @param g
	 */
	public void draw(DojaGraphics g){			
		//週表示BOXを描画
		drawWeekBox(g, curYear,curMonth);
		//月表示画面を描画
		drawTop(g,curYear,curMonth);		
		//選択枠を描画
		drawSelector(g);
	}
	
	/**
	 * 上へ
	 */
	public void up(){
		if(state == STATE_SELECT_WEEK){	
			//週選択時
			if(weekIndex == 0){
				//0ならつき選択モードへ
				state = STATE_SELECT_MONTH;
				weekIndex = -1;
			} else{
				//前の週へ
				weekIndex--;
                curDay -= 7;
			}
		} else if(state == STATE_SELECT_MONTH){	
			//月選択時
			changeFocus = true;
		}
	}
	
	/**
	 * 下へ
	 */
	public void down(){		
		if(state == STATE_SELECT_WEEK){	
			//週選択時		
			// 描画する月の週数を算出する
			int weekNum = CalendarUtil.getMonthWeekNum(curYear,curMonth);
			
			if(weekIndex+1 > weekNum-1){
				//フォーカスを切り替える
				changeFocus = true;
			}else{
				//週のインデックスを増やす
				weekIndex++;
				curDay += NUM_OF_DAY_IN_WEEK;
			}			
		}else if(state == STATE_SELECT_MONTH){	
			//月選択時
			//週選択モードへ
			state = STATE_SELECT_WEEK;
			weekIndex = 0;
			curDay = 1;
		}
	}
	
	/**
	 * 左へ
	 */
	public void left(){		
		if(state == STATE_SELECT_MONTH){
			//月選択時
			curMonth -= 1;
			if(curMonth < 1){
				curYear--;
				curMonth = NUM_OF_MONTH_IN_YEAR;
			}
		}
	}
	
	/**
	 * 右へ
	 */
	public void right(){
		if(state == STATE_SELECT_MONTH){	
			//月選択時
			int[] info = CalendarUtil.calcTimeInfo(System.currentTimeMillis());
			if(curYear >= info[0] && curMonth >= info[1]){
				//これ以上表示する月がない時は何もしない
				return;
			}
			//月を一つ増やす
			curMonth += 1;
			if(curMonth > NUM_OF_MONTH_IN_YEAR){
				//翌年へ
				curYear++;
				curMonth = 1;
			}
		}
	}
	
	/**
	 * フォーカスの状態を設定する
	 *
	 * @param focus フォーカスの状態
	 * @param key 状態繊維前に押したキー
	 */
	public void setFocus(boolean focus, int key){
		this.focus = focus;
		if(key == CalendarManager.UP_REQ){
			//上を押したとき
			weekIndex = CalendarUtil.getMonthWeekNum(curYear,curMonth) - 1;
			//週モードへ移行
			state = STATE_SELECT_WEEK;			
		} else if(key == CalendarManager.DOWN_REQ){
			//下を押したとき
			//月モードへ移行
			weekIndex = -1;
			state = STATE_SELECT_MONTH;			
		}
	}
	
	/**
	 * フォーカスが変化したかどうか
	 * @return true:フォーカスが変化した false:フォーカスが変わってない
	 */
	public boolean isChangeFocus(){		
		if(changeFocus){
			changeFocus = false;
			return true;
		}
		return false;
	}
	
	/**
	 * 週表示BOXを作成する
	 */
	private void createWeekBoxImg(){

		//第何週かを表す文字列の長さ
		final Font font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
		final int weekStrlen = font.stringWidth("第5週");

		for(int i=0; i < NUM_OF_DAY_IN_WEEK-1; i++){
			//描画用空イメージの作成
			weekBox[i] = Image.createImage(boxWidth+weekStrlen+4, boxHeight+2);
			Graphics wg = weekBox[i].getGraphics();
			
			//外枠の描画
			int x = weekStrlen;
			int y = 0;
			wg.setColor(CalendarColor.COLOR_LIGHT_GRAY);
			wg.drawRect(x+2,  y  , boxWidth, boxHeight);
			wg.drawRect(x+2,  y+1, boxWidth, boxHeight);
			wg.drawRect(x+2+1,y  , boxWidth, boxHeight);
			wg.drawRect(x+2+1,y+1, boxWidth, boxHeight);
			
			// 週番号の描画
			String dstr = "第" + (i+1) + "週";
			x = 0;
			y = 2; //外枠の太さ
			wg.setFont(font);
			wg.setColor(CalendarColor.COLOR_WATER_BLUE);
			wg.drawString(dstr, x  , y  , Graphics.TOP|Graphics.LEFT);
			wg.drawString(dstr, x  , y+1, Graphics.TOP|Graphics.LEFT);
			wg.drawString(dstr, x+1, y  , Graphics.TOP|Graphics.LEFT);
			wg.drawString(dstr, x+1, y+1, Graphics.TOP|Graphics.LEFT);			
		}
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
	
	/**
	 * 週表示BOXを描画
	 *
	 * @param g 描画対象のGraphicsクラス
	 * @param year 描画する年
	 * @param month 描画する月
	 *
	 */
	private void drawWeekBox(DojaGraphics g, int year, int month){
		
		//枠外側の余白の合計
		final int outmargin = (width-weekBox[0].getWidth())>>1;

		//縦方向の描画開始座標
		final int hstart = height-(sep*7+(sep>>1));		
		//第何週かを表す文字列の長さ
		final int weekStrlen = sFONT.stringWidth("第5週");		
		//描画する月の週数を算出する
		final int weekNum = CalendarUtil.getMonthWeekNum(year,month);
		
		//枠内に描画する日を描画するときの左側の余白
		final int strMargin = 5;
		//箱間の余白(縦)
		final int boxHMargin = 10;
		
		for(int i=0; i < weekNum; i++){			
			//描画する週の日のリストを算出する
			final int[] days = CalendarUtil.getWeekDays(year, month, i+1);			
			//週表示BOXの描画(余白の端から描画)
			g.drawImage(weekBox[i],outmargin, hstart+i*(boxHeight+boxHMargin));
			
			//最初の日
			final String firstDay = Integer.toString(days[0]);
			//最後の日
			final String lastDay  = Integer.toString(days[days.length-1]);			
			final String uday = "日";
			final String gap = "～";
			
			//文字間の余白
			final int space = 3;
			
			// 描画開始位置 (左端の余白 + 第何週かを表す文字列)
			int startX = outmargin+weekStrlen+strMargin;				
			int startY = hstart+boxHMargin+ 
			            ((weekBox[i].getHeight() - (sFONT.getAscent()+sFONT.getDescent()))>>1)
			            + (sFONT.getAscent()>>1);
			
			//------------------------ｘ日までの部分-------------------------//
			//緑色に変更
			g.setColor(CalendarColor.COLOR_MOTH_GREEN);
			g.setFont(sFONT);
			
			//-------------最初の日を描画-------------//			
			int strLen = sFONT.stringWidth(firstDay);
			if(days[0] >= 10){
				//日が2ケタのとき
				startX += space;
			}else{
				//日が1ケタのとき
				startX += (space<<1);	
			}
			//描画開始座標
			int x = startX + (strLen>>1);
			int y = startY+i*(boxHeight+boxHMargin);
			//次の描画開始座標決定の為の処理
			startX += (strLen) + space;;
			//描画処理
			g.drawString(firstDay,x  ,y  );
			g.drawString(firstDay,x+1,y  );
			g.drawString(firstDay,x  ,y+1);
			g.drawString(firstDay,x+1,y+1);
			
			//灰色に変更
			g.setColor(CalendarColor.COLOR_GRAY);
			g.setFont(sFONT);
			
			//---------------"日"を描画---------------//			
			strLen = sFONT.stringWidth(uday);
			//描画開始座標
			x = startX + (strLen>>1);
			//次の描画開始座標決定の為の処理
			startX += (strLen) + space;
			//描画処理
			g.drawString(uday,x  ,y  );
			g.drawString(uday,x+1,y  );
			g.drawString(uday,x  ,y+1);
			g.drawString(uday,x+1,y+1);
			
			//------------------------～からの部分-------------------------//
			
			if(days.length > 1){	
				//その月の週の日数が2日以上の場合
				
				//黒色に変更
				g.setColor(CalendarColor.COLOR_BLACK);
				g.setFont(sFONT);
								
				//---------------"～"を描画---------------//
				strLen = sFONT.stringWidth(gap);
				//描画開始座標
				x = startX + (strLen>>1);
				//次の描画開始座標決定の為の処理
				if(days[days.length-1] >= 10){
					//日が2ケタのとき
					startX += space + (strLen);
				}else{
					//日が1ケタのとき
					startX += (space<<1) + (strLen);	
				}				
				//描画処理
				g.drawString(gap,x  ,y  );
				g.drawString(gap,x+1,y  );
				g.drawString(gap,x  ,y+1);
				g.drawString(gap,x+1,y+1);
				
				//緑色
				g.setColor(CalendarColor.COLOR_MOTH_GREEN);
				g.setFont(sFONT);
				
				//---------------日を描画---------------//
				strLen = sFONT.stringWidth(lastDay);
				//描画開始座標
				x = startX + (strLen>>1);
				//次の描画開始座標決定の為の処理
				startX += (strLen) + space;
				//描画処理
				g.drawString(lastDay,x  ,y  );
				g.drawString(lastDay,x+1,y  );
				g.drawString(lastDay,x  ,y+1);
				g.drawString(lastDay,x+1,y+1);
				
				//灰色
				g.setColor(CalendarColor.COLOR_GRAY);
				g.setFont(sFONT);
				
				//---------------"日"を描画---------------//
				strLen = sFONT.stringWidth(uday);
				//描画開始座標
				x = startX + (strLen>>1);
				//描画処理
				g.drawString(uday,x  ,y  );
				g.drawString(uday,x+1,y  );
				g.drawString(uday,x  ,y+1);
				g.drawString(uday,x+1,y+1);
			}
		}
	}

	/**
	 * 上部を描画
	 * @param g     Graphicsオブジェクト
	 * @param year  年 
	 * @param month 月
	 */
	private void drawTop(DojaGraphics g, int year, int month) {

		// 枠外側の余白 (横幅から、日~土までの幅を引いた残り)	
		final int outmargin = (width-size)>>1;
		// 文字間の余白（横)
		final int strMargin = 5;
		
		// 外枠の描画
		g.setColor(CalendarColor.COLOR_LIGHT_GRAY);
		int x = outmargin + sep;
		int y = monthY;
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
		y = monthY;
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
	 */
	private void drawSelector(DojaGraphics g) {
		//フォーカスがなければ描画しない		
		if (!focus)	return;
		
		//第何週かを表す文字列の長さ
		final int weekStrlen = sFONT.stringWidth("第5週");
		//余白
		final int margin = 2;

		//箱間の余白(縦)
		final int boxHMargin = 10;
		int x = 0,y = 0,w = 0,h = 0;
		g.setColor(CalendarColor.COLOR_LIGHT_RED);
		
		if (state == STATE_SELECT_WEEK) {
			// 枠の描画(+2は枠作成時に+2としてあるため)			 						
			x = ((width-weekBox[0].getWidth())>>1) + +weekStrlen+margin;
			y = height-(sep*7+(sep>>1))                    //ここまでで最初のボックスの位置
			      +(weekIndex)*(boxHeight+boxHMargin);     //インデックの増分
			w = boxWidth;
			h = boxHeight;			
		} else if (state == STATE_SELECT_MONTH) {
			// 枠の描画
			x = ((width-size)>>1) + sep;
			y = monthY;
			w = sep*5;
			h = sep;
		}
		
		g.drawRect(x,  y  ,w,h);
		g.drawRect(x+1,y  ,w,h);
		g.drawRect(x,  y+1,w,h);
		g.drawRect(x+1,y+1,w,h);
	}
}
