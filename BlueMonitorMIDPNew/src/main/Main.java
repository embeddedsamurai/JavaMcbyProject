package main;


import javax.microedition.midlet.MIDlet;

import calendar.CalendarManager;


public class Main extends Thread {

	//================================定数==================================//
	/**
	 * デバックフラグを定義します。
	 * <br>
	 * TODO: リリース時には<code>false</code>にしてください。
	 */
	public static final boolean DEBUG = true;

	/** 実機での実行かどうか true:実機 false:エミュレータ */
	public static final boolean IS_ACTUAL = false;
	
	/** エクリプスでのコンパイルかどうか(実機には影響なし) */
	public static final boolean IS_ECLIPSE = false;

	/** ループ内でのスリープ時間*/
	private static final int SLEEP_TIME = 100;

	/** タイトルモード */
	public static final int TITLE_MODE   = 0;
	/** 測定 モード */
	public static final int MEASURE_MODE = 1;
	/** カレンダーモード */
	public static final int CALENDAR_MODE = 2;
	/** 終了 モード */
	public static final int QUIT_MODE = 3;

	//=================================変数==================================//
	/** MIDlet */
	private MIDlet midlet;
	/** 状態 */
	private int mode_state;
	/** ひとつ前の状態 */
	private int back_mode_state;

	/** タイトル画面の描画と計算を担当するクラス */
	private TitleManager title;
	/** カレンダーの描画と計算を担当するクラス */
	private CalendarManager calmon;
	/** 血圧モニタの描画と計算を担当するクラス */
	private BPMonitorManager bpmon;

	/** 実行用スレッド */
	private Thread thread=null;

	//===============================初期化処理================================//

	/**
	 * コンストラクタ
	 *
	 * @param midlet MIDletオブジェクト
	 */
	public Main(MIDlet midlet) {
		this.midlet = midlet;

		//最初はタイトルモード
		mode_state = TITLE_MODE;
		back_mode_state = mode_state;
		modeChange(mode_state);

		//メインループを起動
		thread = new Thread(this);
		//実行の開始
		thread.start();
	}

	//================================メイン処理=================================//

	/**
	 * アプリケーションのループをスレッド内で実行
	 */
	public void run(){
		//スリープ時間
		long startTime = System.currentTimeMillis();
		//計算にかかった時間
		long pastTime = 0;

		while (true){

			//キー、計算、描画の各種処理
			if(mode_state == TITLE_MODE && title != null){
				//タイトルモード
				title.process();
			}else if(mode_state == CALENDAR_MODE && calmon != null){
				//カレンダーモード
				calmon.process();
			}else if(mode_state == MEASURE_MODE  && bpmon != null){
				//計算モード
				bpmon.process();
			}

			if(back_mode_state != mode_state){//モードの変更がないかどうかチェック。
				//モード変更の反映
				modeChange(mode_state);
				//前のモードの終了
				modeQuit(back_mode_state);
				//今の状態を取っておく
				back_mode_state = mode_state;
			}

			//計算にかかった時間
			pastTime = System.currentTimeMillis() - startTime;

			if(pastTime < SLEEP_TIME){
				//休止
				pause(SLEEP_TIME+5 - pastTime);
			}
			startTime = System.currentTimeMillis();
		}
	}


	/**
	 * スレッドの休止
	 */
	public void pause(long time){
		try {
			//スリープ
			Thread.sleep(time);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//=============================モードの切り替え等================================//

	/**
	 * モードの変更を反映する
	 *
	 * @param newMode 変更先モード
	 */
	private void modeChange(int newMode){
		if(newMode == TITLE_MODE){
			//タイトル
			title = new TitleManager(this);
			title.setDisplay(midlet);
		}else if(newMode == MEASURE_MODE){
			//計測
			bpmon = new BPMonitorManager(this,midlet);
			bpmon.setDisplay(midlet);
		}else if(newMode == CALENDAR_MODE){
			//カレンダー
			calmon = new CalendarManager(this,midlet);
			calmon.setDisplay(midlet);
		}else if(newMode == QUIT_MODE){
			//終了
			midlet.notifyDestroyed();
		}
	}

	/**
	 * モードを終了する
	 *
	 * @param mode 終了するモード
	 */
	private void modeQuit(int mode){
		switch (mode) {
		case TITLE_MODE://タイトル
			if(title != null)title = null;
			break;
		case CALENDAR_MODE://カレンダーモード
			if(calmon != null)calmon = null;
			break;		
		case MEASURE_MODE://計測モード
			if(bpmon != null)bpmon = null;
			break;
		}
	}

	/**
	 * モードを変更する
	 * 実際に反映されるのは、ループ内での次の状態のチェック時
	 *
	 * @param mode
	 */
	public void setMode(int mode){
		mode_state = mode;
	}
}
