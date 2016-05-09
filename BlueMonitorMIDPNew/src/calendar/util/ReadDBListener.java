package calendar.util;

public interface ReadDBListener {

	/**
	 * DBからの読み込みに成功したとき
	 * 
	 * @param data 読み込んだデータ
	 * @param type データタイプ
	 * @param time 取得したデータのdata_start_time
	 */
	public void onCompleteReadDB(int[] data,int type,long time);
		
	/**
	 * DBからの読み込みに失敗したとき
	 */
	public void onErrorReadDB();
}
