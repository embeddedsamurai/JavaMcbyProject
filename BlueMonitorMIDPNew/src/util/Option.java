package util;

public class Option {
	
	//���ʂ̐ݒ�
	/** �o�b�N���C�g���펞�_�����ǂ��� */
	private boolean isBackLightOn = false;
	/** �T�E���h���n�m���ǂ��� */
	private boolean isSoundOn     = false;
	/** MCBY���g�p���邩ZEAL���g�p���邩 (true:MCBY���g�p,false:ZEAL���g�p) */
	private boolean isMCBY        = true;
	/** blue tooth���炩�A�t�@�C�����炩 (true:Bluetooth(���@)���g�p,false:�t�@�C������̓ǂݍ���(�T���v��) */
	private boolean isBluetooth   = false;
	/** �o�C�u���[�V�������n�m���ǂ��� */
	private boolean isVibrationOn = false;
	/** 50Hz�̓d�����g���ł��邩 */
	private boolean is50HzPowerSupply = true;
	/** Bluetooth�̐ڑ����Ɉꗗ�\�����邩�ǂ��� */
	private boolean isBluetoothViewList = true;
	
	/** ���̃N���X�̃C���X�^���X */
	private static Option op = null;
	
	/**
	 * �R���X�g���N�^ 
	 */
	private Option() {}
	
	//Setter,Getter
	/**
	 * Option�N���X�̗B��̃C���X�^���X�𓾂�
	 * @return Option�N���X�̗B��̃C���X�^���X
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
