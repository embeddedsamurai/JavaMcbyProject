package util;


public interface Protocol{
	
	String DAT_CUFF_DCIN  = "80";
	String DAT_CUFF_ACIN = "90";
	
	int CMD_START = 0x01;
	int CMD_READ_CUFF_IO = 0x07;
	int CMD_SET_LED = 0x07;
	int CMD_STOP = 0x02;
	int CMD_READ_VER = 0x05;
	int CMD_PATTERN = 0x03;
	int HEAD_MASK = 0xf0;
	
	String DAT_SPO2IR_DCIN = "80";
	String DAT_SPO2IR_ACIN = "90";
	String DAT_SPO2R_DCIN = "a0";
	String DAT_SPO2R_ACIN = "b0";
	String DAT_ECG = "c0";	
}
