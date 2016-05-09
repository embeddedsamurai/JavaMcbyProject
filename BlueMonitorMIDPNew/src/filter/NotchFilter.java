package filter;

public interface NotchFilter {
	/** 50Hzノッチフィルタの分母係数 */
	public static final double[] NOTCH50_DEN =
	{
			1,
			-0.58070491619799569,
			0.87920058368503273,			
	};	
	/** 50Hzノッチフィルタの分子係数 */
	public static final double[] NOTCH50_NUM =
	{
		    0.93960029184251637,
		    -0.58070491619799569,
		    0.93960029184251637,
	};
	
	/** 60Hzノッチフィルタの分母係数 */
	public static final double[] NOTCH60_DEN = {  
		  1                   ,                
		  -0.11799598094937247,                 
		   0.87920058368503307,    
	};
	
	/** 60Hzノッチフィルタの分子係数 */
	public static final double[] NOTCH60_NUM = {
		 0.93960029184251637,                 
		 -0.11799598094937243,                 
		  0.93960029184251703,   
	};
}
