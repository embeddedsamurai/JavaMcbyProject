/*
 * çÏê¨ì˙: 2007/11/04
 *
 * Copyright 1993 - 2007 Embedded.Samurai, Inc. All rights reserved.
 *
 * $Id: MailBox.java,v 1.1 2007/11/22 06:51:08 esamurai Exp $
 */
package mailbox;
import java.util.Vector;

/**
 * MailBox ÉNÉâÉXÅB
 * <br>
 *
 * @version $Revision: 1.1 $
 * @copyright 2007 Embedded.Samurai, Inc.
 * @author embedded.samurai <embedded.samurai@gmail.com>
 */
public class MailBox {

	public final Vector vec = new Vector();

	public synchronized Message Pend(long timeout) {

		Message msg=null;

		while (vec.size() <= 0) {

			if(timeout > 0){
				try{
					Thread.sleep(timeout);
				}catch(Exception e){
					System.out.println("sleep error");
				}
				break;
			}

			try {
				wait();
			}catch(InterruptedException e) {
				System.out.println("error");
			}

		}

		if(vec.size() > 0){
			msg = (Message)vec.elementAt(0);
			vec.removeElementAt(0);
		}

		notifyAll();
		return msg;
	}

	public synchronized void Post(Message msg){
		vec.addElement(msg);
		notifyAll();
	}

}
