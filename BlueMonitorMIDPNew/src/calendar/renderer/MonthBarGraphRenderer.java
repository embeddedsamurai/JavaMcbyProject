package calendar.renderer;

import javax.microedition.m3g.PolygonMode;
import javax.microedition.xml.rpc.Type;

import util.DojaFont;
import util.DojaGraphics;
import calendar.util.CalendarUtil;
import calendar.util.ReadDBData;
import calendar.util.ReadDBListener;
import calendar.util.ReadNumOfDBData;

public class MonthBarGraphRenderer implements ReadDBListener {
	//================================定数====================================//

	//------- 色関連  --------//
	/** 背景色 */
	private static final int BG_COLOR = 0x00FFFFFF;
	/** バックグランドの目盛りの色 */
	private static final int BG_LINE_COLOR = 0x999999;
	/** 目盛りの字の色 */
	private final int BG_STR_COLOR = 0x000000FF;
	/** 棒グラフの色 */
	private final int BAR_COLOR = 0x00FFC26A;
	/** 棒グラフの色 */
	private final int BAR_RED_COLOR = 0x00FF0000;

	//-----------座標------------//
	/** 画基準座Y座標 */
	private final int ground_y;	
	
	//------- フォント関連   --------//
	/** フォント */
	private static final DojaFont FONT = DojaFont.getFont(DojaFont.SIZE_TINY);
	/** フォントのディセント */
	private static final int FONT_DECENT = FONT.getDescent();
	/** フォントのアセント */
	private static final int FONT_ASCENT = FONT.getAscent();
	/** フォントの高さ */
	private static final int FONT_HEIGHT = FONT_ASCENT + FONT_DECENT;
		
	/** X軸方向の余白 */
	private static final int xMargin = 5;
	/** Y軸方向の余白 */
	private static final int yMargin = 5;
	
	/** 幅 */
	private final int width;
	/** 高さ */
	private final int height;
	
	//================================変数====================================//
			
	/** 受信データ(棒グラフ用) */
	private int[] barData = new int[]{0};;
	/** 受信データ(折れ線グラフ用) */
	private int[] polygonalData = new int[]{0};;
	
	/** 背景の縦の目盛り線の高さ*/
	private int bg_vertical_line_h;
	/** 選択されたバーのインデックス */
	private int selectedIndex = -1; 
	
	/** 日付ラベル */
	private String dateLabel ="";
	
	/** X軸方向の描画開始座標 */	
	private int startX = 0;
	/** Y軸に表示する数を割る数*/
	private int den = 1;
	/** 棒グラフ用データの最大値 */
	private int barMax = 1;	
	/** 最大の桁数 */ 
	private int sig = String.valueOf(barMax).length();
	/** 日付を保持する配列*/
	private int[] dateArray = new int[]{0};
	
	/** 折れ線グラフ用データの最大値 */
	private int polygonalMax = 1;
	
	/** 折れ線グラフを描画するかどうか*/
	private boolean polygonalFlag = false;
	
	/**
	 * コンストラクタ
	 * @param canvas
	 * @param baseSbp
	 * @param w
	 * @param h
	 */
	public MonthBarGraphRenderer(int width, int height){				
		this.width = width;
		this.height = height;
		
		//描画基準Y座標
		ground_y = height - (FONT_HEIGHT) - yMargin;
		bg_vertical_line_h = (FONT_HEIGHT<<1) + yMargin;		
				
	}/* End of StaticWaveRenderer */
	
	/**
	 * 選択されている日を返す
	 * 
	 * @return 選択する項目がない時はすべて-1の配列を返す
	 * <br />  0番目に年、１番目に月、２番目に日,３番目に時間 
	 */
	public int[] getSelectedDate(){
		if(selectedIndex ==-1){
			return new int[]{-1,-1,-1,-1};
		}else{
			return new int[]{dateArray[0],dateArray[1],(selectedIndex+1),0};
		}
	}
	
	/**
	 * 次のバーを選択する
	 */
	public void nextBar(){
		//データがない時は何もしない
		if(selectedIndex < 0){
			return;
		}
		int i = selectedIndex;		
		while(true){
			i = (i + 1)%barData.length;
			if(barData[i] != 0){
				selectedIndex = i;
				break;
			}				
		}		
	}
	
	/**
	 * 前のバーを選択する
	 */
	public void previousBar(){
		//データがない時は何もしない
		if(selectedIndex < 0){
			return;	
		}
		int i = selectedIndex;
		while(true){
			i = ((i-1) +barData.length)%barData.length;
			if(barData[i] != 0){
				selectedIndex = i;
				break;
			}				
		}		
	}
	
	/**
	 * 描画処理を行う
	 * @param g
	 */
	public void draw(DojaGraphics g){
		
		//背景の塗りつぶし
		g.setColor(BG_COLOR);
		g.clearRect(0,0, width, height);
		
		//棒グラフの描画
		drawBarGraph(g, ground_y);
		//折れ線グラフの描画
		if(polygonalFlag){
			drawPolygonalGraph(g);			
		}

	}//End of draw()
	
	/**
	 * 棒グラフを描画
	 * @param g   Graphicsオブジェクト
	 * @param glY 描画の基準座標 ECG,PLSのグランドライン
	 */
	private void drawBarGraph(DojaGraphics g,int glY){
		//描画色を変更
		g.setColor(BG_LINE_COLOR);
		
		int x = 0;
		int y = 0;
		
		if(selectedIndex < 0){
			//データがない時
			g.setColor(BAR_RED_COLOR);
			g.drawString("データがありません",(width>>1),(height>>1));
		}else{
			startX = xMargin + FONT.stringWidth("000");
			
			//X軸		
			x = startX - 1;
			g.drawLine(x,glY,width-(xMargin<<1),glY);		
			//Y軸
			g.drawLine(x,glY,x,bg_vertical_line_h);

			//X軸ラベルの描画		
			g.setColor(BG_STR_COLOR);
			int interval = ((width-xMargin)-startX)/barData.length;
			for(int i = 0; i <= barData.length - 5; i+=5){
				x = interval*i + startX + (interval>>1);
				
				if(i == selectedIndex && barData[i] != 0){
					//データがあり、かつ選択されているバーのとき
					g.setColor(BAR_RED_COLOR);
				}else{
					g.setColor(BG_STR_COLOR);
				}//End of if()
				
				g.drawString((i+1)+"",x,glY + FONT_ASCENT);
			}//End of for()
			x = interval*barData.length + startX + (interval>>1);
			g.drawString((barData.length-1)+"",x,glY + FONT_ASCENT);
			
			//Y軸の高さ
			final int yAxisH = glY -bg_vertical_line_h;
			//正規化のための倍率
			double scaleY = (double)(yAxisH)/(double)barMax;
			
			//棒グラフを描画
			x = 0;
			y = 0;
			int x1 = 0;
			int y1 = 0;
			g.setColor(BAR_COLOR);
			for(int i = 0; i < barData.length ; i++){
				//データがないときは描画しない
				if(barData[i] == 0)continue;
				
				x  = interval*i + startX + 1;
				double size = (barData[i]*scaleY);
				if(size < 3){
					//あまりにも小さい時
					size = 3;
				}
				y  = (int)(glY - size);			
				x1 = interval - 1;			
				y1 = glY - y -1;
				
				//------------棒の描画------------//
				if(i == selectedIndex){
					//選択にされているとき
					g.setColor(BAR_RED_COLOR);	
				}//End of if()
				
				//棒の描画
				g.clearRect(x, y, x1 ,y1);
				
				if(i == selectedIndex){
					//選択にされているとき(色を戻す)
					g.setColor(BAR_COLOR);	
				}//End of if()
				//------------棒の描画------------//						
				
			}//End of for()
			
			//Y軸のメモリを描画
			int division = 5;
			//軸の間隔
			interval = yAxisH/division;			
			
			//目盛りを描画
			for(int i = 0; i <= division ; i++){
				//目盛り線
				x = startX - 1;
				y =  glY - interval*i;			
				if(i != 0){							
					g.setColor(BG_LINE_COLOR);
					g.drawLine(x-2,y,x+2,y);	
				}//End of if()			
				
				//目盛り
				x = (startX - 1)>>1;
				y += (FONT_ASCENT>>1);
				g.setColor(BG_STR_COLOR);			
				double scale = (double)i/(double)division;				
				//小数点数１桁まで表示。桁数によって調整
				g.drawString("" + (double)((int)((barMax*scale)/den))/10,x,y);			
			}//End of for()
			
			//x 10^a を描画　 aは最大値の桁数によって決定する
			y = bg_vertical_line_h - FONT_HEIGHT;			
			String str = "x 10^"+(sig-1);
			x = xMargin + (FONT.stringWidth(str)>>1);
			g.drawString(str, x, y);
			
			//選択されているバーの横に数値を描画
			double size = (barData[selectedIndex]*scaleY);
			if(size < 3){
				//あまりにも小さい時
				size = 3;
			}
			interval = ((width-xMargin)-startX)/barData.length;
			str = (selectedIndex+1)+"日:"+barData[selectedIndex]+"個";		
			if(selectedIndex > (barData.length>>1)){
				//画面右側の棒のとき,数値は左に表示		
				x  = (interval*selectedIndex) + startX + 1 - (interval>>1)
				- (FONT.stringWidth(str)>>1);
			}else{
				//画面左の棒のとき,数値は右に表示	
				x  = (interval*selectedIndex) + startX + 1 +  interval + (interval>>1)
				+ (FONT.stringWidth(str)>>1);
			}
			y  = (int)(glY - size) + FONT_HEIGHT;
			g.setColor(BAR_RED_COLOR);
			g.drawString(str,x, y);
		}
		
		//上部のラベルを描画
		y = bg_vertical_line_h - FONT_HEIGHT;
		x = (width<<1)/3;
		g.setColor(BG_STR_COLOR);
		g.drawString(dateLabel, x, y);
		
	}//End of drawBGLine()

	/**
	 * 折れ線グラフの描画
	 * @param g
	 */
	private void drawPolygonalGraph(DojaGraphics g){
		g.setColor(BG_STR_COLOR);
		
		//描画感覚
		int interval = ((width-xMargin)-startX)/polygonalData.length;
		//Y軸の高さ
		final int yAxisH = ground_y -bg_vertical_line_h;
		//正規化のための倍率
		double scaleY = (double)(yAxisH)/(double)polygonalMax;

		for(int i = 0; i < polygonalData.length ; i++){
			if(polygonalData[i] != -1){
				//データがあるとき
				
				//･を打つ
				final int pointSize = 4;
				int x = i*interval + (interval>>1) + startX - (pointSize>>1);
				//グラフの高さの半分を最大時の大きさにする
				int y = (((int)(scaleY*polygonalData[i])) >>1) + (pointSize>>1);				 
				g.clearRect(x, ground_y - y, pointSize, pointSize);

								
				if(i == selectedIndex){
					//選択されているとき、横に数値を表示
					String str = "Ave:" + polygonalData[i];
						
					x = x+ (pointSize>>1);
					y += FONT_ASCENT>>1;
					if(x + (FONT.stringWidth(str)>>1) > width){
						x -= (FONT.stringWidth(str)>>1);
					}else if (x - (FONT.stringWidth(str)>>1) < startX ){
						x += (FONT.stringWidth(str)>>1);
					}
					g.drawString(str, x, ground_y - y);									
				}//End of if(i == selected)
				
				//前回の点と線を結ぶ
				for(int j = i - 1 ; j >= 0 ; j --){
					if(polygonalData[j] != -1){
						x = i*interval + (interval>>1) + startX;
						y = ((int)(scaleY*polygonalData[i])) >>1;
						
						int x1 = j*interval + (interval>>1) + startX;
						int y1 = ((int)(scaleY*polygonalData[j])) >>1;
						
						g.drawLine(x, ground_y - y, x1, ground_y - y1);						
						
						break;
					}//End of if(polygonalData[j] != -1)
				}//End of for()
				
			}//End of if(polygonalData[i] != -1)			
		}//End of for()		
		
	}//End of drawPolygonalGraph()
	
	//======================ReadDBListener======================//
	/**
	 * DBからの読み込みが成功したときの呼ばれる 
	 */
	public void onCompleteReadDB(int[] data,int type,long time) {
		System.out.println("#onCompleteReadDB@staticWave");
		
		if(type == ReadDBData.TYPE_MONTH_BP_AVE || 
			     type == ReadDBData.TYPE_MONTH_HR_AVE ){
			//折れ線グラフ用データ
			polygonalFlag = true;
			// データをコピー
			System.out.println("折れ線データ" );
			polygonalData = new int[data.length];
			for (int i = 0; i < data.length; i++) {
				polygonalData[i] = data[i];
				System.out.println(i + " : " + polygonalData[i]);
			}
			
			// 折れ線グラフ用データ
			// の最大値を求める
			polygonalMax = polygonalData[0];
			for (int i = 0; i < polygonalData.length; i++) {
				if (polygonalMax < polygonalData[i]) {
					// 最大値
					polygonalMax = polygonalData[i];
				}
			}// End of for()
			
		}else {
			//棒グラフ用データのとき
			
			// データをコピー
			barData = new int[data.length];
			for (int i = 0; i < data.length; i++) {
				barData[i] = data[i];
			}

			// 最初の選択バーを決める
			selectedIndex = -1;
			for (int i = 0; i < barData.length; i++) {
				if (barData[i] != 0) {
					selectedIndex = i;
					break;
				}
			}// End of for()

			// ラベルを更新
			dateArray = CalendarUtil.calcTimeInfo(time);

			String typeStr = "";
			switch (type) {
			case ReadNumOfDBData.TYPE_MONTH_ECG_NUM:
				typeStr = "心電図";
				break;
			case ReadNumOfDBData.TYPE_MONTH_PLS_NUM:
				typeStr = "脈波";
				break;			
			case ReadNumOfDBData.TYPE_MONTH_BP_NUM:
				typeStr = "血圧";
				break;
			case ReadNumOfDBData.TYPE_MONTH_HR_NUM:
				typeStr = "心拍数";
				break;
			}// End of switch

			dateLabel = typeStr + " " + dateArray[1] + "月 ";

			if (selectedIndex < 0) {
				// データがない時はここでおしまい
				return;
			}

			// 棒グラフの描画
			// 最大値を求める
			barMax = barData[0];
			for (int i = 0; i < barData.length; i++) {
				if (barMax < barData[i]) {
					// 最大値
					barMax = barData[i];
				}
			}// End of for()

			// 最大の桁数
			sig = String.valueOf(barMax).length();
			den = 1;
			for (int i = 0; i < sig - 2; i++) {
				den *= 10;
			}		
		} //End of if(type = ...)
		 					
	}/* End of onCompleteReadDB() */
	
	/**
	 * DBからの読み込みが失敗したときの呼ばれる
	 */
	public void onErrorReadDB() {
		System.out.println("#onErrorReadDB@waitScreen");
	}/* End of onErrorReadDB */
	
	/**
	 * 選択されている棒グラフのデータのデータ長を返す
	 * @return
	 */
	public int getSelectedDataLen(){
		if(selectedIndex < 0 || selectedIndex >= barData.length){
			return 0;
		}
		return this.barData[selectedIndex];
	}

}
