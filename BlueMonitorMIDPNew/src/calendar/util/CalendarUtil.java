package calendar.util;

import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;

public class CalendarUtil {
	/** レコードストアのロケーション*/
	public static final String RS ="rs";
	
	/**
	 * UNIX時間を年月日に変換する
	 * @param time UNIX時間
	 * @return　年月日を保持する配列
	 */
	public static int[] calcTimeInfo(long time) {
		// JSTに補正
		time += 9 * 60 * 60 * 1000L;
		// 年を求める
		int year = (int) ((time / (365 * 24 * 60 * 60 * 1000L)) + 1970);
		// 月を求める
		long utMonth = time;
		
		for (int i = 1970; i < year; i++) {
			int daysOfYear = 365;
			if (uruCheck(i))
				daysOfYear += 1;
			utMonth = utMonth - (daysOfYear * 24 * 60 * 60 * 1000L);
		}
		utMonth = utMonth / (24 * 60 * 60 * 1000L);
		int month = 0;
		for (int i = 0; i < 12; i++) {
			utMonth = utMonth - getLastDay(year, (i + 1));
			if (utMonth <= 0) {
				month = i + 1;
				break;
			}
		}

		int day = 1;
		// 日を求める
		if(utMonth + getLastDay(year, month)<= 0){
			//年末のときに日がマイナスになるのでそれを修正
			year = year - 1; 
			day = (getLastDay(year, month)) -(int) Math.abs(utMonth + getLastDay(year, month));
			month = month - 1;
			if(month == 0)month = 12;
		}else{
			day = (int) (utMonth + getLastDay(year, month) + 1);
		}
		
		// info = {年, 月,日}
		int[] info = { year, month, day };

		return info;
	}

	/**
	 * スクラッチパッドから計測日を取得する
	 * @param year
	 * @param month
	 * @return
	 */
	public static boolean[] getMesurementDays(int year, int month) {		

		int lastday = getLastDay(year, month);
		boolean[] mesurementDays = new boolean[lastday];

		try {
			String header = "";
			if (month < 10)
				header = year + "0" + month + ":";
			else
				header = year + month + ":";

			// 既にデータがスクラッチパッドに存在するかチェックする
			int exist = -1;
			RecordStore rs = RecordStore.openRecordStore(RS,true);
			for (int i = 0; i < rs.getNumRecords(); i++) {
				byte[] tmpdata = rs.getRecord(i + 1);
				String tmpStr = new String(tmpdata);
				System.out.println(tmpStr);
				int cindex = tmpStr.indexOf(':');
				String compData = tmpStr.substring(0, cindex + 1);
				if (compData.equals(header)) {
					exist = i + 1;
					break;
				}
			}

			if (exist < 0) {
				for (int i = 0; i < mesurementDays.length; i++) {
					mesurementDays[i] = false;
				}
			} else {
				byte[] rdata = rs.getRecord(exist);
				String rstr = new String(rdata);
				int cindex = rstr.indexOf(':');
				String daystr = rstr.substring(cindex + 1, rstr.length()
						- cindex);
				for (int i = 0; i < daystr.length(); i++) {
					char c = daystr.charAt(i);
					if (c == '1')
						mesurementDays[i] = true;
					else if (c == '0')
						mesurementDays[i] = false;
				}
			}

			rs.closeRecordStore();
		} catch (RecordStoreException e) {
			System.out.println("Get Mesuremeent Days Error: " + e.toString());
			e.printStackTrace();
		}
		return mesurementDays;
	}
	

	/**
	 * 計測日をスクラッチパッドから削除する
	 * @param year  年
	 * @param month　月 
	 * @param day   日
	 */
	public static void removeMesurementDay(int year, int month, int day) {
		try {
			String header = "";
			if (month < 10)
				header = year + "0" + month + ":";
			else
				header = year + month + ":";

			// 既にデータがスクラッチパッドに存在するかチェックする
			int exist = -1;
			RecordStore rs = RecordStore.openRecordStore(RS, false);
			for (int i = 0; i < rs.getNumRecords(); i++) {
				byte[] tmpdata = rs.getRecord(i + 1);
				String tmpStr = new String(tmpdata);
				int cindex = tmpStr.indexOf(':');
				String compData = tmpStr.substring(0, cindex + 1);
				if (compData.equals(header)) {
					exist = i + 1;
					break;
				}
			}

			if (exist > 0) {
				byte[] rdata = rs.getRecord(exist);
				String rstr = new String(rdata);
				char[] rchars = rstr.toCharArray();
				rchars[6 + day] = '0';
				String wstr = new String(rchars);
				byte[] wdata = wstr.getBytes();
				rs.setRecord(exist, wdata, 0, wdata.length);
			}

			rs.closeRecordStore();
		} catch (RecordStoreException e) {
			System.out.println("Remove Mesuremeent Day Error: " + e.toString());
			e.printStackTrace();
		}
	}
	

	/**
	 * 計測日をスクラッチパッドに書き込み
	 * @param year  年
	 * @param month 月
	 * @param day   日
	 */
	public static void addMesurementDay(int year, int month, int day) {
		try {
			String header = "";
			if (month < 10)
				header = year + "0" + month + ":";
			else
				header = year + month + ":";

			// 既にデータがスクラッチパッドに存在するかチェックする
			int exist = -1;
			RecordStore rs = RecordStore.openRecordStore(RS, true);
			for (int i = 0; i < rs.getNumRecords(); i++) {
				System.out.println(i + 1);
				byte[] tmpdata = rs.getRecord(i + 1);
				String tmpStr = new String(tmpdata);
				System.out.println(tmpStr);
				int cindex = tmpStr.indexOf(':');
				String compData = tmpStr.substring(0, cindex + 1);
				if (compData.equals(header)) {
					exist = i + 1;
					break;
				}
			}

			if (exist < 0) {
				String sdata = header;
				for (int i = 0; i < getLastDay(year, month); i++) {
					if ((i + 1) == day)
						sdata += "1";
					else
						sdata += "0";
				}
				byte[] data = sdata.getBytes();
				rs.addRecord(data, 0, data.length);
			} else {
				byte[] rdata = rs.getRecord(exist);
				String rstr = new String(rdata);
				char[] rchars = rstr.toCharArray();
				rchars[6 + day] = '1';
				String wstr = new String(rchars);
				byte[] wdata = wstr.getBytes();
				rs.setRecord(exist, wdata, 0, wdata.length);
			}

			rs.closeRecordStore();
		} catch (RecordStoreException e) {
			System.out.println("Add Mesuremeent Day Error: " + e.toString());
			e.printStackTrace();
		}
	}

	/**
	 * 日付から曜日のインデックスを取得する
	 * @param year 年
	 * @param month　月 
	 * @param day   日
	 * @return
	 */
	public static int getWeek(int year, int month, int day) {

		if (month == 1 || month == 2) {
			year -= 1;
			month += 12;
		}
		int uy = year / 100;
		int dy = year % 100;

		int week = ((21 * uy) / 4 + (5 * dy) / 4 + (26 * (month + 1)) / 10
				+ day - 1) % 7;

		return week;
	}

	/**
	 * 月の最大日数を取得する
	 * @param year 年 
	 * @param month 月
	 * @return 月の最大日数
	 */
	public static int getLastDay(int year, int month) {		

		// 月ごとの日数
		int[] daysOfMonth = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

		int lastday = daysOfMonth[month - 1];
		if (month == 2) {
			if (uruCheck(year))
				lastday += 1;
		}

		return lastday;
	}

	/**
	 * うるう年かどうか調べる
	 * @param year　年
	 * @return true:うるう年 falseうるう年でない
	 * 
	 */
	public static boolean uruCheck(int year) {

		if ((year % 4) == 0 && !((year % 100) == 0 && (year % 400) != 0)) {
			return true;
		}
		return false;
	}
	
	/**
	 * 現在の時間を得る
	 * @param year  年
	 * @param month　月
	 * @param day   日
	 * @return 現在の時間
	 */
	public static long getCurrentTime(int year,int month,int day) {

		// 月ごとの日数
		int[] daysOfMonth = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
		// 時間
		long time = 0;

		for (int i = 1970; i < year; i++) {
			int days;
			if (CalendarUtil.uruCheck(i)){
				days = 366;
			}else{
				days = 365;				
			}
			time += days * 24 * 60 * 60 * 1000L;
		}
		if (CalendarUtil.uruCheck(year)){
			daysOfMonth[1]++;
		}			
		for (int i = 0; i < month - 1; i++) {
			time += (long) daysOfMonth[i] * 24 * 60 * 60 * 1000L;
		}
		time += (long) (day * 24 * 60 * 60 * 1000L);

		return time;
	}
	
	/**
	 * 現在の週数を求める
	 *
	 * @param year 対象の年
	 * @param month 対象の月
	 * @param day 対象の日
	 * @return 現在の週数
	 */
	public static int getCurrentWeekNum(int year,int month, int day){
		
		// 1日の曜日を求める
		int onedayWeek = getWeek(year, month, 1);
		
		// 週数を求める
		int weekNum = 0;
		int adjustDay = day - (7 - onedayWeek);
		if(adjustDay <= 0){
			weekNum = 1;
		} else{
			if(adjustDay%7 == 0){
				weekNum = adjustDay / 7 + 1;
			} else{
				weekNum = adjustDay / 7 + 2;
			}
		}

		return weekNum;
	}

	/**
	 * 月の週数を求める
	 *
	 * @param year 対象の年
	 * @param month 対象の月
	 * @return 対象の月の週数	 
	 */
	public static int getMonthWeekNum(int year, int month){
		
		// 月の最大日数を取得する。
		int lastDay = getLastDay(year,month);
		
		// 1日の曜日を求める
		int onedayWeek = getWeek(year, month, 1);
		
		// 週数を求める
		int weekNum = 0;
		int adjustDay = lastDay - (7 - onedayWeek);
		if(adjustDay <= 0){
			weekNum = 1;
		} else{
			if(adjustDay%7 == 0){
				weekNum = adjustDay / 7 + 1;
			} else{
				weekNum = adjustDay / 7 + 2;
			}
		}

		return weekNum;
	}
	
	/**
	 * 対象の週の日を求める
	 *
	 * @param year 対象の年
	 * @param month 対象の月
	 * @param week 対象の週
	 * @return 日のリスト
	 *
	 */
	public static int[] getWeekDays(int year, int month, int week){
		
		// 月の最大日数を取得する。
		int lastDay = getLastDay(year,month);
		
		// 1日の曜日を求める
		int onedayWeek = getWeek(year, month, 1);
		
		// 日のリストを求める
		int[] days = null;
		
		if(week == 1){
			days = new int[7-onedayWeek];
			for(int i=0; i < (7-onedayWeek); i++){
				days[i] = i + 1;
			}
		} else{
			int start = (7-onedayWeek) + 1 + (week-2)*7;
			int dayNum = 0;
		
			if((start+6) <= lastDay){
				dayNum = 7;
			} else{
				dayNum = lastDay - start + 1;
			}
			days = new int[dayNum];
			for(int i=0; i < dayNum; i++){
				days[i] = start + i;
			}
			
		}
		
		return days;
	}
}
