/*
 * 作成日: 2007/11/04
 *
 * Copyright 1993 - 2007 Embedded.Samurai, Inc. All rights reserved.
 *
 * $Id: State.java,v 1.1 2007/11/22 06:51:08 esamurai Exp $
 */

package main;

/**
 * 状態を表すクラス。
 * <br>
 *
 * @version $Revision: 1.1 $
 * @copyright 2007 Embedded.Samurai, Inc.
 * @author embedded.samurai <embedded.samurai@gmail.com>
 */
public class State {

	int state_id;
	int action;
	int actionPending;

	public State() {
		state_id=0;
		action=0;
		actionPending=0;
	}
}
