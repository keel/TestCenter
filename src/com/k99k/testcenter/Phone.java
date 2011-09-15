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
import com.k99k.khunter.DaoInterface;
import com.k99k.khunter.DaoManager;
import com.k99k.khunter.HttpActionMsg;
import com.k99k.khunter.KFilter;
import com.k99k.khunter.KObjManager;
import com.k99k.khunter.KObjSchema;
import com.k99k.khunter.KObject;
import com.k99k.khunter.dao.StaticDao;
import com.k99k.tools.StringUtil;

/**
 * 终端
 * @author keel
 *
 */
public class Phone extends Action {

	/**
	 * @param name
	 */
	public Phone(String name) {
		super(name);
	}
	static DaoInterface dao;
	static KObjSchema schema;
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
			
		}else if(subact.equals("find")){
			this.find(req, u, httpmsg);
		}else if(subact.equals("json")){
			this.json(req, u, httpmsg);
		}else if (StringUtil.isDigits(subact)) {
			
		}else if(subact.equals("a_a")){
			
		}else if(subact.equals("a_u")){
			
		}else if(subact.equals("a_d")){
			
		}else if(subact.equals("a_s")){
			
		}
		return super.act(msg);
	}

	/**
	 * 用于机型的快速查找,需要有机型系统参数,
	 * 例:http://localhost/TestCenter/phone/find?s=1&q=00
	 * @param req
	 * @param u
	 * @param msg
	 */
	private void find(HttpServletRequest req,KObject u,HttpActionMsg msg){
		//s为手机的操作系统
		String s = req.getParameter("s");
		String q = req.getParameter("q");
		//长度少于2直接返回空,机型系统必为数字
		if (!StringUtil.isStringWithLen(q, 2) || !StringUtil.isDigits(s)) {
			msg.addData("[print]", "");
			return ;
		}
		Pattern p = Pattern.compile(q.trim());
		HashMap<String,Object> query = new HashMap<String, Object>(6);
		query.put("shortName", p);
		query.put("type", Integer.parseInt(s));
		String re = StaticDao.queryStr(dao, query,  null, 0, 0, null);
		msg.addData("[print]",re);
		return;
	}
	
	/**
	 * 用于机型组的备选,必须要有系统参数
	 * 例:http://localhost/TestCenter/phone/json?s=1
	 * @param req
	 * @param u
	 * @param msg
	 */
	private void json(HttpServletRequest req,KObject u,HttpActionMsg msg){
		//s为手机的操作系统
		String s = req.getParameter("s");
		//长度少于2直接返回空,机型系统必为数字
		if (!StringUtil.isDigits(s)) {
			msg.addData("[print]", "");
			return ;
		}
		HashMap<String,Object> query = new HashMap<String, Object>(2);
		query.put("type", Integer.parseInt(s));
		String re = StaticDao.queryGroupJson(dao, query, 0, 0, null);
		msg.addData("[print]",re);
		return;
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
		ArrayList<KObject> list = dao.queryByPage(page,pageSize,StaticDao.prop_state_normal, null, StaticDao.prop_id_desc, null);
		msg.addData("u", u);
		msg.addData("list", list);
		msg.addData("pz", pz);
		msg.addData("p", page);
		msg.addData("[jsp]", "/WEB-INF/tc/phones.jsp");
	}
	
	
	/* (non-Javadoc)
	 * @see com.k99k.khunter.Action#init()
	 */
	@Override
	public void init() {
		dao = DaoManager.findDao("TCPhoneDao");
		schema = KObjManager.findSchema("TCPhone");
		super.init();
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
