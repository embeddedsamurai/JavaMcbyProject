package main;

import gui.TitleCanvas;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;

import request.Key;
import request.Request;
import request.RequestQueue;

import util.Option;

public class TitleManager implements CommandListener {
	
	//================================�萔====================================//
	
	/**	 ���j���[�̃C���f�b�N�X����i�߂邱�Ƃ�\�� */
	private static final int INCREMENT_MENU = 0;
	/**	 ���j���[�̃C���f�b�N�X������炷���Ƃ�\�� */
	private static final int DECREMENT_MENU = 1;
	/**	 ���݃t�H�[�J�X�̂��郁�j���[���ڂ�I�����邱�Ƃ�\�� */
	private static final int SELECT_MENU = 2;
	
	//================================�ϐ�====================================//
	
	/** �L�����o�X */
	private TitleCanvas canvas;	
	/** ���N�G�X�g�L���[ */
	private RequestQueue requestQueue;
	/** �eThread */
	private Main parent;
	
	//===============================����������=================================//
	
	/**
	 * �R���X�g���N�^ 
	 */
	public TitleManager(Main main) {
		this.parent = main;
		//�L�����o�X���쐬
		canvas = new TitleCanvas();
		//�R�}���h���X�i�̐ݒ�
		canvas.setCommandListener(this);
		//���N�G�X�g�L���[���쐬
		requestQueue = new RequestQueue();		
	}
	
	//===============================���C������==================================//
	
	/**
	 * �`��A�v�Z�Ȃǂ̈�A�̏������s��
	 */
	public void process(){
		//�L�[���X�V
		if(canvas.isShown())Key.registKeyEvent();
		//�L�[����
		key();
		//���N�G�X�g�̏���
		doRequest();
		//�`��
		draw();
	}
		
	/**
	 * �`�揈��
	 */
	private void draw() {
		canvas.draw();
	}
	
	/**
	 * ���N�G�X�g�̏���������
	 */
	private void doRequest(){
		//�L���[���烊�N�G�X�g�����o��
		Request req = requestQueue.getRequest();
		
		if(req != null){
			//�L���[���J���łȂ���
			int next = 0;
			switch (req.getCommand()){							
				case DECREMENT_MENU:				
					//���j���[�̃t�H�[�J�X�̃C���f�b�N�X����i�߂�
					next = (canvas.getSelectedMenuIndex()
							+canvas.getNumOfMenuItem() - 1)%canvas.getNumOfMenuItem();
					canvas.setSelectedMenuIndex(next);
					break;
				case INCREMENT_MENU:
					//���j���[�̃t�H�[�J�X�̃C���f�b�N�X����߂�
					next = (canvas.getSelectedMenuIndex()+ 1)%canvas.getNumOfMenuItem();
					canvas.setSelectedMenuIndex(next);
					break;
				case SELECT_MENU:
					//���݃t�H�[�J�X����Ă��郁�j���[���ڂ�I������	
					menuProcess();
					break;
				default:
					break;
			}
		}
	}
	
	 	
	//=============================�L�[�A�R�}���h����================================//

	/**
	 * �L�[����
	 */
	private void key(){
		if(Key.isKeyPressed(Canvas.FIRE)){//����L�[���������Ƃ�
			//�t�H�[�J�X����Ă��郁�j���[���ڂ�I������
			requestQueue.putRequest(new Request(SELECT_MENU));
		}else if(Key.isKeyPressed(Canvas.DOWN)){//���L�[���������Ƃ�			
			//�L�����o�X�Ń��j���[�̃t�H�[�J�X�̃C���f�b�N�X����i�߂�
			requestQueue.putRequest(new Request(INCREMENT_MENU));
		}else if(Key.isKeyPressed(Canvas.UP)){//��L�[���������Ƃ�			
			//�L�����o�X�Ń��j���[�̃t�H�[�J�X�̃C���f�b�N�X����߂�
			requestQueue.putRequest(new Request(DECREMENT_MENU));								
		}
	}
		
	/**
	 * �R�}���h���������� 
	 * @param com �R�}���h
	 * @param disp �R�}���h��ێ�����f�B�X�v���[�I�u�W�F�N�g�̎Q��
	 * 
	 */
	public void commandAction(Command com, Displayable disp) {	

	}
	
	/**
	 * �I�����ꂽ���j���[�ɉ���������
	 */
	private void menuProcess(){
		
		//�I�����ꂽ���j���[
		String menu = canvas.getSelectedMenu();
		Option op = Option.getOp();
		
		if(menu.equals(TitleCanvas.MENU_EXIT)){
			//�I��
			parent.setMode(Main.QUIT_MODE);			
		}else if(menu.equals(TitleCanvas.MENU_BACK)){
			//�g�b�v�֖߂�
			canvas.setTitleMode(TitleCanvas.TITLE_TOP);			
		}else if(menu.equals(TitleCanvas.MENU_MESUREMENT)){
			//�v���J�n���j���[��
			canvas.setTitleMode(TitleCanvas.TITLE_MEASUREMENT);
		}else if(menu.equals(TitleCanvas.MENU_SETTING)){
			//�ݒ胁�j���[��			
			canvas.setTitleMode(TitleCanvas.TITLE_SETTING);
		}else if(menu.equals(TitleCanvas.MENU_MESUREMENT_FILE)){
			//�t�@�C������v���J�n	
			op.setBluetooth(false);			
			parent.setMode(Main.MEASURE_MODE);
		}else if(menu.equals(TitleCanvas.MENU_MESUREMENT_IO)){
			//Bluetooth����v��
			op.setBluetooth(true);			
			parent.setMode(Main.MEASURE_MODE);
		}else if(menu.equals(TitleCanvas.MENU_OLD_GRAPH)){
			//�l�b�g���[�N����O���t�擾
			parent.setMode(Main.CALENDAR_MODE);
		}else if(menu.equals(TitleCanvas.MENU_BACK_LIGHT)){			
			//�o�b�N���C�g�̐ݒ��			
			canvas.setTitleMode(TitleCanvas.TITLE_SETTING_LIGHT);
			//���j���[�C���f�b�N�X��ύX
			setMenuIndexForSetting(op.isBackLightOn());		
		}else if(menu.equals(TitleCanvas.MENU_MCBY_CHANGE)){			
			//�}�N�r�[�̕ύX�̐ݒ��
			canvas.setTitleMode(TitleCanvas.TITLE_SETTING_MCBY);
			//���j���[�C���f�b�N�X��ύX		
			setMenuIndexForSetting(op.isMCBY());					
		}else if(menu.equals(TitleCanvas.MENU_VIBRATION)){			
			//�o�C�u���[�V�����̐ݒ��
			canvas.setTitleMode(TitleCanvas.TITLE_SETTING_VIBRATION);
			//���j���[�C���f�b�N�X��ύX
			setMenuIndexForSetting(op.isVibrationOn());		
		}else if(menu.equals(TitleCanvas.MENU_SOUND)){			
			//�T�E���h�̐ݒ��
			canvas.setTitleMode(TitleCanvas.TITLE_SETTING_SOUND);
			//���j���[�C���f�b�N�X��ύX
			setMenuIndexForSetting(op.isSoundOn());			
		}else if(menu.equals(TitleCanvas.MENU_SOUND)){			
			//QR�R�[�h�̐ݒ��			
		}else if(menu.equals(TitleCanvas.MENU_POWER)){
			//�d�����g���̕ύX���j���[��
			canvas.setTitleMode(TitleCanvas.TITLE_SETTING_POWER);
			//���j���[�C���f�b�N�X��ύX
			setMenuIndexForSetting(op.is50HzPowerSupply());
		}else if(menu.equals(TitleCanvas.MENU_MCBY1)){		
			//�}�N�r�[�̕ύX(MCBY)
			op.setMCBY(true);			
			//�ݒ�ꗗ��ʂɖ߂�
			canvas.setTitleMode(TitleCanvas.TITLE_SETTING);
		}else if(menu.equals(TitleCanvas.MENU_MCBY2)){
			//�}�N�r�[�̕ύX(ZEAL)
			op.setMCBY(false);
			//�ݒ�ꗗ��ʂɖ߂�
			canvas.setTitleMode(TitleCanvas.TITLE_SETTING);
		}else if(menu.equals(TitleCanvas.MENU_50HZ)){
			//�d�����g���̕ύX(50Hz)
			op.set50HzPowerSupply(true);
			//�ݒ�ꗗ��ʂɖ߂�
			canvas.setTitleMode(TitleCanvas.TITLE_SETTING);
		}else if(menu.equals(TitleCanvas.MENU_60HZ)){
			//�d�����g���̕ύX(60Hz)
			op.set50HzPowerSupply(false);
			//�ݒ�ꗗ��ʂɖ߂�
			canvas.setTitleMode(TitleCanvas.TITLE_SETTING);
		}else if(menu.equals(TitleCanvas.MENU_ON)){
			//�ݒ���n�m�ɂ���
			setEnableSetting(canvas.getTitleMode(),true);
			//�ݒ�ꗗ��ʂɖ߂�
			canvas.setTitleMode(TitleCanvas.TITLE_SETTING);
		}else if(menu.equals(TitleCanvas.MENU_OFF)){
			//�ݒ���nFF�ɂ���
			setEnableSetting(canvas.getTitleMode(),false);
			//�ݒ�ꗗ��ʂɖ߂�
			canvas.setTitleMode(TitleCanvas.TITLE_SETTING);
		}
	}		
	
	/**
	 * �ݒ��ʂ̂Ƃ��AON�Ƃn�e�e�ōŏ��ɑI������Ă���C���f�b�N�X��ύX����
	 * @param flag �L�����o�X���̐ݒ�̃C���f�b�N�X���Atrue�Ȃ�n�m��ԁAfalse�Ȃ�OFF���
	 */
	private void setMenuIndexForSetting(boolean flag){
		if(flag){                          //����ON�̂Ƃ�
			canvas.setSelectedMenuIndex(0);
		}else{                             //����OFF�̂Ƃ�
			canvas.setSelectedMenuIndex(1);
		}
	}
	
	/**
	 * �ݒ���n�m�A�n�e�e����
	 * 
	 * @param mode �^�C�g�����[�h
	 * @param flag true�Ȃ�ON, false�Ȃ�OFF
	 */
	private void setEnableSetting(int mode,boolean flag){
		Option op = Option.getOp();
		switch (mode) {
			case TitleCanvas.TITLE_SETTING_LIGHT:
				//�o�b�N���C�g�̐ݒ�		
				op.setBackLightOn(flag);							
				break;
			case TitleCanvas.TITLE_SETTING_SOUND:
				//�T�E���h�̐ݒ�
				op.setSoundOn(flag);				
				break;
			case TitleCanvas.TITLE_SETTING_VIBRATION:
				//�o�C�u���[�V�����̐ݒ�
				op.setVibrationOn(flag);				
				break;
		}
	}
	
	//=================================���̑�===================================//	
	
	/**
	 * �\������
	 */
	public void setDisplay(MIDlet midlet){
		Display.getDisplay(midlet).setCurrent(canvas);
	}
	
}
