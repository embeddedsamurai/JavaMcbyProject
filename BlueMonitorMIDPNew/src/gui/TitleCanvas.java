package gui;

import java.util.Vector;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import request.Key;

import main.Main;

import util.DojaFont;
import util.DojaGraphics;

public class TitleCanvas extends Canvas {

	//================================定数====================================//

	//メニュー用のプロパティ
	/** タイトル画面の"トップ画面"を表す定数 */
	public static final int TITLE_TOP = 0;
	/** タイトル画面の"測定の開始"を表す定数 */
	public static final int TITLE_MEASUREMENT = 1;
	/** タイトル画面の"設定"を表す定数 */
	public static final int TITLE_SETTING = 2;
	/** タイトル画面の"MCBY設定"を表す定数 */
	public static final int TITLE_SETTING_MCBY = 3;
	/** タイトル画面の"バックライト設定"を表す定数 */
	public static final int TITLE_SETTING_LIGHT = 4;
	/** タイトル画面の"サウンド設定"を表す定数 */
	public static final int TITLE_SETTING_SOUND = 5;
	/** タイトル画面の"バイブレーション設定"を表す定数 */
	public static final int TITLE_SETTING_VIBRATION = 6;
	/** タイトル画面の"電源周波数"を表す定数 */
	public static final int TITLE_SETTING_POWER = 7;

	//共通メニュー
	/** 戻るメニューを表す*/
	public static final String MENU_BACK = "戻る";
	/** ON/OFFメニューを表す */
	public static final String MENU_ON = "ON";
	public static final String MENU_OFF = "OFF";

	//最初に表示されるメニュー
	/** 計測の開始メニューを表す */
	public static final String MENU_MESUREMENT = "測定の開始";
	/** ネットワークからグラフを表示する */
	public static final String MENU_OLD_GRAPH = "過去のグラフを表示";
	/** 設定メニューを表す */
	public static final String MENU_SETTING = "設定";
	/** 終了メニューを表す*/
	public static final String MENU_EXIT = "終了";
	/** トップメニュー群 */
	private static final String[] TOPMENU = {MENU_MESUREMENT,
									         MENU_OLD_GRAPH,MENU_SETTING,MENU_EXIT};

	//計測のメニュー
	/** 通信ポートから計測を開始するメニューを表す */
	public static final String MENU_MESUREMENT_IO = "Bluetoothで計測";
	/** ファイルから計測を開始するメニューを表す */
	public static final String MENU_MESUREMENT_FILE = "ファイルから計測";
	/**計測のメニュー群 */
	private static final String[] MEASUREMENTMENU = {MENU_MESUREMENT_IO,
		                                            MENU_MESUREMENT_FILE,MENU_BACK};

	//設定一覧メニュー
	/** 最高血圧値のメニューを表す*/
	public static final String MENU_MAX_BP = "最高血圧値";
	/** ログの保存メニューを表す */
	public static final String MENU_ISSAVE_LOG = "ログの保存";
	/** MCBY ID */
	public static final String MENU_MCBY_CHANGE = "MCBYの切り替え";
	/** バックライトの設定 */
	public static final String MENU_BACK_LIGHT = "バックライト";
	/** サウンド */
	public static final String MENU_SOUND = "サウンド";
	/** バイブレーション */
	public static final String MENU_VIBRATION = "バイブレーション";
	/** QRコードの取得 */
	public static final String MENU_QR_CODE = "QRコードの取得";
	/** 電源周波数 */
	public static final String MENU_POWER = "電源周波数";
	/** 設定一覧のメニュー群(上記メニューをまとめたもの) */
	private static final String[] SETTINGMENU = {MENU_MCBY_CHANGE,MENU_POWER,
		                                        MENU_BACK_LIGHT,MENU_SOUND,MENU_VIBRATION,
		                                        MENU_QR_CODE,MENU_BACK};

	//電源周波数設定のメニュー
	/** 電源周波数設定のメニュー(50Hz) */
	public static final String MENU_50HZ = "50Hz";
	/** 電源周波数設定のメニュー(60Hz) */
	public static final String MENU_60HZ = "60Hz";
	/** 設定一覧のメニュー群(上記メニューをまとめたもの) */
	private static final String[] POWERMENU = {MENU_50HZ,MENU_60HZ};

	//MCBYの選択メニュー
	/** MCBY設定のメニュー(マウス) */
	public static final String MENU_MCBY1 = "MCBY 1 (マウス)";
	/** MCBY設定のメニュー(ZEAL) */
	public static final String MENU_MCBY2 = "MCBY 2 (ZEAL)";
	/** MCBY設定メニュー群(上記メニューをまとめたもの)　*/
	public static final String[] MCBYMENU = {MENU_MCBY1,MENU_MCBY2};

	/** バックライト設定メニュー群 */
	public static final String[] BACKLIGHTMENU = {MENU_ON,MENU_OFF};
	/** サウンド設定メニュー群 */
	public static final String[] SOUNDMENU = {MENU_ON,MENU_OFF};
	/** バイブレーション設定メニュー群 */
	public static final String[] VIBRATIONMENU = {MENU_ON,MENU_OFF};

	//----------------- 画像 -------------------//
	/**タイトル画面に表示する画像のURL */
	private static final String TITLE_IMG_URL = (Main.IS_ECLIPSE)?("/res/MCBY_white.gif"):("/MCBY_white.gif") ;
	/** コーナーの画像 */
	private static final String IMG_CORNER = (Main.IS_ECLIPSE)?("/res/menu_waku_01.gif"):("/menu_waku_01.gif");
	/** 左端・右端の画像 */
	private static final String IMG_VERTICAL_LINE = (Main.IS_ECLIPSE)?("/res/menu_waku_02.gif"):("/menu_waku_02.gif");
	/** 上端・下端の画像 */
	private static final String IMG_HORIZONTAL_LINE = (Main.IS_ECLIPSE)?("/res/menu_waku_03.gif"):("/menu_waku_03.gif");

	//----------------- 描画関連 -------------------//
	/** 背景色 */
	private static final int BG_WHITE = 0x00FFFFFF;
	/** 文字色 */
	private static final int STR_COLOR = 0x00FF66B4;
	/** フォーカスされている文字の色 */
	private static final int FOCUS_STR_COLOR =  0x00FFC0CB;
	/** フォント */
	private static final DojaFont FONT = DojaFont.getFont(DojaFont.SIZE_TINY);
	/** フォントのディセント */
	private static final int FONT_DECENT = FONT.getDescent();
	/** フォントのアセント */
	private static final int FONT_ASCENT = FONT.getAscent();
	/** フォントの高さ */
	private static final int FONT_HEIGHT = FONT_DECENT + FONT_ASCENT;
	
	/** ウィンドウの端のマージン*/
	private static final int WINDOW_MARGIN = 25;
	/** 点滅間隔 */
	private static final int BLINK_IINTERVAL = 8;

	//=================================変数=====================================//

	//-----------------高さ、幅-------------------//

	/** ウィンドウ幅 */
	private int width;
	/** ウィンドウ高さ */
	private int height;

	//----------------描画関連 -------------------//

	/** オフグラフィックス */
	protected DojaGraphics offGra;
	/** オフイメージ */
	protected Image offImg;
	/** タイトルに表示する画像 */
	private Image titleImg;
	/** 枠画像 */
	private Image[] frameImages = new Image[3];
	private FrmWnd frameWindow;
		
	/**　▲を点滅させるためのカウンタ */
	private int blinkCnter = 0;
	/**　▲を点滅させるためのフラグ */
	private boolean blinkflag = true;

	//---------------- メニュー -------------------//

	/** タイトル画面へ表示するメニューの選択されているインデックス */
	private int menuIndex = 0;
	/** 表示するメニュー項目を保持するコレクション */
	private Vector menu;
	/** 現在のタイトルモード */
	private int titleMode = TITLE_TOP;

	//=============================初期化処理===================================//

	/**
	 * コンストラクタ
	 */
	public TitleCanvas() {
		//幅
		width = getWidth();
		//高さ
		height = getHeight();

		//ダブルバッファリングのためのオフグラフィックスイメージの生成
		if (offImg == null) {
			offImg   =Image.createImage(width,height);
			offGra =  new DojaGraphics(offImg.getGraphics(),this);
		}
	    // フォントを設定
	    offGra.setFont(FONT);
	    //画像の読み込み
	    loadImage();
	    //メニュー用コレクションを初期化
	    menu = new Vector();

	    //変数の初期化
	    init();
	}

	/**
	 * 変数の初期化処理
	 */
	public void init(){
		//メニューを初期状態に(初期はトップメニュー)
		menu.removeAllElements();
		for(int i = 0; i < TOPMENU.length ; i++){
			menu.addElement(TOPMENU[i]);
		}
	}

	/**
	 * 画像の読み込み処理を行う
	 */
	private void loadImage(){
		try {
			// タイトル画像のロード
	        titleImg = Image.createImage(TITLE_IMG_URL);

	        //枠	の画像読み込み
	    	frameImages[0] = Image.createImage(IMG_CORNER);
	      	frameImages[1] = Image.createImage(IMG_VERTICAL_LINE);
	      	frameImages[2] = Image.createImage(IMG_HORIZONTAL_LINE);

	      	//枠ウィンドウを作成
	      	frameWindow = new FrmWnd(frameImages,true);

		} catch (Exception e) {
			System.out.println("Read Images Error: " + e.getMessage());
	        System.out.println(e.toString());
	    }
	}

	//================================描画=====================================//

	/**
	 * 描画(オフグラフィックスを反映させる)
	 */
	protected void paint(Graphics g) {
		g.drawImage(offImg,0,0,Graphics.LEFT|Graphics.TOP);
	}

	/**
	 * 描画処理を行う
	 */
	public void draw(){
		offGra.lock();

		//背景塗りつぶし
		offGra.setColor(BG_WHITE);
		offGra.clearRect(0,0,width,height);

		//タイトル画面描画
		drawtitle(offGra);

		offGra.unlock(true);
	}

	/**
	 * タイトルを描画
	 * @param g レンダリング用Graphicsオブジェクト
	 */
	private void drawtitle(DojaGraphics g){
   		//点滅カウンタを更新
   		blinkCnter++;
   		
	    //文字間の隙間の大きさ
	    final int margin = 10;

		//タイトル画像を描画
	    g.drawImage(titleImg,(width - titleImg.getWidth())>>1,margin);
	    //フォントの設定
	    g.setColor(STR_COLOR);

		//描画調整用
		final int charWidth = FONT.stringWidth("▲");
        
		//最大表示数
		final int maxNum = 4;
		//メニュー項目の数
		final int menuLen = menu.size();
		
		//ウィンドウの高さ
		int windowHeight = 0;
		if(menuLen > maxNum){
			//項目が多いときは	//一度に全部を表示しない
			windowHeight = (FONT_HEIGHT + margin) * (maxNum) + margin;					
		}else{
			windowHeight = (FONT_HEIGHT + margin) * menuLen + margin;
		}
		
		// メニューウィンドウを表示
		Image img = frameWindow.getWindow(width-(WINDOW_MARGIN<<1),windowHeight);
		offGra.drawImage(img,(width-img.getWidth())>>1,((height)>>1));
		
		//描画開始座標
		int y = ((height)>>1) + FONT_HEIGHT + (margin>>1);
		
		// メニュー項目を描画		
		if(menuLen <= maxNum){
			
			//表示可能な最大数を超えていないとき
			for (int i = 0; i < menuLen  ; i++) {
				
				// フォーカスがあるときは、バックグラウンドを違う色で描画				
				if (i == menuIndex) {
					offGra.setColor(FOCUS_STR_COLOR);
				}else{
					offGra.setColor(STR_COLOR);
				}
				
				// メニュー項目を描画
				int x = (width >> 1);
				offGra.drawString(((String)menu.elementAt(i)), x, y);

				y += margin + FONT_HEIGHT;
			}
		}else{
			//表示可能な最大数を超えているとき
			
			offGra.setColor(STR_COLOR);
			//▲マークの表示
			if(menuIndex > (maxNum >> 1) && blinkflag ){
				//▲マークで上にまだ表示する項目があることを示す
				offGra.drawString("▲",width - (WINDOW_MARGIN >> 1) - charWidth, y );				
			}
						
			int offset = menuIndex - (maxNum >> 1);
			
			if(offset < 0) {
				offset = 0;
			}else if(menuIndex + (maxNum >> 1) > menuLen - 1){
				offset = menuLen - maxNum ;					
			}
			
			for (int i = offset; i < offset + maxNum ; i++) {
				
				// フォーカスがあるときは、バックグラウンドを違う色で描画
				// フォーカスがあるときは、バックグラウンドを違う色で描画				
				if (i == menuIndex) {
					offGra.setColor(FOCUS_STR_COLOR);
				}else{
					offGra.setColor(STR_COLOR);
				}

				// メニュー項目を描画
				int x = (width >> 1);
				offGra.drawString((String)menu.elementAt(i), x, y);

				y += margin + FONT_HEIGHT;
			}
			
						
			//▼マークの表示
			if( offset < menuLen - maxNum && blinkflag){			
				//▼マークで下にまだ表示する項目があることを示す
				offGra.drawString("▼",width - (WINDOW_MARGIN >> 1 ) - charWidth , y - (margin + FONT_HEIGHT));
			}
		}
		
		//点滅用カウンタをリセット
		if(blinkCnter >= BLINK_IINTERVAL){
			blinkCnter = 0;
			blinkflag = !blinkflag;
		}

	}

	//==============================キー処理===================================//

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

	//=============================setter,getter=================================//

	/**
	 * メニューのフォーカスを変更
	 * @param index 新しくフォーカスするインデックス
	 */
	public void setSelectedMenuIndex(int index){
		if(menu.size() <= index)index = 0;
		this.menuIndex = index;
	}
	/**
	 * メニューのフォーカスを返す
	 * @return フォーカスしているメニューのインデックス
	 */
	public int getSelectedMenuIndex(){
		return this.menuIndex;
	}

	/**
	 * メニューの項目数を返す
	 * @return
	 */
	public int getNumOfMenuItem(){
		return menu.size();
	}

	/**
	 * フォーカスのあるメニューを返す
	 * @return
	 */
	public String getSelectedMenu(){
		return (String)menu.elementAt(menuIndex);
	}

	/**
	 * タイトルモードを変更する
	 * @param mode
	 */
	public void setTitleMode(int mode){
		this.titleMode = mode;
		switch (mode) {
		case TITLE_TOP:              //トップ画面変更
			setMenu(TOPMENU);
			break;
		case TITLE_MEASUREMENT:      //測定画面メニュー群へ変更
			setMenu(MEASUREMENTMENU);
			break;
		case TITLE_SETTING:		     //設定メニュー群へ変更
			setMenu(SETTINGMENU);
			break;
		case TITLE_SETTING_LIGHT:    //バックライト設定メニューへ変更
			setMenu(BACKLIGHTMENU);
			break;
		case TITLE_SETTING_MCBY:     //マクビー選択メニューへ
			setMenu(MCBYMENU);
			break;
		case TITLE_SETTING_SOUND:    //サウンド設定メニューへ
			setMenu(SOUNDMENU);
			break;
		case TITLE_SETTING_VIBRATION://バイブレーション設定メニューへ
			setMenu(VIBRATIONMENU);
			break;
		case TITLE_SETTING_POWER:    //電源周波数設定メニューへ
			setMenu(POWERMENU);
		}
		menuIndex = 0;
	}

	/**
	 * 現在のタイトルモードを得る
	 */
	public int getTitleMode(){
		return this.titleMode;
	}

	/**
	 * 表示中のメニュー群を変更する
	 * @param array 新たに表示するメニュー群
	 */
	private void setMenu(String[] array){
		menu.removeAllElements();
		for(int i = 0; i < array.length ; i++){
			menu.addElement(array[i]);
		}
	}

}
