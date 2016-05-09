package gui;

import java.util.Vector;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import request.Key;

import main.Main;

import util.DojaFont;
import util.DojaGraphics;

public class TitleCanvas extends Canvas {

	//================================�萔====================================//

	//���j���[�p�̃v���p�e�B
	/** �^�C�g����ʂ�"�g�b�v���"��\���萔 */
	public static final int TITLE_TOP = 0;
	/** �^�C�g����ʂ�"����̊J�n"��\���萔 */
	public static final int TITLE_MEASUREMENT = 1;
	/** �^�C�g����ʂ�"�ݒ�"��\���萔 */
	public static final int TITLE_SETTING = 2;
	/** �^�C�g����ʂ�"MCBY�ݒ�"��\���萔 */
	public static final int TITLE_SETTING_MCBY = 3;
	/** �^�C�g����ʂ�"�o�b�N���C�g�ݒ�"��\���萔 */
	public static final int TITLE_SETTING_LIGHT = 4;
	/** �^�C�g����ʂ�"�T�E���h�ݒ�"��\���萔 */
	public static final int TITLE_SETTING_SOUND = 5;
	/** �^�C�g����ʂ�"�o�C�u���[�V�����ݒ�"��\���萔 */
	public static final int TITLE_SETTING_VIBRATION = 6;
	/** �^�C�g����ʂ�"�d�����g��"��\���萔 */
	public static final int TITLE_SETTING_POWER = 7;

	//���ʃ��j���[
	/** �߂郁�j���[��\��*/
	public static final String MENU_BACK = "�߂�";
	/** ON/OFF���j���[��\�� */
	public static final String MENU_ON = "ON";
	public static final String MENU_OFF = "OFF";

	//�ŏ��ɕ\������郁�j���[
	/** �v���̊J�n���j���[��\�� */
	public static final String MENU_MESUREMENT = "����̊J�n";
	/** �l�b�g���[�N����O���t��\������ */
	public static final String MENU_OLD_GRAPH = "�ߋ��̃O���t��\��";
	/** �ݒ胁�j���[��\�� */
	public static final String MENU_SETTING = "�ݒ�";
	/** �I�����j���[��\��*/
	public static final String MENU_EXIT = "�I��";
	/** �g�b�v���j���[�Q */
	private static final String[] TOPMENU = {MENU_MESUREMENT,
									         MENU_OLD_GRAPH,MENU_SETTING,MENU_EXIT};

	//�v���̃��j���[
	/** �ʐM�|�[�g����v�����J�n���郁�j���[��\�� */
	public static final String MENU_MESUREMENT_IO = "Bluetooth�Ōv��";
	/** �t�@�C������v�����J�n���郁�j���[��\�� */
	public static final String MENU_MESUREMENT_FILE = "�t�@�C������v��";
	/**�v���̃��j���[�Q */
	private static final String[] MEASUREMENTMENU = {MENU_MESUREMENT_IO,
		                                            MENU_MESUREMENT_FILE,MENU_BACK};

	//�ݒ�ꗗ���j���[
	/** �ō������l�̃��j���[��\��*/
	public static final String MENU_MAX_BP = "�ō������l";
	/** ���O�̕ۑ����j���[��\�� */
	public static final String MENU_ISSAVE_LOG = "���O�̕ۑ�";
	/** MCBY ID */
	public static final String MENU_MCBY_CHANGE = "MCBY�̐؂�ւ�";
	/** �o�b�N���C�g�̐ݒ� */
	public static final String MENU_BACK_LIGHT = "�o�b�N���C�g";
	/** �T�E���h */
	public static final String MENU_SOUND = "�T�E���h";
	/** �o�C�u���[�V���� */
	public static final String MENU_VIBRATION = "�o�C�u���[�V����";
	/** QR�R�[�h�̎擾 */
	public static final String MENU_QR_CODE = "QR�R�[�h�̎擾";
	/** �d�����g�� */
	public static final String MENU_POWER = "�d�����g��";
	/** �ݒ�ꗗ�̃��j���[�Q(��L���j���[���܂Ƃ߂�����) */
	private static final String[] SETTINGMENU = {MENU_MCBY_CHANGE,MENU_POWER,
		                                        MENU_BACK_LIGHT,MENU_SOUND,MENU_VIBRATION,
		                                        MENU_QR_CODE,MENU_BACK};

	//�d�����g���ݒ�̃��j���[
	/** �d�����g���ݒ�̃��j���[(50Hz) */
	public static final String MENU_50HZ = "50Hz";
	/** �d�����g���ݒ�̃��j���[(60Hz) */
	public static final String MENU_60HZ = "60Hz";
	/** �ݒ�ꗗ�̃��j���[�Q(��L���j���[���܂Ƃ߂�����) */
	private static final String[] POWERMENU = {MENU_50HZ,MENU_60HZ};

	//MCBY�̑I�����j���[
	/** MCBY�ݒ�̃��j���[(�}�E�X) */
	public static final String MENU_MCBY1 = "MCBY 1 (�}�E�X)";
	/** MCBY�ݒ�̃��j���[(ZEAL) */
	public static final String MENU_MCBY2 = "MCBY 2 (ZEAL)";
	/** MCBY�ݒ胁�j���[�Q(��L���j���[���܂Ƃ߂�����)�@*/
	public static final String[] MCBYMENU = {MENU_MCBY1,MENU_MCBY2};

	/** �o�b�N���C�g�ݒ胁�j���[�Q */
	public static final String[] BACKLIGHTMENU = {MENU_ON,MENU_OFF};
	/** �T�E���h�ݒ胁�j���[�Q */
	public static final String[] SOUNDMENU = {MENU_ON,MENU_OFF};
	/** �o�C�u���[�V�����ݒ胁�j���[�Q */
	public static final String[] VIBRATIONMENU = {MENU_ON,MENU_OFF};

	//----------------- �摜 -------------------//
	/**�^�C�g����ʂɕ\������摜��URL */
	private static final String TITLE_IMG_URL = (Main.IS_ECLIPSE)?("/res/MCBY_white.gif"):("/MCBY_white.gif") ;
	/** �R�[�i�[�̉摜 */
	private static final String IMG_CORNER = (Main.IS_ECLIPSE)?("/res/menu_waku_01.gif"):("/menu_waku_01.gif");
	/** ���[�E�E�[�̉摜 */
	private static final String IMG_VERTICAL_LINE = (Main.IS_ECLIPSE)?("/res/menu_waku_02.gif"):("/menu_waku_02.gif");
	/** ��[�E���[�̉摜 */
	private static final String IMG_HORIZONTAL_LINE = (Main.IS_ECLIPSE)?("/res/menu_waku_03.gif"):("/menu_waku_03.gif");

	//----------------- �`��֘A -------------------//
	/** �w�i�F */
	private static final int BG_WHITE = 0x00FFFFFF;
	/** �����F */
	private static final int STR_COLOR = 0x00FF66B4;
	/** �t�H�[�J�X����Ă��镶���̐F */
	private static final int FOCUS_STR_COLOR =  0x00FFC0CB;
	/** �t�H���g */
	private static final DojaFont FONT = DojaFont.getFont(DojaFont.SIZE_TINY);
	/** �t�H���g�̃f�B�Z���g */
	private static final int FONT_DECENT = FONT.getDescent();
	/** �t�H���g�̃A�Z���g */
	private static final int FONT_ASCENT = FONT.getAscent();
	/** �t�H���g�̍��� */
	private static final int FONT_HEIGHT = FONT_DECENT + FONT_ASCENT;
	
	/** �E�B���h�E�̒[�̃}�[�W��*/
	private static final int WINDOW_MARGIN = 25;
	/** �_�ŊԊu */
	private static final int BLINK_IINTERVAL = 8;

	//=================================�ϐ�=====================================//

	//-----------------�����A��-------------------//

	/** �E�B���h�E�� */
	private int width;
	/** �E�B���h�E���� */
	private int height;

	//----------------�`��֘A -------------------//

	/** �I�t�O���t�B�b�N�X */
	protected DojaGraphics offGra;
	/** �I�t�C���[�W */
	protected Image offImg;
	/** �^�C�g���ɕ\������摜 */
	private Image titleImg;
	/** �g�摜 */
	private Image[] frameImages = new Image[3];
	private FrmWnd frameWindow;
		
	/**�@����_�ł����邽�߂̃J�E���^ */
	private int blinkCnter = 0;
	/**�@����_�ł����邽�߂̃t���O */
	private boolean blinkflag = true;

	//---------------- ���j���[ -------------------//

	/** �^�C�g����ʂ֕\�����郁�j���[�̑I������Ă���C���f�b�N�X */
	private int menuIndex = 0;
	/** �\�����郁�j���[���ڂ�ێ�����R���N�V���� */
	private Vector menu;
	/** ���݂̃^�C�g�����[�h */
	private int titleMode = TITLE_TOP;

	//=============================����������===================================//

	/**
	 * �R���X�g���N�^
	 */
	public TitleCanvas() {
		//��
		width = getWidth();
		//����
		height = getHeight();

		//�_�u���o�b�t�@�����O�̂��߂̃I�t�O���t�B�b�N�X�C���[�W�̐���
		if (offImg == null) {
			offImg   =Image.createImage(width,height);
			offGra =  new DojaGraphics(offImg.getGraphics(),this);
		}
	    // �t�H���g��ݒ�
	    offGra.setFont(FONT);
	    //�摜�̓ǂݍ���
	    loadImage();
	    //���j���[�p�R���N�V������������
	    menu = new Vector();

	    //�ϐ��̏�����
	    init();
	}

	/**
	 * �ϐ��̏���������
	 */
	public void init(){
		//���j���[��������Ԃ�(�����̓g�b�v���j���[)
		menu.removeAllElements();
		for(int i = 0; i < TOPMENU.length ; i++){
			menu.addElement(TOPMENU[i]);
		}
	}

	/**
	 * �摜�̓ǂݍ��ݏ������s��
	 */
	private void loadImage(){
		try {
			// �^�C�g���摜�̃��[�h
	        titleImg = Image.createImage(TITLE_IMG_URL);

	        //�g	�̉摜�ǂݍ���
	    	frameImages[0] = Image.createImage(IMG_CORNER);
	      	frameImages[1] = Image.createImage(IMG_VERTICAL_LINE);
	      	frameImages[2] = Image.createImage(IMG_HORIZONTAL_LINE);

	      	//�g�E�B���h�E���쐬
	      	frameWindow = new FrmWnd(frameImages,true);

		} catch (Exception e) {
			System.out.println("Read Images Error: " + e.getMessage());
	        System.out.println(e.toString());
	    }
	}

	//================================�`��=====================================//

	/**
	 * �`��(�I�t�O���t�B�b�N�X�𔽉f������)
	 */
	protected void paint(Graphics g) {
		g.drawImage(offImg,0,0,Graphics.LEFT|Graphics.TOP);
	}

	/**
	 * �`�揈�����s��
	 */
	public void draw(){
		offGra.lock();

		//�w�i�h��Ԃ�
		offGra.setColor(BG_WHITE);
		offGra.clearRect(0,0,width,height);

		//�^�C�g����ʕ`��
		drawtitle(offGra);

		offGra.unlock(true);
	}

	/**
	 * �^�C�g����`��
	 * @param g �����_�����O�pGraphics�I�u�W�F�N�g
	 */
	private void drawtitle(DojaGraphics g){
   		//�_�ŃJ�E���^���X�V
   		blinkCnter++;
   		
	    //�����Ԃ̌��Ԃ̑傫��
	    final int margin = 10;

		//�^�C�g���摜��`��
	    g.drawImage(titleImg,(width - titleImg.getWidth())>>1,margin);
	    //�t�H���g�̐ݒ�
	    g.setColor(STR_COLOR);

		//�`�撲���p
		final int charWidth = FONT.stringWidth("��");
        
		//�ő�\����
		final int maxNum = 4;
		//���j���[���ڂ̐�
		final int menuLen = menu.size();
		
		//�E�B���h�E�̍���
		int windowHeight = 0;
		if(menuLen > maxNum){
			//���ڂ������Ƃ���	//��x�ɑS����\�����Ȃ�
			windowHeight = (FONT_HEIGHT + margin) * (maxNum) + margin;					
		}else{
			windowHeight = (FONT_HEIGHT + margin) * menuLen + margin;
		}
		
		// ���j���[�E�B���h�E��\��
		Image img = frameWindow.getWindow(width-(WINDOW_MARGIN<<1),windowHeight);
		offGra.drawImage(img,(width-img.getWidth())>>1,((height)>>1));
		
		//�`��J�n���W
		int y = ((height)>>1) + FONT_HEIGHT + (margin>>1);
		
		// ���j���[���ڂ�`��		
		if(menuLen <= maxNum){
			
			//�\���\�ȍő吔�𒴂��Ă��Ȃ��Ƃ�
			for (int i = 0; i < menuLen  ; i++) {
				
				// �t�H�[�J�X������Ƃ��́A�o�b�N�O���E���h���Ⴄ�F�ŕ`��				
				if (i == menuIndex) {
					offGra.setColor(FOCUS_STR_COLOR);
				}else{
					offGra.setColor(STR_COLOR);
				}
				
				// ���j���[���ڂ�`��
				int x = (width >> 1);
				offGra.drawString(((String)menu.elementAt(i)), x, y);

				y += margin + FONT_HEIGHT;
			}
		}else{
			//�\���\�ȍő吔�𒴂��Ă���Ƃ�
			
			offGra.setColor(STR_COLOR);
			//���}�[�N�̕\��
			if(menuIndex > (maxNum >> 1) && blinkflag ){
				//���}�[�N�ŏ�ɂ܂��\�����鍀�ڂ����邱�Ƃ�����
				offGra.drawString("��",width - (WINDOW_MARGIN >> 1) - charWidth, y );				
			}
						
			int offset = menuIndex - (maxNum >> 1);
			
			if(offset < 0) {
				offset = 0;
			}else if(menuIndex + (maxNum >> 1) > menuLen - 1){
				offset = menuLen - maxNum ;					
			}
			
			for (int i = offset; i < offset + maxNum ; i++) {
				
				// �t�H�[�J�X������Ƃ��́A�o�b�N�O���E���h���Ⴄ�F�ŕ`��
				// �t�H�[�J�X������Ƃ��́A�o�b�N�O���E���h���Ⴄ�F�ŕ`��				
				if (i == menuIndex) {
					offGra.setColor(FOCUS_STR_COLOR);
				}else{
					offGra.setColor(STR_COLOR);
				}

				// ���j���[���ڂ�`��
				int x = (width >> 1);
				offGra.drawString((String)menu.elementAt(i), x, y);

				y += margin + FONT_HEIGHT;
			}
			
						
			//���}�[�N�̕\��
			if( offset < menuLen - maxNum && blinkflag){			
				//���}�[�N�ŉ��ɂ܂��\�����鍀�ڂ����邱�Ƃ�����
				offGra.drawString("��",width - (WINDOW_MARGIN >> 1 ) - charWidth , y - (margin + FONT_HEIGHT));
			}
		}
		
		//�_�ŗp�J�E���^�����Z�b�g
		if(blinkCnter >= BLINK_IINTERVAL){
			blinkCnter = 0;
			blinkflag = !blinkflag;
		}

	}

	//==============================�L�[����===================================//

	/**
	 * �L�[���͂��ꂽ�Ƃ��̏���
	 * @param keyCode �L�[�R�[�h
	 */
	protected void keyReleased(int keyCode) {
		int param = getGameAction(keyCode);
		Key.keyFlag[2] &= ~(1L << param);
	}

	/**
	 * �L�[�������ꂽ�Ƃ��̏���
	 * @param keycode �L�[�R�[�h
	 */
	protected void keyPressed(int keyCode) {
		int param = getGameAction(keyCode);
		Key.keyFlag[0] |= (1L << param);
		Key.keyFlag[2] |= (1L << param);
	}

	//=============================setter,getter=================================//

	/**
	 * ���j���[�̃t�H�[�J�X��ύX
	 * @param index �V�����t�H�[�J�X����C���f�b�N�X
	 */
	public void setSelectedMenuIndex(int index){
		if(menu.size() <= index)index = 0;
		this.menuIndex = index;
	}
	/**
	 * ���j���[�̃t�H�[�J�X��Ԃ�
	 * @return �t�H�[�J�X���Ă��郁�j���[�̃C���f�b�N�X
	 */
	public int getSelectedMenuIndex(){
		return this.menuIndex;
	}

	/**
	 * ���j���[�̍��ڐ���Ԃ�
	 * @return
	 */
	public int getNumOfMenuItem(){
		return menu.size();
	}

	/**
	 * �t�H�[�J�X�̂��郁�j���[��Ԃ�
	 * @return
	 */
	public String getSelectedMenu(){
		return (String)menu.elementAt(menuIndex);
	}

	/**
	 * �^�C�g�����[�h��ύX����
	 * @param mode
	 */
	public void setTitleMode(int mode){
		this.titleMode = mode;
		switch (mode) {
		case TITLE_TOP:              //�g�b�v��ʕύX
			setMenu(TOPMENU);
			break;
		case TITLE_MEASUREMENT:      //�����ʃ��j���[�Q�֕ύX
			setMenu(MEASUREMENTMENU);
			break;
		case TITLE_SETTING:		     //�ݒ胁�j���[�Q�֕ύX
			setMenu(SETTINGMENU);
			break;
		case TITLE_SETTING_LIGHT:    //�o�b�N���C�g�ݒ胁�j���[�֕ύX
			setMenu(BACKLIGHTMENU);
			break;
		case TITLE_SETTING_MCBY:     //�}�N�r�[�I�����j���[��
			setMenu(MCBYMENU);
			break;
		case TITLE_SETTING_SOUND:    //�T�E���h�ݒ胁�j���[��
			setMenu(SOUNDMENU);
			break;
		case TITLE_SETTING_VIBRATION://�o�C�u���[�V�����ݒ胁�j���[��
			setMenu(VIBRATIONMENU);
			break;
		case TITLE_SETTING_POWER:    //�d�����g���ݒ胁�j���[��
			setMenu(POWERMENU);
		}
		menuIndex = 0;
	}

	/**
	 * ���݂̃^�C�g�����[�h�𓾂�
	 */
	public int getTitleMode(){
		return this.titleMode;
	}

	/**
	 * �\�����̃��j���[�Q��ύX����
	 * @param array �V���ɕ\�����郁�j���[�Q
	 */
	private void setMenu(String[] array){
		menu.removeAllElements();
		for(int i = 0; i < array.length ; i++){
			menu.addElement(array[i]);
		}
	}

}
