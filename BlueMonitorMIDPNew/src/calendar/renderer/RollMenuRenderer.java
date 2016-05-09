package calendar.renderer;

import java.io.IOException;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import request.Request;
import request.RequestQueue;

import util.DojaGraphics;

import main.Main;


public class RollMenuRenderer{
	
	//TODO: active() getIndex() left() inactive() right() 
	
	//================================�萔====================================//
	
	//�F
	private static final int COLOR_BLACK = 0x000000;
	private static final int COLOR_LIGHT_RED = 0xFF6666;

	//�A�C�R���摜
	/** ���A�C�R�� */
	private static final String ICON_1 = (Main.IS_ECLIPSE)?("/res/monthicon.gif"):("/monthicon.gif");
	/** �N�A�C�R�� */
	private static final String ICON_2 = (Main.IS_ECLIPSE)?("/res/yearicon.gif"):("/yearicon.gif");
	/** ���A�C�R�� */
	private static final String ICON_3 = (Main.IS_ECLIPSE)?("/res/dayicon.gif"):("/dayicon.gif");
	/** �T�A�C�R�� */
	private static final String ICON_4 = (Main.IS_ECLIPSE)?("/res/weekicon.gif"):("/weekicon.gif");	
	/** �Z���N�^ */
	private static final String ICON_SELECTOR = (Main.IS_ECLIPSE)?("/res/iconselector.gif"):("/iconselector.gif");

	//���̒萔
	/** �� */
	public static final int DAY = 0;
	/** �T */
	public static final int WEEK = 1;
	/** �� */
	public static final int MONTH = 2;
	/** �O���� */
	public static final int YEAR = 3;
	
	//���,����
	/** �A�N�e�B�u */
	public static final int ACTIVE = 0;
	/** ��A�N�e�B�u */
	public static final int INACTIVE = 1;
	/** ���� */
	public static final int LEFT = 2;
	/** �E�� */
	public static final int RIGHT = 3;		

	//================================�萔====================================//
	/** �� */
	private int width = 0;
	/** ���� */
	private int height = 0;

	/** �A�C�R���摜��ێ�����z�� */
	private Image[] icon = null;
	private Image iconMap = null;
	
	private int margin = 5;

	/** ���j���[�̃C���f�b�N�X */
	private int index = 0;

	/** �s�ʒu*/
	private int rowPosition = 1000;
	/** ��ʒu*/
	private int colPosition = 0;
	/** �t�H�[�J�X�����邩�ǂ��� */
	private boolean focus = false;
	
	/** �Z���N�^�̉摜*/
	private Image selectorImg;
	
	/** �A�j���[�V���������s�����ǂ����̃t���O */
	private boolean animationFlag = false;
	/** �A�N�V������ێ�����L���[ */
	private RequestQueue queue;
	
	private int state = INACTIVE;

	//================================�֐�====================================//
	/**
	 * �R���X�g���N�^
	 * @param width ��
	 * @param height�@����
	 */
	public RollMenuRenderer(int width,int height){
		this.width = width;
		this.height = height;

		//�摜�̓ǂݍ���
		loadImages();
		createIconMap();
		
		queue = new RequestQueue(); 
	}

	/**
	 * ���j���[���A�N�e�B�u���ǂ���
	 * @return
	 */
	public boolean isActive(){
		if(state == ACTIVE){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * �`�揈��
	 * @param g Graphics�I�u�W�F�N�g
	 */
	public void draw(DojaGraphics g){
		//�A�C�R���̕`��
		drawIcon(g);
		//�Z���N�^�̕`��
		drawSelector(g);
	}

	/**
	 * �摜�̓ǂݍ���
	 */
	private void loadImages(){
		try{
			icon = new Image[4];
			icon[0] = Image.createImage(ICON_1);
			icon[1] = Image.createImage(ICON_2);
			icon[2] = Image.createImage(ICON_3);
			icon[3] = Image.createImage(ICON_4);
			selectorImg = Image.createImage(ICON_SELECTOR);
		} catch(IOException e){
			e.printStackTrace();
		}
	}
	
	/**
	 * �C���f�b�N�X�𓾂�
	 * @return
	 */
	public int getIndex() {
		return index;
	}
	
	/**
	 * �C���f�b�N�X��ύX����
	 * @param index 
	 */
	public void setIndex(int index){
		this.index = index;
	}
	
	/**
	 * �A�C�R���̃��[�u
	 * @param action
	 */
	public void action(int action){
		if(animationFlag){
			return;
		}
		animationFlag = true;
		if(action == LEFT){
			index = (index + 1) % icon.length;
		}else if(action == RIGHT){
			index = (index + icon.length - 1) % icon.length;
		}	
		new RendererThread(action).start();
	}

	/**
	 * �A�N�e�B�u(�\��)��Ԃ�
	 */
	private void active() {
		colPosition += 50;
		if (colPosition >= 1000){
			colPosition = 1000;						
		}
	}
	
	/**
	 * ��A�N�e�B�u(��\��)��Ԃ�
	 */
	private void inactive() {
		colPosition -= 50;
		if (colPosition <= 0){
			colPosition = 0;						
		}
	}

	/**
	 * ����
	 */
	public void left() {
		rowPosition += 50;
		if (rowPosition >= icon.length * 1000){
			rowPosition = 0;
		}		
	}

	/**
	 * �E��
	 */
	public void right() {
		rowPosition -= 50;
		if (rowPosition < 0){
			rowPosition = icon.length * 1000 - 50;
			System.out.println("test");
		}
	}

	/**
	 * �A�C�R���������Ő�������摜�̏�ɕ\������ ���̉摜�𐶐�
	 */
	private void createIconMap() {
		// �A�C�R���̕�
		int mw = icon[0].getWidth();
		// �A�C�R���̍���
		int mh = icon[0].getHeight();

		// ��������摜�̍����ƕ�
		int imWidth = mw * icon.length + margin * 3;
		int imHeight = mh;

		// �摜�̐�������
		Image tmpImg = Image.createImage(imWidth, imHeight);
		Graphics tg = tmpImg.getGraphics();

		tg.setColor(COLOR_BLACK);
		tg.fillRect(0, 0, imWidth, imHeight);

		for (int i = 0; i < icon.length; i++) {
			tg.drawImage(icon[i], i * (mw + margin), 0, Graphics.TOP|Graphics.LEFT);
		}

		int[] rgb = new int[imWidth * imHeight];
		tmpImg.getRGB(rgb, 0, imWidth, 0, 0, imWidth, imHeight);
		int ndColor = rgb[mw + margin / 2];
		for (int i = 0; i < rgb.length; i++) {
			if (rgb[i] == ndColor)
				rgb[i] = (rgb[i] & 0xFFFFFF) | (0x00 << 24);
		}
		iconMap = Image.createRGBImage(rgb, imWidth, imHeight, true);
	}

	/**
	 * �A�C�R���̕`��
	 * @param g Graphics�I�u�W�F�N�g
	 */
	private void drawIcon(DojaGraphics g) {
		//�A�C�R���̕�
		int mw = icon[0].getWidth();
		//�A�C�R���̕�
		int mh = icon[0].getHeight();

		//x���W(�^�񒆂���A�C�R���̕��̔���+�]�����������l)
		int x = (int) ((width>>1) - (mw * 1.5 + margin));
		//y���W
		int y = height;

		double rowPos = (double) (rowPosition) / 1000;
		double colPos = (double) (colPosition) / 1000;

		//x,��������A�C�R���̍���+�]�����������l,�A�C�R��3�̕��ƍ��E�̗]�����A�A�C�R���̍���+�]��
		g.setClip(x,y-mh-margin,(mw*3+margin*2),mh+margin);
		g.drawImage(iconMap, x-(int)(rowPos*(mw+margin)), y-(int)(colPos*(mh+margin)));
		if ((rowPos + 3) > icon.length) {
			g.drawImage(iconMap, x - (int) (rowPos * (mw + margin))
					+ iconMap.getWidth() + margin,
					y - (int) (colPos * (mh + margin)));
		}
		g.setClip(0, 0, width, height);
	}

	/**
	 * �Z���N�^��`��
	 * @param g Graphics�I�u�W�F�N�g
	 */
	private void drawSelector(DojaGraphics g) {

		if (!focus)return;

		int mw = icon[0].getWidth();
		int mh = icon[0].getHeight();

		double colPos = (double) (colPosition) / 1000;

		g.setColor(COLOR_LIGHT_RED);
		g.drawImage(selectorImg,((width-mw)>>1),height-(int)(colPos*(mh+margin)));
	}
	
	/**
	 * �t�H�[�J�X�̕ύX
	 * @param focus true:�t�H�[�J�X��������,false:�t�H�[�J�X���Ȃ����
	 */
	public void setFocus(boolean focus) {
		this.focus = focus;
	}
	
	/**
	 * �A�C�R���𓮂����X���b�h 
	 *
	 */
	public class RendererThread extends Thread {
		
		/** ���� */		
		private int action = -1;
		/** ���[�v�̃J�E���^ */
		private int cnter = 0;
		/** �ő僋�[�v�� */
		private static final int MAX_LOOP = 20;
		
		/**
		 * �R���X�g���N�^
		 * @param ����
		 */
		public RendererThread(int action) {
			this.action = action;
		}
		
		public void run() {
			long start = System.currentTimeMillis();
			
			while(cnter < MAX_LOOP){
								
				//�A�C�R���𓮂���
				switch (action) {
				case ACTIVE:
					active();
					break;
				case INACTIVE:
					inactive();
					break;
				case LEFT:
					left();
					break;
				case RIGHT:
					right();
					break;
				}
				
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
				
				cnter++;
			}//End of while()
									
			//�A�j���[�V�����̏I��
			if(action== ACTIVE){
				state = ACTIVE;
			}else{
				start = INACTIVE;
			}

			//�Ȃ�������A�j���[�V�������I��
			animationFlag = false;
		}
	}
}