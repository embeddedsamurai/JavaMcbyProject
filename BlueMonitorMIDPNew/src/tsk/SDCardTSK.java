/*
 * �쐬��: 2007/11/04
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
 * ��Ԃ�\���N���X�B
 * <br>
 * version $Revision: 1.1
 * @copyright 2007 Embedded.Samurai, Inc.
 * @author embedded.samurai <embedded.samurai@gmail.com>
 */
public class SDCardTSK extends Thread{

	/**
	 * �f�o�b�N�t���O���`���܂��B
	 * <br>
	 * TODO: �����[�X���ɂ�<code>false</code>�ɂ��Ă��������B
	 */
	//sone1013
	public static final boolean DEBUG = true;
	private DebugPrint dp1=null;


	public static final int TRUE=1;
	public static final int FALSE=0;


	//--------------SD�J�[�h�֘A---------------//

	/** �S�d�}�M�����Ƃ肾���r�c�J�[�h�̃p�X */
	private static final String ECG_SD_PATH ="ecg.txt";
	/** ���g�M�����Ƃ肾���r�c�J�[�h�̃p�X */
	private static final String PLS_SD_PATH ="dppg.txt";
	/** ��x�Ɏ�M����f�[�^�� */
	private static final int SIZE_OF_RECEIVE_AT_A_TIME=50;

	/** �S�d�}�f�[�^��ǂݍ��ނ��߂�SDCard�I�u�W�F�N�g */
	private SDCardMIDP ecgSD;
	/** ���g�f�[�^��ǂݍ��ނ��߂�SDCard�I�u�W�F�N�g   */
	private SDCardMIDP plsSD;

	//--------------���b�Z�[�W�֘A----------------//

	private Message sd_indata_msg  = null;
	private Message sd_outdata_msg = null;
	private Message sd_msg         = null;

	/** Message�p */
	private MessageQueue sd_msg_fifo       = null;
	/** Read�p */
	private MessageQueue sd_indata_fifo    = null;
	/** Write�p */
	private MessageQueue sd_outdata_fifo   = null;

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

	/** MailBoxControl (�C���X�^���X�͍��Ȃ�) */
	private MailBoxControl mbx_con=null;

	/** Thread��j�����邽�߂Ɏg���t���O */
	boolean runFlag=true;

	/** ���s�p�X���b�h */
	private Thread thread=null;

	/** SDCardTSK���N�������true�ɂȂ� */
	private static boolean enable_sd_task=false;

	//sone1212
	/** Debug canvas */
	BPMCanvas canvas=null;

	/**
	 * �R���g���[��
	 * @param mbx_con�@���[���{�b�N�X����
	 */
	public SDCardTSK(MailBoxControl mbx_con,BPMCanvas canvas){

		if(Main.DEBUG) dp1=new DebugPrint();

		//sone1213
		this.canvas = canvas;


		//���[���{�b�N�X�R���g���[��
		this.mbx_con = mbx_con;

		// �S�d�}�f�[�^��ǂݍ��ނ��߂�SDCard�I�u�W�F�N�g
		ecgSD = new SDCardMIDP(Main.IS_ACTUAL);
		// ���g�f�[�^��ǂݍ��ނ��߂�SDCard�I�u�W�F�N�g
		plsSD = new SDCardMIDP(Main.IS_ACTUAL);

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
		sd_msg_fifo     = new MessageQueue();
		sd_indata_fifo  = new MessageQueue();
		sd_outdata_fifo = new MessageQueue();

		//������
		init();
	}//end of BPTASK

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
		sd_msg_fifo.clear();
		sd_indata_fifo.clear();
		sd_outdata_fifo.clear();

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
		sd_outdata_fifo.putRequest(msg1);
		sd_outdata_fifo.putRequest(msg2);
		sd_outdata_fifo.putRequest(msg3);
		sd_outdata_fifo.putRequest(msg4);
		sd_outdata_fifo.putRequest(msg5);
		sd_outdata_fifo.putRequest(msg6);

	}//init

	/**
	 * SD�J�[�h�̎�M�������J�n����
	 */
	public void start(){
		//����������
		init();
		//���s�t���O�����Ă�
		runFlag=true;
		//�X���b�h�̋N��
		thread = new Thread(this);
		//���s�̊J�n
		thread.start();
	}

	/**
	 * �S�d�}�M���A���g�M���̎�M�������s��(SD�J�[�h)
	 */

	public static final int SLEEP_TIME = 1000;
	private static double sd_time=0;

	public void run() {
		try{
			//------------------�f�[�^��M�ׂ̈̑O����---------------------//
			//�r�c�J�[�h���I�[�v��
			ecgSD.open(ECG_SD_PATH,Connector.READ);
			plsSD.open(PLS_SD_PATH,Connector.READ);

			//-------------------------��M����-------------------------//

			//�X���[�v����
			long startTime = System.currentTimeMillis();
			//�v�Z�ɂ�����������
			long pastTime = 0;

			//��M����
			while(runFlag){

				//���b�Z�[�W�̃y���h
				sd_msg=mbx_con.MsgPend(mbx_con.MBX_sd,-1);

				startTime = System.currentTimeMillis();
				if(Main.DEBUG) dp1.StartTime1();

				switch(sd_msg.msg_id){

				case MailBoxControl.MSG_SD_PLAY:
					enable_sd_task=true;
					mbx_con.AckMsgPost(sd_msg);
				break;

				//�g��ꂽ�o�b�t�@���ԋp�����
				case MailBoxControl.MSG_BPCALC_DATA_ACK:
					sd_outdata_fifo.putRequest(sd_msg);
				break;

				default:
					break;
				}

				if(enable_sd_task){

					while(true){
						//���b�Z�[�W�����o��
						sd_outdata_msg =sd_outdata_fifo.getRequest();
						//���b�Z�[�W���Ȃ��Ȃ甲����
						if(sd_outdata_msg == null) break;
						//�f�[�^�̓ǂݍ���
						getSDData(sd_outdata_msg.payload_double_ptr1,sd_outdata_msg.payload_double_ptr2);
						//SD�J�[�h����ǂݍ��񂾃f�[�^�����b�Z�[�W�Ƃ���BPCALC_DATA�ɑ���܂��B
						mbx_con.WriteMsgPost(MailBoxControl.MSG_BPCALC_DATA,sd_outdata_msg,mbx_con.MBX_sd);
					};

				}//end of if(enable_sd_task)

				//�v�Z�ɂ�����������
				pastTime = System.currentTimeMillis() - startTime;

				if(pastTime < SLEEP_TIME){
					//�x�~
					pause(SLEEP_TIME+5 - pastTime);
				}


				if(Main.DEBUG){
					dp1.EndTime1();
					sd_time=dp1.GetPeriodTime1();
					System.out.println("SDCardTSK spendtime="+sd_time);
					this.canvas.drawDBG("sdtsk="+Long.toString((long)sd_time));
				}
			}//end of while(runFlag)

			//SD�J�[�h�����
			ecgSD.close();
			plsSD.close();

		}catch (Exception e) {
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

	/** SD�J�[�h����f�[�^��ǂݍ���Ńo�b�t�@�Ɋi�[���� */
	void getSDData(double[] ecgData,double[] plsData){
		for(int i = 0; i < SIZE_OF_RECEIVE_AT_A_TIME; i++){
			try{
				double ecg = 0;
				double pls = 0;

				//�S�d�}�f�[�^�̓ǂݍ���
				String ecgStr =new String(ecgSD.readLine());
				ecg = Double.parseDouble(ecgStr);

				//���g�f�[�^�̓ǂݍ���
				String plsStr =new String(plsSD.readLine());
				pls = Double.parseDouble(plsStr);

				ecgData[i] = ecg;
				plsData[i] = pls;
			}catch (NumberFormatException e) {
				//������x�擪����ǂ݂Ȃ���
				ecgSD.close();
				ecgSD.open(ECG_SD_PATH,Connector.READ);
				plsSD.close();
				plsSD.open(PLS_SD_PATH,Connector.READ);
			}
		}
	}//end of getSDData

	/**
	 * ���s�̒��~
	 */
	public void stop(){
		runFlag = false;
	}

	/**
	 * ���s�����ǂ�����Ԃ�
	 * @return ���s�����ǂ���
	 */
	public boolean isRunFlag() {
		return runFlag;
	}
}