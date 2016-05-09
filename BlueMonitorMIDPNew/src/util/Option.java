package util;

public class Option {
	
	//共通の設定
	/** バックライトが常時点灯かどうか */
	private boolean isBackLightOn = false;
	/** サウンドがＯＮかどうか */
	private boolean isSoundOn     = false;
	/** MCBYを使用するかZEALを使用するか (true:MCBYを使用,false:ZEALを使用) */
	private boolean isMCBY        = true;
	/** blue toothからか、ファイルからか (true:Bluetooth(実機)を使用,false:ファイルからの読み込み(サンプル) */
	private boolean isBluetooth   = false;
	/** バイブレーションがＯＮかどうか */
	private boolean isVibrationOn = false;
	/** 50Hzの電源周波数であるか */
	private boolean is50HzPowerSupply = true;
	/** Bluetoothの接続時に一覧表示するかどうか */
	private boolean isBluetoothViewList = true;
	
	/** このクラスのインスタンス */
	private static Option op = null;
	
	/**
	 * コンストラクタ 
	 */
	private Option() {}
	
	//Setter,Getter
	/**
	 * Optionクラスの唯一のインスタンスを得る
	 * @return Optionクラスの唯一のインスタンス
	 */
	public static Option getOp() {
		if(op == null){
			return (op = new Option());
		}else{
			return op;
		}
	}

	public boolean isBackLightOn() {
		return isBackLightOn;
	}
	public void setBackLightOn(boolean isBackLightOn) {
		this.isBackLightOn = isBackLightOn;
	}

	public boolean isSoundOn() {
		return isSoundOn;
	}
	public void setSoundOn(boolean isSoundOn) {
		this.isSoundOn = isSoundOn;
	}

	public boolean isMCBY() {
		return isMCBY;
	}
	public void setMCBY(boolean isMCBY) {
		this.isMCBY = isMCBY;
	}

	public boolean isBluetooth() {
		return isBluetooth;
	}
	public void setBluetooth(boolean isBluetooth) {
		this.isBluetooth = isBluetooth;
	}

	public boolean isVibrationOn() {
		return isVibrationOn;
	}
	public void setVibrationOn(boolean isVibrationOn) {
		this.isVibrationOn = isVibrationOn;
	}

	public boolean is50HzPowerSupply() {
		return is50HzPowerSupply;
	}
	public void set50HzPowerSupply(boolean is50HzPowerSupply) {
		this.is50HzPowerSupply = is50HzPowerSupply;
	}

	public boolean isBluetoothViewList() {
		return isBluetoothViewList;
	}
	public void setBluetoothViewList(boolean isBluetoothViewList) {
		this.isBluetoothViewList = isBluetoothViewList;
	}
	
}
