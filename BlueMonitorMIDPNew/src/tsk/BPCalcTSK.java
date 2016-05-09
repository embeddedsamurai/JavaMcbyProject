/*
 * 作成日: 2008/12/02
 *
 * $Id: BPCalcTSK.java,v 1.1 2008/12/2 06:51:08 shusaku sone Exp $
 */

package tsk;

import java.util.Vector;

import filter.BPEstimationFilter_BAK;
import filter.BPEstimationFilter;
import filter.FIRFilter;
import filter.HighPassFilter;
import filter.IIRFilterDirectI;
import filter.IIRFilterDirectIISOS;
import filter.NotchFilter;

import gui.BPMCanvas;

import javax.microedition.io.Connector;

import sd.SDCardMIDP;
import util.DebugPrint;
import util.Option;
import util.PAT_HR;

import mailbox.MailBoxControl;
import mailbox.Message;
import mailbox.MessageQueue;
import main.BPMonitorManager;
import main.Main;


/**
 * BPCalcTSKを表すクラス。
 * <br>
 *
 * @version $Revision: 1.1 $
 * @author shusaku sone <embedded.samurai@gmail.com>
 */
public class BPCalcTSK extends Thread{

	private DebugPrint dp2=null;

	/** サンプリングレート */
	public static final int SAMPLE = 250;

	//---------------バッファサイズ----------------//
	/** 計算用バッファサイズ */
	private static final int CALC_BUF_SIZE = SAMPLE*5;

	//-------------データ受信関連---------------//
	/** １分間のデータ数 */
	private static final int NUM_OF_DATA_PER_MIN = SAMPLE*60;
	/** １度に受信するデータ数 */
	private static final int SIZE_OF_RECEIVE_AT_A_TIME = 50;
	/** データ受信のタイミング(単位はMSEC)*/
	private static final int RECEIVE_INTERVAL = (1000/SAMPLE)*SIZE_OF_RECEIVE_AT_A_TIME;
	/** 最大のリングバッファサイズ(受信用) */
	private static final int MAX_RECV_RINGBUF_SIZE = 5000;


	/** 最初に閾値をきめるときに信号の最大値にこの値を掛けたものを閾値とする*/
	private static final double THRESHOLD_COFF = 0.7;

	//---------心電図R波ピーク検出のモード----------//
	/** 閾値の検出中であることを示す定数 */
	private static final int THRESHOLD_DETECT_MODE = 0;
	/** フィルタ適用心電図のR波のピークの検出中であることを示す定数 */
	private static final int FILTERED_ECG_R_DETECT_MODE = 1;

	/** RRインターバル用カウンタがこの値を超えたら閾値をリセットする */
	private static final int MAX_RR_COUNTER = SAMPLE*4;

	/** Ｒ波のピーク１度検出したことを表す */
	private static final int FIRST_R_DETECTION = -1;
	/** 一度もＲ波のピークを検出していないことを表す */
	private static final int ZERO_R_DETECTION = -2;

	/** R波を検出する際の検索開始時間 */
	private static final int TIME_START_DETECT_R = 20;
	/** R波を検出する際の検索終了時間 */
	private static final int TIME_END_DETECT_R = 20;

	//---------計算等の為のバッファ----------//
	/** 心電図信号バッファ  */
	public static double[] ECGBuf = new double[CALC_BUF_SIZE];
	/** 脈波信号バッファ  */
	public static double[] PLSBuf = new double[CALC_BUF_SIZE];
	/** ノッチフィルタ適用心電図信号バッファ*/
	public static double[] notchECGBuf = new double[CALC_BUF_SIZE];
	/** ハイパスフィルタ適用心電図信号バッファ*/
	public static double[] hpfECGBuf = new double[CALC_BUF_SIZE];
	/** 閾値計算用心電図信号バッファ*/
	private double[] thresholdECGBuf = new double[CALC_BUF_SIZE];

	//------------フィルタ関連-------------//
	/**	ノッチフィルタを実行するためのオブジェクト (IIR) */
	private IIRFilterDirectI notchFilter;
	/**	ハイパスフィルタを実行するためのオブジェクト(FIR) */
	private FIRFilter highPassFilter;
	/**	血圧推定のフィルタを実行するためのオブジェクト(二次セクション　IIR) */
	private IIRFilterDirectI sbpFilter;

	//------------R波検出関連-------------//
	/** R波ピーク検出モード(最初は閾値検出モード) */
	private int rWaveDetectionMode = THRESHOLD_DETECT_MODE;

	/** R波ピーク検出の為の閾値の設定がおわっているかどうか */
	private boolean isInitThreshold = false;
	/** R波ピーク検出の為の閾値検出用カウンタ */
	private int thCounter = 0;
	/** R波ピーク検出の為の閾値 */
	private double threshold = 0;

	/** 心拍数用カウンター*/
	private int rrCounter = 0;
	/** 前回のRR間隔 */
	private int rrInterval = ZERO_R_DETECTION;

	//-------------PAT計算--------------//
	/** リスケールPAT,HR計算用のバッファ*/
	private Vector patHRBuf;
	/**	リスケールした脈波伝播時間 */
	private double[] rPATBuf ;
	/**	リスケールした心拍数 */
	private double[] rHRBuf ;

	/** 基準の血圧値 */
	private double baseSBP = 120;

	/*************************************************************************/

	Message new_msg        = null;
	Message dec_worked_msg = null;
	Message dec_reload_msg = null;

	/** メッセージキュー　*/
	MessageQueue dec_buf_fifo = null;
	/** メールボックス制御への参照 */
	MailBoxControl mbx_con=null;

	/** 実行用スレッド */
	private Thread thread=null;
	/** 実行フラグ */
	private boolean runFlag = false;

	/** デバッグ用SD */
	private SDCardMIDP dbgSD;
	/** デバック用データのＳＤカードのパス */
	private static final String DBG_SD_PATH ="dbg.txt";
	private static final String DBG_SD_PATH2 ="dbg2.txt";

	// 実体を作らない
	Message hmsgin=null,hmsgout=null;

	/** キャンバスへの参照*/
	BPMCanvas canvas=null;

	//------Currentモードの描画バッファ------//
	/** 心電図信号描画用バッファ */
	private double[] ecgGraBuf;
	/** 脈波信号描画用バッファ */
	private double[] plsGraBuf;
	/**	R波ピークを保持するリスト (R波を検出したときは1を入れる)*/
	private int[] rWavePeekGraBuf;
	/**	ハイパスフィルタ適用心電図のR波ピークを保持するリスト
	 * (R波を検出したときは1を入れる)*/
	private int[] hpfRWavePeekGraBuf;

	//-------Trendモードの描画バッファ-------//
	/** 心拍数描画用バッファ */
	private double[] hrGraBuf;
	/** 脈波伝搬時間描画用バッファ */
	private double[] patGraBuf;
	/** 血圧値描画用バッファ */
	private double[] sbpGraBuf;
	/** 現在の心拍数値*/
	private int hr  = -1;
	/** 現在の血圧値 */
	private double sbp = -1;
	/** 現在の脈波伝搬時間値 */
	private int pat = -1;

	/************************************************************************/
	/* Http TSK, FFT TSK に送るためのメソッド、フィールド */

	/** 一度に受信するデータ数 */
	private static final int SIZE_OF_SEND_ECG_PLS =1000;
	private static final int SIZE_OF_SEND_HR_BP   =100;
	private static final int SIZE_OF_SEND_FFT     =512;

	/** Write用 */
	private MessageQueue http_ecg_pls_fifo = null;
	private MessageQueue http_hr_bp_fifo   = null;	
	private MessageQueue fft_ecg_fifo      = null;

	/** HTTPTSK に渡すデータを入れる */
	private Message http_ecg_pls_msg1=null;
	private Message http_ecg_pls_msg2=null;
	private Message http_ecg_pls_msg3=null;
	private Message http_ecg_pls_msg4=null;

	private Message http_hr_bp_msg1=null;
	private Message http_hr_bp_msg2=null;
	private Message http_hr_bp_msg3=null;
	private Message http_hr_bp_msg4=null;
	
	/** FFTTSK に渡すデータを入れる */
	private Message fft_ecg_msg1=null;
	private Message fft_ecg_msg2=null;
	private Message fft_ecg_msg3=null;
	private Message fft_ecg_msg4=null;

	/** 心電,脈波,心拍,血圧 受信バッファ */
	private int[] ecg_hbuf1=null;
	private int[] ecg_hbuf2=null;
	private int[] ecg_hbuf3=null;
	private int[] ecg_hbuf4=null;

	private int[] pls_hbuf1=null;
	private int[] pls_hbuf2=null;
	private int[] pls_hbuf3=null;
	private int[] pls_hbuf4=null;

	private int[] hr_hbuf1=null;
	private int[] hr_hbuf2=null;
	private int[] hr_hbuf3=null;
	private int[] hr_hbuf4=null;

	private int[] bp_hbuf1=null;
	private int[] bp_hbuf2=null;
	private int[] bp_hbuf3=null;
	private int[] bp_hbuf4=null;
	
	private double[] fft_ecg_hbuf1 = null;
	private double[] fft_ecg_hbuf2 = null;
	private double[] fft_ecg_hbuf3 = null;
	private double[] fft_ecg_hbuf4 = null;	

	//Message インスタンスを作らない。
	private Message hnew_ecg_pls_msg=null;
	private Message hnew_hr_bp_msg =null;
	private Message hnew_fft_msg   =null;

	/**
     * コンストラクタ
	 */
	public BPCalcTSK(MailBoxControl mbx_con,BPMCanvas canvas){

		if(Main.DEBUG) dp2=new DebugPrint();

		//キャンバスへの参照
		this.canvas = canvas;
		//メールボックスコントローラ
		this.mbx_con = mbx_con;
		//メッセージキュー
		dec_buf_fifo = new MessageQueue();

		//心電図からノイズなどを除去するフィルタ
		//フィルタ用係数
		int len = 0;

		if(Option.getOp().is50HzPowerSupply()){   //商用電源周波数が50Hz
			len = NotchFilter.NOTCH50_DEN.length;
		}else{	                                  //商用電源周波数が60Hz
			len = NotchFilter.NOTCH60_DEN.length;
		}

		//フィルタ
		notchFilter = new IIRFilterDirectI(len);
		highPassFilter = new FIRFilter(HighPassFilter.COFF.length);
		sbpFilter = new IIRFilterDirectI(BPEstimationFilter.DEN.length);

		//脈波伝播時間用バッファ
		patHRBuf = new Vector();
		rPATBuf = new double[BPEstimationFilter.DEN.length];
		//心拍数用バッファ
		rHRBuf = new double[BPEstimationFilter.DEN.length];

		//httpバッファ作成
		http_buf_create();
		//http初期化処理
		http_init();

		//初期化処理
		init();

	}//BPCalcTask

	/**
	 * HTTP Buffer 生成
	 */
	public void http_buf_create() {

		// 心電,脈波,心拍,血圧 受信バッファ
		ecg_hbuf1 = new int[SIZE_OF_SEND_ECG_PLS];
		ecg_hbuf2 = new int[SIZE_OF_SEND_ECG_PLS];
		ecg_hbuf3 = new int[SIZE_OF_SEND_ECG_PLS];
		ecg_hbuf4 = new int[SIZE_OF_SEND_ECG_PLS];

		pls_hbuf1 = new int[SIZE_OF_SEND_ECG_PLS];
		pls_hbuf2 = new int[SIZE_OF_SEND_ECG_PLS];
		pls_hbuf3 = new int[SIZE_OF_SEND_ECG_PLS];
		pls_hbuf4 = new int[SIZE_OF_SEND_ECG_PLS];

		hr_hbuf1 = new int[SIZE_OF_SEND_HR_BP];
		hr_hbuf2 = new int[SIZE_OF_SEND_HR_BP];
		
		hr_hbuf3 = new int[SIZE_OF_SEND_HR_BP];
		hr_hbuf4 = new int[SIZE_OF_SEND_HR_BP];

		bp_hbuf1 = new int[SIZE_OF_SEND_HR_BP];
		bp_hbuf2 = new int[SIZE_OF_SEND_HR_BP];
		bp_hbuf3 = new int[SIZE_OF_SEND_HR_BP];
		bp_hbuf4 = new int[SIZE_OF_SEND_HR_BP];
		
		fft_ecg_hbuf1 = new double[SIZE_OF_SEND_FFT];
		fft_ecg_hbuf2 = new double[SIZE_OF_SEND_FFT];
		fft_ecg_hbuf3 = new double[SIZE_OF_SEND_FFT];
		fft_ecg_hbuf4 = new double[SIZE_OF_SEND_FFT];	

		http_ecg_pls_msg1 = new Message();
		http_ecg_pls_msg2 = new Message();
		http_ecg_pls_msg3 = new Message();
		http_ecg_pls_msg4 = new Message();

		http_hr_bp_msg1 = new Message();
		http_hr_bp_msg2 = new Message();
		http_hr_bp_msg3 = new Message();
		http_hr_bp_msg4 = new Message();
		
		fft_ecg_msg1 = new Message();
		fft_ecg_msg2 = new Message();
		fft_ecg_msg3 = new Message();
		fft_ecg_msg4 = new Message();				

		http_ecg_pls_fifo = new MessageQueue();
		http_hr_bp_fifo   = new MessageQueue();
		fft_ecg_fifo      = new MessageQueue();
	}

	/**
	 * HTTP Buffer 初期化処理
	 */
	public void http_init() {

		// 心電,脈波の初期化
		for (int i = 0; i < SIZE_OF_SEND_ECG_PLS; i++) {
			ecg_hbuf1[i] = 0;
			ecg_hbuf2[i] = 0;
			ecg_hbuf3[i] = 0;
			ecg_hbuf4[i] = 0;

			pls_hbuf1[i] = 0;
			pls_hbuf2[i] = 0;
			pls_hbuf3[i] = 0;
			pls_hbuf4[i] = 0;
		}

		// 心拍,血圧の初期化
		for (int i = 0; i < SIZE_OF_SEND_HR_BP; i++) {
			hr_hbuf1[i] = 0;
			hr_hbuf1[i] = 0;
			hr_hbuf1[i] = 0;
			hr_hbuf1[i] = 0;

			bp_hbuf1[i] = 0;
			bp_hbuf2[i] = 0;
			bp_hbuf3[i] = 0;
			bp_hbuf4[i] = 0;
		}
		
		//FFTTSKに送る心電の初期化
		for(int i = 0; i < SIZE_OF_SEND_FFT; i++){
			fft_ecg_hbuf1[i] = 0;
			fft_ecg_hbuf2[i] = 0;
			fft_ecg_hbuf3[i] = 0;
			fft_ecg_hbuf4[i] = 0;
		}
		
		// メッセージキューの初期化
		http_ecg_pls_fifo.clear();
		http_hr_bp_fifo.clear();
		fft_ecg_fifo.clear();

		// ///////////////////////////////////////////////////
		// ECG,PLS メッセージの初期化
		http_ecg_pls_msg1.clear();
		http_ecg_pls_msg2.clear();
		http_ecg_pls_msg3.clear();
		http_ecg_pls_msg4.clear();

		// メッセージを設定
		http_ecg_pls_msg1.payload_int_ptr1 = ecg_hbuf1;
		http_ecg_pls_msg2.payload_int_ptr1 = ecg_hbuf2;
		http_ecg_pls_msg3.payload_int_ptr1 = ecg_hbuf3;
		http_ecg_pls_msg4.payload_int_ptr1 = ecg_hbuf4;

		http_ecg_pls_msg1.payload_int_ptr2 = pls_hbuf1;
		http_ecg_pls_msg2.payload_int_ptr2 = pls_hbuf2;
		http_ecg_pls_msg3.payload_int_ptr2 = pls_hbuf3;
		http_ecg_pls_msg4.payload_int_ptr2 = pls_hbuf4;

		// int_ptr1,int_ptr2のサイズはsize1
		http_ecg_pls_msg1.payload_size1 = SIZE_OF_SEND_ECG_PLS;
		http_ecg_pls_msg2.payload_size1 = SIZE_OF_SEND_ECG_PLS;
		http_ecg_pls_msg3.payload_size1 = SIZE_OF_SEND_ECG_PLS;
		http_ecg_pls_msg4.payload_size1 = SIZE_OF_SEND_ECG_PLS;

		// キューにデータを入れる
		http_ecg_pls_fifo.putRequest(http_ecg_pls_msg1);
		http_ecg_pls_fifo.putRequest(http_ecg_pls_msg2);
		http_ecg_pls_fifo.putRequest(http_ecg_pls_msg3);
		http_ecg_pls_fifo.putRequest(http_ecg_pls_msg4);

		// /////////////////////////////////////////////////////
		// HR,BP メッセージの初期化
		http_hr_bp_msg1.clear();
		http_hr_bp_msg2.clear();
		http_hr_bp_msg3.clear();
		http_hr_bp_msg4.clear();

		// メッセージを設定
		http_hr_bp_msg1.payload_int_ptr1 = hr_hbuf1;
		http_hr_bp_msg2.payload_int_ptr1 = hr_hbuf2;
		http_hr_bp_msg3.payload_int_ptr1 = hr_hbuf3;
		http_hr_bp_msg4.payload_int_ptr1 = hr_hbuf4;

		http_hr_bp_msg1.payload_int_ptr2 = bp_hbuf1;
		http_hr_bp_msg2.payload_int_ptr2 = bp_hbuf2;
		http_hr_bp_msg3.payload_int_ptr2 = bp_hbuf3;
		http_hr_bp_msg4.payload_int_ptr2 = bp_hbuf4;

		// int_ptr1,int_ptr2のサイズはsize1
		http_hr_bp_msg1.payload_size1 = SIZE_OF_SEND_HR_BP;
		http_hr_bp_msg2.payload_size1 = SIZE_OF_SEND_HR_BP;
		http_hr_bp_msg3.payload_size1 = SIZE_OF_SEND_HR_BP;
		http_hr_bp_msg4.payload_size1 = SIZE_OF_SEND_HR_BP;

		// キューにデータを入れる
		http_hr_bp_fifo.putRequest(http_hr_bp_msg1);
		http_hr_bp_fifo.putRequest(http_hr_bp_msg2);
		http_hr_bp_fifo.putRequest(http_hr_bp_msg3);
		http_hr_bp_fifo.putRequest(http_hr_bp_msg4);
		
	
		// /////////////////////////////////////////////////////
		// FFT ECG メッセージの初期化
		fft_ecg_msg1.clear();
		fft_ecg_msg2.clear();
		fft_ecg_msg3.clear();
		fft_ecg_msg4.clear();		

		// メッセージを設定
		fft_ecg_msg1.payload_double_ptr1 = fft_ecg_hbuf1;
		fft_ecg_msg2.payload_double_ptr1 = fft_ecg_hbuf2;
		fft_ecg_msg3.payload_double_ptr1 = fft_ecg_hbuf3;
		fft_ecg_msg4.payload_double_ptr1 = fft_ecg_hbuf4;

		// int_ptr1,int_ptr2のサイズはsize1
		fft_ecg_msg1.payload_size1 = SIZE_OF_SEND_FFT;
		fft_ecg_msg2.payload_size1 = SIZE_OF_SEND_FFT;
		fft_ecg_msg3.payload_size1 = SIZE_OF_SEND_FFT;
		fft_ecg_msg4.payload_size1 = SIZE_OF_SEND_FFT;

		// キューにデータを入れる
		fft_ecg_fifo.putRequest(fft_ecg_msg1);
		fft_ecg_fifo.putRequest(fft_ecg_msg2);
		fft_ecg_fifo.putRequest(fft_ecg_msg3);
		fft_ecg_fifo.putRequest(fft_ecg_msg4);
		
		HttpTSK.http_ecg_pls_send_flag = false;
		HttpTSK.http_hr_bp_send_flag = false;
		FFTTSK.fft_send_flag = false;
	}

	/**
	 * 初期化処理
	 */
	public void init(){

		//描画バッファの初期化
		InitGraphBuffer();

		//計算用バッファの初期化
		for(int i = 0; i < CALC_BUF_SIZE ;i++){
			ECGBuf[i] = 0;
			PLSBuf[i] = 0;
			notchECGBuf[i] = 0;
			hpfECGBuf[i] = 0;
		}
		//心拍、脈波伝播時間用バッファをクリア
		patHRBuf.removeAllElements();

		//RPATバッファの初期化
		for(int i = 0; i < rPATBuf.length ; i++){
			rPATBuf[i] = 0;
		}
		//RHRバッファの初期化
		for(int i = 0; i < rHRBuf.length ; i++){
			rHRBuf[i] = 0;
		}

		//メッセージキューを空に
		dec_buf_fifo.clear();

	}//init


	/**
	 * 描画バッファを初期化
	 */
	public void InitGraphBuffer(){

		//描画用バッファの初期化(カレント画面) (画面の幅*最大の縮小倍率分のバッファを確保)
		int width = canvas.getWidth()*BPMCanvas.MAN_X_REDUCTION_RATE;
		ecgGraBuf = new double[width];
		plsGraBuf = new double[width];
		rWavePeekGraBuf = new int[width];
		hpfRWavePeekGraBuf = new int[width];
		for(int i = 0; i < width ; i++){
			ecgGraBuf[i] = 0;
			plsGraBuf[i] = 0;
			rWavePeekGraBuf[i] = 0;
			hpfRWavePeekGraBuf[i] = 0;
		}

		//描画用バッファの初期化(トレンド画面) (画面の幅分のバッファを確保)
		width = canvas.getWidth();
		hrGraBuf  = new double[width];
		patGraBuf = new double[width];
		sbpGraBuf = new double[width];
		for(int i = 0; i < width ; i++){
			hrGraBuf[i]  = 0;
			patGraBuf[i] = 0;
			sbpGraBuf[i] = 0;
		}

		//Managerの描画バッファを初期化
		//波形用
		BPMonitorManager.ecgGraBuf=ecgGraBuf;
		BPMonitorManager.plsGraBuf=plsGraBuf;
		//ピーク用
		BPMonitorManager.rWavePeekGraBuf=rWavePeekGraBuf;
		BPMonitorManager.hpfRWavePeekGraBuf=hpfRWavePeekGraBuf;
		//HR,PAT値用
		BPMonitorManager.hr=hr;
		BPMonitorManager.pat=pat;
		//トレント用
		BPMonitorManager.hrGraBuf=hrGraBuf;
		BPMonitorManager.patGraBuf=patGraBuf;
		BPMonitorManager.sbpGraBuf=sbpGraBuf;

	}//InitGraphBuffer

	/**
	 * 実行の開始
	 */
	public void start(){
		//初期化処理
		init();
		//実行フラグを立てる
		runFlag = true;
		//スレッドの起動
		thread = new Thread(this);
		//実行の開始
		thread.start();
	}

	/** MainLoope */
	public static final int SLEEP_TIME = 0;

	private static double bpcalc_time=0;

	public void run(){

		try{

			if(Main.DEBUG){
				//デバッグ用SDの作成
				dbgSD = new SDCardMIDP(Main.IS_ACTUAL);
				dbgSD.open(DBG_SD_PATH,Connector.READ_WRITE);
			}

			//スリープ時間
			long startTime = System.currentTimeMillis();
			//計算にかかった時間
			long pastTime = 0;

			while(runFlag){

				//メッセージをペンド
				new_msg=mbx_con.MsgPend(mbx_con.MBX_bpcalc,-1);

				startTime = System.currentTimeMillis();
				if(Main.DEBUG) dp2.StartTime1();

				switch(new_msg.msg_id){

					case MailBoxControl.MSG_BPCALC_DATA:
						dec_buf_fifo.putRequest(new_msg);
					break;

					//使われたバッファが返却される
					case MailBoxControl.MSG_HTTP_ECG_PLS_ACK:
						http_ecg_pls_fifo.putRequest(new_msg);
					break;
					
					//使われたバッファが返却される
					case MailBoxControl.MSG_HTTP_HR_BP_ACK:
						http_hr_bp_fifo.putRequest(new_msg);
					break;
					
					//使われたバッファが返却される
					case MailBoxControl.MSG_FFT_ECG_ACK:
						fft_ecg_fifo.putRequest(new_msg);
					break;

					default:
						break;
				}/* End of Switch */

				//メッセージキューからメッセージを取り出す
				new_msg=dec_buf_fifo.getRequest();

				if (new_msg!=null) {
					//この時点でdec_worked_msgは一個前のメッセージである
					if(dec_worked_msg != null){
						mbx_con.AckMsgPost(dec_worked_msg);
					}
					dec_worked_msg = dec_reload_msg;
					dec_reload_msg = new_msg;

					//計算処理
					calc(dec_reload_msg.payload_double_ptr1,
							dec_reload_msg.payload_double_ptr2,
							dec_reload_msg.payload_size1);

					//描画フラグを立てる
					BPMonitorManager.bp_draw_on_flag=true;
				}//end of if

				//計算にかかった時間
				pastTime = System.currentTimeMillis() - startTime;

				if(pastTime < SLEEP_TIME){
					//休止
					pause(SLEEP_TIME+5 - pastTime);
				}

				if(Main.DEBUG && new_msg!=null){
					dp2.EndTime1();
					bpcalc_time=dp2.GetPeriodTime1();
					if(Main.DEBUG){
						dbgSD.write(("bpcalc" + bpcalc_time +"\r\n").getBytes());
						dbgSD.flush();
					}
					System.out.println("BPCalcTSK spendtime="+bpcalc_time);
					this.canvas.drawDBG2("bpcalc="+Long.toString((long)bpcalc_time));
				}
			}//end of while(1)
		}catch(Exception e){
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

	private int cnt = 0;
	/**
	 * 計算処理
	 */
	private void calc(double[] tmpECG,double[] tmpPLS,int size){
		System.out.println("size="+size);

		if(HttpTSK.http_ecg_pls_send_flag==false){
			do{
				hnew_ecg_pls_msg=http_ecg_pls_fifo.getRequest();
				System.out.println("http_ecg_pls_fifo.get");
			}while(hnew_ecg_pls_msg==null);

			HttpTSK.http_ecg_pls_send_flag=true;
		}

		if(HttpTSK.http_hr_bp_send_flag==false){
			do{
				hnew_hr_bp_msg=http_hr_bp_fifo.getRequest();
				System.out.println("http_hr_bp_fifo.get");
			}while(hnew_hr_bp_msg==null);

			HttpTSK.http_hr_bp_send_flag=true;
		}
		
		if(FFTTSK.fft_send_flag == false){
			do{
				hnew_fft_msg = fft_ecg_fifo.getRequest();
				System.out.println("fft_ecg_fifo.get");
			}while(hnew_fft_msg==null);
			
			FFTTSK.fft_send_flag = true;			
		}

		//System.out.println("calc start");
		//---------------受信バッファからデータをとりだし、計算用配列に格納----------------//
		//心電図データを格納する一時バッファ
		double[] tmpNotchECG=new double[size];
		double[] tmpHpfECG=new double[size];
		cnt += size;

		//--------------------------フィルタ処理を行う-----------------------------//
		//心電図にノッチフィルタを施す
		//notchECG[]に結果を格納

		if(Option.getOp().is50HzPowerSupply()){//50Hzの電源ノイズを除去
			notchFilter.doFilter(tmpECG,tmpNotchECG,NotchFilter.NOTCH50_NUM,NotchFilter.NOTCH50_DEN);
		}else{                     //60Hzの電源ノイズを除去
			notchFilter.doFilter(tmpECG,tmpNotchECG,NotchFilter.NOTCH60_NUM,NotchFilter.NOTCH60_DEN);
		}

		//ハイパスフィルタ.基線変動を取り除く
		//hpfECG[]に結果を格納
		highPassFilter.filterRapper(tmpNotchECG,tmpHpfECG,HighPassFilter.COFF);

		//---------------------バッファの更新-----------------------//

		//脈波バッファ(３つのバッファは同じサイズ)のシフト
		//ノッチフィルタ適用の心電図バッファのシフト
		//ハイパスフィルタ適用の心電図バッファのシフト
		//閾値用バッファのシフト
		for(int i = 0 ; i < CALC_BUF_SIZE - size ; i++){
			PLSBuf[i] = PLSBuf[i+size];
			notchECGBuf[i] = notchECGBuf[i+size];
			hpfECGBuf[i] = hpfECGBuf[i+size];
			ECGBuf[i] = ECGBuf[i+size];
			thresholdECGBuf[i] = thresholdECGBuf[i+size];
		}

		//R波のピークのシフト
		//ハイパスフィルタ適用心電図のR波のピークのシフト
		for(int i = 0 ; i < rWavePeekGraBuf.length - size  ; i++){
			rWavePeekGraBuf[i] = rWavePeekGraBuf[i+size];
			hpfRWavePeekGraBuf[i] = hpfRWavePeekGraBuf[i+size];
		}

		//新しいデータを入れる(最新のデータは一番最後)
		for(int i = 0; i < size ; i++){
			int index = CALC_BUF_SIZE + i - size;
			PLSBuf[index]          = tmpPLS[i];
			notchECGBuf[index]     = tmpNotchECG[i];
			hpfECGBuf[index]       = tmpHpfECG[i];
			ECGBuf[index]          = tmpECG[i];
			thresholdECGBuf[index] = tmpHpfECG[i];

			index = rWavePeekGraBuf.length + i - size;
			rWavePeekGraBuf[index] = 0;
			hpfRWavePeekGraBuf[index] = 0;

			//HTTPに送る sone1207 abe1212
			/*
			if(hnew_ecg_pls_msg == null){
				System.out.println("hnew_ecg_pls_msg=null");
			}
			*/

			//ECG,PLSの送信
			if(hnew_ecg_pls_msg != null){
				hnew_ecg_pls_msg.payload_int_ptr1[hnew_ecg_pls_msg.msg_count1++]=(int)tmpHpfECG[i];
				hnew_ecg_pls_msg.payload_int_ptr2[hnew_ecg_pls_msg.msg_count2++]=(int)tmpPLS[i];
				if(hnew_ecg_pls_msg.msg_count1 == hnew_ecg_pls_msg.payload_size1){
					System.out.println("hnew_ecg_pls_msg.msg_count1="+hnew_ecg_pls_msg.msg_count1);
					mbx_con.WriteMsgPost(MailBoxControl.MSG_HTTP_ECG_PLS_DATA,hnew_ecg_pls_msg,mbx_con.MBX_bpcalc);
					hnew_ecg_pls_msg.count_clear();
					HttpTSK.http_ecg_pls_send_flag=false;
				}
			}
			//FFT ECGの送信
			if(hnew_fft_msg != null){
				double tECG = 0.0;
				switch (BPMonitorManager.displayECGSignal) {
					//HPFECG
					case BPMonitorManager.HPF_ECG:
						tECG = tmpHpfECG[i];
					break;
					//NotchECG
					case BPMonitorManager.NOTCH_ECG:
						tECG = tmpNotchECG[i];
					break;
					//ECG
					default:
						tECG = tmpECG[i];
					break;				
				}
				hnew_fft_msg.payload_double_ptr1[hnew_fft_msg.msg_count1++]= tECG;
				if(hnew_fft_msg.msg_count1 == hnew_fft_msg.payload_size1){
					System.out.println("hnew_fft_msg.msg_count1="+hnew_fft_msg.msg_count1);
					mbx_con.WriteMsgPost(MailBoxControl.MSG_FFT_ECG_DATA,hnew_fft_msg,mbx_con.MBX_bpcalc);
					hnew_fft_msg.count_clear();
					FFTTSK.fft_send_flag = false;					
				}
			}
		}

		if(isInitThreshold){

			//---------Ｒ波ピークをもとめるための閾値の設定がおわっているとき------//
			for(int i = 0 ; i < size ; i++){
				//--------心電図のR波ピーク、脈波の立ち上がり点をもとめる-------//
				int bufIndex = CALC_BUF_SIZE - size + i;

				//--------RR間隔カウント用変数を更新--------//
				if(rrCounter++ > MAX_RR_COUNTER){
					//一定時間R波ピークが検出されなかったら
					//閾値をリセット
					rrCounter = 0;
					//閾値の計算完了フラグをリセット
					isInitThreshold = false;
				}

				//-------閾値の検出、R波ピークの検出--------//
				switch (rWaveDetectionMode) {

				//////////閾値検出モードのとき//////////////
				case THRESHOLD_DETECT_MODE:

					if(hpfECGBuf[bufIndex] > threshold){
						//閾値を超えたとき (フィルタ適用の心電R波ピーク検出モードに)
						rWaveDetectionMode = FILTERED_ECG_R_DETECT_MODE;
					}
					break;

				////フィルタ適用の心電R波ピーク検出モードのとき////
				case FILTERED_ECG_R_DETECT_MODE:

					if(hpfECGBuf[bufIndex] < threshold){
						//閾値を超えなくなったとき

						//フィルタ適用心電図の最大値(R波ピーク)を確定する
						double max = hpfECGBuf[bufIndex];
						//最大値のインデックス(R波ピークのインデックス)
						int rIndex = bufIndex;
						//最大値の検索(戻りながら検索)
						for(int j = bufIndex-1; j > 0 ; j--){
							if (max < hpfECGBuf[j]){
								max = hpfECGBuf[j];
								rIndex = j;
							}else{
								break;
							}
						}

						//描画用にインデックスを保存
						//(CALC_BUF_SIZE - 1) = バッファの最後のインデックス(=最新のデータ)
						//　　　　　　　　　　　　 index = バッファ中のR波ピークのインデックス
						//           graIndex = R波ピークを取得してから、最新のデータまでの経過時間
						int graIndex = (CALC_BUF_SIZE - 1) - rIndex;
						//描画用のインデックスも、バッファの最後のインデックスが最新のデータだから、
						//R波の描画用インデックス = 描画バッファの最後のインデックス - R波を取得してからの時間
						hpfRWavePeekGraBuf[(hpfRWavePeekGraBuf.length - 1) - graIndex] = 1;

						//--元の心電図のR波のピークをもとめる--//
						//(ハイパスフィルタの群遅延分だけ戻って、その周辺で元の心電図のR波ピークをもとめる)

						//検索開始地点
						//tmpIndex = i のデータが最新
						//tmpIndex = フィルタ適用R波ピーク取得地点 - ハイパスの群遅延サンプル - 検索開始地点)
						int tmpIndex = rIndex - HighPassFilter.GROUP_DELAY - TIME_START_DETECT_R;
						//検索する長さ
						int len = TIME_START_DETECT_R + TIME_END_DETECT_R + 1;
						//最大値
						max = ECGBuf[tmpIndex];
						//最大値のインデックス
						rIndex = tmpIndex;
						//最大値を検索
						for(int j = tmpIndex; j < tmpIndex + len ; j++ ){
							if(max < ECGBuf[j]){
								max = ECGBuf[j];
								rIndex = j;
							}
						}

						//RR間隔
						//             (bufIndex - index) = R波ピークを取得してから経過した時間
						// rrCounter - (bufIndex - index) = RR間隔
						rrCounter = rrCounter - (bufIndex - rIndex);

						//脈波伝搬時間を計算するかどうかのフラグ
						boolean patFlag = false;
						//前回のRピーク地点(脈波伝搬時間計算用)
						int prevRIndex = rIndex - rrCounter;

						//前回のRR間隔の1/2以下または、1.5倍以上の場合は
						//アーティファクトとみなして無視する。また検出してから
						//最初の一回は計算があっていないので無視する
						if((rrCounter > rrInterval/2 && rrCounter < (rrInterval*3/2))
						   || rrInterval == FIRST_R_DETECTION){
							//RR間隔を保存
							rrInterval = rrCounter;
							//心拍数
							hr = (NUM_OF_DATA_PER_MIN/(rrCounter));
							//カウンタをリセット(R波ピークを検出してから現在まで経過した時間を足しておく)
							rrCounter = bufIndex - rIndex;
							//描画用にインデックスを保存
							graIndex = (hpfECGBuf.length - rIndex - 1);
							rWavePeekGraBuf[rWavePeekGraBuf.length - 1 - graIndex] = 1;
							//脈波伝搬時間の計算を行う
							patFlag = true;

						}else if(rrInterval == ZERO_R_DETECTION){
							//検出し始めてからの最初の一回は捨てる
							//カウンタをリセット(R波ピークを検出してから現在まで経過した時間を足しておく)
							rrCounter = bufIndex - rIndex;
							rrInterval = FIRST_R_DETECTION;

						}else if(rrCounter >= (rrInterval*3/2)){
							//前回のRR間隔から1.5倍以上のときはリセット
							rrInterval = ZERO_R_DETECTION;
							rrCounter = 0;
						}

						//脈波の立ち上がり点を求める。
						if(patFlag){
							//最低点のインデックス
							int minIndex = prevRIndex;
							//最低点
							double min = PLSBuf[minIndex];

							//前回のR波のピークのインデックスから、今回のR波のインデックスの
							//間の脈波伝搬時間の最低点を脈波の立ち上がり点とする。
							for(int j = prevRIndex + 1 ; j <= rIndex ; j++){
								if(PLSBuf[j] <= min){
									min = PLSBuf[j];
									minIndex = j;
								}
							}
							System.out.println((minIndex - prevRIndex));
							//脈波伝播時間
							pat = (minIndex - prevRIndex)*(1000/SAMPLE);

							//心拍、脈波伝播時間の取得時間
							double time = (double)60/(double)hr;
							//心拍、脈波伝播時間バッファに追加
							patHRBuf.addElement(new PAT_HR(pat,hr,time));
						}

						//----------------------------脈波伝播時間のリスケール--------------------------//
						
						//バッファサイズ
						int bufSize  = patHRBuf.size();
						
						//心拍、脈波伝播時間の取得時間の合計
						double total = 0;						
						for(int j = 0; j < bufSize; j++){
							total += ((PAT_HR)(patHRBuf.elementAt(j))).getTime();
						}

						//合計取得時間が1を超えたら、脈波伝播時間が整数のときの
						//心拍、脈波伝播時間をリスケールして求める												
						if(total >= 1 && bufSize >= 2){
							
							//リスケール脈波伝播時間バッファのシフト
							for(int j =  rPATBuf.length - 1 ; j >= (int)total ; j--){
								rPATBuf[j] = rPATBuf[j - (int)total];
							}
							
							//リスケール心拍数バッファのシフト
							for(int j =  rHRBuf.length - 1 ; j >= (int)total ; j--){
								rHRBuf[j] = rHRBuf[j - (int)total];
							}
							
							//---------------------脈波伝播時間のリスケール---------------------//
							double x  = 1;
							double x0 = total-((PAT_HR)(patHRBuf.elementAt(bufSize-1))).getTime();
							double y0 = ((PAT_HR)(patHRBuf.elementAt(bufSize-2))).getPat();
							double x1 = total;
							double y1 = ((PAT_HR)(patHRBuf.elementAt(bufSize-1))).getPat();

							for(int j = 0; j < (int)total && j < rPATBuf.length ; j++){
								rPATBuf[j] =  y0 + ((y1-y0)/(x1-x0))*(x-x0);
								x++;
							}
							
							//------------------------心拍数のリスケール-----------------------//
							x  = 1;
							x0 = total-((PAT_HR)(patHRBuf.elementAt(bufSize-1))).getTime();
							y0 = ((PAT_HR)(patHRBuf.elementAt(bufSize-2))).getHR();
							x1 = total;
							y1 = ((PAT_HR)(patHRBuf.elementAt(bufSize-1))).getHR();

							for(int j = 0; j < (int)total && j < rHRBuf.length ; j++){
								rHRBuf[j] =  y0 + ((y1-y0)/(x1-x0))*(x-x0);
								x++;
							}

							//最後の心拍、脈波伝播時間を取り出す
							PAT_HR last = (PAT_HR)(patHRBuf.elementAt(bufSize-1));
							//心拍、脈波伝播時間バッファをクリア
							patHRBuf.removeAllElements();
							//次の心拍、脈波伝播時間の最初のデータにする
							//心拍、脈波伝播時間の取得時間は
							//最後に保管した時間からの経過時間の小数点数部分
							double next = last.getTime()-(int)(last.getTime());
							patHRBuf.addElement(new PAT_HR(last.getPat(),last.getHR(),next));

							//収縮期血圧の変化分の計算
							//フィルタの計算を実行し、血圧の変化値を計算する。
							double[] output = new double[rPATBuf.length];
							sbpFilter.doFilter(rPATBuf,output,BPEstimationFilter.NUM,BPEstimationFilter.DEN);
							
							//最新の収縮期血圧値
							sbp = output[0];

							//リスケールした分だけ保存
							for(int j = (int)(total - 1); j >= 0 ; j-- ){
								//HTTPで送信するデータをバッファに入れ込む
								if(hnew_hr_bp_msg != null){
									//HR
									hnew_hr_bp_msg.payload_int_ptr1[hnew_hr_bp_msg.msg_count1++]=(int)rHRBuf[j];
									//SBP
									hnew_hr_bp_msg.payload_int_ptr2[hnew_hr_bp_msg.msg_count2++]=(int)(output[j] + baseSBP);
									//100個分のデータが溜まったら送信
									if(hnew_hr_bp_msg.msg_count1 == hnew_hr_bp_msg.payload_size1){										
										System.out.println("hnew_hr_bp_msg.msg_count1="+hnew_hr_bp_msg.msg_count1);
										mbx_con.WriteMsgPost(MailBoxControl.MSG_HTTP_HR_BP_DATA,hnew_hr_bp_msg,mbx_con.MBX_bpcalc);
										hnew_hr_bp_msg.count_clear();
										HttpTSK.http_hr_bp_send_flag = false;										
									}
								}								
							}
						}
						//閾値検出モードに戻る
						rWaveDetectionMode = THRESHOLD_DETECT_MODE;
					}
					break;
				}
				//--------閾値の検出、R波ピークの検出終わり----------//
			}
		}else{
			//---Ｒ波ピークをもとめるための閾値の設定がおわっていないとき------//
			if(thCounter >= CALC_BUF_SIZE){
				//----閾値設定のために十分データがあるとき----//

				//最大値を求める(最初の2秒は捨てる)
				//カウンタは初めて閾値をもとめるときのみ使用。
				//以降閾値がリセットされたら、即時にこのブロックに
				//入り、閾値を計算しなおす
				double max = 0;
				for(int j = SAMPLE*2; j < CALC_BUF_SIZE ; j++ ){
					if(thresholdECGBuf[j] > max){
						max = thresholdECGBuf[j];
					}
				}
				//最大値*THHRESHOLDを最初の閾値と設定する
				threshold = max * THRESHOLD_COFF;
				//閾値設定完了フラグを立てる
				isInitThreshold = true;
			}else{
				//----閾値設定のために十分データがまだない時----//
				//閾値用カウンタの更新
				thCounter+= size;
			}
		}


		//血圧値の計算
		//-----------------------------描画バッファの更新------------------------------//
		//データのシフト(新たに入れるデータ分シフトする)
		//このバッファを移した状態では、配列の後ろにsize分だけ同じデータが重複している
		//1 2 3 4 5 6 7 8 9 10 11 12 13 14 15
		//o o o o o o o o o o  o  o  *  *  *
		//    x x x x x x x x  x  x  *  *  *
		//x x x x x x x x x *  *  *  *  *  *
		//の状態にある。13,14,15は、次で更新される
		for(int i = 0 ; i < ecgGraBuf.length - size; i++){
			ecgGraBuf[i] = ecgGraBuf[i+size];
			plsGraBuf[i] = plsGraBuf[i+size];
		}
		//新たなデータを入れる
		for(int i = 0; i < size ; i++){
			//心電図を入れる
			int index = ecgGraBuf.length + i - size;
			//sone1201

			switch(BPMonitorManager.displayECGSignal){
			case BPMonitorManager.RAW_ECG:
				ecgGraBuf[index] = tmpECG[i];
				break;
			case BPMonitorManager.NOTCH_ECG:
				ecgGraBuf[index] = tmpNotchECG[i];				
				break;
			case BPMonitorManager.HPF_ECG:
				ecgGraBuf[index] = tmpHpfECG[i];
				break;
			}
			//脈波を入れる
			plsGraBuf[index] = tmpPLS[i];
		}

		//--------------------描画バッファへの反映-------------------------//
		//sone1201
		//波形用
		BPMonitorManager.ecgGraBuf=ecgGraBuf;
		BPMonitorManager.plsGraBuf=plsGraBuf;
		//ピーク用
		BPMonitorManager.rWavePeekGraBuf=rWavePeekGraBuf;
		BPMonitorManager.hpfRWavePeekGraBuf=hpfRWavePeekGraBuf;
		//HR,PAT値用
		BPMonitorManager.hr=hr;
		BPMonitorManager.pat=pat;
		BPMonitorManager.sbp = (int)(sbp + (double)baseSBP);
		//トレント用
		BPMonitorManager.hrGraBuf=hrGraBuf;
		BPMonitorManager.patGraBuf=patGraBuf;
		BPMonitorManager.sbpGraBuf=sbpGraBuf;
	}

	/**
	 * 計算の中止
	 */
	public void stop(){
		runFlag = false;
	}

	/**
	 * 実行中かどうか
	 * @return 実行中かどうかを返す
	 */
	public boolean isRunFlag() {
		return runFlag;
	}

}//end of BpCalcTSK
