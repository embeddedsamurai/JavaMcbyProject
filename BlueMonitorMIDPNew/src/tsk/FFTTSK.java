
package tsk;

import javax.microedition.io.Connector;

import sd.SDCardMIDP;
import gui.BPMCanvas;
import mailbox.MailBoxControl;
import mailbox.Message;
import mailbox.MessageQueue;
import main.Main;

public class FFTTSK extends Thread{

	/** ���̃N���X�̃C���X�^���X */
	private static FFTTSK instance = null;

	/** FFT�̐��x */
	private final int N = 512;
	/** FFT�̐��x *3/4 */
	private final int N34 = (N*3)>>2;

	/** ���֐��Ȃ���\���萔 */
	public static final int WND_NONE = 0;
	/** �n�~���O��\���萔 */
	public static final int WND_HAMMING  = 1;
	/** �u���b�N�}����\���萔 */
	public static final int WND_BLKMAN  = 2;
	/** �n����\���萔 */
	public static final int WND_HANN = 3;
	/** �g�p���̑��֐� */
	public int wndFnc = WND_NONE;

	/** ��]���q�p�z�� */
	private double[] wnfft = new double[N34];
	/** �r�b�g���]�p�z�� */
	private int[] brfft = new int[N];

	/** ���s�p�X���b�h */
	private Thread thread=null;
	/** flag */
	private boolean runFlag=false;

	/** BPMCanvas */
	BPMCanvas canvas=null;
	
	/** ���̂����Ȃ� */
	Message new_msg        = null;
	Message dec_worked_msg = null;
	Message dec_reload_msg = null;

	/** ���b�Z�[�W�L���[ */
	MessageQueue fft_ecg_fifo = null;
	/** ���[���{�b�N�X����ւ̎Q�� */
	MailBoxControl mbx_con=null;
	/** fft flag */
	static boolean fft_send_flag=false;
	
	private SDCardMIDP dbgSD4;
	private static final String DBG_SD_PATH4 ="dbg4.txt";
	
	/**
	 * �R���X�g���N�^
	 *
	 * @param FFT�̐��x
	 */
	public FFTTSK(MailBoxControl mbx_con,BPMCanvas canvas) {
		this.canvas = canvas;
		this.mbx_con = mbx_con;
		//��]���q�e�[�u���̍쐬		
		fftTable();
		//�r�b�g���]�e�[�u���̍쐬
		bitReverseTable();
	}
	
	/** FFT�v�Z�̊Ԋu */
	private static final int FFT_INTERVAL = 50;

	/**
	 * FFT�̌v�Z�̊J�n
	 */
	public void start(){
		runFlag = true;
		fft_ecg_fifo  = new MessageQueue();
		//����������
		init();
		//�X���b�h�̋N��
		thread = new Thread(this);
		//���s�̊J�n
		thread.start();	
	}
	
	/**
	 * ����������
	 */
	public void init(){
		fft_send_flag = false;
		fft_ecg_fifo.clear();
	}
	
	/**
	 * FFT�̌v�Z�������Ȃ�
	 */
	public void run() {
		
		//�X���[�v����
		long startTime = System.currentTimeMillis();
		//�v�Z�ɂ�����������
		long pastTime = 0;		
		
		while(runFlag){			
			//���b�Z�[�W���y���h
			new_msg=mbx_con.MsgPend(mbx_con.MBX_fft,-1);
			//�J�n����
			startTime = System.currentTimeMillis();
			//���b�Z�[�WID�Ŕ��f
			switch(new_msg.msg_id){
				case MailBoxControl.MSG_FFT_ECG_DATA:
					fft_ecg_fifo.putRequest(new_msg);								
				break;
				
				default:					
				break;
			}/* End of Switch */
	
			//�L���[���烁�b�Z�[�W�����o��
			new_msg=fft_ecg_fifo.getRequest();
			
			if(new_msg != null){
				if(dec_worked_msg != null){
					//ACK��Ԃ�
					mbx_con.AckMsgPost(dec_worked_msg);
				}	
				dec_worked_msg = dec_reload_msg;
				dec_reload_msg = new_msg;				
				//FFT				
				double[] tmpBuf = this.getASpectrum(dec_reload_msg.payload_double_ptr1);				
				//�L�����o�X�ɓn��
				canvas.setEcgSpectrum(tmpBuf);
			}

			//�v�Z�ɂ�����������
			pastTime = System.currentTimeMillis() - startTime;
			if(pastTime < FFT_INTERVAL){
				//�x�~
				System.out.println("fft" + pastTime);
				pause(FFT_INTERVAL+5 - pastTime);
			}
		}/* End of while() */	
	}/* End of run() */

 /**
	 * �X���b�h�̋x�~
	 */
	public void pause(long time){
		try {
			//�X���[�v
			Thread.sleep(time);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * FFT�e�[�u���̍쐬
	 */
	private void fftTable(){

		//�z�񏉊���
		for(int i = 0; i < wnfft.length ; i++){
			wnfft[i] = 0;
		}
		//��������p�x
		double arg = 2*Math.PI/N;

		//COS�e�[�u���̍쐬
		for(int i = 0; i < wnfft.length ; i++){
			wnfft[i] = Math.cos(arg*i);
		}
	}

	/**
	 * �r�b�g���o�[�X�e�[�u���̍쐬
	 */
	private void bitReverseTable(){
		int nHalf = N/2;

		//�z�񏉊���
		for(int i = 0; i < brfft.length ; i++){
			brfft[i] = 0;
		}
		//�r�b�g���]�e�[�u���쐬
		for(int i = 1; i < N ; i = i << 1){
			for(int j = 0 ; j < i; j++){
				brfft[i+j] = brfft[j] + nHalf;
			}
			nHalf = nHalf >> 1;
		}
	}

	/**
	 * ���ԊԈ���FFT
	 */
	private void fft_time(double[] xr,double[] xi,double[] yr,double[] yi){

		//���֐��̓K�p
		window(yr);

		double xtmpr,xtmpi;
		int jnh,jxC,nHalf,nHalf2;
		int step;
		double arg;

		//���ԊԈ����̂��߃f�[�^�𔽓]
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
	 * ���֐��̓K�p
	 */
	private void window(double[] array){
		double weight = 0;

		switch (wndFnc) {
			case WND_NONE:   //�Ȃ�
				return;
			case WND_HAMMING://�n�~���O
				for(int i = 0; i < array.length ; i++){
					weight = 0.54 - 0.46 * Math.cos(2*Math.PI*i/(array.length - 1));
					array[i] = array[i]*weight;
				}
				break;
			case WND_BLKMAN: //�u���b�N�}��
				for(int i = 0; i < array.length ; i++){
					weight = 0.42 - 0.5 * Math.cos(2*Math.PI*i/(array.length - 1))
						    +0.08 * Math.cos(4*Math.PI*i/(array.length - 1));
					array[i] = array[i]*weight;
				}
				break;
			case WND_HANN:   //�n��
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
	 * �U���X�y�N�g���̔z��𓾂�
	 *
	 * @param xr ���͐M���̎�������
	 *
	 * @return �U���X�y�N�g���̔z��
	 */
	public synchronized double[] getASpectrum(double[] xr){

		// ���͗p����
		double[] xi = new double[N];
		// �v�Z���ʊi�[�p����
		double[] yr = new double[N];
		// �v�Z���ʊi�[�p����
		double[] yi = new double[N];

		//���̓f�[�^���R�s�[(�f�B�[�v�R�s�[)
		for(int i = 0; i < N ; i++){

			yr[i] = xr[i];
			yi[i] = 0;
			xi[i] = 0;
		}

		//FFT
		fft_time(xr,xi,yr,yi);

		//�U���X�y�N�g���̌v�Z
		for(int i = 0; i < yr.length ; i++){
			yr[i] = Math.sqrt(yr[i]*yr[i] + yi[i]*yi[i]);
		}
		return yr;
	}

	/**
	 * FFT�̐��xN��Ԃ�
	 * @return
	 */
	public int getN(){
		return N;
	}

	/**
	 * ���֐��̐ݒ�
	 * @param wndFnc ���֐��萔
	 */
	public void setWndFnc(int wndFnc){
		this.wndFnc = wndFnc;
	}

	/**
	 * ���֐��𓾂�
	 * @return wndFnc ���֐�
	 */
	public int getWndFnc(){
		return wndFnc;
	}

	/**
	 * ���֐����𓾂�
	 * @return wndFnc ���֐�
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
	 * ���s���~����
	 */
	public void stop(){
		runFlag = false;
	}
}
