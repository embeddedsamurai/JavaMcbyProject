/*
 * 作成日: 2007/11/04
 *
 * Copyright 1993 - 2007 Embedded.Samurai, Inc. All rights reserved.
 *
 * $Id: MailBoxControl.java,v 1.1 2007/11/22 06:51:08 esamurai Exp $
 */
package mailbox;

/**
 * MailBox クラス。
 * <br>
 *
 * @version $Revision: 1.1 $
 * @author shusaku sone <embedded.samurai@gmail.com>
 */
public class MailBoxControl{

	public static final int MSG_FIELD_ACK=0x8000;

	public static final int MSG_SD_PLAY=0x0001;
	public static final int MSG_SD_EXIT=0x0002;

	public static final int MSG_BPCALC_DATA=0x0003;
	public static final int MSG_MAIN_DATA=0x0004;
	public static final int MSG_MAIN_DRAW_ON=0x0005;
	public static final int MSG_CALC_STOP = 0x0006;
	
	public static final int MSG_HTTP_ECG_PLS_DATA=0x0007;
	public static final int MSG_HTTP_HR_BP_DATA=0x0008;
	
	public static final int MSG_BT_PLAY = 0x0009;
	public static final int MSG_BT_EXIT = 0x000a;
	
	public static final int MSG_FFT_ECG_DATA = 0x000b;

	public static final int TOTAL_NO_MSGS=0x000c;

	public static final int MSG_SD_PLAY_ACK     = (MSG_SD_PLAY  | 0x8000);
	public static final int MSG_BPCALC_DATA_ACK = (MSG_BPCALC_DATA | 0x8000);
	public static final int MSG_MAIN_DATA_ACK   = (MSG_MAIN_DATA | 0x8000);
	public static final int MSG_MAIN_DRAW_ON_ACK   = (MSG_MAIN_DRAW_ON | 0x8000);

	public static final int MSG_HTTP_ECG_PLS_ACK   = (MSG_HTTP_ECG_PLS_DATA | 0x8000);
	public static final int MSG_HTTP_HR_BP_ACK     = (MSG_HTTP_HR_BP_DATA | 0x8000);
	
	public static final int MSG_BT_PLAY_ACK     = (MSG_BT_PLAY  | 0x8000);
	
	public static final int MSG_FFT_ECG_ACK     = (MSG_FFT_ECG_DATA  | 0x8000);

	public MailBox MBX_main=null;
	public MailBox MBX_sd=null;
	public MailBox MBX_bpcalc=null;
	public MailBox MBX_http=null;
	public MailBox MBX_bt=null;
	public MailBox MBX_fft = null;

	//-------------------------------------//
	public MailBox MSG_sd_play  = null;
	public MailBox MSG_sd_exit  = null;
	public MailBox MSG_bpcalc_data = null;
	public MailBox MSG_main_data = null;
	public MailBox MSG_main_draw_on=null;
	public MailBox MSG_calc_stop=null;
	public MailBox MSG_http_ecg_pls_data=null;
	public MailBox MSG_http_hr_bp_data=null;
	public MailBox MSG_bt_play  = null;
	public MailBox MSG_bt_exit  = null;
	public MailBox MSG_fft_ecg_data = null;
	//-------------------------------------//

	private MailBox[] dispatch_table= new MailBox[TOTAL_NO_MSGS+1] ;

	public MailBoxControl(){
		MBX_main    = new MailBox();
		MBX_sd      = new MailBox();
		MBX_bpcalc  = new MailBox();		
		MBX_http    = new MailBox();
		MBX_bt      = new MailBox();
		MBX_fft     = new MailBox();

		MSG_sd_play     = MBX_sd;
		MSG_sd_exit     = MBX_sd;

		MSG_bpcalc_data = MBX_bpcalc;

		MSG_main_data    = MBX_main;
		MSG_main_draw_on = MBX_main;

		MSG_http_ecg_pls_data = MBX_http;
		MSG_http_hr_bp_data   = MBX_http;
		
		MSG_bt_play     = MBX_bt;
		MSG_bt_exit     = MBX_bt;
		
		MSG_fft_ecg_data = MBX_fft;

		dispatch_table[MSG_SD_PLAY]   = MSG_sd_play;
		dispatch_table[MSG_SD_EXIT]   = MSG_sd_exit;

		dispatch_table[MSG_MAIN_DATA] = MSG_main_data;
		dispatch_table[MSG_MAIN_DRAW_ON] = MSG_main_draw_on;

		dispatch_table[MSG_BPCALC_DATA] = MSG_bpcalc_data;
		dispatch_table[MSG_CALC_STOP] = MSG_calc_stop;

		dispatch_table[MSG_HTTP_ECG_PLS_DATA] = MSG_http_ecg_pls_data;
		dispatch_table[MSG_HTTP_HR_BP_DATA]   = MSG_http_hr_bp_data;
		
		dispatch_table[MSG_BT_PLAY] = MSG_bt_exit;
		dispatch_table[MSG_BT_EXIT] = MSG_bt_play;
		
		dispatch_table[MSG_FFT_ECG_DATA] = MSG_fft_ecg_data;
	}

	public void ReadMsgPost(int msg_id, Message msg_ptr,MailBox ret_MBX_ptr){
		//コマンドを入れる
		msg_ptr.msg_id = msg_id;
		//return MBX
		msg_ptr.ret_MBX_ptr = ret_MBX_ptr;
		dispatch_table[msg_id].Post(msg_ptr);
	}


	public void WriteMsgPost(int msg_id, Message msg_ptr,MailBox ret_MBX_ptr){
		msg_ptr.msg_id = msg_id;
		//return MBX
		msg_ptr.ret_MBX_ptr= ret_MBX_ptr;
		// 書き込みなら1
		msg_ptr.payload_valid = 1;
		dispatch_table[msg_id].Post(msg_ptr);
	}


	public void ControlMsgPost(int msg_id, Message msg_ptr,MailBox ret_MBX_ptr){

		msg_ptr.msg_id = msg_id;
		//return MBX
		msg_ptr.ret_MBX_ptr= ret_MBX_ptr;

		//メッセージなので何も持たない
		msg_ptr.payload_size1  = 0;
		msg_ptr.payload_size2  = 0;
		msg_ptr.payload_valid = 0;
		msg_ptr.payload_int_ptr1   = null;
		msg_ptr.payload_int_ptr2   = null;
		msg_ptr.payload_double_ptr1   = null;
		msg_ptr.payload_double_ptr2   = null;

		dispatch_table[msg_id].Post(msg_ptr);
	}

	public int AckMsgPost(Message msg_ptr)
	{
		msg_ptr.msg_id = msg_ptr.msg_id | 0x8000;
		msg_ptr.ret_MBX_ptr.Post(msg_ptr);
		return 1;
	}

	public Message MsgPend(MailBox MBX_ptr,long timeout)
	{
		Message msg=MBX_ptr.Pend(timeout);
		return msg;
	}

}

