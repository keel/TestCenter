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
import com.k99k.khunter.dao.StaticDao;
import com.k99k.tools.StringUtil;

/**
 * @author keel
 *
 */
public class Comm extends Action {

	/**
	 * @param name
	 */
	public Comm(String name) {
		super(name);
	}
	private int pageSize = 30;
	static final Logger log = Logger.getLogger(Comm.class);
	static DaoInterface dao;
	static KObjSchema schema;
	

	

	/* (non-Javadoc)
	 * @see com.k99k.khunter.Action#act(com.k99k.khunter.ActionMsg)
	 */
	@Override
	public ActionMsg act(ActionMsg msg) {
		HttpActionMsg httpmsg = (HttpActionMsg)msg;
		HttpServletRequest req = httpmsg.getHttpReq();
		String[] pathArr = (String[]) msg.getData("[pathArr]");
		int r = KFilter.getRootNum()+2;
		String tp_c_ID = (pathArr.length <= r) ? "" : pathArr[r];
		String subact = (pathArr.length <= r+1) ? "" : pathArr[r+1];
		if (!StringUtil.isDigits(tp_c_ID)) {
			JOut.err(403, httpmsg);
			return super.act(msg);
		}
		//topicId 或者是 commId,根据动作不同含义不同
		long tp_comm_Id = Long.parseLong(tp_c_ID);
		KObject u = Auth.checkCookieLogin(httpmsg);
		if (u == null) {
			msg.addData("[redirect]", "/login");
			return super.act(msg);
		}
		
		if (subact.equals("update")) {
			this.toUpdate(req,u,httpmsg,tp_comm_Id);
		}else if (subact.equals("a_a")) {
			this.add(req,u,httpmsg,tp_comm_Id);
		}else if(subact.equals("a_u")){
			this.update(req, u, httpmsg, tp_comm_Id);
		}else if(subact.equals("a_d")){
			this.del(req, u, httpmsg, tp_comm_Id);
		}
		return super.act(msg);
	}
	
	void queryPage(HashMap<String,Object> query,String subact,HttpServletRequest req,KObject u,HttpActionMsg msg){
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
		msg.addData("[jsp]", "/WEB-INF/tc/topics.jsp");
	}
	
	/**
	 * 删除
	 * @param req
	 * @param u
	 * @param msg
	 * @param cid
	 */
	private void del(HttpServletRequest req,KObject u,HttpActionMsg msg,long cid){
		KObject kobj = dao.findOne(cid);
		if (kobj==null) {
			JOut.err(404, msg);
			return;
		}
		if (u.getType() <= 10 && !u.getName().equals(kobj.getCreatorName())) {
			JOut.err(401,"E401"+Err.ERR_AUTH_FAIL, msg);
			return;
		}
		if (dao.deleteOne(cid) != null) {
			msg.addData("[print]", "ok");
			return;
		}
		JOut.err(500, "E500"+Err.ERR_COMM_DEL, msg);
	}
	
	/**
	 * 更新
	 * @param req
	 * @param u
	 * @param msg
	 * @param cid
	 */
	private void update(HttpServletRequest req,KObject u,HttpActionMsg msg,long cid){
		String c_name = req.getParameter("c_name");
		String c_text = req.getParameter("c_text");
		String c_state = req.getParameter("c_state");
		if (!StringUtil.isStringWithLen(c_text, 1)) {
			JOut.err(401,"E403"+Err.ERR_AUTH_FAIL, msg);
			return;
		}
		c_text =  StringUtil.repstr1(c_text.trim());
		c_name = (StringUtil.isStringWithLen(c_name, 1)) ?  StringUtil.repstr1(c_name.trim()) : "";
		KObject kobj = dao.findOne(cid);
		if (kobj==null) {
			JOut.err(404, msg);
			return;
		}
		if (u.getType() <= 10 && !u.getName().equals(kobj.getCreatorName())) {
			JOut.err(401,"E401"+Err.ERR_AUTH_FAIL, msg);
			return;
		}
		kobj.setName(c_name);
		kobj.setProp("text", c_text);
		if (StringUtil.isDigits(c_state)) {
			kobj.setState(Integer.parseInt(c_state));
		}
		if (dao.save(kobj)) {
			msg.addData("[print]", "ok");
			return;
		}
		JOut.err(500, "E500"+Err.ERR_COMM_UPDATE, msg);
	}

	/**
	 * 转到编辑页
	 * @param req
	 * @param u
	 * @param msg
	 */
	private void toUpdate(HttpServletRequest req,KObject u,HttpActionMsg msg,long cid){
		KObject kobj = dao.findOne(cid);
		if (kobj == null) {
			JOut.err(404, msg);
			return;
		}
		if (u.getType() <= 10 && !u.getName().equals(kobj.getCreatorName())) {
			JOut.err(401,"E401"+Err.ERR_AUTH_FAIL, msg);
			return;
		}
		msg.addData("comm",kobj);
		msg.addData("u", u);
		msg.addData("[jsp]", "/WEB-INF/tc/comm_edit.jsp");
		
	}
	
	/**
	 * 新增评论
	 * @param req
	 * @param u
	 * @param msg
	 * @param tpId
	 */
	private void add(HttpServletRequest req,KObject u,HttpActionMsg msg,long tpId){
		String c_name = req.getParameter("c_name");
		String c_text = req.getParameter("c_text");
		if (!StringUtil.isStringWithLen(c_text, 1)) {
			JOut.err(401,"E403"+Err.ERR_AUTH_FAIL, msg);
			return;
		}
		c_text =  StringUtil.repstr1(c_text.trim());
		c_name = (StringUtil.isStringWithLen(c_name, 1)) ?  StringUtil.repstr1(c_name.trim()) : "";
		KObject kobj = schema.createEmptyKObj(dao);
		kobj.setName(c_name);
		kobj.setProp("text", c_text.trim());
		kobj.setProp("TPID", tpId);
		kobj.setProp("creatorName", u.getName());
		if (dao.save(kobj)) {
			//re为commId
			String re = String.valueOf(kobj.getId());
			//更新topic
			HashMap<String,Object> q = new HashMap<String, Object>(2);
			q.put("_id", tpId);
			boolean re2 = Topic.dao.updateOne(q, StaticDao.prop_topic_comm_inc);
			if (!re2) {
				JOut.err(500,"E500"+Err.ERR_COMM_ADD_COUNT, msg);
			}else{
				msg.addData("[print]", re);
			}
			return;
		}
		JOut.err(500, "E500"+Err.ERR_COMM_ADD, msg);
	}


	/* (non-Javadoc)
	 * @see com.k99k.khunter.Action#init()
	 */
	@Override
	public void init() {
		dao = DaoManager.findDao("TCCommDao");
		schema = KObjManager.findSchema("TCComm");
		super.init();
	}



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
