package gui;


import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.midlet.MIDlet;

import request.Key;

import util.DojaFont;
import util.DojaGraphics;

public class BluetoothCanvas extends Canvas {
	
	//NOTICE
	//DojaFont�N���X�̗̂��݂ŁADojaFont.drawString(string,x,y)
	//����Ƃ��Ɉ����Ƃ��Ďw�肷��x���W�͕`��J�n�̍��W�ł͂Ȃ��āA������̒�����������W�ł���B

	//================================�萔====================================//
	//------- �F�֘A  --------//
	/** �w�i�F */
	private static final int BG_COLOR = 0x0000FFFF;
	/** �����F */
	private static final int STR_COLOR = 0x00000000;
	/** �E�B���h�E�F */
	private static final int WND_COLOR = 0x00FFFFFF;
	/** �t�H�[�J�X�F */
	private static final int FCS_COLOR = 0x00FF66B4;
	
	//------- �t�H���g�֘A   --------//
	/** �t�H���g */
	private static final DojaFont FONT = DojaFont.getFont(DojaFont.SIZE_TINY);
	/** �t�H���g�̃f�B�Z���g */
	private static final int FONT_DECENT = FONT.getDescent();
	/** �t�H���g�̃A�Z���g */
	private static final int FONT_ASCENT = FONT.getAscent();
	/** �t�H���g�̍��� */
	private static final int FONT_HEIGHT = FONT_ASCENT + FONT_DECENT;
	
	private static final int BLINK_IINTERVAL = 12;
		
	//------- ���ʂ̕`��p�ϐ� --------//
	/** �I�t�O���t�B�b�N�X */
	protected DojaGraphics offGra;
	/** �I�t�C���[�W */
	protected Image offImg;

	/** �E�B���h�E�� */
	private int width ;
	/** �E�B���h�E���� */
	private int height;
	
	/** MIDLET�I�u�W�F�N�g�ւ̎Q�� */
	private MIDlet midlet;
	
	/** �E�B���h�E�g */
	private FrmWnd frmWnd;
	
	/**�@����_�ł����邽�߂̃J�E���^ */
	private int blinkCnter = 0;
	/**�@����_�ł����邽�߂̃t���O */
	private boolean blinkflag = true;

	/**
	 * �R���X�g���N�^
	 * @param midlet midlet�I�u�W�F�N�g�ւ̎Q��
	 */
	public BluetoothCanvas(MIDlet midlet) {
		this.midlet = midlet;
		//��
		width = getWidth();
		//����
		height = getHeight();

		//�_�u���o�b�t�@�����O�̂��߂̃I�t�O���t�B�b�N�X�C���[�W�̐���
		if (offImg == null) {
			offImg   =Image.createImage(width,height);
			offGra =  new DojaGraphics(offImg.getGraphics(),this);
		}
		//�t�H���g���O���t�B�b�N�X�I�u�W�F�N�g�ɓK�p
		offGra.setFont(FONT);
		
		//�E�B���h�E�g
		frmWnd = new FrmWnd(true);
	}
	
	/**
	 * �`�悷��
	 * @param g �O���t�B�b�N�X�I�u�W�F�N�g
	 */
	protected void paint(Graphics g) {
		g.drawImage(offImg,0,0,Graphics.LEFT|Graphics.TOP);
	}
	
	/**
	 * �\�����郁�b�Z�[�W��`�悷��
	 * 
	 * @param message �`�悷�郁�b�Z�[�W
	 */
	public void drawMessage(String[] message){
		offGra.lock();
   		
		//--------------�w�i�̕`��---------------//
   		//�w�i�h��Ԃ�
   		offGra.setColor(BG_COLOR);
   		offGra.clearRect(0,0,width,height);
   		
   		//--------------���C���̕`��---------------//
   		//���b�Z�[�W�̕`��
   		offGra.setColor(STR_COLOR);

   		
		//�E�B���h�E�̍���
   		final int margin = 3;
		final int windowHeight = (FONT_HEIGHT + margin) * message.length + margin;
		
		// ���j���[�E�B���h�E��\��
		frmWnd.setBGColor(WND_COLOR);
		Image img = frmWnd.getWindow(width-(margin<<1),windowHeight);
		offGra.drawImage(img,(width-img.getWidth())>>1,(height-img.getHeight())>>1);
		
		//�`��J�n���W
		int y = ((height-img.getHeight())>>1) + FONT_HEIGHT;
		
		
		for (int i = 0; i < message.length ; i++) {

				// ���j���[���ڂ�`��
				offGra.setColor(STR_COLOR);
				int x = (width >> 1);
				offGra.drawString(message[i], x, y);

				y += margin + FONT_HEIGHT;
		}	

   		offGra.unlock(true);
	}
	
	/**
	 * �f�o�C�X�������̓T�[�r�X�̈ꗗ�\����`�悷��
	 * 
	 * @param message �\�����郁�b�Z�[�W
	 * @param menu �`�悷��f�o�C�X/�T�[�r�X�����z��
	 * @param ���j���[�̃C���f�b�N�X
	 */
	public void drawViewList(String message,String[] menu,int menuIndex){
		offGra.lock();
		
		//--------------�w�i�̕`��---------------//
   		//�w�i�h��Ԃ�
   		offGra.setColor(BG_COLOR);
   		offGra.clearRect(0,0,width,height);
   		
   		//--------------���C���̕`��---------------//
   		//�ꗗ�\�����郊�X�g�̕`��
   		offGra.setColor(STR_COLOR);
   		//�_�ŃJ�E���^���X�V
   		blinkCnter++;
   		   		
   		//�����̊Ԋu
		final int margin = 3;
		//�E�B���h�E�̍���
		int windowHeight = (FONT_HEIGHT + margin) + margin;
		
		//���b�Z�[�W��\��
		frmWnd.setBGColor(WND_COLOR);
		Image img = frmWnd.getWindow(width-(margin<<1),windowHeight);
		offGra.drawImage(img,(width-img.getWidth())>>1,((FONT_HEIGHT-img.getHeight())>>1) +10);
		int y = ((FONT_HEIGHT-img.getHeight())>>1) + FONT_HEIGHT + 10;
		offGra.drawString(message,width>>1, y);
   		   		
		//�ő�\����
		final int maxNum = 4;
		//�`�撲���p
		final int charWidth = FONT.stringWidth("��");
		
		//�E�B���h�E�̍���
		
		if(menu.length > maxNum){
			//���ڂ������Ƃ���	//��x�ɑS����\�����Ȃ�
			windowHeight = (FONT_HEIGHT + margin) * (maxNum) + margin;					
		}else{
			windowHeight = (FONT_HEIGHT + margin) * menu.length + margin;
		}
		
		// ���j���[�E�B���h�E��\��
		frmWnd.setBGColor(WND_COLOR);
		img = frmWnd.getWindow(width-(margin<<1),windowHeight);
		offGra.drawImage(img,(width-img.getWidth())>>1,(height-img.getHeight())>>1);
		
		//�`��J�n���W
		y = ((height-img.getHeight())>>1) + FONT_HEIGHT;
		
		// ���j���[���ڂ�`��		
		if(menu.length <= maxNum){
			//�\���\�ȍő吔�𒴂��Ă��Ȃ��Ƃ�
			
			for (int i = 0; i < menu.length ; i++) {
				
				// �t�H�[�J�X������Ƃ��́A�o�b�N�O���E���h���Ⴄ�F�ŕ`��				
				if (i == menuIndex) {
					frmWnd.setBGColor(FCS_COLOR);
					Image fImg = frmWnd.getWindow(width-(margin<<1),FONT_HEIGHT);
					offGra.drawImage(fImg,margin,y - FONT_ASCENT);
				}
				
				// ���j���[���ڂ�`��
				offGra.setColor(STR_COLOR);
				int x = (width >> 1);
				offGra.drawString(menu[i], x, y);

				y += margin + FONT_HEIGHT;
			}
		}else{
			//�\���\�ȍő吔�𒴂��Ă���Ƃ�
			
			offGra.setColor(STR_COLOR);
			//���}�[�N�̕\��
			if(menuIndex > (maxNum >> 1) && blinkflag ){
				//���}�[�N�ŏ�ɂ܂��\�����鍀�ڂ����邱�Ƃ�����
				offGra.drawString("��",width - (margin << 1) - charWidth, y );				
			}
						
			int offset = menuIndex - (maxNum >> 1);
			
			if(offset < 0) {
				offset = 0;
			}else if(menuIndex + (maxNum >> 1) > menu.length - 1){
				offset = menu.length - maxNum ;					
			}
			
			for (int i = offset; i < offset + maxNum ; i++) {
				
				// �t�H�[�J�X������Ƃ��́A�o�b�N�O���E���h���Ⴄ�F�ŕ`��				
				if (i == menuIndex) {
					frmWnd.setBGColor(FCS_COLOR);
					Image fImg = frmWnd.getWindow(width-(margin<<1),FONT_HEIGHT);
					offGra.drawImage(fImg,margin,y - FONT_ASCENT);
				}

				// ���j���[���ڂ�`��
				offGra.setColor(STR_COLOR);
				int x = (width >> 1);
				offGra.drawString(menu[i], x, y);

				y += margin + FONT_HEIGHT;
			}
			
						
			//���}�[�N�̕\��
			if( offset < menu.length - maxNum && blinkflag){			
				//���}�[�N�ŉ��ɂ܂��\�����鍀�ڂ����邱�Ƃ�����
				offGra.drawString("��",width - (margin << 1 ) - charWidth , y - (margin + FONT_HEIGHT));
			}
		}
		
		//�_�ŗp�J�E���^�����Z�b�g
		if(blinkCnter >= BLINK_IINTERVAL){
			blinkCnter = 0;
			blinkflag = !blinkflag;
		}

		offGra.unlock(true);
	}
	
	//===============================�L�[����==================================//

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
	
	/**
	 * ���̃L�����o�X��\������
	 */
	public void setDisplay(){
		Display.getDisplay(midlet).setCurrent(this);
	}
	
}
