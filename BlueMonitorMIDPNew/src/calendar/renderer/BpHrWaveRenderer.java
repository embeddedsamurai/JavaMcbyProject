package calendar.renderer;

import util.DojaFont;
import util.DojaGraphics;
import calendar.util.CalendarUtil;
import calendar.util.ReadDBData;
import calendar.util.ReadDBListener;

public class BpHrWaveRenderer implements ReadDBListener{
	
	//================================�萔====================================//

	//------- �F�֘A  --------//
	/** �w�i�F */
	private static final int BG_COLOR = 0x00FFFFFF;
	/** �o�b�N�O�����h�̖ڐ���̐F */
	private static final int BG_LINE_COLOR = 0x999999;
	/** �o�b�N�O�����h�̖ڐ���̐F */
	private static final int BG_LINE_COLOR2 = 0xC9C9C9;
	/** �S�d�}�M���̐F */
	private static final int ECG_COLOR = 0x00FF0066;
	/** �ڐ���̎��̐F */
	private final int BG_STR_COLOR = 0x000000FF;
		
	/** �g�`�̕\���y�[�W */
	private final int NUM_OF_PAGE   = 10;

	//-----------���W------------//
	/** �g�`�`����Y���W (0������)*/
	private final int ground_y;	
	
	//------- ECG,PLS�p�̊g��k���{���̒�` --------//
	/** �ő��X�������̕\���k���{�� */
	public final int maxReductionRate = 1;
	/** �ŏ���X�������̕\���k���{�� */
	public final int minReductionRate = 1;
	
	//------- �o�b�N�O���E���h  --------//
	/** �o�b�N�O�����h�̖ڐ���̊Ԋu */
	private static final int BG_LINE_MARGIN = 10;
	/**	�o�b�N�O���E���h�̖ڐ�����̐�(�O�����h���C��������)*/
	private static final int NUM_OF_BGLINE = 17;
	/** �w�i�̏c�̖ڐ�����̍���*/
	private final int bg_vertical_line_h = (BG_LINE_MARGIN * (NUM_OF_BGLINE>>1));	
	
	//------- �t�H���g�֘A   --------//
	/** �t�H���g */
	private static final DojaFont FONT = DojaFont.getFont(DojaFont.SIZE_TINY);
	/** �t�H���g�̃f�B�Z���g */
	private static final int FONT_DECENT = FONT.getDescent();
	/** �t�H���g�̃A�Z���g */
	private static final int FONT_ASCENT = FONT.getAscent();
	/** �t�H���g�̍��� */
	private static final int FONT_HEIGHT = FONT_ASCENT + FONT_DECENT;
	
	/** DB�����x�ɓǂݍ��ރf�[�^�� */
	public static int numOfDataReadDB;
	
	//================================�ϐ�====================================//
		
	/** �J�����g�g�`�̕\���̏k���{��(X������)*/
	private int xReductionRate;
	/** �J�����g�g�`�̕\���̔{��(Y������)*/
	private int yScale = 1;
	
	/** ���[�h */
	int mode = -1;
	
	/** ��M�f�[�^ */
	private int[] recieveData;	
	/** �`��p�f�[�^ */
	private int[] drawData;
	
	/** �� */
	private int width;
	/** ���� */
	private int height;
		
	/** �g�`�̕\���̃I�t�Z�b�g */
	private int offset = 0;
	
	/**
	 *  �ő�f�[�^���i������Z�b�g���Ă����Ƃ���ȏ�̃f�[�^��DB����ǂ܂Ȃ�)
	 *  �_�O���t�œ����f�[�^�����Z�b�g���Ă���
	 */ 
	private int maxdataLen = 0;
	
	/** 
	 * �g�`�̑S�̂̃C���f�b�N�X 
	 * �I�t�Z�b�g�͂��炽��DB����ǂނ��тɃ��Z�b�g����邪�A
	 * ���̃C���f�b�N�X�̓��Z�b�g���ꂸ�S�̂ł̕b��������o���̂�
	 * �g����
	 */
	private int index = 0;	
	
	/** 
	 * ���߂ēǂݍ��񂾃f�[�^�Ȃ�0
	 * �Ō�̃y�[�W�̉E�[�܂ł��čĂѓǂݍ��݁A�\��������+1
	 * �ŏ��̃y�[�W�̍��[�܂ł��čĂѓǂݍ��݁A�\��������-1
	 */
	private int pageIndex = 0;
	/** ���t���x�� */
	private String dateLabel ="";
	
	/** �f�[�^�̓�����UNIX���Ԃŕ\��������*/
	private long time = 0;
	
	/**
	 * �R���X�g���N�^
	 * @param canvas
	 * @param baseSbp
	 * @param w
	 * @param h
	 */
	public BpHrWaveRenderer(int width, int height){				
		this.width = width;
		this.height = height;
						
		//DB����̃f�[�^�ǂݍ��ݐ�
		numOfDataReadDB = (maxReductionRate*width*NUM_OF_PAGE);
		
		//X�������̔{��
		xReductionRate   = maxReductionRate;
		
		//�`��Y���W
		ground_y = height >> 1;
		
		//�`��p�f�[�^
		drawData = new int[width*maxReductionRate];
				
	}/* End of StaticWaveRenderer */
	
	/**
	 * ������
	 * �V���Ȕg�`��\������O�ɌĂяo��
	 */
	public void init(){
		offset = 0;
		pageIndex = 0;
		xReductionRate = maxReductionRate;
	}
	
	/**
	 * �`�揈�����s��
	 * @param g
	 */
	public void draw(DojaGraphics g){
		
		//�w�i�̓h��Ԃ�
		g.setColor(BG_COLOR);
		g.clearRect(0,0, width, height);		

		//���g�`�̕`��
		drawRawSignal(g);
			
	}//End of draw()
	
	/**
	 * �w�i�̖ڐ������`��
	 * @param g   Graphics�I�u�W�F�N�g
	 * @param glY �`��̊���W ECG,PLS�̃O�����h���C��
	 */
	private void drawBGLine(DojaGraphics g,int glY){
		//�`��F��ύX
		g.setColor(BG_LINE_COLOR);
		//�`��̊�̐���`��
		g.drawLine(0,glY,width,glY);

		//�`��F��ύX
		g.setColor(BG_LINE_COLOR2);
	    //�w�i�̖ڐ������`��(x�������̐�)
	    for (int i = 1; i < (NUM_OF_BGLINE>>1); i++) {
	    	//�㔼��
	    	int y = glY - i*BG_LINE_MARGIN;
	    	g.drawLine(0,y,width,y);
	    	//������
	    	y = glY + i * BG_LINE_MARGIN;
	        g.drawLine(0,y,width,y);
	    }
	    //�w�i�̖ڐ������`��(y�������̐�)
	    int startY = glY - bg_vertical_line_h;
	    int endY   = glY + bg_vertical_line_h;
	    for (int i = 1; i*BG_LINE_MARGIN < width; i++) {
	    	int x = i*BG_LINE_MARGIN ;
	        g.drawLine(x,startY,x,endY);
	    }
	    
	    //�c�̃�����
	    g.setColor(BG_STR_COLOR);
	    g.drawLine(3*BG_LINE_MARGIN,startY,3*BG_LINE_MARGIN, endY);
	    		
	}
	
	//-----------------------���g�`�̕\��----------------------------//
	/**
	 * �S�d�}�▬�g�̐��g�`�`��
	 * @param g Graphics �I�u�W�F�N�g
	 */
	private void drawRawSignal(DojaGraphics g){
		//�t�H���g�̐ݒ�
		g.setFont(FONT);
		
		//�o�b�N�O���E���h�̕`��
		drawBGLine(g,ground_y);
			
		//�F�̕ύX		
		g.setColor(ECG_COLOR);				
		//�M���̕`��
		drawSignal(g,drawData,ground_y);
				
		//���Ԏ��̕`��
		final int baseY = ground_y + bg_vertical_line_h ;
		final int topY  = ground_y - bg_vertical_line_h ;
		int y = baseY + BG_LINE_MARGIN;
		g.setColor(BG_STR_COLOR);
			
		//���x���̕`��
		final int xMargin = 5;
		int x = width>>1;
		y = topY - FONT_DECENT - 5;
		g.drawString(dateLabel,x,y);
	
		String str = "";
		if(xReductionRate == 1){
			//str = "���g�� ���k�� : x "+xReductionRate +" ���O ����";
			str = "x "+xReductionRate +" ���O ����";
		}else{
			//str = "���g�� ���k�� : x 1/"+xReductionRate +" ���O ����";;
			str = "x 1/"+xReductionRate +" ���O ����";
		}		
		x = xMargin + (FONT.stringWidth(str)>>1);
		y = baseY + BG_LINE_MARGIN + FONT_ASCENT ;		
		g.drawString(str,x,y);
	}
		
	/**
	 * �M���̕`��
	 * @param g      DojaGraphics
	 * @param signal 
	 * @param glY
	 * @param offset
	 */
	private void drawSignal(DojaGraphics g, int[] signal,int glY){
				
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
		
		double scale= ((double)(bg_vertical_line_h<<1)/(double)(max))*yScale;

		//�g�`�̕`��
		// (offset + i < recieveData.length) �ɂ��f�[�^�̒[�܂ŗ����炻��ȏ�`�悵�Ȃ��Ƃ������Ƃ�����
		for(int i = 0;
		   ((i+1)*xReductionRate < signal.length) && 
	       (offset + (i+1)*xReductionRate < recieveData.length);i++){
			
			g.drawLine(i  ,glY + bg_vertical_line_h - (int)(signal[i*xReductionRate]*scale ) 
					  ,i+1,glY + bg_vertical_line_h - (int)(signal[(i+1)*xReductionRate]*scale));
			
		}
		
		g.setColor(BG_STR_COLOR);
		//�c�̃��������ő�l����ݒ�
		int heightY = (bg_vertical_line_h<<1);
		String str = "";
		int x = 0;
		int y = 0;
		
		for(int i = 0; i*BG_LINE_MARGIN <= heightY; i+=4){
		
			str = (int)(max * ((double)(i*BG_LINE_MARGIN)/(double)heightY)) + " ";									     					
	
			x   = (3*BG_LINE_MARGIN) - (FONT.stringWidth(str)>>1);
			y   =  glY + bg_vertical_line_h - i*BG_LINE_MARGIN + (FONT_ASCENT>>1);						
			g.drawString(str,x,y);
			
			x   = (3*BG_LINE_MARGIN);
			y   =  glY + bg_vertical_line_h - i*BG_LINE_MARGIN ;
			g.drawLine(x, y , x+3, y);
		}
			
	}//End of drawSignal()
	
	/**
	 * �`�惂�[�h��ύX
	 * @param mode
	 */
	public void setMode(int mode){
		this.mode = mode;
	}
	
	/**
	 * �g�`�\���ʒu��ύX
	 */
	private void setOffset(int offset){		
		
		if(recieveData.length > drawData.length + offset){
			for(int  i = 0 ; i < drawData.length ; i++){
				drawData[i] = recieveData[i+offset];
			}						
		}else{			
			int  i = 0;
			
			if(offset < recieveData.length){
				//�ꕔ�g�`�����݂���Ƃ�
				for(i = 0; i < recieveData.length - offset ; i++){
					drawData[i] = recieveData[i+offset];
				}				
				for(i = recieveData.length - offset; i < drawData.length ; i++){
					drawData[i] = 0;
				}	
			}else{
				//�g�`���܂������Ȃ��Ƃ�
				for(i = 0; i < drawData.length ; i++){
					drawData[i] = 0;
				}
			}						
		}
	}
	
	//========================���E�̈ړ�=========================//
	
	/**
	 * ���̔g�`��\��
	 * �[�܂ŗ�����DB����ǂݍ��݂�����
	 * 
	 * @return DB���玟�̃f�[�^��ǂݍ��ނƂ���DB��ł̃f�[�^�̃I�t�Z�b�g��Ԃ�
	 *         �ǂݍ��ޕK�v���Ȃ����-1
	 */
	public int nextWave(){
		int dbOffset = -1;
		//���̃C���f�b�N�X
		int nextIndex = offset + (width*xReductionRate);
		System.out.println("#nextWave()" + nextIndex);
		
		if(numOfDataReadDB <= nextIndex && 
		   index + numOfDataReadDB < maxdataLen ){
			//�����o�b�t�@�ɂȂ��Ƃ��A���܂�DB����ǂރf�[�^������Ƃ�
			//DB����f�[�^��ǂݍ���ŕ\������
			System.out.println("DB����ǂݍ���");
			//�S�̂̃C���f�b�N�X
			index  = pageIndex*numOfDataReadDB + nextIndex;
			//DB�ォ��ǂݍ��ރf�[�^�̃I�t�Z�b�g
			dbOffset = index;
			//�y�[�W������₷			
			pageIndex++;
			//�y�[�W���I�t�Z�b�g��0��			
			offset = 0;
		}else if(nextIndex < xReductionRate*recieveData.length){		
			//���̔g�`��\��
			offset = nextIndex;			
			index  = pageIndex*numOfDataReadDB + nextIndex;			
			//��ʒ���(�`�悷��g�`�̕ύX)		
			setOffset(offset);			
		}//End of nextWave()
		
		//DEBG
		System.out.println("offset     " + offset);
		System.out.println("index      " + index);
		System.out.println("page       "  + pageIndex);
		System.out.println("nextIndex  "  + nextIndex);
		System.out.println("maxDataLen  "  + maxdataLen);
		System.out.println("numOfDataReadDB "  + numOfDataReadDB);		
		System.out.println();
		
		return dbOffset;
	}//End of nextWave()
	
	/**
	 * ���O�̔g�`��\��
	 * �������͕\�����Ȃ�
	 * 
	 * @return DB���玟�̃f�[�^��ǂݍ��ނƂ���DB��ł̃f�[�^�̃I�t�Z�b�g��Ԃ�
	 *         �ǂݍ��ޕK�v���Ȃ����-1
	 */
	public int previousWave(){
		int dbOffset = -1;
		//���̃C���f�b�N�X
		int nextIndex = offset - (width*xReductionRate);
		System.out.println("#previousWave()" + nextIndex);
		
		if(nextIndex < 0){
			//����ȏ�Ȃ��Ƃ��A
			if(pageIndex == 0){
				//�ŏ��̃y�[�W�̂Ƃ��͂Ȃɂ����Ȃ�
				offset = 0;	
				index = 0;
			}else{
				//�܂��߂�y�[�W������Ƃ���
				//DB����f�[�^��ǂݍ���ŕ\������
				System.out.println("DB����ǂݍ���");
				//�S�̂̃C���f�b�N�X
				index  = pageIndex*numOfDataReadDB + nextIndex; 
				//DB�ォ��ǂݍ��ރf�[�^�̃I�t�Z�b�g
				dbOffset = index;
				//�y�[�W���I�t�Z�b�g��O�̃y�[�W�̍Ō��
				offset = pageIndex*numOfDataReadDB - (width*xReductionRate);
				//�y�[�W����߂�
				pageIndex--;				 
			}//End of if()			
		}else{
			offset = nextIndex;
			index  = pageIndex*numOfDataReadDB + nextIndex;
			//��ʒ���(�`�悷��g�`�̕ύX)		
			setOffset(offset);
		}//End of if()
		
		//DEBG
		System.out.println("offset     " + offset);
		System.out.println("index      " + index);
		System.out.println("page       "  + pageIndex);
		System.out.println("nextIndex  "  + nextIndex);	
		System.out.println("maxDataLen  "  + maxdataLen);
		System.out.println("numOfDataReadDB "  + numOfDataReadDB);		
		System.out.println();
					
		return dbOffset;
	}//End of previousWave()
	
	//=======================�g�嗦�̒���========================//

	/**
	 * x�������̏k���{����ύX
	 * @param xReductionRate x�������̏k���{��
	 */
	public void setXReductionRate(int xReductionRate){		
		if(maxReductionRate < xReductionRate
		 ||minReductionRate > xReductionRate )return;

		this.xReductionRate = xReductionRate;
		setOffset(offset);
	}

	/**
	 * x�������̏k���{���𓾂�
	 * @return x�������̏k���{��
	 */
	public int getXReductionRate() {		
		return xReductionRate;
	}
	
	//======================ReadDBListener======================//
	/**
	 * DB����̓ǂݍ��݂����������Ƃ��̌Ă΂�� 
	 */
	public void onCompleteReadDB(int[] data,int type,long time) {
		System.out.println("#onCompleteReadDB@staticWave,BPHRWAVE");
		
		//������ۑ�
		this.time = time;		
		
		//�ǂݍ��񂾃f�[�^��ۑ����Ă���
		recieveData = new int[data.length];
		for(int i = 0; i < data.length ; i++){
			recieveData[i] = data[i];
		}/* End of for() */
		
		//���x�����X�V
		int[] dateArray = CalendarUtil.calcTimeInfo(time);
		
		String typeStr = "";
		if(type == ReadDBData.TYPE_BP  || type == ReadDBData.TYPE_YEAR_BP ){
			
			typeStr ="����";
			
		}else if(type == ReadDBData.TYPE_HR    || type == ReadDBData.TYPE_YEAR_HR ){
			
			typeStr ="�S����";
			
		}//End of if
		
		dateLabel = typeStr +" " + dateArray[1] +"�� " + dateArray[2] + "��";
		
		setOffset(offset);
		
	}/* End of onCompleteReadDB() */
	
	/**
	 * DB����̓ǂݍ��݂����s�����Ƃ��̌Ă΂��
	 */
	public void onErrorReadDB() {
		System.out.println("#onErrorReadDB@waitScreen");
	}/* End of onErrorReadDB */
 
	
	//---------------------set,get----------------------//
	/**
	 * �ő�f�[�^�����Z�b�g���Ă���
	 * @param maxdataLen
	 */
	public void setMaxdataLen(int maxdataLen) {
		this.maxdataLen = maxdataLen;
	}
	
	/**
	 * �g�`�̍ő�f�[�^���𓾂�B
	 * @return �g�`�̍ő�f�[�^��
	 */
	public int getMaxdataLen() {
		return maxdataLen;
	}
	
	/**
	 * DB����̎擾�f�[�^�̎����p�����[�^�𓾂�
	 * @return DB����̎擾�f�[�^�̎����p�����[�^
	 */
	public long getTime() {
		return time;
	}
}
