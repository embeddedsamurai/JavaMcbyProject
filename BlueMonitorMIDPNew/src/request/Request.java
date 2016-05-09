/*
 * 作成日: 2007/11/04
 *
 * Copyright 1993 - 2007 Embedded.Samurai, Inc. All rights reserved.
 *
 * $Id: Request.java,v 1.1 2007/11/22 06:51:08 esamurai Exp $
 */

package request;

/**
 * リクエストを表すクラス。
 * <br>
 *
 * @version $Revision: 1.1 $
 * @copyright 2007 Embedded.Samurai, Inc.
 * @original Hiroshi Yuki
 * @author embedded.samurai <embedded.samurai@gmail.com>
 */
public final class Request {

	public static final int SDCARD_STOP=0;
	public static final int SDCARD_START=1;

	private final int command;
	public Request(int command) {
		this.command = command;
	}
	public int getCommand() {
		return command;
	}
	public String toString() {
		return "[ Request " + command + " ]";
	}
}
