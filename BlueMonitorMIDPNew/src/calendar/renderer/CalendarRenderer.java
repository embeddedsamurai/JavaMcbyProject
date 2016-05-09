package calendar.renderer;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import calendar.util.CalendarColor;
import calendar.util.CalendarUtil;

import util.DojaFont;
import util.DojaGraphics;

public class CalendarRenderer {

	//================================�萔====================================//
		
	//�t�H���g
	/** ���t�H���g */
	private static final DojaFont sFONT = DojaFont.getFont(DojaFont.SIZE_SMALL);
	/** ��t�H���g */
	private static final DojaFont mFONT = DojaFont.getFont(DojaFont.SIZE_MEDIUM);

	//���
	private static final int STATE_SELECT_DAY = 0;
	private static final int STATE_SELECT_MONTH = 1;
	
	/** �J�����_�[�g�̏㕔��Y���W */
	private final int calendarY;
	/** �T�`��g�㕔��Y���W */
	private final int weekY;
	/** ������9����  (�� �� �� �� �� �� �� �y ��) */
	private final int sep;
	/** �g�̑傫�� */
	private final int size;
	/** �g�O���̗]��(��������A��~�y�܂ł̕����������c��) */
	private final int outmargin;
	/** �J�����_�[�g�ƊO�g�̗]�� */
	private final int margin = 10;


	//================================�ϐ�====================================//
	
	//�摜
	private static Image CHECK_IMG = null;
	private static Image LEFT_ARROW_IMG = null;
	private static Image RIGHT_ARROW_IMG = null;
	private static Image RIGHT_ARROW_IMG_2 = null;

	/** ��� */
	private int state = STATE_SELECT_DAY;
	/** �摜 */
	private Image storeImg = null;
	/** �t�H�[�J�X�����邩�ǂ��� */
	private boolean focus = true;
	
	/** �J�����_�[��̓��̃C���f�b�N�X */
	private int curDay = 0;

	/** �� */
	private int width = 0;
	/** ���� */
	private int height = 0;

	/**���݂̔N */
	private int curYear = 0;
	/**���݂̌� */
	private int curMonth = 0;

	/**�`��ς݂̔N */
	private int drawnYear = 0;
	/**�`��ς݂̌� */
	private int drawnMonth = 0;
	
	//================================�֐�====================================//
	/**
	 * �R���X�g���N�^
	 * @param width  ��
	 * @param height ����
	 */
	public CalendarRenderer(int width, int height) {
		// ���j�^�̃T�C�Y��ݒ�
		this.width = width;
		this.height = height;
				
 
		// ������9����  (�� �� �� �� �� �� �� �y ��)
		sep  = width/9; 
		// �g�̑傫��
		size = sep*7;
		// �J�����_�[�g�̏㕔��Y���W
		calendarY = height - (size+(sep>>1));
		// �g�O���̗]��(��������A��~�y�܂ł̕����������c��)
		outmargin = (width-size)>>1;
		// �T�`��g�㕔��Y���W
		weekY = calendarY - sep - margin;

		createCheckImg();
		createArrowImg();

		// ���݂̔N�������擾
		int[] info = CalendarUtil.calcTimeInfo(System.currentTimeMillis());
		curYear = info[0];
		curMonth = info[1];
		curDay = info[2] - 1;

		//�摜�̓ǂݍ���
		storeImg = Image.createImage(width, height);
	}

	//==============================�摜�̐���==================================//

	/**
	 * '��'�摜�̐��� 
	 */
	private void createCheckImg() {
		// �`�悷�镶��
		final char c = '��';

		final Font font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
		// �����̏c���̗]��
		final int hcharmargin = ((sep - font.getHeight())>>1) + 1;
		// �����̉����̗]��
		final int wcharmargin = ((sep - font.stringWidth(c+""))>>1);

		// �ꎞ�`��p�C���[�W�̍쐬
		Image naImg = Image.createImage(sep + 2, sep + 2);
		Graphics nag = naImg.getGraphics();

		// �t�H���g�̐ݒ�
		nag.setFont(font);

		// �F�̐ݒ�
		nag.setColor(CalendarColor.COLOR_RED);

		// �~�̕`��
		int x = 0;
		int y = 0;
		int w = sep;
		int h = sep;
		nag.drawArc(x  ,y  , w, h, 0, 360);
		nag.drawArc(x  ,y+1, w, h, 0, 360);
		nag.drawArc(x+1,y  , w, h, 0, 360);
		nag.drawArc(x+1,y+1, w, h, 0, 360);

		// �����̕`��
		x = wcharmargin;
		y = hcharmargin;
		int position = (Graphics.TOP | Graphics.LEFT);
		nag.drawChar(c, x  , y  , position);
		nag.drawChar(c, x+1, y  , position);
		nag.drawChar(c, x  , y+1, position);
		nag.drawChar(c, x+1, y+1, position);

		// ���߃C���[�W�̍쐬
		int[] rgb = new int[(sep + 2) * (sep + 2)];
		naImg.getRGB(rgb, 0, sep + 2, 0, 0, sep + 2, sep + 2);
		int ndColor = rgb[0];
		for (int i = 0; i < rgb.length; i++) {
			if (rgb[i] == ndColor)
				rgb[i] = ((rgb[i] & 0xFFFFFF) | 0x00 << 24);
		}
		CHECK_IMG = Image.createRGBImage(rgb, sep + 2, sep + 2, true);
	}

	/**
	 * ���̕`��
	 */
	private void createArrowImg() {

		// �ꎞ�`��p�C���[�W�̍쐬
		Image naImg1 = Image.createImage(sep, sep);
		Graphics nag1 = naImg1.getGraphics();		
		Image naImg2 = Image.createImage(sep, sep);
		Graphics nag2 = naImg2.getGraphics();
		Image naImg3 = Image.createImage(sep, sep);
		Graphics nag3 = naImg3.getGraphics();
		
		// �O�p�`�̒��_
		int x1 = 0;
		int y1 = sep/2;
		int x2 = sep;
		int y2 = 0;
		int x3 = sep;
		int y3 = sep;
		
		// �O�p�`�̕`��
		nag1.setColor(CalendarColor.COLOR_MOTH_GREEN);
		nag1.fillTriangle(x1, y1, x2, y2, x3, y3);		
		// ���߃C���[�W�̍쐬
		int[] rgb1 = new int[sep * sep];
		naImg1.getRGB(rgb1, 0, sep, 0, 0, sep, sep);
		int ndColor1 = rgb1[0];
		for (int i = 0; i < rgb1.length; i++) {
			if (rgb1[i] == ndColor1)rgb1[i] = ((rgb1[i] & 0xFFFFFF) | 0x00 << 24);
		}
		LEFT_ARROW_IMG = Image.createRGBImage(rgb1, sep, sep, true);
		
		// �O�p�`�̒��_
		x1 = sep;
		y1 = sep/2;
		x2 = 0;
		y2 = 0;		
		x3 = 0;
		y3 = sep;
		
		nag2.setColor(CalendarColor.COLOR_MOTH_GREEN);
		nag2.fillTriangle(x1, y1, x2, y2, x3, y3);
		int[] rgb2 = new int[sep * sep];
		naImg2.getRGB(rgb2, 0, sep, 0, 0, sep, sep);
		int ndColor2 = rgb2[rgb2.length-1];
		for (int i = 0; i < rgb2.length; i++) {
			if (rgb2[i] == ndColor2)
				rgb2[i] = ((rgb2[i] & 0xFFFFFF) | 0x00 << 24);
		}
		RIGHT_ARROW_IMG = Image.createRGBImage(rgb2, sep, sep, true);
		
		nag3.setColor(CalendarColor.COLOR_LIGHT_GRAY);
		nag3.fillTriangle(x1, y1, x2, y2, x3, y3);
		int[] rgb3 = new int[sep * sep];
		naImg3.getRGB(rgb3, 0, sep, 0, 0, sep, sep);
		int ndColor3 = rgb3[rgb3.length-1];
		for (int i = 0; i < rgb3.length; i++) {
			if (rgb3[i] == ndColor3)
				rgb3[i] = ((rgb3[i] & 0xFFFFFF) | 0x00 << 24);
		}
		RIGHT_ARROW_IMG_2 = Image.createRGBImage(rgb3, sep, sep, true);
	}

	//==============================�`�揈��==================================//
	

	/**
	 * �`�揈��
	 * @param g Graphics�I�u�W�F�N�g
	 */
	public void draw(DojaGraphics g) {

		// �J�����_�[��`��
		if (drawnYear == curYear && drawnMonth == curMonth) {
			//�`�撆�̔N�A���ƌ��݂̔N�A������v�����Ƃ�
			g.drawImage(storeImg, 0, 0);
		} else {
			//�`�撆�̔N�A���ƌ��݂̔N�A������v���Ȃ��Ƃ�
			DojaGraphics sg = new DojaGraphics(storeImg.getGraphics(),null);
			//�摜���쐬���Ȃ���
			sg.setColor(CalendarColor.COLOR_WHITE);
			sg.clearRect(0,0, width, height);
			drawCalendar(sg, curYear, curMonth);
			//�摜��`��
			g.drawImage(storeImg, 0, 0);
			//�`�撆�̔N�A�����X�V
			drawnYear = curYear;
			drawnMonth = curMonth;
		}

		// �v�����Ɉ������
		boolean[] mesurementDays = CalendarUtil.getMesurementDays(curYear, curMonth);
		drawMesurementDays(g, mesurementDays, curYear, curMonth);

		// �I��g��`��
		drawSelector(g, curYear, curMonth);
	}
		
	
	/**
	 * �J�����_�[�̕`��
	 * @param g Graphics�I�u�W�F�N�g
	 * @param year  �N
	 * @param month ��
	 */
	private void drawCalendar(DojaGraphics g, int year, int month) {
		// ���݂̌����ǂ����`�F�b�N
		int today = -1;
		int[] info = CalendarUtil.calcTimeInfo(System.currentTimeMillis());
		if (info[0] == year && info[1] == month) {
			today = info[2];
		}
		// �w�i��`��
		drawBGLine(g);
		// �j����`��
		drawWeeks(g);
		// ����`��
		drawDays(g, year, month, today);
		// ����`��
		drawTop(g, year, month);
	}

	/**
	 * �w�i�̕`��
	 * @param g
	 */
	private void drawBGLine(DojaGraphics g) {

		// �F�̐ݒ�
		g.setColor(CalendarColor.COLOR_LIGHT_GRAY);

		// �O�g�̕`��		
		int x = outmargin;
		int y = calendarY;
		g.drawRect(x  , y, size, size);
		g.drawRect(x+1, y, size, size);
		g.drawRect(x  , y, size, size);
		g.drawRect(x+1, y, size, size);

		// ���ڂ̕`��
		int x1 = 0;
		int y1 = 0;
		for (int i = 1; i < 7; i++) {
			x  = outmargin;
			y  = (calendarY + sep*i);
			x1 = (outmargin + size);
			y1 = (calendarY + sep*i);
			
			g.drawLine(x,y,x1,y1);
			
			x  = (outmargin + i*sep);
			y  = calendarY;
			x1 = (outmargin + i*sep);
			y1 = calendarY + size;
			
			g.drawLine(x,y,x1,y1);
		}

		// ��d���̕`��
		g.drawLine(outmargin        , (calendarY + sep - 2),
				  (outmargin + size), (calendarY + sep - 2));
	}

	/**
	 * �T�̕`��
	 * @param g Graphics�I�u�W�F�N�g
	 */
	private void drawWeeks(DojaGraphics g) {
		// �j��
		final char[] weeks = {'��','��','��','��','��','��','�y'};

		// �t�H���g�̐ݒ�
		g.setFont(sFONT);
		
		// �j���̕`��
		for (int i = 0; i < 7; i++) {			
			//���j�͐ԐF
			if (i == 0)g.setColor(CalendarColor.COLOR_LIGHT_RED);
			//�����͊D�F
			if (i == 1)g.setColor(CalendarColor.COLOR_GRAY);			
			//�y�j�͐F
			if (i == 6)g.setColor(CalendarColor.COLOR_LIGHT_BLUE);
			
			//�����̕`��
			int x = (outmargin + i*sep    + (sep>>1));
			int y = (calendarY + (sep>>1) + (sFONT.getAscent()>>1));
			
			g.drawString(weeks[i]+"",x,y);					    			
			g.drawString(weeks[i]+"",x+1,y);								
			g.drawString(weeks[i]+"",x,y+1);			
			g.drawString(weeks[i]+"",x+1,y+1);
		}
	}

	/**
	 * ����`��
	 * @param g     Graphics
	 * @param year  �N
	 * @param month ��
	 * @param today �����̓�
	 */
	private void drawDays(DojaGraphics g, int year, int month, int today) {
		
		// �t�H���g�̐ݒ�
		g.setFont(sFONT);
		// 1���̗j�����擾
		int start = CalendarUtil.getWeek(year, month, 1);
		// ���̍ő�������擾
		int lastday = CalendarUtil.getLastDay(year, month);
		
		// ���̕`��
		for (int i = 0; i < lastday; i++) {
			//��
			String day = Integer.toString(i + 1);
			//���̃C���f�b�N�X
			int xIndex = (i+start)%7;
			//�c�̃C���f�b�N�X
			int yIndex = (i+start)/7+1;
			
			if ((i+1) == today){
				//�����͗΂ŕ`��
				g.setColor(CalendarColor.COLOR_LIGHT_GREEN);
			}else if (xIndex == 0){
				//���j�͐Ԃŕ`��
				g.setColor(CalendarColor.COLOR_LIGHT_RED);
			}else if (xIndex == 6){
				//�y�j�͐ŕ`��
				g.setColor(CalendarColor.COLOR_LIGHT_BLUE);
			}else{
				//���̑��͍��ŕ`��
				g.setColor(CalendarColor.COLOR_BLACK);
			}
			
			int x = (outmargin + xIndex*sep + (sep>>1));
			int y = (calendarY + (sep>>1)+ yIndex*sep + (sFONT.getAscent()>>1));
			
			//������`��			
			g.drawString(day,x,y);
			g.drawString(day,x+1,y);
			g.drawString(day,x,y+1);
			g.drawString(day,x+1,y+1);
		}
	}

	/**
	 * �㕔��`��
	 * @param g     Graphics�I�u�W�F�N�g
	 * @param year  �N 
	 * @param month ��
	 */
	private void drawTop(DojaGraphics g, int year, int month) {

		// �����Ԃ̗]���i��)
		final int strMargin = 5;
		
		// �O�g�̕`��
		g.setColor(CalendarColor.COLOR_LIGHT_GRAY);
		int x = outmargin + sep;
		int y = weekY;
		g.drawRect(x  , y  ,sep*5, sep);
		g.drawRect(x+1, y  ,sep*5, sep);
		g.drawRect(x  , y+1,sep*5, sep);
		g.drawRect(x+1, y+1,sep*5, sep);
		
		// �P�ʂ̕`��,�N���̕`��
		final String mstr = Integer.toString(month);
		final String ystr = Integer.toString(year);
		final String mchar = "��";
		final String ychar = "�N";
		
		//�N�̕`��		
		y = y + (sep>>1) + (sFONT.getAscent()*2/3);
		x = x + ((mFONT.stringWidth(ystr))>>1) + strMargin;
		g.setFont(mFONT);		
		g.setColor(CalendarColor.COLOR_WATER_BLUE);
		g.drawString(ystr,x,y);		
		
		//�N���̂��̂̕`��
		x = x + ((mFONT.stringWidth(ystr) + sFONT.stringWidth(ychar))>>1) + strMargin;
		g.setFont(sFONT);
		g.setColor(CalendarColor.COLOR_GRAY);		
		g.drawString(ychar,x,y);
		
		//���̕`��
		x = x + ((sFONT.stringWidth(ychar) + mFONT.stringWidth(mstr))>>1) + strMargin;
		g.setFont(mFONT);
		g.setColor(CalendarColor.COLOR_DARK_RED);
		g.drawString(mstr,x,y);
		
		//�����̂��̂̕`��
		x = x + ((mFONT.stringWidth(mstr) + sFONT.stringWidth(mchar))>>1) + strMargin;
		g.setFont(sFONT);
		g.setColor(CalendarColor.COLOR_GRAY);
		g.drawString(mchar,x,y);		

	    // ���݂̔N�������擾
	    int[] info = CalendarUtil.calcTimeInfo(System.currentTimeMillis());
	    
		// ���̕`��(��)
		x = outmargin + sep - LEFT_ARROW_IMG.getWidth() - strMargin;
		y = weekY;
		g.drawImage(LEFT_ARROW_IMG,x,y);			
		
		// ���̕`��(�E)
		x = outmargin + size - RIGHT_ARROW_IMG.getWidth() + strMargin;
		if (curYear >= info[0] && curMonth >= info[1]) {
			// ���̌�������Ƃ�
			g.drawImage(RIGHT_ARROW_IMG_2,x,y);			
		}else{
			// ���̌����Ȃ��Ƃ�
			g.drawImage(RIGHT_ARROW_IMG,x,y);
		}
	}

	/**
	 * �Z���N�^�̕`��
	 * @param g    Graphics�I�u�W�F�N�g
	 * @param year  �N
	 * @param month ��
	 */
	private void drawSelector(DojaGraphics g, int year, int month) {
		//�t�H�[�J�X���Ȃ���Ε`�悵�Ȃ�
		if (!focus)	return;

		int x = 0,y = 0,w = 0,h = 0;
		g.setColor(CalendarColor.COLOR_RED);
		if (state == STATE_SELECT_DAY) {
			// 1���̗j�����擾
			int start = CalendarUtil.getWeek(year, month, 1);

			// �`��ʒu������
			int windex = (start + curDay)%7;
			int hindex = (start + curDay)/7+1;
			
			x = outmargin + windex * sep;
			y = calendarY + hindex * sep;
			w = h = sep;
			
		} else if (state == STATE_SELECT_MONTH) {
			// �`��ʒu������
			x = outmargin + sep;
			y = weekY;
			w = sep*5;
			h = sep;							
		}
		
		// �g�̕`��
		g.drawRect(x,  y  ,w,h);
		g.drawRect(x+1,y  ,w,h);
		g.drawRect(x,  y+1,w,h);
		g.drawRect(x+1,y+1,w,h);
	}
	
	/**
	 * �v�����Ɉ������
	 * @param g Graphics�I�u�W�F�N�g
	 * @param mesurementDays ���łɌv���������ǂ����̃t���O�̔z��
	 * @param year  �N
	 * @param month ��
	 */
	private void drawMesurementDays(DojaGraphics g, boolean[] mesurementDays,int year, int month) {
	
		// 1���̗j�����擾
		int start = CalendarUtil.getWeek(year, month, 1);

		for (int i = 0; i < mesurementDays.length; i++) {
			if (mesurementDays[i]) {
				//�g�̉��̃C���f�b�N�X
				int xIndex = (i + start) % 7;
				//�g�̏c�̂̃C���f�b�N�X
				int yIndex = (i + start)/7+1;	
				//�`��J�nx���W
				int x = (outmargin + xIndex*sep);
				//�`��J�ny���W
				int y = (calendarY + yIndex*sep);
				//�`��
				g.drawImage(CHECK_IMG,x,y);						  
			}
		}
	}

	//================================���̑�===================================//
	
	public int getCurrentYear() {
		return curYear;
	}
	public int getCurrentMonth() {
		return curMonth;
	}
	public int getDayIndex() {
		return curDay;
	}
	
	public long getCurrentDate(){
		System.out.println(curYear+ " " + curMonth + " " + curDay);
		return CalendarUtil.getCurrentTime(curYear,curMonth,curDay);
	}
	
	public boolean isSelectDay() {
		return (state == STATE_SELECT_DAY);
	}
	
	public void setDayIndex(int index) {
		curDay = index;
	}

	public void setLastDayIndex() {
		curDay = CalendarUtil.getLastDay(curYear, curMonth) - 1;
	}

	public void setStateDay() {
		state = STATE_SELECT_DAY;
	}

	public void setStateMonth() {
		state = STATE_SELECT_MONTH;
	}

	public void setCurrentDay(int y, int m, int d) {
		curYear = y;
		curMonth = m;
		curDay = d;
	}		

	public void addMesurementDay(long time) {
		int[] info = CalendarUtil.calcTimeInfo(time);
		CalendarUtil.addMesurementDay(info[0], info[1], info[2]);
	}

	public void removeMesurementDay(long time) {
		int[] info = CalendarUtil.calcTimeInfo(time);
		CalendarUtil.removeMesurementDay(info[0], info[1], info[2]);
	}

	public void setFocus(boolean focus) {
		this.focus = focus;
	}
	

	/**
	 * �����œn���������`�F�b�N����Ă��邩�ǂ���
	 * @param index ��
	 * @return
	 */
	public boolean isCheckedDay(int index) {
		boolean[] mesurementDays = CalendarUtil.getMesurementDays(curYear, curMonth);
		return mesurementDays[index];
	}
	
	/**
	 * �I������Ă���g�����
	 */
	public void up() {
		if (state == STATE_SELECT_DAY) {
			if ((curDay - 7) < 0) {
				state = STATE_SELECT_MONTH;
			} else {
				curDay -= 7;
			}
		} else if (state == STATE_SELECT_MONTH) {
			state = STATE_SELECT_DAY;
			curDay = CalendarUtil.getLastDay(curYear, curMonth) - 1;
		}
	}

	/**
	 * �I������Ă���g������ 
	 */
	public void down() {
		if (state == STATE_SELECT_DAY) {
			int lastday = CalendarUtil.getLastDay(curYear, curMonth);
			if ((curDay + 7) >= lastday) {
				state = STATE_SELECT_MONTH;
			} else {
				curDay += 7;
			}
		} else if (state == STATE_SELECT_MONTH) {
			state = STATE_SELECT_DAY;
			curDay = 0;
		}
	}

	/**
	 * �I������Ă���g���E��
	 */
	public void right() {
		if (state == STATE_SELECT_DAY) {
			int lastday = CalendarUtil.getLastDay(curYear, curMonth);
			curDay++;
			if (curDay >= lastday) {
				int[] info = CalendarUtil.calcTimeInfo(System.currentTimeMillis());
				if (curYear >= info[0] && curMonth >= info[1]) {
					curDay--;
					return;
				}
				curDay = 0;
				curMonth += 1;
				if (curMonth > 12) {
					curYear++;
					curMonth = 1;
				}
			}
		} else if (state == STATE_SELECT_MONTH) {

			int[] info = CalendarUtil.calcTimeInfo(System.currentTimeMillis());
			if (curYear >= info[0] && curMonth >= info[1]) {
				return;
			}
			curMonth += 1;
			if (curMonth > 12) {
				curYear++;
				curMonth = 1;
			}
		}
	}

	/**
	 * �I������Ă���g������
	 */
	public void left() {
		if (state == STATE_SELECT_DAY) {
			curDay--;
			if (curDay < 0) {
				curMonth -= 1;
				if (curMonth < 1) {
					curYear--;
					curMonth = 12;
				}
				curDay = CalendarUtil.getLastDay(curYear, curMonth) - 1;
			}
		} else if (state == STATE_SELECT_MONTH) {
			curMonth -= 1;
			if (curMonth < 1) {
				curYear--;
				curMonth = 12;
			}
		}
	}

}
