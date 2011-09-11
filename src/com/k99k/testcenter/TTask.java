/**
 * 
 */
package com.k99k.testcenter;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import com.k99k.khunter.Action;
import com.k99k.khunter.ActionMsg;
import com.k99k.khunter.HttpActionMsg;
import com.k99k.khunter.KFilter;
import com.k99k.khunter.KObject;
import com.k99k.khunter.dao.StaticDao;
import com.k99k.tools.StringUtil;

/**
 * 测试任务
 * @author keel
 *
 */
public class TTask extends Action {

	/**
	 * @param name
	 */
	public TTask(String name) {
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
			this.list(req, u, httpmsg);
		}else if(subact.equals("new")){
			
		}else if (StringUtil.isDigits(subact)) {
			
		}else if(subact.equals("add")){
			
		}else if(subact.equals("update")){
			
		}else if(subact.equals("del")){
			
		}else if(subact.equals("search")){
			
		}
		
		return super.act(msg);
	}
	
	private void one(String subact,HttpServletRequest req,KObject u,ActionMsg msg){
		
	}
	
	/**
	 * 查看列表
	 * @param req
	 * @param u
	 * @param msg
	 */
	private void list(HttpServletRequest req,KObject u,ActionMsg msg){
		String p_str = req.getParameter("p");
		String pz_str = req.getParameter("pz");
		int page = StringUtil.isDigits(p_str)?Integer.parseInt(p_str):1;
		int pz = StringUtil.isDigits(pz_str)?Integer.parseInt(pz_str):this.pageSize;
		ArrayList<KObject> list = StaticDao.loadNews(page, pz);
		msg.addData("u", u);
		msg.addData("list", list);
		msg.addData("pz", pz);
		msg.addData("p", page);
		msg.addData("[jsp]", "/WEB-INF/tc/news.jsp");
	}
	
	private int pageSize = 30;

	/**
	 * @return the pageSize
	 */
	public final int getPageSize() {
		return pageSize;
	}

	/**
	 * @param pageSize the pageSize to set
	 */
	public final void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	
	

}
