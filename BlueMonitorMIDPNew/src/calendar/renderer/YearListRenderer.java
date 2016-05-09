package calendar.renderer;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import calendar.util.CalendarColor;
import calendar.util.CalendarUtil;


import util.DojaFont;
import util.DojaGraphics;

public class YearListRenderer {
	//フォント
	/** 大フォント */
	private static final DojaFont mFONT = DojaFont.getFont(DojaFont.SIZE_MEDIUM);
	
	/** アニメーションモード */
	private static final int MODE_STOP = 0;
	private static final int MODE_GO_LEFT = 1;
	private static final int MODE_GO_RIGHT = 2;
	
	/** 現在のアニメーションモード */
	private int mode = MODE_STOP;
	
	/** 現在描画中の年 */
	private int curYear = 0;
	
	/** 年数描画用イメージのサイズ */
	private int yearBigImgWidth = 0;
	private int yearBigImgHeight = 0;
	private int yearSmallImgWidth = 0;
	private int yearSmallImgHeight = 0;
	
	/** 非アニメーションの描画位置 */
	private int centerFixedX = 0;
	private int centerFixedY = 0;
	private int leftFixedX = 0;
	private int leftFixedY = 0;
	private int rightFixedX = 0;
	private int rightFixedY = 0;
	
	/** FPS */
	private int fps = 20;
	
	/** アニメーション途中の描画位置(中心が終点) */
	private int[] leftAnimationX = null;
	private int[] leftAnimationY = null;
	private int[] rightAnimationX = null;
	private int[] rightAnimationY = null;
	
	/** アニメーション途中の描画位置(画面外スタート、左右が終点) */
	private int[] leftOutAnimationX = null;
	private int[] leftOutAnimationY = null;
	private int[] rightOutAnimationX = null;
	private int[] rightOutAnimationY = null;
	
	/** アニメーション途中の拡大率(小さい方から) */
	private double[] animationScale = null;
	
	/** 中心と左右の描画位置の高低差 */
	private int widthMargin = 30;
	private int heightMargin = 70;

	/** アニメーション途中の各座標/拡大率のインデックス */
	private int centerAnimationIndex = 0;
	private int leftAnimationIndex = 0;
	private int rightAnimationIndex = 0;
	private int outAnimationIndex = 0;
	private int centerScaleIndex = 0;
	private int leftScaleIndex = 0;
	private int rightScaleIndex = 0;
	
	/**
	 * コンストラクタ
	 *
	 * @param canvas 描画するキャンバス
	 */
	public YearListRenderer(int width,int height){
		
		// 現在の時間を取得
		int[] info = CalendarUtil.calcTimeInfo(System.currentTimeMillis());
		curYear = info[0];		
		
		// 年数描画用イメージのサイズを求める
		String yearStr     = changeNumber2B(1000);
		yearBigImgWidth    = mFONT.stringWidth(yearStr);
		yearBigImgHeight   = mFONT.getAscent() + mFONT.getDescent();
		yearSmallImgWidth  = (yearBigImgWidth  << 1)/5;
		yearSmallImgHeight = (yearBigImgHeight << 1)/5;
		
        // 非アニメーション時の描画開始位置を求める
		centerFixedX = (width - yearBigImgWidth)   >> 1;
		centerFixedY = (height - yearBigImgHeight) >> 1;
		leftFixedX = centerFixedX - widthMargin - yearSmallImgWidth;
		leftFixedY = centerFixedY - heightMargin;
		rightFixedX = centerFixedX + yearBigImgWidth + widthMargin;
		rightFixedY = centerFixedY - heightMargin;
		
		// アニメーション途中の描画開始位置と拡大率
		int ux = widthMargin / fps;
		int uy = heightMargin / fps;
		double uix = (double)(yearBigImgWidth - yearSmallImgWidth)   / (double)((fps<<1)/3);
		double uiy = (double)(yearBigImgHeight - yearSmallImgHeight) / (double)((fps<<1)/3);
		
		leftAnimationX = new int[fps];
		leftAnimationY = new int[fps];
		rightAnimationX = new int[fps];
		rightAnimationY = new int[fps];
		leftOutAnimationX = new int[fps];
		leftOutAnimationY = new int[fps];
		rightOutAnimationX = new int[fps];
		rightOutAnimationY = new int[fps];
		animationScale = new double[fps];
		double[] tmpImgWidth = new double[fps];
		double[] tmpImgHeight = new double[fps];
		
		leftAnimationX[0] = leftFixedX;
		leftAnimationY[0] = leftFixedY;
		rightAnimationX[0] = rightFixedX;
		rightAnimationY[0] = rightFixedY;
		leftOutAnimationX[0] = leftFixedX - widthMargin;
		leftOutAnimationY[0] = leftFixedY - heightMargin;
		rightOutAnimationX[0] = rightFixedX + widthMargin;
		rightOutAnimationY[0] = rightFixedY - heightMargin;
		animationScale[0] = (double)yearSmallImgWidth / (double)yearBigImgWidth;
		tmpImgWidth[0] = yearSmallImgWidth;
		tmpImgHeight[0] = yearSmallImgHeight;
		
		for(int i=1; i < fps; i++){
		
			leftAnimationX[i] = leftAnimationX[0] + ux*i;
			leftAnimationY[i] = leftAnimationY[0] + uy*i;
			rightAnimationX[i] = rightAnimationX[0] - ux*i;
			rightAnimationY[i] = rightAnimationY[0] + uy*i;
			leftOutAnimationX[i] = leftOutAnimationX[0] + ux*i;
			leftOutAnimationY[i] = leftOutAnimationY[0] + uy*i;
			rightOutAnimationX[i] = rightOutAnimationX[0] - ux*i;
			rightOutAnimationY[i] = rightOutAnimationY[0] + uy*i;
			tmpImgWidth[i] = tmpImgWidth[0] + uix*i;
			tmpImgHeight[i] = tmpImgHeight[0] + uiy*i;
			animationScale[i] = (double)tmpImgWidth[i] / (double)yearBigImgWidth;
			
			if(leftAnimationX[i] >= centerFixedX){
				leftAnimationX[i] = centerFixedX;
			}
			if(leftAnimationY[i] >= centerFixedY){
				leftAnimationY[i] = centerFixedY;
			} 
			if(rightAnimationX[i] <= centerFixedX){
				rightAnimationX[i] = centerFixedX;
			}
			if(rightAnimationY[i] >= centerFixedY){
				rightAnimationY[i] = centerFixedY;
			}
			if(leftOutAnimationX[i] >= leftFixedX){
				leftOutAnimationX[i] = leftFixedX;
			}
			if(leftOutAnimationY[i] >= leftFixedY){
				leftOutAnimationY[i] = leftFixedY;
			} 
			if(rightOutAnimationX[i] <= rightFixedX){
				rightOutAnimationX[i] = rightFixedX;
			}
			if(rightOutAnimationY[i] >= rightFixedY){
				rightOutAnimationY[i] = rightFixedY;
			}
			if(animationScale[i] > 1){
				animationScale[i] = 1;
			}
			if(tmpImgWidth[i] != yearBigImgWidth){
				tmpImgWidth[i] = yearBigImgWidth;
			}
			
		}/* End of for()*/		
	}/* End of YearListRenderer() */
	
	
	/**
	 * 現在の年の月リストを描画
	 *
	 * @param g 描画対象のグラフィックスクラス
	 *
	 */
	public void draw(DojaGraphics g){	
		drawYear(g);
	}
	
	/**
	 * 左へ移動
	 */
	public void left(){		
		int[] info = CalendarUtil.calcTimeInfo(System.currentTimeMillis());
		
		if(curYear < info[0]){
			mode = MODE_GO_LEFT;
			AnimationThread thread = new AnimationThread();
			thread.start();
		}/* End of if() */	
		
	}/* End of left() */
	
	/**
	 * 右へ移動
	 */
	public void right(){		
		mode = MODE_GO_RIGHT;
		AnimationThread thread = new AnimationThread();
		thread.start();			
	}/* End of right() */
	
	/**
	 * アニメーション中であるかどうか調べる
	 *
	 * @return アニメーション中であるかどうか	 
	 */
	public boolean isAnimation(){
		return mode != MODE_STOP;
	}/* End of isAnimation() */			
	
	/**
	 * 月を描画
	 *
	 * @param g 描画対象のグラフィックスクラス	 
	 */
	private void drawYear(DojaGraphics g){
		
		// 描画する文字列
		String centerYear = changeNumber2B(curYear);
		String leftYear = changeNumber2B(curYear-1);
		String rightYear = changeNumber2B(curYear+1);
		
		//フォント 
		final Font font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_LARGE);
		
		if(mode == MODE_STOP){	// アニメーション中でない場合
						
			// 固定サイズのイメージを作成
			Image centerImg = Image.createImage(yearBigImgWidth+2, yearBigImgHeight+2);
			Graphics cg = centerImg.getGraphics();
			cg.setFont(font);
			cg.setColor(CalendarColor.COLOR_GRAY);
			cg.drawString(centerYear, 3, 3, Graphics.TOP|Graphics.LEFT);
			cg.drawString(centerYear, 2, 3, Graphics.TOP|Graphics.LEFT);
			cg.drawString(centerYear, 3, 2, Graphics.TOP|Graphics.LEFT);
			cg.drawString(centerYear, 2, 2, Graphics.TOP|Graphics.LEFT);
			cg.setColor(CalendarColor.COLOR_LIGHT_GRAY);
			cg.drawString(centerYear, 1, 2, Graphics.TOP|Graphics.LEFT);
			cg.drawString(centerYear, 2, 1, Graphics.TOP|Graphics.LEFT);
			cg.drawString(centerYear, 1, 1, Graphics.TOP|Graphics.LEFT);
			cg.drawString(centerYear, 0, 1, Graphics.TOP|Graphics.LEFT);
			cg.drawString(centerYear, 1, 0, Graphics.TOP|Graphics.LEFT);
			cg.drawString(centerYear, 0, 0, Graphics.TOP|Graphics.LEFT);
			
			double scale = (double)(yearSmallImgWidth) / (double)(yearBigImgWidth);
			
			Image bigLeftImg = Image.createImage(yearBigImgWidth+2, yearBigImgHeight+2);
			Graphics blg = bigLeftImg.getGraphics();
			blg.setFont(font);
			blg.setColor(CalendarColor.COLOR_GRAY);
			blg.drawString(leftYear, 3, 3, Graphics.TOP|Graphics.LEFT);
			blg.drawString(leftYear, 2, 3, Graphics.TOP|Graphics.LEFT);
			blg.drawString(leftYear, 3, 2, Graphics.TOP|Graphics.LEFT);
			blg.drawString(leftYear, 2, 2, Graphics.TOP|Graphics.LEFT);
			blg.setColor(CalendarColor.COLOR_LIGHT_GRAY);
			blg.drawString(leftYear, 1, 2, Graphics.TOP|Graphics.LEFT);
			blg.drawString(leftYear, 2, 1, Graphics.TOP|Graphics.LEFT);
			blg.drawString(leftYear, 1, 1, Graphics.TOP|Graphics.LEFT);
			blg.drawString(leftYear, 0, 1, Graphics.TOP|Graphics.LEFT);
			blg.drawString(leftYear, 1, 0, Graphics.TOP|Graphics.LEFT);
			blg.drawString(leftYear, 0, 0, Graphics.TOP|Graphics.LEFT);
			Image leftImg = scaleDownImage(bigLeftImg, scale);
			
			Image bigRightImg = Image.createImage(yearBigImgWidth+2, yearBigImgHeight+2);
			Graphics brg = bigRightImg.getGraphics();
			brg.setFont(font);
			brg.setColor(CalendarColor.COLOR_GRAY);
			brg.drawString(rightYear, 3, 3, Graphics.TOP|Graphics.LEFT);
			brg.drawString(rightYear, 2, 3, Graphics.TOP|Graphics.LEFT);
			brg.drawString(rightYear, 3, 2, Graphics.TOP|Graphics.LEFT);
			brg.drawString(rightYear, 2, 2, Graphics.TOP|Graphics.LEFT);
			brg.setColor(CalendarColor.COLOR_LIGHT_GRAY);
			brg.drawString(rightYear, 1, 2, Graphics.TOP|Graphics.LEFT);
			brg.drawString(rightYear, 2, 1, Graphics.TOP|Graphics.LEFT);
			brg.drawString(rightYear, 1, 1, Graphics.TOP|Graphics.LEFT);
			brg.drawString(rightYear, 0, 1, Graphics.TOP|Graphics.LEFT);
			brg.drawString(rightYear, 1, 0, Graphics.TOP|Graphics.LEFT);
			brg.drawString(rightYear, 0, 0, Graphics.TOP|Graphics.LEFT);
			Image rightImg = scaleDownImage(bigRightImg, scale);
			
			// イメージの描画
			g.drawImage(centerImg, centerFixedX, centerFixedY);
			g.drawImage(leftImg  , leftFixedX  , leftFixedY  );
			g.drawImage(rightImg , rightFixedX , rightFixedY );
			
		} else{	// アニメーション中の場合
			
			// 拡大率に応じたイメージを作成
			Image bigCenterImg = Image.createImage(yearBigImgWidth+2, yearBigImgHeight+2);
			Graphics bcg = bigCenterImg.getGraphics();
			bcg.setFont(font);
			bcg.setColor(CalendarColor.COLOR_GRAY);
			bcg.drawString(centerYear, 3, 3, Graphics.TOP|Graphics.LEFT);
			bcg.drawString(centerYear, 2, 3, Graphics.TOP|Graphics.LEFT);
			bcg.drawString(centerYear, 3, 2, Graphics.TOP|Graphics.LEFT);
			bcg.drawString(centerYear, 2, 2, Graphics.TOP|Graphics.LEFT);
			bcg.setColor(CalendarColor.COLOR_LIGHT_GRAY);
			bcg.drawString(centerYear, 1, 2, Graphics.TOP|Graphics.LEFT);
			bcg.drawString(centerYear, 2, 1, Graphics.TOP|Graphics.LEFT);
			bcg.drawString(centerYear, 1, 1, Graphics.TOP|Graphics.LEFT);
			bcg.drawString(centerYear, 0, 1, Graphics.TOP|Graphics.LEFT);
			bcg.drawString(centerYear, 1, 0, Graphics.TOP|Graphics.LEFT);
			bcg.drawString(centerYear, 0, 0, Graphics.TOP|Graphics.LEFT);
			Image centerImg = scaleDownImage(bigCenterImg, animationScale[centerScaleIndex]);
			
			Image bigLeftImg = Image.createImage(yearBigImgWidth+2, yearBigImgHeight+2);
			Graphics blg = bigLeftImg.getGraphics();
			blg.setFont(font);
			blg.setColor(CalendarColor.COLOR_GRAY);
			blg.drawString(leftYear, 3, 3, Graphics.TOP|Graphics.LEFT);
			blg.drawString(leftYear, 2, 3, Graphics.TOP|Graphics.LEFT);
			blg.drawString(leftYear, 3, 2, Graphics.TOP|Graphics.LEFT);
			blg.drawString(leftYear, 2, 2, Graphics.TOP|Graphics.LEFT);
			blg.setColor(CalendarColor.COLOR_LIGHT_GRAY);
			blg.drawString(leftYear, 1, 2, Graphics.TOP|Graphics.LEFT);
			blg.drawString(leftYear, 2, 1, Graphics.TOP|Graphics.LEFT);
			blg.drawString(leftYear, 1, 1, Graphics.TOP|Graphics.LEFT);
			blg.drawString(leftYear, 0, 1, Graphics.TOP|Graphics.LEFT);
			blg.drawString(leftYear, 1, 0, Graphics.TOP|Graphics.LEFT);
			blg.drawString(leftYear, 0, 0, Graphics.TOP|Graphics.LEFT);
			Image leftImg = scaleDownImage(bigLeftImg, animationScale[leftScaleIndex]);
			
			Image bigRightImg = Image.createImage(yearBigImgWidth+2, yearBigImgHeight+2);
			Graphics brg = bigRightImg.getGraphics();
			brg.setFont(font);
			brg.setColor(CalendarColor.COLOR_GRAY);
			brg.drawString(rightYear, 3, 3, Graphics.TOP|Graphics.LEFT);
			brg.drawString(rightYear, 2, 3, Graphics.TOP|Graphics.LEFT);
			brg.drawString(rightYear, 3, 2, Graphics.TOP|Graphics.LEFT);
			brg.drawString(rightYear, 2, 2, Graphics.TOP|Graphics.LEFT);
			brg.setColor(CalendarColor.COLOR_LIGHT_GRAY);
			brg.drawString(rightYear, 1, 2, Graphics.TOP|Graphics.LEFT);
			brg.drawString(rightYear, 2, 1, Graphics.TOP|Graphics.LEFT);
			brg.drawString(rightYear, 1, 1, Graphics.TOP|Graphics.LEFT);
			brg.drawString(rightYear, 0, 1, Graphics.TOP|Graphics.LEFT);
			brg.drawString(rightYear, 1, 0, Graphics.TOP|Graphics.LEFT);
			brg.drawString(rightYear, 0, 0, Graphics.TOP|Graphics.LEFT);
			Image rightImg = scaleDownImage(bigRightImg, animationScale[rightScaleIndex]);
			
			if(mode == MODE_GO_LEFT){
				
				// 画面外から現れる年のイメージを作成
				double scale = (double)(yearSmallImgWidth) / (double)(yearBigImgWidth);
				Image outBigImg = Image.createImage(yearBigImgWidth+2, yearBigImgHeight+2);
				Graphics og = outBigImg.getGraphics();
				og.setFont(font);
				og.setColor(CalendarColor.COLOR_GRAY);
				og.drawString(centerYear, 3, 3, Graphics.TOP|Graphics.LEFT);
				og.drawString(centerYear, 2, 3, Graphics.TOP|Graphics.LEFT);
				og.drawString(centerYear, 3, 2, Graphics.TOP|Graphics.LEFT);
				og.drawString(centerYear, 2, 2, Graphics.TOP|Graphics.LEFT);
				og.setColor(CalendarColor.COLOR_LIGHT_GRAY);
				og.drawString(centerYear, 1, 2, Graphics.TOP|Graphics.LEFT);
				og.drawString(centerYear, 2, 1, Graphics.TOP|Graphics.LEFT);
				og.drawString(centerYear, 1, 1, Graphics.TOP|Graphics.LEFT);
				og.drawString(centerYear, 0, 1, Graphics.TOP|Graphics.LEFT);
				og.drawString(centerYear, 1, 0, Graphics.TOP|Graphics.LEFT);
				og.drawString(centerYear, 0, 0, Graphics.TOP|Graphics.LEFT);
				Image outImg = scaleDownImage(outBigImg, scale);
				
				// イメージの描画
				g.drawImage(centerImg ,leftAnimationX[centerAnimationIndex] ,leftAnimationY[centerAnimationIndex] );
				g.drawImage(leftImg   ,leftOutAnimationX[leftAnimationIndex],leftOutAnimationY[leftAnimationIndex]);
				g.drawImage(rightImg  ,rightAnimationX[rightAnimationIndex] ,rightAnimationY[rightAnimationIndex] );
				g.drawImage(outImg    ,rightOutAnimationX[outAnimationIndex],rightOutAnimationY[outAnimationIndex]);
				
			} else if(mode == MODE_GO_RIGHT){
				
				// 画面外から現れる年のイメージを作成
				double scale = (double)(yearSmallImgWidth) / (double)(yearBigImgWidth);
				Image outBigImg = Image.createImage(yearBigImgWidth+2, yearBigImgHeight+2);
				Graphics og = outBigImg.getGraphics();
				og.setFont(font);
				og.setColor(CalendarColor.COLOR_GRAY);
				og.drawString(centerYear, 3, 3, Graphics.TOP|Graphics.LEFT);
				og.drawString(centerYear, 2, 3, Graphics.TOP|Graphics.LEFT);
				og.drawString(centerYear, 3, 2, Graphics.TOP|Graphics.LEFT);
				og.drawString(centerYear, 2, 2, Graphics.TOP|Graphics.LEFT);
				og.setColor(CalendarColor.COLOR_LIGHT_GRAY);
				og.drawString(centerYear, 1, 2, Graphics.TOP|Graphics.LEFT);
				og.drawString(centerYear, 2, 1, Graphics.TOP|Graphics.LEFT);
				og.drawString(centerYear, 1, 1, Graphics.TOP|Graphics.LEFT);
				og.drawString(centerYear, 0, 1, Graphics.TOP|Graphics.LEFT);
				og.drawString(centerYear, 1, 0, Graphics.TOP|Graphics.LEFT);
				og.drawString(centerYear, 0, 0, Graphics.TOP|Graphics.LEFT);
				Image outImg = scaleDownImage(outBigImg, scale);
				
				// イメージの描画
				g.drawImage(centerImg,rightAnimationX[centerAnimationIndex]  ,rightAnimationY[centerAnimationIndex]  );
				g.drawImage(leftImg  ,leftAnimationX[leftAnimationIndex]     ,leftAnimationY[leftAnimationIndex]     );
				g.drawImage(rightImg ,rightOutAnimationX[rightAnimationIndex],rightOutAnimationY[rightAnimationIndex]);
				g.drawImage(outImg   ,leftOutAnimationX[outAnimationIndex]   ,leftOutAnimationY[outAnimationIndex]   );
				
			}/* End of if() */			
		}/* End of if() */				
	}/* End of drawYear() */

	/**
	 * 数値を全角数字の文字列に変換する
	 *
	 * @param  num 変換する数値
	 * @return     変換された文字列	 
	 */
	private String changeNumber2B(int num){
	
		// 全角数字
		final String[] numbers = {"０", "１", "２", "３", "４", "５", "６", "７", "８", "９"};
		
		// 桁数を求める
		int standard = 10;
		int figure = 1;
		while(true){
			int amari = num % standard;
			if(amari == num) break;
			standard *= 10;
			figure++;
		}
		
		// 数値を分解
		int[] sepNum = new int[figure];
		int restNum = num;
		int divNum = pow(10, (figure-1));
		for(int i=0; i < figure-1; i++){
			sepNum[i] = restNum / divNum;
			restNum = restNum % divNum;
			divNum /= 10;
		}
		sepNum[sepNum.length-1] = restNum;
		
		// 文字列に変換
		String result = "";
		for(int i=0; i < sepNum.length; i++){
			result += numbers[sepNum[i]];
		}
		
		return result;		
		
	}/* End of changeNumber2B() */
	
	/**
	 * べき乗を求める
	 *
	 * @param  num べき乗する数値
	 * @param  x   乗数
	 * @return     べき乗された数値
	 */
	private int pow(int num, int x){		
		int result = 1;
		for(int i=0; i < x; i++){
			result *= num;
		}		
		return result;
		
	}/* End of pow() */
	
	/**
	 * 対象のイメージを縮小する
	 *
	 * @param  source 対象のイメージ
	 * @param  scale  縮小率
	 * @return        縮小されたイメージ	 
	 */
	private Image scaleDownImage(Image source, double scale){
		
		// 縮小率が1以上の場合処理を中止
		if(scale > 1){
			return null;
		} else if(scale == 1){
			return source;
		}
		
		// イメージのサイズを取得
		int imgWidth = source.getWidth();
		int imgHeight = source.getHeight();
		
		// 縮小後のイメージのサイズを求める
		int scaledWidth = (int)((double)imgWidth * scale);
		int scaledHeight = (int)((double)imgHeight * scale);
		
		// 縮小前の何Pixelを縮小後の1Pixelとするか求める
		int wpix = 0;
		int hpix = 0;
		double wpixDouble = 1 / scale;
		double hpixDouble = 1 / scale;
		int wpixInt = (int)(1 / scale);
		int hpixInt = (int)(1 / scale);
		
		if((wpixDouble-wpixInt) >= 0.2){
			wpix = wpixInt + 1;
		} else{
			wpix = wpixInt;
		}
		
		if((hpixDouble-hpixInt) >= 0.2){
			hpix = hpixInt + 1;
		} else{
			hpix = hpixInt;
		}
		
		if(wpix <= 1 || hpix <= 1) return source;
		
		// イメージをRGB情報に変換
		int[] pRGB = new int[imgWidth*imgHeight];
		source.getRGB(pRGB, 0, imgWidth, 0, 0, imgWidth, imgHeight);
		
		// 縮小後のRGB情報
		int[] tRGB = new int[scaledWidth*scaledHeight];
		
		// 縮小を行う
		boolean breakFlag = false;
		for(int x=0; x < scaledWidth; x++){
			for(int y=0; y < scaledHeight; y++){
				int sumR = 0;
				int sumG = 0;
				int sumB = 0;
				int count = 0;
				for(int i=wpix*x; i < wpix*(x+1); i++){
					for(int j=hpix*y; j < hpix*(y+1); j++){
						int pi = i;
						int pj = j;
						
						if(i >= imgWidth && j >= imgHeight){
							breakFlag = true;
							break;
						} else if(i >= imgWidth){
							pi = imgWidth - 1;
						} else if(j >= imgHeight){
							pj = imgHeight - 1;
						}
						
						int pixel = pRGB[pi+pj*imgWidth];
						sumR += (pixel & 0xFF0000) >> 16;
						sumG += (pixel & 0x00FF00) >> 8;
						sumB += (pixel & 0x0000FF);
						count++;
						
					}/*End of for() */
					if(breakFlag) break;					
				}/* End of for() */
				
				if(count > 0){
					int aveR = (int)(sumR / count);
					int aveG = (int)(sumG / count);
					int aveB = (int)(sumB / count);
					tRGB[x+y*scaledWidth] = ((aveR & 0xFF) << 16) | ((aveG & 0xFF) << 8) | (aveB & 0xFF);
				} else{
					tRGB[x+y*scaledWidth] = CalendarColor.COLOR_WHITE;
				}/* End of if() */
				
			}/* End of for() */
		}/* End of for() */
		
		// RGB情報をイメージに変換
		Image translateImage = Image.createRGBImage(tRGB, scaledWidth, scaledHeight, false);		
		return translateImage;
		
	}/* End of scaleDownImage() */
	
	/**
	 * アニメーション時の再描画用スレッド
	 */
	class AnimationThread extends Thread{
		
		public void run(){			
			try{				
				// 各インデックスの初期値を決定する				
				if(mode == MODE_GO_LEFT){				
					centerAnimationIndex = fps - 1;
					leftAnimationIndex = fps - 1;
					rightAnimationIndex = 0;
					outAnimationIndex = 0;
					centerScaleIndex = fps - 1;
					leftScaleIndex = 0;
					rightScaleIndex = 0;					
				} else if(mode == MODE_GO_RIGHT){					
					centerAnimationIndex = fps - 1;
					leftAnimationIndex = 0;
					rightAnimationIndex = fps - 1;
					outAnimationIndex = 0;
					centerScaleIndex = fps - 1;
					leftScaleIndex = 0;
					rightScaleIndex = 0;
					
				}/* End of if ()*/
				
				// 描画間隔(msec)を求める
				long interval = 500 / fps;
				
				for(int i=0; i < fps-1; i++){				
					// 描画開始時間を取得
					long start = System.currentTimeMillis();					
					
					// インデックスを変更
					if(mode == MODE_GO_LEFT){						
						centerAnimationIndex--;
						leftAnimationIndex--;
						rightAnimationIndex++;
						outAnimationIndex++;
						centerScaleIndex--;
						rightScaleIndex++;						
					} else if(mode == MODE_GO_RIGHT){						
						centerAnimationIndex--;
						leftAnimationIndex++;
						rightAnimationIndex--;
						outAnimationIndex++;
						centerScaleIndex--;
						leftScaleIndex++;						
					}/* End of if() */
					
					// 描画終了時間を取得
					long end = System.currentTimeMillis();
					
					// FPSが一定になるように待機する
					long period = end - start;
					if(period < interval){
						Thread.sleep(interval - period);
					}
				}/* End of for() */
				
				// 年の変更
				if(mode == MODE_GO_LEFT){
					curYear++;
				} else if(mode == MODE_GO_RIGHT){
					curYear--;
				}/* End of if() */
				
				mode = MODE_STOP;			
			
			} catch(InterruptedException e){
				e.printStackTrace();				
			}/* End of try catch() */
			
		}/* End of run() */		
	}/* End of AnimationThread() */
}
