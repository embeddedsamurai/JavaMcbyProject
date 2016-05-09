package util;

public abstract class ScratchPad {
	
	/** 収縮期血圧      　　　*/
	protected double baseSbp            = 0.0;
	/** 赤外線長(660nm) */
	protected double red_wavelength     = 0.0;
	/** 光電波長(340nm) */
	protected double infared_wavelength = 0.0;
	/** 駆動電流        */
	protected double pls_div_cur        = 0.0;
	
	/**
	 *　コンストラクタ 
	 */
	public ScratchPad() {
	
	}
	
	/**
	 * スクラッチパッドへの書き込み
	 */
	public abstract void write();
	
	/**
	 * スクラッチパッドからの読み込み
	 */
	public abstract void read();
	
	//ゲッター、セッター
	public double getBaseSbp() {
		return baseSbp;
	}
	public void setBaseSbp(double baseSbp) {
		this.baseSbp = baseSbp;
	}

	public double getRed_wavelength() {
		return red_wavelength;
	}
	public void setRed_wavelength(double red_wavelength) {
		this.red_wavelength = red_wavelength;
	}

	public double getInfared_wavelength() {
		return infared_wavelength;
	}
	public void setInfared_wavelength(double infared_wavelength) {
		this.infared_wavelength = infared_wavelength;
	}

	public double getPls_div_cur() {
		return pls_div_cur;
	}
	public void setPls_div_cur(double pls_div_cur) {
		this.pls_div_cur = pls_div_cur;
	}	
	
	
		
}
