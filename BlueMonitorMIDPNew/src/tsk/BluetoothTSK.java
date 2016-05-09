package tsk;

import gui.BPMCanvas;
import gui.BluetoothCanvas;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.bluetooth.DataElement;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

import mailbox.MailBoxControl;
import mailbox.Message;
import mailbox.MessageQueue;
import main.Main;

import request.Key;
import request.Request;
import request.RequestQueue;
import sd.SDCardMIDP;
import util.ByteQueue;
import util.DebugPrint;
import util.Option;


public class BluetoothTSK extends Thread implements DiscoveryListener,CommandListener{

	//=======================定数==========================//
	/** データ受信間隔 */
	private static final int RECEIVE_INTERVAL = 50;
	/** 一度に受信するデータ数 */
	private static final int SIZE_OF_RECEIVE_AT_A_TIME = 50;
	/** キューのサイズ */
	private static final int SIZE_OF_QUEUE = 3000;

	//BTサービス、デバイス
	/** マウス型マクビーのデバイス名を表す */
	private static final String DEVICE_NAME_MCBY1 = "BiTEK";
	/** マウス型マクビーのサービス名を表す */
	private static final String SERVICE_NAME_MCBY1 = "Dev B";
	/** ZEAL版マクビーのデバイス名を表す */
	private static final String DEVICE_NAME_MCBY2 = "Zeal";
	/** ZEAL版マクビーのサービス名を表す */
	private static final String SERVICE_NAME_MCBY2 = "SerialPort";

	//BT接続状況
	public final static int BT_IDLE               = 0;  // 未接続
	public final static int BT_DEVICE_SEARCH      = 1;  // デバイス検索中
	public final static int BT_DEVICE_DISCOVERED  = 2;  // デバイス検索完了
	public final static int BT_SERVICE_SEARCH     = 3;  // サービス検索中
	public final static int BT_SERVICE_DISCOVERED = 4;  // サービス検索完了
	public final static int BT_CONNECTED          = 5;  // 接続中
	public final static int BT_ERROR              = 6;  //エラー

	//キー関連
	/** 一覧表示中に選択項目を一つ上にする */
	private static final int INCRESE_SELECTION = 0;
	/** 一覧表示中に選択項目を一つ下にする */
	private static final int DECREASE_SELECTION = 1;
	/** 一覧表示中に選択項目に接続する*/
	private static final int CONNECT_SELECTED_ITEM = 2;

	//=======================変数==========================//
	/** メールボックス制御オブジェクト */
	private MailBoxControl mbx_con;

	/** 実行フラグ */
	private boolean runFlag = false;
	/** メインキャンバスへの参照 */
	private BPMCanvas canvas;
	/** Bluetooth用キャンバス */
	private BluetoothCanvas btCanvas;

	/** btCanvasに表示するメッセージ*/
	private String btMessage = "";
	private String errMessage = "";

	/** 接続可能なBluetoothデバイス/サービスを保持する配列 */
	private String[] menu;
	/** 接続可能なBluetoothデバイス/サービスなどを選択するメニューのインデックス*/
	private int menuIndex = 0;

	/** BT接続で得られたデータを保存しておくバッファ */
	private ByteQueue queue;
	/** 受信したデータを半分にするフラグ*/
	private boolean bypathFlag = true;

	/** リクエストを保持するキュー*/
	private RequestQueue reqQueue;

	/** 中断コマンド*/
	private Command exitCom;
	/** サービス検索を中断する際に必要になるID */
	private int transID = 0;

	//---------------BT接続関連--------------//
	/** Bluetooth接続の入力ストリーム*/
	private InputStream btIn;
	/** Bluetooth接続の出力ストリーム*/
	private OutputStream btOut;
	/** Bluetoothのコネクション*/
	protected StreamConnection conn = null;

	/** ステータス*/
	protected int status = BT_IDLE;

	/** 発見したデバイスのためのマップ */
	protected Hashtable devices = new Hashtable();
	/** 発見したデバイスのためのサービス */
	protected Hashtable services = new Hashtable();

	/** UUID */
	protected static final UUID[] UUID_SET = {new UUID("0003",true)};
	/** ATR SET (サービス名) */
	protected static int[] ATTR_SET = {0x0100};

	/** デバイス*/
	protected static LocalDevice localDevice;
	/** Bluetoothデバイス、サービス検索のためのエージェント */
	protected static DiscoveryAgent agent = null;
	/** デバッグプリント */
	private DebugPrint dp4=null;

	//--------------メッセージ関連---------------//
	private Message bt_indata_msg  = null;
	private Message bt_outdata_msg = null;
	private Message bt_msg         = null;

	/** Message用 */
	private MessageQueue bt_msg_fifo       = null;
	/** Read用 */
	private MessageQueue bt_indata_fifo    = null;
	/** Write用 */
	private MessageQueue bt_outdata_fifo   = null;

	/** BPCalcTSK に渡すデータを入れる */
	private Message msg1=null;
	private Message msg2=null;
	private Message msg3=null;
	private Message msg4=null;
	private Message msg5=null;
	private Message msg6=null;

	/** 心電図受信バッファ */
	private double[] ecg_buf1=null;
	private double[] ecg_buf2=null;
	private double[] ecg_buf3=null;
	private double[] ecg_buf4=null;
	private double[] ecg_buf5=null;
	private double[] ecg_buf6=null;

	private double[] pls_buf1=null;
	private double[] pls_buf2=null;
	private double[] pls_buf3=null;
	private double[] pls_buf4=null;
	private double[] pls_buf5=null;
	private double[] pls_buf6=null;

	/** SDCardTSKが起動するとtrueになる */
	private static boolean enable_bt_task=false;
	
	private SDCardMIDP dbgSD3;
	private static final String DBG_SD_PATH3 ="dbg3.txt";

	//======================メソッド=========================//
	/**
	 * @param mbx_con メールボックス制御
	 */
	public BluetoothTSK(MailBoxControl mbx_con,BPMCanvas canvas,BluetoothCanvas btCanvas) {
		this.mbx_con = mbx_con;
		this.canvas  = canvas;
		this.btCanvas = btCanvas;

		//BTキャンバスのリスナとして設定
		btCanvas.setCommandListener(this);
		//コマンド
		exitCom = new Command("中断",Command.SCREEN,0);
		btCanvas.addCommand(exitCom);

		//心電図データバッファの作成
		ecg_buf1=new double[SIZE_OF_RECEIVE_AT_A_TIME];
		ecg_buf2=new double[SIZE_OF_RECEIVE_AT_A_TIME];
		ecg_buf3=new double[SIZE_OF_RECEIVE_AT_A_TIME];
		ecg_buf4=new double[SIZE_OF_RECEIVE_AT_A_TIME];
		ecg_buf5=new double[SIZE_OF_RECEIVE_AT_A_TIME];
		ecg_buf6=new double[SIZE_OF_RECEIVE_AT_A_TIME];

		//脈波データバッファの作成
		pls_buf1=new double[SIZE_OF_RECEIVE_AT_A_TIME];
		pls_buf2=new double[SIZE_OF_RECEIVE_AT_A_TIME];
		pls_buf3=new double[SIZE_OF_RECEIVE_AT_A_TIME];
		pls_buf4=new double[SIZE_OF_RECEIVE_AT_A_TIME];
		pls_buf5=new double[SIZE_OF_RECEIVE_AT_A_TIME];
		pls_buf6=new double[SIZE_OF_RECEIVE_AT_A_TIME];

		//メッセージの作成
		msg1 = new Message();
		msg2 = new Message();
		msg3 = new Message();
		msg4 = new Message();
		msg5 = new Message();
		msg6 = new Message();

		//メッセージキューの作成
		bt_msg_fifo     = new MessageQueue();
		bt_indata_fifo  = new MessageQueue();
		bt_outdata_fifo = new MessageQueue();

		//BT経由で得られたデータを保存しておくバッファ
		queue = new ByteQueue(SIZE_OF_QUEUE);

		//リクエストを保持するキュー
		reqQueue = new RequestQueue();
		
		dp4 = new DebugPrint();

		//初期化処理
		init();
	}

	/**
	 * 初期化処理
	 */
	public void init(){

		//心電図、脈波のためのバッファの初期化
		for(int i=0;i<SIZE_OF_RECEIVE_AT_A_TIME;i++){
			ecg_buf1[i]=0;
			ecg_buf2[i]=0;
			ecg_buf3[i]=0;
			ecg_buf4[i]=0;
			ecg_buf5[i]=0;
			ecg_buf6[i]=0;

			pls_buf1[i]=0;
			pls_buf2[i]=0;
			pls_buf3[i]=0;
			pls_buf4[i]=0;
			pls_buf5[i]=0;
			pls_buf6[i]=0;
		}

		//メッセージキューの初期化
		bt_msg_fifo.clear();
		bt_indata_fifo.clear();
		bt_outdata_fifo.clear();

		//メッセージの初期化
		msg1.clear();
		msg2.clear();
		msg3.clear();
		msg4.clear();
		msg5.clear();
		msg6.clear();

		//メッセージの設定
		msg1.payload_double_ptr1 = ecg_buf1;
		msg2.payload_double_ptr1 = ecg_buf2;
		msg3.payload_double_ptr1 = ecg_buf3;
		msg4.payload_double_ptr1 = ecg_buf4;
		msg5.payload_double_ptr1 = ecg_buf5;
		msg6.payload_double_ptr1 = ecg_buf6;

		msg1.payload_size1 = SIZE_OF_RECEIVE_AT_A_TIME;
		msg2.payload_size1 = SIZE_OF_RECEIVE_AT_A_TIME;
		msg3.payload_size1 = SIZE_OF_RECEIVE_AT_A_TIME;
		msg4.payload_size1 = SIZE_OF_RECEIVE_AT_A_TIME;
		msg5.payload_size1 = SIZE_OF_RECEIVE_AT_A_TIME;
		msg6.payload_size1 = SIZE_OF_RECEIVE_AT_A_TIME;

		msg1.payload_double_ptr2 = pls_buf1;
		msg2.payload_double_ptr2 = pls_buf2;
		msg3.payload_double_ptr2 = pls_buf3;
		msg4.payload_double_ptr2 = pls_buf4;
		msg5.payload_double_ptr2 = pls_buf5;
		msg6.payload_double_ptr2 = pls_buf6;

		msg1.payload_size2 = SIZE_OF_RECEIVE_AT_A_TIME;
		msg2.payload_size2 = SIZE_OF_RECEIVE_AT_A_TIME;
		msg3.payload_size2 = SIZE_OF_RECEIVE_AT_A_TIME;
		msg4.payload_size2 = SIZE_OF_RECEIVE_AT_A_TIME;
		msg5.payload_size2 = SIZE_OF_RECEIVE_AT_A_TIME;
		msg6.payload_size2 = SIZE_OF_RECEIVE_AT_A_TIME;

		//キューにデータを入れる
		bt_outdata_fifo.putRequest(msg1);
		bt_outdata_fifo.putRequest(msg2);
		bt_outdata_fifo.putRequest(msg3);
		bt_outdata_fifo.putRequest(msg4);
		bt_outdata_fifo.putRequest(msg5);
		bt_outdata_fifo.putRequest(msg6);

		queue.clear();
		reqQueue.clear();
		Key.init();
		menuIndex = 0;

	}//End of init

	/**
	 * 実行を開始する
	 */
	public void start() {
		//画面をBT接続画面に変更
		btCanvas.setDisplay();
		//スレッドを起動
		Thread th = new Thread(this);
		th.start();
		//実行フラグを立てる
		runFlag = true;
	}//End of Start

	/**
	 * 実行の中止
	 */
	public void stop(){
		disconnect();
		runFlag = false;
		enable_bt_task=false;
	}

	//=========================メインの処理===========================//

	//開始時間
	private long startTime = 0;
	
	/**
	 * タスクの実行
	 */
	public void run() {
		
		if(Main.DEBUG){
			//デバッグ用SDの作成
			dbgSD3 = new SDCardMIDP(Main.IS_ACTUAL);
			dbgSD3.open(DBG_SD_PATH3,Connector.READ_WRITE);
		}

		//開始時間
		startTime = System.currentTimeMillis();
		//計算にかかった時間
		long pastTime = 0;

		//接続処理の開始
		System.out.println("device search");
		try{
			//ローカルのBluetoothデバイスのオブジェクトを得る
			localDevice = LocalDevice.getLocalDevice();
			//デバイス、サービス検索の為のエージェント
			agent = localDevice.getDiscoveryAgent();
			//デバイス検索の開始
			agent.startInquiry(DiscoveryAgent.GIAC, this);
			//デバイス検索状態へ遷移
			status = BT_DEVICE_SEARCH;
		}catch (Exception e) {
			errMessage = "error@run()#startInquiry()";
			System.out.println("error@run()#startInquiry()");
			e.printStackTrace();
			status = BT_ERROR;
		}

		while(runFlag){
			try{
				if(btCanvas.isShown()){//キャンバスを表示中のみ
					//キー処理の登録
					Key.registKeyEvent();
					//キー処理
					key();
					//リクエストの処理
					doRequest();
					//BTキャンバスの描画
					draw();
					System.out.println("draw()");
				}

				if(status == BT_CONNECTED){//接続が完了しているとき
					//データの受信処理
					receive();
				}

				//処理ににかかった時間
				pastTime = System.currentTimeMillis() - startTime;
				if(pastTime < RECEIVE_INTERVAL){
					//休止
					pause(RECEIVE_INTERVAL+5 - pastTime);
				}
				startTime = System.currentTimeMillis();

				if(Main.DEBUG && enable_bt_task){
					dp4.EndTime1();
					double time =dp4.GetPeriodTime1();
					if(Main.DEBUG){
						dbgSD3.write(("bttsk," + time +"\r\n").getBytes());
						dbgSD3.flush();
					}					
					this.canvas.drawDBG("btTSK="+Long.toString((long)time));
				}
			}catch (Exception e) {
				status = BT_ERROR;
				errMessage = "error@run()";
				e.printStackTrace();
			}//End of try
		}//End of while
	}//End of run

	/**
	 * 描画処理
	 */
	private void draw(){

		if(Option.getOp().isBluetoothViewList()){
			//一覧表示から選択するモードのとき
			switch (status) {

			//BTデバイスを検索中
			case BT_DEVICE_SEARCH:
				//接続状況を表示
				btCanvas.drawMessage(new String[]{"デバイス検索中",devices.size() + "件発見"});
			break;

			//BTデバイスが見つかったとき
			case BT_DEVICE_DISCOVERED:
				//デバイス一覧を表示
				btCanvas.drawViewList("デバイスを選択してください",menu, menuIndex);
			break;

			//BTサービスを検索中
			case BT_SERVICE_SEARCH:
				//接続状況を表示
				btCanvas.drawMessage(new String[]{"サービス検索中",services.size() + "件発見"});
			break;

			//BTサービスが見つかったとき
			case BT_SERVICE_DISCOVERED:
				//サービス一覧を表示
				btCanvas.drawViewList("サービスを選択してください",menu, menuIndex);
			break;

			//未接続
			case BT_IDLE:
				btCanvas.drawMessage(new String[]{"未接続"});
			break;

			//エラー
			case BT_ERROR:
				btCanvas.drawMessage(new String[]{"エラー",errMessage});
			break;

			//エラー
			case BT_CONNECTED:
				btCanvas.drawMessage(new String[]{"接続中",errMessage});
			break;

			}//End of switch

		}else{//End of if
			//自動接続モードのとき

		}

	}//End of draw

	/**
	 * 受信処理を行う
	 */
	private void receive(){
		//メッセージのペンド
		bt_msg=mbx_con.MsgPend(mbx_con.MBX_bt,-1);
		//開始時間
		startTime = System.currentTimeMillis();
		
		if(Main.DEBUG)dp4.StartTime1();
		switch(bt_msg.msg_id){

			//実行を開始したことを知らせる
			case MailBoxControl.MSG_BT_PLAY:
				enable_bt_task=true;
				mbx_con.AckMsgPost(bt_msg);
			break;

			//使われたバッファが返却される
			case MailBoxControl.MSG_BPCALC_DATA_ACK:
				bt_outdata_fifo.putRequest(bt_msg);
			break;

			default:
			break;

		}//End of switch

		if(enable_bt_task){
			while(true){
				//メッセージを取り出す
				bt_outdata_msg =bt_outdata_fifo.getRequest();
				//メッセージがないなら抜ける
				if(bt_outdata_msg == null) break;
				//データの読み込み
				getBluetoothData(bt_outdata_msg.payload_double_ptr1,bt_outdata_msg.payload_double_ptr2);
				mbx_con.WriteMsgPost(MailBoxControl.MSG_BPCALC_DATA,bt_outdata_msg,mbx_con.MBX_bt);

			}//End of while
		}//End of if(enable_sd_task)
	}

	/**
	 * BT接続でデータを取得し、バッファに格納する
	 *
	 * @param ecgData ECGバッファ
	 * @param plsDat  PLSバッファ
	 *
	 * @return データをバッファに格納したどうか。 true: １つでも格納した false:1つも格納できなかった
	 */
	private void getBluetoothData(double[] ecgData,double[] plsData){

		try {
			//読み込んだデータの一時保存先
			//500hzで読みこんで半分のデータが50個(ECG,PLS)まで読み込み
			byte buf[] = new byte[SIZE_OF_RECEIVE_AT_A_TIME*6*2];
			int inputsize=SIZE_OF_RECEIVE_AT_A_TIME*6*2;

			do{

				//バッファにデータを格納
				int tmpCounter = btIn.read(buf, 0, inputsize);
				if(tmpCounter == -1) continue;

				//受信したデータをエンキュー
				for(int i = 0; i < tmpCounter; i++){
					try{
						queue.enque(buf[i]);
					}catch(ByteQueue.OverflowIntQueueException e){
						System.out.println("Excep at ByteQueue:DataOverFlow");
					}
				}//end of enqueue

				inputsize = inputsize - tmpCounter;

			}while(inputsize != 0);


			//１つの心電脈波データのかたまり
			byte[] tmpData=new byte[6];
			int index=0;

			//キューのデータを計算処理に渡すECG,PLSバッファに格納
			while(true){

				//キューから読みだせる最大数
				int readableSize=queue.size();				
				if(Main.DEBUG) System.out.println("readableSize="+readableSize);

				//break条件
				if(readableSize==0) break;

				//キューからのかたまりの取り出し
				for(int i = 0; i < 6 ;i++){
					try{
						tmpData[i] = queue.deque();
					}catch(ByteQueue.EmptyIntQueueException e){
						System.out.println("Que is empty!");
					}
				}//end of for

				// 受信したデータから心電図と脈波を分離して格納する
				// 250Hzにするため1つ飛ばしでデータを格納する
				if (bypathFlag) {
					int ecg = ((tmpData[2]&0x3)<<8) + (tmpData[3]&0xFF);
					int pls = ((tmpData[4]&0x3)<<8) + (tmpData[5]&0xFF);

					if(ecg >= 1024) ecg=1024;
					if(pls >= 1024) pls=1024;

					ecgData[index] = (double)ecg;
				 	plsData[index] = (double)pls;
				 	index++;
				}//byperFlag

				 bypathFlag = !bypathFlag;

			}//end of while(true)
			
		} catch (Exception e) {
			errMessage ="getBluetoothData() error";
			System.out.println("BluetoothTSK#getBluetoothData() error");
			e.printStackTrace();
		}//End of Try
	}//End of getBluetoothData

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

	/**
	 * リクエストの処理をする
	 */
	private void doRequest(){
		//キューからリクエストを取り出す
		Request req = reqQueue.getRequest();

		if(req != null){
			//リクエストがあるとき
			switch (req.getCommand()) {
				case INCRESE_SELECTION:
					//選択項目を一つ上へ
					if(menuIndex > 0)menuIndex--;
					break;
				case DECREASE_SELECTION:
					//選択項目を一つ下へ
					if(menuIndex < menu.length -1)menuIndex++;
					break;
				case CONNECT_SELECTED_ITEM:
					if(status == BT_DEVICE_DISCOVERED){
						//選択されているデバイスへ接続
						try {
							transID = agent.searchServices(ATTR_SET, UUID_SET,(RemoteDevice)devices.get(menu[menuIndex]),this);
							status = BT_SERVICE_SEARCH;
							menuIndex = 0;
						} catch (Exception e) {
							status = BT_ERROR;
							errMessage ="サービス検索失敗";
						}
					}else if(status == BT_SERVICE_DISCOVERED){
						//選択されているサービスへ接続
						connect();
						menuIndex = 0;
					}
					break;
			}
		}
	}

	/**
	 * BTサービスに接続する
	 */
	private void connect(){
		try{
			//URL
			String url = ((ServiceRecord)services.get(menu[menuIndex])).getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
			//コネクションを確立する
			conn = (StreamConnection)Connector.open(url);
			//接続状態へ
			status = BT_CONNECTED;
			//インプットストリームを得る
			btIn = conn.openInputStream();
			btOut = conn.openOutputStream();

			//データ送受信の為の合図を送る
			if(Option.getOp().isMCBY()){
				btOut.write(util.Protocol.CMD_START);
				btOut.flush();
			}else{
				btOut.write('X');
				btOut.flush();
			}
			//表示を変更
			canvas.setDisplay();
		} catch(IllegalArgumentException e){
			status = BT_ERROR;
			errMessage ="接続に失敗";
		} catch(ConnectionNotFoundException e){
			status = BT_ERROR;
			errMessage ="接続に失敗";
		} catch(IOException e){
			status = BT_ERROR;
			errMessage ="接続に失敗";
		}
	}

	/**
	 * BT接続/接続処理を切断する
	 */
	public void disconnect(){
		try{
			if(btIn != null){
				btIn.close();
				btIn = null;
			}
			if(btOut != null){
				btOut.close();
				btOut = null;
			}
			if(conn != null){
				conn.close();
				conn = null;
			}
			if(status == BT_DEVICE_SEARCH){
				agent.cancelInquiry(this);
			}else if(status == BT_SERVICE_SEARCH){
				agent.cancelServiceSearch(transID);
			}

		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	//===========================キー、コマンド処理=============================//
	/**
	 * キー処理
	 */
	private void key(){

		if(status == BT_DEVICE_DISCOVERED || status == BT_SERVICE_DISCOVERED){
			//デバイス/サービス一覧表示状態のとき
			if(Key.isKeyPressed(Canvas.UP)){

				//上キーを押したとき 選択している項目を一つ上へ
				reqQueue.putRequest(new Request(INCRESE_SELECTION));

			}else if(Key.isKeyPressed(Canvas.DOWN)){

				//下キーを押したとき 選択している項目を一つ下へ
				reqQueue.putRequest(new Request(DECREASE_SELECTION));

			}else if(Key.isKeyPressed(Canvas.FIRE)){

				//決定キーを押したとき 現在選択中のデバイス・サービスへ接続する
				reqQueue.putRequest(new Request(CONNECT_SELECTED_ITEM));

			}//End of if
		}//End of if(status == BT_DEVICE_DISCOVERED || status == BT_SERVICE_DISCOVERED)
	}//End of key()

	/**
	 * コマンドを処理する
	 *
	 * @param com  コマンド
	 * @param disp コマンドが発生したディスプレイ
	 */
	public void commandAction(Command com, Displayable disp) {
		if(!disp.equals(btCanvas))return; //BTキャンバスのときのみ処理

		if(com.equals(exitCom)){
			//中断
			disconnect();
			//画面を戻す
			canvas.setDisplay();
		}
	}

	//=======================DiscoveryListenner関連=========================//
	/**
	 * BTデバイスを発見しときに呼ばれる
	 */
	public void deviceDiscovered(RemoteDevice rd, DeviceClass arg1) {
		try{
			devices.put(rd.getFriendlyName(false), rd);
		} catch(IOException e){
			status = BT_ERROR;
			errMessage = "デバイス検索失敗";
			System.out.println("Get Device Name Error: " + e.toString());
			e.printStackTrace();
		}
	}
	/**
	 * デバイス検索が完了したときに呼ばれる
	 */
	public void inquiryCompleted(int type) {

		if(type == DiscoveryListener.INQUIRY_COMPLETED){

			if(Option.getOp().isBluetoothViewList()){
				//デバイスを一覧表示して選択させる
				//デバイスの列挙
				Enumeration enm = devices.keys();

				if(!enm.hasMoreElements()){
					//1つもデバイスがなかったとき
					status = BT_ERROR;
					errMessage = "デバイスが見つかりません";
					return;
				}
				menu = new String[devices.size()];
				int i = 0;
				while(enm.hasMoreElements()){
					//デバイス名を表示用のリストへ格納
					menu[i++]= (String)enm.nextElement();
				}

				//デバイスを一覧表示するモードへ変更
				status = BT_DEVICE_DISCOVERED;
			}else{
				//該当するデバイスを自動選択

				//接続先デバイス
				String name = (Option.getOp().isMCBY())? DEVICE_NAME_MCBY1:DEVICE_NAME_MCBY2;

				//デバイス検索が完了したとき
				//デバイスの列挙
				Enumeration enm = devices.keys();

				//発見したデバイスと一致するものがあるか検索
				while(enm.hasMoreElements()){
					if(name.equals((String)enm.nextElement())){
						//発見したとき
						status = BT_DEVICE_DISCOVERED;
						try{
							//サービス検索の開始
							agent.searchServices(ATTR_SET,UUID_SET,(RemoteDevice)devices.get(name),this);
						}catch (Exception e) {
							e.printStackTrace();
						}
						return;
					}
				}//End of While

				//デバイスを発見できなかった
				status = BT_ERROR;

			}//End of If(Option.getOp().isBluetoothViewList())
		}else{//End of if(type == DiscoveryListener.INQUIRY_COMPLETED)
			//それ以外はエラー
			status = BT_ERROR;
		}//End of else(type == DiscoveryListener.INQUIRY_COMPLETED)
	}//End of inquiryCompleted

	/**
	 * サービスを発見したときに呼ばれる
	 */
	public void servicesDiscovered(int arg0, ServiceRecord[] record) {
		for(int i=0; i < record.length; i++){
			DataElement serviceNameElement = record[i].getAttributeValue(ATTR_SET[0]);
			String serviceName = (String)serviceNameElement.getValue();
			services.put(serviceName, record[i]);
		}
	}

	/**
	 * サービス検索が終了したときに呼ばれる
	 */
	public void serviceSearchCompleted(int arg0, int arg1) {

		if(Option.getOp().isBluetoothViewList()){
			//一覧表示モードのとき

			//サービスを一覧表示して選択させる
			//サービスの列挙
			Enumeration enm = services.keys();

			if(!enm.hasMoreElements()){
				//1つもデバイスがなかったとき
				status = BT_ERROR;
				errMessage = "サービスが見つかりません";
				return;
			}
			menu = new String[services.size()];
			int i = 0;
			while(enm.hasMoreElements()){
				//デバイス名を表示用のリストへ格納
				menu[i++]= (String)enm.nextElement();
			}
			//サービスを一覧表示するモードへ変更
			status = BT_SERVICE_DISCOVERED;
		}else{
			//自動接続モードのとき
		}//End of if
	}
}
