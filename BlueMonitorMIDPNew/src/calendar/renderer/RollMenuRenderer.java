package calendar.renderer;

import java.io.IOException;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import request.Request;
import request.RequestQueue;

import util.DojaGraphics;

import main.Main;


public class RollMenuRenderer{
	
	//TODO: active() getIndex() left() inactive() right() 
	
	//================================定数====================================//
	
	//色
	private static final int COLOR_BLACK = 0x000000;
	private static final int COLOR_LIGHT_RED = 0xFF6666;

	//アイコン画像
	/** 月アイコン */
	private static final String ICON_1 = (Main.IS_ECLIPSE)?("/res/monthicon.gif"):("/monthicon.gif");
	/** 年アイコン */
	private static final String ICON_2 = (Main.IS_ECLIPSE)?("/res/yearicon.gif"):("/yearicon.gif");
	/** 日アイコン */
	private static final String ICON_3 = (Main.IS_ECLIPSE)?("/res/dayicon.gif"):("/dayicon.gif");
	/** 週アイコン */
	private static final String ICON_4 = (Main.IS_ECLIPSE)?("/res/weekicon.gif"):("/weekicon.gif");	
	/** セレクタ */
	private static final String ICON_SELECTOR = (Main.IS_ECLIPSE)?("/res/iconselector.gif"):("/iconselector.gif");

	//日の定数
	/** 日 */
	public static final int DAY = 0;
	/** 週 */
	public static final int WEEK = 1;
	/** 月 */
	public static final int MONTH = 2;
	/** 三か月 */
	public static final int YEAR = 3;
	
	//状態,動作
	/** アクティブ */
	public static final int ACTIVE = 0;
	/** 非アクティブ */
	public static final int INACTIVE = 1;
	/** 左へ */
	public static final int LEFT = 2;
	/** 右へ */
	public static final int RIGHT = 3;		

	//================================定数====================================//
	/** 幅 */
	private int width = 0;
	/** 高さ */
	private int height = 0;

	/** アイコン画像を保持する配列 */
	private Image[] icon = null;
	private Image iconMap = null;
	
	private int margin = 5;

	/** メニューのインデックス */
	private int index = 0;

	/** 行位置*/
	private int rowPosition = 1000;
	/** 列位置*/
	private int colPosition = 0;
	/** フォーカスがあるかどうか */
	private boolean focus = false;
	
	/** セレクタの画像*/
	private Image selectorImg;
	
	/** アニメーションが実行中かどうかのフラグ */
	private boolean animationFlag = false;
	/** アクションを保持するキュー */
	private RequestQueue queue;
	
	private int state = INACTIVE;

	//================================関数====================================//
	/**
	 * コンストラクタ
	 * @param width 幅
	 * @param height　高さ
	 */
	public RollMenuRenderer(int width,int height){
		this.width = width;
		this.height = height;

		//画像の読み込み
		loadImages();
		createIconMap();
		
		queue = new RequestQueue(); 
	}

	/**
	 * メニューがアクティブかどうか
	 * @return
	 */
	public boolean isActive(){
		if(state == ACTIVE){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * 描画処理
	 * @param g Graphicsオブジェクト
	 */
	public void draw(DojaGraphics g){
		//アイコンの描画
		drawIcon(g);
		//セレクタの描画
		drawSelector(g);
	}

	/**
	 * 画像の読み込み
	 */
	private void loadImages(){
		try{
			icon = new Image[4];
			icon[0] = Image.createImage(ICON_1);
			icon[1] = Image.createImage(ICON_2);
			icon[2] = Image.createImage(ICON_3);
			icon[3] = Image.createImage(ICON_4);
			selectorImg = Image.createImage(ICON_SELECTOR);
		} catch(IOException e){
			e.printStackTrace();
		}
	}
	
	/**
	 * インデックスを得る
	 * @return
	 */
	public int getIndex() {
		return index;
	}
	
	/**
	 * インデックスを変更する
	 * @param index 
	 */
	public void setIndex(int index){
		this.index = index;
	}
	
	/**
	 * アイコンのムーブ
	 * @param action
	 */
	public void action(int action){
		if(animationFlag){
			return;
		}
		animationFlag = true;
		if(action == LEFT){
			index = (index + 1) % icon.length;
		}else if(action == RIGHT){
			index = (index + icon.length - 1) % icon.length;
		}	
		new RendererThread(action).start();
	}

	/**
	 * アクティブ(表示)状態へ
	 */
	private void active() {
		colPosition += 50;
		if (colPosition >= 1000){
			colPosition = 1000;						
		}
	}
	
	/**
	 * 非アクティブ(非表示)状態へ
	 */
	private void inactive() {
		colPosition -= 50;
		if (colPosition <= 0){
			colPosition = 0;						
		}
	}

	/**
	 * 左へ
	 */
	public void left() {
		rowPosition += 50;
		if (rowPosition >= icon.length * 1000){
			rowPosition = 0;
		}		
	}

	/**
	 * 右へ
	 */
	public void right() {
		rowPosition -= 50;
		if (rowPosition < 0){
			rowPosition = icon.length * 1000 - 50;
			System.out.println("test");
		}
	}

	/**
	 * アイコンをここで生成する画像の上に表示する その画像を生成
	 */
	private void createIconMap() {
		// アイコンの幅
		int mw = icon[0].getWidth();
		// アイコンの高さ
		int mh = icon[0].getHeight();

		// 生成する画像の高さと幅
		int imWidth = mw * icon.length + margin * 3;
		int imHeight = mh;

		// 画像の生成処理
		Image tmpImg = Image.createImage(imWidth, imHeight);
		Graphics tg = tmpImg.getGraphics();

		tg.setColor(COLOR_BLACK);
		tg.fillRect(0, 0, imWidth, imHeight);

		for (int i = 0; i < icon.length; i++) {
			tg.drawImage(icon[i], i * (mw + margin), 0, Graphics.TOP|Graphics.LEFT);
		}

		int[] rgb = new int[imWidth * imHeight];
		tmpImg.getRGB(rgb, 0, imWidth, 0, 0, imWidth, imHeight);
		int ndColor = rgb[mw + margin / 2];
		for (int i = 0; i < rgb.length; i++) {
			if (rgb[i] == ndColor)
				rgb[i] = (rgb[i] & 0xFFFFFF) | (0x00 << 24);
		}
		iconMap = Image.createRGBImage(rgb, imWidth, imHeight, true);
	}

	/**
	 * アイコンの描画
	 * @param g Graphicsオブジェクト
	 */
	private void drawIcon(DojaGraphics g) {
		//アイコンの幅
		int mw = icon[0].getWidth();
		//アイコンの幅
		int mh = icon[0].getHeight();

		//x座標(真ん中からアイコンの幅の半分+余白を引いた値)
		int x = (int) ((width>>1) - (mw * 1.5 + margin));
		//y座標
		int y = height;

		double rowPos = (double) (rowPosition) / 1000;
		double colPos = (double) (colPosition) / 1000;

		//x,高さからアイコンの高さ+余白を引いた値,アイコン3つの幅と左右の余白分、アイコンの高さ+余白
		g.setClip(x,y-mh-margin,(mw*3+margin*2),mh+margin);
		g.drawImage(iconMap, x-(int)(rowPos*(mw+margin)), y-(int)(colPos*(mh+margin)));
		if ((rowPos + 3) > icon.length) {
			g.drawImage(iconMap, x - (int) (rowPos * (mw + margin))
					+ iconMap.getWidth() + margin,
					y - (int) (colPos * (mh + margin)));
		}
		g.setClip(0, 0, width, height);
	}

	/**
	 * セレクタを描画
	 * @param g Graphicsオブジェクト
	 */
	private void drawSelector(DojaGraphics g) {

		if (!focus)return;

		int mw = icon[0].getWidth();
		int mh = icon[0].getHeight();

		double colPos = (double) (colPosition) / 1000;

		g.setColor(COLOR_LIGHT_RED);
		g.drawImage(selectorImg,((width-mw)>>1),height-(int)(colPos*(mh+margin)));
	}
	
	/**
	 * フォーカスの変更
	 * @param focus true:フォーカスがある状態,false:フォーカスがない状態
	 */
	public void setFocus(boolean focus) {
		this.focus = focus;
	}
	
	/**
	 * アイコンを動かすスレッド 
	 *
	 */
	public class RendererThread extends Thread {
		
		/** 動作 */		
		private int action = -1;
		/** ループのカウンタ */
		private int cnter = 0;
		/** 最大ループ回数 */
		private static final int MAX_LOOP = 20;
		
		/**
		 * コンストラクタ
		 * @param 動作
		 */
		public RendererThread(int action) {
			this.action = action;
		}
		
		public void run() {
			long start = System.currentTimeMillis();
			
			while(cnter < MAX_LOOP){
								
				//アイコンを動かす
				switch (action) {
				case ACTIVE:
					active();
					break;
				case INACTIVE:
					inactive();
					break;
				case LEFT:
					left();
					break;
				case RIGHT:
					right();
					break;
				}
				
				//スレッドの停止
				long end = System.currentTimeMillis();
				long ptime = end - start;
				if (ptime < 5) {
					try {
						Thread.sleep(5 - ptime);					
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				start = System.currentTimeMillis();
				
				cnter++;
			}//End of while()
									
			//アニメーションの終了
			if(action== ACTIVE){
				state = ACTIVE;
			}else{
				start = INACTIVE;
			}

			//なかったらアニメーションを終了
			animationFlag = false;
		}
	}
}