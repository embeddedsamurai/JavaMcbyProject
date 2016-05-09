package calendar.renderer;

import util.DojaFont;
import util.DojaGraphics;

import calendar.util.CalendarUtil;
import calendar.util.ReadDBData;
import calendar.util.ReadDBListener;

public class RawWaveRenderer implements ReadDBListener{
	
	//================================定数====================================//

	//------- 色関連  --------//
	/** 背景色 */
	private static final int BG_COLOR = 0x00FFFFFF;
	/** バックグランドの目盛りの色 */
	private static final int BG_LINE_COLOR = 0x999999;
	/** バックグランドの目盛りの色 */
	private static final int BG_LINE_COLOR2 = 0xC9C9C9;
	/** 心電図信号の色 */
	private static final int ECG_COLOR = 0x00FF0066;
	/** 目盛りの字の色 */
	private final int BG_STR_COLOR = 0x000000FF;
		
	/** 波形の表示ページ */
	private final int NUM_OF_PAGE   = 10;

	//-----------座標------------//
	/** 波形描画基準座Y座標 (0が中央)*/
	private final int ground_y;
	
	//------- ECG,PLS用の拡大縮小倍率の定義 --------//
	/** 最大のX軸方向の表示縮小倍率 */
	public final int maxReductionRate = 5;
	/** 最小のX軸方向の表示縮小倍率 */
	public final int minReductionRate = 5;
	
	//------- バックグラウンド  --------//
	/** バックグランドの目盛りの間隔 */
	private static final int BG_LINE_MARGIN = 10;
	/**	バックグラウンドの目盛り線の数(グランドラインを除く)*/
	private static final int NUM_OF_BGLINE = 13;
	/** 背景の縦の目盛り線の高さ*/
	private final int bg_vertical_line_h = (BG_LINE_MARGIN * (NUM_OF_BGLINE>>1));	
	
	//------- フォント関連   --------//
	/** フォント */
	private static final DojaFont FONT = DojaFont.getFont(DojaFont.SIZE_TINY);
	/** フォントのディセント */
	private static final int FONT_DECENT = FONT.getDescent();
	/** フォントのアセント */
	private static final int FONT_ASCENT = FONT.getAscent();
	/** フォントの高さ */
	private static final int FONT_HEIGHT = FONT_ASCENT + FONT_DECENT;
	
	/** DBから一度に読み込むデータ数 */
	public static int numOfDataReadDB;
	
	//================================変数====================================//
		
	/** カレント波形の表示の縮小倍率(X軸方向)*/
	private int xReductionRate;
	/** カレント波形の表示の倍率(Y軸方向)*/
	private int yScale = 1;
	
	/** モード */
	int mode = -1;
	
	/** 受信データ */
	private int[] recieveData;	
	/** 描画用データ */
	private int[] drawData;
	
	/** 幅 */
	private int width;
	/** 高さ */
	private int height;	
	/** 波形の表示のオフセット */
	private int offset = 0;
	
	/**
	 *  最大データ数（これをセットしておくとこれ以上のデータをDBから読まない)
	 *  棒グラフで得たデータ長をセットしておく
	 */ 
	private int maxdataLen = 0;
	
	/** 
	 * 波形の全体のインデックス 
	 * オフセットはあらたにDBから読むたびにリセットされるが、
	 * このインデックスはリセットされず全体での秒数を割り出すのに
	 * 使われる
	 */
	private int index = 0;	
	
	/** 
	 * 初めて読み込んだデータなら0
	 * 最後のページの右端まできて再び読み込み、表示したら+1
	 * 最初のページの左端まできて再び読み込み、表示したら-1
	 */
	private int pageIndex = 0;
	/** 日付ラベル */
	private String dateLabel ="";
	
	/** データの日時をUNIX時間で表したもの*/
	private long time = 0;

	/**
	 * コンストラクタ
	 * @param canvas
	 * @param baseSbp
	 * @param w
	 * @param h
	 */
	public RawWaveRenderer(int width, int height){				
		this.width = width;
		this.height = height;
						
		//DBからのデータ読み込み数
		numOfDataReadDB = (maxReductionRate*width*NUM_OF_PAGE);
		
		//X軸方向の倍率
		xReductionRate   = maxReductionRate;
		
		//描画基準Y座標
		ground_y = bg_vertical_line_h + FONT_HEIGHT + FONT_ASCENT;
		
		//描画用データ
		drawData = new int[width*maxReductionRate];
				
	}/* End of StaticWaveRenderer */
	
	/**
	 * 初期化
	 * 新たな波形を表示する前に呼び出す
	 */
	public void init(){
		offset = 0;
		pageIndex = 0;
		xReductionRate = maxReductionRate;
	}
	
	/**
	 * 描画処理を行う
	 * @param g
	 */
	public void draw(DojaGraphics g){
		
		//背景の塗りつぶし
		g.setColor(BG_COLOR);
		g.clearRect(0,0, width, height);		

		//生波形の描画
		drawRawSignal(g);
			
	}//End of draw()
	
	/**
	 * 背景の目盛り線を描画
	 * @param g   Graphicsオブジェクト
	 * @param glY 描画の基準座標 ECG,PLSのグランドライン
	 */
	private void drawBGLine(DojaGraphics g,int glY){
		//描画色を変更
		g.setColor(BG_LINE_COLOR);
		//描画の基準の線を描画
		g.drawLine(0,glY,width,glY);

		//描画色を変更
		g.setColor(BG_LINE_COLOR2);
	    //背景の目盛り線を描画(x軸方向の線)
	    for (int i = 1; i < (NUM_OF_BGLINE>>1); i++) {
	    	//上半分
	    	int y = glY - i*BG_LINE_MARGIN;
	    	g.drawLine(0,y,width,y);
	    	//下半分
	    	y = glY + i * BG_LINE_MARGIN;
	        g.drawLine(0,y,width,y);
	    }
	    //背景の目盛り線を描画(y軸方向の線)
	    int startY = glY - bg_vertical_line_h;
	    int endY   = glY + bg_vertical_line_h;
	    for (int i = 1; i*BG_LINE_MARGIN < width; i++) {
	    	int x = i*BG_LINE_MARGIN;
	        g.drawLine(x,startY,x,endY);
	    }
	}
	
	//-----------------------生波形の表示----------------------------//
	/**
	 * 心電図や脈波の生波形描画
	 * @param g Graphics オブジェクト
	 */
	private void drawRawSignal(DojaGraphics g){
		//フォントの設定
		g.setFont(FONT);
		
		//バックグラウンドの描画
		drawBGLine(g,ground_y);
			
		//色の変更		
		g.setColor(ECG_COLOR);				
		//信号の描画
		drawSignal(g,drawData,ground_y);
				
		//時間軸の描画
		final int baseY = ground_y + bg_vertical_line_h ;
		final int topY  = ground_y - bg_vertical_line_h ;
		int y = baseY + BG_LINE_MARGIN;
		g.setColor(BG_STR_COLOR);
		g.drawLine(0,y,width,y);
		
		//目盛りの描画
		int interval = (BG_LINE_MARGIN<<1);
		int x = 0;
		String str = "";
		for(int i = 0; i <= width ; i+= interval){
			if(i == width){//最後
				x = i-1;
			}else{
				x = i;
			} 
			g.drawLine(x, y-2, x, y+2);			
		}
		
		//目盛りの文字の描画
		interval = interval * 3;
		y += FONT_ASCENT;
		for(int i = 0; i <= width; i+= interval){
			//×4は一目盛りが4msだから
			// 100で割ってdoubleにして10で割っているのは小数点以下一桁にするため
			str = "" + (double)((index*4 + i*(xReductionRate*4))/100)/10;
			
			if(i == width){//最後
				x = i-1 - (FONT.stringWidth(str)>>1);
			}else if(i == 0){
				x = (FONT.stringWidth(str)>>1);
			}else{	
				x = i;
			} 			
			g.drawString(str, x , y);
		}
			
		
		//ラベルの描画
		final int xMargin = 5;
		x = width>>1;
		y = topY - FONT_DECENT - 5;
		g.drawString(dateLabel,x,y);
				
		str = "(秒)";
		x = width - (FONT.stringWidth(str)>>1) - xMargin;
		y = baseY + BG_LINE_MARGIN + FONT_ASCENT + FONT_HEIGHT;
		g.drawString(str,x,y);
		
		if(xReductionRate == 1){
			//str = "↓拡大 ↑縮小 : x "+xReductionRate +" ←前 次→";
			str = "x "+xReductionRate +" ←前 次→";
		}else{
			//str = "↓拡大 ↑縮小 : x 1/"+xReductionRate +" ←前 次→";;
			str = "x 1/"+xReductionRate +" ←前 次→";
		}		
		x = xMargin + (FONT.stringWidth(str)>>1);
		y += FONT_HEIGHT; 
		g.drawString(str,x,y);
	}
		
	/**
	 * 信号の描画
	 * @param g      DojaGraphics
	 * @param signal 
	 * @param glY
	 * @param offset
	 */
	private void drawSignal(DojaGraphics g, int[] signal,int glY){
		
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
		
		double scale= ((double)(bg_vertical_line_h)/(double)(max-ave))*yScale;

		//波形の描画
		// (offset + i < recieveData.length) によりデータの端まで来たらそれ以上描画しないということを示す
		for(int i = 0;
		   ((i+1)*xReductionRate < signal.length) && 
	       (offset + (i+1)*xReductionRate < recieveData.length);i++){
			
			g.drawLine(i  ,glY - (int)(signal[i*xReductionRate]*scale) + (int)(ave*scale)
					  ,i+1,glY - (int)(signal[(i+1)*xReductionRate]*scale) + (int)(ave*scale) );
			
		}
			
	}//End of drawSignal()
	
	/**
	 * 描画モードを変更
	 * @param mode
	 */
	public void setMode(int mode){
		this.mode = mode;
	}
	
	/**
	 * 波形表示位置を変更
	 */
	private void setOffset(int offset){		
		
		if(recieveData.length > drawData.length + offset){
			for(int  i = 0 ; i < drawData.length ; i++){
				drawData[i] = recieveData[i+offset];
			}						
		}else{			
			int  i = 0;
			
			if(offset < recieveData.length){
				//一部波形が存在するとき
				for(i = 0; i < recieveData.length - offset ; i++){
					drawData[i] = recieveData[i+offset];
				}				
				for(i = recieveData.length - offset; i < drawData.length ; i++){
					drawData[i] = 0;
				}	
			}else{
				//波形がまったくないとき
				for(i = 0; i < drawData.length ; i++){
					drawData[i] = 0;
				}
			}						
		}
	}
	
	//========================左右の移動=========================//
	
	/**
	 * 次の波形を表示
	 * 端まで来たらDBから読み込みをする
	 * 
	 * @return DBから次のデータを読み込むときにDB上でのデータのオフセットを返す
	 *         読み込む必要がなければ-1
	 */
	public int nextWave(){
		int dbOffset = -1;
		//次のインデックス
		int nextIndex = offset + (width*xReductionRate);
		System.out.println("#nextWave()" + nextIndex);
		
		if(numOfDataReadDB <= nextIndex && 
		   index + numOfDataReadDB < maxdataLen ){
			//もうバッファにないとき、かつまだDBから読むデータがあるとき
			//DBからデータを読み込んで表示する
			System.out.println("DBから読み込み");
			//全体のインデックス
			index  = pageIndex*numOfDataReadDB + nextIndex;
			//DB上から読み込むデータのオフセット
			dbOffset = index;
			//ページを一つ増やす			
			pageIndex++;
			//ページ内オフセットを0に			
			offset = 0;
		}else if(nextIndex < xReductionRate*recieveData.length){		
			//次の波形を表示
			offset = nextIndex;			
			index  = pageIndex*numOfDataReadDB + nextIndex;			
			//画面調整(描画する波形の変更)		
			setOffset(offset);			
		}//End of nextWave()
		
		//DEBG
		System.out.println("offset     " + offset);
		System.out.println("index      " + index);
		System.out.println("page       "  + pageIndex);
		System.out.println("nextIndex  "  + nextIndex);
		System.out.println("maxDataLen  "  + maxdataLen);
		System.out.println("numOfDataReadDB "  + numOfDataReadDB);		
		System.out.println();
		
		return dbOffset;
	}//End of nextWave()
	
	/**
	 * 直前の波形を表示
	 * 無い時は表示しない
	 * 
	 * @return DBから次のデータを読み込むときにDB上でのデータのオフセットを返す
	 *         読み込む必要がなければ-1
	 */
	public int previousWave(){
		int dbOffset = -1;
		//次のインデックス
		int nextIndex = offset - (width*xReductionRate);
		System.out.println("#previousWave()" + nextIndex);
		
		if(nextIndex < 0){
			//これ以上ないとき、
			if(pageIndex == 0){
				//最初のページのときはなにもしない
				offset = 0;	
				index = 0;
			}else{
				//まだ戻るページがあるときは
				//DBからデータを読み込んで表示する
				System.out.println("DBから読み込み");
				//全体のインデックス
				index  = pageIndex*numOfDataReadDB + nextIndex; 
				//DB上から読み込むデータのオフセット
				dbOffset = index;
				//ページ内オフセットを前のページの最後に
				offset = numOfDataReadDB - (width*xReductionRate);
				//ページを一つ戻す
				pageIndex--;				 
			}//End of if()			
		}else{
			offset = nextIndex;
			index  = pageIndex*numOfDataReadDB + nextIndex;
			//画面調整(描画する波形の変更)		
			setOffset(offset);
		}//End of if()
		
		//DEBG
		System.out.println("offset     " + offset);
		System.out.println("index      " + index);
		System.out.println("page       "  + pageIndex);
		System.out.println("nextIndex  "  + nextIndex);	
		System.out.println("maxDataLen  "  + maxdataLen);
		System.out.println("numOfDataReadDB "  + numOfDataReadDB);		
		System.out.println();
					
		return dbOffset;
	}//End of previousWave()
	
	//=======================拡大率の調整========================//

	/**
	 * x軸方向の縮小倍率を変更
	 * @param xReductionRate x軸方向の縮小倍率
	 */
	public void setXReductionRate(int xReductionRate){		
		if(maxReductionRate < xReductionRate
		 ||minReductionRate > xReductionRate )return;

		this.xReductionRate = xReductionRate;
		setOffset(offset);
	}

	/**
	 * x軸方向の縮小倍率を得る
	 * @return x軸方向の縮小倍率
	 */
	public int getXReductionRate() {		
		return xReductionRate;
	}
	
	//======================ReadDBListener======================//
	/**
	 * DBからの読み込みが成功したときの呼ばれる 
	 */
	public void onCompleteReadDB(int[] data,int type,long time) {
		System.out.println("#onCompleteReadDB@staticWave");
		
		//時刻を保存
		this.time = time;		
		
		//読み込んだデータを保存しておく
		recieveData = new int[data.length];
		for(int i = 0; i < data.length ; i++){
			recieveData[i] = data[i];
		}/* End of for() */
		
		//ラベルを更新
		int[] dateArray = CalendarUtil.calcTimeInfo(time);
		
		String typeStr = "";
		switch (type) {		
		case ReadDBData.TYPE_ECG:
			typeStr ="心電図";
			break;
		case ReadDBData.TYPE_PLS:
			typeStr ="脈波";
			break;
		case ReadDBData.TYPE_HR:
			typeStr ="心拍数";
			break;
		case ReadDBData.TYPE_BP:
			typeStr ="血圧";
			break;		
		}//End of switch
		
		dateLabel = typeStr +" " + dateArray[1] +"月 " + dateArray[2] + "日";
		
		setOffset(offset);
		
	}/* End of onCompleteReadDB() */
	
	/**
	 * DBからの読み込みが失敗したときの呼ばれる
	 */
	public void onErrorReadDB() {
		System.out.println("#onErrorReadDB@waitScreen");
	}/* End of onErrorReadDB */
 
	
	//---------------------set,get----------------------//
	/**
	 * 最大データ長をセットしておく
	 * @param maxdataLen
	 */
	public void setMaxdataLen(int maxdataLen) {
		this.maxdataLen = maxdataLen;
	}
	
	/**
	 * 波形の最大データ長を得る。
	 * @return 波形の最大データ長
	 */
	public int getMaxdataLen() {
		return maxdataLen;
	}
	
	/**
	 * DBからの取得データの時刻パラメータを得る
	 * @return DBからの取得データの時刻パラメータ
	 */
	public long getTime() {
		return time;
	}
	
}
