package main;

import gui.TitleCanvas;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;

import request.Key;
import request.Request;
import request.RequestQueue;

import util.Option;

public class TitleManager implements CommandListener {
	
	//================================定数====================================//
	
	/**	 メニューのインデックスを一つ進めることを表す */
	private static final int INCREMENT_MENU = 0;
	/**	 メニューのインデックスを一つ減らすことを表す */
	private static final int DECREMENT_MENU = 1;
	/**	 現在フォーカスのあるメニュー項目を選択することを表す */
	private static final int SELECT_MENU = 2;
	
	//================================変数====================================//
	
	/** キャンバス */
	private TitleCanvas canvas;	
	/** リクエストキュー */
	private RequestQueue requestQueue;
	/** 親Thread */
	private Main parent;
	
	//===============================初期化処理=================================//
	
	/**
	 * コンストラクタ 
	 */
	public TitleManager(Main main) {
		this.parent = main;
		//キャンバスを作成
		canvas = new TitleCanvas();
		//コマンドリスナの設定
		canvas.setCommandListener(this);
		//リクエストキューを作成
		requestQueue = new RequestQueue();		
	}
	
	//===============================メイン処理==================================//
	
	/**
	 * 描画、計算などの一連の処理を行う
	 */
	public void process(){
		//キーを更新
		if(canvas.isShown())Key.registKeyEvent();
		//キー処理
		key();
		//リクエストの処理
		doRequest();
		//描画
		draw();
	}
		
	/**
	 * 描画処理
	 */
	private void draw() {
		canvas.draw();
	}
	
	/**
	 * リクエストの処理をする
	 */
	private void doRequest(){
		//キューからリクエストを取り出す
		Request req = requestQueue.getRequest();
		
		if(req != null){
			//キューがカラでない時
			int next = 0;
			switch (req.getCommand()){							
				case DECREMENT_MENU:				
					//メニューのフォーカスのインデックスを一つ進める
					next = (canvas.getSelectedMenuIndex()
							+canvas.getNumOfMenuItem() - 1)%canvas.getNumOfMenuItem();
					canvas.setSelectedMenuIndex(next);
					break;
				case INCREMENT_MENU:
					//メニューのフォーカスのインデックスを一つ戻す
					next = (canvas.getSelectedMenuIndex()+ 1)%canvas.getNumOfMenuItem();
					canvas.setSelectedMenuIndex(next);
					break;
				case SELECT_MENU:
					//現在フォーカスされているメニュー項目を選択する	
					menuProcess();
					break;
				default:
					break;
			}
		}
	}
	
	 	
	//=============================キー、コマンド処理================================//

	/**
	 * キー処理
	 */
	private void key(){
		if(Key.isKeyPressed(Canvas.FIRE)){//決定キーを押したとき
			//フォーカスされているメニュー項目を選択する
			requestQueue.putRequest(new Request(SELECT_MENU));
		}else if(Key.isKeyPressed(Canvas.DOWN)){//下キーを押したとき			
			//キャンバスでメニューのフォーカスのインデックスを一つ進める
			requestQueue.putRequest(new Request(INCREMENT_MENU));
		}else if(Key.isKeyPressed(Canvas.UP)){//上キーを押したとき			
			//キャンバスでメニューのフォーカスのインデックスを一つ戻す
			requestQueue.putRequest(new Request(DECREMENT_MENU));								
		}
	}
		
	/**
	 * コマンドを処理する 
	 * @param com コマンド
	 * @param disp コマンドを保持するディスプレーオブジェクトの参照
	 * 
	 */
	public void commandAction(Command com, Displayable disp) {	

	}
	
	/**
	 * 選択されたメニューに応じた処理
	 */
	private void menuProcess(){
		
		//選択されたメニュー
		String menu = canvas.getSelectedMenu();
		Option op = Option.getOp();
		
		if(menu.equals(TitleCanvas.MENU_EXIT)){
			//終了
			parent.setMode(Main.QUIT_MODE);			
		}else if(menu.equals(TitleCanvas.MENU_BACK)){
			//トップへ戻る
			canvas.setTitleMode(TitleCanvas.TITLE_TOP);			
		}else if(menu.equals(TitleCanvas.MENU_MESUREMENT)){
			//計測開始メニューへ
			canvas.setTitleMode(TitleCanvas.TITLE_MEASUREMENT);
		}else if(menu.equals(TitleCanvas.MENU_SETTING)){
			//設定メニューへ			
			canvas.setTitleMode(TitleCanvas.TITLE_SETTING);
		}else if(menu.equals(TitleCanvas.MENU_MESUREMENT_FILE)){
			//ファイルから計測開始	
			op.setBluetooth(false);			
			parent.setMode(Main.MEASURE_MODE);
		}else if(menu.equals(TitleCanvas.MENU_MESUREMENT_IO)){
			//Bluetoothから計測
			op.setBluetooth(true);			
			parent.setMode(Main.MEASURE_MODE);
		}else if(menu.equals(TitleCanvas.MENU_OLD_GRAPH)){
			//ネットワークからグラフ取得
			parent.setMode(Main.CALENDAR_MODE);
		}else if(menu.equals(TitleCanvas.MENU_BACK_LIGHT)){			
			//バックライトの設定へ			
			canvas.setTitleMode(TitleCanvas.TITLE_SETTING_LIGHT);
			//メニューインデックスを変更
			setMenuIndexForSetting(op.isBackLightOn());		
		}else if(menu.equals(TitleCanvas.MENU_MCBY_CHANGE)){			
			//マクビーの変更の設定へ
			canvas.setTitleMode(TitleCanvas.TITLE_SETTING_MCBY);
			//メニューインデックスを変更		
			setMenuIndexForSetting(op.isMCBY());					
		}else if(menu.equals(TitleCanvas.MENU_VIBRATION)){			
			//バイブレーションの設定へ
			canvas.setTitleMode(TitleCanvas.TITLE_SETTING_VIBRATION);
			//メニューインデックスを変更
			setMenuIndexForSetting(op.isVibrationOn());		
		}else if(menu.equals(TitleCanvas.MENU_SOUND)){			
			//サウンドの設定へ
			canvas.setTitleMode(TitleCanvas.TITLE_SETTING_SOUND);
			//メニューインデックスを変更
			setMenuIndexForSetting(op.isSoundOn());			
		}else if(menu.equals(TitleCanvas.MENU_SOUND)){			
			//QRコードの設定へ			
		}else if(menu.equals(TitleCanvas.MENU_POWER)){
			//電源周波数の変更メニューへ
			canvas.setTitleMode(TitleCanvas.TITLE_SETTING_POWER);
			//メニューインデックスを変更
			setMenuIndexForSetting(op.is50HzPowerSupply());
		}else if(menu.equals(TitleCanvas.MENU_MCBY1)){		
			//マクビーの変更(MCBY)
			op.setMCBY(true);			
			//設定一覧画面に戻る
			canvas.setTitleMode(TitleCanvas.TITLE_SETTING);
		}else if(menu.equals(TitleCanvas.MENU_MCBY2)){
			//マクビーの変更(ZEAL)
			op.setMCBY(false);
			//設定一覧画面に戻る
			canvas.setTitleMode(TitleCanvas.TITLE_SETTING);
		}else if(menu.equals(TitleCanvas.MENU_50HZ)){
			//電源周波数の変更(50Hz)
			op.set50HzPowerSupply(true);
			//設定一覧画面に戻る
			canvas.setTitleMode(TitleCanvas.TITLE_SETTING);
		}else if(menu.equals(TitleCanvas.MENU_60HZ)){
			//電源周波数の変更(60Hz)
			op.set50HzPowerSupply(false);
			//設定一覧画面に戻る
			canvas.setTitleMode(TitleCanvas.TITLE_SETTING);
		}else if(menu.equals(TitleCanvas.MENU_ON)){
			//設定をＯＮにする
			setEnableSetting(canvas.getTitleMode(),true);
			//設定一覧画面に戻る
			canvas.setTitleMode(TitleCanvas.TITLE_SETTING);
		}else if(menu.equals(TitleCanvas.MENU_OFF)){
			//設定をＯFFにする
			setEnableSetting(canvas.getTitleMode(),false);
			//設定一覧画面に戻る
			canvas.setTitleMode(TitleCanvas.TITLE_SETTING);
		}
	}		
	
	/**
	 * 設定画面のとき、ONとＯＦＦで最初に選択されているインデックスを変更する
	 * @param flag キャンバス中の設定のインデックスが、trueならＯＮ状態、falseならOFF状態
	 */
	private void setMenuIndexForSetting(boolean flag){
		if(flag){                          //現在ONのとき
			canvas.setSelectedMenuIndex(0);
		}else{                             //現在OFFのとき
			canvas.setSelectedMenuIndex(1);
		}
	}
	
	/**
	 * 設定をＯＮ、ＯＦＦする
	 * 
	 * @param mode タイトルモード
	 * @param flag trueならON, falseならOFF
	 */
	private void setEnableSetting(int mode,boolean flag){
		Option op = Option.getOp();
		switch (mode) {
			case TitleCanvas.TITLE_SETTING_LIGHT:
				//バックライトの設定		
				op.setBackLightOn(flag);							
				break;
			case TitleCanvas.TITLE_SETTING_SOUND:
				//サウンドの設定
				op.setSoundOn(flag);				
				break;
			case TitleCanvas.TITLE_SETTING_VIBRATION:
				//バイブレーションの設定
				op.setVibrationOn(flag);				
				break;
		}
	}
	
	//=================================その他===================================//	
	
	/**
	 * 表示する
	 */
	public void setDisplay(MIDlet midlet){
		Display.getDisplay(midlet).setCurrent(canvas);
	}
	
}
