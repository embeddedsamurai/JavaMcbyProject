package calendar;


import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;

import calendar.renderer.RollMenuRenderer;
import calendar.renderer.RawWaveRenderer;
import calendar.renderer.TripleRotateMenuRenderer;
import calendar.util.CalendarUtil;
import calendar.util.ReadDBData;
import calendar.util.ReadDBListener;
import calendar.util.ReadDBThread;
import calendar.util.ReadNumOfDBData;

import request.Key;
import request.Request;
import request.RequestQueue;

import main.Main;


public class CalendarManager implements CommandListener,ReadDBListener{

	//=================================�萔==================================//
	//��Ԃ�\���萔
	/** �J�����_�[ */
	private static final int STATUS_CALENDAR = 0;
	/** ���j���[ */
	private static final int STATUS_ROLL_MENU = 1;
	/** ���j���[ */
	private static final int STATUS_ROTATE_MENU = 2;
	/** ���g�` */
	private static final int STATUS_RAW_WAVE = 3;
	/** BP,HR�g�` */
	private static final int STATUS_BPHR_WAVE = 4;
	/** �_�O���t(��) */
	private static final int STATUS_DAILY_BAR_GEAPH = 6;
	/** �_�O���t(�T) */
	private static final int STATUS_WEEK_BAR_GEAPH = 7;
	/** �_�O���t(��) */
	private static final int STATUS_MONTH_BAR_GEAPH = 8;
	/** �_�O���t(�N) */
	private static final int STATUS_YEAR_BAR_GEAPH = 9;	
	/** Wait */
	private static final int STATUS_WAIT = 10;
	
	//���N�G�X�g
	public static final int LEFT_REQ  = 0;
	public static final int RIGHT_REQ = 1;
	public static final int UP_REQ   = 2;
	public static final int DOWN_REQ  = 3;
	public static final int ENTER_REQ  = 4;	
	public static final int READDB_REQ  = 5;

	//=================================�ϐ�==================================//
	//��Ԃ�\���ϐ�
	/** ��� */
	private int status = STATUS_CALENDAR;
	/** �L�[��� */
	private int keyStatus = STATUS_CALENDAR;
	/** �ЂƂO�̏�� */
	private int preStatus = STATUS_CALENDAR;
	  
	/** �J�����_�[�L�����o�X */	
	private CalendarCanvas canvas;
	/** �߂�R�}���h*/
	private Command backCmd = new Command("�߂�", Command.SCREEN, 0);
	
	/** �e�X���b�h */
	private Main parent;
	
	/** �L�[��ۑ����Ă����L���[ */
	private RequestQueue requestQueue;
	
	/** DB����f�[�^��ǂݍ���ł��邩�ǂ����̃t���O */
	private boolean readDBFlag = false;
	/** DB����̃f�[�^�ǂݍ��ݗp�N���X */
	private ReadDBThread readDB;
	
	/** ���j���[��`�悷�邩�ǂ����̃t���O */
	private boolean rollmenuFlag = false;		 
	
	//=================================�֐�==================================//	
	/**
	 * �R���X�g���N�^
	 * @param parent �e�X���b�h
	 */
	public CalendarManager(Main parent,MIDlet midlet) {
		if(Main.DEBUG)System.out.println("CalendarManger#constructor()");
		this.parent = parent;		
		
		//�L�����o�X�𐶐�
		canvas = new CalendarCanvas(midlet);
		//�R�}���h���X�i�Ƃ��Đݒ�
		canvas.setCommandListener(this);
		//�߂�R�}���h��ǉ�
		canvas.addCommand(backCmd);

		//�L�[��ۑ����Ă����L���[
		requestQueue = new RequestQueue();
	}
	
	/**
	 * �J�n
	 */
	public void start(){
		if(Main.DEBUG)System.out.println("CalendarManger#start()");
	}

	/**
	 * �I��
	 */
	public void exit(){
		if(Main.DEBUG)System.out.println("CalendarManger#exit()");		
		//�^�C�g�����[�h�ɖ߂�
		parent.setMode(Main.TITLE_MODE);
	}

	//===============================���C���̏���==================================//
	
	/**
	 * ���C���̏��� 
	 */
	public void process(){
		try {
			//�L�[���X�V
			if(canvas.isShown())Key.registKeyEvent();
			//�L�[����
			key();
			//���N�G�X�g�̏���
			doRequest();
			//�`��			
			draw();			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * �`�揈��
	 */
	private void draw(){ 
		if(status == STATUS_CALENDAR){
			canvas.drawCalender();			
		}else if(status == STATUS_ROTATE_MENU){
			canvas.drawTrippleRotateMenu();
		}else if(status == STATUS_RAW_WAVE){
			canvas.drawRawWave();
		}else if(status == STATUS_BPHR_WAVE){
			canvas.drawBpHrWave();
		}else if(status == STATUS_DAILY_BAR_GEAPH){
			canvas.drawDailyBarGraph();
		}else if(status == STATUS_WEEK_BAR_GEAPH){
			canvas.drawWeekBarGraph();
		}else if(status == STATUS_MONTH_BAR_GEAPH){
			canvas.drawMonthBarGraph();
		}else if(status == STATUS_YEAR_BAR_GEAPH){
			canvas.drawYearBarGraph();
		}
	}/* End of draw() */
	
	/**
	 * �L�[����
	 */
	private void key(){
		if(!canvas.isShown())return; //�\�����łȂ����͂Ȃɂ����Ȃ�		
		
		if(Key.isKeyPressed(Canvas.FIRE)){
			//����L�[���������Ƃ�
			requestQueue.putRequest(new Request(ENTER_REQ));
		}else if(Key.isKeyPressed(Canvas.LEFT)){
			//���L�[���������Ƃ�
			requestQueue.putRequest(new Request(LEFT_REQ));			
		}else if(Key.isKeyPressed(Canvas.RIGHT)){
			//�E�L�[���������Ƃ�
			requestQueue.putRequest(new Request(RIGHT_REQ));			
		}else if(Key.isKeyPressed(Canvas.UP)){
			//��L�[���������Ƃ�
			requestQueue.putRequest(new Request(UP_REQ));
		}else if(Key.isKeyPressed(Canvas.DOWN)){
			//���L�[���������Ƃ�
			requestQueue.putRequest(new Request(DOWN_REQ));
		}//End of if
	}//End of key()
	
	
	//============================���N�G�X�g����==================================//
	/**
	 * ���N�G�X�g�̏���
	 */
	private void doRequest(){
		
		Request req = requestQueue.getRequest();
		
		if(req != null){
			switch (keyStatus) {
			case STATUS_CALENDAR:
				//�J�����_�[��Ԃ̂Ƃ�
				calendarRequest(req);
				break;
			case STATUS_ROLL_MENU:
				//���[�����j���[��Ԃ̂Ƃ�
				rollMenuRequest(req);
				break;
			case STATUS_ROTATE_MENU:
				//��]���j���[�̂Ƃ�
				triRotateMenuRequest(req);
				break;
			case STATUS_WAIT:
				//Wait���j���[�̂Ƃ�
				waitScreenRequest(req);
				break;
			case STATUS_RAW_WAVE:
				//�g�`��Ԃ̂Ƃ�
				rawWaveRequest(req);
				break;
			case STATUS_BPHR_WAVE:
				//BP,HR�g�`��Ԃ̂Ƃ�
				bpHrWaveRequest(req);
				break;
			case STATUS_DAILY_BAR_GEAPH:
				//�_�O���t��Ԃ̂Ƃ�
				dailyBarGraphRequest(req);
				break;
			case STATUS_WEEK_BAR_GEAPH:
				//�_�O���t��Ԃ̂Ƃ�
				weekBarGraphRequest(req);
				break;
			case STATUS_MONTH_BAR_GEAPH:
				//�_�O���t��Ԃ̂Ƃ�
				monthBarGraphRequest(req);
				break;
			case STATUS_YEAR_BAR_GEAPH:
				//�_�O���t��Ԃ̂Ƃ�
				yearBarGraphRequest(req);
				break;
			}//end of switch
			
		}//end of if
		
	}//end of doRequest
	
	/**
	 * �J�����_�[��Ԃ̂Ƃ��̃��N�G�X�g�̏���
	 * @param req
	 */
	private void calendarRequest(Request req){

		switch (req.getCommand()) {
		case ENTER_REQ:
			if(!canvas.calendar.isSelectDay()){
				//�����I������Ă��Ȃ�������Ȃɂ����Ȃ�
				return;
			}
			//��]���j���[�̕\��
			preStatus = status;
			status = STATUS_ROTATE_MENU;
			keyStatus = STATUS_ROTATE_MENU;
			canvas.triRotatemenu.setMode(TripleRotateMenuRenderer.MODE_DAY);
			canvas.triRotatemenu.posInit();
			break;			
		case LEFT_REQ:
			//�t�H�[�J�X������
			canvas.calendar.left();
			break;
		case RIGHT_REQ:
			//�t�H�[�J�X���E��
			canvas.calendar.right();
			break;
		case UP_REQ:
	        canvas.calendar.up();
			break;
		case DOWN_REQ:
			//�t�H�[�J�X������
	        canvas.calendar.down();
			break;
		}//end of switch
	}//end of calenderRequest
	
	/**
	 * ���[�����j���[��Ԃ̂Ƃ��̃��N�G�X�g�̏���
	 * @param req
	 */
	private void rollMenuRequest(Request req){
		
		int com = req.getCommand();
		switch (com) {
		case ENTER_REQ:			
			break;
		case RIGHT_REQ:
			//�t�H�[�J�X���E�ֈړ�
			System.out.println("�E");
	        canvas.rollMenu.action(RollMenuRenderer.RIGHT);
	        //DB����f�[�^�ǂݍ���
	        displayBarGraph();
			break;
		case LEFT_REQ:
			//�t�H�[�J�X�����ֈړ�
			System.out.println("��");
	        canvas.rollMenu.action(RollMenuRenderer.LEFT);
	        //DB����f�[�^�ǂݍ���
	        displayBarGraph();
			break;
		case UP_REQ:
			//�t�H�[�J�X����ֈړ�
	        keyStatus = status;
			canvas.rollMenu.setFocus(false);
			canvas.rollMenu.action(RollMenuRenderer.INACTIVE);	        
			break;			
		case DOWN_REQ:
			//�t�H�[�J�X�����ֈړ�
	        keyStatus = status;
			canvas.rollMenu.setFocus(false);
			canvas.rollMenu.action(RollMenuRenderer.INACTIVE);	        
			break;		
		}//end of switch
	}//end of rollmenuRequest()
	
	/**
	 * ��]���j���[�̃��N�G�X�g�̏���
	 * @param req ���N�G�X�g
	 */
	private void triRotateMenuRequest(Request req){
		int com = req.getCommand();								
		
		switch (com) {	      
		case ENTER_REQ:			
			canvas.removeCommand(backCmd);
			//DB����̓ǂݍ���			
			displayBarGraph();
        break;
		case UP_REQ:
			//���
			canvas.triRotatemenu.action(TripleRotateMenuRenderer.UP);
		break;
		case DOWN_REQ:
			//����
			canvas.triRotatemenu.action(TripleRotateMenuRenderer.DOWN);				
        break;	      
		}//end of switch()
	}//end of triRotateMenuRequest()

	/**
	 * Wait��ʂ̃��N�G�X�g�̏���
	 * @param req ���N�G�X�g
	 */
	private void waitScreenRequest(Request req){
		int com = req.getCommand();						
		
		switch(com){
      	case ENTER_REQ:
      		if(canvas.waitScreen.isMenuFlag()){
      			//���j���[���I���̂Ƃ�
          		int index = canvas.waitScreen.getIndex();
          		
          		if(index == 0){          			        			
          			//���X�i�̉���
          			if(readDB != null){
          				readDB.deleteListener(canvas.rawWave);
        				readDB.deleteListener(this);	
          			}    				
    				//�X���b�h�̔j���A��]���j���[�֖߂�
    				onErrorReadDB();
          		} else{
          			//DB����̓ǂݍ��݂��p��,���j���[������
          			canvas.waitScreen.menuOff();          			
          		}//End of if()
          		
      		}else{
      			//���j���[���I�t�̂Ƃ�
      			canvas.waitScreen.menuOn();
      		}//End of if() 
      		break;
      	case UP_REQ:
      		canvas.waitScreen.up();
      		break;
      	case DOWN_REQ:
      		canvas.waitScreen.down();      		
      		break;
	    }//End of switch
	}//End of waitScreenRequest()
	
	/**
	 * �g�`��ʂ̂Ƃ��̃��N�G�X�g�̏���
	 * @param req ���N�G�X�g
	 */
	private void rawWaveRequest(Request req){
		int com = req.getCommand();
		int offset = 0;
	    switch(com){
	    case UP_REQ:
	    	//x�������̊g�嗦�̑���	    	
	    	canvas.rawWave.setXReductionRate(canvas.rawWave.getXReductionRate()+1);
	    	break;
	    case DOWN_REQ:
	    	//x�������̊g�嗦�̌���
	    	canvas.rawWave.setXReductionRate(canvas.rawWave.getXReductionRate()-1);
	    	break;
	    case LEFT_REQ:
	    	//�O�̔g�`��\��
	    	offset = canvas.rawWave.previousWave();
	    	if(offset != -1){
	    		//DB����f�[�^�̂̓ǂݍ���
	    		displayRawWave(offset);
	    	}
	    	break;
	    case RIGHT_REQ:
	    	//���̔g�`��\��
	    	offset = canvas.rawWave.nextWave();
	    	if(offset != -1){
	    		//DB����f�[�^�̓ǂݍ���
	    		displayRawWave(offset);
	    	}
	    	break;
	    }//End of switch()	    
	}//End of rawWaveRequest()
	
	/**
	 * BP,HR�g�`�`���Ԃ̂Ƃ��̃��N�G�X�g�̏���
	 * @param req
	 */
	private void bpHrWaveRequest(Request req){
		int com = req.getCommand();
		int offset = 0;
	    switch(com){
	    case UP_REQ:
	    	//x�������̊g�嗦�̑���	    	
	    	canvas.bpHrWave.setXReductionRate(canvas.bpHrWave.getXReductionRate()+1);
	    	break;
	    case DOWN_REQ:
	    	//x�������̊g�嗦�̌���
	    	canvas.bpHrWave.setXReductionRate(canvas.bpHrWave.getXReductionRate()-1);
	    	break;
	    case LEFT_REQ:
	    	//�O�̔g�`��\��
	    	offset = canvas.bpHrWave.previousWave();
	    	if(offset != -1){
	    		//DB����f�[�^�̂̓ǂݍ���
	    		displayRawWave(offset);
	    	}
	    	break;
	    case RIGHT_REQ:
	    	//���̔g�`��\��
	    	offset = canvas.bpHrWave.nextWave();
	    	if(offset != -1){
	    		//DB����f�[�^�̓ǂݍ���
	    		displayRawWave(offset);
	    	}
	    	break;
	    }//End of switch()	
	}//End of bpHrWaveRequest()
	
	/**
	 * �_�O���t��Ԃ̂Ƃ��̃��N�G�X�g�̏���
	 * @param req ���N�G�X�g
	 */
	private void ecgPlsBarGraphRequest(Request req){
		int com = req.getCommand();
	    switch(com){
	    case ENTER_REQ:
	    	//�o�[�̃N���b�N
	    	//�I���������Ԃ̐��g�`���擾����	    	
	    	displayRawWave(0);
	    	//�g�`�C���X�^���X�̏�����
	    	canvas.rawWave.init();
	    	break;
	    case UP_REQ:
	    	break;
	    case DOWN_REQ:
	    	break;
	    case LEFT_REQ:
	    	//�O�̃o�[�̑I��
	    	canvas.dailyBarGraph.previousBar();
	    	break;
	    case RIGHT_REQ:	        
	    	//���̃o�[�̑I��
	    	canvas.dailyBarGraph.nextBar();
	    	break;
	    }//End of switch()
	}//End of barGraphRequest()
	
	private void dailyBarGraphRequest(Request req){
		int com = req.getCommand();
	    switch(com){
	    case ENTER_REQ:
	    	//�o�[�̃N���b�N
	    	//�I���������Ԃ̐��g�`���擾����	    	
	    	displayRawWave(0);
	    	//�g�`�C���X�^���X�̏�����
	    	canvas.bpHrWave.init();
	    	break;
	    case UP_REQ:
	    	//�������
	    	if(rollmenuFlag){
	    		//���j���[��\��
				canvas.rollMenu.setFocus(true);
				canvas.rollMenu.action(RollMenuRenderer.ACTIVE);
				keyStatus = STATUS_ROLL_MENU;
	    	}
	    	break;
	    case DOWN_REQ:
	    	//��������
	    	if(rollmenuFlag){
	    		//���j���[��\��
				canvas.rollMenu.setFocus(true);
				canvas.rollMenu.action(RollMenuRenderer.ACTIVE);
				keyStatus = STATUS_ROLL_MENU;
	    	}
	    	break;
	    case LEFT_REQ:
	    	//�O�̃o�[�̑I��
	    	canvas.dailyBarGraph.previousBar();
	    	break;
	    case RIGHT_REQ:	        
	    	//���̃o�[�̑I��
	    	canvas.dailyBarGraph.nextBar();
	    	break;
	    }//End of switch()
	}//End of dailyBarGraphRequest() 
	
	/**
	 * �_�O���t(�T)��Ԃ̂Ƃ��̃��N�G�X�g�̏���
	 * @param req ���N�G�X�g
	 */
	private void weekBarGraphRequest(Request req){
		int com = req.getCommand();
	    switch(com){
	    case ENTER_REQ:
	    	//�o�[�̃N���b�N
	    	//�I���������Ԃ̐��g�`���擾����	    	
	    	displayRawWave(0);
	    	//�g�`�C���X�^���X�̏�����
	    	canvas.bpHrWave.init();	    	
	    	break;
	    case UP_REQ:
	    	//�������
	    	if(rollmenuFlag){
	    		//���j���[��\��
				canvas.rollMenu.setFocus(true);
				canvas.rollMenu.action(RollMenuRenderer.ACTIVE);
				keyStatus = STATUS_ROLL_MENU;
	    	}
	    	break;
	    case DOWN_REQ:
	    	//��������
	    	if(rollmenuFlag){
	    		//���j���[��\��
				canvas.rollMenu.setFocus(true);
				canvas.rollMenu.action(RollMenuRenderer.ACTIVE);
				keyStatus = STATUS_ROLL_MENU;
	    	}
	    	break;
	    case LEFT_REQ:
	    	//�O�̃o�[�̑I��
	    	canvas.weekBarGraph.previousBar();
	    	break;
	    case RIGHT_REQ:	        
	    	//���̃o�[�̑I��
	    	canvas.weekBarGraph.nextBar();
	    	break;
	    }//End of switch()
	}//End of weekBarGraphRequest()	
	
	/**
	 * �_�O���t(���j��Ԃ̂Ƃ��̃��N�G�X�g����
	 * @param req
	 */
	private void monthBarGraphRequest(Request req){
		int com = req.getCommand();
	    switch(com){
	    case ENTER_REQ:
	    	//�o�[�̃N���b�N
	    	//�I���������Ԃ̐��g�`���擾����	    	
	    	displayRawWave(0);
	    	//�g�`�C���X�^���X�̏�����
	    	canvas.bpHrWave.init();
	    	break;
	    case UP_REQ:
	    	//�������
	    	if(rollmenuFlag){
	    		//���j���[��\��
				canvas.rollMenu.setFocus(true);
				canvas.rollMenu.action(RollMenuRenderer.ACTIVE);
				keyStatus = STATUS_ROLL_MENU;
	    	}
	    	break;
	    case DOWN_REQ:
	    	//��������
	    	if(rollmenuFlag){
	    		//���j���[��\��
				canvas.rollMenu.setFocus(true);
				canvas.rollMenu.action(RollMenuRenderer.ACTIVE);
				keyStatus = STATUS_ROLL_MENU;
	    	}
	    	break;
	    case LEFT_REQ:
	    	//�O�̃o�[�̑I��
	    	canvas.monthBarGraph.previousBar();
	    	break;
	    case RIGHT_REQ:	        
	    	//���̃o�[�̑I��
	    	canvas.monthBarGraph.nextBar();
	    	break;
	    }//End of switch()
	}//End of monthBarGraphRequest()
	
	/**
	 * �_�O���t�i�N)��Ԃ̂Ƃ��̃��N�G�X�g����
	 * @param req
	 */
	private void yearBarGraphRequest(Request req){
			int com = req.getCommand();
	    switch(com){
	    case ENTER_REQ:
	    	//�o�[�̃N���b�N
	    	//�I���������Ԃ̐��g�`���擾����	    	
	    	displayRawWave(0);
	    	//�g�`�C���X�^���X�̏�����
	    	canvas.bpHrWave.init();	    	
	    	break;
	    case UP_REQ:
	    	//�������
	    	if(rollmenuFlag){
	    		//���j���[��\��
				canvas.rollMenu.setFocus(true);
				canvas.rollMenu.action(RollMenuRenderer.ACTIVE);
				keyStatus = STATUS_ROLL_MENU;
	    	}
	    	break;
	    case DOWN_REQ:
	    	//��������
	    	if(rollmenuFlag){
	    		//���j���[��\��
				canvas.rollMenu.setFocus(true);
				canvas.rollMenu.action(RollMenuRenderer.ACTIVE);
				keyStatus = STATUS_ROLL_MENU;
	    	}
	    	break;
	    case LEFT_REQ:
	    	//�O�̃o�[�̑I��
	    	canvas.yearBarGraph.previousBar();
	    	break;
	    case RIGHT_REQ:	        
	    	//���̃o�[�̑I��
	    	canvas.yearBarGraph.nextBar();
	    	break;
	    }//End of switch()
	}//End of yearbarGraphRequest()
	
	/**
	 * DB�ǂݍ���,�_�O���t�̕\��
	 */
	private void displayBarGraph(){
		
		if(readDBFlag){
			//�ǂݍ��ݒ��̂Ƃ��͉������Ȃ�
			return;
		}
				
		//�ǂݍ��ރf�[�^�̃^�C�v
		int type = getDataType();							
		//DB��̓ǂݍ��݃t���O�𗧂Ă�
		readDBFlag = true;
		
		//���Ԃ��擾����
		long time = 0;				
		time = canvas.calendar.getCurrentDate();
		System.out.println("time:" + time);
		
		//DB����̓ǂݍ���		
		readDB = new ReadDBThread(type,2345,0,RawWaveRenderer.numOfDataReadDB,time);
		
		//DB����̓ǂݍ��݂��I������ƌĂ΂��C���X�^���X��o�^
		if(type == ReadNumOfDBData.TYPE_ECG_NUM || 
		   type == ReadNumOfDBData.TYPE_PLS_NUM || 
		   type == ReadNumOfDBData.TYPE_HR_NUM || 
		   type == ReadNumOfDBData.TYPE_BP_NUM  ){
			//���\��,BPHR
			readDB.addListener(canvas.dailyBarGraph);
		}else if(type == ReadNumOfDBData.TYPE_WEEK_ECG_NUM || 
				 type == ReadNumOfDBData.TYPE_WEEK_PLS_NUM || 
				 type == ReadNumOfDBData.TYPE_WEEK_BP_NUM ||
			     type == ReadNumOfDBData.TYPE_WEEK_HR_NUM){
			//�T�\��			
			readDB.addListener(canvas.weekBarGraph);			
		}else if(type == ReadNumOfDBData.TYPE_MONTH_ECG_NUM || 
			     type == ReadNumOfDBData.TYPE_MONTH_PLS_NUM ||
				 type == ReadNumOfDBData.TYPE_MONTH_BP_NUM || 
			     type == ReadNumOfDBData.TYPE_MONTH_HR_NUM){
			//���\��			
			readDB.addListener(canvas.monthBarGraph);			
		}else if(type == ReadNumOfDBData.TYPE_YEAR_ECG_NUM || 
			     type == ReadNumOfDBData.TYPE_YEAR_PLS_NUM ||
				 type == ReadNumOfDBData.TYPE_YEAR_BP_NUM || 
			     type == ReadNumOfDBData.TYPE_YEAR_HR_NUM){
			//�N�\��			
			readDB.addListener(canvas.yearBarGraph);			
		}
		
		readDB.addListener(this);
			
		//�ǂݍ��݂��J�n
		readDB.start();
		
		//�҂���ʂ̕\��
		canvas.waitScreen.action();
		//�҂���ʏ�Ԃ�
		keyStatus = STATUS_WAIT;
		
		if(rollmenuFlag){
			//���j���[���B��
			//���j���[���łĂ�Ƃ��͂����Ă���
			rollmenuFlag = false;
			canvas.rollMenu.setFocus(false);
			canvas.rollMenu.action(RollMenuRenderer.INACTIVE);		
		}//End of if()
			
	}//End of displayBarGraph()
	
	/**
	 * ���g�`�̕`��
	 * 
	 * @param offset �擾����f�[�^�̃I�t�Z�b�g
	 */
	private void displayRawWave(int offset){
		if(readDBFlag)return;
		
    	//�ǂݍ��ރf�[�^�̃^�C�v
		int type = getDataType();		
		
		//DB��̓ǂݍ��݃t���O�𗧂Ă�
		readDBFlag = true;
		
		//����
		long time = 0;
		//�ő�f�[�^��
		int maxlen = 0;
		
		//ECG,PLS�g�`�Ȃ�true,����ȊO��false;
		boolean flag = true;
				
		if(status == STATUS_RAW_WAVE){			
			//�g�`�\�����ɁA���̃f�[�^�܂��͑O�̃f�[�^���ēǂݍ��݂���Ƃ�			
			//���Ԃ̎擾
			time = canvas.rawWave.getTime();
			
		}else if(status == STATUS_DAILY_BAR_GEAPH || 
				 status == STATUS_WEEK_BAR_GEAPH  || 
				 status == STATUS_MONTH_BAR_GEAPH || 
				 status == STATUS_YEAR_BAR_GEAPH   ){
			//BP,HR�g�`(���߂ĕ\������Ƃ�)
			
			flag = false;
			
			//����
			int[] times = null;
						
			if(status == STATUS_DAILY_BAR_GEAPH ){
				//��
				times  = canvas.dailyBarGraph.getSelectedDate();
				maxlen = canvas.dailyBarGraph.getSelectedDataLen();				 
			}else if(status == STATUS_WEEK_BAR_GEAPH ){
				//�T
				times  = canvas.weekBarGraph.getSelectedDate();
				maxlen = canvas.weekBarGraph.getSelectedDataLen();				
			}else if(status == STATUS_MONTH_BAR_GEAPH ){ 
				//��
				times  = canvas.monthBarGraph.getSelectedDate();
				maxlen = canvas.monthBarGraph.getSelectedDataLen();				
			}else if(status == STATUS_YEAR_BAR_GEAPH ){
				//�N
				times  = canvas.yearBarGraph.getSelectedDate();
				maxlen = canvas.yearBarGraph.getSelectedDataLen();				
			}else{
				System.out.println("out" + type);
				return;
			}
			
			//�f�[�^�擾�Ɏ��s���Ă���Ƃ�
			if(times[0] == -1){
				return;		
			}
			//�P���Ԃ̃~���b��
			long hour  =  60 * 60 * 1000L;			
			//-(9*hour)�Ƃ����̂�getCurrentTime()�ŕԂ��Ă��鎞����
			//JST������9���ԁ{����Ă��邩��A�O�ɖ߂��K�v�����邽�߁B
			time  = CalendarUtil.getCurrentTime(times[0],times[1],times[2]-1) + (times[3]*hour - (9*hour));
			//�f�[�^�ő咷��o�^
			if(type == ReadDBData.TYPE_ECG || type == ReadDBData.TYPE_PLS){
				canvas.rawWave.setMaxdataLen(maxlen);
			}else if(type == ReadDBData.TYPE_BP || type == ReadDBData.TYPE_HR ||
					 type == ReadDBData.TYPE_YEAR_BP || type == ReadDBData.TYPE_YEAR_HR){
				canvas.bpHrWave.setMaxdataLen(maxlen);	
			}
						
		}else if(status == STATUS_BPHR_WAVE){			
			//�g�`�\�����ɁA���̃f�[�^�܂��͑O�̃f�[�^���ēǂݍ��݂���Ƃ�			
			//���Ԃ̎擾
			flag = false;
			
			time = canvas.bpHrWave.getTime();
			
		}else{
			return;
		}
		//DB����̓ǂݍ���
		readDB = new ReadDBThread(type,2345,offset,RawWaveRenderer.numOfDataReadDB,time);
		readDB.start();
		
		if(flag){
			//ECG,PLS
			readDB.addListener(canvas.rawWave);	
		}else{
			//BP,HR
			readDB.addListener(canvas.bpHrWave);
		}
		
		readDB.addListener(this);
		
		//�҂���ʂ̕\��
		canvas.waitScreen.action();
		//�҂���ʏ�Ԃ�
		keyStatus = STATUS_WAIT;
	}
	
	
	/**
	 * �R�}���h�̏���
	 * 
	 * @param com
	 * @param disp
	 */
	public void commandAction(Command com, Displayable disp) {
		if(Main.DEBUG)System.out.println("CalendarManger#commandAction()");
		
		if(com.equals(backCmd)){
			//�߂�R�}���h�����s�����Ƃ��̏���
			
			if(status == STATUS_CALENDAR ){
				//�J�����_�[�\�����Ȃ�I��
				exit();
			}else if(status == STATUS_ROTATE_MENU){
				//��]���j���[�Ȃ�O�̏�Ԃɖ߂�
				status = preStatus;
				keyStatus = preStatus;
			}else if(status == STATUS_RAW_WAVE || status == STATUS_BPHR_WAVE){
				//���g�`�̂Ƃ�
				int type = getDataType();
		    	  
				if(type == ReadNumOfDBData.TYPE_ECG_NUM||
				   type == ReadNumOfDBData.TYPE_PLS_NUM ||
    			   type == ReadNumOfDBData.TYPE_BP_NUM||
				   type == ReadNumOfDBData.TYPE_HR_NUM){					
					//�_�O���t��  					
					status = STATUS_DAILY_BAR_GEAPH;
					keyStatus = STATUS_DAILY_BAR_GEAPH;
					
				}else if(type == ReadNumOfDBData.TYPE_WEEK_ECG_NUM||
						 type == ReadNumOfDBData.TYPE_WEEK_PLS_NUM||
						 type == ReadNumOfDBData.TYPE_WEEK_BP_NUM||
						 type == ReadNumOfDBData.TYPE_WEEK_HR_NUM){					
					//�_�O���t��  					
					status = STATUS_WEEK_BAR_GEAPH;
					keyStatus = STATUS_WEEK_BAR_GEAPH;
					
				}else if(type == ReadNumOfDBData.TYPE_MONTH_ECG_NUM||
						 type == ReadNumOfDBData.TYPE_MONTH_PLS_NUM||
						 type == ReadNumOfDBData.TYPE_MONTH_BP_NUM||
						 type == ReadNumOfDBData.TYPE_MONTH_HR_NUM){					
					//�_�O���t��  					
					status = STATUS_MONTH_BAR_GEAPH;
					keyStatus = STATUS_MONTH_BAR_GEAPH;
					
				}else if(type == ReadNumOfDBData.TYPE_YEAR_ECG_NUM||
						 type == ReadNumOfDBData.TYPE_YEAR_PLS_NUM||
						 type == ReadNumOfDBData.TYPE_YEAR_BP_NUM ||
						 type == ReadNumOfDBData.TYPE_YEAR_HR_NUM){					
					//�_�O���t��  					
					status = STATUS_YEAR_BAR_GEAPH;
					keyStatus = STATUS_YEAR_BAR_GEAPH;
					
				}else{	
					//��]���j���[��		    		  
					status = STATUS_ROTATE_MENU;
					keyStatus = STATUS_ROTATE_MENU;
				}//End of if()
				
			}else if(status == STATUS_DAILY_BAR_GEAPH  ||
					 status == STATUS_WEEK_BAR_GEAPH   ||
					 status == STATUS_MONTH_BAR_GEAPH  || 
					 status == STATUS_YEAR_BAR_GEAPH){
				
				//�_�O���t��\�����Ă���Ƃ�
				if(rollmenuFlag){
					//���j���[���łĂ�Ƃ��͂����Ă���
					rollmenuFlag = false;
					canvas.rollMenu.setFocus(false);
					canvas.rollMenu.action(RollMenuRenderer.INACTIVE);
				}
				status = STATUS_ROTATE_MENU;
				keyStatus = STATUS_ROTATE_MENU;		          
			}//end of if()
		}//end of if()
	}//end of commandAction()
	
	//===============================���̑�================================//

	/**
	 * �\������
	 */
	public void setDisplay(MIDlet midlet){
		if(Main.DEBUG)System.out.println("BPMonitorManager#setDisplay()");
		((CalendarCanvas)canvas).setDisplay();		
	}
	
	/**
	 * �O�̏�ԂƁA�I���������j���[����A
	 * DB������o���f�[�^�̃^�C�v�𓾂�
	 * 
	 * @return�@DB������o���ׂ��f�[�^�̃^�C�v,�ǂ�ɂ��Y�����Ȃ��ꍇ�� -1
	 */
	private int getDataType(){
		
		String name = canvas.triRotatemenu.getItemName();
	
		//��		
		if(name.equals(TripleRotateMenuRenderer.ECG)){
			//�S�d�}
			if((status == STATUS_DAILY_BAR_GEAPH && keyStatus == STATUS_DAILY_BAR_GEAPH) ||
			   (status == STATUS_WEEK_BAR_GEAPH  && keyStatus == STATUS_WEEK_BAR_GEAPH)  ||
			   (status == STATUS_MONTH_BAR_GEAPH && keyStatus == STATUS_MONTH_BAR_GEAPH) || 
			   (status == STATUS_YEAR_BAR_GEAPH  && keyStatus == STATUS_YEAR_BAR_GEAPH)  ||
			    status == STATUS_RAW_WAVE){
				//�_�O���t�\�����L�[��Ԃ����_�O���t
				//(�L�[��Ԃ����[�����j���[�̂Ƃ���DB����̓ǂݍ��݂��s��Ȃ�����)
				//�܂��́A���̐��g�`��\������Ƃ� = STATUS_RAW_WAVE				
				
				//���g�`
				if(canvas.rollMenu.getIndex() != RollMenuRenderer.YEAR){
					//ECG�̂Ƃ��͔N�O���t��\�����Ȃ�
					return ReadDBData.TYPE_ECG;	
				}								
			}else{			
				//�_�O���t
				if(canvas.rollMenu.getIndex() == RollMenuRenderer.DAY){
					return ReadNumOfDBData.TYPE_ECG_NUM;	
				}else if(canvas.rollMenu.getIndex() == RollMenuRenderer.WEEK){
					return ReadNumOfDBData.TYPE_WEEK_ECG_NUM;
				}else if(canvas.rollMenu.getIndex() == RollMenuRenderer.MONTH){
					return ReadNumOfDBData.TYPE_MONTH_ECG_NUM;
				}else if(canvas.rollMenu.getIndex() == RollMenuRenderer.YEAR){
					return ReadNumOfDBData.TYPE_YEAR_ECG_NUM;
				}
			}			
		}else if(name.equals(TripleRotateMenuRenderer.PLS)){
			//���g
			if((status == STATUS_DAILY_BAR_GEAPH && keyStatus == STATUS_DAILY_BAR_GEAPH) ||
			   (status == STATUS_WEEK_BAR_GEAPH  && keyStatus == STATUS_WEEK_BAR_GEAPH)  ||
			   (status == STATUS_MONTH_BAR_GEAPH && keyStatus == STATUS_MONTH_BAR_GEAPH) || 
			   (status == STATUS_YEAR_BAR_GEAPH  && keyStatus == STATUS_YEAR_BAR_GEAPH)  ||
			   status == STATUS_RAW_WAVE){
				//�_�O���t�\�����L�[��Ԃ����_�O���t
				//(�L�[��Ԃ����[�����j���[�̂Ƃ���DB����̓ǂݍ��݂��s��Ȃ�����)
				//�܂��́A���̐��g�`��\������Ƃ� = STATUS_RAW_WAVE
				
				//���g�`
				if(canvas.rollMenu.getIndex() != RollMenuRenderer.YEAR){
					//PLS�̂Ƃ��͔N�O���t��\�����Ȃ�
					return ReadDBData.TYPE_PLS;	
				}						
			}else{
				//�_�O���t
				if(canvas.rollMenu.getIndex() == RollMenuRenderer.DAY){
					return ReadNumOfDBData.TYPE_PLS_NUM;	
				}else if(canvas.rollMenu.getIndex() == RollMenuRenderer.WEEK){
					return ReadNumOfDBData.TYPE_WEEK_PLS_NUM;
				}else if(canvas.rollMenu.getIndex() == RollMenuRenderer.MONTH){
					return ReadNumOfDBData.TYPE_MONTH_PLS_NUM;
				}else if(canvas.rollMenu.getIndex() == RollMenuRenderer.YEAR){
					return ReadNumOfDBData.TYPE_YEAR_PLS_NUM;
				}	
			}			
		}else if(name.equals(TripleRotateMenuRenderer.BP)){
			//����		
			
			if((status == STATUS_DAILY_BAR_GEAPH && keyStatus == STATUS_DAILY_BAR_GEAPH) ||
			   (status == STATUS_WEEK_BAR_GEAPH  && keyStatus == STATUS_WEEK_BAR_GEAPH)  ||
			   (status == STATUS_MONTH_BAR_GEAPH && keyStatus == STATUS_MONTH_BAR_GEAPH) || 
			   (status == STATUS_YEAR_BAR_GEAPH  && keyStatus == STATUS_YEAR_BAR_GEAPH)  || 
			   status == STATUS_BPHR_WAVE){
				//�_�O���t�\�����L�[��Ԃ����_�O���t
				//(�L�[��Ԃ����[�����j���[�̂Ƃ���DB����̓ǂݍ��݂��s��Ȃ�����)
				//�܂��́A���̐��g�`��\������Ƃ� = STATUS_BPHR_WAVE
				
				//���g�`
				if(canvas.rollMenu.getIndex() == RollMenuRenderer.DAY ||
				   canvas.rollMenu.getIndex() == RollMenuRenderer.WEEK || 
				   canvas.rollMenu.getIndex() == RollMenuRenderer.MONTH){
					
					return ReadDBData.TYPE_BP;										
				}else if(canvas.rollMenu.getIndex() == RollMenuRenderer.YEAR){
					return ReadDBData.TYPE_YEAR_BP;
				}
			}else{
				//�_�O���t
				if(canvas.rollMenu.getIndex() == RollMenuRenderer.DAY){
					return ReadNumOfDBData.TYPE_BP_NUM;	
				}else if(canvas.rollMenu.getIndex() == RollMenuRenderer.WEEK){
					return ReadNumOfDBData.TYPE_WEEK_BP_NUM;
				}else if(canvas.rollMenu.getIndex() == RollMenuRenderer.MONTH){
					return ReadNumOfDBData.TYPE_MONTH_BP_NUM;
				}else if(canvas.rollMenu.getIndex() == RollMenuRenderer.YEAR){
					return ReadNumOfDBData.TYPE_YEAR_BP_NUM;
				}
			}//End of if()
						
		}else if(name.equals(TripleRotateMenuRenderer.HR)){
			//�S��
			if((status == STATUS_DAILY_BAR_GEAPH && keyStatus == STATUS_DAILY_BAR_GEAPH) ||
			   (status == STATUS_WEEK_BAR_GEAPH  && keyStatus == STATUS_WEEK_BAR_GEAPH)  ||
			   (status == STATUS_MONTH_BAR_GEAPH && keyStatus == STATUS_MONTH_BAR_GEAPH) || 
			   (status == STATUS_YEAR_BAR_GEAPH  && keyStatus == STATUS_YEAR_BAR_GEAPH)  ||
			    status == STATUS_BPHR_WAVE){
				//�_�O���t�\�����L�[��Ԃ����_�O���t
				//(�L�[��Ԃ����[�����j���[�̂Ƃ���DB����̓ǂݍ��݂��s��Ȃ�����)
				//�܂��́A���̐��g�`��\������Ƃ� = STATUS_BPHR_WAVE
				
				//���g�`
				if(canvas.rollMenu.getIndex() == RollMenuRenderer.DAY ||
				   canvas.rollMenu.getIndex() == RollMenuRenderer.WEEK || 
				   canvas.rollMenu.getIndex() == RollMenuRenderer.MONTH){
							
					return ReadDBData.TYPE_HR;										
				}else if(canvas.rollMenu.getIndex() == RollMenuRenderer.YEAR){
					return ReadDBData.TYPE_YEAR_HR;
				}					
			}else{
				//�_�O���t
				if(canvas.rollMenu.getIndex() == RollMenuRenderer.DAY){
					return ReadNumOfDBData.TYPE_HR_NUM;	
				}else if(canvas.rollMenu.getIndex() == RollMenuRenderer.WEEK){
					return ReadNumOfDBData.TYPE_WEEK_HR_NUM;
				}else if(canvas.rollMenu.getIndex() == RollMenuRenderer.MONTH){
					return ReadNumOfDBData.TYPE_MONTH_HR_NUM;
				}else if(canvas.rollMenu.getIndex() == RollMenuRenderer.YEAR){
					return ReadNumOfDBData.TYPE_YEAR_HR_NUM;
				}
			}//End of if()			
		}//End of if()
		return -1;
	}
	
	//===============================ReadDBListener===============================//
	
	/**
	 * DB����̓ǂݍ��݂����������Ƃ��̌Ă΂�� 
	 */
	public void onCompleteReadDB(int[] data,int type,long time) {
		//�g�`�\����ʂֈڍs
		System.out.println("#onCompleteReadDB@manager");

		//�ǂݍ��ݗp�X���b�h��j��
		if(readDB != null){
			readDB.stop();
			readDB = null;	
		}		
		//�߂�R�}���h��߂�
		canvas.addCommand(backCmd);		
		//�ǂݍ��݃t���O���I�t��
		readDBFlag = false;
						
								
		if(type == ReadNumOfDBData.TYPE_ECG_NUM ||
		   type == ReadNumOfDBData.TYPE_PLS_NUM || 
		   type == ReadNumOfDBData.TYPE_BP_NUM  ||
		   type == ReadNumOfDBData.TYPE_HR_NUM ){
			//----------------�_�O���t(��)-----------------//
			System.out.println("�_�O���t(��)");
			status = STATUS_DAILY_BAR_GEAPH;						
			keyStatus = STATUS_ROLL_MENU;			
			
			rollmenuFlag = true;

			canvas.rollMenu.action(RollMenuRenderer.ACTIVE);
			canvas.rollMenu.setFocus(true);									
			
			//�҂���ʂ̕\������߂�
			canvas.waitScreen.stop();		
			
		}else if(type == ReadNumOfDBData.TYPE_WEEK_ECG_NUM ||
				 type == ReadNumOfDBData.TYPE_WEEK_PLS_NUM || 
				 type == ReadNumOfDBData.TYPE_WEEK_BP_NUM ||
			     type == ReadNumOfDBData.TYPE_WEEK_HR_NUM ){
			//----------------�_�O���t(�T)----------------//
			System.out.println("�_�O���t(�T)");

			if(type == ReadNumOfDBData.TYPE_WEEK_ECG_NUM ||
			   type == ReadNumOfDBData.TYPE_WEEK_PLS_NUM ){
				//�܂���O���t�p�f�[�^������Ă���
				
				status    = STATUS_WEEK_BAR_GEAPH;
				keyStatus = STATUS_ROLL_MENU;;
					
				//���j���[��\��
				rollmenuFlag = true;
				canvas.rollMenu.action(RollMenuRenderer.ACTIVE);
				canvas.rollMenu.setFocus(true);				
				
				//�҂���ʂ̕\������߂�
				canvas.waitScreen.stop();	
				
			}else{
				//�܂���O���t�p�̃f�[�^������Ă���		
				
				//DB����̓ǂݍ��݃t���O�𗧂Ă�
				readDBFlag = true;
				//�e���̕��σf�[�^�̎擾
				if(type == ReadNumOfDBData.TYPE_WEEK_BP_NUM){
					type = ReadDBData.TYPE_WEEK_BP_AVE;	
				}else{
					type = ReadDBData.TYPE_WEEK_HR_AVE;
				}		
				
				//DB����̓ǂݍ���		
				readDB = new ReadDBThread(type,2345,0,RawWaveRenderer.numOfDataReadDB,time);
				//�ǂݍ��݊������ɌĂ΂�郊�X�i�m�o�^
				readDB.addListener(this);
				readDB.addListener(canvas.weekBarGraph);
				readDB.start();
				
				//�҂���ʂ̕\��
				canvas.waitScreen.action();
				//�҂���ʏ�Ԃ�
				keyStatus = STATUS_WAIT;
				//�߂�R�}���h�̍폜
				canvas.removeCommand(backCmd);
			}
			
			
		}else if(type == ReadNumOfDBData.TYPE_MONTH_ECG_NUM ||
				 type == ReadNumOfDBData.TYPE_MONTH_PLS_NUM || 
				 type == ReadNumOfDBData.TYPE_MONTH_BP_NUM ||
			     type == ReadNumOfDBData.TYPE_MONTH_HR_NUM ){
			//----------------�_�O���t(��)----------------//
			System.out.println("�_�O���t(��)");

			if(type == ReadNumOfDBData.TYPE_MONTH_ECG_NUM ||
			   type == ReadNumOfDBData.TYPE_MONTH_PLS_NUM ){
				//�܂���O���t�͕\�����Ȃ�
				
				status    = STATUS_MONTH_BAR_GEAPH;
				keyStatus = STATUS_ROLL_MENU;;
					
				//���j���[��\��
				rollmenuFlag = true;
				canvas.rollMenu.action(RollMenuRenderer.ACTIVE);
				canvas.rollMenu.setFocus(true);				
				
				//�҂���ʂ̕\������߂�
				canvas.waitScreen.stop();	
				
			}else{
				//�܂���O���t�p�f�[�^������Ă���
				
				//DB����̓ǂݍ��݃t���O�𗧂Ă�
				readDBFlag = true;
				//�e���̕��σf�[�^�̎擾
				if(type == ReadNumOfDBData.TYPE_MONTH_BP_NUM){
					type = ReadDBData.TYPE_MONTH_BP_AVE;	
				}else{
					type = ReadDBData.TYPE_MONTH_HR_AVE;
				}			
				
				//DB����̓ǂݍ���		
				readDB = new ReadDBThread(type,2345,0,RawWaveRenderer.numOfDataReadDB,time);
				//�ǂݍ��݊������ɌĂ΂�郊�X�i�m�o�^
				readDB.addListener(this);
				readDB.addListener(canvas.monthBarGraph);
				readDB.start();
				
				//�҂���ʂ̕\��
				canvas.waitScreen.action();
				//�҂���ʏ�Ԃ�
				keyStatus = STATUS_WAIT;
				//�߂�R�}���h�̍폜
				canvas.removeCommand(backCmd);
				//�҂���ʂ̕\��
				canvas.waitScreen.action();
			}

						
		}else if(type == ReadNumOfDBData.TYPE_YEAR_ECG_NUM ||
				 type == ReadNumOfDBData.TYPE_YEAR_PLS_NUM || 
				 type == ReadNumOfDBData.TYPE_YEAR_BP_NUM ||
			     type == ReadNumOfDBData.TYPE_YEAR_HR_NUM ){
			//----------------�_�O���t(�N)----------------//
			System.out.println("�_�O���t(�N)");
			
			if(type == ReadNumOfDBData.TYPE_YEAR_ECG_NUM ||
				type == ReadNumOfDBData.TYPE_YEAR_PLS_NUM ){
				// �܂���O���t�͕\�����Ȃ�
						
				status    = STATUS_YEAR_BAR_GEAPH;
				keyStatus = STATUS_ROLL_MENU;;
						
				// ���j���[��\��
				rollmenuFlag = true;
				canvas.rollMenu.action(RollMenuRenderer.ACTIVE);
				canvas.rollMenu.setFocus(true);				
				
				// �҂���ʂ̕\������߂�
				canvas.waitScreen.stop();	
						
			}else{
				// DB��̓ǂݍ��݃t���O�𗧂Ă�
				readDBFlag = true;
				// �e���̕��σf�[�^�̎擾
				if (type == ReadNumOfDBData.TYPE_YEAR_BP_NUM) {
					type = ReadDBData.TYPE_YEAR_BP_AVE;
				} else {
					type = ReadDBData.TYPE_YEAR_HR_AVE;
				}

				// DB����̓ǂݍ���
				readDB = new ReadDBThread(type, 2345, 0,
						RawWaveRenderer.numOfDataReadDB, time);
				// �ǂݍ��݊������ɌĂ΂�郊�X�i�m�o�^
				readDB.addListener(this);
				readDB.addListener(canvas.yearBarGraph);
				readDB.start();

				// �҂���ʂ̕\��
				canvas.waitScreen.action();
				// �҂���ʏ�Ԃ�
				keyStatus = STATUS_WAIT;
				// �߂�R�}���h�̍폜
				canvas.removeCommand(backCmd);
			}
		
		}else if(type == ReadDBData.TYPE_WEEK_BP_AVE || 
				 type == ReadDBData.TYPE_WEEK_HR_AVE ){
			//---------------�܂���O���t(�T)----------------//			
			System.out.println("�܂���O���t(�T)");
			
			status    = STATUS_WEEK_BAR_GEAPH;
			keyStatus = STATUS_ROLL_MENU;;
				
			//���j���[��\��
			rollmenuFlag = true;
			canvas.rollMenu.action(RollMenuRenderer.ACTIVE);
			canvas.rollMenu.setFocus(true);				
			
			//�҂���ʂ̕\������߂�
			canvas.waitScreen.stop();		
		}else if(type == ReadDBData.TYPE_MONTH_BP_AVE || 
				 type == ReadDBData.TYPE_MONTH_HR_AVE ){
			//---------------�܂���O���t(��)----------------//
			System.out.println("�܂���O���t(��)");
			
			status    = STATUS_MONTH_BAR_GEAPH;
			keyStatus = STATUS_ROLL_MENU;;
				
			//���j���[��\��
			rollmenuFlag = true;
			canvas.rollMenu.action(RollMenuRenderer.ACTIVE);
			canvas.rollMenu.setFocus(true);
			
			//�҂���ʂ̕\������߂�
			canvas.waitScreen.stop();		
			
		}else if(type == ReadDBData.TYPE_YEAR_BP_AVE || 
				 type == ReadDBData.TYPE_YEAR_HR_AVE ){
			//---------------�܂���O���t(�N)----------------//
			System.out.println("�܂���O���t(�N)");
			
			status    = STATUS_YEAR_BAR_GEAPH;
			keyStatus = STATUS_ROLL_MENU;;
				
			//���j���[��\��
			rollmenuFlag = true;
			canvas.rollMenu.action(RollMenuRenderer.ACTIVE);
			canvas.rollMenu.setFocus(true);			
			
			//�҂���ʂ̕\������߂�
			canvas.waitScreen.stop();		
			
		}else if(type == ReadDBData.TYPE_ECG ||
				 type == ReadDBData.TYPE_PLS ){
			//---------------ECGPLS�g�`�\�����-------------//
			System.out.println("#�g�` ECGPLS");			
			
			status = STATUS_RAW_WAVE;
			keyStatus = STATUS_RAW_WAVE;
			
			//�҂���ʂ̕\������߂�
			canvas.waitScreen.stop();		
			
		}else if(type == ReadDBData.TYPE_BP       || type == ReadDBData.TYPE_HR  ||						 
				 type == ReadDBData.TYPE_YEAR_BP  || type == ReadDBData.TYPE_YEAR_HR ){
			
			//-----------HR,BP�g�`�\�����-----------------//
			System.out.println("#�g�` BPHR");			
			
			status = STATUS_BPHR_WAVE;
			keyStatus = STATUS_BPHR_WAVE;
			
			//�҂���ʂ̕\������߂�
			canvas.waitScreen.stop();		
			
		}//End of if()
		
	}/* End of onCompleteReadDB() */
	
	/**
	 * DB����̓ǂݍ��݂����s�����Ƃ��̌Ă΂��
	 */
	public void onErrorReadDB() {
		//��]���j���[��ʂւ��ǂ�
		System.out.println("#onErrorReadDB@manager");
		
		//�҂���ʂ̕\������߂�
		canvas.waitScreen.stop();
		//�L�[����]���j���[��
		keyStatus = STATUS_ROTATE_MENU;
		//�߂�R�}���h��߂�
		canvas.addCommand(backCmd);
				
		//�ǂݍ��ݗp�X���b�h��j��
		if(readDB != null){
			readDB.stop();
			readDB = null;					
		}		
		//�ǂݍ��݃t���O���I�t��
		readDBFlag = false;
		
	}/* End of onErrorReadDB */	
}
