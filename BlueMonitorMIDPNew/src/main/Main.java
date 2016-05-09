package main;


import javax.microedition.midlet.MIDlet;

import calendar.CalendarManager;


public class Main extends Thread {

	//================================�萔==================================//
	/**
	 * �f�o�b�N�t���O���`���܂��B
	 * <br>
	 * TODO: �����[�X���ɂ�<code>false</code>�ɂ��Ă��������B
	 */
	public static final boolean DEBUG = true;

	/** ���@�ł̎��s���ǂ��� true:���@ false:�G�~�����[�^ */
	public static final boolean IS_ACTUAL = false;
	
	/** �G�N���v�X�ł̃R���p�C�����ǂ���(���@�ɂ͉e���Ȃ�) */
	public static final boolean IS_ECLIPSE = false;

	/** ���[�v���ł̃X���[�v����*/
	private static final int SLEEP_TIME = 100;

	/** �^�C�g�����[�h */
	public static final int TITLE_MODE   = 0;
	/** ���� ���[�h */
	public static final int MEASURE_MODE = 1;
	/** �J�����_�[���[�h */
	public static final int CALENDAR_MODE = 2;
	/** �I�� ���[�h */
	public static final int QUIT_MODE = 3;

	//=================================�ϐ�==================================//
	/** MIDlet */
	private MIDlet midlet;
	/** ��� */
	private int mode_state;
	/** �ЂƂO�̏�� */
	private int back_mode_state;

	/** �^�C�g����ʂ̕`��ƌv�Z��S������N���X */
	private TitleManager title;
	/** �J�����_�[�̕`��ƌv�Z��S������N���X */
	private CalendarManager calmon;
	/** �������j�^�̕`��ƌv�Z��S������N���X */
	private BPMonitorManager bpmon;

	/** ���s�p�X���b�h */
	private Thread thread=null;

	//===============================����������================================//

	/**
	 * �R���X�g���N�^
	 *
	 * @param midlet MIDlet�I�u�W�F�N�g
	 */
	public Main(MIDlet midlet) {
		this.midlet = midlet;

		//�ŏ��̓^�C�g�����[�h
		mode_state = TITLE_MODE;
		back_mode_state = mode_state;
		modeChange(mode_state);

		//���C�����[�v���N��
		thread = new Thread(this);
		//���s�̊J�n
		thread.start();
	}

	//================================���C������=================================//

	/**
	 * �A�v���P�[�V�����̃��[�v���X���b�h���Ŏ��s
	 */
	public void run(){
		//�X���[�v����
		long startTime = System.currentTimeMillis();
		//�v�Z�ɂ�����������
		long pastTime = 0;

		while (true){

			//�L�[�A�v�Z�A�`��̊e�폈��
			if(mode_state == TITLE_MODE && title != null){
				//�^�C�g�����[�h
				title.process();
			}else if(mode_state == CALENDAR_MODE && calmon != null){
				//�J�����_�[���[�h
				calmon.process();
			}else if(mode_state == MEASURE_MODE  && bpmon != null){
				//�v�Z���[�h
				bpmon.process();
			}

			if(back_mode_state != mode_state){//���[�h�̕ύX���Ȃ����ǂ����`�F�b�N�B
				//���[�h�ύX�̔��f
				modeChange(mode_state);
				//�O�̃��[�h�̏I��
				modeQuit(back_mode_state);
				//���̏�Ԃ�����Ă���
				back_mode_state = mode_state;
			}

			//�v�Z�ɂ�����������
			pastTime = System.currentTimeMillis() - startTime;

			if(pastTime < SLEEP_TIME){
				//�x�~
				pause(SLEEP_TIME+5 - pastTime);
			}
			startTime = System.currentTimeMillis();
		}
	}


	/**
	 * �X���b�h�̋x�~
	 */
	public void pause(long time){
		try {
			//�X���[�v
			Thread.sleep(time);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//=============================���[�h�̐؂�ւ���================================//

	/**
	 * ���[�h�̕ύX�𔽉f����
	 *
	 * @param newMode �ύX�惂�[�h
	 */
	private void modeChange(int newMode){
		if(newMode == TITLE_MODE){
			//�^�C�g��
			title = new TitleManager(this);
			title.setDisplay(midlet);
		}else if(newMode == MEASURE_MODE){
			//�v��
			bpmon = new BPMonitorManager(this,midlet);
			bpmon.setDisplay(midlet);
		}else if(newMode == CALENDAR_MODE){
			//�J�����_�[
			calmon = new CalendarManager(this,midlet);
			calmon.setDisplay(midlet);
		}else if(newMode == QUIT_MODE){
			//�I��
			midlet.notifyDestroyed();
		}
	}

	/**
	 * ���[�h���I������
	 *
	 * @param mode �I�����郂�[�h
	 */
	private void modeQuit(int mode){
		switch (mode) {
		case TITLE_MODE://�^�C�g��
			if(title != null)title = null;
			break;
		case CALENDAR_MODE://�J�����_�[���[�h
			if(calmon != null)calmon = null;
			break;		
		case MEASURE_MODE://�v�����[�h
			if(bpmon != null)bpmon = null;
			break;
		}
	}

	/**
	 * ���[�h��ύX����
	 * ���ۂɔ��f�����̂́A���[�v���ł̎��̏�Ԃ̃`�F�b�N��
	 *
	 * @param mode
	 */
	public void setMode(int mode){
		mode_state = mode;
	}
}
