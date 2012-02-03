/**
 * 
 */
package com.k99k.testcenter;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

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
import com.k99k.tools.JSON;
import com.k99k.tools.StringUtil;

/**
 * TUser
 * @author keel
 *
 */
public class TUser extends Action  {
	/**
	 * @param name
	 */
	public TUser(String name) {
		super(name);
	}
	static final Logger log = Logger.getLogger(TUser.class);
	
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
		if (subact.equals("tester")) {
			this.tester(req, u, httpmsg);
		}else if (subact.equals("one")) {
			this.one(subact, req, u, httpmsg);
		}else if (subact.equals("edit")) {
			this.edit(subact, req, u, httpmsg);
		}else if(subact.equals("a_u")){
			this.update(req, u, httpmsg);
		}else{
			JOut.err(404, httpmsg);
		}
		
		return super.act(msg);
	}
	
	/**
	 * 更新用户信息
	 * @param req
	 * @param u
	 * @param msg
	 */
	private void update(HttpServletRequest req,KObject u,HttpActionMsg msg){
		if (StringUtil.isDigits(req.getParameter("user_id"))) {
			long uid = Long.parseLong(req.getParameter("user_id"));
			KObject user = dao.findOne(uid);
			if (user == null || !(user.getId()==u.getId() || u.getType()>10)) {
				JOut.err(401, msg);
				return;
			}
			HashMap<String,Object> q = new HashMap<String, Object>();
			q.put("_id", uid);
			HashMap<String,Object> update = new HashMap<String, Object>();
			HashMap<String,Object> set = new HashMap<String, Object>();
			
			if (StringUtil.isStringWithLen(req.getParameter("user_pwd"), 6) && req.getParameter("user_pwd").equals(req.getParameter("user_pwd2"))) {
				//修改密码
				set.put("pwd", req.getParameter("user_pwd"));
			}
			if (StringUtil.isDigits(req.getParameter("user_phone"))) {
				set.put("phoneNumber", req.getParameter("user_phone").trim());
			}
			if (StringUtil.isStringWithLen(req.getParameter("user_email"), 4)) {
				set.put("email", req.getParameter("user_email").trim());
			}
			if (StringUtil.isDigits(req.getParameter("user_qq"))) {
				set.put("qq", req.getParameter("user_qq").trim());
			}
			if (!set.isEmpty()) {
				update.put("$set", set);
				if(!dao.updateOne(q, update)){
					JOut.err(500, msg);
					return;
				}
			}
			msg.addData("[print]", "ok");
			return;
		}
		
		JOut.err(404, msg);
		return;
	}
	
	/**
	 * 进入用户自己的编辑页
	 * @param subact
	 * @param req
	 * @param u
	 * @param msg
	 */
	private void edit(String subact,HttpServletRequest req,KObject u,HttpActionMsg msg){
		msg.addData("u", u);
		msg.addData("one", u);
		msg.addData("[jsp]", "/WEB-INF/tc/user_edit.jsp");
	}
	
	/**
	 * 查看或编辑单个用户信息
	 * @param subact
	 * @param req
	 * @param u
	 * @param msg HttpActionMsg
	 */
	private void one(String subact,HttpServletRequest req,KObject u,HttpActionMsg msg){
		if (StringUtil.isStringWithLen(req.getParameter("u"), 1)) {
			String name = null;
			try {
				//TODO 针对tomcatURL编码转换
				name = new String(req.getParameter("u").getBytes("ISO-8859-1"),"utf-8").trim();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			//Task
			KObject one = dao.findOne(name);
			if (one== null || one.getState() == -1) {
				JOut.err(404, msg);
				return;
			}
			//产品
			msg.addData("u", u);
			msg.addData("one", one);
			
			//转到编辑时判断权限:是否为任务创建者或type>=4
			if (req.getParameter("edit")!=null && (u.getId()==one.getId() || u.getType()>10)) {
				msg.addData("[jsp]", "/WEB-INF/tc/user_edit.jsp");
			}else{
				msg.addData("[jsp]", "/WEB-INF/tc/user_one.jsp");
			}
		}else{
			JOut.err(401, msg);
		}
	}
	
	static final HashMap<String,Object> testerType = new HashMap<String, Object>();
	
	static{
		testerType.put("$gte", 2);
		testerType.put("$lte", 3);
	}
	
	/**
	 * 查找某一测试组长的所属测试人员(包含组长自己),id及名称,返回json
	 * @param req
	 * @param u
	 * @param msg 
	 */
	private void tester(HttpServletRequest req,KObject u,HttpActionMsg msg){
		int gid = Integer.parseInt(u.getProp("groupID").toString());
		//且要求user类型为组长以上级别
		if (u.getType()<3) {
			msg.addData("[print]", "");
			return;
		}
		HashMap<String,Object> q = new HashMap<String, Object>(4);
		q.put("groupID", gid);
		//类型为测试人员或组长
		q.put("type", testerType);
		q.put("state", 0);
		HashMap<String,Object> fields = new HashMap<String, Object>(4);
		fields.put("_id", 1);
		fields.put("name", 1);
		ArrayList<HashMap<String,Object>> list = dao.query(q, fields, null, 0, 0, null);
		if (list == null || list.isEmpty()) {
			msg.addData("[print]", "");
			return;
		}
		String re = JSON.write(list);
		msg.addData("[print]", re);
	}
	

	/* (non-Javadoc)
	 * @see com.k99k.khunter.Action#init()
	 */
	@Override
	public void init() {
		dao = DaoManager.findDao("TCUserDao");
		schema = KObjManager.findSchema("TCUser");
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
