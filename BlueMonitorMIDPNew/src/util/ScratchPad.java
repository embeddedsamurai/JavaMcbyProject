package util;

public abstract class ScratchPad {
	
	/** ���k������      �@�@�@*/
	protected double baseSbp            = 0.0;
	/** �ԊO����(660nm) */
	protected double red_wavelength     = 0.0;
	/** ���d�g��(340nm) */
	protected double infared_wavelength = 0.0;
	/** �쓮�d��        */
	protected double pls_div_cur        = 0.0;
	
	/**
	 *�@�R���X�g���N�^ 
	 */
	public ScratchPad() {
	
	}
	
	/**
	 * �X�N���b�`�p�b�h�ւ̏�������
	 */
	public abstract void write();
	
	/**
	 * �X�N���b�`�p�b�h����̓ǂݍ���
	 */
	public abstract void read();
	
	//�Q�b�^�[�A�Z�b�^�[
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
