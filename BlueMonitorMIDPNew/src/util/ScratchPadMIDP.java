package util;

import java.io.InputStream;

import javax.microedition.rms.RecordStore;

public class ScratchPadMIDP extends ScratchPad {
	
	private static final String NAME = "data"; 

	/**
	 *�@�R���X�g���N�^ 
	 */
	public ScratchPadMIDP() {
	
	}
	
	/**
	 * ���R�[�h�X�g�A����̓ǂݍ���
	 */
	public void read() {
		try {
		    //�f�[�^�p
	    	byte[][] data = new byte[4][];
			//���R�[�h�X�g�A
			RecordStore rs = RecordStore.openRecordStore(NAME,false);
			if(rs == null)return;
			
			//���R�[�h�X�g�A����f�[�^�̓ǂݍ���
			for(int i = 0; i < data.length ;i++){
				data[i] = rs.getRecord(i+1);
			}
			
			//���k������
			baseSbp = Double.parseDouble(new String(data[0]));
			red_wavelength = Double.parseDouble(new String(data[1]));
			infared_wavelength = Double.parseDouble(new String(data[2]));
			pls_div_cur = Double.parseDouble(new String(data[3]));
			
			//���R�[�h�X�g�A�����
			rs.closeRecordStore();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * ���R�[�h�X�g�A�ւ̏�������
	 */
	public void write() {
		//HTTP����̃f�[�^�̎擾
		//�����ɂc�a����̃f�[�^�擾�̃R�[�h������
			
		
		RecordStore rs = null;
	    try{
		    //�f�[�^�p
	    	byte[][] data = new byte[4][];
		    //���R�[�h�X�g�A
		    rs = RecordStore.openRecordStore(NAME,true);
		    
	    	//�������ݗp�f�[�^(���k������)
	    	data[0] = Double.toString(baseSbp).getBytes();
	    	//�������ݗp�f�[�^(�ԊO���g��)
	    	data[1] = Double.toString(red_wavelength).getBytes();
	    	//�������ݗp�f�[�^(���d�g��)
	    	data[2] = Double.toString(infared_wavelength).getBytes();
	    	//�������ݗp�f�[�^(�쓮�d��)
	    	data[3] = Double.toString(pls_div_cur).getBytes();
	    	
	    	for(int i = 0; i < data.length; i++){
		    	//�f�[�^�̏������� 0�Ԗ�
		    	if(rs.getNumRecords() == 0){					//�f�[�^���Ȃ��Ƃ�
		    		rs.addRecord(data[i],0,data[i].length);
		    	}else{  										//�f�[�^����������X�V      
		    		rs.setRecord(i+1,data[i],0,data[i].length);
		    	}	    		
	    	}
          	//���R�[�h�X�g�A�����	    	      
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
