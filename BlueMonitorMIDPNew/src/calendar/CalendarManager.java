package calendar;


import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;

import calendar.renderer.RollMenuRenderer;
import calendar.renderer.RawWaveRenderer;
import calendar.renderer.TripleRotateMenuRenderer;
import calendar.util.CalendarUtil;
import calendar.util.ReadDBData;
import calendar.util.ReadDBListener;
import calendar.util.ReadDBThread;
import calendar.util.ReadNumOfDBData;

import request.Key;
import request.Request;
import request.RequestQueue;

import main.Main;


public class CalendarManager implements CommandListener,ReadDBListener{

	//=================================定数==================================//
	//状態を表す定数
	/** カレンダー */
	private static final int STATUS_CALENDAR = 0;
	/** メニュー */
	private static final int STATUS_ROLL_MENU = 1;
	/** メニュー */
	private static final int STATUS_ROTATE_MENU = 2;
	/** 生波形 */
	private static final int STATUS_RAW_WAVE = 3;
	/** BP,HR波形 */
	private static final int STATUS_BPHR_WAVE = 4;
	/** 棒グラフ(日) */
	private static final int STATUS_DAILY_BAR_GEAPH = 6;
	/** 棒グラフ(週) */
	private static final int STATUS_WEEK_BAR_GEAPH = 7;
	/** 棒グラフ(月) */
	private static final int STATUS_MONTH_BAR_GEAPH = 8;
	/** 棒グラフ(年) */
	private static final int STATUS_YEAR_BAR_GEAPH = 9;	
	/** Wait */
	private static final int STATUS_WAIT = 10;
	
	//リクエスト
	public static final int LEFT_REQ  = 0;
	public static final int RIGHT_REQ = 1;
	public static final int UP_REQ   = 2;
	public static final int DOWN_REQ  = 3;
	public static final int ENTER_REQ  = 4;	
	public static final int READDB_REQ  = 5;

	//=================================変数==================================//
	//状態を表す変数
	/** 状態 */
	private int status = STATUS_CALENDAR;
	/** キー状態 */
	private int keyStatus = STATUS_CALENDAR;
	/** ひとつ前の状態 */
	private int preStatus = STATUS_CALENDAR;
	  
	/** カレンダーキャンバス */	
	private CalendarCanvas canvas;
	/** 戻るコマンド*/
	private Command backCmd = new Command("戻る", Command.SCREEN, 0);
	
	/** 親スレッド */
	private Main parent;
	
	/** キーを保存しておくキュー */
	private RequestQueue requestQueue;
	
	/** DBからデータを読み込んでいるかどうかのフラグ */
	private boolean readDBFlag = false;
	/** DBからのデータ読み込み用クラス */
	private ReadDBThread readDB;
	
	/** メニューを描画するかどうかのフラグ */
	private boolean rollmenuFlag = false;		 
	
	//=================================関数==================================//	
	/**
	 * コンストラクタ
	 * @param parent 親スレッド
	 */
	public CalendarManager(Main parent,MIDlet midlet) {
		if(Main.DEBUG)System.out.println("CalendarManger#constructor()");
		this.parent = parent;		
		
		//キャンバスを生成
		canvas = new CalendarCanvas(midlet);
		//コマンドリスナとして設定
		canvas.setCommandListener(this);
		//戻るコマンドを追加
		canvas.addCommand(backCmd);

		//キーを保存しておくキュー
		requestQueue = new RequestQueue();
	}
	
	/**
	 * 開始
	 */
	public void start(){
		if(Main.DEBUG)System.out.println("CalendarManger#start()");
	}

	/**
	 * 終了
	 */
	public void exit(){
		if(Main.DEBUG)System.out.println("CalendarManger#exit()");		
		//タイトルモードに戻る
		parent.setMode(Main.TITLE_MODE);
	}

	//===============================メインの処理==================================//
	
	/**
	 * メインの処理 
	 */
	public void process(){
		try {
			//キーを更新
			if(canvas.isShown())Key.registKeyEvent();
			//キー処理
			key();
			//リクエストの処理
			doRequest();
			//描画			
			draw();			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 描画処理
	 */
	private void draw(){ 
		if(status == STATUS_CALENDAR){
			canvas.drawCalender();			
		}else if(status == STATUS_ROTATE_MENU){
			canvas.drawTrippleRotateMenu();
		}else if(status == STATUS_RAW_WAVE){
			canvas.drawRawWave();
		}else if(status == STATUS_BPHR_WAVE){
			canvas.drawBpHrWave();
		}else if(status == STATUS_DAILY_BAR_GEAPH){
			canvas.drawDailyBarGraph();
		}else if(status == STATUS_WEEK_BAR_GEAPH){
			canvas.drawWeekBarGraph();
		}else if(status == STATUS_MONTH_BAR_GEAPH){
			canvas.drawMonthBarGraph();
		}else if(status == STATUS_YEAR_BAR_GEAPH){
			canvas.drawYearBarGraph();
		}
	}/* End of draw() */
	
	/**
	 * キー処理
	 */
	private void key(){
		if(!canvas.isShown())return; //表示中でない時はなにもしない		
		
		if(Key.isKeyPressed(Canvas.FIRE)){
			//決定キーを押したとき
			requestQueue.putRequest(new Request(ENTER_REQ));
		}else if(Key.isKeyPressed(Canvas.LEFT)){
			//左キーを押したとき
			requestQueue.putRequest(new Request(LEFT_REQ));			
		}else if(Key.isKeyPressed(Canvas.RIGHT)){
			//右キーを押したとき
			requestQueue.putRequest(new Request(RIGHT_REQ));			
		}else if(Key.isKeyPressed(Canvas.UP)){
			//上キーを押したとき
			requestQueue.putRequest(new Request(UP_REQ));
		}else if(Key.isKeyPressed(Canvas.DOWN)){
			//下キーを押したとき
			requestQueue.putRequest(new Request(DOWN_REQ));
		}//End of if
	}//End of key()
	
	
	//============================リクエスト処理==================================//
	/**
	 * リクエストの処理
	 */
	private void doRequest(){
		
		Request req = requestQueue.getRequest();
		
		if(req != null){
			switch (keyStatus) {
			case STATUS_CALENDAR:
				//カレンダー状態のとき
				calendarRequest(req);
				break;
			case STATUS_ROLL_MENU:
				//ロールメニュー状態のとき
				rollMenuRequest(req);
				break;
			case STATUS_ROTATE_MENU:
				//回転メニューのとき
				triRotateMenuRequest(req);
				break;
			case STATUS_WAIT:
				//Waitメニューのとき
				waitScreenRequest(req);
				break;
			case STATUS_RAW_WAVE:
				//波形状態のとき
				rawWaveRequest(req);
				break;
			case STATUS_BPHR_WAVE:
				//BP,HR波形状態のとき
				bpHrWaveRequest(req);
				break;
			case STATUS_DAILY_BAR_GEAPH:
				//棒グラフ状態のとき
				dailyBarGraphRequest(req);
				break;
			case STATUS_WEEK_BAR_GEAPH:
				//棒グラフ状態のとき
				weekBarGraphRequest(req);
				break;
			case STATUS_MONTH_BAR_GEAPH:
				//棒グラフ状態のとき
				monthBarGraphRequest(req);
				break;
			case STATUS_YEAR_BAR_GEAPH:
				//棒グラフ状態のとき
				yearBarGraphRequest(req);
				break;
			}//end of switch
			
		}//end of if
		
	}//end of doRequest
	
	/**
	 * カレンダー状態のときのリクエストの処理
	 * @param req
	 */
	private void calendarRequest(Request req){

		switch (req.getCommand()) {
		case ENTER_REQ:
			if(!canvas.calendar.isSelectDay()){
				//日が選択されていなかったらなにもしない
				return;
			}
			//回転メニューの表示
			preStatus = status;
			status = STATUS_ROTATE_MENU;
			keyStatus = STATUS_ROTATE_MENU;
			canvas.triRotatemenu.setMode(TripleRotateMenuRenderer.MODE_DAY);
			canvas.triRotatemenu.posInit();
			break;			
		case LEFT_REQ:
			//フォーカスを左に
			canvas.calendar.left();
			break;
		case RIGHT_REQ:
			//フォーカスを右に
			canvas.calendar.right();
			break;
		case UP_REQ:
	        canvas.calendar.up();
			break;
		case DOWN_REQ:
			//フォーカスを下へ
	        canvas.calendar.down();
			break;
		}//end of switch
	}//end of calenderRequest
	
	/**
	 * ロールメニュー状態のときのリクエストの処理
	 * @param req
	 */
	private void rollMenuRequest(Request req){
		
		int com = req.getCommand();
		switch (com) {
		case ENTER_REQ:			
			break;
		case RIGHT_REQ:
			//フォーカスを右へ移動
			System.out.println("右");
	        canvas.rollMenu.action(RollMenuRenderer.RIGHT);
	        //DBからデータ読み込み
	        displayBarGraph();
			break;
		case LEFT_REQ:
			//フォーカスを左へ移動
			System.out.println("左");
	        canvas.rollMenu.action(RollMenuRenderer.LEFT);
	        //DBからデータ読み込み
	        displayBarGraph();
			break;
		case UP_REQ:
			//フォーカスを上へ移動
	        keyStatus = status;
			canvas.rollMenu.setFocus(false);
			canvas.rollMenu.action(RollMenuRenderer.INACTIVE);	        
			break;			
		case DOWN_REQ:
			//フォーカスを下へ移動
	        keyStatus = status;
			canvas.rollMenu.setFocus(false);
			canvas.rollMenu.action(RollMenuRenderer.INACTIVE);	        
			break;		
		}//end of switch
	}//end of rollmenuRequest()
	
	/**
	 * 回転メニューのリクエストの処理
	 * @param req リクエスト
	 */
	private void triRotateMenuRequest(Request req){
		int com = req.getCommand();								
		
		switch (com) {	      
		case ENTER_REQ:			
			canvas.removeCommand(backCmd);
			//DBからの読み込み			
			displayBarGraph();
        break;
		case UP_REQ:
			//上へ
			canvas.triRotatemenu.action(TripleRotateMenuRenderer.UP);
		break;
		case DOWN_REQ:
			//下へ
			canvas.triRotatemenu.action(TripleRotateMenuRenderer.DOWN);				
        break;	      
		}//end of switch()
	}//end of triRotateMenuRequest()

	/**
	 * Wait画面のリクエストの処理
	 * @param req リクエスト
	 */
	private void waitScreenRequest(Request req){
		int com = req.getCommand();						
		
		switch(com){
      	case ENTER_REQ:
      		if(canvas.waitScreen.isMenuFlag()){
      			//メニューがオンのとき
          		int index = canvas.waitScreen.getIndex();
          		
          		if(index == 0){          			        			
          			//リスナの解除
          			if(readDB != null){
          				readDB.deleteListener(canvas.rawWave);
        				readDB.deleteListener(this);	
          			}    				
    				//スレッドの破棄、回転メニューへ戻る
    				onErrorReadDB();
          		} else{
          			//DBからの読み込みを継続,メニューを消す
          			canvas.waitScreen.menuOff();          			
          		}//End of if()
          		
      		}else{
      			//メニューがオフのとき
      			canvas.waitScreen.menuOn();
      		}//End of if() 
      		break;
      	case UP_REQ:
      		canvas.waitScreen.up();
      		break;
      	case DOWN_REQ:
      		canvas.waitScreen.down();      		
      		break;
	    }//End of switch
	}//End of waitScreenRequest()
	
	/**
	 * 波形画面のときのリクエストの処理
	 * @param req リクエスト
	 */
	private void rawWaveRequest(Request req){
		int com = req.getCommand();
		int offset = 0;
	    switch(com){
	    case UP_REQ:
	    	//x軸方向の拡大率の増加	    	
	    	canvas.rawWave.setXReductionRate(canvas.rawWave.getXReductionRate()+1);
	    	break;
	    case DOWN_REQ:
	    	//x軸方向の拡大率の減少
	    	canvas.rawWave.setXReductionRate(canvas.rawWave.getXReductionRate()-1);
	    	break;
	    case LEFT_REQ:
	    	//前の波形を表示
	    	offset = canvas.rawWave.previousWave();
	    	if(offset != -1){
	    		//DBからデータのの読み込み
	    		displayRawWave(offset);
	    	}
	    	break;
	    case RIGHT_REQ:
	    	//次の波形を表示
	    	offset = canvas.rawWave.nextWave();
	    	if(offset != -1){
	    		//DBからデータの読み込み
	    		displayRawWave(offset);
	    	}
	    	break;
	    }//End of switch()	    
	}//End of rawWaveRequest()
	
	/**
	 * BP,HR波形描画状態のときのリクエストの処理
	 * @param req
	 */
	private void bpHrWaveRequest(Request req){
		int com = req.getCommand();
		int offset = 0;
	    switch(com){
	    case UP_REQ:
	    	//x軸方向の拡大率の増加	    	
	    	canvas.bpHrWave.setXReductionRate(canvas.bpHrWave.getXReductionRate()+1);
	    	break;
	    case DOWN_REQ:
	    	//x軸方向の拡大率の減少
	    	canvas.bpHrWave.setXReductionRate(canvas.bpHrWave.getXReductionRate()-1);
	    	break;
	    case LEFT_REQ:
	    	//前の波形を表示
	    	offset = canvas.bpHrWave.previousWave();
	    	if(offset != -1){
	    		//DBからデータのの読み込み
	    		displayRawWave(offset);
	    	}
	    	break;
	    case RIGHT_REQ:
	    	//次の波形を表示
	    	offset = canvas.bpHrWave.nextWave();
	    	if(offset != -1){
	    		//DBからデータの読み込み
	    		displayRawWave(offset);
	    	}
	    	break;
	    }//End of switch()	
	}//End of bpHrWaveRequest()
	
	/**
	 * 棒グラフ状態のときのリクエストの処理
	 * @param req リクエスト
	 */
	private void ecgPlsBarGraphRequest(Request req){
		int com = req.getCommand();
	    switch(com){
	    case ENTER_REQ:
	    	//バーのクリック
	    	//選択した時間の生波形を取得する	    	
	    	displayRawWave(0);
	    	//波形インスタンスの初期化
	    	canvas.rawWave.init();
	    	break;
	    case UP_REQ:
	    	break;
	    case DOWN_REQ:
	    	break;
	    case LEFT_REQ:
	    	//前のバーの選択
	    	canvas.dailyBarGraph.previousBar();
	    	break;
	    case RIGHT_REQ:	        
	    	//次のバーの選択
	    	canvas.dailyBarGraph.nextBar();
	    	break;
	    }//End of switch()
	}//End of barGraphRequest()
	
	private void dailyBarGraphRequest(Request req){
		int com = req.getCommand();
	    switch(com){
	    case ENTER_REQ:
	    	//バーのクリック
	    	//選択した時間の生波形を取得する	    	
	    	displayRawWave(0);
	    	//波形インスタンスの初期化
	    	canvas.bpHrWave.init();
	    	break;
	    case UP_REQ:
	    	//上を押す
	    	if(rollmenuFlag){
	    		//メニューを表示
				canvas.rollMenu.setFocus(true);
				canvas.rollMenu.action(RollMenuRenderer.ACTIVE);
				keyStatus = STATUS_ROLL_MENU;
	    	}
	    	break;
	    case DOWN_REQ:
	    	//下を押す
	    	if(rollmenuFlag){
	    		//メニューを表示
				canvas.rollMenu.setFocus(true);
				canvas.rollMenu.action(RollMenuRenderer.ACTIVE);
				keyStatus = STATUS_ROLL_MENU;
	    	}
	    	break;
	    case LEFT_REQ:
	    	//前のバーの選択
	    	canvas.dailyBarGraph.previousBar();
	    	break;
	    case RIGHT_REQ:	        
	    	//次のバーの選択
	    	canvas.dailyBarGraph.nextBar();
	    	break;
	    }//End of switch()
	}//End of dailyBarGraphRequest() 
	
	/**
	 * 棒グラフ(週)状態のときのリクエストの処理
	 * @param req リクエスト
	 */
	private void weekBarGraphRequest(Request req){
		int com = req.getCommand();
	    switch(com){
	    case ENTER_REQ:
	    	//バーのクリック
	    	//選択した時間の生波形を取得する	    	
	    	displayRawWave(0);
	    	//波形インスタンスの初期化
	    	canvas.bpHrWave.init();	    	
	    	break;
	    case UP_REQ:
	    	//上を押す
	    	if(rollmenuFlag){
	    		//メニューを表示
				canvas.rollMenu.setFocus(true);
				canvas.rollMenu.action(RollMenuRenderer.ACTIVE);
				keyStatus = STATUS_ROLL_MENU;
	    	}
	    	break;
	    case DOWN_REQ:
	    	//下を押す
	    	if(rollmenuFlag){
	    		//メニューを表示
				canvas.rollMenu.setFocus(true);
				canvas.rollMenu.action(RollMenuRenderer.ACTIVE);
				keyStatus = STATUS_ROLL_MENU;
	    	}
	    	break;
	    case LEFT_REQ:
	    	//前のバーの選択
	    	canvas.weekBarGraph.previousBar();
	    	break;
	    case RIGHT_REQ:	        
	    	//次のバーの選択
	    	canvas.weekBarGraph.nextBar();
	    	break;
	    }//End of switch()
	}//End of weekBarGraphRequest()	
	
	/**
	 * 棒グラフ(月）状態のときのリクエスト処理
	 * @param req
	 */
	private void monthBarGraphRequest(Request req){
		int com = req.getCommand();
	    switch(com){
	    case ENTER_REQ:
	    	//バーのクリック
	    	//選択した時間の生波形を取得する	    	
	    	displayRawWave(0);
	    	//波形インスタンスの初期化
	    	canvas.bpHrWave.init();
	    	break;
	    case UP_REQ:
	    	//上を押す
	    	if(rollmenuFlag){
	    		//メニューを表示
				canvas.rollMenu.setFocus(true);
				canvas.rollMenu.action(RollMenuRenderer.ACTIVE);
				keyStatus = STATUS_ROLL_MENU;
	    	}
	    	break;
	    case DOWN_REQ:
	    	//下を押す
	    	if(rollmenuFlag){
	    		//メニューを表示
				canvas.rollMenu.setFocus(true);
				canvas.rollMenu.action(RollMenuRenderer.ACTIVE);
				keyStatus = STATUS_ROLL_MENU;
	    	}
	    	break;
	    case LEFT_REQ:
	    	//前のバーの選択
	    	canvas.monthBarGraph.previousBar();
	    	break;
	    case RIGHT_REQ:	        
	    	//次のバーの選択
	    	canvas.monthBarGraph.nextBar();
	    	break;
	    }//End of switch()
	}//End of monthBarGraphRequest()
	
	/**
	 * 棒グラフ（年)状態のときのリクエスト処理
	 * @param req
	 */
	private void yearBarGraphRequest(Request req){
			int com = req.getCommand();
	    switch(com){
	    case ENTER_REQ:
	    	//バーのクリック
	    	//選択した時間の生波形を取得する	    	
	    	displayRawWave(0);
	    	//波形インスタンスの初期化
	    	canvas.bpHrWave.init();	    	
	    	break;
	    case UP_REQ:
	    	//上を押す
	    	if(rollmenuFlag){
	    		//メニューを表示
				canvas.rollMenu.setFocus(true);
				canvas.rollMenu.action(RollMenuRenderer.ACTIVE);
				keyStatus = STATUS_ROLL_MENU;
	    	}
	    	break;
	    case DOWN_REQ:
	    	//下を押す
	    	if(rollmenuFlag){
	    		//メニューを表示
				canvas.rollMenu.setFocus(true);
				canvas.rollMenu.action(RollMenuRenderer.ACTIVE);
				keyStatus = STATUS_ROLL_MENU;
	    	}
	    	break;
	    case LEFT_REQ:
	    	//前のバーの選択
	    	canvas.yearBarGraph.previousBar();
	    	break;
	    case RIGHT_REQ:	        
	    	//次のバーの選択
	    	canvas.yearBarGraph.nextBar();
	    	break;
	    }//End of switch()
	}//End of yearbarGraphRequest()
	
	/**
	 * DB読み込み,棒グラフの表示
	 */
	private void displayBarGraph(){
		
		if(readDBFlag){
			//読み込み中のときは何もしない
			return;
		}
				
		//読み込むデータのタイプ
		int type = getDataType();							
		//DB空の読み込みフラグを立てる
		readDBFlag = true;
		
		//時間を取得する
		long time = 0;				
		time = canvas.calendar.getCurrentDate();
		System.out.println("time:" + time);
		
		//DBからの読み込み		
		readDB = new ReadDBThread(type,2345,0,RawWaveRenderer.numOfDataReadDB,time);
		
		//DBからの読み込みが終了すると呼ばれるインスタンスを登録
		if(type == ReadNumOfDBData.TYPE_ECG_NUM || 
		   type == ReadNumOfDBData.TYPE_PLS_NUM || 
		   type == ReadNumOfDBData.TYPE_HR_NUM || 
		   type == ReadNumOfDBData.TYPE_BP_NUM  ){
			//日表示,BPHR
			readDB.addListener(canvas.dailyBarGraph);
		}else if(type == ReadNumOfDBData.TYPE_WEEK_ECG_NUM || 
				 type == ReadNumOfDBData.TYPE_WEEK_PLS_NUM || 
				 type == ReadNumOfDBData.TYPE_WEEK_BP_NUM ||
			     type == ReadNumOfDBData.TYPE_WEEK_HR_NUM){
			//週表示			
			readDB.addListener(canvas.weekBarGraph);			
		}else if(type == ReadNumOfDBData.TYPE_MONTH_ECG_NUM || 
			     type == ReadNumOfDBData.TYPE_MONTH_PLS_NUM ||
				 type == ReadNumOfDBData.TYPE_MONTH_BP_NUM || 
			     type == ReadNumOfDBData.TYPE_MONTH_HR_NUM){
			//月表示			
			readDB.addListener(canvas.monthBarGraph);			
		}else if(type == ReadNumOfDBData.TYPE_YEAR_ECG_NUM || 
			     type == ReadNumOfDBData.TYPE_YEAR_PLS_NUM ||
				 type == ReadNumOfDBData.TYPE_YEAR_BP_NUM || 
			     type == ReadNumOfDBData.TYPE_YEAR_HR_NUM){
			//年表示			
			readDB.addListener(canvas.yearBarGraph);			
		}
		
		readDB.addListener(this);
			
		//読み込みを開始
		readDB.start();
		
		//待ち画面の表示
		canvas.waitScreen.action();
		//待ち画面状態へ
		keyStatus = STATUS_WAIT;
		
		if(rollmenuFlag){
			//メニューを隠す
			//メニューがでてるときはけしておく
			rollmenuFlag = false;
			canvas.rollMenu.setFocus(false);
			canvas.rollMenu.action(RollMenuRenderer.INACTIVE);		
		}//End of if()
			
	}//End of displayBarGraph()
	
	/**
	 * 生波形の描画
	 * 
	 * @param offset 取得するデータのオフセット
	 */
	private void displayRawWave(int offset){
		if(readDBFlag)return;
		
    	//読み込むデータのタイプ
		int type = getDataType();		
		
		//DB空の読み込みフラグを立てる
		readDBFlag = true;
		
		//時刻
		long time = 0;
		//最大データ数
		int maxlen = 0;
		
		//ECG,PLS波形ならtrue,それ以外はfalse;
		boolean flag = true;
				
		if(status == STATUS_RAW_WAVE){			
			//波形表示中に、次のデータまたは前のデータを再読み込みするとき			
			//時間の取得
			time = canvas.rawWave.getTime();
			
		}else if(status == STATUS_DAILY_BAR_GEAPH || 
				 status == STATUS_WEEK_BAR_GEAPH  || 
				 status == STATUS_MONTH_BAR_GEAPH || 
				 status == STATUS_YEAR_BAR_GEAPH   ){
			//BP,HR波形(初めて表示するとき)
			
			flag = false;
			
			//時間
			int[] times = null;
						
			if(status == STATUS_DAILY_BAR_GEAPH ){
				//日
				times  = canvas.dailyBarGraph.getSelectedDate();
				maxlen = canvas.dailyBarGraph.getSelectedDataLen();				 
			}else if(status == STATUS_WEEK_BAR_GEAPH ){
				//週
				times  = canvas.weekBarGraph.getSelectedDate();
				maxlen = canvas.weekBarGraph.getSelectedDataLen();				
			}else if(status == STATUS_MONTH_BAR_GEAPH ){ 
				//月
				times  = canvas.monthBarGraph.getSelectedDate();
				maxlen = canvas.monthBarGraph.getSelectedDataLen();				
			}else if(status == STATUS_YEAR_BAR_GEAPH ){
				//年
				times  = canvas.yearBarGraph.getSelectedDate();
				maxlen = canvas.yearBarGraph.getSelectedDataLen();				
			}else{
				System.out.println("out" + type);
				return;
			}
			
			//データ取得に失敗しているとき
			if(times[0] == -1){
				return;		
			}
			//１時間のミリ秒数
			long hour  =  60 * 60 * 1000L;			
			//-(9*hour)としたのはgetCurrentTime()で返ってくる時刻が
			//JST時刻で9時間＋されているから、０に戻す必要があるため。
			time  = CalendarUtil.getCurrentTime(times[0],times[1],times[2]-1) + (times[3]*hour - (9*hour));
			//データ最大長を登録
			if(type == ReadDBData.TYPE_ECG || type == ReadDBData.TYPE_PLS){
				canvas.rawWave.setMaxdataLen(maxlen);
			}else if(type == ReadDBData.TYPE_BP || type == ReadDBData.TYPE_HR ||
					 type == ReadDBData.TYPE_YEAR_BP || type == ReadDBData.TYPE_YEAR_HR){
				canvas.bpHrWave.setMaxdataLen(maxlen);	
			}
						
		}else if(status == STATUS_BPHR_WAVE){			
			//波形表示中に、次のデータまたは前のデータを再読み込みするとき			
			//時間の取得
			flag = false;
			
			time = canvas.bpHrWave.getTime();
			
		}else{
			return;
		}
		//DBからの読み込み
		readDB = new ReadDBThread(type,2345,offset,RawWaveRenderer.numOfDataReadDB,time);
		readDB.start();
		
		if(flag){
			//ECG,PLS
			readDB.addListener(canvas.rawWave);	
		}else{
			//BP,HR
			readDB.addListener(canvas.bpHrWave);
		}
		
		readDB.addListener(this);
		
		//待ち画面の表示
		canvas.waitScreen.action();
		//待ち画面状態へ
		keyStatus = STATUS_WAIT;
	}
	
	
	/**
	 * コマンドの処理
	 * 
	 * @param com
	 * @param disp
	 */
	public void commandAction(Command com, Displayable disp) {
		if(Main.DEBUG)System.out.println("CalendarManger#commandAction()");
		
		if(com.equals(backCmd)){
			//戻るコマンドを実行したときの処理
			
			if(status == STATUS_CALENDAR ){
				//カレンダー表示中なら終了
				exit();
			}else if(status == STATUS_ROTATE_MENU){
				//回転メニューなら前の状態に戻る
				status = preStatus;
				keyStatus = preStatus;
			}else if(status == STATUS_RAW_WAVE || status == STATUS_BPHR_WAVE){
				//生波形のとき
				int type = getDataType();
		    	  
				if(type == ReadNumOfDBData.TYPE_ECG_NUM||
				   type == ReadNumOfDBData.TYPE_PLS_NUM ||
    			   type == ReadNumOfDBData.TYPE_BP_NUM||
				   type == ReadNumOfDBData.TYPE_HR_NUM){					
					//棒グラフへ  					
					status = STATUS_DAILY_BAR_GEAPH;
					keyStatus = STATUS_DAILY_BAR_GEAPH;
					
				}else if(type == ReadNumOfDBData.TYPE_WEEK_ECG_NUM||
						 type == ReadNumOfDBData.TYPE_WEEK_PLS_NUM||
						 type == ReadNumOfDBData.TYPE_WEEK_BP_NUM||
						 type == ReadNumOfDBData.TYPE_WEEK_HR_NUM){					
					//棒グラフへ  					
					status = STATUS_WEEK_BAR_GEAPH;
					keyStatus = STATUS_WEEK_BAR_GEAPH;
					
				}else if(type == ReadNumOfDBData.TYPE_MONTH_ECG_NUM||
						 type == ReadNumOfDBData.TYPE_MONTH_PLS_NUM||
						 type == ReadNumOfDBData.TYPE_MONTH_BP_NUM||
						 type == ReadNumOfDBData.TYPE_MONTH_HR_NUM){					
					//棒グラフへ  					
					status = STATUS_MONTH_BAR_GEAPH;
					keyStatus = STATUS_MONTH_BAR_GEAPH;
					
				}else if(type == ReadNumOfDBData.TYPE_YEAR_ECG_NUM||
						 type == ReadNumOfDBData.TYPE_YEAR_PLS_NUM||
						 type == ReadNumOfDBData.TYPE_YEAR_BP_NUM ||
						 type == ReadNumOfDBData.TYPE_YEAR_HR_NUM){					
					//棒グラフへ  					
					status = STATUS_YEAR_BAR_GEAPH;
					keyStatus = STATUS_YEAR_BAR_GEAPH;
					
				}else{	
					//回転メニューへ		    		  
					status = STATUS_ROTATE_MENU;
					keyStatus = STATUS_ROTATE_MENU;
				}//End of if()
				
			}else if(status == STATUS_DAILY_BAR_GEAPH  ||
					 status == STATUS_WEEK_BAR_GEAPH   ||
					 status == STATUS_MONTH_BAR_GEAPH  || 
					 status == STATUS_YEAR_BAR_GEAPH){
				
				//棒グラフを表示しているとき
				if(rollmenuFlag){
					//メニューがでてるときはけしておく
					rollmenuFlag = false;
					canvas.rollMenu.setFocus(false);
					canvas.rollMenu.action(RollMenuRenderer.INACTIVE);
				}
				status = STATUS_ROTATE_MENU;
				keyStatus = STATUS_ROTATE_MENU;		          
			}//end of if()
		}//end of if()
	}//end of commandAction()
	
	//===============================その他================================//

	/**
	 * 表示する
	 */
	public void setDisplay(MIDlet midlet){
		if(Main.DEBUG)System.out.println("BPMonitorManager#setDisplay()");
		((CalendarCanvas)canvas).setDisplay();		
	}
	
	/**
	 * 前の状態と、選択したメニューから、
	 * DBから取り出すデータのタイプを得る
	 * 
	 * @return　DBから取り出すべきデータのタイプ,どれにも該当しない場合は -1
	 */
	private int getDataType(){
		
		String name = canvas.triRotatemenu.getItemName();
	
		//日		
		if(name.equals(TripleRotateMenuRenderer.ECG)){
			//心電図
			if((status == STATUS_DAILY_BAR_GEAPH && keyStatus == STATUS_DAILY_BAR_GEAPH) ||
			   (status == STATUS_WEEK_BAR_GEAPH  && keyStatus == STATUS_WEEK_BAR_GEAPH)  ||
			   (status == STATUS_MONTH_BAR_GEAPH && keyStatus == STATUS_MONTH_BAR_GEAPH) || 
			   (status == STATUS_YEAR_BAR_GEAPH  && keyStatus == STATUS_YEAR_BAR_GEAPH)  ||
			    status == STATUS_RAW_WAVE){
				//棒グラフ表示かつキー状態がが棒グラフ
				//(キー状態がロールメニューのときはDBからの読み込みを行わないため)
				//または、次の生波形を表示するとき = STATUS_RAW_WAVE				
				
				//生波形
				if(canvas.rollMenu.getIndex() != RollMenuRenderer.YEAR){
					//ECGのときは年グラフを表示しない
					return ReadDBData.TYPE_ECG;	
				}								
			}else{			
				//棒グラフ
				if(canvas.rollMenu.getIndex() == RollMenuRenderer.DAY){
					return ReadNumOfDBData.TYPE_ECG_NUM;	
				}else if(canvas.rollMenu.getIndex() == RollMenuRenderer.WEEK){
					return ReadNumOfDBData.TYPE_WEEK_ECG_NUM;
				}else if(canvas.rollMenu.getIndex() == RollMenuRenderer.MONTH){
					return ReadNumOfDBData.TYPE_MONTH_ECG_NUM;
				}else if(canvas.rollMenu.getIndex() == RollMenuRenderer.YEAR){
					return ReadNumOfDBData.TYPE_YEAR_ECG_NUM;
				}
			}			
		}else if(name.equals(TripleRotateMenuRenderer.PLS)){
			//脈波
			if((status == STATUS_DAILY_BAR_GEAPH && keyStatus == STATUS_DAILY_BAR_GEAPH) ||
			   (status == STATUS_WEEK_BAR_GEAPH  && keyStatus == STATUS_WEEK_BAR_GEAPH)  ||
			   (status == STATUS_MONTH_BAR_GEAPH && keyStatus == STATUS_MONTH_BAR_GEAPH) || 
			   (status == STATUS_YEAR_BAR_GEAPH  && keyStatus == STATUS_YEAR_BAR_GEAPH)  ||
			   status == STATUS_RAW_WAVE){
				//棒グラフ表示かつキー状態がが棒グラフ
				//(キー状態がロールメニューのときはDBからの読み込みを行わないため)
				//または、次の生波形を表示するとき = STATUS_RAW_WAVE
				
				//生波形
				if(canvas.rollMenu.getIndex() != RollMenuRenderer.YEAR){
					//PLSのときは年グラフを表示しない
					return ReadDBData.TYPE_PLS;	
				}						
			}else{
				//棒グラフ
				if(canvas.rollMenu.getIndex() == RollMenuRenderer.DAY){
					return ReadNumOfDBData.TYPE_PLS_NUM;	
				}else if(canvas.rollMenu.getIndex() == RollMenuRenderer.WEEK){
					return ReadNumOfDBData.TYPE_WEEK_PLS_NUM;
				}else if(canvas.rollMenu.getIndex() == RollMenuRenderer.MONTH){
					return ReadNumOfDBData.TYPE_MONTH_PLS_NUM;
				}else if(canvas.rollMenu.getIndex() == RollMenuRenderer.YEAR){
					return ReadNumOfDBData.TYPE_YEAR_PLS_NUM;
				}	
			}			
		}else if(name.equals(TripleRotateMenuRenderer.BP)){
			//血圧		
			
			if((status == STATUS_DAILY_BAR_GEAPH && keyStatus == STATUS_DAILY_BAR_GEAPH) ||
			   (status == STATUS_WEEK_BAR_GEAPH  && keyStatus == STATUS_WEEK_BAR_GEAPH)  ||
			   (status == STATUS_MONTH_BAR_GEAPH && keyStatus == STATUS_MONTH_BAR_GEAPH) || 
			   (status == STATUS_YEAR_BAR_GEAPH  && keyStatus == STATUS_YEAR_BAR_GEAPH)  || 
			   status == STATUS_BPHR_WAVE){
				//棒グラフ表示かつキー状態がが棒グラフ
				//(キー状態がロールメニューのときはDBからの読み込みを行わないため)
				//または、次の生波形を表示するとき = STATUS_BPHR_WAVE
				
				//生波形
				if(canvas.rollMenu.getIndex() == RollMenuRenderer.DAY ||
				   canvas.rollMenu.getIndex() == RollMenuRenderer.WEEK || 
				   canvas.rollMenu.getIndex() == RollMenuRenderer.MONTH){
					
					return ReadDBData.TYPE_BP;										
				}else if(canvas.rollMenu.getIndex() == RollMenuRenderer.YEAR){
					return ReadDBData.TYPE_YEAR_BP;
				}
			}else{
				//棒グラフ
				if(canvas.rollMenu.getIndex() == RollMenuRenderer.DAY){
					return ReadNumOfDBData.TYPE_BP_NUM;	
				}else if(canvas.rollMenu.getIndex() == RollMenuRenderer.WEEK){
					return ReadNumOfDBData.TYPE_WEEK_BP_NUM;
				}else if(canvas.rollMenu.getIndex() == RollMenuRenderer.MONTH){
					return ReadNumOfDBData.TYPE_MONTH_BP_NUM;
				}else if(canvas.rollMenu.getIndex() == RollMenuRenderer.YEAR){
					return ReadNumOfDBData.TYPE_YEAR_BP_NUM;
				}
			}//End of if()
						
		}else if(name.equals(TripleRotateMenuRenderer.HR)){
			//心拍
			if((status == STATUS_DAILY_BAR_GEAPH && keyStatus == STATUS_DAILY_BAR_GEAPH) ||
			   (status == STATUS_WEEK_BAR_GEAPH  && keyStatus == STATUS_WEEK_BAR_GEAPH)  ||
			   (status == STATUS_MONTH_BAR_GEAPH && keyStatus == STATUS_MONTH_BAR_GEAPH) || 
			   (status == STATUS_YEAR_BAR_GEAPH  && keyStatus == STATUS_YEAR_BAR_GEAPH)  ||
			    status == STATUS_BPHR_WAVE){
				//棒グラフ表示かつキー状態がが棒グラフ
				//(キー状態がロールメニューのときはDBからの読み込みを行わないため)
				//または、次の生波形を表示するとき = STATUS_BPHR_WAVE
				
				//生波形
				if(canvas.rollMenu.getIndex() == RollMenuRenderer.DAY ||
				   canvas.rollMenu.getIndex() == RollMenuRenderer.WEEK || 
				   canvas.rollMenu.getIndex() == RollMenuRenderer.MONTH){
							
					return ReadDBData.TYPE_HR;										
				}else if(canvas.rollMenu.getIndex() == RollMenuRenderer.YEAR){
					return ReadDBData.TYPE_YEAR_HR;
				}					
			}else{
				//棒グラフ
				if(canvas.rollMenu.getIndex() == RollMenuRenderer.DAY){
					return ReadNumOfDBData.TYPE_HR_NUM;	
				}else if(canvas.rollMenu.getIndex() == RollMenuRenderer.WEEK){
					return ReadNumOfDBData.TYPE_WEEK_HR_NUM;
				}else if(canvas.rollMenu.getIndex() == RollMenuRenderer.MONTH){
					return ReadNumOfDBData.TYPE_MONTH_HR_NUM;
				}else if(canvas.rollMenu.getIndex() == RollMenuRenderer.YEAR){
					return ReadNumOfDBData.TYPE_YEAR_HR_NUM;
				}
			}//End of if()			
		}//End of if()
		return -1;
	}
	
	//===============================ReadDBListener===============================//
	
	/**
	 * DBからの読み込みが成功したときの呼ばれる 
	 */
	public void onCompleteReadDB(int[] data,int type,long time) {
		//波形表示画面へ移行
		System.out.println("#onCompleteReadDB@manager");

		//読み込み用スレッドを破棄
		if(readDB != null){
			readDB.stop();
			readDB = null;	
		}		
		//戻るコマンドを戻す
		canvas.addCommand(backCmd);		
		//読み込みフラグをオフに
		readDBFlag = false;
						
								
		if(type == ReadNumOfDBData.TYPE_ECG_NUM ||
		   type == ReadNumOfDBData.TYPE_PLS_NUM || 
		   type == ReadNumOfDBData.TYPE_BP_NUM  ||
		   type == ReadNumOfDBData.TYPE_HR_NUM ){
			//----------------棒グラフ(日)-----------------//
			System.out.println("棒グラフ(日)");
			status = STATUS_DAILY_BAR_GEAPH;						
			keyStatus = STATUS_ROLL_MENU;			
			
			rollmenuFlag = true;

			canvas.rollMenu.action(RollMenuRenderer.ACTIVE);
			canvas.rollMenu.setFocus(true);									
			
			//待ち画面の表示をやめる
			canvas.waitScreen.stop();		
			
		}else if(type == ReadNumOfDBData.TYPE_WEEK_ECG_NUM ||
				 type == ReadNumOfDBData.TYPE_WEEK_PLS_NUM || 
				 type == ReadNumOfDBData.TYPE_WEEK_BP_NUM ||
			     type == ReadNumOfDBData.TYPE_WEEK_HR_NUM ){
			//----------------棒グラフ(週)----------------//
			System.out.println("棒グラフ(週)");

			if(type == ReadNumOfDBData.TYPE_WEEK_ECG_NUM ||
			   type == ReadNumOfDBData.TYPE_WEEK_PLS_NUM ){
				//折れ線グラフ用データを取ってくる
				
				status    = STATUS_WEEK_BAR_GEAPH;
				keyStatus = STATUS_ROLL_MENU;;
					
				//メニューを表示
				rollmenuFlag = true;
				canvas.rollMenu.action(RollMenuRenderer.ACTIVE);
				canvas.rollMenu.setFocus(true);				
				
				//待ち画面の表示をやめる
				canvas.waitScreen.stop();	
				
			}else{
				//折れ線グラフ用のデータを取ってくる		
				
				//DBからの読み込みフラグを立てる
				readDBFlag = true;
				//各日の平均データの取得
				if(type == ReadNumOfDBData.TYPE_WEEK_BP_NUM){
					type = ReadDBData.TYPE_WEEK_BP_AVE;	
				}else{
					type = ReadDBData.TYPE_WEEK_HR_AVE;
				}		
				
				//DBからの読み込み		
				readDB = new ReadDBThread(type,2345,0,RawWaveRenderer.numOfDataReadDB,time);
				//読み込み完了時に呼ばれるリスナノ登録
				readDB.addListener(this);
				readDB.addListener(canvas.weekBarGraph);
				readDB.start();
				
				//待ち画面の表示
				canvas.waitScreen.action();
				//待ち画面状態へ
				keyStatus = STATUS_WAIT;
				//戻るコマンドの削除
				canvas.removeCommand(backCmd);
			}
			
			
		}else if(type == ReadNumOfDBData.TYPE_MONTH_ECG_NUM ||
				 type == ReadNumOfDBData.TYPE_MONTH_PLS_NUM || 
				 type == ReadNumOfDBData.TYPE_MONTH_BP_NUM ||
			     type == ReadNumOfDBData.TYPE_MONTH_HR_NUM ){
			//----------------棒グラフ(月)----------------//
			System.out.println("棒グラフ(月)");

			if(type == ReadNumOfDBData.TYPE_MONTH_ECG_NUM ||
			   type == ReadNumOfDBData.TYPE_MONTH_PLS_NUM ){
				//折れ線グラフは表示しない
				
				status    = STATUS_MONTH_BAR_GEAPH;
				keyStatus = STATUS_ROLL_MENU;;
					
				//メニューを表示
				rollmenuFlag = true;
				canvas.rollMenu.action(RollMenuRenderer.ACTIVE);
				canvas.rollMenu.setFocus(true);				
				
				//待ち画面の表示をやめる
				canvas.waitScreen.stop();	
				
			}else{
				//折れ線グラフ用データを取ってくる
				
				//DBからの読み込みフラグを立てる
				readDBFlag = true;
				//各日の平均データの取得
				if(type == ReadNumOfDBData.TYPE_MONTH_BP_NUM){
					type = ReadDBData.TYPE_MONTH_BP_AVE;	
				}else{
					type = ReadDBData.TYPE_MONTH_HR_AVE;
				}			
				
				//DBからの読み込み		
				readDB = new ReadDBThread(type,2345,0,RawWaveRenderer.numOfDataReadDB,time);
				//読み込み完了時に呼ばれるリスナノ登録
				readDB.addListener(this);
				readDB.addListener(canvas.monthBarGraph);
				readDB.start();
				
				//待ち画面の表示
				canvas.waitScreen.action();
				//待ち画面状態へ
				keyStatus = STATUS_WAIT;
				//戻るコマンドの削除
				canvas.removeCommand(backCmd);
				//待ち画面の表示
				canvas.waitScreen.action();
			}

						
		}else if(type == ReadNumOfDBData.TYPE_YEAR_ECG_NUM ||
				 type == ReadNumOfDBData.TYPE_YEAR_PLS_NUM || 
				 type == ReadNumOfDBData.TYPE_YEAR_BP_NUM ||
			     type == ReadNumOfDBData.TYPE_YEAR_HR_NUM ){
			//----------------棒グラフ(年)----------------//
			System.out.println("棒グラフ(年)");
			
			if(type == ReadNumOfDBData.TYPE_YEAR_ECG_NUM ||
				type == ReadNumOfDBData.TYPE_YEAR_PLS_NUM ){
				// 折れ線グラフは表示しない
						
				status    = STATUS_YEAR_BAR_GEAPH;
				keyStatus = STATUS_ROLL_MENU;;
						
				// メニューを表示
				rollmenuFlag = true;
				canvas.rollMenu.action(RollMenuRenderer.ACTIVE);
				canvas.rollMenu.setFocus(true);				
				
				// 待ち画面の表示をやめる
				canvas.waitScreen.stop();	
						
			}else{
				// DB空の読み込みフラグを立てる
				readDBFlag = true;
				// 各日の平均データの取得
				if (type == ReadNumOfDBData.TYPE_YEAR_BP_NUM) {
					type = ReadDBData.TYPE_YEAR_BP_AVE;
				} else {
					type = ReadDBData.TYPE_YEAR_HR_AVE;
				}

				// DBからの読み込み
				readDB = new ReadDBThread(type, 2345, 0,
						RawWaveRenderer.numOfDataReadDB, time);
				// 読み込み完了時に呼ばれるリスナノ登録
				readDB.addListener(this);
				readDB.addListener(canvas.yearBarGraph);
				readDB.start();

				// 待ち画面の表示
				canvas.waitScreen.action();
				// 待ち画面状態へ
				keyStatus = STATUS_WAIT;
				// 戻るコマンドの削除
				canvas.removeCommand(backCmd);
			}
		
		}else if(type == ReadDBData.TYPE_WEEK_BP_AVE || 
				 type == ReadDBData.TYPE_WEEK_HR_AVE ){
			//---------------折れ線グラフ(週)----------------//			
			System.out.println("折れ線グラフ(週)");
			
			status    = STATUS_WEEK_BAR_GEAPH;
			keyStatus = STATUS_ROLL_MENU;;
				
			//メニューを表示
			rollmenuFlag = true;
			canvas.rollMenu.action(RollMenuRenderer.ACTIVE);
			canvas.rollMenu.setFocus(true);				
			
			//待ち画面の表示をやめる
			canvas.waitScreen.stop();		
		}else if(type == ReadDBData.TYPE_MONTH_BP_AVE || 
				 type == ReadDBData.TYPE_MONTH_HR_AVE ){
			//---------------折れ線グラフ(月)----------------//
			System.out.println("折れ線グラフ(月)");
			
			status    = STATUS_MONTH_BAR_GEAPH;
			keyStatus = STATUS_ROLL_MENU;;
				
			//メニューを表示
			rollmenuFlag = true;
			canvas.rollMenu.action(RollMenuRenderer.ACTIVE);
			canvas.rollMenu.setFocus(true);
			
			//待ち画面の表示をやめる
			canvas.waitScreen.stop();		
			
		}else if(type == ReadDBData.TYPE_YEAR_BP_AVE || 
				 type == ReadDBData.TYPE_YEAR_HR_AVE ){
			//---------------折れ線グラフ(年)----------------//
			System.out.println("折れ線グラフ(年)");
			
			status    = STATUS_YEAR_BAR_GEAPH;
			keyStatus = STATUS_ROLL_MENU;;
				
			//メニューを表示
			rollmenuFlag = true;
			canvas.rollMenu.action(RollMenuRenderer.ACTIVE);
			canvas.rollMenu.setFocus(true);			
			
			//待ち画面の表示をやめる
			canvas.waitScreen.stop();		
			
		}else if(type == ReadDBData.TYPE_ECG ||
				 type == ReadDBData.TYPE_PLS ){
			//---------------ECGPLS波形表示画面-------------//
			System.out.println("#波形 ECGPLS");			
			
			status = STATUS_RAW_WAVE;
			keyStatus = STATUS_RAW_WAVE;
			
			//待ち画面の表示をやめる
			canvas.waitScreen.stop();		
			
		}else if(type == ReadDBData.TYPE_BP       || type == ReadDBData.TYPE_HR  ||						 
				 type == ReadDBData.TYPE_YEAR_BP  || type == ReadDBData.TYPE_YEAR_HR ){
			
			//-----------HR,BP波形表示画面-----------------//
			System.out.println("#波形 BPHR");			
			
			status = STATUS_BPHR_WAVE;
			keyStatus = STATUS_BPHR_WAVE;
			
			//待ち画面の表示をやめる
			canvas.waitScreen.stop();		
			
		}//End of if()
		
	}/* End of onCompleteReadDB() */
	
	/**
	 * DBからの読み込みが失敗したときの呼ばれる
	 */
	public void onErrorReadDB() {
		//回転メニュー画面へもどる
		System.out.println("#onErrorReadDB@manager");
		
		//待ち画面の表示をやめる
		canvas.waitScreen.stop();
		//キーを回転メニューへ
		keyStatus = STATUS_ROTATE_MENU;
		//戻るコマンドを戻す
		canvas.addCommand(backCmd);
				
		//読み込み用スレッドを破棄
		if(readDB != null){
			readDB.stop();
			readDB = null;					
		}		
		//読み込みフラグをオフに
		readDBFlag = false;
		
	}/* End of onErrorReadDB */	
}
