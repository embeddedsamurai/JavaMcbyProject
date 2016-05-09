package calendar.util;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

public class ReadDBData{
	
	/* �擾������ */
	public static final int TYPE_ECG      = 0x00;
	public static final int TYPE_PLS      = 0x01;
	public static final int TYPE_HR       = 0x02;
	public static final int TYPE_BP       = 0x03;
	

	public static final int TYPE_YEAR_HR  = 0x08;
	public static final int TYPE_YEAR_BP  = 0x09;
	
	public static final int TYPE_WEEK_HR_AVE  = 0x0a;
	public static final int TYPE_WEEK_BP_AVE  = 0x0b;
	public static final int TYPE_MONTH_HR_AVE = 0x0c;
	public static final int TYPE_MONTH_BP_AVE = 0x0d;
	public static final int TYPE_YEAR_HR_AVE  = 0x0e;
	public static final int TYPE_YEAR_BP_AVE  = 0x0f;
	
	/* URL */
	private static final String HOUR_URL  = "http://202.246.9.197/xampp/amChart2/getMcbyDataHour_New.php";
	private static final String DAY_URL   = "http://202.246.9.197/xampp/amChart2/getMcbyData_New.php";	
	private static final String YEAR_URL  = "http://202.246.9.197/xampp/amChart2/getMcbyDataYear_New.php";

	private static final String WEEK_AVE_URL   = "http://202.246.9.197/xampp/amChart2/getMcbyAveDataWeek_New.php";
	private static final String MONTH_AVE_URL   = "http://202.246.9.197/xampp/amChart2/getMcbyAveDataMonth_New.php";
	private static final String YEAR_AVE_URL   = "http://202.246.9.197/xampp/amChart2/getMcbyAveDataYear_New.php";
	
	
	/* URL ���[�J��*/
	//private static final String DAY_URL   = "http://localhost/mc_test/getMcbyData_New.php";
	//private static final String WEEK_URL = "http://localhost/mc_test/getMcbyDataWeek_New.php";
	//private static final String MONTH_URL = "http://localhost/mc_test/getMcbyDataMonth_New.php";
	//private static final String YEAR_URL = "http://localhost/mc_test/getMcbyDataYear_New.php";
	
	//private static final String WEEK_URL  = "http://202.246.9.197/xampp/amChart2/getMcbyDataWeek_New.php";	
	//private static final String MONTH_URL = "http://202.246.9.197/xampp/amChart2/getMcbyDataMonth_New.php";

	/** HTTPConnection */
	private HttpConnection httpConn = null;

	/** ���̓X�g���[�� */
	private DataInputStream dis = null;
	/** �o�̓X�g���[�� */
	private DataOutputStream dos = null;		

	/**
	 * �ڑ�����
	 * @param type �ڑ��^�C�v
	 */
	public boolean connect(final int type) {
		try {
			/* �ڑ���URL */
			String url = "";
			if(type == TYPE_HR || type == TYPE_BP ||
				type == TYPE_ECG || type == TYPE_PLS) {
				System.out.println("#DAY,HOUR");
				url = DAY_URL;
			}else if (type == TYPE_YEAR_HR || type == TYPE_YEAR_BP) {
				System.out.println("#YEAR");
				url = YEAR_URL;
			} else if (type == TYPE_WEEK_HR_AVE || type == TYPE_WEEK_BP_AVE) {
				System.out.println("#WEEK AVE");
				url = WEEK_AVE_URL;
			} else if (type == TYPE_MONTH_HR_AVE || type == TYPE_MONTH_BP_AVE) {
				System.out.println("#MONTH AVE");
				url = MONTH_AVE_URL;				
			} else if (type == TYPE_YEAR_HR_AVE || type == TYPE_YEAR_BP_AVE) {				
				System.out.println("#YEAR AVE");
				url = YEAR_AVE_URL;
			}
			
			System.out.println("url"+ url);
			/* �ڑ� */			 
			httpConn = (HttpConnection)Connector.open(url,Connector.READ_WRITE);
			httpConn.setRequestMethod(HttpConnection.POST);
			httpConn.setRequestProperty("IF-Modified-Since", "15 Oct 2003 08:47:14 GMT");
			httpConn.setRequestProperty("User-Agent", "Profile/MIDP-1.0 Configuration/CLDC-1.0");
			httpConn.setRequestProperty("Content-Language", "en-CA");
			httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			httpConn.setRequestProperty("Connection", "Keep-Alive");
			httpConn.setRequestProperty("name", "BlueMonitor");		
				
			return true;
		} catch (Throwable e) {
			System.out.println("Open HTTP Connection Error.");
			e.printStackTrace();
			disconnect();
			return false;
		}/* End of try */
		
	}/* End of connect() */

	/**
	 * �ؒf����
	 */
	public void disconnect() {
		try {
			if (httpConn != null){
				httpConn.close();
				httpConn = null;
			}			
			
			if (dis != null){
				dis.close();
				dis = null;
			}				
			if (dos != null){
				dos.close();
				dos = null;
			}
				
		} catch (Throwable e) {
			System.out.println("Close HTTP Connection Error.");
			e.printStackTrace();
		}/* End of try */
	}/* */

	/**
	 * �ǂݍ��ݏ���
	 * @param data    �擾�����f�[�^���i�[����z��
	 * @param offset  �f�[�^�擾�J�n�ʒu
	 * @param length  �擾����f�[�^��
	 * @param type    �擾����f�[�^�^�C�v
	 * @param mcby_id MCBY_ID
	 * @param time    �擾����f�[�^�̓���
	 * 
	 * | -------------------HEADER------------------------ |
	 * | 0xFFFE 2bytes | MCBY_ID 4bytes | SEND_TIME 8bytes |
	 * | TYPE   1bytes | OFFSET  4bytes | LENGTH    4bytes |
	 * | 0xFFFE 2bytes |                                   |
	 * |---------------------------------------------------|
	 * 
	 * @return �擾�����f�[�^�� ���s������-1
	 */
	public int read(int[] data,final int offset ,final int length,
			        final int type ,final int mcby_id,long time) {

		/* �ڑ����� */
		if(!connect(type)){
			return -1;
		}

		int num = -1;
		/* data�̓Y���� */
		int iCounter = 0;
		
		try {
			byte[] inst = new byte[25];
			
			int index = 0;
			/* �f�[�^�J�n�ʒu 0xFFFE */
			inst[index]   = (byte) 0xFF; 
			inst[index+1] = (byte) 0xFE;
			
			/* MCBY_ID */
			index = 2;
			for (int i = 0; i < 4; i++) {
				inst[index + i] = (byte) ((mcby_id & (0xFF000000 >> 8 * i)) >> 8 * (3 - i));
			}
			
			/* ���M���� */
			index = 6;
			for (int i = 0; i < 8; i++) {
				inst[index + i] = (byte) ((time & (0xFF00000000000000L >> 8 * i)) >> 8 * (7 - i));
			}
			
			/* OFFSET */
			index = 14;
			for (int i = 0; i < 4; i++) {
				inst[index + i] = (byte) ((offset & (0xFF000000 >> 8 * i)) >> 8 * (3 - i));
			}
			
			/* LENGTH */
			index = 18;
			for (int i = 0; i < 4; i++) {
				inst[index + i] = (byte) ((length & (0xFF000000 >> 8 * i)) >> 8 * (3 - i));
			}

			/* �擾����f�[�^�^�C�v */
			index = 22;
			switch (type) {
			case TYPE_ECG:
				System.out.println("#ECG");
				inst[index] = 0;
				break;
			case TYPE_PLS:
				System.out.println("#PLS");
				inst[index] = 1;
				break;
			case TYPE_HR:
				System.out.println("#HR");
				inst[index] = 2;
				break;
			case TYPE_BP:
				System.out.println("#BP");
				inst[index] = 3;
				break;
			case TYPE_YEAR_HR:
			case TYPE_WEEK_HR_AVE:
			case TYPE_MONTH_HR_AVE:
			case TYPE_YEAR_HR_AVE:
				System.out.println("#_HR");
				inst[index] = 0;
				break;
			case TYPE_YEAR_BP:
			case TYPE_WEEK_BP_AVE:
			case TYPE_MONTH_BP_AVE:
			case TYPE_YEAR_BP_AVE:			
				System.out.println("#_BP");
				inst[index] = 1;
				break;
			}
			System.out.println("type:" + inst[index]);

			index = 23;
			/* �f�[�^�I���ʒu 0xFFFD */
			inst[index]   = (byte) 0xFF;
			inst[index+1] = (byte) 0xFD;
			
			/* ���N�G�X�g�𑗂� */
			dos = httpConn.openDataOutputStream();			
			dos.write(inst);
			dos.flush()    ;
			dos.close()    ;			
			
			/* ���X�|���X�𓾂� */
			dis = httpConn.openDataInputStream();
			
			/* �X�̃f�[�^�@*/
			StringBuffer tmpStr = new StringBuffer();
			
			/* �T�[�o�[����ǂݍ��� */
			while(true){				
				int val = dis.read();
				if(val == -1){
					break;
				}else if((char)val == ','){
					//��؂蕶��							
					data[iCounter] = Integer.parseInt(tmpStr.toString());
					System.out.print(data[iCounter]+",");
					iCounter++;
					tmpStr.delete(0,tmpStr.length());
				}else{
					//���� or ����
					tmpStr.append((char)val);
				}			
				//System.out.print((char)val);
			 }
			System.out.println("\n end \n");
			 dis.close();	
			 			 
			 num = iCounter;

		} catch (IOException e) {
			e.printStackTrace();			
		} catch (Throwable e) {
			System.out.println("icount:" + iCounter);
			e.printStackTrace();
			
		}/* End of try */

		/* �ؒf */
		disconnect();
		
		return num;
		
	}/* End of read() */
}/* End of ReadDBData class */