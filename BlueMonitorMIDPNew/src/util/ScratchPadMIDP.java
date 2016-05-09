package util;

import java.io.InputStream;

import javax.microedition.rms.RecordStore;

public class ScratchPadMIDP extends ScratchPad {
	
	private static final String NAME = "data"; 

	/**
	 *　コンストラクタ 
	 */
	public ScratchPadMIDP() {
	
	}
	
	/**
	 * レコードストアからの読み込み
	 */
	public void read() {
		try {
		    //データ用
	    	byte[][] data = new byte[4][];
			//レコードストア
			RecordStore rs = RecordStore.openRecordStore(NAME,false);
			if(rs == null)return;
			
			//レコードストアからデータの読み込み
			for(int i = 0; i < data.length ;i++){
				data[i] = rs.getRecord(i+1);
			}
			
			//収縮期血圧
			baseSbp = Double.parseDouble(new String(data[0]));
			red_wavelength = Double.parseDouble(new String(data[1]));
			infared_wavelength = Double.parseDouble(new String(data[2]));
			pls_div_cur = Double.parseDouble(new String(data[3]));
			
			//レコードストアを閉じる
			rs.closeRecordStore();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * レコードストアへの書き込み
	 */
	public void write() {
		//HTTPからのデータの取得
		//ここにＤＢからのデータ取得のコードを書く
			
		
		RecordStore rs = null;
	    try{
		    //データ用
	    	byte[][] data = new byte[4][];
		    //レコードストア
		    rs = RecordStore.openRecordStore(NAME,true);
		    
	    	//書き込み用データ(収縮期血圧)
	    	data[0] = Double.toString(baseSbp).getBytes();
	    	//書き込み用データ(赤外線波長)
	    	data[1] = Double.toString(red_wavelength).getBytes();
	    	//書き込み用データ(光電波長)
	    	data[2] = Double.toString(infared_wavelength).getBytes();
	    	//書き込み用データ(駆動電流)
	    	data[3] = Double.toString(pls_div_cur).getBytes();
	    	
	    	for(int i = 0; i < data.length; i++){
		    	//データの書き込み 0番目
		    	if(rs.getNumRecords() == 0){					//データがないとき
		    		rs.addRecord(data[i],0,data[i].length);
		    	}else{  										//データがあったら更新      
		    		rs.setRecord(i+1,data[i],0,data[i].length);
		    	}	    		
	    	}
          	//レコードストアを閉じる	    	      
	    	rs.closeRecordStore();
	      
	    } catch (Exception e) {
	      e.printStackTrace();
	      try{
	        if(rs!=null)rs.closeRecordStore();
	      }catch (Exception e2) {
	         e.printStackTrace();
	      }
	    }
	}
		
}
