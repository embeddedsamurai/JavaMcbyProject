package filter;

public class FIRFilter {

	/**  フィルタの次数 */
	private int order;	
	/** 入力の補完データ用配列(フィルタの次数分必要)*/
	private double[] gapInput;
	
	/**
	 * コンストラクタ
	 *  
	 * @param order 次数
	 */
	public FIRFilter(int order) {
		this.order = order;
		
		//フィルタの補完用データ配列
		gapInput = new double[order];
		
		//初期化
		for(int i = 0; i < gapInput.length ; i++){
			gapInput[i] = 0;
		}
	}	
	
	/**
   	 *FIRフィルターを実行する関数
	 * 
	 * @param input   入力値配列
	 * @param output  出力値の配列
	 * @param coff     係数の配列     
	 */
	public synchronized void filter(final double[] input,double[] output
			                       ,final double[] coff){	
		//50Hz シングルノッチフィルタ-
		for(int i = coff.length - 1; i < input.length; i++){
			output[i] = 0;
			
			//絶対値が小さくなり正しく計算ができないなることをわけるため
			//足し算と引き算をわける			
			double[] am = new double[coff.length];						
			for(int j = 0; j < coff.length ; j++){
				am[j] = coff[j]*input[i-j];
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
	 * フィルタをかける(出力の端の無効データをなくすために、補完データを入力する) *FIRフィルタ
	 * 
	 * @param input     フィルタの入力データ
	 * @param output    フィルタの出力が格納される  (出力と入力の配列は同じでもＯＫ)
	 * @param gapInput  入力の補完データ用配列    (フィルタの次数分必要)     
	 * @param coff　　　　   係数の配列
	 */
	public synchronized void filterRapper(final double[] input,double[] output,
									       final double[] coff){
		
		//フィルタ保管用データのサイズ
		final int gapSize = gapInput.length;
		//フィルタ用バッファ(今回入力分と、補完データ分の大きさを確保)
		double[] tmpInput = new double[input.length+gapSize];
		double[] tmpOutput= new double[input.length+gapSize];
		
		//補完のため前回の入力をいれる
		for(int i = 0; i < gapSize ; i++){
			tmpInput[i]  = gapInput[i];			
		}
		//今回の入力データを入れる
		for(int i = 0 ; i < input.length ; i++){			
			tmpInput[i + gapSize] = input[i];
		}
		
		//フィルタ処理
		filter(tmpInput,tmpOutput,coff);
		
		//次回の補完のために今回の入力を取っておく
		for(int i = 0; i < gapSize ; i++){
			gapInput[i]  = tmpInput[tmpInput.length - gapSize + i];			
		}
		
		//出力へ出力結果をコピー		
		for(int i = 0 ; i < output.length ; i++){
			output[i] = tmpOutput[i + gapSize];
		}
	}
}
