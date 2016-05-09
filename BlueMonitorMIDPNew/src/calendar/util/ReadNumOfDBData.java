package calendar.util;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;


public class ReadNumOfDBData {
		
	//private static final String DAY_NUM_URL = "http://localhost/mc_test/getNumberOfData.php";
	private static final String DAY_NUM_URL = "http://202.246.9.197/xampp/amChart2/getNumberOfData.php";
	
	private static final String WEEK_NUM_URL = "http://202.246.9.197/xampp/amChart2/getNumberOfDataWeek.php";
	//private static final String WEEK_NUM_URL = "http://localhost/mc_test/getNumberOfDataWeek.php";
	
	private static final String MONTH_NUM_URL = "http://202.246.9.197/xampp/amChart2/getNumberOfDataMonth.php";
	//private static final String WEEK_NUM_URL = "http://localhost/mc_test/getNumberOfDataMonth.php";
	
	private static final String YEAR_NUM_URL = "http://202.246.9.197/xampp/amChart2/getNumberOfDataYear.php";
	//private static final String YEAR_NUM_URL = "http://localhost/mc_test/getNumberOfDataYear.php";
	
	/* 取得する情報 */
	public static final int TYPE_ECG_NUM      = 0x10;
	public static final int TYPE_PLS_NUM      = 0x11;
	
	public static final int TYPE_HR_NUM       = 0x12;
	public static final int TYPE_BP_NUM       = 0x13;
	
	public static final int TYPE_WEEK_ECG_NUM = 0x14;
	public static final int TYPE_WEEK_PLS_NUM = 0x15;
	public static final int TYPE_WEEK_HR_NUM  = 0x16;
	public static final int TYPE_WEEK_BP_NUM  = 0x17;
	
	public static final int TYPE_MONTH_ECG_NUM = 0x18;
	public static final int TYPE_MONTH_PLS_NUM = 0x19;
	public static final int TYPE_MONTH_HR_NUM  = 0x1a;
	public static final int TYPE_MONTH_BP_NUM  = 0x1b;
	
	public static final int TYPE_YEAR_ECG_NUM = 0x1c;
	public static final int TYPE_YEAR_PLS_NUM = 0x1d;
	public static final int TYPE_YEAR_HR_NUM  = 0x1e;
	public static final int TYPE_YEAR_BP_NUM  = 0x1f;

	/** HTTPConnection */
	private HttpConnection httpConn = null;	

	/** 入力ストリーム */
	private DataInputStream dis = null;
	/** 出力ストリーム */
	private DataOutputStream dos = null;

	/**
	 * 接続処理
	 * @param type 接続タイプ
	 */
	private boolean connect(int type) {
		try {
			/* 接続先URL */
			String url = "";
			
			if(type == TYPE_BP_NUM || type == TYPE_HR_NUM ||
			  type == TYPE_PLS_NUM || type == TYPE_ECG_NUM ){
				
				url = DAY_NUM_URL;
				
			}else if(type == TYPE_WEEK_PLS_NUM || type == TYPE_WEEK_ECG_NUM ||  
					 type == TYPE_WEEK_BP_NUM || type == TYPE_WEEK_HR_NUM ){
				
				url = WEEK_NUM_URL;
				
			}else if(type == TYPE_MONTH_PLS_NUM || type == TYPE_MONTH_ECG_NUM ||
					 type == TYPE_MONTH_BP_NUM || type == TYPE_MONTH_HR_NUM){
				
				url = MONTH_NUM_URL;
				
			}else if(type == TYPE_YEAR_PLS_NUM || type == TYPE_YEAR_ECG_NUM ||
					 type == TYPE_YEAR_BP_NUM || type == TYPE_YEAR_HR_NUM){
				
				url = YEAR_NUM_URL;
			}
			
			System.out.println("url:" +url);
			/* 接続 */													
			httpConn = (HttpConnection)Connector.open(url,Connector.READ_WRITE);
			httpConn.setRequestMethod(HttpConnection.POST);
			httpConn.setRequestProperty("IF-Modified-Since", "15 Oct 2003 08:47:14 GMT");
			httpConn.setRequestProperty("User-Agent", "Profile/MIDP-1.0 Configuration/CLDC-1.0");
			httpConn.setRequestProperty("Content-Language", "en-CA");
			httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			httpConn.setRequestProperty("Connection", "Keep-Alive");
			httpConn.setRequestProperty("name", "BlueMonitor");	
			
		} catch (Throwable e) {
			System.out.println("Open HTTP Connection Error.");
			e.printStackTrace();
			disconnect();
			return false;
		}/* End of try */
		
		return true;
		
	}/* End of connect() */

	/**
	 * 切断処理
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
	 * 読み込み処理
	 * @param data    取得したデータを格納する配列
	 * @param type    取得するデータタイプ
	 * @param mcby_id MCBY_ID
	 * @param time    取得するデータの日時
	 * 
	 * | -------------------HEADER------------------------ |
	 * | 0xFFFE 2bytes | MCBY_ID 4bytes | SEND_TIME 8bytes |
	 * | TYPE   1bytes | 0xFFFE 2bytes  |                  |
	 * |---------------------------------------------------|
	 * 
	 * @return 取得したデータ数
	 */
	public int read(int[] data,final int type ,final int mcby_id,final long time) {

		/* 接続処理 */
		if(!connect(type)){
			return -1;
		}

		int num = -1;
		try {
			byte[] inst = new byte[17];
			
			int index = 0;
			/* データ開始位置 0xFFFE */
			inst[index]   = (byte) 0xFF; 
			inst[index+1] = (byte) 0xFE;
			
			/* MCBY_ID */
			index = 2;
			for (int i = 0; i < 4; i++) {
				inst[index + i] = (byte) ((mcby_id & (0xFF000000 >> 8 * i)) >> 8 * (3 - i));
			}
			
			/* 送信時間 */
			index = 6;
			for (int i = 0; i < 8; i++) {
				inst[index + i] = (byte) ((time & (0xFF00000000000000L >> 8 * i)) >> 8 * (7 - i));
			}

			/* 取得するデータタイプ */
			index = 14;
			switch (type) {
			case TYPE_ECG_NUM:
			case TYPE_WEEK_ECG_NUM:
			case TYPE_MONTH_ECG_NUM:
			case TYPE_YEAR_ECG_NUM:
				inst[index] = 0;
				break;
			case TYPE_PLS_NUM:
			case TYPE_WEEK_PLS_NUM:
			case TYPE_MONTH_PLS_NUM:
			case TYPE_YEAR_PLS_NUM:
				inst[index] = 1;
				break;
			case TYPE_HR_NUM:
			case TYPE_WEEK_HR_NUM:
			case TYPE_MONTH_HR_NUM:
			case TYPE_YEAR_HR_NUM:			
				inst[index] = 2;
				break;
			case TYPE_BP_NUM:
			case TYPE_WEEK_BP_NUM:
			case TYPE_MONTH_BP_NUM:
			case TYPE_YEAR_BP_NUM:
				inst[index] = 3;
				break;
			default: 
				inst[index] = 0;
				break;
			}

			index = 15;
			/* データ終了位置 0xFFFD */
			inst[index]   = (byte) 0xFF;
			inst[index+1] = (byte) 0xFD;
						
			/* リクエストを送る */			
			dos = httpConn.openDataOutputStream();			
			dos.write(inst);
			dos.flush()    ;
			dos.close()    ;			
						
			/* レスポンスを得る */
			dis = httpConn.openDataInputStream();
			
			/* 個々のデータ　*/
			StringBuffer tmpStr = new StringBuffer();
			/* dataの添え字 */
			int iCounter = 0;
						
			/* サーバーから読み込み */
			while(true){				
				int val = dis.read();
				if(val == -1){
					break;								
				}else if((char)val == ','){
					//区切り文字						
					data[iCounter] = Integer.parseInt(tmpStr.toString());				
					iCounter++;
					tmpStr.delete(0,tmpStr.length());
				}else{
					//数字 or 符号
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
			e.printStackTrace();
		}/* End of try */

		/* 切断 */
		disconnect();
		return num;
		
	}/* End of read() */
}
