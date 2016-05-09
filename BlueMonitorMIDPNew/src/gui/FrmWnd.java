package gui;


import java.util.Hashtable;
import javax.microedition.lcdui.Image;

public class FrmWnd {
	
	//  色	
	/** 灰色 */
	private static final int COLOR_GRAY = 0xDCDCDC;
	
	/** 枠画像の数 */
	private static final int NUM_OF_FRAME_IMG = 3;	
	/** 枠画像 */
	private Image frameImages[] = new Image[NUM_OF_FRAME_IMG];
	/** 枠画像の幅 */
	private int frameWidth;
	/** 枠画像の高さ */
	private int frameHeight;
	
	/** 背景色 */
	private int bgColor = COLOR_GRAY;
	/** 背景の透過度 */
	private int bgAlpha = 0xCF;

	/** 透過処理の許可 */
	private boolean acceptAlpha = true;	
	/**
	 * 枠画像の透過色の指定<br>
	 * <br>
	 * <p>透過PNG非対応の端末で使用(-1で無効)</p>
	 */
	private int alphaColor = -1;
	
	/** 作成したウインドウを保持 */	
	private Hashtable windowList  = new Hashtable();
	
	/** 枠なしか、ありか*/	 
	private boolean enableFrm = false;
	
	/**
	 * 枠なしウィンドウをつくるとき
	 * コンストラクタ 
	 */
	public FrmWnd(boolean alpha){
		this.acceptAlpha = alpha;		
		enableFrm = false;
	}
	
	/**
	 * コンストラクタ
	 * @param frameImages　枠画像の配列
	 * @param alpha　透化するかどうか 
	 * 
	 * @throws Exception
	 */
	public FrmWnd(Image[] frameImages,boolean alpha) throws Exception{
		// 枠イメージの数が間違っている場合
		if(frameImages.length != NUM_OF_FRAME_IMG){
			String msg = "Image Number is not correct.";
			System.out.println(msg);		
			new Exception(msg);
		}		
		// 枠イメージのコピー
		this.frameImages = frameImages;		
		// 枠イメージのサイズを取得
		frameWidth = frameImages[0].getWidth();
		frameHeight = frameImages[0].getHeight();		
		// 透過処理のON/OFF
		this.acceptAlpha = alpha;
		
		enableFrm = true;
	}
	
	/**
	 * 文字列入りウインドウの描画<br>
	 * <br>
	 * <p>ウインドウの幅と高さは枠画像のサイズの整数倍に設定してください</p>
	 * 
	 * @param w ウインドウの幅
	 * @param h ウインドウの高さ
	 * 
	 * @param ウィンドウ枠の画像 
	 */
	public synchronized Image getWindow(int w,int h){		
		// ウインドウのサイズを補正
		if(enableFrm){
			if(w%16 != 0) w -= w%16;
			if(h%16 != 0) h -= h%16;			
		}		
		//ウィンドウのキー
		WindowKey key = new WindowKey(w,h);		
		if(windowList.containsKey(key)){      // ウインドウを再利用できる場合			
			//ウィンドウ画像の取得						
			return (Image)windowList.get(key);			
		}else{                      			
			// ウインドウをまだ作成していない場合
			//色情報を持つ配列
			int[] rgb = new int[w*h];
			//背景の作成
			createWindowBGRGB(rgb,w,h);
			//枠の作成
			if(enableFrm)createWindowFrameRGB(rgb,w,h);
			//RGB列からイメージを作成			
			Image img = Image.createRGBImage(rgb,w,h,acceptAlpha);			
			//ウインドウの登録
			windowList.put(key,img);
			//作成したウィンドウ画像を返す
			return img;
		}			
	}
	
	//================================色情報列の作成==================================//
	
	/**
	 * 枠のRGB情報を描画<br>
	 * <br>
	 * <p>ウインドウの幅と高さは枠画像のサイズの整数倍に設定してください</p>
	 *
	 * @param rgb RGB情報を格納する配列
	 * @param w 枠の幅
	 * @param h 枠の高さ
	 */
	private void createWindowFrameRGB(int[] rgb, int w, int h){
		
		// 枠の角の描画
		int[] cRGB = new int[frameWidth*frameHeight];
		frameImages[0].getRGB(cRGB, 0, frameWidth, 0, 0, frameWidth, frameHeight);
		// 左上の描画
		for(int i=0; i < frameWidth; i++){
			for(int j=0; j < frameHeight; j++){
				if(alphaColor >= 0){
					if(alphaColor != (cRGB[i+j*frameWidth] & 0xFFFFFF)){
						rgb[i+j*w] = cRGB[i+j*frameWidth];
					}
				} else if (((cRGB[i+j*frameWidth] & 0xFF000000) >> 24) != 0x00){
					rgb[i+j*w] = cRGB[i+j*frameWidth];
				}
			}
		}
		// 右上の描画
		for(int i=0; i < frameWidth; i++){
			for(int j=0; j < frameHeight; j++){
				if(alphaColor >= 0){
					if(alphaColor != (cRGB[(frameWidth-i-1)+j*frameWidth] & 0xFFFFFF)){
						rgb[(i+w-frameWidth)+j*w] = cRGB[(frameWidth-i-1)+j*frameWidth];
					}
				} else if(((cRGB[(frameWidth-i-1)+j*frameWidth] & 0xFF000000) >> 24) != 0x00){
					rgb[(i+w-frameWidth)+j*w] = cRGB[(frameWidth-i-1)+j*frameWidth];
				}
			}
		}
		// 右下の描画
		for(int i=0; i < frameWidth; i++){
			for(int j=0; j < frameHeight; j++){
				if(alphaColor >= 0){
					if(alphaColor != (cRGB[(frameWidth-i-1)+(frameHeight-j-1)*frameWidth] & 0xFFFFFF)){
						rgb[(i+w-frameWidth)+(j+h-frameHeight)*w] = cRGB[(frameWidth-i-1)+(frameHeight-j-1)*frameWidth];
					}
				} else if(((cRGB[(frameWidth-i-1)+(frameHeight-j-1)*frameWidth] & 0xFF000000) >> 24) != 0x00){
					rgb[(i+w-frameWidth)+(j+h-frameHeight)*w] = cRGB[(frameWidth-i-1)+(frameHeight-j-1)*frameWidth];
				}
			}
		}
		// 左下の描画
		for(int i=0; i < frameWidth; i++){
			for(int j=0; j < frameHeight; j++){
				if(alphaColor >= 0){
					if(alphaColor != (cRGB[i+(frameHeight-j-1)*frameWidth] & 0xFFFFFF)){
						rgb[i+(j+h-frameHeight)*w] = cRGB[i+(frameHeight-j-1)*frameWidth];
					}
				} else if(((cRGB[i+(frameHeight-j-1)*frameWidth] & 0xFF000000) >> 24) != 0x00){
					rgb[i+(j+h-frameHeight)*w] = cRGB[i+(frameHeight-j-1)*frameWidth];
				}
			}
		}
		
		// 左上の角の外側の透過処理
		for(int i=0; i < frameWidth; i++){
			for(int j=0; j < frameHeight; j++){
				if(alphaColor >= 0){
					if(alphaColor == (cRGB[i+j*frameWidth] & 0xFFFFFF)){
						rgb[i+j*w] &= 0x00FFFFFF;
					} else{
						break;
					}
				} else{
					if(((cRGB[i+j*frameWidth] & 0xFF000000) >> 24) == 0x00){
						rgb[i+j*w] &= 0x00FFFFFF;
					} else{
						break;
					}
				}
			}
		}
		// 右上の角の外側の透過処理
		for(int i=0; i < frameWidth; i++){
			for(int j=0; j < frameHeight; j++){
				if(alphaColor >= 0){
					if(alphaColor == (cRGB[(frameWidth-i-1)+j*frameWidth] & 0xFFFFFF)){
						rgb[(i+w-frameWidth)+j*w] &= 0x00FFFFFF;
					} else{
						break;
					}
				} else{
					if(((cRGB[(frameWidth-i-1)+j*frameWidth] & 0xFF000000) >> 24) == 0x00){
						rgb[(i+w-frameWidth)+j*w] &= 0x00FFFFFF;
					} else{
						break;
					}
				}
			}
		}
		// 右下の角の外側の透過処理
		for(int i=0; i < frameWidth; i++){
			for(int j=frameHeight-1; j >= 0; j--){
				if(alphaColor >= 0){
					if(alphaColor == (cRGB[(frameWidth-i-1)+(frameHeight-j-1)*frameWidth] & 0xFFFFFF)){
						rgb[(i+w-frameWidth)+(j+h-frameHeight)*w] &= 0x00FFFFFF;
					} else{
						break;
					}
				} else{
					if(((cRGB[(frameWidth-i-1)+(frameHeight-j-1)*frameWidth] & 0xFF000000) >> 24) == 0x00){
						rgb[(i+w-frameWidth)+(j+h-frameHeight)*w] &= 0x00FFFFFF;
					} else{
						break;
					}
				}
			}
		}
		// 左下の角の外側の透過処理
		for(int i=0; i < frameWidth; i++){
			for(int j=frameHeight-1; j >= 0; j--){
				if(alphaColor >= 0){
					if(alphaColor == (cRGB[i+(frameHeight-j-1)*frameWidth] & 0xFFFFFF)){
						rgb[i+(j+h-frameHeight)*w] &= 0x00FFFFFF;
					} else{
						break;
					}
				} else{
					if(((cRGB[i+(frameHeight-j-1)*frameWidth] & 0xFF000000) >> 24) == 0x00){
						rgb[i+(j+h-frameHeight)*w] &= 0x00FFFFFF;
					} else{
						break;
					}
				}
			}
		}
		
		// 左右の枠を描画
		int[] lrRGB = new int[frameWidth*frameHeight];
		frameImages[1].getRGB(lrRGB, 0, frameWidth, 0, 0, frameWidth, frameHeight);
		int lrlen = h / frameHeight -2;
		for(int k=0; k < lrlen; k++){
			// 左の枠を描画
			for(int i=0; i < frameWidth; i++){
				for(int j=0; j < frameHeight; j++){
					if(alphaColor >= 0){
						if(alphaColor != (lrRGB[i+j*frameWidth] & 0xFFFFFF)){
							rgb[i+(j+(k+1)*frameHeight)*w] = lrRGB[i+j*frameWidth];
						}
					} else{
						if(((lrRGB[i+j*frameWidth] & 0xFF000000) >> 24) != 0x00){
							rgb[i+(j+(k+1)*frameHeight)*w] = lrRGB[i+j*frameWidth];
						}
					}
				}
			}
			// 右の枠を描画
			for(int i=0; i < frameWidth; i++){
				for(int j=0; j < frameHeight; j++){
					if(alphaColor >= 0){
						if(alphaColor != (lrRGB[(frameWidth-i-1)+j*frameWidth] & 0xFFFFFF)){
							rgb[(i+w-frameWidth)+(j+(k+1)*frameHeight)*w] = lrRGB[(frameWidth-i-1)+j*frameWidth];
						}
					} else{
						if(((lrRGB[(frameWidth-i-1)+j*frameWidth] & 0xFF000000) >> 24) != 0x00){
							rgb[(i+w-frameWidth)+(j+(k+1)*frameHeight)*w] = lrRGB[(frameWidth-i-1)+j*frameWidth];
						}
					}
				}
			}
		}
		
		// 上下の枠を描画
		int[] udRGB = new int[frameWidth*frameHeight];
		frameImages[2].getRGB(udRGB, 0, frameWidth, 0, 0, frameWidth, frameHeight);
		int udlen = w / frameWidth -2;
		for(int k=0; k < udlen; k++){
			// 下の枠を描画
			for(int i=0; i < frameWidth; i++){
				for(int j=0; j < frameHeight; j++){
					if(alphaColor >= 0){
						if(alphaColor != (udRGB[i+(frameHeight-j-1)*frameWidth] & 0xFFFFFF)){
							rgb[(i+(k+1)*frameWidth)+j*w] = udRGB[i+(frameHeight-j-1)*frameWidth];
						}
					} else{
						if(((udRGB[i+(frameHeight-j-1)*frameWidth] & 0xFF000000) >> 24) != 0x00){
							rgb[(i+(k+1)*frameWidth)+j*w] = udRGB[i+(frameHeight-j-1)*frameWidth];
						}
					}
				}
			}
			// 上の枠を描画
			for(int i=0; i < frameWidth; i++){
				for(int j=0; j < frameHeight; j++){
					if(alphaColor >= 0){
						if(alphaColor != (udRGB[i+j*frameWidth] & 0xFFFFFF)){
							rgb[(i+(k+1)*frameWidth)+(j+h-frameHeight)*w] = udRGB[i+j*frameWidth];
						}
					} else{
						if(((udRGB[i+j*frameWidth] & 0xFF000000) >> 24) != 0x00){
							rgb[(i+(k+1)*frameWidth)+(j+h-frameHeight)*w] = udRGB[i+j*frameWidth];
						}
					}
				}
			}
		}		
	}
	
	/**
	 * ウインドウの背景色のRGB情報の描画<br>
	 * <br>
	 * <p>ウインドウの幅と高さは枠画像のサイズの整数倍に設定してください</p>
	 *
	 * @param rgb 枠のRGB情報を描画<br>
	 * @param w ウインドウの幅
	 * @param h ウインドウの高さ
	 */
	private void createWindowBGRGB(int[] rgb, int w, int h){
		// 背景全体を描画
		for(int i=0; i < rgb.length; i++){
			if(acceptAlpha) rgb[i] = (bgColor & 0xFFFFFF) | (bgAlpha << 24);
			else rgb[i] = bgColor & 0xFFFFFF;
		}
	}
	
	//=================================Setter,Getter==================================//
	
	/**
	 * アルファ処理の許可状態を設定
	 *
	 * @param alpha アルファ処理の許可・不許可
	 */
	public synchronized void acceptAlpha(boolean alpha){
		this.acceptAlpha = alpha;
	}
	/**
	 * 背景の色を設定
	 *
	 * @param color 設定する色
	 */
	public synchronized void setBGColor(int color){
		bgColor = color;
	}
	/**
	 * 背景の透過度を設定
	 *
	 * @param alpha 背景の透過度(半透明 0 ～ 100 透明)
	 */
	public synchronized void setAlpha(int alpha){
		bgAlpha = (int)((100 - alpha)*(0xFF/100));
	}
	/**
	 * 枠画像の透過色を設定(透過PNG非対応の端末で使用)
	 *
	 * @param color 枠画像の透過色(-1で無効)
	 */
	public synchronized void setAlphaColor(int color){
		alphaColor = color;
	}
	/**
	 * アルファ処理の許可状態を取得
	 *
	 * @return アルファ処理の許可状態
	 */
	public synchronized boolean getAlphaPermission(){
		return acceptAlpha;
	}

	//=================================Window Key==================================//
	
	/**
	 * Windowを保持するハッシュのキー
	 */
	class WindowKey{

		/** ウインドウの幅 */
		public int kWidth = 0;
		/** ウインドウの高さ */
		public int kHeight = 0;
		/** 透過処理の許可 */
		public boolean kAcceptAlpha = false;
		/** 背景色 */
		public int kBGColor = 0;
		/** 背景の透過度 */
		public int kBGAlpha = 0;
		
		/**
		 * コンストラクタ
		 */
		public WindowKey(int w, int h){			
			// 初期化			
			this.kWidth = w;
			this.kHeight = h;
			this.kAcceptAlpha = acceptAlpha;
			this.kBGColor = bgColor;
			this.kBGAlpha = bgAlpha;	
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode() {			
			return toString().hashCode();
		}		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		public String toString(){
			return ("w="+ kWidth + "h="+kHeight 
					+ "alpha="+String.valueOf(kAcceptAlpha)
					+ kBGColor + kBGAlpha + " ");
		}
		/* 
		 * (non-Javadoc)		    
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object obj) {			
			if(!(obj instanceof WindowKey))return false;
			WindowKey key = (WindowKey)obj;
			return key.toString().equals(this.toString());
		}
	}
}
