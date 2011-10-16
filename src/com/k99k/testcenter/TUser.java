/**
 * 
 */
package com.k99k.testcenter;

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
		}else{
			JOut.err(404, httpmsg);
		}
		
		return super.act(msg);
	}
	
	/**
	 * 查找某一测试组长的所属测试人员(包含组长自己),id及名称,返回json
	 * @param req
	 * @param u
	 * @param msg 
	 */
	private void tester(HttpServletRequest req,KObject u,HttpActionMsg msg){
		int gid = Integer.parseInt(u.getProp("groupID").toString());
		//groupID不能为0，且要求user类型为组长以上级别
		if (gid==0 || u.getType()<3) {
			msg.addData("[print]", "");
			return;
		}
		HashMap<String,Object> q = new HashMap<String, Object>(4);
		q.put("groupID", gid);
		q.put("state", 0);
		//TODO 注意将type>=10的去掉
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
