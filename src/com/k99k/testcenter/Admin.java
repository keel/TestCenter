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
import com.k99k.khunter.TaskManager;
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
		//新增
		else if(subsub.equals("add")){
			this.addUser(subact, subsub, req, u, msg);
		}
		//搜索
		else if(subsub.equals("search")){
			this.searchUser(subact, subsub, req, u, msg);
		}
		else{
			msg.addData("[jsp]", "/WEB-INF/tc/user_search.jsp");
		}
	}
	
	private void addUser(String subact,String subsub,HttpServletRequest req,KObject u,HttpActionMsg msg){
		String uName = req.getParameter("uName");
		String uCom = req.getParameter("uCom");
		String uHand = req.getParameter("uHand");
		String uMail = req.getParameter("uMail");
		String uQQ = req.getParameter("uQQ");
		String uType = req.getParameter("uType");
		String uInfo = req.getParameter("uInfo");
		String uPass = req.getParameter("uPass");
		if (StringUtil.isStringWithLen(uName, 2)
				&& 	StringUtil.isStringWithLen(uCom, 2)
				&& 	StringUtil.isStringWithLen(uHand, 11)
				&& 	StringUtil.isStringWithLen(uMail, 2)
				&& 	StringUtil.isDigits(uType)
				&& 	StringUtil.isStringWithLen(uPass, 2)
				) {
			uName = uName.trim();
			uCom = uCom.trim();
			uHand = uHand.trim();
			uMail = uMail.trim();
			uInfo = StringUtil.isStringWithLen(uInfo, 1) ? uInfo : "";
			uQQ = StringUtil.isStringWithLen(uQQ, 1) ? uQQ : "";
			int userType = Integer.parseInt(uType);
			uPass = uPass.trim();
			KObject one = new KObject();
			one.setCreateTime(System.currentTimeMillis());
			one.setInfo(uInfo);
			one.setLevel(0);
			one.setName(uName);
			one.setState(0);
			one.setType(userType);
			one.setProp("company", uCom);
			one.setProp("email", uMail);
			one.setProp("groupID", 1);
			one.setProp("groupLeader", 0);
			one.setProp("newNews", 0);
			one.setProp("newTasks", 0);
			one.setProp("phoneNumber", uHand);
			one.setProp("pwd", uPass);
			one.setProp("qq", uQQ);
			if (TUser.dao.add(one)) {
				msg.addData("[print]", "ok");
				ActionMsg atask = new ActionMsg("sms");
				atask.addData(TaskManager.TASK_TYPE, TaskManager.TASK_TYPE_EXE_SINGLE);
				atask.addData("dests", new String[]{uHand});
				atask.addData("content", "您好,爱游戏产品测试系统帐号已开通，请在"+uMail+"中查看帐号信息。");
				TaskManager.makeNewTask("sms Task-addUser:"+uName+System.currentTimeMillis(), atask);
				
				ActionMsg atask1 = new ActionMsg("email");
				atask1.addData(TaskManager.TASK_TYPE, TaskManager.TASK_TYPE_EXE_SINGLE);
				atask1.addData("dests", new String[]{uMail});
				atask1.addData("content", "您好：\r\n\r\n用户名:\r\n"+uName+"\r\n密码:\r\n"+uPass+"\r\n\r\n请注意帐号安全，此帐号由申请公司申请人负全部安全责任。");
				atask1.addData("subject", "爱游戏产品测试系统帐号已开通");
				TaskManager.makeNewTask("email Task-addUser:"+uName+System.currentTimeMillis(), atask1);
				return;
			}
		}
		msg.addData("[print]", "err");
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
