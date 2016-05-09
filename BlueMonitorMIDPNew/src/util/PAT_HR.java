package util;

public class PAT_HR {

	/** –¬”g“`”dŠÔ */
	private int pat;
	/** S”” */
	private int hr;
	/** ‘O‰ñ‚Ì–¬”g“`”dŠÔ‚Ìæ“¾‚©‚ç‚ÌŠÔ */
	private double time;
	
	/**
	 * ƒRƒ“ƒXƒgƒ‰ƒNƒ^
	 * @param pat
	 * @param time
	 */
	public PAT_HR(int pat,int hr,double time) {
		this.pat = pat;
		this.hr  = hr;
		this.time = time;
	}
	
	//setter,getter
	
	public int getPat() {
		return pat;
	}
	public void setPat(int pat) {
		this.pat = pat;
	}
	
	public double getTime() {
		return time;
	}
	public void setTime(double time) {
		this.time = time;
	}

	public int getHR() {
		return hr;
	}
	public void setHR(int hr) {
		this.hr = hr;
	}
	
	

}
