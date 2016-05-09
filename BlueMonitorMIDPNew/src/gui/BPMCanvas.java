package gui;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.midlet.MIDlet;

import main.Main;


import request.Key;
import tsk.BPCalcTSK;
import util.DojaFont;
import util.DojaGraphics;

public class BPMCanvas extends Canvas {

	//NOTICE
	//DojaFont�N���X�̗̂��݂ŁADojaFont.drawString(string,x,y)
	//����Ƃ��Ɉ����Ƃ��Ďw�肷��x���W�͕`��J�n�̍��W�ł͂Ȃ��āA������̒�����������W�ł���B

	//================================�萔====================================//

	//------- �F�֘A  --------//
	/** �w�i�F */
	private static final int BG_COLOR = 0x00FFFFFF;
	/** �o�b�N�O�����h�̖ڐ���̐F */
	private static final int BG_LINE_COLOR = 0x999999;
	/** �o�b�N�O�����h�̖ڐ���̐F */
	private static final int BG_LINE_COLOR2 = 0xC9C9C9;
	/** �ڐ���̎��̐F */
	private final int BG_STR_COLOR = 0x000000FF;

	/** �S�d�}�M���̐F */
	private static final int ECG_COLOR = 0x00FF0066;

	/** R�g�s�[�N�̂̐F */
	private static final int R_WAVE_COLOR = 0x00EE3344;
	/** ���g�M���̐F */
	private static final int PLS_COLOR = 0x003366FF;
	/** �����l�`��̐F */
	private static final int SBP_COLOR = 0x0000cc66;
	/** �X�y�N�g���̐F(����) */
	private static final int SPC_COLOR = 0x00FF4500;

	//------- ������   --------//
	/** �S������\�������� */
	private static final String HR_STR =  "�S����";
	/** ���g�`�����Ԃ�\�������� */
	private static final String PAT_STR = "���g�`������";
	/** ������\�������� */
	private static final String SBP_STR =  "����";
	/** �X�y�N�g����\�������� */
	private static final String SPC_STR = "�X�y�N�g��";

	//------- �o�b�N�O���E���h  --------//
	/** �o�b�N�O�����h�̖ڐ���̊Ԋu */
	private static final int BG_LINE_MARGIN = 10;
	/** �ڐ�����̒��� */
	private final int BG_LINE_LONG = 5;
	/**	�o�b�N�O���E���h�̖ڐ�����̐�(�O�����h���C��������)*/
	private static final int NUM_OF_CURRENT_BGLINE = 8;
	/**	�o�b�N�O���E���h�̖ڐ�����̐�(�O�����h���C��������)*/
	private static final int NUM_OF_TREND_BGLINE = 6;

	//------- �t�H���g�֘A   --------//
	/** �t�H���g */
	private static final DojaFont FONT = DojaFont.getFont(DojaFont.SIZE_TINY);
	/** �t�H���g�̃f�B�Z���g */
	private static final int FONT_DECENT = FONT.getDescent();
	/** �t�H���g�̃A�Z���g */
	private static final int FONT_ASCENT = FONT.getAscent();
	/** �t�H���g�̍��� */
	private static final int FONT_HEIGHT = FONT_ASCENT + FONT_DECENT;
	/** �����̊Ԋu */
	private static final int STR_MARGIN = 5;

	//------- �J�����g�p�̊g��k���{���̒�` --------//
	/** �ő��X�������̕\���k���{�� */
	public static final int MAN_X_REDUCTION_RATE = 4;
	/** �ŏ���X�������̕\���k���{�� */
	public static final int MIX_X_REDUCTION_RATE = 1;

	/** �ŏ���Y�������̕\���{�� (1/MIN_X_SCALE)���{��*/
	public static final int MIN_Y_SCALE = 1;
	/** �ő��Y�������̕\���{�� (1/MAN_X_SCALE)���{��*/
	public static final int MAX_Y_SCALE = 3;

	//================================�ϐ�====================================//

	//------- ���ʂ̕`��p�ϐ� --------//
	/** �I�t�O���t�B�b�N�X */
	protected DojaGraphics offGra;
	/** �I�t�C���[�W */
	protected Image offImg;

	/** �E�B���h�E�� */
	private int width ;
	/** �E�B���h�E���� */
	private int height;

	//------- ���x���̍��W�֘A --------//
	/** �S�d�}�`��̊��Y���W */
	private int ecg_gl_y;
	/** ���g�`��̊��Y���W */
	private int pls_gl_y;
	/** �S�����`��̊��Y���W */
	private int hr_gl_y;
	/** ���g�`�����ԕ`��̊��Y���W */
	private int pat_gl_y;
	/** ���k�������`��̊��Y���W */
	private int sbp_gl_y;

	//------- �J�����g��ʂ̍��W�֘A --------//
	/** �w�i�̏c�̖ڐ�����̍���(�J�����g)�@*/
	private int bg_current_vertical_line_h;

	/** �S�����Ȃǂ̏���\�����镶�����y���W(�J�����g) */
	private int labelCurrent1_y;
	/** ���g�`�����ԂȂǂ̏���\�����镶�����y���W(�J�����g) */
	private int labelCurrent2_y;

	/** �J�����g�g�`�̕\���̏k���{��(X������)*/
	private int xReductionRate = MAN_X_REDUCTION_RATE;
	/** �J�����g�g�`�̕\���̔{��(Y������)*/
	private int yScale = MIN_Y_SCALE;

	//------- �g�����h��ʂ̍��W�֘A --------//
	/** �w�i�̏c�̖ڐ�����̍���(�g�����h)�@*/
	private int bg_trend_vertical_line_h;

	/** �S�����Ȃǂ̏���\�����镶�����y���W(�g�����h) */
	private int labelTrend1_y;
	/** ���g�`�����ԂȂǂ̏���\�����镶�����y���W(�g�����h) */
	private int labelTrend2_y;
	/** �����Ȃǂ̏���\�����镶�����y���W(�g�����h) */
	private int labelTrend3_y;

	//------- �X�y�N�g����ʂ̍��W�֘A --------//
	/** �U���X�y�N�g���̍ő�l(���̒l�ŐU���X�y�N�g���𐳋K������) */
	private int maxSpectrum;
	/** �X�y�N�g���`��̊��Y���W */
	private int spectrum_gl_y;

	/** �S�d�}�̃X�y�N�g���z�� */
	public double[] ecgSpectrum;

	/** MIDLET�I�u�W�F�N�g�ւ̎Q�� */
	private MIDlet midlet;

	//==============================����������==================================//
	/**
	 * �R���X�g���N�^
	 */
	public BPMCanvas(MIDlet midlet){
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

		//�S�d�}�`�����W
	    ecg_gl_y = height * 3 / 10;
		//���g�`�����W
	    pls_gl_y = height * 8 / 10;
		//�S�����`�����W
	    hr_gl_y  = height * 3 / 15;
		//���g�`�����ԕ`�����W
	    pat_gl_y = height * 8 / 15;
		//���k�������`�����W
	    sbp_gl_y = height *13 / 15;
		//�X�y�N�g���`�����W
	    spectrum_gl_y = height - 2;

	    //�w�i�̏c�̖ڐ�����̍���(�J�����g���)
	    bg_current_vertical_line_h = (BG_LINE_MARGIN * (NUM_OF_CURRENT_BGLINE>>1));
	    //�w�i�̏c�̖ڐ�����̍���(�g�����h���)
	    bg_trend_vertical_line_h = (BG_LINE_MARGIN * (NUM_OF_TREND_BGLINE>>1));

	    //�S�����Ȃǂ̏���\�����镶�����y���W
	    labelCurrent1_y = ecg_gl_y - bg_current_vertical_line_h - FONT_DECENT;
	    //���g�`�����ԂȂǂ̏���\�����镶�����y���W
	    labelCurrent2_y = pls_gl_y - bg_current_vertical_line_h - FONT_DECENT;

		//�S�����Ȃǂ̏���\�����镶�����y���W
		labelTrend1_y = hr_gl_y - bg_trend_vertical_line_h - FONT_DECENT;
		// ���g�`�����ԂȂǂ̏���\�����镶�����y���W
		labelTrend2_y = pat_gl_y - bg_trend_vertical_line_h - FONT_DECENT;
		// �����Ȃǂ̏���\�����镶�����y���W */
		labelTrend3_y = sbp_gl_y - bg_trend_vertical_line_h - FONT_DECENT;

	    //�U���X�y�N�g���̍ő�l(���̒l�ŐU���X�y�N�g���𐳋K������)
	    maxSpectrum = (height * 2 / 5);

	    //�U���X�y�N�g����ێ�����z��
	    ecgSpectrum = new double[width];
	    for(int i = 0; i < width; i++){
	    	ecgSpectrum[i] = 0;
	    }
	}

	//===============================�`�揈��==================================//
	/**
	 * �`��(�I�t�O���t�B�b�N�X�𔽉f������)
	 */
	protected void paint(Graphics g) {
		g.drawImage(offImg,0,0,Graphics.LEFT|Graphics.TOP);
	}

	//===========================�e��ʂ̕`��ŋ���==============================//
	/**
	 * �X�̃��x����`��
	 * @param g Graphics�I�u�W�F�N�g
	 * @param label �`�悷�郉�x��
	 * @param value �`�悷��l
	 * @param x     x���W
	 * @param y     y���W
	 */
	private void drawLabel(DojaGraphics g,String label,int value,int x,int y){
		String str = "";
		if(value == -1){//�܂��l���Z�o����Ă��Ȃ��Ƃ�
			str = label + " " + "N/A";
		}else{
			if(value >= 100){
				str = label + " " + value;
			}else if (value >= 10){
				str = label + "  " + value;
			}else{
				str = label + "   " + value;
			}
		}
		g.drawString(str,x,y);
	}

	//============================�J�����g��ʂ̕`��================================//
	/**
	 * �J�����g��ʂ̕`�揈�����s��
	 * @param ecgBuf                 �I���W�i����ECG�M��
 	 * @param plsBuf                 ���g�M��
	 * @param rWavePeek              R�g�s�[�N�̃C���f�b�N�X��ێ�����z��
	 * @param hr�@�@�@�@�@�@�@�@�@�@�@�@�@�@�@�@�@�@ �S����
	 * @param pat                    ���g�`������
	 * @param sbp                    ���k������
	 */
	public void drawCurrent(double[] ecgBuf,
	        		        double[] plsBuf,int[] rWavePeek,double hr,double pat,double sbp){
		offGra.lock();

		//--------------�w�i�̕`��---------------//
		//�w�i�h��Ԃ�
		offGra.setColor(BG_COLOR);
		offGra.clearRect(0,0,width,height);
		//�S�d�}�̔w�i�̖ڐ������`��
		drawBGLineCurrent(offGra,ecg_gl_y);
		//���g�̔w�i�̖ڐ������`��
		drawBGLineCurrent(offGra,pls_gl_y);

		//--------------�M���̕`��---------------//
		//�g�`�`��(�S�d�})(�f�t�H���g�ł̓m�b�`�t�B���^�K�p�M����`��)
		offGra.setColor(ECG_COLOR);
		drawCurrentSignal(offGra,ecgBuf,ecg_gl_y);
		//�g�`�`��(���g)
		offGra.setColor(PLS_COLOR);
		drawCurrentSignal(offGra,plsBuf,pls_gl_y);
		//R�g�̃s�[�N��`��
		offGra.setColor(R_WAVE_COLOR);
		drawRwavePeek(rWavePeek);

		//--------------���x���`��---------------//
		//�S����
		int x = (STR_MARGIN + (FONT.stringWidth(HR_STR + "....")>>1));
		offGra.setColor(ECG_COLOR);
		drawLabel(offGra,HR_STR,(int)hr,x,labelCurrent1_y);
		//���g�`������
		x = (STR_MARGIN + (FONT.stringWidth(PAT_STR + "....")>>1));
		offGra.setColor(PLS_COLOR);
		drawLabel(offGra,PAT_STR,(int)pat,x,labelCurrent2_y);
		//����
		offGra.setColor(SBP_COLOR);
		x = width - ((FONT.stringWidth(SBP_STR + "....")>>1) + STR_MARGIN);
		drawLabel(offGra,SBP_STR,(int)sbp,x,labelCurrent2_y);

		if(Main.DEBUG){
			offGra.setColor(PLS_COLOR);

			x = width - ((FONT.stringWidth(dbgMessage)>>1) + STR_MARGIN);
			offGra.drawString(dbgMessage,x,labelCurrent1_y + FONT_HEIGHT);

			x = width - ((FONT.stringWidth(dbgMessage2)>>1) + STR_MARGIN);
			offGra.drawString(dbgMessage2,x,labelCurrent1_y + (FONT_HEIGHT<<1));

			x = width - ((FONT.stringWidth(dbgMessage3)>>1) + STR_MARGIN);
			offGra.drawString(dbgMessage3,x,labelCurrent1_y + (FONT_HEIGHT*3));

			x = width - ((FONT.stringWidth(dbgMessage4)>>1) + STR_MARGIN);
			offGra.drawString(dbgMessage4,x,labelCurrent1_y + (FONT_HEIGHT*4));
		}

		offGra.unlock(true);
	}

	/**
	 * �J�����g��ʂ̔w�i�̖ڐ������`��
	 * @param g   Graphics�I�u�W�F�N�g
	 * @param glY �`��̊���W ECG,PLS�̃O�����h���C��
	 */
	private void drawBGLineCurrent(DojaGraphics g,int glY){
		//�`��F��ύX
		g.setColor(BG_LINE_COLOR);
		//�`��̊�̐���`��
		g.drawLine(0,glY,width,glY);

		//�`��F��ύX
		g.setColor(BG_LINE_COLOR2);
	    //�w�i�̖ڐ������`��(x�������̐�)
	    for (int i = 1; i < (NUM_OF_CURRENT_BGLINE>>1); i++) {
	    	//�㔼��
	    	int y = glY - i*BG_LINE_MARGIN;
	    	offGra.drawLine(0,y,width,y);
	    	//������
	    	y = glY + i * BG_LINE_MARGIN;
	        offGra.drawLine(0,y,width,y);
	    }
	    //�w�i�̖ڐ������`��(y�������̐�)
	    int startY = glY - bg_current_vertical_line_h;
	    int endY   = glY + bg_current_vertical_line_h;
	    for (int i = 1; i*BG_LINE_MARGIN < width; i++) {
	    	int x = i*BG_LINE_MARGIN;
	        offGra.drawLine(x,startY,x,endY);
	    }
	}

	/**
	 * �J�����g�̔g�`�̕`��
	 * @param g          �����_�����O�pGraphics�I�u�W�F�N�g
	 * @param signal     �`�悷��M��
	 * @param gly        �M���̕`�����W
	 */
	private void drawCurrentSignal(DojaGraphics g, double[] signal,int glY){
		//�g�`�̑傫���𒲐�
		double max = signal[0];
		double min = signal[0];
		double ave = 0;
		for(int i = 0; i < signal.length ; i++){
			if(max < signal[i]){
				max = signal[i];
			}else if(min > signal[i]){
				min = signal[i];
			}
			ave += signal[i];
		}
		ave /= signal.length;

		double scale= ((double)(bg_current_vertical_line_h)/(double)(max-ave))*yScale;

		//�g�`�̕`��
		for(int i = 0; (i+1)*xReductionRate < signal.length; i++){
			offGra.drawLine(i,glY - (int)(signal[i*xReductionRate]*scale) + (int)(ave*scale)
					     ,i+1,glY - (int)(signal[(i+1)*xReductionRate]*scale) + (int)(ave*scale) );
		}
	}

	/**
	 * R�g�̃s�[�N��`�悷��
	 * @param rWavePeek�@R�g�s�[�N�̂�����W�ێ������z��(R�g�s�[�N������Ƃ��͂P�A�Ȃ�����0)
	 */
	private void drawRwavePeek(int[] rWavePeek){
		for(int i = 0; i*xReductionRate < rWavePeek.length ; i++){
			for(int j = 0; j < xReductionRate; j++){
				//�k�����Ă���Ƃ���R�g�̃s�[�N��������ƕ\���ł���
				//�悤��R�g�̃s�[�N��(xScale - 1)������
				//�`�悷��_�̎��͂�R�g�̃s�[�N���Ȃ����ǂ����T��
				if(rWavePeek[i*xReductionRate + j] > 0){
					offGra.drawLine(i,labelCurrent1_y,i,labelCurrent1_y - BG_LINE_MARGIN);
					break;
				}
			}
		}
	}

	//===========================�g�����h��ʂ̕`��===============================//
	/**
	 * �g�����h��ʂ̕`��
	 * @param hrBuf  �S�����`��o�b�t�@
	 * @param patBuf ���g�`�����ԕ`��o�b�t�@
	 * @param sbpBuf ���k�������`��o�b�t�@
	 */
	public void drawTrend(double[] hrBuf,double[] patBuf,double[] sbpBuf){
		offGra.lock();

		//�w�i�h��Ԃ�
		offGra.setColor(BG_COLOR);
		offGra.clearRect(0,0,width,height);

		//-------------�w�i��`��---------------//
		//�S�����̔w�i�̖ڐ������`��
		drawBGLineTrend(offGra,hr_gl_y);
		//���g�`�����Ԃ̔w�i�̖ڐ������`��
		drawBGLineTrend(offGra,pat_gl_y);
		//���k�������̔w�i�̖ڐ������`��
		drawBGLineTrend(offGra,sbp_gl_y);

		//-------------�M���̕`��---------------//
		//�g�`�`��(�S����)
		offGra.setColor(ECG_COLOR);
		drawTrendSignal(offGra,hrBuf,hr_gl_y);
		//�g�`�`��(���g�`������)
		offGra.setColor(PLS_COLOR);
		drawTrendSignal(offGra,patBuf,pat_gl_y);
		//�g�`�`��(����)
		offGra.setColor(SBP_COLOR);
		drawTrendSignal(offGra,sbpBuf,sbp_gl_y);

		//-------------���x���`��---------------//
		//�S����
		int x = (STR_MARGIN + (FONT.stringWidth(HR_STR  +"....")>>1));
		offGra.setColor(ECG_COLOR);
		drawLabel(offGra,HR_STR,(int)hrBuf[hrBuf.length-1],x,labelTrend1_y);
		//���g�`������
		x = (STR_MARGIN + (FONT.stringWidth(PAT_STR  +"....")>>1));
		offGra.setColor(PLS_COLOR);
		drawLabel(offGra,PAT_STR,(int)patBuf[patBuf.length-1],x,labelTrend2_y);
		//����
		offGra.setColor(SBP_COLOR);
		x = (STR_MARGIN + (FONT.stringWidth(SBP_STR  +"....")>>1));
		drawLabel(offGra,SBP_STR,(int)sbpBuf[sbpBuf.length-1],x,labelTrend3_y);

		offGra.unlock(true);
	}

	/**
	 * �g�����h�̔g�`�̕`��
	 * @param g         �����_�����O�pGraphics�I�u�W�F�N�g
	 * @param signal    �`�悷��M��
	 * @param gly       �M���̕`�����W
	 */
	private void drawTrendSignal(DojaGraphics g, double[] signal,int glY){
		//�g�`�̑傫���𒲐�
		double max = signal[0];
		double min = signal[0];
		double ave = 0;
		for(int i = 0; i < signal.length ; i++){
			if(max < signal[i]){
				max = signal[i];
			}else if(min > signal[i]){
				min = signal[i];
			}
			ave += signal[i];
		}
		ave /= signal.length;

		double scale  = ((double)(bg_trend_vertical_line_h)/(double)(max));

		//�g�`�̕`��
		for(int i = 0; i< signal.length - 1; i++){
			offGra.drawLine(i,glY - (int)(signal[i]*scale) + (int)(ave*scale)
					     ,i+1,glY - (int)(signal[(i+1)]*scale) + (int)(ave*scale) );
		}
	}


	/**
	 * �g�����h��ʂ̔w�i��`�悷��
	 * @param g
	 * @param glY
	 */
	private void drawBGLineTrend(DojaGraphics g,int glY){
		//�`��F��ύX
		g.setColor(BG_LINE_COLOR);
		//�`��̊�̐���`��
		g.drawLine(0,glY,width,glY);

		//�`��F��ύX
		g.setColor(BG_LINE_COLOR2);
	    //�w�i�̖ڐ������`��(���̐�)
	    for (int i = 1; i < (NUM_OF_TREND_BGLINE>>1); i++) {
	    	//�㔼��
	    	int y = glY - i*BG_LINE_MARGIN;
	    	offGra.drawLine(0,y,width,y);
	    	//������
	    	y = glY + i * BG_LINE_MARGIN;
	        offGra.drawLine(0,y,width,y);
	    }
	    //�w�i�̖ڐ������`��(�c�̐�)
	    int startY = glY - bg_trend_vertical_line_h;
	    int endY   = glY + bg_trend_vertical_line_h;
	    for (int i = 1; i*BG_LINE_MARGIN < width; i++) {
	    	int x = i*BG_LINE_MARGIN;
	        offGra.drawLine(x,startY,x,endY);
	    }
	}

	//============================�U���X�y�N�g���̕`��===============================//

	/**
	 * �U���X�y�N�g����ʂ̕`��
	 * @param ecgBuf  �S�d�}�M���̔z��
	 * @param rWavePeek R�g�s�[�N���i�[�����z��
	 * @param hr �S����
	 *
	 */
	public void drawECGSpectrum(double[] ecgBuf,int[] rWavePeek,int hr){
		offGra.lock();

		//---------------�w�i�̕`��----------------//
		//�w�i�h��Ԃ�
		offGra.setColor(BG_COLOR);
		offGra.clearRect(0,0,width,height);
		//�o�b�N�O���E���h�̕`��
		drawBGLineSpectrum(offGra);

		//-----------���ԗ̈�M���̕`��-------------//
		//�S�d�}
		offGra.setColor(ECG_COLOR);
		drawCurrentSignal(offGra,ecgBuf,ecg_gl_y);
		//R�g�s�[�N���v���b�g
		offGra.setColor(R_WAVE_COLOR);
		drawRwavePeek(rWavePeek);

		//-------------�X�y�N�g���̕`��--------------//
		offGra.setColor(SPC_COLOR);
		drawSpectrum(offGra,ecgSpectrum);

		//---------------���x���̕`��---------------//
		//�S����
		int x = (STR_MARGIN + (FONT.stringWidth(HR_STR +"...." )>>1));
		offGra.setColor(ECG_COLOR);
		drawLabel(offGra,HR_STR,hr,x,labelCurrent1_y);
		//�X�y�N�g��
		offGra.setColor(SPC_COLOR);
		x = (STR_MARGIN + (FONT.stringWidth(SPC_STR)>>1));
		offGra.drawString(SPC_STR,x,height - maxSpectrum - FONT_DECENT);

		offGra.unlock(true);
	}

	/**
	 * �X�y�N�g����ʂ�\�����Ă���Ƃ��̔w�i��`��
	 */
	public void drawBGLineSpectrum(DojaGraphics g){

		//---------------------------���ԗ̈�̃O���t�̂̔w�i---------------------------//
		//�J�����g�Ŏg�p���Ă���㔼���̉�ʂƓ���
		drawBGLineCurrent(offGra,ecg_gl_y);

		//---------------------------���g���̈�̃O���t�̂̔w�i---------------------------//

		//�ڐ����  y������
		int y = height;
		//�F�̐��̊Ԋu
		int blue_margin_x = BG_LINE_MARGIN*5;

		//�ڐ����`��(0.0 ~ )  y������
		for(int i = BG_LINE_MARGIN; i < maxSpectrum ; i += BG_LINE_MARGIN){
			y = height - i;
			if(i % blue_margin_x == 0){
				//���ɒ[����[�܂Ő�������
				g.setColor(BG_LINE_COLOR2);
				g.drawLine(1,y,width,y);
				//0.5���ƂɐF�̐�������
				g.setColor(BG_STR_COLOR);
				g.drawLine(1, y, BG_LINE_LONG, y);
				//���l�𕶎��ŕ`��
				g.setColor(BG_STR_COLOR);
				g.drawString(i + "",BG_LINE_MARGIN,y);
				//�F��߂�
				g.setColor(BG_LINE_COLOR);
			}else{
				g.drawLine(1,y,BG_LINE_LONG,y);
			}
		}

		//X��������
		g.setColor(BG_LINE_COLOR);
		g.drawLine(0,spectrum_gl_y,width,spectrum_gl_y);

		//�i�C�L�X�g���g��
		//sone1201
		//int nyquist = BPMonitorManager.SAMPLE>>1;
		int nyquist = BPCalcTSK.SAMPLE>>1;

		//�F�̐��̊Ԋu
		int blue_margin_y = nyquist/5;
		if(blue_margin_y <= 30)blue_margin_y = 30;
		//�ڐ�����̊Ԋu
		int line_margin_y = blue_margin_y / 5;

		//�ڐ���� x������(���g����)
		for(int freq = 0; freq <= nyquist ; freq += line_margin_y){

			//�P�̖ڐ���łǂꂮ�炢�̎��g����\����
			int	dX = (freq*width)/nyquist;
			//Y��
			if(dX <= 0)dX = 1;

			if(freq % blue_margin_y == 0){
				//�[����[�܂ŏc�̐�������
				g.setColor(BG_LINE_COLOR2);
				g.drawLine(dX,spectrum_gl_y,dX,spectrum_gl_y-maxSpectrum);

				//�F�̐������Ԋu�ŕ`��
				if(freq == nyquist){
					//---�Ō㌩���Ȃ��Ȃ�̂ŏ����O�ɕ`��---//
					//�F�̐�������
					g.setColor(BG_STR_COLOR);
					g.drawLine(dX - 1, spectrum_gl_y, dX - 1, spectrum_gl_y - BG_LINE_LONG);
					//���g���𕶎���Ƃ��ĕ`��
					g.setColor(BG_STR_COLOR);
					g.drawString(freq + "", dX - FONT.stringWidth(freq +"")
							     , spectrum_gl_y - FONT_DECENT);
					//�F��߂��Ă���
					g.setColor(BG_LINE_COLOR);
				}else if(freq == 0){
					//--�ŏ������ɂ����Ȃ�̂ŏ�����ɕ`��--//
					g.setColor(BG_STR_COLOR);
					g.drawLine(dX ,spectrum_gl_y , dX ,spectrum_gl_y - BG_LINE_LONG);
					//���g���𕶎���Ƃ��ĕ`��
					g.setColor(BG_STR_COLOR);
					g.drawString(freq + "", dX + FONT.stringWidth(freq +"")
								 ,spectrum_gl_y - FONT_DECENT);
					//�F��߂��Ă���
					g.setColor(BG_LINE_COLOR);
				}else{
					//�ڐ���̏�Ɏ��g���𕶎���Ƃ��`��
					//�F�̐�������
					g.setColor(BG_STR_COLOR);
					g.drawLine(dX,spectrum_gl_y,dX,spectrum_gl_y-BG_LINE_LONG);
					//���g���𕶎���Ƃ��ĕ`��
					g.setColor(BG_STR_COLOR);
					g.drawString(freq + "", dX,spectrum_gl_y - FONT_DECENT);
					//�F��߂��Ă���
					g.setColor(BG_LINE_COLOR);
				}
			}else{
				//�ڐ����`��
				g.drawLine(dX,spectrum_gl_y, dX,spectrum_gl_y-BG_LINE_LONG);
			}
		}
	}

	/**
	 * �U���X�y�N�g���̕`��
	 * @param spectrum �X�y�N�g���̔z��
	 * @param y �`����y���W
	 */
	private void drawSpectrum(DojaGraphics g,double[] spectrum){
		//�U���X�y�N�g����`��
		for(int i = 0; i < spectrum.length -1; i++){
			g.drawLine(i,(int)spectrum[i],i+1,(int)spectrum[i+1]);
		}
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

	//================================���̑�===================================//

	/**
	 * x�������̏k���{����ύX
	 * @param xReductionRate x�������̏k���{��
	 */
	public void setXReductionRate(int xReductionRate){
		if(MAN_X_REDUCTION_RATE < xReductionRate
		 ||MIX_X_REDUCTION_RATE > xReductionRate )return;

		this.xReductionRate = xReductionRate;
	}

	/**
	 * x�������̏k���{���𓾂�
	 * @return x�������̏k���{��
	 */
	public int getXReductionRate() {
		return xReductionRate;
	}

	public void setEcgSpectrum(double[] val) {
		setSpectrum(val,ecgSpectrum);
	}

	/**
	 * �X�y�N�g�����X�V
	 * @param val  �X�y�N�g���̔z��
	 * @param dest �`��p�o�b�t�@
	 */
	public synchronized void setSpectrum(double val[],double dest[]){
		//�i�C�L�X�g���g���𒴂��Ȃ����g���т�`��o�b�t�@�ɂ����悤�ɒ���
		double xgain = (double)(val.length >> 1)/(double)dest.length;
		double max = val[1];
		//X�������̃}�b�s���O ���������͕\�����Ȃ��̂�1����
		dest[0] = 0;
		for(int i = 1; i < dest.length ; i++){
			dest[i] = val[(int)(i*xgain)];
			if(max < dest[i])max = dest[i];
		}
		//y���̔{��
		double ygain = (double)maxSpectrum/(double)max;
		//Max����Ƃ���Y���W�𐳋K��
		for(int i = 0; i < dest.length ; i++){
			dest[i] = (spectrum_gl_y-ygain*dest[i]);
		}
	}

	//=============================== �f�o�b�O�p=================================//
	String dbgMessage = "";
	String dbgMessage2 = "";
	String dbgMessage3 = "";
	String dbgMessage4 = "";

	public void drawDBG(String str){
		this.dbgMessage = str;
	}
	public void drawDBG2(String str){
		this.dbgMessage2 = str;
	}
	public void drawDBG3(String str){
		this.dbgMessage3 = str;
	}
	public void drawDBG4(String str){
		this.dbgMessage4 = str;
	}


	/**
	 * �\������
	 */
	public void setDisplay(){
		Display.getDisplay(midlet).setCurrent(this);
	}
}
