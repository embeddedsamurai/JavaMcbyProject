
package tsk;

import javax.microedition.io.Connector;

import sd.SDCardMIDP;
import gui.BPMCanvas;
import mailbox.MailBoxControl;
import mailbox.Message;
import mailbox.MessageQueue;
import main.Main;

public class FFTTSK extends Thread{

	/** このクラスのインスタンス */
	private static FFTTSK instance = null;

	/** FFTの精度 */
	private final int N = 512;
	/** FFTの精度 *3/4 */
	private final int N34 = (N*3)>>2;

	/** 窓関数なしを表す定数 */
	public static final int WND_NONE = 0;
	/** ハミングを表す定数 */
	public static final int WND_HAMMING  = 1;
	/** ブラックマンを表す定数 */
	public static final int WND_BLKMAN  = 2;
	/** ハンを表す定数 */
	public static final int WND_HANN = 3;
	/** 使用中の窓関数 */
	public int wndFnc = WND_NONE;

	/** 回転因子用配列 */
	private double[] wnfft = new double[N34];
	/** ビット反転用配列 */
	private int[] brfft = new int[N];

	/** 実行用スレッド */
	private Thread thread=null;
	/** flag */
	private boolean runFlag=false;

	/** BPMCanvas */
	BPMCanvas canvas=null;
	
	/** 実体を作らない */
	Message new_msg        = null;
	Message dec_worked_msg = null;
	Message dec_reload_msg = null;

	/** メッセージキュー */
	MessageQueue fft_ecg_fifo = null;
	/** メールボックス制御への参照 */
	MailBoxControl mbx_con=null;
	/** fft flag */
	static boolean fft_send_flag=false;
	
	private SDCardMIDP dbgSD4;
	private static final String DBG_SD_PATH4 ="dbg4.txt";
	
	/**
	 * コンストラクタ
	 *
	 * @param FFTの精度
	 */
	public FFTTSK(MailBoxControl mbx_con,BPMCanvas canvas) {
		this.canvas = canvas;
		this.mbx_con = mbx_con;
		//回転因子テーブルの作成		
		fftTable();
		//ビット反転テーブルの作成
		bitReverseTable();
	}
	
	/** FFT計算の間隔 */
	private static final int FFT_INTERVAL = 50;

	/**
	 * FFTの計算の開始
	 */
	public void start(){
		runFlag = true;
		fft_ecg_fifo  = new MessageQueue();
		//初期化処理
		init();
		//スレッドの起動
		thread = new Thread(this);
		//実行の開始
		thread.start();	
	}
	
	/**
	 * 初期化処理
	 */
	public void init(){
		fft_send_flag = false;
		fft_ecg_fifo.clear();
	}
	
	/**
	 * FFTの計算をおこなう
	 */
	public void run() {
		
		//スリープ時間
		long startTime = System.currentTimeMillis();
		//計算にかかった時間
		long pastTime = 0;		
		
		while(runFlag){			
			//メッセージをペンド
			new_msg=mbx_con.MsgPend(mbx_con.MBX_fft,-1);
			//開始時間
			startTime = System.currentTimeMillis();
			//メッセージIDで判断
			switch(new_msg.msg_id){
				case MailBoxControl.MSG_FFT_ECG_DATA:
					fft_ecg_fifo.putRequest(new_msg);								
				break;
				
				default:					
				break;
			}/* End of Switch */
	
			//キューからメッセージを取り出す
			new_msg=fft_ecg_fifo.getRequest();
			
			if(new_msg != null){
				if(dec_worked_msg != null){
					//ACKを返す
					mbx_con.AckMsgPost(dec_worked_msg);
				}	
				dec_worked_msg = dec_reload_msg;
				dec_reload_msg = new_msg;				
				//FFT				
				double[] tmpBuf = this.getASpectrum(dec_reload_msg.payload_double_ptr1);				
				//キャンバスに渡す
				canvas.setEcgSpectrum(tmpBuf);
			}

			//計算にかかった時間
			pastTime = System.currentTimeMillis() - startTime;
			if(pastTime < FFT_INTERVAL){
				//休止
				System.out.println("fft" + pastTime);
				pause(FFT_INTERVAL+5 - pastTime);
			}
		}/* End of while() */	
	}/* End of run() */

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
	 * FFTテーブルの作成
	 */
	private void fftTable(){

		//配列初期化
		for(int i = 0; i < wnfft.length ; i++){
			wnfft[i] = 0;
		}
		//分割する角度
		double arg = 2*Math.PI/N;

		//COSテーブルの作成
		for(int i = 0; i < wnfft.length ; i++){
			wnfft[i] = Math.cos(arg*i);
		}
	}

	/**
	 * ビットリバーステーブルの作成
	 */
	private void bitReverseTable(){
		int nHalf = N/2;

		//配列初期化
		for(int i = 0; i < brfft.length ; i++){
			brfft[i] = 0;
		}
		//ビット反転テーブル作成
		for(int i = 1; i < N ; i = i << 1){
			for(int j = 0 ; j < i; j++){
				brfft[i+j] = brfft[j] + nHalf;
			}
			nHalf = nHalf >> 1;
		}
	}

	/**
	 * 時間間引きFFT
	 */
	private void fft_time(double[] xr,double[] xi,double[] yr,double[] yi){

		//窓関数の適用
		window(yr);

		double xtmpr,xtmpi;
		int jnh,jxC,nHalf,nHalf2;
		int step;
		double arg;

		//時間間引きのためデータを反転
		for(int j=0 ; j < N ; j++){
			if(j<brfft[j]){
				double tmp = 0;
				tmp = yr[j];

				yr[j] = yr[brfft[j]];
				yr[brfft[j]] = tmp;

				tmp = yi[j];
				yi[j] = yi[brfft[j]];
				yi[brfft[j]] = tmp;
			}
		}

		nHalf  = 1;
		nHalf2 = 2;

		for(step = (N>>1) ; step >= 1; step = (step>>1)){

			for(int k = 0; k<N; k= k+nHalf2){

				jxC = 0;
				for(int j = k ; j < (k+nHalf);j++){

					jnh = j + nHalf;

					xtmpr = yr[jnh];
					xtmpi = yi[jnh];

					arg = 2*Math.PI / N;

					yr[jnh] = xtmpr*Math.cos(arg*jxC) + xtmpi*Math.sin(arg*jxC);
					yi[jnh] = xtmpi*Math.cos(arg*jxC) - xtmpr*Math.sin(arg*jxC);

					xtmpr = yr[j];
					xtmpi = yi[j];

					yr[j] = xtmpr + yr[jnh];
					yi[j] = xtmpi + yi[jnh];

					yr[jnh] = xtmpr - yr[jnh];
					yi[jnh] = xtmpi - yi[jnh];

					jxC = jxC + step;
				}
			}
			nHalf = nHalf << 1;
			nHalf2 = nHalf2 << 1;
		}
	}

	/**
	 * 窓関数の適用
	 */
	private void window(double[] array){
		double weight = 0;

		switch (wndFnc) {
			case WND_NONE:   //なし
				return;
			case WND_HAMMING://ハミング
				for(int i = 0; i < array.length ; i++){
					weight = 0.54 - 0.46 * Math.cos(2*Math.PI*i/(array.length - 1));
					array[i] = array[i]*weight;
				}
				break;
			case WND_BLKMAN: //ブラックマン
				for(int i = 0; i < array.length ; i++){
					weight = 0.42 - 0.5 * Math.cos(2*Math.PI*i/(array.length - 1))
						    +0.08 * Math.cos(4*Math.PI*i/(array.length - 1));
					array[i] = array[i]*weight;
				}
				break;
			case WND_HANN:   //ハン
				for(int i = 0; i < array.length ; i++){
					weight = 0.5 - 0.5 * Math.cos(2*Math.PI*i/(array.length - 1));
					array[i] = array[i]*weight;
				}
				break;
			default:
				break;
		}
	}

	/**
	 * 振幅スペクトルの配列を得る
	 *
	 * @param xr 入力信号の実数部分
	 *
	 * @return 振幅スペクトルの配列
	 */
	public synchronized double[] getASpectrum(double[] xr){

		// 入力用虚数
		double[] xi = new double[N];
		// 計算結果格納用実数
		double[] yr = new double[N];
		// 計算結果格納用虚数
		double[] yi = new double[N];

		//入力データをコピー(ディープコピー)
		for(int i = 0; i < N ; i++){

			yr[i] = xr[i];
			yi[i] = 0;
			xi[i] = 0;
		}

		//FFT
		fft_time(xr,xi,yr,yi);

		//振幅スペクトルの計算
		for(int i = 0; i < yr.length ; i++){
			yr[i] = Math.sqrt(yr[i]*yr[i] + yi[i]*yi[i]);
		}
		return yr;
	}

	/**
	 * FFTの精度Nを返す
	 * @return
	 */
	public int getN(){
		return N;
	}

	/**
	 * 窓関数の設定
	 * @param wndFnc 窓関数定数
	 */
	public void setWndFnc(int wndFnc){
		this.wndFnc = wndFnc;
	}

	/**
	 * 窓関数を得る
	 * @return wndFnc 窓関数
	 */
	public int getWndFnc(){
		return wndFnc;
	}

	/**
	 * 窓関数名を得る
	 * @return wndFnc 窓関数
	 */
	public String getWndFncName(){
		switch (wndFnc) {
		case WND_NONE:
			return "None";
		case WND_BLKMAN:
			return "Blackman";
		case WND_HAMMING:
			return "Hamming";
		case WND_HANN:
			return "Hann";
		default:
			return "None";
		}
	}
	
	/**
	 * 実行を停止する
	 */
	public void stop(){
		runFlag = false;
	}
}
