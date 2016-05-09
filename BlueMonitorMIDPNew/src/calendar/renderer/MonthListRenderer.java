package calendar.renderer;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import calendar.CalendarManager;
import calendar.util.CalendarColor;
import calendar.util.CalendarUtil;

import util.DojaFont;
import util.DojaGraphics;

public class MonthListRenderer {

	/** ���t�H���g */
	private static final DojaFont sFONT = DojaFont.getFont(DojaFont.SIZE_SMALL);
	/** ��t�H���g */
	private static final DojaFont mFONT = DojaFont.getFont(DojaFont.SIZE_MEDIUM);	
	
	/** �I�����[�h */
	private static final int STATE_SELECT_MONTH = 0;
	private static final int STATE_SELECT_YEAR = 1;	
	
	/** ���݂̑I�����[�h */
	private int state = STATE_SELECT_MONTH;
	
	/** �����X�g�̉摜 */
	private Image[] monthListImg = new Image[12];
	
	/** ���摜 */
	private static Image LEFT_ARROW_IMG = null;
	/** ���摜 */
	private static Image RIGHT_ARROW_IMG = null;
	/** ���摜 */
	private static Image RIGHT_ARROW_IMG_2 = null;
	/** ���t�H�[�J�X�p�摜 */
	private static Image MONTH_FOCUS_IMG = null;
	
	/** ��ʕ� */
	private int width = 0;
	/** ��ʍ� */
	private int height = 0;
	
	/** ���ݕ`�撆�̔N */
	private int curYear = 0;

	
	/** ���ݑI�𒆂̌� */
	private int monthIndex = 0;
	/** �t�H�[�J�X���ł��邩�ǂ��� */
	private boolean focus = false;
	/** �t�H�[�J�X��ON/OFF��؂�ւ��邩�ǂ��� */
	private boolean changeFocus = false;
	
	/** ���I���{�b�N�X�̈�ӂ̒��� */
	private int boxWidth;
	/** ��ʉ��[�̗]�� */
	private int marginWidth;		
	/** ��ʉ��[�̗]�� */
	private int bottomMargin;		
	/** ���I���{�b�N�X���m�̊Ԋu */
	private int boxInterval;
	/** ���I���{�b�N�X�̕`��J�nY���W */
	private int startY;
	
	/**
	 * �R���X�g���N�^	 
	 * @param width ��
	 * @param height�@����
	 */
	public MonthListRenderer(int width,int height){
		//���j�^�̃T�C�Y���擾
		this.width  = width; 
		this.height = height;
		
		// ���I���{�b�N�X�̈�ӂ̒���
		boxWidth = width/9;
		// ��ʉ��[�̗]��
		marginWidth = (width-(boxWidth*7))>>1;		
		// ��ʉ��[�̗]��
		bottomMargin = (marginWidth>>1);
		// ���I���{�b�N�X���m�̊Ԋu
		boxInterval = (boxWidth>>1);
		// ���I���{�b�N�X�̕`��J�nY���W 
		startY = height - (boxWidth*7) - bottomMargin;
		
		// �C���[�W�̍쐬
		createImages();
		// ���݂̎��Ԃ��擾
		int[] info = CalendarUtil.calcTimeInfo(System.currentTimeMillis());
		curYear = info[0];
	}
	
	/**
	 * ���݂̔N�̌����X�g��`��	 
	 * @param g �`��Ώۂ̃O���t�B�b�N�X�N���X	 
	 */
	public void draw(DojaGraphics g){
		drawMonthList(g);
		drawTop(g,curYear);
		drawSelector(g);
	}
	
	
	/**
	 * ��� 
	 */
	public void up(){
		if(state == STATE_SELECT_MONTH){
			if(monthIndex < 4){
				state = STATE_SELECT_YEAR;
				monthIndex = -1;
			} else{
				monthIndex -= 4;
			}
		} else if(state == STATE_SELECT_YEAR){
			changeFocus = true;
		}
	}
	
	/**
	 * ����
	 */
	public void down(){		
		if(state == STATE_SELECT_MONTH){			
			if(monthIndex > 7){
				changeFocus = true;
			} else{
				monthIndex += 4;
			}			
		} else if(state == STATE_SELECT_YEAR){			
			state = STATE_SELECT_MONTH;
			monthIndex = 1;			
		}		
	}
	
	/**
	 * ����
	 */
	public void left(){
		if(state == STATE_SELECT_MONTH){
			monthIndex--;
			if((monthIndex+1)%4 == 0){
				monthIndex += 4;
			}
		}else if(state == STATE_SELECT_YEAR){
			curYear--;
		}
	
	}
	
	/**
	 * �E��
	 */
	public void right(){
		if(state == STATE_SELECT_MONTH){
			monthIndex++;
			if(monthIndex%4 == 0){
				monthIndex -= 4;
			}			
		}else if(state == STATE_SELECT_YEAR){			
			int[] info = CalendarUtil.calcTimeInfo(System.currentTimeMillis());
			if(curYear >= info[0]){
				return;
			}
			curYear++;			
		}		
	}
	
	/**
	 * �t�H�[�J�X�̏�Ԃ�ݒ肷��	 
	 * @param focus �t�H�[�J�X�̏��
	 * @param key ��ԑ@�ۑO�ɉ������L�[	 
	 */
	public void setFocus(boolean focus, int key){
		this.focus = focus;
		if(key == CalendarManager.UP_REQ){
			monthIndex = 9;
			state = STATE_SELECT_MONTH;
		} else if(key == CalendarManager.DOWN_REQ){
			monthIndex = -1;
			state = STATE_SELECT_YEAR;
		}
	}
	
	/**
	 * �t�H�[�J�X���ς�������ǂ���
	 * @return �t�H�[�J�X���ς�������ǂ���
	 */
	public boolean isChangeFocus(){
		if(changeFocus){
			changeFocus = false;
			return true;
		}
		return false;
	}
	
	/**
	 * �����X�g�`��p�C���[�W�̍쐬
	 */
	private void createImages(){
		createMonthListImg();
		createArrowImg();
	}
	
	/**
	 * ���I�𕔕��̃C���[�W���쐬
	 */
	private void createMonthListImg(){
		
		final int margin = 2;
		
		// 12���������[�v����
		for(int i=0;i<12;i++){
			
			// �`��p�C���[�W�̍쐬
			monthListImg[i] = Image.createImage(boxWidth+margin, boxWidth+margin);
			Graphics mg = monthListImg[i].getGraphics();
						
			// �O�g�̕`��
			int x = 0;
			int y = 0;
			int w = boxWidth;
			int h = boxWidth;
			final int curve = 15;
			mg.setColor(CalendarColor.COLOR_LIGHT_GRAY);			
			mg.drawRoundRect(x  ,y  ,w, h, curve, curve);
			mg.drawRoundRect(x  ,y+1,w, h, curve, curve);
			mg.drawRoundRect(x+1,y  ,w, h, curve, curve);
			mg.drawRoundRect(x+1,y+1,w, h, curve, curve);
			
			// ���ԍ��̕`��
			String dstr = Integer.toString(i+1);
			final Font font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_LARGE);
			mg.setFont(font);
			mg.setColor(CalendarColor.COLOR_MOTH_GREEN);
			x = ((boxWidth - font.stringWidth(dstr))>>1);
			y = ((boxWidth - font.getBaselinePosition())>>1)+1;
			
			int position = Graphics.TOP|Graphics.LEFT;
			mg.drawString(dstr, x  , y  , position);
			mg.drawString(dstr, x  , y+1, position);
			mg.drawString(dstr, x+1, y  , position);
			mg.drawString(dstr, x+1, y+1, position);
		}
		
		//���p�t�H�[�J�X�摜�̐���
		Image tmpImg = Image.createImage(boxWidth+margin, boxWidth+margin);
		Graphics g = tmpImg.getGraphics();
		int x = 0;
		int y = 0;
		int w = boxWidth;
		int h = boxWidth;
		final int curve = 15;
		g.setColor(CalendarColor.COLOR_LIGHT_RED);
		g.drawRoundRect(x  ,y  ,w, h, curve, curve);
		g.drawRoundRect(x  ,y+1,w, h, curve, curve);
		g.drawRoundRect(x+1,y  ,w, h, curve, curve);
		g.drawRoundRect(x+1,y+1,w, h, curve, curve);
		
		// ���߃C���[�W�̍쐬
		w = (w+margin);
		h = (h+margin);
		int[] rgb1 = new int[w*h];
		tmpImg.getRGB(rgb1, 0, w, 0, 0,w,h);
		int ndColor1 = rgb1[0];
		for (int i = 0; i < rgb1.length; i++) {
			if (rgb1[i] == ndColor1)rgb1[i] = ((rgb1[i] & 0xFFFFFF) | 0x00 << 24);
		}
		MONTH_FOCUS_IMG = Image.createRGBImage(rgb1,w,h,true);
	}
	
	
	/**
	 * ���̕`��
	 */
	private void createArrowImg() {
		// ���ڂ̊Ԋu
		int sep = width/9;
		
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
	 * �Ώۂ̔N�̌����X�g�̌��I�𕔕���`��
	 *
	 * @param g �`��Ώۂ̃O���t�B�b�N�X�N���X
	 *
	 */
	private void drawMonthList(DojaGraphics g){
		// �����X�g��`��
		int count = 0;
		int margin = (width - ((boxWidth*4)+(boxInterval)*3))>>1;
		for(int i=0; i < 3; i++){
			for(int j=0; j < 4; j++){
				int y = startY + 10 + i*(boxWidth+(boxInterval));
				int x = margin+j*(boxWidth+(boxInterval));
				g.drawImage(monthListImg[count],x,y);
				count++;
			}
		}
	}
	
	/**
	 * �㕔��`��
	 * @param g     Graphics�I�u�W�F�N�g
	 * @param year  �N 
	 */
	private void drawTop(DojaGraphics g, int year) {

		// ���ڂ̊Ԋu
		// ������9����  (�� �� �� �� �� �� �� �y ��)
		int sep = width/9;
		//�@�g�̑傫��
		final int size = sep*7;
		// �g�O���̗]�� (��������A��~�y�܂ł̕����������c��)	
		final int outmargin = (width-size)>>1;
		// �����Ԃ̗]���i��)
		final int strMargin = 5;
		// ���`��g�㕔��Y���W
		final int weekY = height - (size+(sep>>1)) - sep - 10;
		
		
		// �O�g�̕`��
		g.setColor(CalendarColor.COLOR_LIGHT_GRAY);
		int x = outmargin + sep;
		int y = weekY;
		g.drawRect(x  , y  ,sep*5, sep);
		g.drawRect(x+1, y  ,sep*5, sep);
		g.drawRect(x  , y+1,sep*5, sep);
		g.drawRect(x+1, y+1,sep*5, sep);
		
		// �P�ʂ̕`��,�N���̕`��
		final String ystr = Integer.toString(year);
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

	    // ���݂̔N�������擾
	    int[] info = CalendarUtil.calcTimeInfo(System.currentTimeMillis());
	    
		// ���̕`��(��)
		x = outmargin + sep - LEFT_ARROW_IMG.getWidth() - strMargin;
		y = weekY;
		g.drawImage(LEFT_ARROW_IMG,x,y);			
		
		// ���̕`��(�E)
		x = outmargin + size - RIGHT_ARROW_IMG.getWidth() + strMargin;
		if (curYear >= info[0]) {
			// ���̔N��������Ƃ�
			g.drawImage(RIGHT_ARROW_IMG_2,x,y);			
		}else{
			// ���̔N���Ȃ��Ƃ�
			g.drawImage(RIGHT_ARROW_IMG,x,y);
		}
	}
	
	/**
	 * �I��g��`��
	 *
	 * @param g �`��Ώۂ̃O���t�B�b�N�X�N���X
	 *
	 */
	private void drawSelector(DojaGraphics g){
		
		// �t�H�[�J�X���Ȃ��ꍇ�`�悵�Ȃ�
		if(!focus) return;
		final int monthMargin = (width - ((boxWidth*4)+(boxWidth>>1)*3))>>1;
		final int margin = 10;
		
		// �I��g�̕`��
		g.setColor(CalendarColor.COLOR_LIGHT_RED);
		int x = 0,y = 0,w = 0,h = 0;
		if(state == STATE_SELECT_MONTH){	// ���I����
			x = monthMargin + (monthIndex%4)*(boxWidth+boxInterval);
			y = startY + margin + (monthIndex/4)*(boxWidth+boxInterval);
			//�t�H�[�J�X�p�̉摜��`��
			g.drawImage(MONTH_FOCUS_IMG, x, y);
		} else if(state == STATE_SELECT_YEAR){  // �N�I����		
			x = marginWidth + boxWidth;
			
			// ���`��g�㕔��Y���W						
			y = height - (boxWidth*8 + (boxWidth>>1)) - margin;
			w = boxWidth*5;
			h = boxWidth;	
			// �g�̕`��
			g.drawRect(x,  y  ,w,h);
			g.drawRect(x+1,y  ,w,h);
			g.drawRect(x,  y+1,w,h);
			g.drawRect(x+1,y+1,w,h);
		}		
	}
	
}
