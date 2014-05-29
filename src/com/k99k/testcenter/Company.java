/**
 * 
 */
package com.k99k.testcenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
 * @author keel
 *
 */
public class Company extends Action {

	/**
	 * @param name
	 */
	public Company(String name) {
		super(name);
	}
	
	static DaoInterface dao;
	static KObjSchema schema;
	
	/**
	 * {egameId(cpid):name}缓存
	 */
	public static HashMap<String,String> egameIds;
	
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
		}else if (StringUtil.isDigits(subact)) {
			
		}else if(subact.equals("a_a")){
			
		}else if(subact.equals("a_u")){
			
		}else if(subact.equals("a_d")){
			
		}else if(subact.equals("a_s")){
			
		}
		return super.act(msg);
	}
	
	/**
	 * 查询,按名称拼音头字母缩写,根据查询参数q
	 * @param req
	 * @param u
	 * @param msg
	 */
	private void find(HttpServletRequest req,KObject u,HttpActionMsg msg){
		String q = req.getParameter("q");
		//长度少于2直接返回空
		if (!StringUtil.isStringWithLen(q, 2)) {
			msg.addData("[print]", "");
			return ;
		}
		Pattern p = Pattern.compile(q.trim().toLowerCase());
		HashMap<String,Object> query = new HashMap<String, Object>(4);
		query.put("shortName", p);
		query.put("state", 0);
		String re = StaticDao.queryStr(dao, query, null, 0, 0, null);
		msg.addData("[print]", re);
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
		msg.addData("[jsp]", "/WEB-INF/tc/products.jsp");
	}
	/* (non-Javadoc)
	 * @see com.k99k.khunter.Action#init()
	 */
	@Override
	public void init() {
		dao = DaoManager.findDao("TCCompanyDao");
		schema = KObjManager.findSchema("TCCompany");
		
		//缓存egameId
		egameIds = new HashMap<String, String>(1000);
		HashMap<String,Object> f = new HashMap<String, Object>();
		f.put("name", 1);
		f.put("mainUser",1);
		ArrayList<HashMap<String,Object>> ls = dao.query(null, f, null, 0, 0, null);
		for (Iterator<HashMap<String, Object>> it = ls.iterator(); it.hasNext();) {
			HashMap<String, Object> m = it.next();
			Object mu = m.get("mainUser");
			if (mu != null && mu.toString().startsWith("C")) {
				egameIds.put(mu.toString(), m.get("name").toString());
			}
		}
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
