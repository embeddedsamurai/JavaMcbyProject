/*
 * 作成日: 2007/11/04
 *
 * Copyright 1993 - 2007 Embedded.Samurai, Inc. All rights reserved.
 *
 * $Id: RequestQueue.java,v 1.1 2007/11/22 06:51:08 esamurai Exp $
 */
package request;
import java.util.Vector;

/**
 * リクエストを順番に蓄えるクラス。
 * <br>
 *
 * @version $Revision: 1.1 $
 * @copyright 2007 Embedded.Samurai, Inc.
 * @original Hiroshi Yuki
 * @author embedded.samurai <embedded.samurai@gmail.com>
 */
public class RequestQueue {

	public final Vector vec = new Vector();

	public void putRequest(Request request) {
		vec.addElement(request);
	}

	public Request getRequest() {

		if (vec.size() <= 0) {
			//System.out.println("error");
			return null;
		}

		Request req = (Request)vec.elementAt(0);
		vec.removeElementAt(0);
		return req;
		
	}
	
	public void clear(){
		vec.removeAllElements();
	}
	

}
