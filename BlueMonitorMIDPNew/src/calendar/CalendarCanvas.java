package calendar;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.midlet.MIDlet;

import calendar.renderer.BpHrWaveRenderer;
import calendar.renderer.DailyBarGraphRenderer;
import calendar.renderer.EcgPlsBarGraphRenderer;
import calendar.renderer.CalendarRenderer;
import calendar.renderer.MonthBarGraphRenderer;
import calendar.renderer.RollMenuRenderer;
import calendar.renderer.RawWaveRenderer;
import calendar.renderer.TripleRotateMenuRenderer;
import calendar.renderer.WaitScreenRenderer;
import calendar.renderer.WeekBarGraphRenderer;
import calendar.renderer.YearBarGraphRenderer;

import request.Key;

import util.DojaFont;
import util.DojaGraphics;


public class CalendarCanvas extends Canvas {
	
	//================================�萔====================================//
	
	//------- �F�֘A  --------//
	/** �w�i�F */
	private static final int BG_COLOR = 0x00FFFFFF;
	
	//------- �t�H���g�֘A   --------//
	/** �t�H���g */
	private static final DojaFont FONT = DojaFont.getFont(DojaFont.SIZE_TINY);
	
	//================================�ϐ�====================================//

	//------- ���ʂ̕`��p�ϐ� --------//
	/** �I�t�O���t�B�b�N�X */
	protected DojaGraphics offGra;
	/** �I�t�C���[�W */
	protected Image offImg;

	/** �E�B���h�E�� */
	private int width;
	/** �E�B���h�E���� */
	private int height;
	
	/** MIDlet�I�u�W�F�N�g*/
	private MIDlet midlet;
	
	/** �J�����_�[�`��S���N���X */
	protected CalendarRenderer calendar;
	/** ���j���[�`��S���N���X  */
	protected RollMenuRenderer rollMenu;

	/** ��]���j���[�`��S���N���X */
	protected TripleRotateMenuRenderer triRotatemenu;
	/** Wait��ʕ`��S���N���X */
	protected WaitScreenRenderer waitScreen;
	/** �g�`��ʕ`��S���N���X */
	protected RawWaveRenderer rawWave;
	/** �g�`��ʕ`��S���N���X */
	protected BpHrWaveRenderer bpHrWave;
	/** �_�O���t(��)�`��S���N���X */
	protected DailyBarGraphRenderer dailyBarGraph;
	/** �_�O���t(�T)�`��S���N���X */
	protected WeekBarGraphRenderer weekBarGraph;
	/** �_�O���t(��)�`��S���N���X */
	protected MonthBarGraphRenderer monthBarGraph;
	/** �_�O���t(�N)�`��S���N���X */
	protected YearBarGraphRenderer yearBarGraph;
	
	//================================�֐�====================================//
	
	/**
	 * �R���X�g���N�^ 
	 */
	public CalendarCanvas(MIDlet midlet) {
		this.midlet = midlet;
		//��
		this.width = getWidth();
		//����
		this.height = getHeight();
		
		//�_�u���o�b�t�@�����O�̂��߂̃I�t�O���t�B�b�N�X�C���[�W�̐���
		if (offImg == null) {
			offImg   =Image.createImage(width,height);
			offGra =  new DojaGraphics(offImg.getGraphics(),this);
		}
		//�t�H���g���O���t�B�b�N�X�I�u�W�F�N�g�ɓK�p
		offGra.setFont(FONT);
		
		calendar      = new CalendarRenderer(width,height);
		rollMenu      = new RollMenuRenderer(width,height);

		triRotatemenu = new TripleRotateMenuRenderer(width,height);
		waitScreen    = new WaitScreenRenderer(width,height);
		rawWave       = new RawWaveRenderer(width,height);
		bpHrWave      = new BpHrWaveRenderer(width,height);	
		dailyBarGraph = new DailyBarGraphRenderer(width,height);
		weekBarGraph  = new WeekBarGraphRenderer(width,height);
		monthBarGraph = new MonthBarGraphRenderer(width,height);
		yearBarGraph  = new YearBarGraphRenderer(width,height);
	}/* End of CalendarCanvas() */
	
	/** 
	 * �`�揈�����s��
	 */
	protected void paint(Graphics g) {	
		g.drawImage(offImg,0,0,Graphics.LEFT|Graphics.TOP);
	}
	
	/**
	 * �J�����_�[�̕`��
	 */
	public void drawCalender(){
		offGra.lock();
		
		//�w�i�̓h��Ԃ�
		clearCanvas(offGra);		
		//�J�����_�[�̕`��
		calendar.draw(offGra);
		
		offGra.unlock(true);
	}
	
	/**
	 * ��]���j���[�̕`��
	 */
	public void drawTrippleRotateMenu(){
		offGra.lock();
		
		//�w�i�̓h��Ԃ�
		clearCanvas(offGra);
		//��]���j���[�̕`��
		triRotatemenu.draw(offGra);
		//Wait���j���[�̉��
		waitScreen.draw(offGra);
		
		offGra.unlock(true);
	}
	
	/**
	 * �g�`�̕`��
	 */
	public void drawRawWave(){
		offGra.lock();
		
		//�w�i�̓h��Ԃ�
		clearCanvas(offGra);
		rawWave.draw(offGra);
		//Wait���j���[�̉��
		waitScreen.draw(offGra);
		
		offGra.unlock(true);
	}
	
	/**
	 * BP,HR�g�`�̕`��
	 */
	public void drawBpHrWave(){
		offGra.lock();
		
		//�w�i�̓h��Ԃ�
		clearCanvas(offGra);
		bpHrWave.draw(offGra);
		//Wait���j���[�̉��
		waitScreen.draw(offGra);
		
		offGra.unlock(true);
	}
	
	/**
	 * �_�O���t�̕`��(��)
	 */
	public void drawDailyBarGraph(){
		offGra.lock();
		
		//�_�O���t�̕`��
		clearCanvas(offGra);
		dailyBarGraph.draw(offGra);
		//���j���[�̕`��		
		rollMenu.draw(offGra);		
		//Wait���j���[�̉��
		waitScreen.draw(offGra);
		
		offGra.unlock(true);
	}
	
	/**
	 * �_�O���t�̕`��(�T)
	 */
	public void drawWeekBarGraph(){
		offGra.lock();
		
		//�_�O���t�̕`��
		clearCanvas(offGra);
		weekBarGraph.draw(offGra);
		//���j���[�̕`��		
		rollMenu.draw(offGra);		
		//Wait���j���[�̉��
		waitScreen.draw(offGra);
		
		offGra.unlock(true);
	}
	
	/**
	 * �_�O���t�̕`��(��)
	 */
	public void drawMonthBarGraph(){
		offGra.lock();
		
		//�_�O���t�̕`��
		clearCanvas(offGra);
		monthBarGraph.draw(offGra);
		//���j���[�̕`��		
		rollMenu.draw(offGra);		
		//Wait���j���[�̉��
		waitScreen.draw(offGra);
		
		offGra.unlock(true);
	}
	
	/**
	 * �_�O���t�̕`��(�N)
	 */
	public void drawYearBarGraph(){
		offGra.lock();
		
		//�_�O���t�̕`��
		clearCanvas(offGra);
		yearBarGraph.draw(offGra);
		//���j���[�̕`��		
		rollMenu.draw(offGra);		
		//Wait���j���[�̉��
		waitScreen.draw(offGra);
		
		offGra.unlock(true);
	}


	/**
	 * �w�i�̓h��Ԃ�
	 * @param g�@Graphics�I�u�W�F�N�g
	 */
	private void clearCanvas(DojaGraphics g){
	    g.setColor(BG_COLOR);	    
	    g.clearRect(0, 0,width,height);
	}
	
	/**
	 * ���̃L�����o�X��\������
	 */
	public void setDisplay(){
		Display.getDisplay(midlet).setCurrent(this);
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

}
