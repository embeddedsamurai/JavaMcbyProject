package gui;


import java.util.Hashtable;
import javax.microedition.lcdui.Image;

public class FrmWnd {
	
	//  �F	
	/** �D�F */
	private static final int COLOR_GRAY = 0xDCDCDC;
	
	/** �g�摜�̐� */
	private static final int NUM_OF_FRAME_IMG = 3;	
	/** �g�摜 */
	private Image frameImages[] = new Image[NUM_OF_FRAME_IMG];
	/** �g�摜�̕� */
	private int frameWidth;
	/** �g�摜�̍��� */
	private int frameHeight;
	
	/** �w�i�F */
	private int bgColor = COLOR_GRAY;
	/** �w�i�̓��ߓx */
	private int bgAlpha = 0xCF;

	/** ���ߏ����̋��� */
	private boolean acceptAlpha = true;	
	/**
	 * �g�摜�̓��ߐF�̎w��<br>
	 * <br>
	 * <p>����PNG��Ή��̒[���Ŏg�p(-1�Ŗ���)</p>
	 */
	private int alphaColor = -1;
	
	/** �쐬�����E�C���h�E��ێ� */	
	private Hashtable windowList  = new Hashtable();
	
	/** �g�Ȃ����A���肩*/	 
	private boolean enableFrm = false;
	
	/**
	 * �g�Ȃ��E�B���h�E������Ƃ�
	 * �R���X�g���N�^ 
	 */
	public FrmWnd(boolean alpha){
		this.acceptAlpha = alpha;		
		enableFrm = false;
	}
	
	/**
	 * �R���X�g���N�^
	 * @param frameImages�@�g�摜�̔z��
	 * @param alpha�@�������邩�ǂ��� 
	 * 
	 * @throws Exception
	 */
	public FrmWnd(Image[] frameImages,boolean alpha) throws Exception{
		// �g�C���[�W�̐����Ԉ���Ă���ꍇ
		if(frameImages.length != NUM_OF_FRAME_IMG){
			String msg = "Image Number is not correct.";
			System.out.println(msg);		
			new Exception(msg);
		}		
		// �g�C���[�W�̃R�s�[
		this.frameImages = frameImages;		
		// �g�C���[�W�̃T�C�Y���擾
		frameWidth = frameImages[0].getWidth();
		frameHeight = frameImages[0].getHeight();		
		// ���ߏ�����ON/OFF
		this.acceptAlpha = alpha;
		
		enableFrm = true;
	}
	
	/**
	 * ���������E�C���h�E�̕`��<br>
	 * <br>
	 * <p>�E�C���h�E�̕��ƍ����͘g�摜�̃T�C�Y�̐����{�ɐݒ肵�Ă�������</p>
	 * 
	 * @param w �E�C���h�E�̕�
	 * @param h �E�C���h�E�̍���
	 * 
	 * @param �E�B���h�E�g�̉摜 
	 */
	public synchronized Image getWindow(int w,int h){		
		// �E�C���h�E�̃T�C�Y��␳
		if(enableFrm){
			if(w%16 != 0) w -= w%16;
			if(h%16 != 0) h -= h%16;			
		}		
		//�E�B���h�E�̃L�[
		WindowKey key = new WindowKey(w,h);		
		if(windowList.containsKey(key)){      // �E�C���h�E���ė��p�ł���ꍇ			
			//�E�B���h�E�摜�̎擾						
			return (Image)windowList.get(key);			
		}else{                      			
			// �E�C���h�E���܂��쐬���Ă��Ȃ��ꍇ
			//�F�������z��
			int[] rgb = new int[w*h];
			//�w�i�̍쐬
			createWindowBGRGB(rgb,w,h);
			//�g�̍쐬
			if(enableFrm)createWindowFrameRGB(rgb,w,h);
			//RGB�񂩂�C���[�W���쐬			
			Image img = Image.createRGBImage(rgb,w,h,acceptAlpha);			
			//�E�C���h�E�̓o�^
			windowList.put(key,img);
			//�쐬�����E�B���h�E�摜��Ԃ�
			return img;
		}			
	}
	
	//================================�F����̍쐬==================================//
	
	/**
	 * �g��RGB����`��<br>
	 * <br>
	 * <p>�E�C���h�E�̕��ƍ����͘g�摜�̃T�C�Y�̐����{�ɐݒ肵�Ă�������</p>
	 *
	 * @param rgb RGB�����i�[����z��
	 * @param w �g�̕�
	 * @param h �g�̍���
	 */
	private void createWindowFrameRGB(int[] rgb, int w, int h){
		
		// �g�̊p�̕`��
		int[] cRGB = new int[frameWidth*frameHeight];
		frameImages[0].getRGB(cRGB, 0, frameWidth, 0, 0, frameWidth, frameHeight);
		// ����̕`��
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
		// �E��̕`��
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
		// �E���̕`��
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
		// �����̕`��
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
		
		// ����̊p�̊O���̓��ߏ���
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
		// �E��̊p�̊O���̓��ߏ���
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
		// �E���̊p�̊O���̓��ߏ���
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
		// �����̊p�̊O���̓��ߏ���
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
		
		// ���E�̘g��`��
		int[] lrRGB = new int[frameWidth*frameHeight];
		frameImages[1].getRGB(lrRGB, 0, frameWidth, 0, 0, frameWidth, frameHeight);
		int lrlen = h / frameHeight -2;
		for(int k=0; k < lrlen; k++){
			// ���̘g��`��
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
			// �E�̘g��`��
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
		
		// �㉺�̘g��`��
		int[] udRGB = new int[frameWidth*frameHeight];
		frameImages[2].getRGB(udRGB, 0, frameWidth, 0, 0, frameWidth, frameHeight);
		int udlen = w / frameWidth -2;
		for(int k=0; k < udlen; k++){
			// ���̘g��`��
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
			// ��̘g��`��
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
	 * �E�C���h�E�̔w�i�F��RGB���̕`��<br>
	 * <br>
	 * <p>�E�C���h�E�̕��ƍ����͘g�摜�̃T�C�Y�̐����{�ɐݒ肵�Ă�������</p>
	 *
	 * @param rgb �g��RGB����`��<br>
	 * @param w �E�C���h�E�̕�
	 * @param h �E�C���h�E�̍���
	 */
	private void createWindowBGRGB(int[] rgb, int w, int h){
		// �w�i�S�̂�`��
		for(int i=0; i < rgb.length; i++){
			if(acceptAlpha) rgb[i] = (bgColor & 0xFFFFFF) | (bgAlpha << 24);
			else rgb[i] = bgColor & 0xFFFFFF;
		}
	}
	
	//=================================Setter,Getter==================================//
	
	/**
	 * �A���t�@�����̋���Ԃ�ݒ�
	 *
	 * @param alpha �A���t�@�����̋��E�s����
	 */
	public synchronized void acceptAlpha(boolean alpha){
		this.acceptAlpha = alpha;
	}
	/**
	 * �w�i�̐F��ݒ�
	 *
	 * @param color �ݒ肷��F
	 */
	public synchronized void setBGColor(int color){
		bgColor = color;
	}
	/**
	 * �w�i�̓��ߓx��ݒ�
	 *
	 * @param alpha �w�i�̓��ߓx(������ 0 �` 100 ����)
	 */
	public synchronized void setAlpha(int alpha){
		bgAlpha = (int)((100 - alpha)*(0xFF/100));
	}
	/**
	 * �g�摜�̓��ߐF��ݒ�(����PNG��Ή��̒[���Ŏg�p)
	 *
	 * @param color �g�摜�̓��ߐF(-1�Ŗ���)
	 */
	public synchronized void setAlphaColor(int color){
		alphaColor = color;
	}
	/**
	 * �A���t�@�����̋���Ԃ��擾
	 *
	 * @return �A���t�@�����̋����
	 */
	public synchronized boolean getAlphaPermission(){
		return acceptAlpha;
	}

	//=================================Window Key==================================//
	
	/**
	 * Window��ێ�����n�b�V���̃L�[
	 */
	class WindowKey{

		/** �E�C���h�E�̕� */
		public int kWidth = 0;
		/** �E�C���h�E�̍��� */
		public int kHeight = 0;
		/** ���ߏ����̋��� */
		public boolean kAcceptAlpha = false;
		/** �w�i�F */
		public int kBGColor = 0;
		/** �w�i�̓��ߓx */
		public int kBGAlpha = 0;
		
		/**
		 * �R���X�g���N�^
		 */
		public WindowKey(int w, int h){			
			// ������			
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
