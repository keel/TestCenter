/**
 * 
 */
package com.k99k.testcenter;

import com.k99k.khunter.Action;
import com.k99k.khunter.ActionMsg;
import com.k99k.khunter.HttpActionMsg;
import com.k99k.khunter.KObject;

/**
 * 公告
 * @author keel
 *
 */
public class News extends Action {

	/**
	 * @param name
	 */
	public News(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see com.k99k.khunter.Action#act(com.k99k.khunter.ActionMsg)
	 */
	@Override
	public ActionMsg act(ActionMsg msg) {
		HttpActionMsg httpmsg = (HttpActionMsg)msg;
		KObject u = Auth.checkCookieLogin(httpmsg);
		if (u != null) {
			msg.addData("u", u);
			msg.addData("[jsp]", "/WEB-INF/tc/news.jsp");
		}else{
			msg.addData("[redirect]", "/login");
		}
		return super.act(msg);
	}
	
	

}
