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

	//=======================�萔==========================//
	/** �f�[�^��M�Ԋu */
	private static final int RECEIVE_INTERVAL = 50;
	/** ��x�Ɏ�M����f�[�^�� */
	private static final int SIZE_OF_RECEIVE_AT_A_TIME = 50;
	/** �L���[�̃T�C�Y */
	private static final int SIZE_OF_QUEUE = 3000;

	//BT�T�[�r�X�A�f�o�C�X
	/** �}�E�X�^�}�N�r�[�̃f�o�C�X����\�� */
	private static final String DEVICE_NAME_MCBY1 = "BiTEK";
	/** �}�E�X�^�}�N�r�[�̃T�[�r�X����\�� */
	private static final String SERVICE_NAME_MCBY1 = "Dev B";
	/** ZEAL�Ń}�N�r�[�̃f�o�C�X����\�� */
	private static final String DEVICE_NAME_MCBY2 = "Zeal";
	/** ZEAL�Ń}�N�r�[�̃T�[�r�X����\�� */
	private static final String SERVICE_NAME_MCBY2 = "SerialPort";

	//BT�ڑ���
	public final static int BT_IDLE               = 0;  // ���ڑ�
	public final static int BT_DEVICE_SEARCH      = 1;  // �f�o�C�X������
	public final static int BT_DEVICE_DISCOVERED  = 2;  // �f�o�C�X��������
	public final static int BT_SERVICE_SEARCH     = 3;  // �T�[�r�X������
	public final static int BT_SERVICE_DISCOVERED = 4;  // �T�[�r�X��������
	public final static int BT_CONNECTED          = 5;  // �ڑ���
	public final static int BT_ERROR              = 6;  //�G���[

	//�L�[�֘A
	/** �ꗗ�\�����ɑI�����ڂ����ɂ��� */
	private static final int INCRESE_SELECTION = 0;
	/** �ꗗ�\�����ɑI�����ڂ�����ɂ��� */
	private static final int DECREASE_SELECTION = 1;
	/** �ꗗ�\�����ɑI�����ڂɐڑ�����*/
	private static final int CONNECT_SELECTED_ITEM = 2;

	//=======================�ϐ�==========================//
	/** ���[���{�b�N�X����I�u�W�F�N�g */
	private MailBoxControl mbx_con;

	/** ���s�t���O */
	private boolean runFlag = false;
	/** ���C���L�����o�X�ւ̎Q�� */
	private BPMCanvas canvas;
	/** Bluetooth�p�L�����o�X */
	private BluetoothCanvas btCanvas;

	/** btCanvas�ɕ\�����郁�b�Z�[�W*/
	private String btMessage = "";
	private String errMessage = "";

	/** �ڑ��\��Bluetooth�f�o�C�X/�T�[�r�X��ێ�����z�� */
	private String[] menu;
	/** �ڑ��\��Bluetooth�f�o�C�X/�T�[�r�X�Ȃǂ�I�����郁�j���[�̃C���f�b�N�X*/
	private int menuIndex = 0;

	/** BT�ڑ��œ���ꂽ�f�[�^��ۑ����Ă����o�b�t�@ */
	private ByteQueue queue;
	/** ��M�����f�[�^�𔼕��ɂ���t���O*/
	private boolean bypathFlag = true;

	/** ���N�G�X�g��ێ�����L���[*/
	private RequestQueue reqQueue;

	/** ���f�R�}���h*/
	private Command exitCom;
	/** �T�[�r�X�����𒆒f����ۂɕK�v�ɂȂ�ID */
	private int transID = 0;

	//---------------BT�ڑ��֘A--------------//
	/** Bluetooth�ڑ��̓��̓X�g���[��*/
	private InputStream btIn;
	/** Bluetooth�ڑ��̏o�̓X�g���[��*/
	private OutputStream btOut;
	/** Bluetooth�̃R�l�N�V����*/
	protected StreamConnection conn = null;

	/** �X�e�[�^�X*/
	protected int status = BT_IDLE;

	/** ���������f�o�C�X�̂��߂̃}�b�v */
	protected Hashtable devices = new Hashtable();
	/** ���������f�o�C�X�̂��߂̃T�[�r�X */
	protected Hashtable services = new Hashtable();

	/** UUID */
	protected static final UUID[] UUID_SET = {new UUID("0003",true)};
	/** ATR SET (�T�[�r�X��) */
	protected static int[] ATTR_SET = {0x0100};

	/** �f�o�C�X*/
	protected static LocalDevice localDevice;
	/** Bluetooth�f�o�C�X�A�T�[�r�X�����̂��߂̃G�[�W�F���g */
	protected static DiscoveryAgent agent = null;
	/** �f�o�b�O�v�����g */
	private DebugPrint dp4=null;

	//--------------���b�Z�[�W�֘A---------------//
	private Message bt_indata_msg  = null;
	private Message bt_outdata_msg = null;
	private Message bt_msg         = null;

	/** Message�p */
	private MessageQueue bt_msg_fifo       = null;
	/** Read�p */
	private MessageQueue bt_indata_fifo    = null;
	/** Write�p */
	private MessageQueue bt_outdata_fifo   = null;

	/** BPCalcTSK �ɓn���f�[�^������ */
	private Message msg1=null;
	private Message msg2=null;
	private Message msg3=null;
	private Message msg4=null;
	private Message msg5=null;
	private Message msg6=null;

	/** �S�d�}��M�o�b�t�@ */
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

	/** SDCardTSK���N�������true�ɂȂ� */
	private static boolean enable_bt_task=false;
	
	private SDCardMIDP dbgSD3;
	private static final String DBG_SD_PATH3 ="dbg3.txt";

	//======================���\�b�h=========================//
	/**
	 * @param mbx_con ���[���{�b�N�X����
	 */
	public BluetoothTSK(MailBoxControl mbx_con,BPMCanvas canvas,BluetoothCanvas btCanvas) {
		this.mbx_con = mbx_con;
		this.canvas  = canvas;
		this.btCanvas = btCanvas;

		//BT�L�����o�X�̃��X�i�Ƃ��Đݒ�
		btCanvas.setCommandListener(this);
		//�R�}���h
		exitCom = new Command("���f",Command.SCREEN,0);
		btCanvas.addCommand(exitCom);

		//�S�d�}�f�[�^�o�b�t�@�̍쐬
		ecg_buf1=new double[SIZE_OF_RECEIVE_AT_A_TIME];
		ecg_buf2=new double[SIZE_OF_RECEIVE_AT_A_TIME];
		ecg_buf3=new double[SIZE_OF_RECEIVE_AT_A_TIME];
		ecg_buf4=new double[SIZE_OF_RECEIVE_AT_A_TIME];
		ecg_buf5=new double[SIZE_OF_RECEIVE_AT_A_TIME];
		ecg_buf6=new double[SIZE_OF_RECEIVE_AT_A_TIME];

		//���g�f�[�^�o�b�t�@�̍쐬
		pls_buf1=new double[SIZE_OF_RECEIVE_AT_A_TIME];
		pls_buf2=new double[SIZE_OF_RECEIVE_AT_A_TIME];
		pls_buf3=new double[SIZE_OF_RECEIVE_AT_A_TIME];
		pls_buf4=new double[SIZE_OF_RECEIVE_AT_A_TIME];
		pls_buf5=new double[SIZE_OF_RECEIVE_AT_A_TIME];
		pls_buf6=new double[SIZE_OF_RECEIVE_AT_A_TIME];

		//���b�Z�[�W�̍쐬
		msg1 = new Message();
		msg2 = new Message();
		msg3 = new Message();
		msg4 = new Message();
		msg5 = new Message();
		msg6 = new Message();

		//���b�Z�[�W�L���[�̍쐬
		bt_msg_fifo     = new MessageQueue();
		bt_indata_fifo  = new MessageQueue();
		bt_outdata_fifo = new MessageQueue();

		//BT�o�R�œ���ꂽ�f�[�^��ۑ����Ă����o�b�t�@
		queue = new ByteQueue(SIZE_OF_QUEUE);

		//���N�G�X�g��ێ�����L���[
		reqQueue = new RequestQueue();
		
		dp4 = new DebugPrint();

		//����������
		init();
	}

	/**
	 * ����������
	 */
	public void init(){

		//�S�d�}�A���g�̂��߂̃o�b�t�@�̏�����
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

		//���b�Z�[�W�L���[�̏�����
		bt_msg_fifo.clear();
		bt_indata_fifo.clear();
		bt_outdata_fifo.clear();

		//���b�Z�[�W�̏�����
		msg1.clear();
		msg2.clear();
		msg3.clear();
		msg4.clear();
		msg5.clear();
		msg6.clear();

		//���b�Z�[�W�̐ݒ�
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

		//�L���[�Ƀf�[�^������
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
	 * ���s���J�n����
	 */
	public void start() {
		//��ʂ�BT�ڑ���ʂɕύX
		btCanvas.setDisplay();
		//�X���b�h���N��
		Thread th = new Thread(this);
		th.start();
		//���s�t���O�𗧂Ă�
		runFlag = true;
	}//End of Start

	/**
	 * ���s�̒��~
	 */
	public void stop(){
		disconnect();
		runFlag = false;
		enable_bt_task=false;
	}

	//=========================���C���̏���===========================//

	//�J�n����
	private long startTime = 0;
	
	/**
	 * �^�X�N�̎��s
	 */
	public void run() {
		
		if(Main.DEBUG){
			//�f�o�b�O�pSD�̍쐬
			dbgSD3 = new SDCardMIDP(Main.IS_ACTUAL);
			dbgSD3.open(DBG_SD_PATH3,Connector.READ_WRITE);
		}

		//�J�n����
		startTime = System.currentTimeMillis();
		//�v�Z�ɂ�����������
		long pastTime = 0;

		//�ڑ������̊J�n
		System.out.println("device search");
		try{
			//���[�J����Bluetooth�f�o�C�X�̃I�u�W�F�N�g�𓾂�
			localDevice = LocalDevice.getLocalDevice();
			//�f�o�C�X�A�T�[�r�X�����ׂ̈̃G�[�W�F���g
			agent = localDevice.getDiscoveryAgent();
			//�f�o�C�X�����̊J�n
			agent.startInquiry(DiscoveryAgent.GIAC, this);
			//�f�o�C�X������Ԃ֑J��
			status = BT_DEVICE_SEARCH;
		}catch (Exception e) {
			errMessage = "error@run()#startInquiry()";
			System.out.println("error@run()#startInquiry()");
			e.printStackTrace();
			status = BT_ERROR;
		}

		while(runFlag){
			try{
				if(btCanvas.isShown()){//�L�����o�X��\�����̂�
					//�L�[�����̓o�^
					Key.registKeyEvent();
					//�L�[����
					key();
					//���N�G�X�g�̏���
					doRequest();
					//BT�L�����o�X�̕`��
					draw();
					System.out.println("draw()");
				}

				if(status == BT_CONNECTED){//�ڑ����������Ă���Ƃ�
					//�f�[�^�̎�M����
					receive();
				}

				//�����ɂɂ�����������
				pastTime = System.currentTimeMillis() - startTime;
				if(pastTime < RECEIVE_INTERVAL){
					//�x�~
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
	 * �`�揈��
	 */
	private void draw(){

		if(Option.getOp().isBluetoothViewList()){
			//�ꗗ�\������I�����郂�[�h�̂Ƃ�
			switch (status) {

			//BT�f�o�C�X��������
			case BT_DEVICE_SEARCH:
				//�ڑ��󋵂�\��
				btCanvas.drawMessage(new String[]{"�f�o�C�X������",devices.size() + "������"});
			break;

			//BT�f�o�C�X�����������Ƃ�
			case BT_DEVICE_DISCOVERED:
				//�f�o�C�X�ꗗ��\��
				btCanvas.drawViewList("�f�o�C�X��I�����Ă�������",menu, menuIndex);
			break;

			//BT�T�[�r�X��������
			case BT_SERVICE_SEARCH:
				//�ڑ��󋵂�\��
				btCanvas.drawMessage(new String[]{"�T�[�r�X������",services.size() + "������"});
			break;

			//BT�T�[�r�X�����������Ƃ�
			case BT_SERVICE_DISCOVERED:
				//�T�[�r�X�ꗗ��\��
				btCanvas.drawViewList("�T�[�r�X��I�����Ă�������",menu, menuIndex);
			break;

			//���ڑ�
			case BT_IDLE:
				btCanvas.drawMessage(new String[]{"���ڑ�"});
			break;

			//�G���[
			case BT_ERROR:
				btCanvas.drawMessage(new String[]{"�G���[",errMessage});
			break;

			//�G���[
			case BT_CONNECTED:
				btCanvas.drawMessage(new String[]{"�ڑ���",errMessage});
			break;

			}//End of switch

		}else{//End of if
			//�����ڑ����[�h�̂Ƃ�

		}

	}//End of draw

	/**
	 * ��M�������s��
	 */
	private void receive(){
		//���b�Z�[�W�̃y���h
		bt_msg=mbx_con.MsgPend(mbx_con.MBX_bt,-1);
		//�J�n����
		startTime = System.currentTimeMillis();
		
		if(Main.DEBUG)dp4.StartTime1();
		switch(bt_msg.msg_id){

			//���s���J�n�������Ƃ�m�点��
			case MailBoxControl.MSG_BT_PLAY:
				enable_bt_task=true;
				mbx_con.AckMsgPost(bt_msg);
			break;

			//�g��ꂽ�o�b�t�@���ԋp�����
			case MailBoxControl.MSG_BPCALC_DATA_ACK:
				bt_outdata_fifo.putRequest(bt_msg);
			break;

			default:
			break;

		}//End of switch

		if(enable_bt_task){
			while(true){
				//���b�Z�[�W�����o��
				bt_outdata_msg =bt_outdata_fifo.getRequest();
				//���b�Z�[�W���Ȃ��Ȃ甲����
				if(bt_outdata_msg == null) break;
				//�f�[�^�̓ǂݍ���
				getBluetoothData(bt_outdata_msg.payload_double_ptr1,bt_outdata_msg.payload_double_ptr2);
				mbx_con.WriteMsgPost(MailBoxControl.MSG_BPCALC_DATA,bt_outdata_msg,mbx_con.MBX_bt);

			}//End of while
		}//End of if(enable_sd_task)
	}

	/**
	 * BT�ڑ��Ńf�[�^���擾���A�o�b�t�@�Ɋi�[����
	 *
	 * @param ecgData ECG�o�b�t�@
	 * @param plsDat  PLS�o�b�t�@
	 *
	 * @return �f�[�^���o�b�t�@�Ɋi�[�����ǂ����B true: �P�ł��i�[���� false:1���i�[�ł��Ȃ�����
	 */
	private void getBluetoothData(double[] ecgData,double[] plsData){

		try {
			//�ǂݍ��񂾃f�[�^�̈ꎞ�ۑ���
			//500hz�œǂ݂���Ŕ����̃f�[�^��50��(ECG,PLS)�܂œǂݍ���
			byte buf[] = new byte[SIZE_OF_RECEIVE_AT_A_TIME*6*2];
			int inputsize=SIZE_OF_RECEIVE_AT_A_TIME*6*2;

			do{

				//�o�b�t�@�Ƀf�[�^���i�[
				int tmpCounter = btIn.read(buf, 0, inputsize);
				if(tmpCounter == -1) continue;

				//��M�����f�[�^���G���L���[
				for(int i = 0; i < tmpCounter; i++){
					try{
						queue.enque(buf[i]);
					}catch(ByteQueue.OverflowIntQueueException e){
						System.out.println("Excep at ByteQueue:DataOverFlow");
					}
				}//end of enqueue

				inputsize = inputsize - tmpCounter;

			}while(inputsize != 0);


			//�P�̐S�d���g�f�[�^�̂����܂�
			byte[] tmpData=new byte[6];
			int index=0;

			//�L���[�̃f�[�^���v�Z�����ɓn��ECG,PLS�o�b�t�@�Ɋi�[
			while(true){

				//�L���[����ǂ݂�����ő吔
				int readableSize=queue.size();				
				if(Main.DEBUG) System.out.println("readableSize="+readableSize);

				//break����
				if(readableSize==0) break;

				//�L���[����̂����܂�̎��o��
				for(int i = 0; i < 6 ;i++){
					try{
						tmpData[i] = queue.deque();
					}catch(ByteQueue.EmptyIntQueueException e){
						System.out.println("Que is empty!");
					}
				}//end of for

				// ��M�����f�[�^����S�d�}�Ɩ��g�𕪗����Ċi�[����
				// 250Hz�ɂ��邽��1��΂��Ńf�[�^���i�[����
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
	 * ���N�G�X�g�̏���������
	 */
	private void doRequest(){
		//�L���[���烊�N�G�X�g�����o��
		Request req = reqQueue.getRequest();

		if(req != null){
			//���N�G�X�g������Ƃ�
			switch (req.getCommand()) {
				case INCRESE_SELECTION:
					//�I�����ڂ�����
					if(menuIndex > 0)menuIndex--;
					break;
				case DECREASE_SELECTION:
					//�I�����ڂ������
					if(menuIndex < menu.length -1)menuIndex++;
					break;
				case CONNECT_SELECTED_ITEM:
					if(status == BT_DEVICE_DISCOVERED){
						//�I������Ă���f�o�C�X�֐ڑ�
						try {
							transID = agent.searchServices(ATTR_SET, UUID_SET,(RemoteDevice)devices.get(menu[menuIndex]),this);
							status = BT_SERVICE_SEARCH;
							menuIndex = 0;
						} catch (Exception e) {
							status = BT_ERROR;
							errMessage ="�T�[�r�X�������s";
						}
					}else if(status == BT_SERVICE_DISCOVERED){
						//�I������Ă���T�[�r�X�֐ڑ�
						connect();
						menuIndex = 0;
					}
					break;
			}
		}
	}

	/**
	 * BT�T�[�r�X�ɐڑ�����
	 */
	private void connect(){
		try{
			//URL
			String url = ((ServiceRecord)services.get(menu[menuIndex])).getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
			//�R�l�N�V�������m������
			conn = (StreamConnection)Connector.open(url);
			//�ڑ���Ԃ�
			status = BT_CONNECTED;
			//�C���v�b�g�X�g���[���𓾂�
			btIn = conn.openInputStream();
			btOut = conn.openOutputStream();

			//�f�[�^����M�ׂ̈̍��}�𑗂�
			if(Option.getOp().isMCBY()){
				btOut.write(util.Protocol.CMD_START);
				btOut.flush();
			}else{
				btOut.write('X');
				btOut.flush();
			}
			//�\����ύX
			canvas.setDisplay();
		} catch(IllegalArgumentException e){
			status = BT_ERROR;
			errMessage ="�ڑ��Ɏ��s";
		} catch(ConnectionNotFoundException e){
			status = BT_ERROR;
			errMessage ="�ڑ��Ɏ��s";
		} catch(IOException e){
			status = BT_ERROR;
			errMessage ="�ڑ��Ɏ��s";
		}
	}

	/**
	 * BT�ڑ�/�ڑ�������ؒf����
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

	//===========================�L�[�A�R�}���h����=============================//
	/**
	 * �L�[����
	 */
	private void key(){

		if(status == BT_DEVICE_DISCOVERED || status == BT_SERVICE_DISCOVERED){
			//�f�o�C�X/�T�[�r�X�ꗗ�\����Ԃ̂Ƃ�
			if(Key.isKeyPressed(Canvas.UP)){

				//��L�[���������Ƃ� �I�����Ă��鍀�ڂ�����
				reqQueue.putRequest(new Request(INCRESE_SELECTION));

			}else if(Key.isKeyPressed(Canvas.DOWN)){

				//���L�[���������Ƃ� �I�����Ă��鍀�ڂ������
				reqQueue.putRequest(new Request(DECREASE_SELECTION));

			}else if(Key.isKeyPressed(Canvas.FIRE)){

				//����L�[���������Ƃ� ���ݑI�𒆂̃f�o�C�X�E�T�[�r�X�֐ڑ�����
				reqQueue.putRequest(new Request(CONNECT_SELECTED_ITEM));

			}//End of if
		}//End of if(status == BT_DEVICE_DISCOVERED || status == BT_SERVICE_DISCOVERED)
	}//End of key()

	/**
	 * �R�}���h����������
	 *
	 * @param com  �R�}���h
	 * @param disp �R�}���h�����������f�B�X�v���C
	 */
	public void commandAction(Command com, Displayable disp) {
		if(!disp.equals(btCanvas))return; //BT�L�����o�X�̂Ƃ��̂ݏ���

		if(com.equals(exitCom)){
			//���f
			disconnect();
			//��ʂ�߂�
			canvas.setDisplay();
		}
	}

	//=======================DiscoveryListenner�֘A=========================//
	/**
	 * BT�f�o�C�X�𔭌����Ƃ��ɌĂ΂��
	 */
	public void deviceDiscovered(RemoteDevice rd, DeviceClass arg1) {
		try{
			devices.put(rd.getFriendlyName(false), rd);
		} catch(IOException e){
			status = BT_ERROR;
			errMessage = "�f�o�C�X�������s";
			System.out.println("Get Device Name Error: " + e.toString());
			e.printStackTrace();
		}
	}
	/**
	 * �f�o�C�X���������������Ƃ��ɌĂ΂��
	 */
	public void inquiryCompleted(int type) {

		if(type == DiscoveryListener.INQUIRY_COMPLETED){

			if(Option.getOp().isBluetoothViewList()){
				//�f�o�C�X���ꗗ�\�����đI��������
				//�f�o�C�X�̗�
				Enumeration enm = devices.keys();

				if(!enm.hasMoreElements()){
					//1���f�o�C�X���Ȃ������Ƃ�
					status = BT_ERROR;
					errMessage = "�f�o�C�X��������܂���";
					return;
				}
				menu = new String[devices.size()];
				int i = 0;
				while(enm.hasMoreElements()){
					//�f�o�C�X����\���p�̃��X�g�֊i�[
					menu[i++]= (String)enm.nextElement();
				}

				//�f�o�C�X���ꗗ�\�����郂�[�h�֕ύX
				status = BT_DEVICE_DISCOVERED;
			}else{
				//�Y������f�o�C�X�������I��

				//�ڑ���f�o�C�X
				String name = (Option.getOp().isMCBY())? DEVICE_NAME_MCBY1:DEVICE_NAME_MCBY2;

				//�f�o�C�X���������������Ƃ�
				//�f�o�C�X�̗�
				Enumeration enm = devices.keys();

				//���������f�o�C�X�ƈ�v������̂����邩����
				while(enm.hasMoreElements()){
					if(name.equals((String)enm.nextElement())){
						//���������Ƃ�
						status = BT_DEVICE_DISCOVERED;
						try{
							//�T�[�r�X�����̊J�n
							agent.searchServices(ATTR_SET,UUID_SET,(RemoteDevice)devices.get(name),this);
						}catch (Exception e) {
							e.printStackTrace();
						}
						return;
					}
				}//End of While

				//�f�o�C�X�𔭌��ł��Ȃ�����
				status = BT_ERROR;

			}//End of If(Option.getOp().isBluetoothViewList())
		}else{//End of if(type == DiscoveryListener.INQUIRY_COMPLETED)
			//����ȊO�̓G���[
			status = BT_ERROR;
		}//End of else(type == DiscoveryListener.INQUIRY_COMPLETED)
	}//End of inquiryCompleted

	/**
	 * �T�[�r�X�𔭌������Ƃ��ɌĂ΂��
	 */
	public void servicesDiscovered(int arg0, ServiceRecord[] record) {
		for(int i=0; i < record.length; i++){
			DataElement serviceNameElement = record[i].getAttributeValue(ATTR_SET[0]);
			String serviceName = (String)serviceNameElement.getValue();
			services.put(serviceName, record[i]);
		}
	}

	/**
	 * �T�[�r�X�������I�������Ƃ��ɌĂ΂��
	 */
	public void serviceSearchCompleted(int arg0, int arg1) {

		if(Option.getOp().isBluetoothViewList()){
			//�ꗗ�\�����[�h�̂Ƃ�

			//�T�[�r�X���ꗗ�\�����đI��������
			//�T�[�r�X�̗�
			Enumeration enm = services.keys();

			if(!enm.hasMoreElements()){
				//1���f�o�C�X���Ȃ������Ƃ�
				status = BT_ERROR;
				errMessage = "�T�[�r�X��������܂���";
				return;
			}
			menu = new String[services.size()];
			int i = 0;
			while(enm.hasMoreElements()){
				//�f�o�C�X����\���p�̃��X�g�֊i�[
				menu[i++]= (String)enm.nextElement();
			}
			//�T�[�r�X���ꗗ�\�����郂�[�h�֕ύX
			status = BT_SERVICE_DISCOVERED;
		}else{
			//�����ڑ����[�h�̂Ƃ�
		}//End of if
	}
}
