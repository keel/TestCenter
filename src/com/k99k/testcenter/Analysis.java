/**
 * 
 */
package com.k99k.testcenter;

import javax.servlet.http.HttpServletRequest;

import com.k99k.khunter.Action;
import com.k99k.khunter.ActionMsg;
import com.k99k.khunter.HttpActionMsg;
import com.k99k.khunter.JOut;
import com.k99k.khunter.KFilter;
import com.k99k.khunter.KObject;
import com.k99k.khunter.dao.StaticDao;
import com.k99k.tools.StringUtil;

/**
 * @author keel
 *
 */
public class Analysis extends Action {

	/**
	 * @param name
	 */
	public Analysis(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see com.k99k.khunter.Action#act(com.k99k.khunter.ActionMsg)
	 */
	@Override
	public ActionMsg act(ActionMsg msg) {
		HttpActionMsg httpmsg = (HttpActionMsg)msg;
		HttpServletRequest req = httpmsg.getHttpReq();
		String subact = KFilter.actPath(msg, 2, "");
		KObject u = Auth.checkCookieLogin(httpmsg);
		if (u == null) {
			msg.addData("[redirect]", "/login");
			return super.act(msg);
		}
		if (subact.equals("")) {
			//this.list(req, u, httpmsg);
		}else if(subact.equals("week")){
			
		}else if(subact.equals("day")){
			//this.find(req, u, httpmsg);
		}else if(subact.equals("period")){
			this.period(req, u, httpmsg);
		}
//			else if (StringUtil.isDigits(subact)) {
//			
//		}else if(subact.equals("a_a")){
//			
//		}else if(subact.equals("a_u")){
//			
//		}else if(subact.equals("a_d")){
//			
//		}else if(subact.equals("a_s")){
//			
//		}
		return super.act(msg);
	}
	
	private void period(HttpServletRequest req,KObject u,HttpActionMsg msg){
		int userType = u.getType();
		if (userType < 4) {
			//权限不够
			JOut.err(401,"E401"+Err.ERR_AUTH_FAIL, msg);
			return;
		}
		String s = req.getParameter("start");
		String e = req.getParameter("end");
		if (!StringUtil.isDigits(s) || !StringUtil.isDigits(e)) {
			msg.addData("[print]", "");
			return ;
		}
		long start = Long.parseLong(s);
		long end  = Long.parseLong(e);
		//开始时间点必须在2012年之后
		if (end - start < 0 || start < 1325350861000L) {
			msg.addData("[print]", "");
			return ;
		}
		
		String re = StaticDao.analysisPeriod(start,end);
		msg.addData("[print]",re);
		
	}
	

}
