package main;

import gui.BPMCanvas;
import gui.BluetoothCanvas;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;

import mailbox.MailBoxControl;
import mailbox.Message;

import request.Key;
import request.Request;
import request.RequestQueue;
import tsk.BPCalcTSK;
import tsk.BluetoothTSK;
import tsk.FFTTSK;
import tsk.HttpTSK;
import tsk.SDCardTSK;
import util.Option;

public class BPMonitorManager implements CommandListener{

	//=================================定数==================================//
	//--------------画面のモード----------------//
	/** 測定画面モード*/
	private static final int CURRENT_DISP  = 0;
	/** トレンド画面モード */
	private static final int TREND_DISP    = 1;
	/** スペクトル画面モード */
	private static final int SPECTRUM_ECG_DISP = 2;
	/** 現在のモード */
	private int mode = CURRENT_DISP;

	//-----------------リクエスト----------------//
	/**	心電図の表示モードを変更するリクエスト */
	private static final int ECG_MODE_CHANGE_REQ = 0;
	/**	x軸方向の縮小率を大きくするリクエスト */
	private static final int INCREMENT_X_REDUCTION_REQ = 1;
	/**	x軸方向の縮小率を小さくするリクエスト */
	private static final int DECREMENT_X_REDUCTION_REQ = 2;
	
	//--------------表示中の心電図--------------//
	/** 心電図を表す定数 */
	public static final int RAW_ECG = 0;
	/** ノッチフィルタをかけた心電図を表す定数 */
	public static final int NOTCH_ECG = 1;
	/** ノッチフィルタとハイパスフィルタをかけた心電図を表す定数 */
	public static final int HPF_ECG = 2;
	/** 表示する信号(デフォルトでは生の心電図) */
	public static int displayECGSignal = RAW_ECG;

	//=================================変数==================================//
	/** キャンバス */
	private BPMCanvas canvas;
	/** BT接続画面で使用するキャンバス */
	private BluetoothCanvas btCanvas;
	/** 親スレッド */
	private Main parent;
	/** 実行中か否かのフラグ*/
	private boolean runFlag = false;

	//------Currentモードの描画バッファ------//
	/** 心電図信号描画用バッファ */
	public static double[] ecgGraBuf;
	/** 脈波信号描画用バッファ */
	public static double[] plsGraBuf;
	/**	R波ピークを保持するリスト (R波を検出したときは1を入れる)*/
	public static int[] rWavePeekGraBuf;
	/**	ハイパスフィルタ適用心電図のR波ピークを保持するリスト
	 * (R波を検出したときは1を入れる)*/
	public static int[] hpfRWavePeekGraBuf;

	//-------Trendモードの描画バッファ-------//
	/** 心拍数描画用バッファ */
	public static double[] hrGraBuf ;
	/** 脈波伝搬時間描画用バッファ */
	public static double[] patGraBuf;
	/** 血圧値描画用バッファ */
	public static double[] sbpGraBuf;
	/** 現在の心拍数値*/
	public static int hr  = -1;
	/** 現在の血圧値 */
	public static double sbp = -1;
	/** 現在の脈波伝搬時間値 */
	public static int pat = -1;
	
	//--------------コマンド---------------//
	/** 停止コマンド */
	private Command stopCom;
	/** 開始コマンド */
	private Command startCom;
	/** 終了コマンド */
	private Command exitCom;
	/** 切替コマンド */
	private Command changeCom;

	/** リクエストを保持しておくキュー */
	private RequestQueue keyrequestQueue=null;

	/** 受け取り用（インスタンスを作らない）*/
	private Message hmsgin=null,hmsgout=null;

	/** MailBox司令塔(インスタンスを作る) */
	private MailBoxControl mbx_con=null;
	private State utState=null;

	/** バッファリング用タスク */
	private BPCalcTSK    bpcalcTSK=null;
	private BluetoothTSK btTSK =null; 
	private SDCardTSK    sdTSK=null;
	private FFTTSK       fftTSK=null;
	private HttpTSK      httpTSK=null;

	/** calcが終わったらBPCalcTSKがtrueにするフラグ */
	//計算していないときは常にtrue
	public static boolean bp_draw_on_flag=true;

	//==============================初期化処理================================//

	/**
	 * @param parent
	 */
	public BPMonitorManager(Main parent,MIDlet midlet) {
		try{
			this.parent = parent;

			//キャンバスの作成(はじめは測定画面)
			canvas = new BPMCanvas(midlet);
			//BTキャンバスの作成
			btCanvas = new BluetoothCanvas(midlet);
		
			//コマンドの作成
			stopCom = new Command("中断",Command.BACK,1);
			startCom = new Command("開始",Command.BACK,1);
			exitCom = new Command("終了",Command.BACK,0);
			changeCom = new Command("切替",Command.SCREEN,1);		
			//キャンバスにコマンドを追加
			canvas.addCommand(startCom);
			canvas.addCommand(changeCom);
			canvas.setCommandListener(this);

			//リクエストキュー
			keyrequestQueue = new RequestQueue();
			mbx_con         = new MailBoxControl();
		
			//計算を実行するタスク
			bpcalcTSK = new BPCalcTSK(mbx_con,canvas);
			//FFTを実行するタスク
			fftTSK    = new FFTTSK(mbx_con,canvas);
			//SDからのデータの読み込み
			sdTSK     = new SDCardTSK(mbx_con,canvas);
			//Bluetoothからのデータの読み込み
			btTSK     = new BluetoothTSK(mbx_con,canvas,btCanvas);
			//HTTPへのデータ送受信
			httpTSK   = new HttpTSK(mbx_con,canvas);

			//その他変数の初期化
			init();
		
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 変数の初期化処理
	 */
	public void init(){
		//キーを初期化
		Key.init();
		//リクエストキューを空に
		keyrequestQueue.clear();
	}

	//==============================実行制御================================//
	/**
	 * 開始
	 */
	public void start(){
		if(Main.DEBUG)System.out.println("BPMonitorManager#start()");

		//既に実行中のときは実行しない
		if(runFlag)return;

		//実行フラグを立てる
		runFlag = true;

		//データ受信の開始(新しいスレッドで実行)
		if(Option.getOp().isBluetooth()){
			//blue toothでの受信			 
			btTSK.start();
			//開始のメッセージをポスト
			mbx_con.ControlMsgPost(MailBoxControl.MSG_BT_PLAY, new Message(), mbx_con.MBX_main);
		}else{
			//SDカードの読み込みの開始
			sdTSK.start();
			//開始のメッセージをポスト
			mbx_con.ControlMsgPost(MailBoxControl.MSG_SD_PLAY, new Message(), mbx_con.MBX_main);
		}
		//計算の開始
		bpcalcTSK.start();
		//FFTの開始
		fftTSK.start();
		//HTTPタスクの開始
		httpTSK.start();
		
		//変数の初期化
		init();

		//開始コマンドを削除
		canvas.removeCommand(startCom);
		//中断、終了コマンドを追加
		canvas.removeCommand(exitCom);
		canvas.addCommand(stopCom);
	}

	/**
	 * 中断する
	 */
	public void stop(){
		if(Main.DEBUG)System.out.println("BPMonitorManager#stop()");
		
		//実行を止める
		runFlag = false;

		//実行を中止する						
		if(Option.getOp().isBluetooth()){
			//Bluetoothのとき
			btTSK.stop();
		}else{
			//SDカードのとき
			sdTSK.stop();	
		}
		bpcalcTSK.stop();
		fftTSK.stop();
		httpTSK.stop();
		
		//描画フラグをオンに(計算していないときは常にtrue)
		bp_draw_on_flag = true;
			
		//ストップコマンドを削除
		canvas.removeCommand(stopCom);
		//開始コマンド、終了コマンドを追加
		canvas.addCommand(startCom);
		canvas.addCommand(exitCom);
	}

	/**
	 * 終了する
	 */
	public void exit(){
		if(Main.DEBUG)System.out.println("BPMonitorManager#exit()");
		
		//タイトルモードに戻る
		parent.setMode(Main.TITLE_MODE);
	}


	//==============================メイン処理================================//
	/**
	 * 描画、計算などの一連の処理を行う
	 */
	public void process(){
		try {
			//キーを更新
			if(canvas.isShown())Key.registKeyEvent();
			//キー処理
			key();
			//リクエストの処理
			doRequest();
			//メッセージの処理
			returnAction();
			//描画			
			if(bp_draw_on_flag)draw();
			//計算実行中は描画フラグを毎回オフにする
			if(runFlag)bp_draw_on_flag=false;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void returnAction(){
		//メッセージの受取
		hmsgin = mbx_con.MsgPend(mbx_con.MBX_main,10);
		//メッセージがないならおしまい
		if (hmsgin==null) return;
		
		switch (hmsgin.msg_id)
		{
			case MailBoxControl.MSG_SD_PLAY_ACK: //SDからの読み込み開始
				System.out.println("MSG_SD_PLAY_ACK");
				runFlag = true;
				break;
			case MailBoxControl.MSG_BT_PLAY_ACK: //BTからの読み込み開始
				System.out.println("MSG_BT_PLAY_ACK");
				runFlag = true;
				break;
			default:
				break;
		}
	}

	/**
	 * リクエストの処理をする
	 */
	private void doRequest(){
		//キューからリクエストを取り出す
		Request req = keyrequestQueue.getRequest();

		if(req != null){
			//リクエストがあるとき
			switch (req.getCommand()) {
			case ECG_MODE_CHANGE_REQ:
				//表示する心電図を変更する
				displayECGSignal = (displayECGSignal+1)%3;
				break;
			case INCREMENT_X_REDUCTION_REQ:
				//x軸方向の縮小率を大きくする
				canvas.setXReductionRate(canvas.getXReductionRate()+1);
				break;
			case DECREMENT_X_REDUCTION_REQ:
				//x軸方向の縮小率を小さくする
				canvas.setXReductionRate(canvas.getXReductionRate()-1);
				break;
			}
		}
	}


	/**
	 * 描画処理
	 */
	private void draw() {
		
		if(!canvas.isShown())return; //表示中でない時は何もしない
		
		if(mode == CURRENT_DISP){
			//測定画面を描画
			if(displayECGSignal == HPF_ECG){
				canvas.drawCurrent(ecgGraBuf,plsGraBuf,hpfRWavePeekGraBuf,hr,pat,sbp);
			}else{
				canvas.drawCurrent(ecgGraBuf,plsGraBuf,rWavePeekGraBuf,hr,pat,sbp);
			}
		}else if(mode == TREND_DISP){
			//トレンド画面を描画
			canvas.drawTrend(hrGraBuf,patGraBuf,sbpGraBuf);
		}else if(mode == SPECTRUM_ECG_DISP){
			//心電図のスペクトル画面を描画
			if(displayECGSignal == HPF_ECG){
				canvas.drawECGSpectrum(ecgGraBuf,hpfRWavePeekGraBuf,hr);
			}else{
				canvas.drawECGSpectrum(ecgGraBuf,rWavePeekGraBuf,hr);
			}
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

	//==============================キー処理================================//
	/**
	 * コマンドを処理する
	 * @param com コマンド
	 * @param disp コマンドを保持するディスプレーオブジェクトの参照
	 *
	 */
	public void commandAction(Command com, Displayable disp) {
		if(Main.DEBUG)System.out.println("BPMonitorManager#commandAction()");
		
		if(!canvas.isShown())return; //表示中でない時はなにもしない
						
		if(com.equals(stopCom)){//中断コマンドを押したとき
 			stop();
 		}else if(com.equals(startCom)){//開始コマンドを押したとき
 			start();
 		}else if(com.equals(exitCom)){//終了コマンドを押したとき
 			exit();
 		}else if(com.equals(changeCom)){//切り替えコマンドを押したとき
 			modeChange();
 		}
	}

	/**
	 * キー処理をする
	 */
	private void key(){

		if(!canvas.isShown())return; //表示中でない時はなにもしない
		
		if(Key.isKeyPressed(Canvas.FIRE)){//決定キーを押したとき
			if(mode == CURRENT_DISP || mode == SPECTRUM_ECG_DISP){//カレントまたはスペクトル表示のとき
				//表示する心電図波形を変更する
				keyrequestQueue.putRequest(new Request(ECG_MODE_CHANGE_REQ));
			}
		}else if(Key.isKeyPressed(Canvas.LEFT)){//左キーを押したとき
			if(mode 	== CURRENT_DISP || mode == SPECTRUM_ECG_DISP){//カレントまたはスペクトル表示のとき
				//X方向の縮小率を大きく
				keyrequestQueue.putRequest(new Request(INCREMENT_X_REDUCTION_REQ));
			}
		}else if(Key.isKeyPressed(Canvas.RIGHT)){//右キーを押したとき
			if(mode == CURRENT_DISP || mode == SPECTRUM_ECG_DISP){//カレントまたはスペクトル表示のとき
				//X方向の縮小率を小さくする
				keyrequestQueue.putRequest(new Request(DECREMENT_X_REDUCTION_REQ));
			}
		}//End of if					
	}//End of key()
	
	//===============================その他================================//

	/**
	 * 表示する
	 */
	public void setDisplay(MIDlet midlet){
		if(Main.DEBUG)System.out.println("BPMonitorManager#setDisplay()");
		
		((BPMCanvas)canvas).setDisplay();		
	}

	/**
	 * モードの変更
	 */
	private void modeChange(){
		if(Main.DEBUG)System.out.println("BPMonitorManager#modeChange()");
		
		if(mode == CURRENT_DISP){
			//スペクトル画面へ変更
			mode = SPECTRUM_ECG_DISP;
 		}else if(mode == SPECTRUM_ECG_DISP){
 			//トレンド画面へ変更
 			mode = TREND_DISP;
 		}else if(mode == TREND_DISP){
 			//カレント画面へ変更
 			mode = CURRENT_DISP;
 		}
	}

}