package calendar;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.midlet.MIDlet;

import calendar.renderer.BpHrWaveRenderer;
import calendar.renderer.DailyBarGraphRenderer;
import calendar.renderer.EcgPlsBarGraphRenderer;
import calendar.renderer.CalendarRenderer;
import calendar.renderer.MonthBarGraphRenderer;
import calendar.renderer.RollMenuRenderer;
import calendar.renderer.RawWaveRenderer;
import calendar.renderer.TripleRotateMenuRenderer;
import calendar.renderer.WaitScreenRenderer;
import calendar.renderer.WeekBarGraphRenderer;
import calendar.renderer.YearBarGraphRenderer;

import request.Key;

import util.DojaFont;
import util.DojaGraphics;


public class CalendarCanvas extends Canvas {
	
	//================================定数====================================//
	
	//------- 色関連  --------//
	/** 背景色 */
	private static final int BG_COLOR = 0x00FFFFFF;
	
	//------- フォント関連   --------//
	/** フォント */
	private static final DojaFont FONT = DojaFont.getFont(DojaFont.SIZE_TINY);
	
	//================================変数====================================//

	//------- 共通の描画用変数 --------//
	/** オフグラフィックス */
	protected DojaGraphics offGra;
	/** オフイメージ */
	protected Image offImg;

	/** ウィンドウ幅 */
	private int width;
	/** ウィンドウ高さ */
	private int height;
	
	/** MIDletオブジェクト*/
	private MIDlet midlet;
	
	/** カレンダー描画担当クラス */
	protected CalendarRenderer calendar;
	/** メニュー描画担当クラス  */
	protected RollMenuRenderer rollMenu;

	/** 回転メニュー描画担当クラス */
	protected TripleRotateMenuRenderer triRotatemenu;
	/** Wait画面描画担当クラス */
	protected WaitScreenRenderer waitScreen;
	/** 波形画面描画担当クラス */
	protected RawWaveRenderer rawWave;
	/** 波形画面描画担当クラス */
	protected BpHrWaveRenderer bpHrWave;
	/** 棒グラフ(日)描画担当クラス */
	protected DailyBarGraphRenderer dailyBarGraph;
	/** 棒グラフ(週)描画担当クラス */
	protected WeekBarGraphRenderer weekBarGraph;
	/** 棒グラフ(月)描画担当クラス */
	protected MonthBarGraphRenderer monthBarGraph;
	/** 棒グラフ(年)描画担当クラス */
	protected YearBarGraphRenderer yearBarGraph;
	
	//================================関数====================================//
	
	/**
	 * コンストラクタ 
	 */
	public CalendarCanvas(MIDlet midlet) {
		this.midlet = midlet;
		//幅
		this.width = getWidth();
		//高さ
		this.height = getHeight();
		
		//ダブルバッファリングのためのオフグラフィックスイメージの生成
		if (offImg == null) {
			offImg   =Image.createImage(width,height);
			offGra =  new DojaGraphics(offImg.getGraphics(),this);
		}
		//フォントをグラフィックスオブジェクトに適用
		offGra.setFont(FONT);
		
		calendar      = new CalendarRenderer(width,height);
		rollMenu      = new RollMenuRenderer(width,height);

		triRotatemenu = new TripleRotateMenuRenderer(width,height);
		waitScreen    = new WaitScreenRenderer(width,height);
		rawWave       = new RawWaveRenderer(width,height);
		bpHrWave      = new BpHrWaveRenderer(width,height);	
		dailyBarGraph = new DailyBarGraphRenderer(width,height);
		weekBarGraph  = new WeekBarGraphRenderer(width,height);
		monthBarGraph = new MonthBarGraphRenderer(width,height);
		yearBarGraph  = new YearBarGraphRenderer(width,height);
	}/* End of CalendarCanvas() */
	
	/** 
	 * 描画処理を行う
	 */
	protected void paint(Graphics g) {	
		g.drawImage(offImg,0,0,Graphics.LEFT|Graphics.TOP);
	}
	
	/**
	 * カレンダーの描画
	 */
	public void drawCalender(){
		offGra.lock();
		
		//背景の塗りつぶし
		clearCanvas(offGra);		
		//カレンダーの描画
		calendar.draw(offGra);
		
		offGra.unlock(true);
	}
	
	/**
	 * 回転メニューの描画
	 */
	public void drawTrippleRotateMenu(){
		offGra.lock();
		
		//背景の塗りつぶし
		clearCanvas(offGra);
		//回転メニューの描画
		triRotatemenu.draw(offGra);
		//Waitメニューの画面
		waitScreen.draw(offGra);
		
		offGra.unlock(true);
	}
	
	/**
	 * 波形の描画
	 */
	public void drawRawWave(){
		offGra.lock();
		
		//背景の塗りつぶし
		clearCanvas(offGra);
		rawWave.draw(offGra);
		//Waitメニューの画面
		waitScreen.draw(offGra);
		
		offGra.unlock(true);
	}
	
	/**
	 * BP,HR波形の描画
	 */
	public void drawBpHrWave(){
		offGra.lock();
		
		//背景の塗りつぶし
		clearCanvas(offGra);
		bpHrWave.draw(offGra);
		//Waitメニューの画面
		waitScreen.draw(offGra);
		
		offGra.unlock(true);
	}
	
	/**
	 * 棒グラフの描画(日)
	 */
	public void drawDailyBarGraph(){
		offGra.lock();
		
		//棒グラフの描画
		clearCanvas(offGra);
		dailyBarGraph.draw(offGra);
		//メニューの描画		
		rollMenu.draw(offGra);		
		//Waitメニューの画面
		waitScreen.draw(offGra);
		
		offGra.unlock(true);
	}
	
	/**
	 * 棒グラフの描画(週)
	 */
	public void drawWeekBarGraph(){
		offGra.lock();
		
		//棒グラフの描画
		clearCanvas(offGra);
		weekBarGraph.draw(offGra);
		//メニューの描画		
		rollMenu.draw(offGra);		
		//Waitメニューの画面
		waitScreen.draw(offGra);
		
		offGra.unlock(true);
	}
	
	/**
	 * 棒グラフの描画(月)
	 */
	public void drawMonthBarGraph(){
		offGra.lock();
		
		//棒グラフの描画
		clearCanvas(offGra);
		monthBarGraph.draw(offGra);
		//メニューの描画		
		rollMenu.draw(offGra);		
		//Waitメニューの画面
		waitScreen.draw(offGra);
		
		offGra.unlock(true);
	}
	
	/**
	 * 棒グラフの描画(年)
	 */
	public void drawYearBarGraph(){
		offGra.lock();
		
		//棒グラフの描画
		clearCanvas(offGra);
		yearBarGraph.draw(offGra);
		//メニューの描画		
		rollMenu.draw(offGra);		
		//Waitメニューの画面
		waitScreen.draw(offGra);
		
		offGra.unlock(true);
	}


	/**
	 * 背景の塗りつぶし
	 * @param g　Graphicsオブジェクト
	 */
	private void clearCanvas(DojaGraphics g){
	    g.setColor(BG_COLOR);	    
	    g.clearRect(0, 0,width,height);
	}
	
	/**
	 * このキャンバスを表示する
	 */
	public void setDisplay(){
		Display.getDisplay(midlet).setCurrent(this);
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

}
