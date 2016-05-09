/*
 * 作成日: 2007/11/04
 *
 * Copyright 1993 - 2007 Embedded.Samurai, Inc. All rights reserved.
 *
 * $Id: Message.java,v 1.1 2007/11/22 06:51:08 esamurai Exp $
 */

package mailbox;

/**
 * メッセージを表すクラス。
 * <br>
 *
 * @version $Revision: 1.1 $
 * @copyright 2007 Embedded.Samurai, Inc.
 * @author embedded.samurai <embedded.samurai@gmail.com>
 */
public final class Message {

	public static final int NON_DATA=0x0000; //dataなし
	public static final int ECG_DATA=0x0001; //ECGデータ
	public static final int PLS_DATA=0x0002; //PLSデータ
	public static final int HP_DATA=0x0003;  //HR(心拍データ)
	public static final int PAT_DATA=0x0004; //PAT(脈波伝搬時間)
	public static final int BP_DATA=0x0005;  //血圧データ

	public int                       msg_id;
	public int                    msg_label;
	public int                payload_valid;
	public double             payload_ddata;
	public int                payload_idata;

	public int[]            payload_int_ptr1;
	public int[]            payload_int_ptr2;

	public double[]     payload_double_ptr1;
	public double[]     payload_double_ptr2;

	public int                payload_size1;
	public int                payload_size2;

	public int msg_count1;
	public int msg_count2;


	MailBox             ret_MBX_ptr;
	public int status;

	public Message() {
		msg_id=0;
		msg_label=0;
		payload_size1=0;
		payload_size2=0;
		payload_valid=0;

		payload_ddata=0;
		payload_idata=0;

		payload_int_ptr1=null;
		payload_int_ptr2=null;

		payload_double_ptr1=null;
		payload_double_ptr2=null;

		msg_count1=0;
		msg_count2=0;

		ret_MBX_ptr=null;
		status=0;
	}

	public void clear(){
		msg_id=0;
		msg_label=0;
		payload_size1=0;
		payload_size2=0;
		payload_valid=0;

		payload_ddata=0;
		payload_idata=0;

		payload_int_ptr1=null;
		payload_int_ptr2=null;

		payload_double_ptr1=null;
		payload_double_ptr2=null;

		msg_count1=0;
		msg_count2=0;

		ret_MBX_ptr=null;
		status=0;
	}

	public void count_clear(){
		msg_count1=0;
		msg_count2=0;
	}

	public int getCommand() {
		//return command;
		return 0;
	}
	public String toString() {
		//return "[ Request " + command + " ]";
		return null;
	}
}
