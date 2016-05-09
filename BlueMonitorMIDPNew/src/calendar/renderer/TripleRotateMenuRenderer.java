package calendar.renderer;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.Sprite;

import calendar.util.CalendarColor;

import request.Request;
import request.RequestQueue;

import util.DojaGraphics;

public class TripleRotateMenuRenderer {
	
	public static final int MODE_DAY = 0;
	public static final int MODE_OTHER = 1;
	
	/** �A�j���[�V�����̃��[�h(UP) */
	public static final int UP = 0;
	/** �A�j���[�V�����̃��[�h(DOWN) */
	public static final int DOWN = 1;
	
	public static final String ECG = "�S�d�}";
	public static final String PLS = "���g";
	public static final String HR  = "�S��";
	public static final String BP  = "����";

	//sone0910
	private final String[] dayItems = { ECG , PLS , HR , BP };
	//komiya0919
	private final String[] otherItems = { "�S��", "����" };

	private String[] items = null;

	// komiya0919
	private Image[] dayIcons = null;
	private Image[] otherIcons = null;
	private Image dayIconMap = null;
	private Image otherIconMap = null;

	private Image[] icons = null;
	private Image iconMap = null;

	private Image[] arrows = null;
	private Image selector = null;

	private int width;
	private int height;

	private int hMargin;
	private int wMargin;

	private int index = 0;

	// double�̐��x���Ⴂ����1000�{�̐����ŕ\��
	private int indexPosition = 0;
	private int menuPosition = 0;
	
	//�A�N�V���������ǂ����A�A�N�V�������Ȃ玟�̃A�N�V�����͑ҋ@
	private boolean animationFlag = false;
	//�A�N�V������ێ�����L���[
	private RequestQueue queue;
	
	/**
	 * �R���X�g���N�^
	 * @param width  ��
	 * @param height ����
	 */
	public TripleRotateMenuRenderer(int width,int height) {
		
		this.width  = width;
		this.height = height;

		hMargin = height / 27;
		wMargin = 10;

		createImages();
		setMode(MODE_DAY);
		
		queue = new RequestQueue();
		
	}/* End of TripleRotateMenuRenderer() */

	/**
	 * �`�揈�� 
	 * @param g �����_�����O�ɂ���Graphics�I�u�W�F�N�g
	 */
	public void draw(DojaGraphics g) {
		drawArrow(g);
		drawIcon(g);
		drawSelector(g);
	}/* End of draw() */

	public void setMode(int mode) {
		if (mode == MODE_DAY) {
			items = dayItems;
			icons = dayIcons;
			iconMap = dayIconMap;
		} else if (mode == MODE_OTHER) {
			items = otherItems;
			icons = otherIcons;
			iconMap = otherIconMap;
		}
	}/* End of setMode() */
	
	/**
	 * �A�C�R���̃��[�u
	 * @param action
	 */
	public void action(int action){
		if(animationFlag){
			//�A�j���[�V���������s���̂Ƃ�
			//���̃A�j���[�V�������L���[�֓����
			queue.putRequest(new Request(action));
			return;
		}
		animationFlag = true;
		new RendererThread(action).start();
	}

	/**
	 * ��� 
	 */
	public void up(boolean flag) {
		if (flag) {
			indexPosition -= 50;
			if (indexPosition < 0){
				indexPosition = icons.length * 1000 - 50;				
			}										
		} else {
			menuPosition -= 50;
			if (menuPosition < 0){
				menuPosition = icons.length * 1000 - 50;				
			}						
		}	
	}/* End of up() */

	/**
	 * ����
	 */
	public void down(boolean flag) {		
		if (flag) {
			indexPosition += 50;
			if (indexPosition >= icons.length * 1000){
				indexPosition = 0;				
			}											
		} else {
			menuPosition += 50;
			if (menuPosition >= icons.length * 1000){
				menuPosition = 0;				
			}						
		}		
	}/* End of down() */

	/**
	 * �C���f�b�N�X�𓾂�
	 * @return
	 */
	public int getIndex() {
		return index;
	}

	public void posInit() {
		index = 0;
		indexPosition = 0;
		menuPosition = 0;
	}

	/**
	 * �A�C�e�����𓾂�
	 * @return�@�A�C�e����
	 */
	public String getItemName() {
		return items[index];
	}		

	/**
	 * �摜�̍쐬
	 */
	private void createImages() {
		createIcons();
		createArrow();
		createSelector();
		createIconMap();
	}

	/**
	 * �A�C�R���̍쐬
	 */
	private void createIcons() {
		final Font FONT = Font.getFont(Font.FACE_SYSTEM,Font.STYLE_BOLD, Font.SIZE_LARGE);
		
		dayIcons = new Image[dayItems.length];
		for (int i = 0; i < dayItems.length; i++) {
			
			dayIcons[i] = Image.createImage(width - wMargin * 2 + 2,hMargin * 6 + 2);
			Graphics ig = dayIcons[i].getGraphics();
			
			final int iconW = dayIcons[i].getWidth();
			final int iconH = dayIcons[i].getHeight();
		
			ig.setColor(CalendarColor.COLOR_WHITE);					
			ig.fillRoundRect(0, 0, dayIcons[i].getWidth(), dayIcons[i].getHeight(), 10, 10);
			
			ig.setColor(CalendarColor.COLOR_ORANGE);
			ig.setFont(FONT);
			
			final int x = 0;
			final int y = 0;
			final int w = iconW - 3;
			final int h = iconH - 3;
			final int corner = 10;
			
			ig.drawRoundRect(x  , y  , w, h, corner, corner);
			ig.drawRoundRect(x+1, y  , w, h, corner, corner);
			ig.drawRoundRect(x  , y+1, w, h, corner, corner);
			ig.drawRoundRect(x+1, y+1, w, h, corner, corner);
			ig.drawRoundRect(x+1, y+2, w, h, corner, corner);
			ig.drawRoundRect(x+2, y+1, w, h, corner, corner);
			ig.drawRoundRect(x+2, y+2, w, h, corner, corner);
						
			ig.drawString(dayItems[i],((iconW - FONT.stringWidth(dayItems[i]))>>1),
						              ((iconH - FONT.getHeight() )>>1)            ,
						                Graphics.TOP|Graphics.LEFT);
		}/* End of for() */

		otherIcons = new Image[otherItems.length];
		for (int i = 0; i < otherItems.length; i++) {
			otherIcons[i] = Image.createImage(width - (wMargin<<1) + 2, hMargin*6 + 2);
			Graphics ig = otherIcons[i].getGraphics();
			
			final int iconW = otherIcons[i].getWidth();
			final int iconH = otherIcons[i].getHeight();
			
			ig.setColor(CalendarColor.COLOR_WHITE);
			ig.fillRoundRect(0, 0, otherIcons[i].getWidth(), otherIcons[i].getHeight(), 10, 10);
			
			ig.setColor(CalendarColor.COLOR_ORANGE);
			ig.setFont(FONT);
			
			final int x = 0;
			final int y = 0;
			final int w = iconW - 3;
			final int h = iconH - 3;
			final int corner = 10;
			
			ig.drawRoundRect(x  , y  , w, h, corner, corner);
			ig.drawRoundRect(x+1, y  , w, h, corner, corner);
			ig.drawRoundRect(x  , y+1, w, h, corner, corner);
			ig.drawRoundRect(x+1, y+1, w, h, corner, corner);
			ig.drawRoundRect(x+1, y+2, w, h, corner, corner);
			ig.drawRoundRect(x+2, y+1, w, h, corner, corner);
			ig.drawRoundRect(x+2, y+2, w, h, corner, corner);
			
			ig.drawString(otherItems[i],((iconW - FONT.stringWidth(otherItems[i]))>>1),
		              				    ((iconH - FONT.getHeight() )>>1)            ,
		              				     Graphics.TOP|Graphics.LEFT);
		      
		}/* End of for() */
	}/* End of createIcons */

	/**
	 * ���̍쐬
	 */
	private void createArrow() {
		arrows = new Image[2];
		Graphics[] ag = new Graphics[2];
		
		for (int i = 0; i < 2; i++) {
			arrows[i] = Image.createImage((width - (wMargin<<1))/3,hMargin*3);
			ag[i] = arrows[i].getGraphics();
			ag[i].setColor(CalendarColor.COLOR_LIGHT_GREEN);
		}

		final int imgWidth  = arrows[0].getWidth();
		final int imgHeight = arrows[0].getHeight();

		final int x1 = imgWidth / 2;
		final int y1 = 0;
		final int x2 = 0;
		final int y2 = imgHeight;
		final int x3 = imgWidth;
		final int y3 = imgHeight;

		ag[0].fillTriangle(x1, y1, x2, y2, x3, y3);
		ag[1].drawRegion(arrows[0], 0, 0, imgWidth, imgHeight,Sprite.TRANS_MIRROR_ROT180, 0, 0, Graphics.TOP | Graphics.LEFT);
		
	}/* End of createArrow */

	/**
	 * �Z���N�^�̍쐬
	 */
	private void createSelector() {

		final int sWidth  = width - (wMargin<<1) + 2;
		final int sHeight = hMargin * 6 + 2;
		final int iconW = dayIcons[0].getWidth();
		final int iconH = dayIcons[0].getHeight();
		
		final int x = 0;
		final int y = 0;
		final int w = iconW - 3;
		final int h = iconH - 3;
		final int corner = 10;

		Image tmpImg = Image.createImage(sWidth, sHeight);
		Graphics tg = tmpImg.getGraphics();

		tg.setColor(CalendarColor.COLOR_YELLOW);
		tg.fillRoundRect(0, 0, iconW,iconH,corner,corner);
		
		tg.setColor(CalendarColor.COLOR_RED);
		tg.drawRoundRect(x  , y  , w, h, corner, corner);
		tg.drawRoundRect(x+1, y  , w, h, corner, corner);
		tg.drawRoundRect(x  , y+1, w, h, corner, corner);
		tg.drawRoundRect(x+1, y+1, w, h, corner, corner);
		tg.drawRoundRect(x+1, y+2, w, h, corner, corner);
		tg.drawRoundRect(x+2, y+1, w, h, corner, corner);
		tg.drawRoundRect(x+2, y+2, w, h, corner, corner);

		final int[] rgb = new int[sWidth * sHeight];
		tmpImg.getRGB(rgb, 0, sWidth, 0, 0, sWidth, sHeight);
		final int aColor = rgb[(sWidth>>1) + (sHeight>>1) * sWidth];
		
		for (int i = 0; i < rgb.length; i++) {
			if (rgb[i] == aColor)
				rgb[i] = (rgb[i] & 0xFFFFFF) | (0x3F << 24);
		}
		
		selector = Image.createRGBImage(rgb, sWidth, sHeight, true);
		
	}/* End of createSelector() */

	/**
	 * �A�C�R���}�b�v�̍쐬 
	 */
	private void createIconMap() {
	
		int iconH = dayIcons[0].getHeight();		
		int imHeight = iconH * dayIcons.length + (hMargin>>1) * (dayIcons.length - 1);

		Image tmpImg = Image.createImage(width, imHeight);
		Graphics tg = tmpImg.getGraphics();

		tg.setColor(CalendarColor.COLOR_BLACK);
		tg.fillRect(0, 0, tmpImg.getWidth(), tmpImg.getHeight());

		int x = wMargin;
		int[] y = new int[dayIcons.length];
		y[0] = 0;
		for (int i = 1; i < y.length; i++) {
			y[i] = y[i - 1] + iconH + (hMargin>>1);
		}

		for (int i = 0; i < dayIcons.length; i++) {
			tg.drawImage(dayIcons[i], x, y[i], Graphics.TOP | Graphics.LEFT);
		}

		int[] rgb = new int[width * imHeight];
		tmpImg.getRGB(rgb, 0, width, 0, 0, width, imHeight);
		int ndColor = rgb[0];
		for (int i = 0; i < rgb.length; i++) {
			if (rgb[i] == ndColor)
				rgb[i] = (rgb[i] & 0xFFFFFF) | (0x00 << 24);
		}
		dayIconMap = Image.createRGBImage(rgb, width, imHeight, true);

		iconH = otherIcons[0].getHeight();
		imHeight = iconH * otherIcons.length + (hMargin>>1) * (otherIcons.length - 1);

		tmpImg = null;
		tg = null;
		tmpImg = Image.createImage(width, imHeight);
		tg = tmpImg.getGraphics();

		tg.setColor(CalendarColor.COLOR_BLACK);
		tg.fillRect(0, 0, tmpImg.getWidth(), tmpImg.getHeight());

		x = wMargin;
		y = new int[otherIcons.length];
		y[0] = 0;
		for (int i = 1; i < y.length; i++) {
			y[i] = y[i - 1] + iconH + (hMargin>>1);
		}

		for (int i = 0; i < otherIcons.length; i++) {
			tg.drawImage(otherIcons[i], x, y[i], Graphics.TOP | Graphics.LEFT);
		}

		rgb = new int[width * imHeight];
		tmpImg.getRGB(rgb, 0, width, 0, 0, width, imHeight);
		ndColor = rgb[0];
		for (int i = 0; i < rgb.length; i++) {
			if (rgb[i] == ndColor)
				rgb[i] = (rgb[i] & 0xFFFFFF) | (0x00 << 24);
		}
		otherIconMap = Image.createRGBImage(rgb, width, imHeight, true);

	}/* End of createIconMap() */

	/**
	 * ���̕`��
	 * @param g
	 */
	private void drawArrow(DojaGraphics g) {
		int x = wMargin + (width - (wMargin<<1)) / 3;
		int y1 = hMargin;
		int y2 = height - hMargin - arrows[1].getHeight();

		g.drawImage(arrows[0], x, y1);
		g.drawImage(arrows[1], x, y2);
	}/* End of drawArrow() */

	/**
	 * �A�C�R���̕`��
	 * @param g
	 */
	private void drawIcon(DojaGraphics g) {
		int x = 0;
		int y = hMargin + arrows[0].getHeight()
				+ ((height - icons[0].getHeight() * 3 - (arrows[0].getHeight()<<1) - hMargin * 3)>>1);

		double pos = (double) (menuPosition) / 1000;

		g.setClip(x, y, width, height - (y<<1));
		g.drawImage(iconMap, x, y - (int) (pos * (icons[0].getHeight() + (hMargin>>1) )));
		if ((pos+3) > icons.length) {
			g.drawImage(iconMap, x, y - (int) (pos * (icons[0].getHeight() + (hMargin>>1) ))
					                  + iconMap.getHeight() + (hMargin>>1));
		}
		g.setClip(0, 0, width, height);
		
	}/* End of drawIcon() */

	/**
	 * �Z���N�^�̕`��
	 * @param g
	 */
	private void drawSelector(DojaGraphics g) {
		int x = wMargin;		
		int y = hMargin	+ arrows[0].getHeight() 
		        + ((height - icons[0].getHeight() * 3 - (arrows[0].getHeight()<<1) - hMargin * 3)>>1);

		double pos = (double) (indexPosition) / 1000;

		g.drawImage(selector, x, y + (int) (pos * (icons[0].getHeight() + (hMargin>>1))));				
		
	}/* End of drawSelector() */
	
	/**
	 * ���j���[�𓮂����X���b�h 
	 *
	 */
	public class RendererThread extends Thread {
		
		/** ���� */		
		private int action = -1;
		/** ���[�v�̃J�E���^ */
		private int cnter = 0;
		/** �ő僋�[�v�� */
		private static final int MAX_LOOP = 20;		
		/** UP,DOWN�̂ق��ɂ���ɓ���𕪂���t���O */
		private boolean flag;
		
		/**
		 * �R���X�g���N�^
		 * @param ����
		 */
		public RendererThread(int action) {
			this.action = action;
			
			if(action == UP){
				index--;
				if (index < 0){
					index = icons.length - 1;			
				}
				
				if (indexPosition > 0) {
					flag = true;
				}else{
					flag = false;
				}									
			}else{
				index = (index + 1) % icons.length;
				
				if (indexPosition < 2 * 1000) {
					flag = true;
				}else{
					flag = false;
				}
			}			
		}/* End of RendererThread() */
		
		public void run() {
			long start = System.currentTimeMillis();
			
			while(cnter < MAX_LOOP){
								
				//�A�C�R���𓮂���
				switch (action) {
					case UP:
						up(flag);						
					break;
					case DOWN:
						down(flag);
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
				
			}/* End of while() */		
			
			//�A�j���[�V�����̏I��
			//�L���[�Ɏ��̃A�j���[�V�������Ȃ����ǂ������ׂ�
			Request req = queue.getRequest();
			if(req != null){
				//���N�G�X�g����������A�j���[�V�������J�n
				new RendererThread(req.getCommand()).start();
			}else{
				//�Ȃ�������A�j���[�V�������I��
				animationFlag = false;
			}
		}/* End of run() */
		
	}/* End of RendererThread Class */
}
