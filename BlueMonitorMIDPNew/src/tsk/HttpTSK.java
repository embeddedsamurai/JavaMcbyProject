/*
 * �쐬��: 2008/12/02
 *
 * $Id: BPCalcTSK.java,v 1.1 2008/12/2 06:51:08 shusaku sone Exp $
 */

package tsk;

import java.util.Date;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import mailbox.MailBoxControl;
import mailbox.Message;
import mailbox.MessageQueue;

import main.Main;
import sd.SDCardMIDP;
import util.DebugPrint;
import gui.BPMCanvas;


/**
 * HttpTSK��\���N���X�B
 * <br>
 *
 * @version $Revision: 1.1 $
 * @author shusaku sone <embedded.samurai@gmail.com>
 */
public class HttpTSK extends Thread{


	/**
	 * �f�o�b�N�t���O���`���܂��B
	 * <br>
	 * TODO: �����[�X���ɂ�<code>false</code>�ɂ��Ă��������B
	 */
	//sone1013
	public static final boolean DEBUG = true;
	private DebugPrint dp3=null;

	//if Miss,LinLin use this program,this flag do true.
	private static final boolean linlin_flag=false;
	
	// komiya20110331
	private static final String HOST = "ec2-50-17-107-39.compute-1.amazonaws.com";
	//private static final String HOST = "202.246.9.197";
	
	private static final String DUMMY_URL="http://"+HOST+"/amChartTest/dammy.php";
	//ECG,PLS���M��
	private static final String TEST_HTTP_ECG_PLS_URL = "http://"+HOST+"/amChartTest/writeEcgPlsToDB.php";
	//HR,BP���M��
	private static final String TEST_HTTP_HR_BP_URL = "http://"+HOST+"/amChartTest/writeHrBpToDB.php";
	//Servlet ver
	private static final String HTTP_URL = "http://"+HOST+":8080/serv/com/ServletApp";
	private HttpConnection httpConn = null;

	/** �T���v�����O���[�g */
	public static final int SAMPLE = 250;

	/** ���̂����Ȃ� */
	Message new_msg        = null;
	Message dec_worked_msg = null;
	Message dec_reload_msg = null;

	/** ECG,PLS�p�̃��b�Z�[�W�L���[ */
	MessageQueue http_ecg_pls_buf_fifo = null;
	/** HR,BP�p�̃��b�Z�[�W�L���[ */
	MessageQueue http_hr_bp_buf_fifo = null;
	
	/** ���[���{�b�N�X����ւ̎Q�� */
	MailBoxControl mbx_con=null;

	/** ���s�p�X���b�h */
	private Thread thread=null;
	/** ���s�t���O */
	private boolean runFlag = false;

	/** ecg pls flag */
	static boolean http_ecg_pls_send_flag=false;

	/** hr bp flag */
	static boolean http_hr_bp_send_flag=false;

	/** http send count */
	private static int http_ecg_pls_send_count=0;
	
	/** http send count */
	private static int http_hr_bp_send_count=0;
	
	//sone1213
	/** canvas */
	BPMCanvas canvas=null;
	
	private SDCardMIDP dbgSD2;
	private static final String DBG_SD_PATH2 ="dbg2.txt";

	/**
		* �R���X�g���N�^
		*/
	public HttpTSK(MailBoxControl mbx_con,BPMCanvas canvas){

		if(Main.DEBUG) dp3=new DebugPrint();

		//sone1213
		this.canvas=canvas;

		//���[���{�b�N�X�R���g���[��
		this.mbx_con = mbx_con;
		//���b�Z�[�W�L���[
		http_ecg_pls_buf_fifo = new MessageQueue();			
		http_hr_bp_buf_fifo   = new MessageQueue();

		//����������
		init();

	}//BPCalcTask

	/**
	 * ����������
	 */
	public void init(){
		http_ecg_pls_buf_fifo.clear();
		http_hr_bp_buf_fifo.clear();
		
		http_ecg_pls_send_flag=false;
		http_hr_bp_send_flag=false;
		
		http_ecg_pls_send_count=0;
		http_hr_bp_send_count=0;
	}//init


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
	public static final int SLEEP_TIME = 1000;

	private static double http_time=0;

	public void run(){
		try{
			
			if(Main.DEBUG){
				//�f�o�b�O�pSD�̍쐬
				dbgSD2 = new SDCardMIDP(Main.IS_ACTUAL);
				dbgSD2.open(DBG_SD_PATH2,Connector.READ_WRITE);
			}
			
			//�X���[�v����
			long startTime = System.currentTimeMillis();
			//�v�Z�ɂ�����������
			long pastTime = 0;

			while(runFlag){

				//���b�Z�[�W���y���h
				new_msg=mbx_con.MsgPend(mbx_con.MBX_http,-1);

				startTime = System.currentTimeMillis();
				
				if(Main.DEBUG) dp3.StartTime1();
				
				//ECG_PLS�f�[�^�Ȃ�true,HR_BP�f�[�^�Ȃ�false
				boolean ecg_flag = true;

				switch(new_msg.msg_id){

					case MailBoxControl.MSG_HTTP_ECG_PLS_DATA:
						http_ecg_pls_buf_fifo.putRequest(new_msg);
						ecg_flag = true;
					break;
					
					case MailBoxControl.MSG_HTTP_HR_BP_DATA:
						http_hr_bp_buf_fifo.putRequest(new_msg);
						ecg_flag = false;
					break;

					default:
					break;
				}/* End of Switch */

				//���b�Z�[�W�L���[���烁�b�Z�[�W�����o��
				if(ecg_flag){
					new_msg=http_ecg_pls_buf_fifo.getRequest();	
				}else{
					new_msg=http_hr_bp_buf_fifo.getRequest();
				}

				if (new_msg!=null) {
					if(dec_worked_msg != null){
						mbx_con.AckMsgPost(dec_worked_msg);
					}
					dec_worked_msg = dec_reload_msg;
					dec_reload_msg = new_msg;

					if(ecg_flag){
						//ECG,PLS
						//HTTP�ő��M����Ƃ��ɃC���N�������g����
						http_ecg_pls_send_count++;

						//HTTP ���M����
						http_ecg_pls_send(dec_reload_msg.payload_int_ptr1,
							dec_reload_msg.payload_int_ptr2,
							dec_reload_msg.payload_size1);
					}else{
						//HR,BP
						//HTTP�ő��M����Ƃ��ɃC���N�������g����
						http_hr_bp_send_count++;
						
						//HTTP ���M����
						http_hr_bp_send(dec_reload_msg.payload_int_ptr1,
							dec_reload_msg.payload_int_ptr2,
							dec_reload_msg.payload_size1);
					}//end of if
				}//end of if

				//�v�Z�ɂ�����������
				pastTime = System.currentTimeMillis() - startTime;

				if(pastTime < SLEEP_TIME){
					//�x�~
					pause(SLEEP_TIME+5 - pastTime);
				}

				if(Main.DEBUG){
					dp3.EndTime1();
					http_time=dp3.GetPeriodTime1();
					if(Main.DEBUG){
						dbgSD2.write(("httptsk," + http_time +"\r\n").getBytes());
						dbgSD2.flush();
					}
					System.out.println("HTTPTSK spendtime="+http_time);
					this.canvas.drawDBG3("httptsk="+Long.toString((long)http_time));
				}

			}//end of while(runFlag)

		}catch(Exception e){
			System.out.println("http error!");
			e.printStackTrace();
		}

	}//end of run()

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

	/***************************************************************/
	/*************** �������� HTTP Send ******************************/
	/**************************************************************/
	/**************************************************************/
	/* Header Specification                                       */
	/*                                                            */
	/* |0xFFFE|USER_ID|MCBY_ID|Send Time|ECG:PLS@0 or HR:BP@1|    */
	/* |2byte | 4byte | 4byte | 8byte   | 1byte              |    */
	/*                                                            */
	/* |total num  | data num | ECG Data Num | PLS Data NUM  |    */
	/* | usually 0 |          | usually 1000 | usually 1000  |    */
	/* | 1byte     | 1byte    | 4byte(int)   | 4byte(int)    |    */
	/*                                                            */
	/* | ECG | ECG | ECG | ... | PLS | PLS | PLS | ... |          */
	/*  ECG Data 2byte x 1000    PLS Data 2byte x 1000            */
	/*                                                            */
	/* | 0xFFFD |                                                 */
	/* Check Data End 2byte                                       */
	/**************************************************************/

	/**  2+ 4+ 4+ 8+ 3+ 4+ 4+ 2byte=31byte */
	private static final int EP_HEDDER_SIZE=31;
	/** Size of ECG or PLS Data sended using HTTP */
	private static final int EP_SEND_NUM=1000;
	/** �S�̂̃T�C�Y 31 + (1000x2)x(2byte) */
	private static final int EP_SEND_SIZE=EP_HEDDER_SIZE+(EP_SEND_NUM*2)*2;

	/** dummy user_id */
	private int user_id = 1234;
	/** dummy mcby_id */
	private int mcby_id = 2345;

	public void http_ecg_pls_send(int[] tmpECG,int[] tmpPLS,int size){

		System.out.println("http_ecg_pls_send start");
		this.canvas.drawDBG4("���M��");
		try{
			//�_�~�[�ڑ� ���̐ڑ������Ȃ��ƂȂ���Ȃ��炵��
			byte[] dammydata = new byte[1];
			dammydata[0] = 0;
			startHTTPConnection(DUMMY_URL,dammydata);
			closeHTTPConnection();
			
			/***************** ��������X�^�[�g *********************************************/
			//�o�C�i����𐶐�����
			byte[] data = new byte[EP_SEND_SIZE];

			//syncword
			data[0] = (byte)0xFF;
			data[1] = (byte)0xFE;

			//���[�UID
			for(int i=0;i<4;i++) data[2+i] = (byte)((user_id & (0xFF000000 >> 8*i)) >> 8*(3-i));
			//MCBY_ID
			for(int i=0;i<4;i++) data[6+i] = (byte)((mcby_id & (0xFF000000 >> 8*i)) >> 8*(3-i));

			//���M����
			long time = new Date().getTime();
			for(int i=0; i < 8; i++){
				data[10+i] = (byte)((time & (0xFF00000000000000L >> 8*i)) >> 8*(7-i));
			}

			//ECG/DPPG or HR/BP
			data[18] = 0; // ECG/DPP

			//�f�[�^��������
			data[19] = 0;

			// �f�[�^������
			data[20] = (byte) http_ecg_pls_send_count;

			// ECG�f�[�^��
			for(int i=0; i < 4; i++){
				data[21+i] = (byte)((EP_SEND_NUM & (0xFF000000 >> 8*i)) >> 8*(3-i));
			}

			// DPPG�f�[�^��
			for(int i=0; i < 4; i++){
				data[25+i] = (byte)((EP_SEND_NUM & (0xFF000000 >> 8*i)) >> 8*(3-i));
			}

			// ECG�f�[�^
			System.out.println("ECGdata");
			for(int i=0; i < EP_SEND_NUM; i++){
				data[29+i*2] = (byte)(new Integer(tmpECG[i] >> 8).byteValue() & 0x03);
				data[30+i*2] = (byte)(new Integer(tmpECG[i]).byteValue());
				System.out.print(tmpECG[i]+",");
			}

			// PLS�f�[�^
			for(int i=0; i < EP_SEND_NUM; i++){
				data[29+EP_SEND_NUM*2+i*2] = (byte)(new Integer(tmpPLS[i] >> 8).byteValue() & 0x03);
				data[30+EP_SEND_NUM*2+i*2] = (byte)(new Integer(tmpPLS[i]).byteValue());
			}

			// �I�[�`�F�b�N�f�[�^
			data[29+EP_SEND_NUM*4]   = (byte)0xFF;
			data[29+EP_SEND_NUM*4+1] = (byte)0xFD;
		
			// �f�[�^�̓]��
			if(linlin_flag){
				startHTTPConnection(HTTP_URL, data);
			}else{
				startHTTPConnection(TEST_HTTP_ECG_PLS_URL, data);
			}
			closeHTTPConnection();


		} catch(Exception e){
			System.out.println("Exception error");
		}

		System.out.println("http_ecg_pls_send end");
		this.canvas.drawDBG4("���M�I��");
	}
	
		
	//HR,BP�̑��M
	/**************************************************************/
	/* Header Specification                                       */
	/*                                                            */
	/* |0xFFFE|USER_ID|MCBY_ID|Send Time|ECG:PLS@0 or HR:BP@1|    */
	/* |2byte | 4byte | 4byte | 8byte   | 1byte              |    */
	/*                                                            */
	/* |total num  | data num | HR Data Num | BP  Data NUM   |    */
	/* | usually 0 |          | usually 100 | usually 100    |    */
	/* | 1byte     | 1byte    | 4byte(int)  | 4byte(int)     |    */
	/*                                                            */
	/* | HR | HR | HR | ... | BP | BP | BP | ... |                */
	/*  HR Data 2byte x 100   BP Data 2byte x 100                 */
	/*                                                            */
	/* | 0xFFFD |                                                 */
	/* Check Data End 2byte                                       */
	/**************************************************************/
	
	/**  2+ 4+ 4+ 8+ 3+ 4+ 4+ 2byte=31byte */
	private static final int HB_HEDDER_SIZE=31;
	/** Size of HR or PAT Data sended using HTTP */
	private static final int HB_SEND_NUM=100;
	/** �S�̂̃T�C�Y 31 + (100x2)x(2byte) */
	private static final int HB_SEND_SIZE=HB_HEDDER_SIZE+(HB_SEND_NUM*2)*2;
	//
	public void http_hr_bp_send(int[] tmpHR,int[] tmpBP,int size){
		System.out.println("http_hr_bp_send start");
		this.canvas.drawDBG4("���M��");
		try{
			//�_�~�[�ڑ� ���̐ڑ������Ȃ��ƂȂ���Ȃ��炵��
			byte[] dammydata = new byte[1];
			dammydata[0] = 0;
			startHTTPConnection(DUMMY_URL,dammydata);
			closeHTTPConnection();

			/***************** ��������X�^�[�g *********************************************/
			//�o�C�i����𐶐�����
			byte[] data = new byte[HB_SEND_SIZE];

			//syncword
			data[0] = (byte)0xFF;
			data[1] = (byte)0xFE;

			//���[�UID
			for(int i=0;i<4;i++) data[2+i] = (byte)((user_id & (0xFF000000 >> 8*i)) >> 8*(3-i));
			//MCBY_ID
			for(int i=0;i<4;i++) data[6+i] = (byte)((mcby_id & (0xFF000000 >> 8*i)) >> 8*(3-i));

			//���M����
			long time = new Date().getTime();
			for(int i=0; i < 8; i++){
				data[10+i] = (byte)((time & (0xFF00000000000000L >> 8*i)) >> 8*(7-i));
			}

			//ECG/DPPG or HR/BP
			data[18] = 1; //ECG/DPP

			//�f�[�^��������
			data[19] = 0;

			// �f�[�^������
			data[20] = (byte) http_hr_bp_send_count;

			// HR�f�[�^��
			for(int i=0; i < 4; i++){
				data[21+i] = (byte)((HB_SEND_NUM & (0xFF000000 >> 8*i)) >> 8*(3-i));
			}

			// BP�f�[�^��
			for(int i=0; i < 4; i++){
				data[25+i] = (byte)((HB_SEND_NUM & (0xFF000000 >> 8*i)) >> 8*(3-i));
			}

			// HR�f�[�^
			for(int i=0; i < HB_SEND_NUM; i++){
				data[29+i*2] = (byte)(new Integer(tmpHR[i] >> 8).byteValue() & 0x03);
				data[30+i*2] = (byte)(new Integer(tmpHR[i]).byteValue());
			}

			// BP�f�[�^
			for(int i=0; i < HB_SEND_NUM; i++){
				data[29+HB_SEND_NUM*2+i*2] = (byte)(new Integer(tmpBP[i] >> 8).byteValue() & 0x03);
				data[30+HB_SEND_NUM*2+i*2] = (byte)(new Integer(tmpBP[i]).byteValue());
			}

			// �I�[�`�F�b�N�f�[�^
			data[29+HB_SEND_NUM*4]   = (byte)0xFF;
			data[29+HB_SEND_NUM*4+1] = (byte)0xFD;

			// �f�[�^�̓]��
			if(linlin_flag){
				startHTTPConnection(HTTP_URL, data);
			}else{
				startHTTPConnection(TEST_HTTP_HR_BP_URL, data);
			}

			closeHTTPConnection();

		} catch(Exception e){
			System.out.println("Exception error");
			this.canvas.drawDBG3("error@http_send(bp)");
		}

		System.out.println("http_hr_bp_send end");
		this.canvas.drawDBG4("���M�I��");
	}

	private void startHTTPConnection(String http_url, byte[] data){
		DataOutputStream dos = null;
		DataInputStream dis  = null;

		try{
			// �ڑ�
			httpConn = (HttpConnection)Connector.open(http_url,Connector.READ_WRITE);
			httpConn.setRequestMethod(HttpConnection.POST);
			httpConn.setRequestProperty("IF-Modified-Since", "10 Dec 2008 08:47:14 GMT");
			httpConn.setRequestProperty("User-Agent", "Profile/MIDP-2.0 Configuration/CLDC-1.1");
			httpConn.setRequestProperty("Content-Language", "en-CA");
			httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			httpConn.setRequestProperty("Connection", "Keep-Alive");
			httpConn.setRequestProperty("name", "BPMonitorNew");

			dos = httpConn.openDataOutputStream();
			dos.write(data);
			dos.flush();
			dos.close();
			
			dis = httpConn.openDataInputStream();
			while(true){
				int tmp = dis.read();
				if(tmp != -1 ){
					System.out.print((char)tmp);
					break;
				}
			}


		} catch(Throwable e){
			System.out.println("Open HTTP Connection Error.");
			this.canvas.drawDBG3("error@http_send(bp)");
			e.printStackTrace();
			closeHTTPConnection();
		}
	}

	private synchronized void closeHTTPConnection(){
		try{
			if(httpConn != null) httpConn.close();
		} catch(Throwable e){
			System.out.println("Close HTTP Connection Error.");
			e.printStackTrace();
		}
	}


}//end of HttpTSK
