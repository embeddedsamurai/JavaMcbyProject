/*
 * 作成日: 2007/11/04
 *
 * Copyright 1993 - 2007 Embedded.Samurai, Inc. All rights reserved.
 *
 * $Id: SDCardTask.java,v 1.1 2007/11/22 06:51:08 esamurai Exp $
 */

package tsk;

import javax.microedition.io.Connector;

import mailbox.MailBoxControl;
import mailbox.Message;
import mailbox.MessageQueue;
import main.Main;

import sd.SDCardMIDP;
import util.DebugPrint;
import gui.BPMCanvas;

/**
 * 状態を表すクラス。
 * <br>
 * version $Revision: 1.1
 * @copyright 2007 Embedded.Samurai, Inc.
 * @author embedded.samurai <embedded.samurai@gmail.com>
 */
public class SDCardTSK extends Thread{

	/**
	 * デバックフラグを定義します。
	 * <br>
	 * TODO: リリース時には<code>false</code>にしてください。
	 */
	//sone1013
	public static final boolean DEBUG = true;
	private DebugPrint dp1=null;


	public static final int TRUE=1;
	public static final int FALSE=0;


	//--------------SDカード関連---------------//

	/** 心電図信号をとりだすＳＤカードのパス */
	private static final String ECG_SD_PATH ="ecg.txt";
	/** 脈波信号をとりだすＳＤカードのパス */
	private static final String PLS_SD_PATH ="dppg.txt";
	/** 一度に受信するデータ数 */
	private static final int SIZE_OF_RECEIVE_AT_A_TIME=50;

	/** 心電図データを読み込むためのSDCardオブジェクト */
	private SDCardMIDP ecgSD;
	/** 脈波データを読み込むためのSDCardオブジェクト   */
	private SDCardMIDP plsSD;

	//--------------メッセージ関連----------------//

	private Message sd_indata_msg  = null;
	private Message sd_outdata_msg = null;
	private Message sd_msg         = null;

	/** Message用 */
	private MessageQueue sd_msg_fifo       = null;
	/** Read用 */
	private MessageQueue sd_indata_fifo    = null;
	/** Write用 */
	private MessageQueue sd_outdata_fifo   = null;

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

	/** MailBoxControl (インスタンスは作らない) */
	private MailBoxControl mbx_con=null;

	/** Threadを破棄するために使うフラグ */
	boolean runFlag=true;

	/** 実行用スレッド */
	private Thread thread=null;

	/** SDCardTSKが起動するとtrueになる */
	private static boolean enable_sd_task=false;

	//sone1212
	/** Debug canvas */
	BPMCanvas canvas=null;

	/**
	 * コントローラ
	 * @param mbx_con　メールボックス制御
	 */
	public SDCardTSK(MailBoxControl mbx_con,BPMCanvas canvas){

		if(Main.DEBUG) dp1=new DebugPrint();

		//sone1213
		this.canvas = canvas;


		//メールボックスコントローラ
		this.mbx_con = mbx_con;

		// 心電図データを読み込むためのSDCardオブジェクト
		ecgSD = new SDCardMIDP(Main.IS_ACTUAL);
		// 脈波データを読み込むためのSDCardオブジェクト
		plsSD = new SDCardMIDP(Main.IS_ACTUAL);

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
		sd_msg_fifo     = new MessageQueue();
		sd_indata_fifo  = new MessageQueue();
		sd_outdata_fifo = new MessageQueue();

		//初期化
		init();
	}//end of BPTASK

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
		sd_msg_fifo.clear();
		sd_indata_fifo.clear();
		sd_outdata_fifo.clear();

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
		sd_outdata_fifo.putRequest(msg1);
		sd_outdata_fifo.putRequest(msg2);
		sd_outdata_fifo.putRequest(msg3);
		sd_outdata_fifo.putRequest(msg4);
		sd_outdata_fifo.putRequest(msg5);
		sd_outdata_fifo.putRequest(msg6);

	}//init

	/**
	 * SDカードの受信処理を開始する
	 */
	public void start(){
		//初期化処理
		init();
		//実行フラグをたてる
		runFlag=true;
		//スレッドの起動
		thread = new Thread(this);
		//実行の開始
		thread.start();
	}

	/**
	 * 心電図信号、脈波信号の受信処理を行う(SDカード)
	 */

	public static final int SLEEP_TIME = 1000;
	private static double sd_time=0;

	public void run() {
		try{
			//------------------データ受信の為の前処理---------------------//
			//ＳＤカードをオープン
			ecgSD.open(ECG_SD_PATH,Connector.READ);
			plsSD.open(PLS_SD_PATH,Connector.READ);

			//-------------------------受信処理-------------------------//

			//スリープ時間
			long startTime = System.currentTimeMillis();
			//計算にかかった時間
			long pastTime = 0;

			//受信処理
			while(runFlag){

				//メッセージのペンド
				sd_msg=mbx_con.MsgPend(mbx_con.MBX_sd,-1);

				startTime = System.currentTimeMillis();
				if(Main.DEBUG) dp1.StartTime1();

				switch(sd_msg.msg_id){

				case MailBoxControl.MSG_SD_PLAY:
					enable_sd_task=true;
					mbx_con.AckMsgPost(sd_msg);
				break;

				//使われたバッファが返却される
				case MailBoxControl.MSG_BPCALC_DATA_ACK:
					sd_outdata_fifo.putRequest(sd_msg);
				break;

				default:
					break;
				}

				if(enable_sd_task){

					while(true){
						//メッセージを取り出す
						sd_outdata_msg =sd_outdata_fifo.getRequest();
						//メッセージがないなら抜ける
						if(sd_outdata_msg == null) break;
						//データの読み込み
						getSDData(sd_outdata_msg.payload_double_ptr1,sd_outdata_msg.payload_double_ptr2);
						//SDカードから読み込んだデータをメッセージとしてBPCALC_DATAに送ります。
						mbx_con.WriteMsgPost(MailBoxControl.MSG_BPCALC_DATA,sd_outdata_msg,mbx_con.MBX_sd);
					};

				}//end of if(enable_sd_task)

				//計算にかかった時間
				pastTime = System.currentTimeMillis() - startTime;

				if(pastTime < SLEEP_TIME){
					//休止
					pause(SLEEP_TIME+5 - pastTime);
				}


				if(Main.DEBUG){
					dp1.EndTime1();
					sd_time=dp1.GetPeriodTime1();
					System.out.println("SDCardTSK spendtime="+sd_time);
					this.canvas.drawDBG("sdtsk="+Long.toString((long)sd_time));
				}
			}//end of while(runFlag)

			//SDカードを閉じる
			ecgSD.close();
			plsSD.close();

		}catch (Exception e) {
			e.printStackTrace();
		}

	}//end of run

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

	/** SDカードからデータを読み込んでバッファに格納する */
	void getSDData(double[] ecgData,double[] plsData){
		for(int i = 0; i < SIZE_OF_RECEIVE_AT_A_TIME; i++){
			try{
				double ecg = 0;
				double pls = 0;

				//心電図データの読み込み
				String ecgStr =new String(ecgSD.readLine());
				ecg = Double.parseDouble(ecgStr);

				//脈波データの読み込み
				String plsStr =new String(plsSD.readLine());
				pls = Double.parseDouble(plsStr);

				ecgData[i] = ecg;
				plsData[i] = pls;
			}catch (NumberFormatException e) {
				//もう一度先頭から読みなおし
				ecgSD.close();
				ecgSD.open(ECG_SD_PATH,Connector.READ);
				plsSD.close();
				plsSD.open(PLS_SD_PATH,Connector.READ);
			}
		}
	}//end of getSDData

	/**
	 * 実行の中止
	 */
	public void stop(){
		runFlag = false;
	}

	/**
	 * 実行中かどうかを返す
	 * @return 実行中かどうか
	 */
	public boolean isRunFlag() {
		return runFlag;
	}
}