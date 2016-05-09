/*
 * �쐬��: 2008/12/02
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
 * BPCalcTSK��\���N���X�B
 * <br>
 *
 * @version $Revision: 1.1 $
 * @author shusaku sone <embedded.samurai@gmail.com>
 */
public class BPCalcTSK extends Thread{

	private DebugPrint dp2=null;

	/** �T���v�����O���[�g */
	public static final int SAMPLE = 250;

	//---------------�o�b�t�@�T�C�Y----------------//
	/** �v�Z�p�o�b�t�@�T�C�Y */
	private static final int CALC_BUF_SIZE = SAMPLE*5;

	//-------------�f�[�^��M�֘A---------------//
	/** �P���Ԃ̃f�[�^�� */
	private static final int NUM_OF_DATA_PER_MIN = SAMPLE*60;
	/** �P�x�Ɏ�M����f�[�^�� */
	private static final int SIZE_OF_RECEIVE_AT_A_TIME = 50;
	/** �f�[�^��M�̃^�C�~���O(�P�ʂ�MSEC)*/
	private static final int RECEIVE_INTERVAL = (1000/SAMPLE)*SIZE_OF_RECEIVE_AT_A_TIME;
	/** �ő�̃����O�o�b�t�@�T�C�Y(��M�p) */
	private static final int MAX_RECV_RINGBUF_SIZE = 5000;


	/** �ŏ���臒l�����߂�Ƃ��ɐM���̍ő�l�ɂ��̒l���|�������̂�臒l�Ƃ���*/
	private static final double THRESHOLD_COFF = 0.7;

	//---------�S�d�}R�g�s�[�N���o�̃��[�h----------//
	/** 臒l�̌��o���ł��邱�Ƃ������萔 */
	private static final int THRESHOLD_DETECT_MODE = 0;
	/** �t�B���^�K�p�S�d�}��R�g�̃s�[�N�̌��o���ł��邱�Ƃ������萔 */
	private static final int FILTERED_ECG_R_DETECT_MODE = 1;

	/** RR�C���^�[�o���p�J�E���^�����̒l�𒴂�����臒l�����Z�b�g���� */
	private static final int MAX_RR_COUNTER = SAMPLE*4;

	/** �q�g�̃s�[�N�P�x���o�������Ƃ�\�� */
	private static final int FIRST_R_DETECTION = -1;
	/** ��x���q�g�̃s�[�N�����o���Ă��Ȃ����Ƃ�\�� */
	private static final int ZERO_R_DETECTION = -2;

	/** R�g�����o����ۂ̌����J�n���� */
	private static final int TIME_START_DETECT_R = 20;
	/** R�g�����o����ۂ̌����I������ */
	private static final int TIME_END_DETECT_R = 20;

	//---------�v�Z���ׂ̈̃o�b�t�@----------//
	/** �S�d�}�M���o�b�t�@  */
	public static double[] ECGBuf = new double[CALC_BUF_SIZE];
	/** ���g�M���o�b�t�@  */
	public static double[] PLSBuf = new double[CALC_BUF_SIZE];
	/** �m�b�`�t�B���^�K�p�S�d�}�M���o�b�t�@*/
	public static double[] notchECGBuf = new double[CALC_BUF_SIZE];
	/** �n�C�p�X�t�B���^�K�p�S�d�}�M���o�b�t�@*/
	public static double[] hpfECGBuf = new double[CALC_BUF_SIZE];
	/** 臒l�v�Z�p�S�d�}�M���o�b�t�@*/
	private double[] thresholdECGBuf = new double[CALC_BUF_SIZE];

	//------------�t�B���^�֘A-------------//
	/**	�m�b�`�t�B���^�����s���邽�߂̃I�u�W�F�N�g (IIR) */
	private IIRFilterDirectI notchFilter;
	/**	�n�C�p�X�t�B���^�����s���邽�߂̃I�u�W�F�N�g(FIR) */
	private FIRFilter highPassFilter;
	/**	��������̃t�B���^�����s���邽�߂̃I�u�W�F�N�g(�񎟃Z�N�V�����@IIR) */
	private IIRFilterDirectI sbpFilter;

	//------------R�g���o�֘A-------------//
	/** R�g�s�[�N���o���[�h(�ŏ���臒l���o���[�h) */
	private int rWaveDetectionMode = THRESHOLD_DETECT_MODE;

	/** R�g�s�[�N���o�ׂ̈�臒l�̐ݒ肪������Ă��邩�ǂ��� */
	private boolean isInitThreshold = false;
	/** R�g�s�[�N���o�ׂ̈�臒l���o�p�J�E���^ */
	private int thCounter = 0;
	/** R�g�s�[�N���o�ׂ̈�臒l */
	private double threshold = 0;

	/** �S�����p�J�E���^�[*/
	private int rrCounter = 0;
	/** �O���RR�Ԋu */
	private int rrInterval = ZERO_R_DETECTION;

	//-------------PAT�v�Z--------------//
	/** ���X�P�[��PAT,HR�v�Z�p�̃o�b�t�@*/
	private Vector patHRBuf;
	/**	���X�P�[���������g�`�d���� */
	private double[] rPATBuf ;
	/**	���X�P�[�������S���� */
	private double[] rHRBuf ;

	/** ��̌����l */
	private double baseSBP = 120;

	/*************************************************************************/

	Message new_msg        = null;
	Message dec_worked_msg = null;
	Message dec_reload_msg = null;

	/** ���b�Z�[�W�L���[�@*/
	MessageQueue dec_buf_fifo = null;
	/** ���[���{�b�N�X����ւ̎Q�� */
	MailBoxControl mbx_con=null;

	/** ���s�p�X���b�h */
	private Thread thread=null;
	/** ���s�t���O */
	private boolean runFlag = false;

	/** �f�o�b�O�pSD */
	private SDCardMIDP dbgSD;
	/** �f�o�b�N�p�f�[�^�̂r�c�J�[�h�̃p�X */
	private static final String DBG_SD_PATH ="dbg.txt";
	private static final String DBG_SD_PATH2 ="dbg2.txt";

	// ���̂����Ȃ�
	Message hmsgin=null,hmsgout=null;

	/** �L�����o�X�ւ̎Q��*/
	BPMCanvas canvas=null;

	//------Current���[�h�̕`��o�b�t�@------//
	/** �S�d�}�M���`��p�o�b�t�@ */
	private double[] ecgGraBuf;
	/** ���g�M���`��p�o�b�t�@ */
	private double[] plsGraBuf;
	/**	R�g�s�[�N��ێ����郊�X�g (R�g�����o�����Ƃ���1������)*/
	private int[] rWavePeekGraBuf;
	/**	�n�C�p�X�t�B���^�K�p�S�d�}��R�g�s�[�N��ێ����郊�X�g
	 * (R�g�����o�����Ƃ���1������)*/
	private int[] hpfRWavePeekGraBuf;

	//-------Trend���[�h�̕`��o�b�t�@-------//
	/** �S�����`��p�o�b�t�@ */
	private double[] hrGraBuf;
	/** ���g�`�����ԕ`��p�o�b�t�@ */
	private double[] patGraBuf;
	/** �����l�`��p�o�b�t�@ */
	private double[] sbpGraBuf;
	/** ���݂̐S�����l*/
	private int hr  = -1;
	/** ���݂̌����l */
	private double sbp = -1;
	/** ���݂̖��g�`�����Ԓl */
	private int pat = -1;

	/************************************************************************/
	/* Http TSK, FFT TSK �ɑ��邽�߂̃��\�b�h�A�t�B�[���h */

	/** ��x�Ɏ�M����f�[�^�� */
	private static final int SIZE_OF_SEND_ECG_PLS =1000;
	private static final int SIZE_OF_SEND_HR_BP   =100;
	private static final int SIZE_OF_SEND_FFT     =512;

	/** Write�p */
	private MessageQueue http_ecg_pls_fifo = null;
	private MessageQueue http_hr_bp_fifo   = null;	
	private MessageQueue fft_ecg_fifo      = null;

	/** HTTPTSK �ɓn���f�[�^������ */
	private Message http_ecg_pls_msg1=null;
	private Message http_ecg_pls_msg2=null;
	private Message http_ecg_pls_msg3=null;
	private Message http_ecg_pls_msg4=null;

	private Message http_hr_bp_msg1=null;
	private Message http_hr_bp_msg2=null;
	private Message http_hr_bp_msg3=null;
	private Message http_hr_bp_msg4=null;
	
	/** FFTTSK �ɓn���f�[�^������ */
	private Message fft_ecg_msg1=null;
	private Message fft_ecg_msg2=null;
	private Message fft_ecg_msg3=null;
	private Message fft_ecg_msg4=null;

	/** �S�d,���g,�S��,���� ��M�o�b�t�@ */
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

	//Message �C���X�^���X�����Ȃ��B
	private Message hnew_ecg_pls_msg=null;
	private Message hnew_hr_bp_msg =null;
	private Message hnew_fft_msg   =null;

	/**
     * �R���X�g���N�^
	 */
	public BPCalcTSK(MailBoxControl mbx_con,BPMCanvas canvas){

		if(Main.DEBUG) dp2=new DebugPrint();

		//�L�����o�X�ւ̎Q��
		this.canvas = canvas;
		//���[���{�b�N�X�R���g���[��
		this.mbx_con = mbx_con;
		//���b�Z�[�W�L���[
		dec_buf_fifo = new MessageQueue();

		//�S�d�}����m�C�Y�Ȃǂ���������t�B���^
		//�t�B���^�p�W��
		int len = 0;

		if(Option.getOp().is50HzPowerSupply()){   //���p�d�����g����50Hz
			len = NotchFilter.NOTCH50_DEN.length;
		}else{	                                  //���p�d�����g����60Hz
			len = NotchFilter.NOTCH60_DEN.length;
		}

		//�t�B���^
		notchFilter = new IIRFilterDirectI(len);
		highPassFilter = new FIRFilter(HighPassFilter.COFF.length);
		sbpFilter = new IIRFilterDirectI(BPEstimationFilter.DEN.length);

		//���g�`�d���ԗp�o�b�t�@
		patHRBuf = new Vector();
		rPATBuf = new double[BPEstimationFilter.DEN.length];
		//�S�����p�o�b�t�@
		rHRBuf = new double[BPEstimationFilter.DEN.length];

		//http�o�b�t�@�쐬
		http_buf_create();
		//http����������
		http_init();

		//����������
		init();

	}//BPCalcTask

	/**
	 * HTTP Buffer ����
	 */
	public void http_buf_create() {

		// �S�d,���g,�S��,���� ��M�o�b�t�@
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
	 * HTTP Buffer ����������
	 */
	public void http_init() {

		// �S�d,���g�̏�����
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

		// �S��,�����̏�����
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
		
		//FFTTSK�ɑ���S�d�̏�����
		for(int i = 0; i < SIZE_OF_SEND_FFT; i++){
			fft_ecg_hbuf1[i] = 0;
			fft_ecg_hbuf2[i] = 0;
			fft_ecg_hbuf3[i] = 0;
			fft_ecg_hbuf4[i] = 0;
		}
		
		// ���b�Z�[�W�L���[�̏�����
		http_ecg_pls_fifo.clear();
		http_hr_bp_fifo.clear();
		fft_ecg_fifo.clear();

		// ///////////////////////////////////////////////////
		// ECG,PLS ���b�Z�[�W�̏�����
		http_ecg_pls_msg1.clear();
		http_ecg_pls_msg2.clear();
		http_ecg_pls_msg3.clear();
		http_ecg_pls_msg4.clear();

		// ���b�Z�[�W��ݒ�
		http_ecg_pls_msg1.payload_int_ptr1 = ecg_hbuf1;
		http_ecg_pls_msg2.payload_int_ptr1 = ecg_hbuf2;
		http_ecg_pls_msg3.payload_int_ptr1 = ecg_hbuf3;
		http_ecg_pls_msg4.payload_int_ptr1 = ecg_hbuf4;

		http_ecg_pls_msg1.payload_int_ptr2 = pls_hbuf1;
		http_ecg_pls_msg2.payload_int_ptr2 = pls_hbuf2;
		http_ecg_pls_msg3.payload_int_ptr2 = pls_hbuf3;
		http_ecg_pls_msg4.payload_int_ptr2 = pls_hbuf4;

		// int_ptr1,int_ptr2�̃T�C�Y��size1
		http_ecg_pls_msg1.payload_size1 = SIZE_OF_SEND_ECG_PLS;
		http_ecg_pls_msg2.payload_size1 = SIZE_OF_SEND_ECG_PLS;
		http_ecg_pls_msg3.payload_size1 = SIZE_OF_SEND_ECG_PLS;
		http_ecg_pls_msg4.payload_size1 = SIZE_OF_SEND_ECG_PLS;

		// �L���[�Ƀf�[�^������
		http_ecg_pls_fifo.putRequest(http_ecg_pls_msg1);
		http_ecg_pls_fifo.putRequest(http_ecg_pls_msg2);
		http_ecg_pls_fifo.putRequest(http_ecg_pls_msg3);
		http_ecg_pls_fifo.putRequest(http_ecg_pls_msg4);

		// /////////////////////////////////////////////////////
		// HR,BP ���b�Z�[�W�̏�����
		http_hr_bp_msg1.clear();
		http_hr_bp_msg2.clear();
		http_hr_bp_msg3.clear();
		http_hr_bp_msg4.clear();

		// ���b�Z�[�W��ݒ�
		http_hr_bp_msg1.payload_int_ptr1 = hr_hbuf1;
		http_hr_bp_msg2.payload_int_ptr1 = hr_hbuf2;
		http_hr_bp_msg3.payload_int_ptr1 = hr_hbuf3;
		http_hr_bp_msg4.payload_int_ptr1 = hr_hbuf4;

		http_hr_bp_msg1.payload_int_ptr2 = bp_hbuf1;
		http_hr_bp_msg2.payload_int_ptr2 = bp_hbuf2;
		http_hr_bp_msg3.payload_int_ptr2 = bp_hbuf3;
		http_hr_bp_msg4.payload_int_ptr2 = bp_hbuf4;

		// int_ptr1,int_ptr2�̃T�C�Y��size1
		http_hr_bp_msg1.payload_size1 = SIZE_OF_SEND_HR_BP;
		http_hr_bp_msg2.payload_size1 = SIZE_OF_SEND_HR_BP;
		http_hr_bp_msg3.payload_size1 = SIZE_OF_SEND_HR_BP;
		http_hr_bp_msg4.payload_size1 = SIZE_OF_SEND_HR_BP;

		// �L���[�Ƀf�[�^������
		http_hr_bp_fifo.putRequest(http_hr_bp_msg1);
		http_hr_bp_fifo.putRequest(http_hr_bp_msg2);
		http_hr_bp_fifo.putRequest(http_hr_bp_msg3);
		http_hr_bp_fifo.putRequest(http_hr_bp_msg4);
		
	
		// /////////////////////////////////////////////////////
		// FFT ECG ���b�Z�[�W�̏�����
		fft_ecg_msg1.clear();
		fft_ecg_msg2.clear();
		fft_ecg_msg3.clear();
		fft_ecg_msg4.clear();		

		// ���b�Z�[�W��ݒ�
		fft_ecg_msg1.payload_double_ptr1 = fft_ecg_hbuf1;
		fft_ecg_msg2.payload_double_ptr1 = fft_ecg_hbuf2;
		fft_ecg_msg3.payload_double_ptr1 = fft_ecg_hbuf3;
		fft_ecg_msg4.payload_double_ptr1 = fft_ecg_hbuf4;

		// int_ptr1,int_ptr2�̃T�C�Y��size1
		fft_ecg_msg1.payload_size1 = SIZE_OF_SEND_FFT;
		fft_ecg_msg2.payload_size1 = SIZE_OF_SEND_FFT;
		fft_ecg_msg3.payload_size1 = SIZE_OF_SEND_FFT;
		fft_ecg_msg4.payload_size1 = SIZE_OF_SEND_FFT;

		// �L���[�Ƀf�[�^������
		fft_ecg_fifo.putRequest(fft_ecg_msg1);
		fft_ecg_fifo.putRequest(fft_ecg_msg2);
		fft_ecg_fifo.putRequest(fft_ecg_msg3);
		fft_ecg_fifo.putRequest(fft_ecg_msg4);
		
		HttpTSK.http_ecg_pls_send_flag = false;
		HttpTSK.http_hr_bp_send_flag = false;
		FFTTSK.fft_send_flag = false;
	}

	/**
	 * ����������
	 */
	public void init(){

		//�`��o�b�t�@�̏�����
		InitGraphBuffer();

		//�v�Z�p�o�b�t�@�̏�����
		for(int i = 0; i < CALC_BUF_SIZE ;i++){
			ECGBuf[i] = 0;
			PLSBuf[i] = 0;
			notchECGBuf[i] = 0;
			hpfECGBuf[i] = 0;
		}
		//�S���A���g�`�d���ԗp�o�b�t�@���N���A
		patHRBuf.removeAllElements();

		//RPAT�o�b�t�@�̏�����
		for(int i = 0; i < rPATBuf.length ; i++){
			rPATBuf[i] = 0;
		}
		//RHR�o�b�t�@�̏�����
		for(int i = 0; i < rHRBuf.length ; i++){
			rHRBuf[i] = 0;
		}

		//���b�Z�[�W�L���[�����
		dec_buf_fifo.clear();

	}//init


	/**
	 * �`��o�b�t�@��������
	 */
	public void InitGraphBuffer(){

		//�`��p�o�b�t�@�̏�����(�J�����g���) (��ʂ̕�*�ő�̏k���{�����̃o�b�t�@���m��)
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

		//�`��p�o�b�t�@�̏�����(�g�����h���) (��ʂ̕����̃o�b�t�@���m��)
		width = canvas.getWidth();
		hrGraBuf  = new double[width];
		patGraBuf = new double[width];
		sbpGraBuf = new double[width];
		for(int i = 0; i < width ; i++){
			hrGraBuf[i]  = 0;
			patGraBuf[i] = 0;
			sbpGraBuf[i] = 0;
		}

		//Manager�̕`��o�b�t�@��������
		//�g�`�p
		BPMonitorManager.ecgGraBuf=ecgGraBuf;
		BPMonitorManager.plsGraBuf=plsGraBuf;
		//�s�[�N�p
		BPMonitorManager.rWavePeekGraBuf=rWavePeekGraBuf;
		BPMonitorManager.hpfRWavePeekGraBuf=hpfRWavePeekGraBuf;
		//HR,PAT�l�p
		BPMonitorManager.hr=hr;
		BPMonitorManager.pat=pat;
		//�g�����g�p
		BPMonitorManager.hrGraBuf=hrGraBuf;
		BPMonitorManager.patGraBuf=patGraBuf;
		BPMonitorManager.sbpGraBuf=sbpGraBuf;

	}//InitGraphBuffer

	/**
	 * ���s�̊J�n
	 */
	public void start(){
		//����������
		init();
		//���s�t���O�𗧂Ă�
		runFlag = true;
		//�X���b�h�̋N��
		thread = new Thread(this);
		//���s�̊J�n
		thread.start();
	}

	/** MainLoope */
	public static final int SLEEP_TIME = 0;

	private static double bpcalc_time=0;

	public void run(){

		try{

			if(Main.DEBUG){
				//�f�o�b�O�pSD�̍쐬
				dbgSD = new SDCardMIDP(Main.IS_ACTUAL);
				dbgSD.open(DBG_SD_PATH,Connector.READ_WRITE);
			}

			//�X���[�v����
			long startTime = System.currentTimeMillis();
			//�v�Z�ɂ�����������
			long pastTime = 0;

			while(runFlag){

				//���b�Z�[�W���y���h
				new_msg=mbx_con.MsgPend(mbx_con.MBX_bpcalc,-1);

				startTime = System.currentTimeMillis();
				if(Main.DEBUG) dp2.StartTime1();

				switch(new_msg.msg_id){

					case MailBoxControl.MSG_BPCALC_DATA:
						dec_buf_fifo.putRequest(new_msg);
					break;

					//�g��ꂽ�o�b�t�@���ԋp�����
					case MailBoxControl.MSG_HTTP_ECG_PLS_ACK:
						http_ecg_pls_fifo.putRequest(new_msg);
					break;
					
					//�g��ꂽ�o�b�t�@���ԋp�����
					case MailBoxControl.MSG_HTTP_HR_BP_ACK:
						http_hr_bp_fifo.putRequest(new_msg);
					break;
					
					//�g��ꂽ�o�b�t�@���ԋp�����
					case MailBoxControl.MSG_FFT_ECG_ACK:
						fft_ecg_fifo.putRequest(new_msg);
					break;

					default:
						break;
				}/* End of Switch */

				//���b�Z�[�W�L���[���烁�b�Z�[�W�����o��
				new_msg=dec_buf_fifo.getRequest();

				if (new_msg!=null) {
					//���̎��_��dec_worked_msg�͈�O�̃��b�Z�[�W�ł���
					if(dec_worked_msg != null){
						mbx_con.AckMsgPost(dec_worked_msg);
					}
					dec_worked_msg = dec_reload_msg;
					dec_reload_msg = new_msg;

					//�v�Z����
					calc(dec_reload_msg.payload_double_ptr1,
							dec_reload_msg.payload_double_ptr2,
							dec_reload_msg.payload_size1);

					//�`��t���O�𗧂Ă�
					BPMonitorManager.bp_draw_on_flag=true;
				}//end of if

				//�v�Z�ɂ�����������
				pastTime = System.currentTimeMillis() - startTime;

				if(pastTime < SLEEP_TIME){
					//�x�~
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

	private int cnt = 0;
	/**
	 * �v�Z����
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
		//---------------��M�o�b�t�@����f�[�^���Ƃ肾���A�v�Z�p�z��Ɋi�[----------------//
		//�S�d�}�f�[�^���i�[����ꎞ�o�b�t�@
		double[] tmpNotchECG=new double[size];
		double[] tmpHpfECG=new double[size];
		cnt += size;

		//--------------------------�t�B���^�������s��-----------------------------//
		//�S�d�}�Ƀm�b�`�t�B���^���{��
		//notchECG[]�Ɍ��ʂ��i�[

		if(Option.getOp().is50HzPowerSupply()){//50Hz�̓d���m�C�Y������
			notchFilter.doFilter(tmpECG,tmpNotchECG,NotchFilter.NOTCH50_NUM,NotchFilter.NOTCH50_DEN);
		}else{                     //60Hz�̓d���m�C�Y������
			notchFilter.doFilter(tmpECG,tmpNotchECG,NotchFilter.NOTCH60_NUM,NotchFilter.NOTCH60_DEN);
		}

		//�n�C�p�X�t�B���^.����ϓ�����菜��
		//hpfECG[]�Ɍ��ʂ��i�[
		highPassFilter.filterRapper(tmpNotchECG,tmpHpfECG,HighPassFilter.COFF);

		//---------------------�o�b�t�@�̍X�V-----------------------//

		//���g�o�b�t�@(�R�̃o�b�t�@�͓����T�C�Y)�̃V�t�g
		//�m�b�`�t�B���^�K�p�̐S�d�}�o�b�t�@�̃V�t�g
		//�n�C�p�X�t�B���^�K�p�̐S�d�}�o�b�t�@�̃V�t�g
		//臒l�p�o�b�t�@�̃V�t�g
		for(int i = 0 ; i < CALC_BUF_SIZE - size ; i++){
			PLSBuf[i] = PLSBuf[i+size];
			notchECGBuf[i] = notchECGBuf[i+size];
			hpfECGBuf[i] = hpfECGBuf[i+size];
			ECGBuf[i] = ECGBuf[i+size];
			thresholdECGBuf[i] = thresholdECGBuf[i+size];
		}

		//R�g�̃s�[�N�̃V�t�g
		//�n�C�p�X�t�B���^�K�p�S�d�}��R�g�̃s�[�N�̃V�t�g
		for(int i = 0 ; i < rWavePeekGraBuf.length - size  ; i++){
			rWavePeekGraBuf[i] = rWavePeekGraBuf[i+size];
			hpfRWavePeekGraBuf[i] = hpfRWavePeekGraBuf[i+size];
		}

		//�V�����f�[�^������(�ŐV�̃f�[�^�͈�ԍŌ�)
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

			//HTTP�ɑ��� sone1207 abe1212
			/*
			if(hnew_ecg_pls_msg == null){
				System.out.println("hnew_ecg_pls_msg=null");
			}
			*/

			//ECG,PLS�̑��M
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
			//FFT ECG�̑��M
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

			//---------�q�g�s�[�N�����Ƃ߂邽�߂�臒l�̐ݒ肪������Ă���Ƃ�------//
			for(int i = 0 ; i < size ; i++){
				//--------�S�d�}��R�g�s�[�N�A���g�̗����オ��_�����Ƃ߂�-------//
				int bufIndex = CALC_BUF_SIZE - size + i;

				//--------RR�Ԋu�J�E���g�p�ϐ����X�V--------//
				if(rrCounter++ > MAX_RR_COUNTER){
					//��莞��R�g�s�[�N�����o����Ȃ�������
					//臒l�����Z�b�g
					rrCounter = 0;
					//臒l�̌v�Z�����t���O�����Z�b�g
					isInitThreshold = false;
				}

				//-------臒l�̌��o�AR�g�s�[�N�̌��o--------//
				switch (rWaveDetectionMode) {

				//////////臒l���o���[�h�̂Ƃ�//////////////
				case THRESHOLD_DETECT_MODE:

					if(hpfECGBuf[bufIndex] > threshold){
						//臒l�𒴂����Ƃ� (�t�B���^�K�p�̐S�dR�g�s�[�N���o���[�h��)
						rWaveDetectionMode = FILTERED_ECG_R_DETECT_MODE;
					}
					break;

				////�t�B���^�K�p�̐S�dR�g�s�[�N���o���[�h�̂Ƃ�////
				case FILTERED_ECG_R_DETECT_MODE:

					if(hpfECGBuf[bufIndex] < threshold){
						//臒l�𒴂��Ȃ��Ȃ����Ƃ�

						//�t�B���^�K�p�S�d�}�̍ő�l(R�g�s�[�N)���m�肷��
						double max = hpfECGBuf[bufIndex];
						//�ő�l�̃C���f�b�N�X(R�g�s�[�N�̃C���f�b�N�X)
						int rIndex = bufIndex;
						//�ő�l�̌���(�߂�Ȃ��猟��)
						for(int j = bufIndex-1; j > 0 ; j--){
							if (max < hpfECGBuf[j]){
								max = hpfECGBuf[j];
								rIndex = j;
							}else{
								break;
							}
						}

						//�`��p�ɃC���f�b�N�X��ۑ�
						//(CALC_BUF_SIZE - 1) = �o�b�t�@�̍Ō�̃C���f�b�N�X(=�ŐV�̃f�[�^)
						//�@�@�@�@�@�@�@�@�@�@�@�@ index = �o�b�t�@����R�g�s�[�N�̃C���f�b�N�X
						//           graIndex = R�g�s�[�N���擾���Ă���A�ŐV�̃f�[�^�܂ł̌o�ߎ���
						int graIndex = (CALC_BUF_SIZE - 1) - rIndex;
						//�`��p�̃C���f�b�N�X���A�o�b�t�@�̍Ō�̃C���f�b�N�X���ŐV�̃f�[�^������A
						//R�g�̕`��p�C���f�b�N�X = �`��o�b�t�@�̍Ō�̃C���f�b�N�X - R�g���擾���Ă���̎���
						hpfRWavePeekGraBuf[(hpfRWavePeekGraBuf.length - 1) - graIndex] = 1;

						//--���̐S�d�}��R�g�̃s�[�N�����Ƃ߂�--//
						//(�n�C�p�X�t�B���^�̌Q�x���������߂��āA���̎��ӂŌ��̐S�d�}��R�g�s�[�N�����Ƃ߂�)

						//�����J�n�n�_
						//tmpIndex = i �̃f�[�^���ŐV
						//tmpIndex = �t�B���^�K�pR�g�s�[�N�擾�n�_ - �n�C�p�X�̌Q�x���T���v�� - �����J�n�n�_)
						int tmpIndex = rIndex - HighPassFilter.GROUP_DELAY - TIME_START_DETECT_R;
						//�������钷��
						int len = TIME_START_DETECT_R + TIME_END_DETECT_R + 1;
						//�ő�l
						max = ECGBuf[tmpIndex];
						//�ő�l�̃C���f�b�N�X
						rIndex = tmpIndex;
						//�ő�l������
						for(int j = tmpIndex; j < tmpIndex + len ; j++ ){
							if(max < ECGBuf[j]){
								max = ECGBuf[j];
								rIndex = j;
							}
						}

						//RR�Ԋu
						//             (bufIndex - index) = R�g�s�[�N���擾���Ă���o�߂�������
						// rrCounter - (bufIndex - index) = RR�Ԋu
						rrCounter = rrCounter - (bufIndex - rIndex);

						//���g�`�����Ԃ��v�Z���邩�ǂ����̃t���O
						boolean patFlag = false;
						//�O���R�s�[�N�n�_(���g�`�����Ԍv�Z�p)
						int prevRIndex = rIndex - rrCounter;

						//�O���RR�Ԋu��1/2�ȉ��܂��́A1.5�{�ȏ�̏ꍇ��
						//�A�[�e�B�t�@�N�g�Ƃ݂Ȃ��Ė�������B�܂����o���Ă���
						//�ŏ��̈��͌v�Z�������Ă��Ȃ��̂Ŗ�������
						if((rrCounter > rrInterval/2 && rrCounter < (rrInterval*3/2))
						   || rrInterval == FIRST_R_DETECTION){
							//RR�Ԋu��ۑ�
							rrInterval = rrCounter;
							//�S����
							hr = (NUM_OF_DATA_PER_MIN/(rrCounter));
							//�J�E���^�����Z�b�g(R�g�s�[�N�����o���Ă��猻�݂܂Ōo�߂������Ԃ𑫂��Ă���)
							rrCounter = bufIndex - rIndex;
							//�`��p�ɃC���f�b�N�X��ۑ�
							graIndex = (hpfECGBuf.length - rIndex - 1);
							rWavePeekGraBuf[rWavePeekGraBuf.length - 1 - graIndex] = 1;
							//���g�`�����Ԃ̌v�Z���s��
							patFlag = true;

						}else if(rrInterval == ZERO_R_DETECTION){
							//���o���n�߂Ă���̍ŏ��̈��͎̂Ă�
							//�J�E���^�����Z�b�g(R�g�s�[�N�����o���Ă��猻�݂܂Ōo�߂������Ԃ𑫂��Ă���)
							rrCounter = bufIndex - rIndex;
							rrInterval = FIRST_R_DETECTION;

						}else if(rrCounter >= (rrInterval*3/2)){
							//�O���RR�Ԋu����1.5�{�ȏ�̂Ƃ��̓��Z�b�g
							rrInterval = ZERO_R_DETECTION;
							rrCounter = 0;
						}

						//���g�̗����オ��_�����߂�B
						if(patFlag){
							//�Œ�_�̃C���f�b�N�X
							int minIndex = prevRIndex;
							//�Œ�_
							double min = PLSBuf[minIndex];

							//�O���R�g�̃s�[�N�̃C���f�b�N�X����A�����R�g�̃C���f�b�N�X��
							//�Ԃ̖��g�`�����Ԃ̍Œ�_�𖬔g�̗����オ��_�Ƃ���B
							for(int j = prevRIndex + 1 ; j <= rIndex ; j++){
								if(PLSBuf[j] <= min){
									min = PLSBuf[j];
									minIndex = j;
								}
							}
							System.out.println((minIndex - prevRIndex));
							//���g�`�d����
							pat = (minIndex - prevRIndex)*(1000/SAMPLE);

							//�S���A���g�`�d���Ԃ̎擾����
							double time = (double)60/(double)hr;
							//�S���A���g�`�d���ԃo�b�t�@�ɒǉ�
							patHRBuf.addElement(new PAT_HR(pat,hr,time));
						}

						//----------------------------���g�`�d���Ԃ̃��X�P�[��--------------------------//
						
						//�o�b�t�@�T�C�Y
						int bufSize  = patHRBuf.size();
						
						//�S���A���g�`�d���Ԃ̎擾���Ԃ̍��v
						double total = 0;						
						for(int j = 0; j < bufSize; j++){
							total += ((PAT_HR)(patHRBuf.elementAt(j))).getTime();
						}

						//���v�擾���Ԃ�1�𒴂�����A���g�`�d���Ԃ������̂Ƃ���
						//�S���A���g�`�d���Ԃ����X�P�[�����ċ��߂�												
						if(total >= 1 && bufSize >= 2){
							
							//���X�P�[�����g�`�d���ԃo�b�t�@�̃V�t�g
							for(int j =  rPATBuf.length - 1 ; j >= (int)total ; j--){
								rPATBuf[j] = rPATBuf[j - (int)total];
							}
							
							//���X�P�[���S�����o�b�t�@�̃V�t�g
							for(int j =  rHRBuf.length - 1 ; j >= (int)total ; j--){
								rHRBuf[j] = rHRBuf[j - (int)total];
							}
							
							//---------------------���g�`�d���Ԃ̃��X�P�[��---------------------//
							double x  = 1;
							double x0 = total-((PAT_HR)(patHRBuf.elementAt(bufSize-1))).getTime();
							double y0 = ((PAT_HR)(patHRBuf.elementAt(bufSize-2))).getPat();
							double x1 = total;
							double y1 = ((PAT_HR)(patHRBuf.elementAt(bufSize-1))).getPat();

							for(int j = 0; j < (int)total && j < rPATBuf.length ; j++){
								rPATBuf[j] =  y0 + ((y1-y0)/(x1-x0))*(x-x0);
								x++;
							}
							
							//------------------------�S�����̃��X�P�[��-----------------------//
							x  = 1;
							x0 = total-((PAT_HR)(patHRBuf.elementAt(bufSize-1))).getTime();
							y0 = ((PAT_HR)(patHRBuf.elementAt(bufSize-2))).getHR();
							x1 = total;
							y1 = ((PAT_HR)(patHRBuf.elementAt(bufSize-1))).getHR();

							for(int j = 0; j < (int)total && j < rHRBuf.length ; j++){
								rHRBuf[j] =  y0 + ((y1-y0)/(x1-x0))*(x-x0);
								x++;
							}

							//�Ō�̐S���A���g�`�d���Ԃ����o��
							PAT_HR last = (PAT_HR)(patHRBuf.elementAt(bufSize-1));
							//�S���A���g�`�d���ԃo�b�t�@���N���A
							patHRBuf.removeAllElements();
							//���̐S���A���g�`�d���Ԃ̍ŏ��̃f�[�^�ɂ���
							//�S���A���g�`�d���Ԃ̎擾���Ԃ�
							//�Ō�ɕۊǂ������Ԃ���̌o�ߎ��Ԃ̏����_������
							double next = last.getTime()-(int)(last.getTime());
							patHRBuf.addElement(new PAT_HR(last.getPat(),last.getHR(),next));

							//���k�������̕ω����̌v�Z
							//�t�B���^�̌v�Z�����s���A�����̕ω��l���v�Z����B
							double[] output = new double[rPATBuf.length];
							sbpFilter.doFilter(rPATBuf,output,BPEstimationFilter.NUM,BPEstimationFilter.DEN);
							
							//�ŐV�̎��k�������l
							sbp = output[0];

							//���X�P�[�������������ۑ�
							for(int j = (int)(total - 1); j >= 0 ; j-- ){
								//HTTP�ő��M����f�[�^���o�b�t�@�ɓ��ꍞ��
								if(hnew_hr_bp_msg != null){
									//HR
									hnew_hr_bp_msg.payload_int_ptr1[hnew_hr_bp_msg.msg_count1++]=(int)rHRBuf[j];
									//SBP
									hnew_hr_bp_msg.payload_int_ptr2[hnew_hr_bp_msg.msg_count2++]=(int)(output[j] + baseSBP);
									//100���̃f�[�^�����܂����瑗�M
									if(hnew_hr_bp_msg.msg_count1 == hnew_hr_bp_msg.payload_size1){										
										System.out.println("hnew_hr_bp_msg.msg_count1="+hnew_hr_bp_msg.msg_count1);
										mbx_con.WriteMsgPost(MailBoxControl.MSG_HTTP_HR_BP_DATA,hnew_hr_bp_msg,mbx_con.MBX_bpcalc);
										hnew_hr_bp_msg.count_clear();
										HttpTSK.http_hr_bp_send_flag = false;										
									}
								}								
							}
						}
						//臒l���o���[�h�ɖ߂�
						rWaveDetectionMode = THRESHOLD_DETECT_MODE;
					}
					break;
				}
				//--------臒l�̌��o�AR�g�s�[�N�̌��o�I���----------//
			}
		}else{
			//---�q�g�s�[�N�����Ƃ߂邽�߂�臒l�̐ݒ肪������Ă��Ȃ��Ƃ�------//
			if(thCounter >= CALC_BUF_SIZE){
				//----臒l�ݒ�̂��߂ɏ\���f�[�^������Ƃ�----//

				//�ő�l�����߂�(�ŏ���2�b�͎̂Ă�)
				//�J�E���^�͏��߂�臒l�����Ƃ߂�Ƃ��̂ݎg�p�B
				//�ȍ~臒l�����Z�b�g���ꂽ��A�����ɂ��̃u���b�N��
				//����A臒l���v�Z���Ȃ���
				double max = 0;
				for(int j = SAMPLE*2; j < CALC_BUF_SIZE ; j++ ){
					if(thresholdECGBuf[j] > max){
						max = thresholdECGBuf[j];
					}
				}
				//�ő�l*THHRESHOLD���ŏ���臒l�Ɛݒ肷��
				threshold = max * THRESHOLD_COFF;
				//臒l�ݒ芮���t���O�𗧂Ă�
				isInitThreshold = true;
			}else{
				//----臒l�ݒ�̂��߂ɏ\���f�[�^���܂��Ȃ���----//
				//臒l�p�J�E���^�̍X�V
				thCounter+= size;
			}
		}


		//�����l�̌v�Z
		//-----------------------------�`��o�b�t�@�̍X�V------------------------------//
		//�f�[�^�̃V�t�g(�V���ɓ����f�[�^���V�t�g����)
		//���̃o�b�t�@���ڂ�����Ԃł́A�z��̌���size�����������f�[�^���d�����Ă���
		//1 2 3 4 5 6 7 8 9 10 11 12 13 14 15
		//o o o o o o o o o o  o  o  *  *  *
		//    x x x x x x x x  x  x  *  *  *
		//x x x x x x x x x *  *  *  *  *  *
		//�̏�Ԃɂ���B13,14,15�́A���ōX�V�����
		for(int i = 0 ; i < ecgGraBuf.length - size; i++){
			ecgGraBuf[i] = ecgGraBuf[i+size];
			plsGraBuf[i] = plsGraBuf[i+size];
		}
		//�V���ȃf�[�^������
		for(int i = 0; i < size ; i++){
			//�S�d�}������
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
			//���g������
			plsGraBuf[index] = tmpPLS[i];
		}

		//--------------------�`��o�b�t�@�ւ̔��f-------------------------//
		//sone1201
		//�g�`�p
		BPMonitorManager.ecgGraBuf=ecgGraBuf;
		BPMonitorManager.plsGraBuf=plsGraBuf;
		//�s�[�N�p
		BPMonitorManager.rWavePeekGraBuf=rWavePeekGraBuf;
		BPMonitorManager.hpfRWavePeekGraBuf=hpfRWavePeekGraBuf;
		//HR,PAT�l�p
		BPMonitorManager.hr=hr;
		BPMonitorManager.pat=pat;
		BPMonitorManager.sbp = (int)(sbp + (double)baseSBP);
		//�g�����g�p
		BPMonitorManager.hrGraBuf=hrGraBuf;
		BPMonitorManager.patGraBuf=patGraBuf;
		BPMonitorManager.sbpGraBuf=sbpGraBuf;
	}

	/**
	 * �v�Z�̒��~
	 */
	public void stop(){
		runFlag = false;
	}

	/**
	 * ���s�����ǂ���
	 * @return ���s�����ǂ�����Ԃ�
	 */
	public boolean isRunFlag() {
		return runFlag;
	}

}//end of BpCalcTSK
