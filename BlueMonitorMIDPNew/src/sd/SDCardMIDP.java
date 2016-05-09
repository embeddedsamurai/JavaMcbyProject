/***************************************************/
/* SDCardMIDPクラス                                */
/*                     written by embedded.samurai */
/***************************************************/
package sd;


import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.microedition.io.Connector;

//for MIDP
import javax.microedition.io.file.*;

public class SDCardMIDP extends SDCard{

	/**
		* 実機での実行か、エミュレータでの実行かのフラグ
		* true:  実機での実行
		* false:エミュレータからの実行
		*/
	private  boolean runFlag;

	/** コネクション */
	private FileConnection storage;

	/**
		* コンストラクタ
		* @param runFlag 実機での実行か、エミュレータでの実行か
		* true: 実機での実行  false:エミュレータからの実行
		*/
	public SDCardMIDP(boolean runFlag){
		this.runFlag = runFlag;
	}


	//--実行時例外 サイズが超えている ---
	public class ArraySizeOutOfBoundsError extends Exception{
		public ArraySizeOutOfBoundsError(String str){
			System.out.println("str="+str);
		}
	}


	/** ファイルをオープンする */
	public synchronized void open(final String fname,final int mode){
		// ファイルをオープンする
		try{
			String name = null;

			if(runFlag){
				name = "file:///E:/Data/"+fname;    //SDカードから実行時
			}else{
				name = "file:///root1/Data/"+fname; //WTK エミュレータから実行時
			}

			storage = (FileConnection)Connector.open(name,mode);

			if(!storage.exists()){
				storage.create();
			}

			if(mode == Connector.READ){
				is = storage.openInputStream();
			}else if(mode == Connector.WRITE){
				os = storage.openOutputStream();
			}else if(mode == Connector.READ_WRITE){
				is = storage.openInputStream();
				os = storage.openOutputStream();
			}
		} catch(Exception e){
			this.close();
			e.printStackTrace();
		}
	}

	/**
	 * ファイルストリームをクローズする
	 */
	public synchronized void close(){
		try{
			if( storage != null ){
				storage.close();
			}

			if( is != null ){
				is.close();
			}

			if( os != null ){
				os.close();
			}
		}catch(Throwable e){
			e.printStackTrace();
		}
	}

	/**
	 * rdata配列のoffsetの位置からlength分、データを読み込む
	 *
	 * @return 読み込んだデータの数
	 */
	public synchronized int read(byte[] rdata,int offset,int length) throws ArraySizeOutOfBoundsError{

		if(length > rdata.length){
		 	throw new ArraySizeOutOfBoundsError("size too large");
		}
		int size=0;
		try{
		 size=is.read(rdata, offset,length);
		}catch(IOException e){
			System.out.println("can't read");
		}
		return size;
	}

 	/**
	 * rdata配列の0の位置からrdata.length分、データを読み込む
	 *
	 * @return 読み込んだデータの数
	 */
	public synchronized int read(byte[] rdata){

		int size=0;
		try{
		 size=is.read(rdata,0,rdata.length);
		}catch(IOException e){
			System.out.println("can't read");
		}

		return size;
	}


	/**
	 * ファイルから1バイト読み出す
	 *
	 * @return 読み込んだ1バイトデータ
	 */
	public synchronized int read(){
		int data=0;
		try{
		  data = is.read();
		}catch(IOException e){
			System.out.println("can't read");
		}
	  return data;
	}


	/**
	 * ファイルから1行読み出す
	 *
	 * @return 読み込んだ1行のデータ
	 */
	public synchronized byte[] readLine(){
		ByteArrayOutputStream buf = new ByteArrayOutputStream();

		while(true){
			int data = 0;
			try{
				data = is.read();
			} catch(IOException e){
				e.printStackTrace();
			}
			// ファイルの末端または改行を見つけたとき
			if( data < 0 || data == '\n'){
				return buf.toByteArray();
			}
			else{
				buf.write(data);
			}
		}//end of while
	}


	/**
	 * ファイルにデータを書き込む
	 *
	 * @param data   書き込むデータ
	 * @param offset
	 * @param size
	 */
	public synchronized void write(final byte[] data,final int offset,final int size){
		try{
			os.write(data,offset,size);
		}catch(Throwable e){
			System.out.println("can't write stack data");
		}
	}

	/**
	 * ファイルにデータを書き込む
	 *
	 * @param data 書き込むデータ
	 *
	 */
	public synchronized void write(final byte[] data){
		try{
			os.write(data);
		}catch(Throwable e){
			System.out.println("can't write stack data");
		}
	}

	/**
	 * バッファ中のデータをフラッシュする
	 *
	 */
	public synchronized void flush(){
		try{
			os.flush();
		}catch (Exception e) {
			System.out.println("can't flush data");
		}
	}

}