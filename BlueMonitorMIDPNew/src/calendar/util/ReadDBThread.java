package calendar.util;

import java.util.Enumeration;
import java.util.Vector;


/**
 * DBに接続し、必要なデータを取ってくるクラス	 
 */
public class ReadDBThread extends Thread{

	/** モード */
	private final int type;
	/** MCBY ID */
	private final int mcby_id;
	/** 読み込み完了、または失敗したときの通知先のリスト */
	private Vector listenerList;
	/** 読み込んだデータ用のバッファ */
	private int[] receiveData;
	/** DBからのデータ読み込みクラス */
	private ReadDBData readDB;
	/** DBからのデータ読み込みクラス */
	private ReadNumOfDBData readNum;
	/** データ読み込み: true データ数読み込み: false*/
	private boolean flag = true;
	/** 読み込むデータの時間 */
	private final long time;
	private final int  offset;
	
    /**
     * コンストラクタ
     * @param type     データタイプ
     * @param mcby_id  MCBY_ID
     * @param len      読み込むデータ長
     */
    public ReadDBThread(final int type,final int mcby_id,final int offset,
    		            final int len,final long time){
    	
    	this.type    = type;
    	this.mcby_id = mcby_id;
    	this.time    = time;
    	this.offset  = offset;
    	
    	if(len <= Integer.MAX_VALUE){    	
    		this.receiveData = new int[len];	
    	}else{
    		new Exception("Error");
    	}    	
    	
    	if(this.type == ReadNumOfDBData.TYPE_BP_NUM  ||
   		   this.type == ReadNumOfDBData.TYPE_HR_NUM ||
   		   this.type == ReadNumOfDBData.TYPE_ECG_NUM  ||
   		   this.type == ReadNumOfDBData.TYPE_PLS_NUM ||
   		   
   		   this.type == ReadNumOfDBData.TYPE_WEEK_BP_NUM ||
		   this.type == ReadNumOfDBData.TYPE_WEEK_HR_NUM ||
		   this.type == ReadNumOfDBData.TYPE_WEEK_ECG_NUM ||
		   this.type == ReadNumOfDBData.TYPE_WEEK_PLS_NUM ||
		   
   		   this.type == ReadNumOfDBData.TYPE_MONTH_BP_NUM ||
		   this.type == ReadNumOfDBData.TYPE_MONTH_HR_NUM ||
		   this.type == ReadNumOfDBData.TYPE_MONTH_ECG_NUM ||
		   this.type == ReadNumOfDBData.TYPE_MONTH_PLS_NUM ||
		   
   		   this.type == ReadNumOfDBData.TYPE_YEAR_BP_NUM  ||
    	   this.type == ReadNumOfDBData.TYPE_YEAR_HR_NUM  ||
    	   this.type == ReadNumOfDBData.TYPE_YEAR_ECG_NUM ||
		   this.type == ReadNumOfDBData.TYPE_YEAR_PLS_NUM ){
    		flag = false;    		    		
        	readNum = new ReadNumOfDBData();    			
    	}else{
    		flag = true;
    		readDB = new ReadDBData();    		
    	}    	

    	listenerList = new Vector();
    }
    
    //-------------------リスナの追加、削除--------------------//
    
    /**
     * DBからの読み込み完了、失敗の通知先を追加
     */
    public void addListener(ReadDBListener listener){    	
    	if(!listenerList.contains(listener)){
    		//まだリスナに含まれていないとき,リストにリスナを追加する
    		listenerList.addElement(listener);
    	}
    }
    
    /**
     * DBからの読み込み完了、失敗の通知先を削除
     */
    public void deleteListener(ReadDBListener listener){
    	if(listenerList.contains(listener)){
    		//リストに含まれているとき、リストから削除
    		listenerList.removeElement(listener);
    	}
    }
    
    /**
     * リスナに通知 
     * @param flag true:成功を通知 false:失敗を通知
     * @param num  読み込んだデータ 
     */
    private void notifyListener(boolean flag,int num){
    	//リスナの列挙
    	Enumeration enumration = listenerList.elements();
    	
    	if(flag){
    		//成功を通知
    		int[] array ;
    		if(num <= 0){
    			array = new int[1];
    			array[0] = -1;
    		}else{
    			array = new int[num];
    			for(int i = 0; i < array.length ; i++){
    				array[i] = receiveData[i];    				
    			}//End of for()    			
    		}//End of if()
    		
    		while(enumration.hasMoreElements()){
    			ReadDBListener listener = (ReadDBListener)enumration.nextElement();    	    	
    			listener.onCompleteReadDB(array,type,time);
    		}//End of while()	    		
    	}else{
    		//失敗を通知
    		while(enumration.hasMoreElements()){
    			ReadDBListener listener = (ReadDBListener)enumration.nextElement();
    			listener.onErrorReadDB();
    		}//End of while()	    		
    	}//End of if(flag)
    }

    //-------------------DB接続、取り出し--------------------//
    
    /**
     * DBへの接続、データの取り出し
     */
    public void run() {
    	try{	    		
    		//データ取得数
	    	int num = -1;

	    	//DBからのデータの取得
	    	if(flag){	    		
	    		num = readDB.read(receiveData,offset,receiveData.length,type,mcby_id,time);
	    	}else{
	    		num = readNum.read(receiveData,type,mcby_id,time);	    		
	    	}
	    	
	    	// Debug Print
			System.out.println("\n読み込み数:" + num);
			
			//リスナに通知
			if(num != -1){
				//データが取得できたら、成功		
				System.out.println("データ取得成功");
				notifyListener(true,num);	
			}else{				
				//データが取得できなかったら、失敗
				notifyListener(false,num);
			}
	    	
    	}catch (Exception e) {
    		e.printStackTrace();
    		//失敗を通知
    		notifyListener(false,-1);
    		return;
		}// End of try catch
    }//End of run()
        
    /**
     * 読み込みの中止
     */
    public void stop(){
    	if(flag){
    		readDB.disconnect();	
    	}else{
    		readNum.disconnect();
    	}//End of if()
    }//End of stop()
    	
}//End of HTTPThread 
