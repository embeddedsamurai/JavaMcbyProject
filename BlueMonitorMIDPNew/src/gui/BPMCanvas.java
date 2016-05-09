package gui;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.midlet.MIDlet;

import main.Main;


import request.Key;
import tsk.BPCalcTSK;
import util.DojaFont;
import util.DojaGraphics;

public class BPMCanvas extends Canvas {

	//NOTICE
	//DojaFontクラスのの絡みで、DojaFont.drawString(string,x,y)
	//するときに引数として指定するx座標は描画開始の座標ではなくて、文字列の中央が来る座標である。

	//================================定数====================================//

	//------- 色関連  --------//
	/** 背景色 */
	private static final int BG_COLOR = 0x00FFFFFF;
	/** バックグランドの目盛りの色 */
	private static final int BG_LINE_COLOR = 0x999999;
	/** バックグランドの目盛りの色 */
	private static final int BG_LINE_COLOR2 = 0xC9C9C9;
	/** 目盛りの字の色 */
	private final int BG_STR_COLOR = 0x000000FF;

	/** 心電図信号の色 */
	private static final int ECG_COLOR = 0x00FF0066;

	/** R波ピークのの色 */
	private static final int R_WAVE_COLOR = 0x00EE3344;
	/** 脈波信号の色 */
	private static final int PLS_COLOR = 0x003366FF;
	/** 血圧値描画の色 */
	private static final int SBP_COLOR = 0x0000cc66;
	/** スペクトルの色(文字) */
	private static final int SPC_COLOR = 0x00FF4500;

	//------- 文字列   --------//
	/** 心拍数を表す文字列 */
	private static final String HR_STR =  "心拍数";
	/** 脈波伝搬時間を表す文字列 */
	private static final String PAT_STR = "脈波伝搬時間";
	/** 血圧を表す文字列 */
	private static final String SBP_STR =  "血圧";
	/** スペクトルを表す文字列 */
	private static final String SPC_STR = "スペクトル";

	//------- バックグラウンド  --------//
	/** バックグランドの目盛りの間隔 */
	private static final int BG_LINE_MARGIN = 10;
	/** 目盛り線の長さ */
	private final int BG_LINE_LONG = 5;
	/**	バックグラウンドの目盛り線の数(グランドラインを除く)*/
	private static final int NUM_OF_CURRENT_BGLINE = 8;
	/**	バックグラウンドの目盛り線の数(グランドラインを除く)*/
	private static final int NUM_OF_TREND_BGLINE = 6;

	//------- フォント関連   --------//
	/** フォント */
	private static final DojaFont FONT = DojaFont.getFont(DojaFont.SIZE_TINY);
	/** フォントのディセント */
	private static final int FONT_DECENT = FONT.getDescent();
	/** フォントのアセント */
	private static final int FONT_ASCENT = FONT.getAscent();
	/** フォントの高さ */
	private static final int FONT_HEIGHT = FONT_ASCENT + FONT_DECENT;
	/** 文字の間隔 */
	private static final int STR_MARGIN = 5;

	//------- カレント用の拡大縮小倍率の定義 --------//
	/** 最大のX軸方向の表示縮小倍率 */
	public static final int MAN_X_REDUCTION_RATE = 4;
	/** 最小のX軸方向の表示縮小倍率 */
	public static final int MIX_X_REDUCTION_RATE = 1;

	/** 最小のY軸方向の表示倍率 (1/MIN_X_SCALE)が倍率*/
	public static final int MIN_Y_SCALE = 1;
	/** 最大のY軸方向の表示倍率 (1/MAN_X_SCALE)が倍率*/
	public static final int MAX_Y_SCALE = 3;

	//================================変数====================================//

	//------- 共通の描画用変数 --------//
	/** オフグラフィックス */
	protected DojaGraphics offGra;
	/** オフイメージ */
	protected Image offImg;

	/** ウィンドウ幅 */
	private int width ;
	/** ウィンドウ高さ */
	private int height;

	//------- ラベルの座標関連 --------//
	/** 心電図描画の基準のY座標 */
	private int ecg_gl_y;
	/** 脈波描画の基準のY座標 */
	private int pls_gl_y;
	/** 心拍数描画の基準のY座標 */
	private int hr_gl_y;
	/** 脈波伝搬時間描画の基準のY座標 */
	private int pat_gl_y;
	/** 収縮期血圧描画の基準のY座標 */
	private int sbp_gl_y;

	//------- カレント画面の座標関連 --------//
	/** 背景の縦の目盛り線の高さ(カレント)　*/
	private int bg_current_vertical_line_h;

	/** 心拍数などの情報を表示する文字列のy座標(カレント) */
	private int labelCurrent1_y;
	/** 脈波伝搬時間などの情報を表示する文字列のy座標(カレント) */
	private int labelCurrent2_y;

	/** カレント波形の表示の縮小倍率(X軸方向)*/
	private int xReductionRate = MAN_X_REDUCTION_RATE;
	/** カレント波形の表示の倍率(Y軸方向)*/
	private int yScale = MIN_Y_SCALE;

	//------- トレンド画面の座標関連 --------//
	/** 背景の縦の目盛り線の高さ(トレンド)　*/
	private int bg_trend_vertical_line_h;

	/** 心拍数などの情報を表示する文字列のy座標(トレンド) */
	private int labelTrend1_y;
	/** 脈波伝搬時間などの情報を表示する文字列のy座標(トレンド) */
	private int labelTrend2_y;
	/** 血圧などの情報を表示する文字列のy座標(トレンド) */
	private int labelTrend3_y;

	//------- スペクトル画面の座標関連 --------//
	/** 振幅スペクトルの最大値(この値で振幅スペクトルを正規化する) */
	private int maxSpectrum;
	/** スペクトル描画の基準のY座標 */
	private int spectrum_gl_y;

	/** 心電図のスペクトル配列 */
	public double[] ecgSpectrum;

	/** MIDLETオブジェクトへの参照 */
	private MIDlet midlet;

	//==============================初期化処理==================================//
	/**
	 * コンストラクタ
	 */
	public BPMCanvas(MIDlet midlet){
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

		//心電図描画基準座標
	    ecg_gl_y = height * 3 / 10;
		//脈波描画基準座標
	    pls_gl_y = height * 8 / 10;
		//心拍数描画基準座標
	    hr_gl_y  = height * 3 / 15;
		//脈波伝搬時間描画基準座標
	    pat_gl_y = height * 8 / 15;
		//収縮期血圧描画基準座標
	    sbp_gl_y = height *13 / 15;
		//スペクトル描画基準座標
	    spectrum_gl_y = height - 2;

	    //背景の縦の目盛り線の高さ(カレント画面)
	    bg_current_vertical_line_h = (BG_LINE_MARGIN * (NUM_OF_CURRENT_BGLINE>>1));
	    //背景の縦の目盛り線の高さ(トレンド画面)
	    bg_trend_vertical_line_h = (BG_LINE_MARGIN * (NUM_OF_TREND_BGLINE>>1));

	    //心拍数などの情報を表示する文字列のy座標
	    labelCurrent1_y = ecg_gl_y - bg_current_vertical_line_h - FONT_DECENT;
	    //脈波伝搬時間などの情報を表示する文字列のy座標
	    labelCurrent2_y = pls_gl_y - bg_current_vertical_line_h - FONT_DECENT;

		//心拍数などの情報を表示する文字列のy座標
		labelTrend1_y = hr_gl_y - bg_trend_vertical_line_h - FONT_DECENT;
		// 脈波伝搬時間などの情報を表示する文字列のy座標
		labelTrend2_y = pat_gl_y - bg_trend_vertical_line_h - FONT_DECENT;
		// 血圧などの情報を表示する文字列のy座標 */
		labelTrend3_y = sbp_gl_y - bg_trend_vertical_line_h - FONT_DECENT;

	    //振幅スペクトルの最大値(この値で振幅スペクトルを正規化する)
	    maxSpectrum = (height * 2 / 5);

	    //振幅スペクトルを保持する配列
	    ecgSpectrum = new double[width];
	    for(int i = 0; i < width; i++){
	    	ecgSpectrum[i] = 0;
	    }
	}

	//===============================描画処理==================================//
	/**
	 * 描画(オフグラフィックスを反映させる)
	 */
	protected void paint(Graphics g) {
		g.drawImage(offImg,0,0,Graphics.LEFT|Graphics.TOP);
	}

	//===========================各画面の描画で共通==============================//
	/**
	 * 個々のラベルを描画
	 * @param g Graphicsオブジェクト
	 * @param label 描画するラベル
	 * @param value 描画する値
	 * @param x     x座標
	 * @param y     y座標
	 */
	private void drawLabel(DojaGraphics g,String label,int value,int x,int y){
		String str = "";
		if(value == -1){//まだ値が算出されていないとき
			str = label + " " + "N/A";
		}else{
			if(value >= 100){
				str = label + " " + value;
			}else if (value >= 10){
				str = label + "  " + value;
			}else{
				str = label + "   " + value;
			}
		}
		g.drawString(str,x,y);
	}

	//============================カレント画面の描画================================//
	/**
	 * カレント画面の描画処理を行う
	 * @param ecgBuf                 オリジナルのECG信号
 	 * @param plsBuf                 脈波信号
	 * @param rWavePeek              R波ピークのインデックスを保持する配列
	 * @param hr　　　　　　　　　　　　　　　　　　 心拍数
	 * @param pat                    脈波伝搬時間
	 * @param sbp                    収縮期血圧
	 */
	public void drawCurrent(double[] ecgBuf,
	        		        double[] plsBuf,int[] rWavePeek,double hr,double pat,double sbp){
		offGra.lock();

		//--------------背景の描画---------------//
		//背景塗りつぶし
		offGra.setColor(BG_COLOR);
		offGra.clearRect(0,0,width,height);
		//心電図の背景の目盛り線を描画
		drawBGLineCurrent(offGra,ecg_gl_y);
		//脈波の背景の目盛り線を描画
		drawBGLineCurrent(offGra,pls_gl_y);

		//--------------信号の描画---------------//
		//波形描画(心電図)(デフォルトではノッチフィルタ適用信号を描画)
		offGra.setColor(ECG_COLOR);
		drawCurrentSignal(offGra,ecgBuf,ecg_gl_y);
		//波形描画(脈波)
		offGra.setColor(PLS_COLOR);
		drawCurrentSignal(offGra,plsBuf,pls_gl_y);
		//R波のピークを描画
		offGra.setColor(R_WAVE_COLOR);
		drawRwavePeek(rWavePeek);

		//--------------ラベル描画---------------//
		//心拍数
		int x = (STR_MARGIN + (FONT.stringWidth(HR_STR + "....")>>1));
		offGra.setColor(ECG_COLOR);
		drawLabel(offGra,HR_STR,(int)hr,x,labelCurrent1_y);
		//脈波伝搬時間
		x = (STR_MARGIN + (FONT.stringWidth(PAT_STR + "....")>>1));
		offGra.setColor(PLS_COLOR);
		drawLabel(offGra,PAT_STR,(int)pat,x,labelCurrent2_y);
		//血圧
		offGra.setColor(SBP_COLOR);
		x = width - ((FONT.stringWidth(SBP_STR + "....")>>1) + STR_MARGIN);
		drawLabel(offGra,SBP_STR,(int)sbp,x,labelCurrent2_y);

		if(Main.DEBUG){
			offGra.setColor(PLS_COLOR);

			x = width - ((FONT.stringWidth(dbgMessage)>>1) + STR_MARGIN);
			offGra.drawString(dbgMessage,x,labelCurrent1_y + FONT_HEIGHT);

			x = width - ((FONT.stringWidth(dbgMessage2)>>1) + STR_MARGIN);
			offGra.drawString(dbgMessage2,x,labelCurrent1_y + (FONT_HEIGHT<<1));

			x = width - ((FONT.stringWidth(dbgMessage3)>>1) + STR_MARGIN);
			offGra.drawString(dbgMessage3,x,labelCurrent1_y + (FONT_HEIGHT*3));

			x = width - ((FONT.stringWidth(dbgMessage4)>>1) + STR_MARGIN);
			offGra.drawString(dbgMessage4,x,labelCurrent1_y + (FONT_HEIGHT*4));
		}

		offGra.unlock(true);
	}

	/**
	 * カレント画面の背景の目盛り線を描画
	 * @param g   Graphicsオブジェクト
	 * @param glY 描画の基準座標 ECG,PLSのグランドライン
	 */
	private void drawBGLineCurrent(DojaGraphics g,int glY){
		//描画色を変更
		g.setColor(BG_LINE_COLOR);
		//描画の基準の線を描画
		g.drawLine(0,glY,width,glY);

		//描画色を変更
		g.setColor(BG_LINE_COLOR2);
	    //背景の目盛り線を描画(x軸方向の線)
	    for (int i = 1; i < (NUM_OF_CURRENT_BGLINE>>1); i++) {
	    	//上半分
	    	int y = glY - i*BG_LINE_MARGIN;
	    	offGra.drawLine(0,y,width,y);
	    	//下半分
	    	y = glY + i * BG_LINE_MARGIN;
	        offGra.drawLine(0,y,width,y);
	    }
	    //背景の目盛り線を描画(y軸方向の線)
	    int startY = glY - bg_current_vertical_line_h;
	    int endY   = glY + bg_current_vertical_line_h;
	    for (int i = 1; i*BG_LINE_MARGIN < width; i++) {
	    	int x = i*BG_LINE_MARGIN;
	        offGra.drawLine(x,startY,x,endY);
	    }
	}

	/**
	 * カレントの波形の描画
	 * @param g          レンダリング用Graphicsオブジェクト
	 * @param signal     描画する信号
	 * @param gly        信号の描画基準座標
	 */
	private void drawCurrentSignal(DojaGraphics g, double[] signal,int glY){
		//波形の大きさを調整
		double max = signal[0];
		double min = signal[0];
		double ave = 0;
		for(int i = 0; i < signal.length ; i++){
			if(max < signal[i]){
				max = signal[i];
			}else if(min > signal[i]){
				min = signal[i];
			}
			ave += signal[i];
		}
		ave /= signal.length;

		double scale= ((double)(bg_current_vertical_line_h)/(double)(max-ave))*yScale;

		//波形の描画
		for(int i = 0; (i+1)*xReductionRate < signal.length; i++){
			offGra.drawLine(i,glY - (int)(signal[i*xReductionRate]*scale) + (int)(ave*scale)
					     ,i+1,glY - (int)(signal[(i+1)*xReductionRate]*scale) + (int)(ave*scale) );
		}
	}

	/**
	 * R波のピークを描画する
	 * @param rWavePeek　R波ピークのある座標保持した配列(R波ピークがあるときは１、ない時は0)
	 */
	private void drawRwavePeek(int[] rWavePeek){
		for(int i = 0; i*xReductionRate < rWavePeek.length ; i++){
			for(int j = 0; j < xReductionRate; j++){
				//縮小しているときもR波のピークをきちんと表示できる
				//ようにR波のピークが(xScale - 1)分だけ
				//描画する点の周囲にR波のピークがないかどうか探す
				if(rWavePeek[i*xReductionRate + j] > 0){
					offGra.drawLine(i,labelCurrent1_y,i,labelCurrent1_y - BG_LINE_MARGIN);
					break;
				}
			}
		}
	}

	//===========================トレンド画面の描画===============================//
	/**
	 * トレンド画面の描画
	 * @param hrBuf  心拍数描画バッファ
	 * @param patBuf 脈波伝搬時間描画バッファ
	 * @param sbpBuf 収縮期血圧描画バッファ
	 */
	public void drawTrend(double[] hrBuf,double[] patBuf,double[] sbpBuf){
		offGra.lock();

		//背景塗りつぶし
		offGra.setColor(BG_COLOR);
		offGra.clearRect(0,0,width,height);

		//-------------背景を描画---------------//
		//心拍数の背景の目盛り線を描画
		drawBGLineTrend(offGra,hr_gl_y);
		//脈波伝搬時間の背景の目盛り線を描画
		drawBGLineTrend(offGra,pat_gl_y);
		//収縮期血圧の背景の目盛り線を描画
		drawBGLineTrend(offGra,sbp_gl_y);

		//-------------信号の描画---------------//
		//波形描画(心拍数)
		offGra.setColor(ECG_COLOR);
		drawTrendSignal(offGra,hrBuf,hr_gl_y);
		//波形描画(脈波伝搬時間)
		offGra.setColor(PLS_COLOR);
		drawTrendSignal(offGra,patBuf,pat_gl_y);
		//波形描画(血圧)
		offGra.setColor(SBP_COLOR);
		drawTrendSignal(offGra,sbpBuf,sbp_gl_y);

		//-------------ラベル描画---------------//
		//心拍数
		int x = (STR_MARGIN + (FONT.stringWidth(HR_STR  +"....")>>1));
		offGra.setColor(ECG_COLOR);
		drawLabel(offGra,HR_STR,(int)hrBuf[hrBuf.length-1],x,labelTrend1_y);
		//脈波伝搬時間
		x = (STR_MARGIN + (FONT.stringWidth(PAT_STR  +"....")>>1));
		offGra.setColor(PLS_COLOR);
		drawLabel(offGra,PAT_STR,(int)patBuf[patBuf.length-1],x,labelTrend2_y);
		//血圧
		offGra.setColor(SBP_COLOR);
		x = (STR_MARGIN + (FONT.stringWidth(SBP_STR  +"....")>>1));
		drawLabel(offGra,SBP_STR,(int)sbpBuf[sbpBuf.length-1],x,labelTrend3_y);

		offGra.unlock(true);
	}

	/**
	 * トレンドの波形の描画
	 * @param g         レンダリング用Graphicsオブジェクト
	 * @param signal    描画する信号
	 * @param gly       信号の描画基準座標
	 */
	private void drawTrendSignal(DojaGraphics g, double[] signal,int glY){
		//波形の大きさを調整
		double max = signal[0];
		double min = signal[0];
		double ave = 0;
		for(int i = 0; i < signal.length ; i++){
			if(max < signal[i]){
				max = signal[i];
			}else if(min > signal[i]){
				min = signal[i];
			}
			ave += signal[i];
		}
		ave /= signal.length;

		double scale  = ((double)(bg_trend_vertical_line_h)/(double)(max));

		//波形の描画
		for(int i = 0; i< signal.length - 1; i++){
			offGra.drawLine(i,glY - (int)(signal[i]*scale) + (int)(ave*scale)
					     ,i+1,glY - (int)(signal[(i+1)]*scale) + (int)(ave*scale) );
		}
	}


	/**
	 * トレンド画面の背景を描画する
	 * @param g
	 * @param glY
	 */
	private void drawBGLineTrend(DojaGraphics g,int glY){
		//描画色を変更
		g.setColor(BG_LINE_COLOR);
		//描画の基準の線を描画
		g.drawLine(0,glY,width,glY);

		//描画色を変更
		g.setColor(BG_LINE_COLOR2);
	    //背景の目盛り線を描画(横の線)
	    for (int i = 1; i < (NUM_OF_TREND_BGLINE>>1); i++) {
	    	//上半分
	    	int y = glY - i*BG_LINE_MARGIN;
	    	offGra.drawLine(0,y,width,y);
	    	//下半分
	    	y = glY + i * BG_LINE_MARGIN;
	        offGra.drawLine(0,y,width,y);
	    }
	    //背景の目盛り線を描画(縦の線)
	    int startY = glY - bg_trend_vertical_line_h;
	    int endY   = glY + bg_trend_vertical_line_h;
	    for (int i = 1; i*BG_LINE_MARGIN < width; i++) {
	    	int x = i*BG_LINE_MARGIN;
	        offGra.drawLine(x,startY,x,endY);
	    }
	}

	//============================振幅スペクトルの描画===============================//

	/**
	 * 振幅スペクトル画面の描画
	 * @param ecgBuf  心電図信号の配列
	 * @param rWavePeek R波ピークを格納した配列
	 * @param hr 心拍数
	 *
	 */
	public void drawECGSpectrum(double[] ecgBuf,int[] rWavePeek,int hr){
		offGra.lock();

		//---------------背景の描画----------------//
		//背景塗りつぶし
		offGra.setColor(BG_COLOR);
		offGra.clearRect(0,0,width,height);
		//バックグラウンドの描画
		drawBGLineSpectrum(offGra);

		//-----------時間領域信号の描画-------------//
		//心電図
		offGra.setColor(ECG_COLOR);
		drawCurrentSignal(offGra,ecgBuf,ecg_gl_y);
		//R波ピークをプロット
		offGra.setColor(R_WAVE_COLOR);
		drawRwavePeek(rWavePeek);

		//-------------スペクトルの描画--------------//
		offGra.setColor(SPC_COLOR);
		drawSpectrum(offGra,ecgSpectrum);

		//---------------ラベルの描画---------------//
		//心拍数
		int x = (STR_MARGIN + (FONT.stringWidth(HR_STR +"...." )>>1));
		offGra.setColor(ECG_COLOR);
		drawLabel(offGra,HR_STR,hr,x,labelCurrent1_y);
		//スペクトル
		offGra.setColor(SPC_COLOR);
		x = (STR_MARGIN + (FONT.stringWidth(SPC_STR)>>1));
		offGra.drawString(SPC_STR,x,height - maxSpectrum - FONT_DECENT);

		offGra.unlock(true);
	}

	/**
	 * スペクトル画面を表示しているときの背景を描画
	 */
	public void drawBGLineSpectrum(DojaGraphics g){

		//---------------------------時間領域のグラフのの背景---------------------------//
		//カレントで使用している上半分の画面と同じ
		drawBGLineCurrent(offGra,ecg_gl_y);

		//---------------------------周波数領域のグラフのの背景---------------------------//

		//目盛り線  y軸方向
		int y = height;
		//青色の線の間隔
		int blue_margin_x = BG_LINE_MARGIN*5;

		//目盛りを描画(0.0 ~ )  y軸方向
		for(int i = BG_LINE_MARGIN; i < maxSpectrum ; i += BG_LINE_MARGIN){
			y = height - i;
			if(i % blue_margin_x == 0){
				//横に端から端まで線を引く
				g.setColor(BG_LINE_COLOR2);
				g.drawLine(1,y,width,y);
				//0.5ごとに青色の線を引く
				g.setColor(BG_STR_COLOR);
				g.drawLine(1, y, BG_LINE_LONG, y);
				//数値を文字で描画
				g.setColor(BG_STR_COLOR);
				g.drawString(i + "",BG_LINE_MARGIN,y);
				//色を戻す
				g.setColor(BG_LINE_COLOR);
			}else{
				g.drawLine(1,y,BG_LINE_LONG,y);
			}
		}

		//X軸を引く
		g.setColor(BG_LINE_COLOR);
		g.drawLine(0,spectrum_gl_y,width,spectrum_gl_y);

		//ナイキスト周波数
		//sone1201
		//int nyquist = BPMonitorManager.SAMPLE>>1;
		int nyquist = BPCalcTSK.SAMPLE>>1;

		//青色の線の間隔
		int blue_margin_y = nyquist/5;
		if(blue_margin_y <= 30)blue_margin_y = 30;
		//目盛り線の間隔
		int line_margin_y = blue_margin_y / 5;

		//目盛り線 x軸方向(周波数域)
		for(int freq = 0; freq <= nyquist ; freq += line_margin_y){

			//１つの目盛りでどれぐらいの周波数を表すか
			int	dX = (freq*width)/nyquist;
			//Y軸
			if(dX <= 0)dX = 1;

			if(freq % blue_margin_y == 0){
				//端から端まで縦の線を引く
				g.setColor(BG_LINE_COLOR2);
				g.drawLine(dX,spectrum_gl_y,dX,spectrum_gl_y-maxSpectrum);

				//青色の線を一定間隔で描画
				if(freq == nyquist){
					//---最後見えなくなるので少し前に描画---//
					//青色の線を引く
					g.setColor(BG_STR_COLOR);
					g.drawLine(dX - 1, spectrum_gl_y, dX - 1, spectrum_gl_y - BG_LINE_LONG);
					//周波数を文字列として描画
					g.setColor(BG_STR_COLOR);
					g.drawString(freq + "", dX - FONT.stringWidth(freq +"")
							     , spectrum_gl_y - FONT_DECENT);
					//色を戻しておく
					g.setColor(BG_LINE_COLOR);
				}else if(freq == 0){
					//--最初見えにくくなるので少し後に描画--//
					g.setColor(BG_STR_COLOR);
					g.drawLine(dX ,spectrum_gl_y , dX ,spectrum_gl_y - BG_LINE_LONG);
					//周波数を文字列として描画
					g.setColor(BG_STR_COLOR);
					g.drawString(freq + "", dX + FONT.stringWidth(freq +"")
								 ,spectrum_gl_y - FONT_DECENT);
					//色を戻しておく
					g.setColor(BG_LINE_COLOR);
				}else{
					//目盛りの上に周波数を文字列とし描画
					//青色の線を引く
					g.setColor(BG_STR_COLOR);
					g.drawLine(dX,spectrum_gl_y,dX,spectrum_gl_y-BG_LINE_LONG);
					//周波数を文字列として描画
					g.setColor(BG_STR_COLOR);
					g.drawString(freq + "", dX,spectrum_gl_y - FONT_DECENT);
					//色を戻しておく
					g.setColor(BG_LINE_COLOR);
				}
			}else{
				//目盛りを描画
				g.drawLine(dX,spectrum_gl_y, dX,spectrum_gl_y-BG_LINE_LONG);
			}
		}
	}

	/**
	 * 振幅スペクトルの描画
	 * @param spectrum スペクトルの配列
	 * @param y 描画基準のy座標
	 */
	private void drawSpectrum(DojaGraphics g,double[] spectrum){
		//振幅スペクトルを描画
		for(int i = 0; i < spectrum.length -1; i++){
			g.drawLine(i,(int)spectrum[i],i+1,(int)spectrum[i+1]);
		}
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

	//================================その他===================================//

	/**
	 * x軸方向の縮小倍率を変更
	 * @param xReductionRate x軸方向の縮小倍率
	 */
	public void setXReductionRate(int xReductionRate){
		if(MAN_X_REDUCTION_RATE < xReductionRate
		 ||MIX_X_REDUCTION_RATE > xReductionRate )return;

		this.xReductionRate = xReductionRate;
	}

	/**
	 * x軸方向の縮小倍率を得る
	 * @return x軸方向の縮小倍率
	 */
	public int getXReductionRate() {
		return xReductionRate;
	}

	public void setEcgSpectrum(double[] val) {
		setSpectrum(val,ecgSpectrum);
	}

	/**
	 * スペクトルを更新
	 * @param val  スペクトルの配列
	 * @param dest 描画用バッファ
	 */
	public synchronized void setSpectrum(double val[],double dest[]){
		//ナイキスト周波数を超えない周波数帯を描画バッファにいれるように調整
		double xgain = (double)(val.length >> 1)/(double)dest.length;
		double max = val[1];
		//X軸方向のマッピング 直流成分は表示しないので1から
		dest[0] = 0;
		for(int i = 1; i < dest.length ; i++){
			dest[i] = val[(int)(i*xgain)];
			if(max < dest[i])max = dest[i];
		}
		//y軸の倍率
		double ygain = (double)maxSpectrum/(double)max;
		//Maxを基準としてY座標を正規化
		for(int i = 0; i < dest.length ; i++){
			dest[i] = (spectrum_gl_y-ygain*dest[i]);
		}
	}

	//=============================== デバッグ用=================================//
	String dbgMessage = "";
	String dbgMessage2 = "";
	String dbgMessage3 = "";
	String dbgMessage4 = "";

	public void drawDBG(String str){
		this.dbgMessage = str;
	}
	public void drawDBG2(String str){
		this.dbgMessage2 = str;
	}
	public void drawDBG3(String str){
		this.dbgMessage3 = str;
	}
	public void drawDBG4(String str){
		this.dbgMessage4 = str;
	}


	/**
	 * 表示する
	 */
	public void setDisplay(){
		Display.getDisplay(midlet).setCurrent(this);
	}
}
