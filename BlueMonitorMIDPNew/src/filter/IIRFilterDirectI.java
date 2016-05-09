package filter;

public class IIRFilterDirectI {

	/**  フィルタの次数 */
	private int order;	
	/** 入力の補完データ用配列(フィルタの次数分必要)*/
	private double[] gapInput;
	/** 入力の補完データ用配列(フィルタの次数分必要)*/
	private double[] gapOutput;
	
	/**
	 * コンストラクタ 
	 * @param order 次数
	 */
	public IIRFilterDirectI(int order) {
		this.order = order;
		
		//フィルタの補完用データ配列
		gapInput = new double[order];
		gapOutput = new double[order];
		
		//初期化
		for(int i = 0; i < gapInput.length ; i++){
			gapInput[i] = 0;
			gapOutput[i] = 0;
		}
		
	}
	
	/**
   	 * 直接型Ｉ IIRフィルターを実行する関数
	 * 
	 * @param input   入力値配列
	 * @param output  出力値の配列
	 * @param num     分子係数の配列     
	 * @param den　　　　 分母係数の配列
	 */
	public synchronized void filter(final double[] input,double[] output
			                       ,final double[] num, final double[] den){	
	
		for(int i = den.length - 1; i < input.length; i++){
			output[i] = 0;
			
			//絶対値が小さくなり正しく計算ができないなることをわけるため
			//足し算と引き算をわける			
			double[] am = new double[num.length + den.length];						
			for(int j = 0; j < num.length ; j++){
				am[j] = num[j]*input[i-j];
				am[j+num.length] = -1 * (den[j]*output[i-j]);
			}			
			//足し算
			for(int j = 0; j < am.length ; j++){
				if(am[j] >= 0){
					output[i] += am[j];
				}
			}
			//引き算
			for(int j = 0; j < am.length ; j++){
				if(am[j] < 0){
					output[i] += am[j];
				}
			}		
		}		
	}
	
	/**
	 * フィルタをかける(出力の端の無効データをなくすために、補完データを入力する) *IIRフィルタ
	 * 
	 * @param input     フィルタの入力データ
	 * @param output    フィルタの出力が格納される (出力と入力の配列は同じでもＯＫ)
	 * @param gapInput  入力の補完データ用配列(フィルタの次数分必要)
	 * @param gapOutput 出力の補完データ用配列(フィルタの次数分必要)
	 * @param num     分子係数の配列     
	 * @param den　　　　 分母係数の配列
	 */
	public synchronized void doFilter(final double[] input,double[] output,						
									       final double[] num,final double[] den){
		
		//フィルタ保管用データのサイズ
		final int gapSize = gapInput.length;
		//フィルタ用バッファ(今回入力分と、補完データ分の大きさを確保)
		double[] tmpInput = new double[input.length+gapSize];
		double[] tmpOutput= new double[output.length+gapSize];
		
		//補完のため前回の入力と出力をいれる
		for(int i = 0; i < gapSize ; i++){
			tmpInput[i]  = gapInput[i];
			tmpOutput[i] = gapOutput[i];
		}
		//今回の入力データを入れる
		for(int i = 0 ; i < input.length ; i++){			
			tmpInput[i + gapSize] = input[i];
		}
		
		//フィルタ処理
		filter(tmpInput,tmpOutput,num,den);
		
		//次回の補完のために今回の端の出力と、入力を取っておく
		for(int i = 0; i < gapSize ; i++){
			gapInput[i]  = tmpInput[tmpInput.length - gapSize + i];
			gapOutput[i] = tmpOutput[tmpOutput.length - gapSize + i];
		}
		
		//出力へ出力結果をコピー		
		for(int i = 0 ; i < output.length ; i++){
			output[i] = tmpOutput[i + gapSize];
		}
	}
}
