/*
 * 作成日: 2005/9/10
 *
 * Copyright 2001 - 2008 Embedded.Samurai, Inc. All rights reserved.
 *
 * $Id: Key.java,v 1.1 2005/9/10 06:11:11 esamurai Exp $
 */

package request;


/**
	* インスタンスを作らない場合はfinalクラス
	*/
public final class Key{

	/**
	 * キー取得用フラグ.
	 * <p>
	 * keyFlag[0]: キーが押されたことを記憶する。<br>
	 * keyFlag[1]: キーが押されたことの判定に使う。<br>
	 * keyFlag[2]: キーが離されたことを記憶する。<br>
	 * keyFlag[3]: キーが長押しされたことの判定に使う。<br>
	 * </p>
	 */
	public static long keyFlag[] = new long[4];

	/**
		* コンストラクタ
		*/
	public Key(){
		for(int i=0;i<4;i++) keyFlag[i] = 0;
	}

	/**
		* 初期化
		*/
	public static void init(){
		// キーの初期化
		keyFlag[0] = 0;
		keyFlag[2] = 0;
	}

	/**
	 * キー処理を登録します。
	 * <br>
	 */
	public static void registKeyEvent() {
		keyFlag[1] = keyFlag[0];
		keyFlag[3] = keyFlag[2];
		keyFlag[0] = 0;
	}

	/**
	 * キー押し判断。
	 * @param key - キーコード
	 */
	public static boolean isKeyPressed(int key) {
		if ((keyFlag[1] & (1L << key)) != 0) {
			return true;
		}
		return false;
	}

	/**
		* キー長押し判断。
		* @param key - キーコード
		*/
	public static boolean isKeyRepeated(int key) {
		if ((keyFlag[3] & (1L << key)) != 0) {
			return true;
		}
		return false;
	}

	/**
	 * キーリリース判断
	 * @param key - キーコード
	 */
	public static boolean isKeyReleased(int key){
		if ((keyFlag[2] & (1L << key)) == 0) {
			return true;
		}
		return false;
	}

	/**
		* キーがとりあえず押されているかどうか確かめる。<BR>
		* この場合、押下と長押しの両方が含まれる。
		* @param key - キーコード
		* @return <lo><li>true - とりあえず押されている<li>false - 触られてすらいない</lo>
		*/
	public static boolean isKeyPressOrRepeated(int key){
		return isKeyPressed(key) || isKeyRepeated(key);
	}

	/**
		* キーが押されているか、リピートされていればtrueを返す。<BR>
		* ただしkeyWaitTimerとkeyRepeatWaitによってあらわされる<BR>
		* リピート間隔でtrueが帰る。<BR>
		* またisKeyPressed()に反応したときはkeyWaitTimerは意味を成さない。
		* これは押しっぱなしにするとゆるくカーソルが動くが、
		* キー連打するとカーソルが早く動かせるという機能に相当する。
		*/

	private static int keyWaitTimer;
	private static int keyRepeatWait = 250;

	public static boolean isKeyPressOrRepeatedWithWait(int key){
		if(isKeyPressed(key)){
			keyWaitTimer = 0;
			return true;
		}else if(isKeyRepeated(key)){
			if(keyWaitTimer > keyRepeatWait){
				keyWaitTimer = 0;
				return true;
			}
		}
		return false;
	}

}//end of Class
