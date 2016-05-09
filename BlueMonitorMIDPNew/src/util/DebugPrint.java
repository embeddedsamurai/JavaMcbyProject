package util;

//デバッグ用のメソッド

public class DebugPrint{

	public int debugIntPrint1=0;
	public int debugIntPrint2=0;
	public int debugIntPrint3=0;
	public int debugIntPrint4=0;
	public int debugIntPrint5=0;
	public int debugIntPrint6=0;
	public int debugIntPrint7=0;
	public int debugIntPrint8=0;
	public int debugIntPrint9=0;
	public int debugIntPrint10=0;

	public long debugLongPrint1=0;
	public long debugLongPrint2=0;

	private long startTime1=0;
	private long endTime1=0;

	public String debugStringPrint1="";


	public void StartTime1(){
		this.startTime1=System.currentTimeMillis();
	}

	private long getperiodonetime1=0;

	public void EndOneTime1(){
		this.endTime1=System.currentTimeMillis();
		getperiodonetime1 = this.endTime1 - this.startTime1;
	}

	private long measureCount1=0;
	private long mSum1=0;
	private double getperiodtime1=0;

	public void EndTime1(){
		this.endTime1=System.currentTimeMillis();

		this.mSum1 += (this.endTime1-this.startTime1);
		this.measureCount1++;

		this.getperiodtime1 = (double)(this.mSum1) / this.measureCount1;

		if(this.mSum1 > Long.MAX_VALUE){
			this.mSum1=0;
	    this.measureCount1=0;
	  }
	}

	public long GetPeriodOneTime1(){
	 //この関数を呼び出すときは、
	 //DebugPrint.startTime1();
	 //DebugPrint.endOneTime1();
	 //を実行すること
		return this.getperiodonetime1;
	}

	public double GetPeriodTime1(){
	 //この関数を呼び出すときは、
	 //DebugPrint.startTime1();
	 //DebugPrint.endTime1();
	 //を実行すること
		return this.getperiodtime1;
	}

	public DebugPrint() {

	}

	//------------------------------------------------------------
	//1メソッドの呼び出し時間計測 spanTime
	//---------------------------------------------------------------
	private float spantime1   = 0;
	private int   spancount1 = 19;
	private long  methodtime1  = System.currentTimeMillis();

	public void SpanTime1()
	{
		if (++spancount1 == 20){
			spantime1   = (float)((System.currentTimeMillis() - methodtime1) / 20);
			methodtime1  = System.currentTimeMillis();
			spancount1 = 0;
		}
	}

	public float GetSpanTime1(){
		return spantime1;
	}

		//------------------------------------------------------------
	//1メソッドの呼び出し時間計測 spanTime OneTime
	//---------------------------------------------------------------
	private float spantime_onetime1   = 0;
	private long  methodtimeOneTime1  = System.currentTimeMillis();

	public void SpanTimeOneTime1()
	{
			spantime_onetime1   = (float)(System.currentTimeMillis() - methodtimeOneTime1);
			methodtimeOneTime1  = System.currentTimeMillis();
	}

	public float GetSpanOneTime1(){
		return spantime_onetime1;
	}


		//------------------------------------------------------------
	//1メソッドの呼び出し時間計測 spanTime2
	//---------------------------------------------------------------
	private float spantime2   = 0;
	private int   spancount2 = 19;
	private long  methodtime2  = System.currentTimeMillis();

	public void SpanTime2()
	{
		if (++spancount2 == 20){
			spantime2   = (float)((System.currentTimeMillis() - methodtime2) / 20);
			methodtime2  = System.currentTimeMillis();
			spancount2 = 0;
		}
	}

	public float GetSpanTime2(){
		return spantime2;
	}

}
