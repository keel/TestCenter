/**
 * 
 */
package com.k99k.testcenter;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import com.k99k.khunter.Action;
import com.k99k.khunter.ActionMsg;
import com.k99k.khunter.DaoInterface;
import com.k99k.khunter.DaoManager;
import com.k99k.khunter.HttpActionMsg;
import com.k99k.khunter.JOut;
import com.k99k.khunter.KFilter;
import com.k99k.khunter.KObjManager;
import com.k99k.khunter.KObjSchema;
import com.k99k.khunter.KObject;
import com.k99k.khunter.MongoDao;
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
			this.list(subact,req, u, httpmsg);
		}if (subact.equals("my")) {
			String subact2 = KFilter.actPath(msg, 3, "");
			if (subact2.equals("")) {
				this.list(subact,req, u, httpmsg);
			}else if(subact2.equals("a_s")){
				this.search(subact,req, u, httpmsg);
			}
		}else if(subact.equals("add")){
			msg.addData("sub", subact);
			this.toAdd(u, httpmsg);
		}else if (StringUtil.isDigits(subact)) {
			this.one(subact, req, u, httpmsg);
		}else if(subact.equals("a_a")){
			
		}else if(subact.equals("a_u")){
			
		}else if(subact.equals("a_d")){
			
		}else if(subact.equals("a_s")){
			this.search(subact,req, u, httpmsg);
		}else{
			JOut.err(404, httpmsg);
		}
		return super.act(msg);
	}
	
	private void search(String subact,HttpServletRequest req,KObject u,HttpActionMsg msg){
		if (StringUtil.isStringWithLen(req.getParameter("k"), 1)) {
			String key = null;
			try {
				//TODO 针对tomcatURL编码转换
				key = new String(req.getParameter("k").getBytes("ISO-8859-1"),"utf-8").trim();
			} catch (UnsupportedEncodingException e) {
			}
			HashMap<String,Object> query = new HashMap<String, Object>(2);
			Pattern p = Pattern.compile(key);
			query.put("name", p);
			query.putAll(StaticDao.prop_state_normal);
			this.queryPage(query,subact, req, u, msg);
			return;
		}else{
			JOut.err(401, msg);
		}
	}
	
	/**
	 * 转到增加页
	 * @param u
	 * @param msg
	 */
	private void toAdd(KObject u,HttpActionMsg msg){
		if (Integer.parseInt(u.getType()) < 1) {
			//权限不够
			JOut.err(403, msg);
			return;
		}
		msg.addData("u", u);
		msg.addData("[jsp]", "/WEB-INF/tc/task_add.jsp");
	}
	
	/**
	 * 查看单个任务
	 * @param subact
	 * @param req
	 * @param u
	 * @param msg HttpActionMsg
	 */
	private void one(String subact,HttpServletRequest req,KObject u,HttpActionMsg msg){
		long id = Long.parseLong(subact);
		KObject one = dao.findOne(id);
		if (one== null || one.getState() == -1) {
			JOut.err(404, msg);
			return;
		}
		long pid = (Long)one.getProp("PID");
		KObject product = Product.dao.findOne(pid);
		//查找本Task所属的TestUnit
		HashMap<String,Object> q = new HashMap<String, Object>(2);
		q.put("TID", one.getId());
		ArrayList<KObject> tus = TestUnit.dao.queryKObj(q, null, MongoDao.prop_id_desc, 0, 0, null);
		msg.addData("u", u);
		msg.addData("one", one);
		msg.addData("product", product);
		msg.addData("tus", tus);
		//转到编辑时判断权限:是否为任务创建者或type>=4
		if (req.getParameter("edit")!=null && (u.getName().equals(one.getCreatorName()) || Integer.parseInt(u.getType())>=4)) {
			msg.addData("[jsp]", "/WEB-INF/tc/task_edit.jsp");
		}else{
			msg.addData("[jsp]", "/WEB-INF/tc/task_one.jsp");
		}
	}
	
	
	/**
	 * 查看列表
	 * @param subact
	 * @param req
	 * @param u
	 * @param msg
	 */
	private void list(String subact,HttpServletRequest req,KObject u,HttpActionMsg msg){
		this.queryPage(StaticDao.prop_state_normal,subact, req, u, msg);
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
		ArrayList<KObject> list = dao.queryByPage(page,pageSize,query, null, StaticDao.prop_level_id_desc, null);
		msg.addData("u", u);
		msg.addData("list", list);
		msg.addData("pz", pz);
		msg.addData("p", page);
		msg.addData("sub", subact);
		msg.addData("[jsp]", "/WEB-INF/tc/tasks.jsp");
	}
	
	/* (non-Javadoc)
	 * @see com.k99k.khunter.Action#init()
	 */
	@Override
	public void init() {
		dao = DaoManager.findDao("TCTaskDao");
		schema = KObjManager.findSchema("TCTask");
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
