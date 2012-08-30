/**
 * 
 */
package com.k99k.testcenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import com.k99k.khunter.Action;
import com.k99k.khunter.ActionMsg;
import com.k99k.khunter.HttpActionMsg;
import com.k99k.khunter.JOut;
import com.k99k.khunter.KFilter;
import com.k99k.khunter.KObject;
import com.k99k.khunter.dao.StaticDao;
import com.k99k.tools.JSON;
import com.k99k.tools.StringUtil;

/**
 * @author keel
 *
 */
public class Admin extends Action {

	/**
	 * @param name
	 */
	public Admin(String name) {
		super(name);
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

	@Override
	public ActionMsg act(ActionMsg msg) {
		HttpActionMsg httpmsg = (HttpActionMsg)msg;
		HttpServletRequest req = httpmsg.getHttpReq();
		
		String[] pathArr = (String[]) msg.getData("[pathArr]");
		int r = KFilter.getRootNum()+2;
		String subact = (pathArr.length <= r) ? "" : pathArr[r];
		String subsub = (pathArr.length <= r+1) ? "" : pathArr[r+1];
		KObject u = Auth.checkCookieLogin(httpmsg);
		if (u == null) {
			msg.addData("[redirect]", "/login");
			return super.act(msg);
		}
		if (u.getType()<5) {
			JOut.err(401, httpmsg);
			return super.act(msg);
		}
		msg.addData("u", u);
		if (subact.equals("user")) {
			//用户管理
			this.user(subact, subsub, req, u, httpmsg);
			
		}else{
			JOut.err(404, httpmsg);
		}
		
		
		return super.act(msg);
	}
	
	
	
	private void user(String subact,String subsub,HttpServletRequest req,KObject u,HttpActionMsg msg){
		if(subsub.equals("")){
			msg.addData("[jsp]", "/WEB-INF/tc/user_search.jsp");
		}
		//修改
		else if(subsub.equals("update")){
			if (StringUtil.isDigits(req.getParameter("uid"))) {
				long uid = Long.parseLong(req.getParameter("uid"));
				KObject one = TUser.dao.findOne(uid);
				if (one== null || one.getState() == -1) {
					JOut.err(404, msg);
					return;
				}
				//产品
				msg.addData("one", one);
				msg.addData("[jsp]", "/WEB-INF/tc/user_edit.jsp");
			}
		}
		//新增
		else if(subsub.equals("add")){
			msg.addData("[jsp]", "/WEB-INF/tc/user_add.jsp");
			
		}
		//搜索
		else if(subsub.equals("search")){
			this.searchUser(subact, subsub, req, u, msg);
		}
		else{
			msg.addData("[jsp]", "/WEB-INF/tc/user_search.jsp");
		}
		
		
	}
	
	private void searchUser(String subact,String subsub,HttpServletRequest req,KObject u,HttpActionMsg msg){
		HashMap<String,Object> query = new HashMap<String, Object>();
		if (StringUtil.isStringWithLen(req.getParameter("uName"), 1)) {
			Pattern p = Pattern.compile(req.getParameter("uName").trim());
			query.put("name", p);
		}
		if (StringUtil.isStringWithLen(req.getParameter("uCom"), 1)) {
			Pattern p = Pattern.compile(req.getParameter("uCom").trim());
			query.put("company", p);
		}
		if (StringUtil.isStringWithLen(req.getParameter("uHand"), 1)) {
			Pattern p = Pattern.compile(req.getParameter("uHand").trim());
			query.put("phoneNumber", p);
		}
		if (StringUtil.isStringWithLen(req.getParameter("uMail"), 1)) {
			Pattern p = Pattern.compile(req.getParameter("uMail").trim());
			query.put("email", p);
		}
		query.putAll(StaticDao.prop_state_normal);
		this.queryPage(query,subact, req, u, msg);
	}
	
	/**
	 * 按条件查看列表
	 * @param query
	 * @param subact
	 * @param req
	 * @param u
	 * @param msg
	 */
	private void queryPage(HashMap<String,Object> query,String subact,HttpServletRequest req,KObject u,HttpActionMsg msg){
		String p_str = req.getParameter("p");
		String pz_str = req.getParameter("pz");
		int page = StringUtil.isDigits(p_str)?Integer.parseInt(p_str):1;
		int pz = StringUtil.isDigits(pz_str)?Integer.parseInt(pz_str):this.pageSize;
		ArrayList<KObject> list = TUser.dao.queryByPage(page,pageSize,query, null, StaticDao.prop_id_desc, null);
		msg.addData("[print]", JSON.write(list));
	}
	
	
}
