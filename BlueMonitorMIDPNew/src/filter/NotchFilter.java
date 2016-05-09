package filter;

public interface NotchFilter {
	/** 50Hz�m�b�`�t�B���^�̕���W�� */
	public static final double[] NOTCH50_DEN =
	{
			1,
			-0.58070491619799569,
			0.87920058368503273,			
	};	
	/** 50Hz�m�b�`�t�B���^�̕��q�W�� */
	public static final double[] NOTCH50_NUM =
	{
		    0.93960029184251637,
		    -0.58070491619799569,
		    0.93960029184251637,
	};
	
	/** 60Hz�m�b�`�t�B���^�̕���W�� */
	public static final double[] NOTCH60_DEN = {  
		  1                   ,                
		  -0.11799598094937247,                 
		   0.87920058368503307,    
	};
	
	/** 60Hz�m�b�`�t�B���^�̕��q�W�� */
	public static final double[] NOTCH60_NUM = {
		 0.93960029184251637,                 
		 -0.11799598094937243,                 
		  0.93960029184251703,   
	};
}
