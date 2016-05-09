package calendar.renderer;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import calendar.CalendarManager;
import calendar.util.CalendarColor;
import calendar.util.CalendarUtil;

import util.DojaFont;
import util.DojaGraphics;

public class WeekListRenderer {
	
	//====================================�萔====================================//
	//�t�H���g
	/** ���t�H���g */
	private static final DojaFont sFONT = DojaFont.getFont(DojaFont.SIZE_SMALL);
	/** ��t�H���g */
	private static final DojaFont mFONT = DojaFont.getFont(DojaFont.SIZE_MEDIUM);
	
	//���
	/** �T�I����� */
	private static final int STATE_SELECT_WEEK = 0;
	/** ���I����� */ 
	private static final int STATE_SELECT_MONTH = 1;
	
	/** 1�T�Ԃ̓���*/
	private static final int NUM_OF_DAY_IN_WEEK = 7;
	/** 1�N�̌���*/
	private static final int NUM_OF_MONTH_IN_YEAR = 12;
	
	/** ������9����  (�� �� �� �� �� �� �� �y ��) */
	private final int sep;
	/** �g�̑傫�� */
	private final int size;
	/** �T�̔���̕� */
	private final int boxWidth;
	/** BOX�̍��� (+3�͏㉺�̗]��) */
	private final int boxHeight;
	/** ���`��g�㕔��Y���W */
	private final int monthY;		

	//===================================�ϐ�====================================//
	/** ���݂̏�� */
	private int state = STATE_SELECT_WEEK;
		
	/** �T�\���{�b�N�X���i�[����z�� */
	private Image[] weekBox = new Image[6];
	
	/** ���摜 */
	private static Image LEFT_ARROW_IMG = null;
	private static Image RIGHT_ARROW_IMG = null;
	private static Image RIGHT_ARROW_IMG_2 = null;
	
	/** ��ʕ� */
	private int width = 0;
	/** ��ʍ��� */
	private int height = 0;
	
	/** ���ݕ`�撆�̔N */
	private int curYear = 0;
	/** ���ݕ`�撆�̌� */
	private int curMonth = 0;
	/** ���ݕ`�撆�̓� */
    private int curDay = 0;
	
	/** �I�𒆂̏T */
	private int weekIndex = 0;
	
	/** �t�H�[�J�X���ł��邩 */
	private boolean focus = false;
	
	/** �t�H�[�J�X��ON/OFF��؂�ւ��邩�ǂ��� */
	private boolean changeFocus = false;
	
	//====================================�֐�====================================//
	/**
	 * �R���X�g���N�^
	 */
	public WeekListRenderer(int width,int height){
		
		// ���j�^�̃T�C�Y���擾
		this.width = width;
		this.height = height;
		
		// ������9����  (�� �� �� �� �� �� �� �y ��)
		sep  = width/9; 
		// �g�̑傫��
		size = sep*7;		
		//BOX�̃T�C�Y 
		boxWidth = sep*5;		
		//BOX�̍��� (+3�͏㉺�̗]��)
		boxHeight = sep;
		// ���`��g�㕔��Y���W
		monthY = height - (size+(sep>>1)) - sep - 10;
		
		// �`��C���[�W�̍쐬
		createWeekBoxImg();
		createArrowImg();
		
		// ���݂̎��Ԃ��擾
		int[] info = CalendarUtil.calcTimeInfo(System.currentTimeMillis());
		curYear  = info[0];
		curMonth = info[1];
        curDay   = info[2];
		weekIndex = CalendarUtil.getCurrentWeekNum(info[0], info[1], info[2]);		
	}
	
	/**
	 * �`�揈�����s��
	 * @param g
	 */
	public void draw(DojaGraphics g){			
		//�T�\��BOX��`��
		drawWeekBox(g, curYear,curMonth);
		//���\����ʂ�`��
		drawTop(g,curYear,curMonth);		
		//�I��g��`��
		drawSelector(g);
	}
	
	/**
	 * ���
	 */
	public void up(){
		if(state == STATE_SELECT_WEEK){	
			//�T�I����
			if(weekIndex == 0){
				//0�Ȃ���I�����[�h��
				state = STATE_SELECT_MONTH;
				weekIndex = -1;
			} else{
				//�O�̏T��
				weekIndex--;
                curDay -= 7;
			}
		} else if(state == STATE_SELECT_MONTH){	
			//���I����
			changeFocus = true;
		}
	}
	
	/**
	 * ����
	 */
	public void down(){		
		if(state == STATE_SELECT_WEEK){	
			//�T�I����		
			// �`�悷�錎�̏T�����Z�o����
			int weekNum = CalendarUtil.getMonthWeekNum(curYear,curMonth);
			
			if(weekIndex+1 > weekNum-1){
				//�t�H�[�J�X��؂�ւ���
				changeFocus = true;
			}else{
				//�T�̃C���f�b�N�X�𑝂₷
				weekIndex++;
				curDay += NUM_OF_DAY_IN_WEEK;
			}			
		}else if(state == STATE_SELECT_MONTH){	
			//���I����
			//�T�I�����[�h��
			state = STATE_SELECT_WEEK;
			weekIndex = 0;
			curDay = 1;
		}
	}
	
	/**
	 * ����
	 */
	public void left(){		
		if(state == STATE_SELECT_MONTH){
			//���I����
			curMonth -= 1;
			if(curMonth < 1){
				curYear--;
				curMonth = NUM_OF_MONTH_IN_YEAR;
			}
		}
	}
	
	/**
	 * �E��
	 */
	public void right(){
		if(state == STATE_SELECT_MONTH){	
			//���I����
			int[] info = CalendarUtil.calcTimeInfo(System.currentTimeMillis());
			if(curYear >= info[0] && curMonth >= info[1]){
				//����ȏ�\�����錎���Ȃ����͉������Ȃ�
				return;
			}
			//��������₷
			curMonth += 1;
			if(curMonth > NUM_OF_MONTH_IN_YEAR){
				//���N��
				curYear++;
				curMonth = 1;
			}
		}
	}
	
	/**
	 * �t�H�[�J�X�̏�Ԃ�ݒ肷��
	 *
	 * @param focus �t�H�[�J�X�̏��
	 * @param key ��ԑ@�ۑO�ɉ������L�[
	 */
	public void setFocus(boolean focus, int key){
		this.focus = focus;
		if(key == CalendarManager.UP_REQ){
			//����������Ƃ�
			weekIndex = CalendarUtil.getMonthWeekNum(curYear,curMonth) - 1;
			//�T���[�h�ֈڍs
			state = STATE_SELECT_WEEK;			
		} else if(key == CalendarManager.DOWN_REQ){
			//�����������Ƃ�
			//�����[�h�ֈڍs
			weekIndex = -1;
			state = STATE_SELECT_MONTH;			
		}
	}
	
	/**
	 * �t�H�[�J�X���ω��������ǂ���
	 * @return true:�t�H�[�J�X���ω����� false:�t�H�[�J�X���ς���ĂȂ�
	 */
	public boolean isChangeFocus(){		
		if(changeFocus){
			changeFocus = false;
			return true;
		}
		return false;
	}
	
	/**
	 * �T�\��BOX���쐬����
	 */
	private void createWeekBoxImg(){

		//�扽�T����\��������̒���
		final Font font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
		final int weekStrlen = font.stringWidth("��5�T");

		for(int i=0; i < NUM_OF_DAY_IN_WEEK-1; i++){
			//�`��p��C���[�W�̍쐬
			weekBox[i] = Image.createImage(boxWidth+weekStrlen+4, boxHeight+2);
			Graphics wg = weekBox[i].getGraphics();
			
			//�O�g�̕`��
			int x = weekStrlen;
			int y = 0;
			wg.setColor(CalendarColor.COLOR_LIGHT_GRAY);
			wg.drawRect(x+2,  y  , boxWidth, boxHeight);
			wg.drawRect(x+2,  y+1, boxWidth, boxHeight);
			wg.drawRect(x+2+1,y  , boxWidth, boxHeight);
			wg.drawRect(x+2+1,y+1, boxWidth, boxHeight);
			
			// �T�ԍ��̕`��
			String dstr = "��" + (i+1) + "�T";
			x = 0;
			y = 2; //�O�g�̑���
			wg.setFont(font);
			wg.setColor(CalendarColor.COLOR_WATER_BLUE);
			wg.drawString(dstr, x  , y  , Graphics.TOP|Graphics.LEFT);
			wg.drawString(dstr, x  , y+1, Graphics.TOP|Graphics.LEFT);
			wg.drawString(dstr, x+1, y  , Graphics.TOP|Graphics.LEFT);
			wg.drawString(dstr, x+1, y+1, Graphics.TOP|Graphics.LEFT);			
		}
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
	
	/**
	 * �T�\��BOX��`��
	 *
	 * @param g �`��Ώۂ�Graphics�N���X
	 * @param year �`�悷��N
	 * @param month �`�悷�錎
	 *
	 */
	private void drawWeekBox(DojaGraphics g, int year, int month){
		
		//�g�O���̗]���̍��v
		final int outmargin = (width-weekBox[0].getWidth())>>1;

		//�c�����̕`��J�n���W
		final int hstart = height-(sep*7+(sep>>1));		
		//�扽�T����\��������̒���
		final int weekStrlen = sFONT.stringWidth("��5�T");		
		//�`�悷�錎�̏T�����Z�o����
		final int weekNum = CalendarUtil.getMonthWeekNum(year,month);
		
		//�g���ɕ`�悷�����`�悷��Ƃ��̍����̗]��
		final int strMargin = 5;
		//���Ԃ̗]��(�c)
		final int boxHMargin = 10;
		
		for(int i=0; i < weekNum; i++){			
			//�`�悷��T�̓��̃��X�g���Z�o����
			final int[] days = CalendarUtil.getWeekDays(year, month, i+1);			
			//�T�\��BOX�̕`��(�]���̒[����`��)
			g.drawImage(weekBox[i],outmargin, hstart+i*(boxHeight+boxHMargin));
			
			//�ŏ��̓�
			final String firstDay = Integer.toString(days[0]);
			//�Ō�̓�
			final String lastDay  = Integer.toString(days[days.length-1]);			
			final String uday = "��";
			final String gap = "�`";
			
			//�����Ԃ̗]��
			final int space = 3;
			
			// �`��J�n�ʒu (���[�̗]�� + �扽�T����\��������)
			int startX = outmargin+weekStrlen+strMargin;				
			int startY = hstart+boxHMargin+ 
			            ((weekBox[i].getHeight() - (sFONT.getAscent()+sFONT.getDescent()))>>1)
			            + (sFONT.getAscent()>>1);
			
			//------------------------�����܂ł̕���-------------------------//
			//�ΐF�ɕύX
			g.setColor(CalendarColor.COLOR_MOTH_GREEN);
			g.setFont(sFONT);
			
			//-------------�ŏ��̓���`��-------------//			
			int strLen = sFONT.stringWidth(firstDay);
			if(days[0] >= 10){
				//����2�P�^�̂Ƃ�
				startX += space;
			}else{
				//����1�P�^�̂Ƃ�
				startX += (space<<1);	
			}
			//�`��J�n���W
			int x = startX + (strLen>>1);
			int y = startY+i*(boxHeight+boxHMargin);
			//���̕`��J�n���W����ׂ̈̏���
			startX += (strLen) + space;;
			//�`�揈��
			g.drawString(firstDay,x  ,y  );
			g.drawString(firstDay,x+1,y  );
			g.drawString(firstDay,x  ,y+1);
			g.drawString(firstDay,x+1,y+1);
			
			//�D�F�ɕύX
			g.setColor(CalendarColor.COLOR_GRAY);
			g.setFont(sFONT);
			
			//---------------"��"��`��---------------//			
			strLen = sFONT.stringWidth(uday);
			//�`��J�n���W
			x = startX + (strLen>>1);
			//���̕`��J�n���W����ׂ̈̏���
			startX += (strLen) + space;
			//�`�揈��
			g.drawString(uday,x  ,y  );
			g.drawString(uday,x+1,y  );
			g.drawString(uday,x  ,y+1);
			g.drawString(uday,x+1,y+1);
			
			//------------------------�`����̕���-------------------------//
			
			if(days.length > 1){	
				//���̌��̏T�̓�����2���ȏ�̏ꍇ
				
				//���F�ɕύX
				g.setColor(CalendarColor.COLOR_BLACK);
				g.setFont(sFONT);
								
				//---------------"�`"��`��---------------//
				strLen = sFONT.stringWidth(gap);
				//�`��J�n���W
				x = startX + (strLen>>1);
				//���̕`��J�n���W����ׂ̈̏���
				if(days[days.length-1] >= 10){
					//����2�P�^�̂Ƃ�
					startX += space + (strLen);
				}else{
					//����1�P�^�̂Ƃ�
					startX += (space<<1) + (strLen);	
				}				
				//�`�揈��
				g.drawString(gap,x  ,y  );
				g.drawString(gap,x+1,y  );
				g.drawString(gap,x  ,y+1);
				g.drawString(gap,x+1,y+1);
				
				//�ΐF
				g.setColor(CalendarColor.COLOR_MOTH_GREEN);
				g.setFont(sFONT);
				
				//---------------����`��---------------//
				strLen = sFONT.stringWidth(lastDay);
				//�`��J�n���W
				x = startX + (strLen>>1);
				//���̕`��J�n���W����ׂ̈̏���
				startX += (strLen) + space;
				//�`�揈��
				g.drawString(lastDay,x  ,y  );
				g.drawString(lastDay,x+1,y  );
				g.drawString(lastDay,x  ,y+1);
				g.drawString(lastDay,x+1,y+1);
				
				//�D�F
				g.setColor(CalendarColor.COLOR_GRAY);
				g.setFont(sFONT);
				
				//---------------"��"��`��---------------//
				strLen = sFONT.stringWidth(uday);
				//�`��J�n���W
				x = startX + (strLen>>1);
				//�`�揈��
				g.drawString(uday,x  ,y  );
				g.drawString(uday,x+1,y  );
				g.drawString(uday,x  ,y+1);
				g.drawString(uday,x+1,y+1);
			}
		}
	}

	/**
	 * �㕔��`��
	 * @param g     Graphics�I�u�W�F�N�g
	 * @param year  �N 
	 * @param month ��
	 */
	private void drawTop(DojaGraphics g, int year, int month) {

		// �g�O���̗]�� (��������A��~�y�܂ł̕����������c��)	
		final int outmargin = (width-size)>>1;
		// �����Ԃ̗]���i��)
		final int strMargin = 5;
		
		// �O�g�̕`��
		g.setColor(CalendarColor.COLOR_LIGHT_GRAY);
		int x = outmargin + sep;
		int y = monthY;
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
		y = monthY;
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
	 */
	private void drawSelector(DojaGraphics g) {
		//�t�H�[�J�X���Ȃ���Ε`�悵�Ȃ�		
		if (!focus)	return;
		
		//�扽�T����\��������̒���
		final int weekStrlen = sFONT.stringWidth("��5�T");
		//�]��
		final int margin = 2;

		//���Ԃ̗]��(�c)
		final int boxHMargin = 10;
		int x = 0,y = 0,w = 0,h = 0;
		g.setColor(CalendarColor.COLOR_LIGHT_RED);
		
		if (state == STATE_SELECT_WEEK) {
			// �g�̕`��(+2�͘g�쐬����+2�Ƃ��Ă��邽��)			 						
			x = ((width-weekBox[0].getWidth())>>1) + +weekStrlen+margin;
			y = height-(sep*7+(sep>>1))                    //�����܂łōŏ��̃{�b�N�X�̈ʒu
			      +(weekIndex)*(boxHeight+boxHMargin);     //�C���f�b�N�̑���
			w = boxWidth;
			h = boxHeight;			
		} else if (state == STATE_SELECT_MONTH) {
			// �g�̕`��
			x = ((width-size)>>1) + sep;
			y = monthY;
			w = sep*5;
			h = sep;
		}
		
		g.drawRect(x,  y  ,w,h);
		g.drawRect(x+1,y  ,w,h);
		g.drawRect(x,  y+1,w,h);
		g.drawRect(x+1,y+1,w,h);
	}
}
