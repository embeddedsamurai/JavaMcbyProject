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

	//=================================�萔==================================//
	//--------------��ʂ̃��[�h----------------//
	/** �����ʃ��[�h*/
	private static final int CURRENT_DISP  = 0;
	/** �g�����h��ʃ��[�h */
	private static final int TREND_DISP    = 1;
	/** �X�y�N�g����ʃ��[�h */
	private static final int SPECTRUM_ECG_DISP = 2;
	/** ���݂̃��[�h */
	private int mode = CURRENT_DISP;

	//-----------------���N�G�X�g----------------//
	/**	�S�d�}�̕\�����[�h��ύX���郊�N�G�X�g */
	private static final int ECG_MODE_CHANGE_REQ = 0;
	/**	x�������̏k������傫�����郊�N�G�X�g */
	private static final int INCREMENT_X_REDUCTION_REQ = 1;
	/**	x�������̏k���������������郊�N�G�X�g */
	private static final int DECREMENT_X_REDUCTION_REQ = 2;
	
	//--------------�\�����̐S�d�}--------------//
	/** �S�d�}��\���萔 */
	public static final int RAW_ECG = 0;
	/** �m�b�`�t�B���^���������S�d�}��\���萔 */
	public static final int NOTCH_ECG = 1;
	/** �m�b�`�t�B���^�ƃn�C�p�X�t�B���^���������S�d�}��\���萔 */
	public static final int HPF_ECG = 2;
	/** �\������M��(�f�t�H���g�ł͐��̐S�d�}) */
	public static int displayECGSignal = RAW_ECG;

	//=================================�ϐ�==================================//
	/** �L�����o�X */
	private BPMCanvas canvas;
	/** BT�ڑ���ʂŎg�p����L�����o�X */
	private BluetoothCanvas btCanvas;
	/** �e�X���b�h */
	private Main parent;
	/** ���s�����ۂ��̃t���O*/
	private boolean runFlag = false;

	//------Current���[�h�̕`��o�b�t�@------//
	/** �S�d�}�M���`��p�o�b�t�@ */
	public static double[] ecgGraBuf;
	/** ���g�M���`��p�o�b�t�@ */
	public static double[] plsGraBuf;
	/**	R�g�s�[�N��ێ����郊�X�g (R�g�����o�����Ƃ���1������)*/
	public static int[] rWavePeekGraBuf;
	/**	�n�C�p�X�t�B���^�K�p�S�d�}��R�g�s�[�N��ێ����郊�X�g
	 * (R�g�����o�����Ƃ���1������)*/
	public static int[] hpfRWavePeekGraBuf;

	//-------Trend���[�h�̕`��o�b�t�@-------//
	/** �S�����`��p�o�b�t�@ */
	public static double[] hrGraBuf ;
	/** ���g�`�����ԕ`��p�o�b�t�@ */
	public static double[] patGraBuf;
	/** �����l�`��p�o�b�t�@ */
	public static double[] sbpGraBuf;
	/** ���݂̐S�����l*/
	public static int hr  = -1;
	/** ���݂̌����l */
	public static double sbp = -1;
	/** ���݂̖��g�`�����Ԓl */
	public static int pat = -1;
	
	//--------------�R�}���h---------------//
	/** ��~�R�}���h */
	private Command stopCom;
	/** �J�n�R�}���h */
	private Command startCom;
	/** �I���R�}���h */
	private Command exitCom;
	/** �ؑփR�}���h */
	private Command changeCom;

	/** ���N�G�X�g��ێ����Ă����L���[ */
	private RequestQueue keyrequestQueue=null;

	/** �󂯎��p�i�C���X�^���X�����Ȃ��j*/
	private Message hmsgin=null,hmsgout=null;

	/** MailBox�i�ߓ�(�C���X�^���X�����) */
	private MailBoxControl mbx_con=null;
	private State utState=null;

	/** �o�b�t�@�����O�p�^�X�N */
	private BPCalcTSK    bpcalcTSK=null;
	private BluetoothTSK btTSK =null; 
	private SDCardTSK    sdTSK=null;
	private FFTTSK       fftTSK=null;
	private HttpTSK      httpTSK=null;

	/** calc���I�������BPCalcTSK��true�ɂ���t���O */
	//�v�Z���Ă��Ȃ��Ƃ��͏��true
	public static boolean bp_draw_on_flag=true;

	//==============================����������================================//

	/**
	 * @param parent
	 */
	public BPMonitorManager(Main parent,MIDlet midlet) {
		try{
			this.parent = parent;

			//�L�����o�X�̍쐬(�͂��߂͑�����)
			canvas = new BPMCanvas(midlet);
			//BT�L�����o�X�̍쐬
			btCanvas = new BluetoothCanvas(midlet);
		
			//�R�}���h�̍쐬
			stopCom = new Command("���f",Command.BACK,1);
			startCom = new Command("�J�n",Command.BACK,1);
			exitCom = new Command("�I��",Command.BACK,0);
			changeCom = new Command("�ؑ�",Command.SCREEN,1);		
			//�L�����o�X�ɃR�}���h��ǉ�
			canvas.addCommand(startCom);
			canvas.addCommand(changeCom);
			canvas.setCommandListener(this);

			//���N�G�X�g�L���[
			keyrequestQueue = new RequestQueue();
			mbx_con         = new MailBoxControl();
		
			//�v�Z�����s����^�X�N
			bpcalcTSK = new BPCalcTSK(mbx_con,canvas);
			//FFT�����s����^�X�N
			fftTSK    = new FFTTSK(mbx_con,canvas);
			//SD����̃f�[�^�̓ǂݍ���
			sdTSK     = new SDCardTSK(mbx_con,canvas);
			//Bluetooth����̃f�[�^�̓ǂݍ���
			btTSK     = new BluetoothTSK(mbx_con,canvas,btCanvas);
			//HTTP�ւ̃f�[�^����M
			httpTSK   = new HttpTSK(mbx_con,canvas);

			//���̑��ϐ��̏�����
			init();
		
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * �ϐ��̏���������
	 */
	public void init(){
		//�L�[��������
		Key.init();
		//���N�G�X�g�L���[�����
		keyrequestQueue.clear();
	}

	//==============================���s����================================//
	/**
	 * �J�n
	 */
	public void start(){
		if(Main.DEBUG)System.out.println("BPMonitorManager#start()");

		//���Ɏ��s���̂Ƃ��͎��s���Ȃ�
		if(runFlag)return;

		//���s�t���O�𗧂Ă�
		runFlag = true;

		//�f�[�^��M�̊J�n(�V�����X���b�h�Ŏ��s)
		if(Option.getOp().isBluetooth()){
			//blue tooth�ł̎�M			 
			btTSK.start();
			//�J�n�̃��b�Z�[�W���|�X�g
			mbx_con.ControlMsgPost(MailBoxControl.MSG_BT_PLAY, new Message(), mbx_con.MBX_main);
		}else{
			//SD�J�[�h�̓ǂݍ��݂̊J�n
			sdTSK.start();
			//�J�n�̃��b�Z�[�W���|�X�g
			mbx_con.ControlMsgPost(MailBoxControl.MSG_SD_PLAY, new Message(), mbx_con.MBX_main);
		}
		//�v�Z�̊J�n
		bpcalcTSK.start();
		//FFT�̊J�n
		fftTSK.start();
		//HTTP�^�X�N�̊J�n
		httpTSK.start();
		
		//�ϐ��̏�����
		init();

		//�J�n�R�}���h���폜
		canvas.removeCommand(startCom);
		//���f�A�I���R�}���h��ǉ�
		canvas.removeCommand(exitCom);
		canvas.addCommand(stopCom);
	}

	/**
	 * ���f����
	 */
	public void stop(){
		if(Main.DEBUG)System.out.println("BPMonitorManager#stop()");
		
		//���s���~�߂�
		runFlag = false;

		//���s�𒆎~����						
		if(Option.getOp().isBluetooth()){
			//Bluetooth�̂Ƃ�
			btTSK.stop();
		}else{
			//SD�J�[�h�̂Ƃ�
			sdTSK.stop();	
		}
		bpcalcTSK.stop();
		fftTSK.stop();
		httpTSK.stop();
		
		//�`��t���O���I����(�v�Z���Ă��Ȃ��Ƃ��͏��true)
		bp_draw_on_flag = true;
			
		//�X�g�b�v�R�}���h���폜
		canvas.removeCommand(stopCom);
		//�J�n�R�}���h�A�I���R�}���h��ǉ�
		canvas.addCommand(startCom);
		canvas.addCommand(exitCom);
	}

	/**
	 * �I������
	 */
	public void exit(){
		if(Main.DEBUG)System.out.println("BPMonitorManager#exit()");
		
		//�^�C�g�����[�h�ɖ߂�
		parent.setMode(Main.TITLE_MODE);
	}


	//==============================���C������================================//
	/**
	 * �`��A�v�Z�Ȃǂ̈�A�̏������s��
	 */
	public void process(){
		try {
			//�L�[���X�V
			if(canvas.isShown())Key.registKeyEvent();
			//�L�[����
			key();
			//���N�G�X�g�̏���
			doRequest();
			//���b�Z�[�W�̏���
			returnAction();
			//�`��			
			if(bp_draw_on_flag)draw();
			//�v�Z���s���͕`��t���O�𖈉�I�t�ɂ���
			if(runFlag)bp_draw_on_flag=false;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void returnAction(){
		//���b�Z�[�W�̎��
		hmsgin = mbx_con.MsgPend(mbx_con.MBX_main,10);
		//���b�Z�[�W���Ȃ��Ȃ炨���܂�
		if (hmsgin==null) return;
		
		switch (hmsgin.msg_id)
		{
			case MailBoxControl.MSG_SD_PLAY_ACK: //SD����̓ǂݍ��݊J�n
				System.out.println("MSG_SD_PLAY_ACK");
				runFlag = true;
				break;
			case MailBoxControl.MSG_BT_PLAY_ACK: //BT����̓ǂݍ��݊J�n
				System.out.println("MSG_BT_PLAY_ACK");
				runFlag = true;
				break;
			default:
				break;
		}
	}

	/**
	 * ���N�G�X�g�̏���������
	 */
	private void doRequest(){
		//�L���[���烊�N�G�X�g�����o��
		Request req = keyrequestQueue.getRequest();

		if(req != null){
			//���N�G�X�g������Ƃ�
			switch (req.getCommand()) {
			case ECG_MODE_CHANGE_REQ:
				//�\������S�d�}��ύX����
				displayECGSignal = (displayECGSignal+1)%3;
				break;
			case INCREMENT_X_REDUCTION_REQ:
				//x�������̏k������傫������
				canvas.setXReductionRate(canvas.getXReductionRate()+1);
				break;
			case DECREMENT_X_REDUCTION_REQ:
				//x�������̏k����������������
				canvas.setXReductionRate(canvas.getXReductionRate()-1);
				break;
			}
		}
	}


	/**
	 * �`�揈��
	 */
	private void draw() {
		
		if(!canvas.isShown())return; //�\�����łȂ����͉������Ȃ�
		
		if(mode == CURRENT_DISP){
			//�����ʂ�`��
			if(displayECGSignal == HPF_ECG){
				canvas.drawCurrent(ecgGraBuf,plsGraBuf,hpfRWavePeekGraBuf,hr,pat,sbp);
			}else{
				canvas.drawCurrent(ecgGraBuf,plsGraBuf,rWavePeekGraBuf,hr,pat,sbp);
			}
		}else if(mode == TREND_DISP){
			//�g�����h��ʂ�`��
			canvas.drawTrend(hrGraBuf,patGraBuf,sbpGraBuf);
		}else if(mode == SPECTRUM_ECG_DISP){
			//�S�d�}�̃X�y�N�g����ʂ�`��
			if(displayECGSignal == HPF_ECG){
				canvas.drawECGSpectrum(ecgGraBuf,hpfRWavePeekGraBuf,hr);
			}else{
				canvas.drawECGSpectrum(ecgGraBuf,rWavePeekGraBuf,hr);
			}
		}
	}
	
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

	//==============================�L�[����================================//
	/**
	 * �R�}���h����������
	 * @param com �R�}���h
	 * @param disp �R�}���h��ێ�����f�B�X�v���[�I�u�W�F�N�g�̎Q��
	 *
	 */
	public void commandAction(Command com, Displayable disp) {
		if(Main.DEBUG)System.out.println("BPMonitorManager#commandAction()");
		
		if(!canvas.isShown())return; //�\�����łȂ����͂Ȃɂ����Ȃ�
						
		if(com.equals(stopCom)){//���f�R�}���h���������Ƃ�
 			stop();
 		}else if(com.equals(startCom)){//�J�n�R�}���h���������Ƃ�
 			start();
 		}else if(com.equals(exitCom)){//�I���R�}���h���������Ƃ�
 			exit();
 		}else if(com.equals(changeCom)){//�؂�ւ��R�}���h���������Ƃ�
 			modeChange();
 		}
	}

	/**
	 * �L�[����������
	 */
	private void key(){

		if(!canvas.isShown())return; //�\�����łȂ����͂Ȃɂ����Ȃ�
		
		if(Key.isKeyPressed(Canvas.FIRE)){//����L�[���������Ƃ�
			if(mode == CURRENT_DISP || mode == SPECTRUM_ECG_DISP){//�J�����g�܂��̓X�y�N�g���\���̂Ƃ�
				//�\������S�d�}�g�`��ύX����
				keyrequestQueue.putRequest(new Request(ECG_MODE_CHANGE_REQ));
			}
		}else if(Key.isKeyPressed(Canvas.LEFT)){//���L�[���������Ƃ�
			if(mode 	== CURRENT_DISP || mode == SPECTRUM_ECG_DISP){//�J�����g�܂��̓X�y�N�g���\���̂Ƃ�
				//X�����̏k������傫��
				keyrequestQueue.putRequest(new Request(INCREMENT_X_REDUCTION_REQ));
			}
		}else if(Key.isKeyPressed(Canvas.RIGHT)){//�E�L�[���������Ƃ�
			if(mode == CURRENT_DISP || mode == SPECTRUM_ECG_DISP){//�J�����g�܂��̓X�y�N�g���\���̂Ƃ�
				//X�����̏k����������������
				keyrequestQueue.putRequest(new Request(DECREMENT_X_REDUCTION_REQ));
			}
		}//End of if					
	}//End of key()
	
	//===============================���̑�================================//

	/**
	 * �\������
	 */
	public void setDisplay(MIDlet midlet){
		if(Main.DEBUG)System.out.println("BPMonitorManager#setDisplay()");
		
		((BPMCanvas)canvas).setDisplay();		
	}

	/**
	 * ���[�h�̕ύX
	 */
	private void modeChange(){
		if(Main.DEBUG)System.out.println("BPMonitorManager#modeChange()");
		
		if(mode == CURRENT_DISP){
			//�X�y�N�g����ʂ֕ύX
			mode = SPECTRUM_ECG_DISP;
 		}else if(mode == SPECTRUM_ECG_DISP){
 			//�g�����h��ʂ֕ύX
 			mode = TREND_DISP;
 		}else if(mode == TREND_DISP){
 			//�J�����g��ʂ֕ύX
 			mode = CURRENT_DISP;
 		}
	}

}