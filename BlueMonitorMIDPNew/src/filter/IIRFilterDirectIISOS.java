package filter;

public class IIRFilterDirectIISOS {
	
	/**  セクションの数 */
	private int section;	
	
	/** 計算バッファ用補完データの配列 */
	private double[][] gapUn;
		
	/** 入力の補完データ用配列(フィルタの次数分必要)*/
	private double[] gapInput;

	/**
	 * コンストラクタ
	 * 
	 * @param section セクションの数
	 */
	public IIRFilterDirectIISOS(int order) {
		this.section = order;
		
		//フィルタの補完用データ配列
		gapInput = new double[2];
		
		//初期化
		for(int i = 0; i < gapInput.length ; i++){
			gapInput[i] = 0;
		}
		
		//計算バッファ用補完データ
		gapUn = new double[order][2];
			
		//初期化
		for(int i = 0; i < order ; i++){
			for(int j = 0; j < gapUn[i].length; j++){
				gapUn[i][j] = 0;
			}
		}
	}
	
	/**
	 * フィルタをかける(出力の端の無効データをなくすために、補完データを入力する)
	 * (二次セクションの継続接続の直接形II IIRフィルター)
	 * @param input         フィルタの入力データ
	 * @param output        フィルタの出力が格納される (出力と入力の配列は同じでもＯＫ)
	 * @param num           各セクションの分子係数を格納する配列
	 * @param den		   	各セクションの分母係数を格納する配列			
	 * @param gain        　　　　　	各セクションのゲインを格納する配列
	 */
	public void doFilter(final double[] input, double[] output
	        			,final double[][] num, final double[][] den
	        			,final double[] gain){
		
		//フィルタ用バッファ(今回入力分と、補完データ分の大きさを確保)
		double[] tmpInput  = new double[input.length+2];
		double[] tmpOutput = new double[output.length+2];
		//補完のため前回の入力をいれる
		for(int i = 0; i < 2 ; i++){
			tmpInput[i]  = gapInput[i];			
		}
		//今回の入力データを入れる
		for(int i = 0 ; i < input.length ; i++){			
			tmpInput[i+2] = input[i];
		}
		
		//計算用のバッファ(これも補完のために前回のデータを入れる)
		double[][] un = new double[section][input.length  + 2];
		double[][] yn = new double[section][output.length + 2];
		//補完のため前回の計算用バッファのデータをいれる
		for(int i = 0; i < section ; i++){
			for(int j = 0; j < 2 ; j++){
				un[i][j] = gapUn[i][j];
				yn[i][j] = 0;
			}				
		}		
				
		//フィルタ処理
		filter(tmpInput,tmpOutput,num,den,un,yn,gain);
		
		//次回の補完のために今回の端の入力を取っておく
		for(int i = 0; i < 2 ; i++){
			gapInput[i]  = tmpInput[tmpInput.length - 2 + i];			
		}
		
		for(int i = 0; i < section ; i++){
			for(int j = 0; j < 2 ; j++){
				gapUn[i][j] = un[i][un[i].length - 2 + j];				
			}				
		}
				
		//出力へ出力結果をコピー		
		for(int i = 0 ; i < output.length ; i++){
			output[i] = tmpOutput[i + 2];
		}
	
	}
	
	/**
	 * フィルタ処理をする
	 * @param input		入力データ
	 * @param output	出力データ
	 * @param num		分子係数
	 * @param den		分母係数
	 * @param un        計算用バッファ 
	 * @param yn		計算用バッファ
	 * @param gain		ゲインの配列  
	 */
	public void filter(final double[] input, double[] output
	        		   ,final double[][] num, final double[][] den
	        		   ,double[][] un,double[][] yn
	        		   ,final double[] gain){

		for(int i = 2; i < input.length ; i++){
						
			un[0][i] -= den[0][1]*un[0][i-1] + den[0][2]*un[0][i-2] + input[i]; 	 						
			yn[0][i] = num[0][0]*un[0][i] + num[0][1]*un[0][i-1] + num[0][2]*un[0][i-2];
			
			for(int j = 1; j < section ; j++){
				un[j][i] -= den[j][1]*un[j][i-1] + den[j][2]*un[j][i-2] + yn[j-1][i]*gain[j-1];	 						
				yn[j][i] = num[j][0]*un[j][i] + num[j][1]*un[j][i-1] + num[j][2]*un[j][i-2];			
			}
			output[i] = yn[section-1][i]*gain[section-1];

		}		

	}
}
