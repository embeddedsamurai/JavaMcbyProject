/***************************************************/
/* SDCardMIDP�N���X                                */
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
		* ���@�ł̎��s���A�G�~�����[�^�ł̎��s���̃t���O
		* true:  ���@�ł̎��s
		* false:�G�~�����[�^����̎��s
		*/
	private  boolean runFlag;

	/** �R�l�N�V���� */
	private FileConnection storage;

	/**
		* �R���X�g���N�^
		* @param runFlag ���@�ł̎��s���A�G�~�����[�^�ł̎��s��
		* true: ���@�ł̎��s  false:�G�~�����[�^����̎��s
		*/
	public SDCardMIDP(boolean runFlag){
		this.runFlag = runFlag;
	}


	//--���s����O �T�C�Y�������Ă��� ---
	public class ArraySizeOutOfBoundsError extends Exception{
		public ArraySizeOutOfBoundsError(String str){
			System.out.println("str="+str);
		}
	}


	/** �t�@�C�����I�[�v������ */
	public synchronized void open(final String fname,final int mode){
		// �t�@�C�����I�[�v������
		try{
			String name = null;

			if(runFlag){
				name = "file:///E:/Data/"+fname;    //SD�J�[�h������s��
			}else{
				name = "file:///root1/Data/"+fname; //WTK �G�~�����[�^������s��
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
	 * �t�@�C���X�g���[�����N���[�Y����
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
	 * rdata�z���offset�̈ʒu����length���A�f�[�^��ǂݍ���
	 *
	 * @return �ǂݍ��񂾃f�[�^�̐�
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
	 * rdata�z���0�̈ʒu����rdata.length���A�f�[�^��ǂݍ���
	 *
	 * @return �ǂݍ��񂾃f�[�^�̐�
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
	 * �t�@�C������1�o�C�g�ǂݏo��
	 *
	 * @return �ǂݍ���1�o�C�g�f�[�^
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
	 * �t�@�C������1�s�ǂݏo��
	 *
	 * @return �ǂݍ���1�s�̃f�[�^
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
			// �t�@�C���̖��[�܂��͉��s���������Ƃ�
			if( data < 0 || data == '\n'){
				return buf.toByteArray();
			}
			else{
				buf.write(data);
			}
		}//end of while
	}


	/**
	 * �t�@�C���Ƀf�[�^����������
	 *
	 * @param data   �������ރf�[�^
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
	 * �t�@�C���Ƀf�[�^����������
	 *
	 * @param data �������ރf�[�^
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
	 * �o�b�t�@���̃f�[�^���t���b�V������
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