package calendar.renderer;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import calendar.util.CalendarColor;

import main.Main;

import util.DojaFont;
import util.DojaGraphics;

public class WaitScreenRenderer{
	
	/* �摜�̃p�X */
	private static final String ACTIVE_1     = (Main.IS_ECLIPSE)?("/res/wait_a1.png"):("/wait_a1.png");
	private static final String ACTIVE_2     = (Main.IS_ECLIPSE)?("/res/wait_a2.png"):("/wait_a2.png");
	private static final String ACTIVE_3     = (Main.IS_ECLIPSE)?("/res/wait_a3.png"):("/wait_a3.png");
	private static final String ACTIVE_4     = (Main.IS_ECLIPSE)?("/res/wait_a4.png"):("/wait_a4.png");
	private static final String NON_ACTIVE_1 = (Main.IS_ECLIPSE)?("/res/wait_b1.png"):("/wait_b1.png");
	private static final String NON_ACTIVE_2 = (Main.IS_ECLIPSE)?("/res/wait_b2.png"):("/wait_b2.png");
	private static final String NON_ACTIVE_3 = (Main.IS_ECLIPSE)?("/res/wait_b3.png"):("/wait_b3.png");
	private static final String NON_ACTIVE_4 = (Main.IS_ECLIPSE)?("/res/wait_b4.png"):("/wait_b4.png");
	
	/** ���t�H���g */
	private static final DojaFont sFONT = DojaFont.getFont(DojaFont.SIZE_SMALL);

	/** ��ʕ�  */
	private int width;
	/** ��ʍ��� */
	private int height;
	
	/* �摜 */
	private Image[] rounds;
	private Image backGround;
	private Image menuImg;
	private Image arrowImg;
	
	private int roundsIndex = 0;
	
	/** �A�j���[�V�����̎��s�t���O */
	private boolean runFlag = false;
	/** ���j���[�̃t���O */
	private boolean menuFlag = false;			
	/** ���j���[�̃C���f�b�N�X*/	 
	private int menuIndex = 0;		
	/** �A�j���[�V�����p�X���b�h */
	private RendererThread animation = null;
	
	/**
	 * �R���X�g���N�^
	 * @param width
	 * @param height
	 */
	public WaitScreenRenderer(int width,int height){
		/* ���� */
		this.height = height;
		/* �� */
		this.width  = width;		
		/* �摜�̍쐬 */
		createImages();
	}
	
	/**
	 * �`�揈�����s��
	 * @param g
	 */
	public void draw(DojaGraphics g){
		if(!runFlag) return;
		
		/* �w�i�̕`��*/
		g.drawImage(backGround, 0, 0 );
		g.drawImage(rounds[roundsIndex],
				  ((width-rounds[roundsIndex].getWidth())>>1),
				  ((height-rounds[roundsIndex].getHeight())>>1));
		
		if(menuFlag){
			drawMenu(g);
		}
	}
	
	/**
	 * �A�j���[�V�����̊J�n
	 */
	public void action(){	
		if(runFlag)return;		
		runFlag = true;		
		animation = new RendererThread();
		animation.start();
	}
	
	/**
	 * �A�j���[�V�����̒�~
	 */
	public void stop(){
		runFlag = false;
		
		if(animation != null){			
			animation = null;
		}
		
		menuFlag = false;		
	}
	
	/**
	 * ���j���[���I����
	 */
	public void menuOn(){
		menuFlag = true;
	}	
	/**
	 * ���j���[���I�t��
	 */
	public void menuOff(){
		menuFlag = false;
	}	
	/**
	 * @return ���j���[���I�����I�t��
	 */
	public boolean isMenuFlag() {
		return menuFlag;
	}
	
	/**
	 * ���
	 */
	public void up(){
		if(!menuFlag){
			return;
		}
		//���j���[���I���̂Ƃ�
		menuIndex--;
		if(menuIndex < 0) menuIndex = 1;
	}
	
	/**
	 * ����
	 */
	public void down(){
		if(!menuFlag){
			return;
		}	
		//���j���[���I���̂Ƃ�
		menuIndex = (menuIndex + 1) % 2;
	}
	
	/**
	 * �C���f�b�N�X�𓾂�
	 * @return
	 */
	public int getIndex(){
		return menuIndex;
	}	
	
	/**
	 * �摜�̐���
	 */
	private void createImages(){		
		createRounds();
		createBGImage();
		createMenu();
		createArrow();
	}
	
	/**
	 * �w�i�摜�̍쐬
	 */
	private void createBGImage(){		
		Image tmpImg = Image.createImage(width,height);
		Graphics tg = tmpImg.getGraphics();
		tg.setColor(CalendarColor.COLOR_GRAY);
		tg.fillRect(0,0,width,height);
		int[] rgb = new int[width*height];
		tmpImg.getRGB(rgb, 0, width, 0, 0, width, height);
		for(int i=0; i < rgb.length; i++){
			rgb[i] = (rgb[i] & 0xFFFFFF) | 0x8F << 24;
		}
		backGround = Image.createRGBImage(rgb, width, height, true);
	}
	
	/**
	 * �摜�̍쐬
	 */
	private void createRounds(){		
		try{
			rounds = new Image[8];
			
			Image[] atImg = new Image[4];
			atImg[0] = Image.createImage(ACTIVE_1);
			atImg[1] = Image.createImage(ACTIVE_2);
			atImg[2] = Image.createImage(ACTIVE_3);
			atImg[3] = Image.createImage(ACTIVE_4);
			
			Image[] natImg = new Image[4];
			natImg[0] = Image.createImage(NON_ACTIVE_1);
			natImg[1] = Image.createImage(NON_ACTIVE_2);
			natImg[2] = Image.createImage(NON_ACTIVE_3);
			natImg[3] = Image.createImage(NON_ACTIVE_4);
			
			int iw = atImg[0].getWidth();
			
			Image[] backImg = new Image[8];
			
			int[] posX = new int[8];
			posX[0] = iw;
			posX[1] = iw*2;
			posX[2] = iw*2;
			posX[3] = iw*2;
			posX[4] = iw;
			posX[5] = 0;
			posX[6] = 0;
			posX[7] = 0;
			
			int[] posY = new int[8];
			posY[0] = 0;
			posY[1] = 0;
			posY[2] = iw;
			posY[3] = iw*2;
			posY[4] = iw*2;
			posY[5] = iw*2;
			posY[6] = iw;
			posY[7] = 0;
			
			for(int i=0; i < 8; i++){				
				backImg[i] = Image.createImage(iw*3,iw*3);
				Graphics bg = backImg[i].getGraphics();
				
				bg.setColor(CalendarColor.COLOR_BLACK);
				bg.fillRect(0,0,iw*3,iw*3);
				
				for(int j=0; j < 8; j++){
					if(i == j){
						bg.drawImage(atImg[j%4], posX[j], posY[j], Graphics.TOP|Graphics.LEFT);
					} else{
						bg.drawImage(natImg[j%4], posX[j], posY[j], Graphics.TOP|Graphics.LEFT);
					}
				}
				
				int[] rgb = new int[iw*iw*9];
				backImg[i].getRGB(rgb, 0, iw*3, 0, 0, iw*3, iw*3);
				int ndColor = rgb[0];				
				for(int j=0; j < rgb.length; j++){
					if(rgb[j] == ndColor){
						rgb[j] = (rgb[j] & 0xFFFFFF) | 0x00 << 24;					
					}
				}
				rounds[i] = Image.createRGBImage(rgb, iw*3, iw*3, true);
			}
		
		} catch(Exception e){
			e.printStackTrace();
		}
	}/* End of ecreateRounds() */
	
	/**
	 * ���j���[�̍쐬
	 */
	private void createMenu(){
		
		final String[] items = {"�͂�", "������"};
		final Font FONT = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
		
		int margin = 5;
		
		int iw = (FONT.stringWidth(items[1]))<<1;
		int ih = (margin*3) + (FONT.getHeight()<<1);
		
		Image tmpMenuImg = Image.createImage(iw+3,ih+3);
		Graphics mg = tmpMenuImg.getGraphics();
		
		mg.setColor(CalendarColor.COLOR_BLACK);
		mg.fillRect(0, 0, iw+3, ih+3);
		
		mg.setColor(CalendarColor.COLOR_WHITE);
		mg.fillRoundRect(0, 0, iw, ih, 10, 10);
		
		mg.setColor(CalendarColor.COLOR_ORANGE);
		mg.drawRoundRect(0, 0, iw, ih, 10, 10);
		mg.drawRoundRect(1, 0, iw, ih, 10, 10);
		mg.drawRoundRect(0, 1, iw, ih, 10, 10);
		mg.drawRoundRect(1, 1, iw, ih, 10, 10);
		mg.drawRoundRect(1, 1, iw, ih, 10, 10);
		mg.drawRoundRect(2, 1, iw, ih, 10, 10);
		mg.drawRoundRect(1, 2, iw, ih, 10, 10);
		mg.drawRoundRect(2, 2, iw, ih, 10, 10);
		
		mg.setFont(FONT);
		for(int i=0; i < 2; i++){
			mg.drawString(items[i], ((iw+3-FONT.stringWidth(items[0]))>>1)
					              , ((ih-(FONT.getHeight()<<1)-margin)>>1)
					                + i*(FONT.getHeight()+margin)
					              , Graphics.TOP|Graphics.LEFT);
		}
		
		int[] rgb = new int[(iw+3)*(ih+3)];
		tmpMenuImg.getRGB(rgb, 0, iw+3, 0, 0, iw+3, ih+3);
		int ndColor = rgb[0];
		for(int i=0; i < rgb.length; i++){
			if(rgb[i] == ndColor) rgb[i] = (rgb[i] & 0xFFFFFF) | 0x00 << 24;
		}
		menuImg = Image.createRGBImage(rgb, iw+3, ih+3, true);
		
	}/* End of createRounds() */
	
	/**
	 * �����쐬����
	 */
	public void createArrow(){
		
		Image tmpArrowImg = Image.createImage(20,20);
		Graphics ag = tmpArrowImg.getGraphics();
		
		int iw = tmpArrowImg.getWidth();
		int ih = tmpArrowImg.getHeight();
		
		int x1 = 0;
		int y1 = 0;
		int x2 = 0;
		int y2 = ih;
		int x3 = iw;
		int y3 = ih / 2;
		
		ag.setColor(CalendarColor.COLOR_LIGHT_GREEN);
		ag.fillTriangle(x1,y1,x2,y2,x3,y3);
		
		int[] rgb = new int[20*20];
		tmpArrowImg.getRGB(rgb, 0, 20, 0, 0, 20, 20);
		int ndColor = rgb[rgb.length-1];
		for(int i=0; i < rgb.length; i++){
			if(rgb[i] == ndColor) rgb[i] = (rgb[i] & 0xFFFFFF) | 0x00 << 24;
		}
		arrowImg = Image.createRGBImage(rgb, 20, 20, true);
	}/* End of createArrow() */
	
	/**
	 * ���j���[��`�悷��
	 * @param g
	 */
	private void drawMenu(DojaGraphics g){		
		final String title = "���f���܂����H";		
		int margin = 5;
		
		g.setColor(CalendarColor.COLOR_WHITE);
		g.setFont(sFONT);
		g.drawString(title,(width>>1)
				          ,((height-menuImg.getHeight())>>1) 
				          -(sFONT.getAscent()+sFONT.getDescent())-margin);
		
		int x = ((width-menuImg.getWidth())>>1);
		int y = ((height-menuImg.getHeight())>>1);
		g.drawImage(menuImg, x, y);
		x = ((width-menuImg.getWidth())>>1)   - margin;
		y = ((height-menuImg.getHeight())>>1) + margin + menuIndex*(margin+(sFONT.getAscent()+sFONT.getDescent()));
		g.drawImage(arrowImg,x, y);
	}/* End of drawMenu()*/
	
	/**
	 * �A�j���[�V�����N���X
	 */
	class RendererThread extends Thread{

		public void run(){						
			long start = System.currentTimeMillis();
			
			while(runFlag){									
				roundsIndex = (roundsIndex+1) % rounds.length;
				
				//�X���b�h�̒�~
				long end = System.currentTimeMillis();
				long ptime = end - start;
				if (ptime < 5) {
					try {
						Thread.sleep(5 - ptime);					
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				start = System.currentTimeMillis();				
			}/* End of while() */			
			
			//�A�j���[�V�����̏I��

		}/* End of run() */		
	}/* End of RendererThread class */
	
}
