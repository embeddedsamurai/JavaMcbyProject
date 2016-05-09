package calendar.renderer;

import calendar.util.CalendarUtil;
import calendar.util.ReadDBListener;
import calendar.util.ReadNumOfDBData;
import util.DojaFont;
import util.DojaGraphics;

public class EcgPlsBarGraphRenderer implements ReadDBListener{
	//================================�萔====================================//

	//------- �F�֘A  --------//
	/** �w�i�F */
	private static final int BG_COLOR = 0x00FFFFFF;
	/** �o�b�N�O�����h�̖ڐ���̐F */
	private static final int BG_LINE_COLOR = 0x999999;
	/** �ڐ���̎��̐F */
	private final int BG_STR_COLOR = 0x000000FF;
	/** �_�O���t�̐F */
	private final int BAR_COLOR = 0x00FFC26A;
	/** �_�O���t�̐F */
	private final int BAR_RED_COLOR = 0x00FF0000;

	//-----------���W------------//
	/** ����Y���W */
	private final int ground_y;	
	
	//------- �t�H���g�֘A   --------//
	/** �t�H���g */
	private static final DojaFont FONT = DojaFont.getFont(DojaFont.SIZE_TINY);
	/** �t�H���g�̃f�B�Z���g */
	private static final int FONT_DECENT = FONT.getDescent();
	/** �t�H���g�̃A�Z���g */
	private static final int FONT_ASCENT = FONT.getAscent();
	/** �t�H���g�̍��� */
	private static final int FONT_HEIGHT = FONT_ASCENT + FONT_DECENT;
		
	/** X�������̗]�� */
	private static final int xMargin = 5;
	/** Y�������̗]�� */
	private static final int yMargin = 5;
	
	/** �� */
	private final int width;
	/** ���� */
	private final int height;
	
	//================================�ϐ�====================================//
	
	/** ��M�f�[�^ */
	private int[] recieveData;				
	/** �w�i�̏c�̖ڐ�����̍���*/
	private int bg_vertical_line_h;
	/** �I�����ꂽ�o�[�̃C���f�b�N�X */
	private int selectedIndex = -1; 
	
	/** ���t���x�� */
	private String dateLabel ="";
	
	/** X�������̕`��J�n���W */	
	private int startX = 0;
	/** Y���ɕ\�����鐔�����鐔*/
	private int den = 1;
	/** �\������ő�l */
	private int max = 1;
	/** �ő�̌��� */ 
	private int sig = String.valueOf(max).length();
	/** ���t��ێ�����z��*/
	private int[] dateArray;
	
	/**
	 * �R���X�g���N�^
	 * @param canvas
	 * @param baseSbp
	 * @param w
	 * @param h
	 */
	public EcgPlsBarGraphRenderer(int width, int height){				
		this.width = width;
		this.height = height;
		
		//�`��Y���W
		ground_y = height - (FONT_HEIGHT) - yMargin;
		bg_vertical_line_h = (FONT_HEIGHT<<1) + yMargin;		
				
	}/* End of StaticWaveRenderer */
	
	/**
	 * �I������Ă������Ԃ�
	 * 
	 * @return �I�����鍀�ڂ��Ȃ����͂��ׂ�-1�̔z���Ԃ�
	 * <br />  0�ԖڂɔN�A�P�ԖڂɌ��A�Q�Ԗڂɓ�,�R�ԖڂɎ��� 
	 */
	public int[] getSelectedDate(){
		if(selectedIndex ==-1){
			return new int[]{-1,-1,-1,-1};
		}else{
			return new int[]{dateArray[0],dateArray[1],dateArray[2],selectedIndex};
		}
	}
	
	/**
	 * �I������Ă���_�O���t�̃f�[�^�̃f�[�^����Ԃ�
	 * @return
	 */
	public int getSelectedDataLen(){
		if(selectedIndex < 0 || selectedIndex >= recieveData.length){
			return 0;
		}
		return this.recieveData[selectedIndex];
	}
	
	/**
	 * ���̃o�[��I������
	 */
	public void nextBar(){
		//�f�[�^���Ȃ����͉������Ȃ�
		if(selectedIndex < 0){
			return;
		}
		int i = selectedIndex;		
		while(true){
			i = (i + 1)%recieveData.length;
			if(recieveData[i] != 0){
				selectedIndex = i;
				break;
			}				
		}		
	}
	
	/**
	 * �O�̃o�[��I������
	 */
	public void previousBar(){
		//�f�[�^���Ȃ����͉������Ȃ�
		if(selectedIndex < 0){
			return;	
		}
		int i = selectedIndex;
		while(true){
			i = ((i-1) +recieveData.length)%recieveData.length;
			if(recieveData[i] != 0){
				selectedIndex = i;
				break;
			}				
		}		
	}
	
	/**
	 * �`�揈�����s��
	 * @param g
	 */
	public void draw(DojaGraphics g){
		
		//�w�i�̓h��Ԃ�
		g.setColor(BG_COLOR);
		g.clearRect(0,0, width, height);
		
		//�_�O���t�̕`��
		drawBarGraph(g, ground_y);

	}//End of draw()
	
	/**
	 * �_�O���t��`��
	 * @param g   Graphics�I�u�W�F�N�g
	 * @param glY �`��̊���W ECG,PLS�̃O�����h���C��
	 */
	private void drawBarGraph(DojaGraphics g,int glY){
		//�`��F��ύX
		g.setColor(BG_LINE_COLOR);
		
		int x = 0;
		int y = 0;
		
		if(selectedIndex < 0){
			//�f�[�^���Ȃ���
			g.setColor(BAR_RED_COLOR);
			g.drawString("�f�[�^������܂���",(width>>1),(height>>1));
		}else{
			startX = xMargin + FONT.stringWidth("000");
			
			//X��		
			x = startX - 1;
			g.drawLine(x,glY,width-(xMargin<<1),glY);		
			//Y��
			g.drawLine(x,glY,x,bg_vertical_line_h);

			//X�����x���̕`��		
			g.setColor(BG_STR_COLOR);
			int interval = ((width-xMargin)-startX)/recieveData.length;
			for(int i = 0; i <= recieveData.length; i+=4){
				x = interval*i + startX + (interval>>1);
				
				if(i == selectedIndex && recieveData[i] != 0){
					//�f�[�^������A���I������Ă���o�[�̂Ƃ�
					g.setColor(BAR_RED_COLOR);
				}else{
					g.setColor(BG_STR_COLOR);
				}//End of if()
				
				g.drawString(i+"",x,glY + FONT_ASCENT);
			}//End of for()
			
			//Y���̍���
			final int yAxisH = glY -bg_vertical_line_h;
			//���K���̂��߂̔{��
			double scaleY = (double)(yAxisH)/(double)max;
			
			//�_�O���t��`��
			x = 0;
			y = 0;
			int x1 = 0;
			int y1 = 0;
			g.setColor(BAR_COLOR);
			for(int i = 0; i < recieveData.length ; i++){
				//�f�[�^���Ȃ��Ƃ��͕`�悵�Ȃ�
				if(recieveData[i] == 0)continue;
				
				x  = interval*i + startX + 1;
				double size = (recieveData[i]*scaleY);
				if(size < 3){
					//���܂�ɂ���������
					size = 3;
				}
				y  = (int)(glY - size);			
				x1 = interval - 1;			
				y1 = glY - y -1;
				
				//------------�_�̕`��------------//
				if(i == selectedIndex){
					//�I���ɂ���Ă���Ƃ�
					g.setColor(BAR_RED_COLOR);	
				}//End of if()
				
				//�_�̕`��
				g.clearRect(x, y, x1 ,y1);
				
				if(i == selectedIndex){
					//�I���ɂ���Ă���Ƃ�(�F��߂�)
					g.setColor(BAR_COLOR);	
				}//End of if()
				//------------�_�̕`��------------//						
				
			}//End of for()
			
			//Y���̃�������`��
			int division = 5;
			//���̊Ԋu
			interval = yAxisH/division;			
			
			//�ڐ����`��
			for(int i = 0; i <= division ; i++){
				//�ڐ����
				x = startX - 1;
				y =  glY - interval*i;			
				if(i != 0){							
					g.setColor(BG_LINE_COLOR);
					g.drawLine(x-2,y,x+2,y);	
				}//End of if()			
				
				//�ڐ���
				x = (startX - 1)>>1;
				y += (FONT_ASCENT>>1);
				g.setColor(BG_STR_COLOR);			
				double scale = (double)i/(double)division;				
				//�����_���P���܂ŕ\���B�����ɂ���Ē���
				g.drawString("" + (double)((int)((max*scale)/den))/10,x,y);			
			}//End of for()
			
			//x 10^a ��`��@ a�͍ő�l�̌����ɂ���Č��肷��
			y = bg_vertical_line_h - FONT_HEIGHT;			
			String str = "x 10^"+(sig-1);
			x = xMargin + (FONT.stringWidth(str)>>1);
			g.drawString(str, x, y);
			
			//�I������Ă���o�[�̉��ɐ��l��`��
			double size = (recieveData[selectedIndex]*scaleY);
			if(size < 3){
				//���܂�ɂ���������
				size = 3;
			}
			interval = ((width-xMargin)-startX)/recieveData.length;
			str = selectedIndex+"��:"+recieveData[selectedIndex]+"��";		
			if(selectedIndex > (recieveData.length>>1)){
				//��ʉE���̖_�̂Ƃ�,���l�͍��ɕ\��		
				x  = (interval*selectedIndex) + startX + 1 - (interval>>1)
				- (FONT.stringWidth(str)>>1);
			}else{
				//��ʍ��̖_�̂Ƃ�,���l�͉E�ɕ\��	
				x  = (interval*selectedIndex) + startX + 1 +  interval + (interval>>1)
				+ (FONT.stringWidth(str)>>1);
			}
			y  = (int)(glY - size) + FONT_HEIGHT;
			g.setColor(BAR_RED_COLOR);
			g.drawString(str,x, y);
		}
		
		//�㕔�̃��x����`��
		y = bg_vertical_line_h - FONT_HEIGHT;
		x = (width<<1)/3;
		g.setColor(BG_STR_COLOR);
		g.drawString(dateLabel, x, y);
		
	}//End of drawBGLine()

	//======================ReadDBListener======================//
	/**
	 * DB����̓ǂݍ��݂����������Ƃ��̌Ă΂�� 
	 */
	public void onCompleteReadDB(int[] data,int type,long time) {
		System.out.println("#onCompleteReadDB@staticWave");
		
		//�f�[�^���R�s�[
		recieveData = new int[data.length];
		for(int i = 0; i < data.length; i++){			
			recieveData[i] = data[i];
		}
				
		//�ŏ��̑I���o�[�����߂�
		selectedIndex = -1;
		for(int i = 0; i < recieveData.length; i++){
			if(recieveData[i] != 0){
				selectedIndex = i;
				break;
			}
		}//End of for()
		
		//���x�����X�V
		dateArray = CalendarUtil.calcTimeInfo(time);
		
		String typeStr = "";
		switch (type) {		
		case ReadNumOfDBData.TYPE_ECG_NUM:
			typeStr ="�S�d�}";
			break;
		case ReadNumOfDBData.TYPE_PLS_NUM:
			typeStr ="���g";
			break;
		}//End of switch
		
		dateLabel = typeStr +" " + dateArray[1] +"�� " + dateArray[2] + "��";
				
		if(selectedIndex < 0 ){
			//�f�[�^���Ȃ����͂����ł����܂�
			return;
		}
		
		//�_�O���t�̕`��
		//�ő�l�����߂�	
		max = 1;
		for(int i = 0; i < recieveData.length; i++){
			if(max < recieveData[i]){
				//�ő�l
				max = recieveData[i];
			}
		}//End of for()
		
		//�ő�̌��� 
		sig = String.valueOf(max).length();
		den = 1;
		for(int  i = 0; i < sig -2; i++){
			den *= 10;	
		}
		
		 					
	}/* End of onCompleteReadDB() */
	
	/**
	 * DB����̓ǂݍ��݂����s�����Ƃ��̌Ă΂��
	 */
	public void onErrorReadDB() {
		System.out.println("#onErrorReadDB@waitScreen");
	}/* End of onErrorReadDB */
 
}
