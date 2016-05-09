package calendar.util;

import java.util.Enumeration;
import java.util.Vector;


/**
 * DB�ɐڑ����A�K�v�ȃf�[�^������Ă���N���X	 
 */
public class ReadDBThread extends Thread{

	/** ���[�h */
	private final int type;
	/** MCBY ID */
	private final int mcby_id;
	/** �ǂݍ��݊����A�܂��͎��s�����Ƃ��̒ʒm��̃��X�g */
	private Vector listenerList;
	/** �ǂݍ��񂾃f�[�^�p�̃o�b�t�@ */
	private int[] receiveData;
	/** DB����̃f�[�^�ǂݍ��݃N���X */
	private ReadDBData readDB;
	/** DB����̃f�[�^�ǂݍ��݃N���X */
	private ReadNumOfDBData readNum;
	/** �f�[�^�ǂݍ���: true �f�[�^���ǂݍ���: false*/
	private boolean flag = true;
	/** �ǂݍ��ރf�[�^�̎��� */
	private final long time;
	private final int  offset;
	
    /**
     * �R���X�g���N�^
     * @param type     �f�[�^�^�C�v
     * @param mcby_id  MCBY_ID
     * @param len      �ǂݍ��ރf�[�^��
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
    
    //-------------------���X�i�̒ǉ��A�폜--------------------//
    
    /**
     * DB����̓ǂݍ��݊����A���s�̒ʒm���ǉ�
     */
    public void addListener(ReadDBListener listener){    	
    	if(!listenerList.contains(listener)){
    		//�܂����X�i�Ɋ܂܂�Ă��Ȃ��Ƃ�,���X�g�Ƀ��X�i��ǉ�����
    		listenerList.addElement(listener);
    	}
    }
    
    /**
     * DB����̓ǂݍ��݊����A���s�̒ʒm����폜
     */
    public void deleteListener(ReadDBListener listener){
    	if(listenerList.contains(listener)){
    		//���X�g�Ɋ܂܂�Ă���Ƃ��A���X�g����폜
    		listenerList.removeElement(listener);
    	}
    }
    
    /**
     * ���X�i�ɒʒm 
     * @param flag true:������ʒm false:���s��ʒm
     * @param num  �ǂݍ��񂾃f�[�^ 
     */
    private void notifyListener(boolean flag,int num){
    	//���X�i�̗�
    	Enumeration enumration = listenerList.elements();
    	
    	if(flag){
    		//������ʒm
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
    		//���s��ʒm
    		while(enumration.hasMoreElements()){
    			ReadDBListener listener = (ReadDBListener)enumration.nextElement();
    			listener.onErrorReadDB();
    		}//End of while()	    		
    	}//End of if(flag)
    }

    //-------------------DB�ڑ��A���o��--------------------//
    
    /**
     * DB�ւ̐ڑ��A�f�[�^�̎��o��
     */
    public void run() {
    	try{	    		
    		//�f�[�^�擾��
	    	int num = -1;

	    	//DB����̃f�[�^�̎擾
	    	if(flag){	    		
	    		num = readDB.read(receiveData,offset,receiveData.length,type,mcby_id,time);
	    	}else{
	    		num = readNum.read(receiveData,type,mcby_id,time);	    		
	    	}
	    	
	    	// Debug Print
			System.out.println("\n�ǂݍ��ݐ�:" + num);
			
			//���X�i�ɒʒm
			if(num != -1){
				//�f�[�^���擾�ł�����A����		
				System.out.println("�f�[�^�擾����");
				notifyListener(true,num);	
			}else{				
				//�f�[�^���擾�ł��Ȃ�������A���s
				notifyListener(false,num);
			}
	    	
    	}catch (Exception e) {
    		e.printStackTrace();
    		//���s��ʒm
    		notifyListener(false,-1);
    		return;
		}// End of try catch
    }//End of run()
        
    /**
     * �ǂݍ��݂̒��~
     */
    public void stop(){
    	if(flag){
    		readDB.disconnect();	
    	}else{
    		readNum.disconnect();
    	}//End of if()
    }//End of stop()
    	
}//End of HTTPThread 
