package calendar.util;

public interface ReadDBListener {

	/**
	 * DB����̓ǂݍ��݂ɐ��������Ƃ�
	 * 
	 * @param data �ǂݍ��񂾃f�[�^
	 * @param type �f�[�^�^�C�v
	 * @param time �擾�����f�[�^��data_start_time
	 */
	public void onCompleteReadDB(int[] data,int type,long time);
		
	/**
	 * DB����̓ǂݍ��݂Ɏ��s�����Ƃ�
	 */
	public void onErrorReadDB();
}
