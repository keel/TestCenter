/**
 * 
 */
package com.k99k.testcenter;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Pattern;

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
import com.k99k.khunter.MongoDao;
import com.k99k.khunter.TaskManager;
import com.k99k.khunter.dao.StaticDao;
import com.k99k.tools.JSON;
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
	static final Logger log = Logger.getLogger(TTask.class);
	static DaoInterface dao;
	static KObjSchema schema;
	
	public static final int TASK_STATE_NEW = 0;
	public static final int TASK_STATE_TEST = 1;
	public static final int TASK_STATE_PASS = 2;
	public static final int TASK_STATE_PASS_PART = 4;
	public static final int TASK_STATE_NEED_MOD = 3;
	public static final int TASK_STATE_DROP = -2;
	public static final int TASK_STATE_DEL = -1;
	public static final int TASK_STATE_PAUSE = 5;
	public static final int TASK_STATE_CONFIRM = 6;
	public static final int TASK_STATE_BACKED = 8;
	

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
		}else if (subact.equals("my")) {
			String subact2 = KFilter.actPath(msg, 3, "");
			if (subact2.equals("")) {
				this.listMy(subact,req, u, httpmsg);
			}else if(subact2.equals("a_s")){
				this.search(subact,req, u, httpmsg);
			}
		}else if(subact.equals("add")){
			msg.addData("sub", subact);
			this.toAdd(u, httpmsg);
		}else if (StringUtil.isDigits(subact)) {
			this.one(subact, req, u, httpmsg);
		}else if(subact.equals("a_a")){
			this.add(req, u, httpmsg);
		}else if(subact.equals("a_p")){
			this.appoint(req, u, httpmsg);
		}else if(subact.equals("a_exec")){
			this.exec(req, u, httpmsg);
		}else if(subact.equals("a_send")){
			this.send(req, u, httpmsg);
//		}else if(subact.equals("a_comm")){
//			this.comm(req, u, httpmsg);
		}else if(subact.equals("a_summary")){
			this.summary(req, u, httpmsg);
		}else if(subact.equals("a_finish")){
			this.finish(req, u, httpmsg);
		}else if(subact.equals("a_back")){
			this.back(req, u, httpmsg);
		}else if(subact.equals("a_online")){
			this.online(req, u, httpmsg);
//		}else if(subact.equals("a_u")){
//			this.update(req, u, httpmsg);
		}else if(subact.equals("a_d")){
			this.del(req, u, httpmsg);
		}else if(subact.equals("a_drop")){
			this.drop(req, u, httpmsg);
		}else if(subact.equals("a_confirm")){
			this.confirm(req, u, httpmsg);
		}else if(subact.equals("a_s")){
			this.search(subact,req, u, httpmsg);
		}else{
			JOut.err(404, httpmsg);
		}
		return super.act(msg);
	}
	
	/**
	 * 退回厂家,实际上是将任务放弃,厂家需要重新创建
	 * @param req
	 * @param u
	 * @param msg
	 */
	private void back(HttpServletRequest req,KObject u,HttpActionMsg msg){
		//验证权限
		if (u.getType() < 4) {
			//权限不够
			JOut.err(401,"E401"+Err.ERR_AUTH_FAIL, msg);
			return;
		}
		String task_id = req.getParameter("tid");
		if(!StringUtil.isDigits(task_id) ){
			JOut.err(403,"E403"+Err.ERR_PARAS, msg);
			return;
		}
		long tid = Integer.parseInt(task_id);
		KObject task = dao.findOne(tid);
		if (task==null) {
			JOut.err(403,"E403"+Err.ERR_PARAS, msg);
			return;
		}
		//FIXME 将状态置为驳回状态，不可修改，由厂家新建
		
		
	}
	
	/**
	 * 指派任务,确定测试机型,生成TestUnit,确定执行人,调整等级,说明等
	 * @param req
	 * @param u
	 * @param msg
	 */
	@SuppressWarnings("unchecked")
	private void appoint(HttpServletRequest req,KObject u,HttpActionMsg msg){
		//验证权限
		if (u.getType() < 4) {
			//权限不够
			JOut.err(401,"E401"+Err.ERR_AUTH_FAIL, msg);
			return;
		}
		//
		String task_id = req.getParameter("tid");
		String task_info = req.getParameter("task_info");
		String task_level = req.getParameter("task_level");
		String task_operator = req.getParameter("task_operator");
		String task_tu_json_h = req.getParameter("task_tu_json_h");
		//验证
		if(!StringUtil.isDigits(task_id) ||
			!StringUtil.isStringWithLen(task_info, 1) || 
			!StringUtil.isDigits(task_level) ||
			!StringUtil.isStringWithLen(task_operator, 2) || 
			!StringUtil.isStringWithLen(task_tu_json_h, 3) 
		){
			JOut.err(403,"E403"+Err.ERR_PARAS, msg);
			return;
		}
		long tid = Long.parseLong(task_id);
		KObject task = dao.findOne(tid);
		if (task==null) {
			JOut.err(403,"E403"+Err.ERR_PARAS, msg);
			return;
		}
		KObject operator = TUser.dao.findOne(task_operator);
		if (operator== null || operator.getType()<1) {
			JOut.err(403,"E403"+ Err.ERR_ADD_OPERATOR_FAIL, msg);
			return;
		}
		int level = Integer.parseInt(task_level);
		//生成TestUnit
		try {
			ArrayList<HashMap<String,Object>> json = (ArrayList<HashMap<String,Object>>) JSON.read(task_tu_json_h);
			if (json==null || json.isEmpty()) {
				JOut.err(403,"E403"+ Err.ERR_JSON, msg);
				return;
			}
			Iterator<HashMap<String,Object>> it = json.iterator();
			KObject tu = new KObject();
			tu.setProp("TID", tid);
			tu.setProp("PID", task.getProp("PID"));
			tu.setProp("tester", operator.getName());
			tu.setInfo(task_info);
			tu.setLevel(level);
			tu.setCreatorName(u.getName());
			while (it.hasNext()) {
				HashMap<String,Object> map = it.next();
				String gFile = map.get("gFile").toString();
				long fileId = Long.parseLong(map.get("fileId").toString());
				ArrayList<String> phList = (ArrayList<String>)map.get("phone");
				Iterator<String> li = phList.iterator();
				tu.setProp("gFile", gFile);
				tu.setProp("fileId", fileId);
				tu.setProp("level", task.getLevel());
				while (li.hasNext()) {
					String ph = li.next();
					tu.setProp("phone", ph);
					if(!TestUnit.dao.add(tu)){
						JOut.err(500,"E500"+ Err.ERR_DB_UPDATE, msg);
						return;
					}
				}
			}
		} catch (Exception e) {
			JOut.err(403,"E403"+ Err.ERR_ADD_TESTUNIT, msg);
			return;
		}
		//更新任务属性
		
		HashMap<String,Object> q = new HashMap<String, Object>(2);
		q.put("_id", tid);
		HashMap<String,Object> set = new HashMap<String, Object>();
		set.put("level", level);
		set.put("operator", operator.getName());
		set.put("state", TASK_STATE_TEST);
		HashMap<String,Object> logmsg = new HashMap<String, Object>();
		logmsg.put("time", System.currentTimeMillis());
		logmsg.put("user", u.getName());
		logmsg.put("info", "分配任务 - "+task_info);
		HashMap<String,Object> push = new HashMap<String, Object>(2);
		push.put("log", logmsg);
		HashMap<String,Object> update = new HashMap<String, Object>();
		update.put("$set", set);
		update.put("$push", push);
		if(dao.update(q, update, false, false)){
			//清除自己待办任务,指定为一下执行人
			ActionMsg atask = new ActionMsg("tTaskTask");
			atask.addData(TaskManager.TASK_TYPE, TaskManager.TASK_TYPE_EXE_POOL);
			atask.addData("tid", tid);
			atask.addData("oid", operator.getId());
			atask.addData("uName", task.getProp("operator").toString());
			atask.addData("act", "appoint");
			TaskManager.makeNewTask("TTaskTask-appoint_"+tid, atask);
			msg.addData("[print]", task.getId());
		}else{
			log.error("appoint task faild:"+tid);
			JOut.err(500,"E500"+ Err.ERR_DB_UPDATE, msg);
			return;
		}
		
		
	}
	
	/**
	 * 转发TestUnit
	 * @param req
	 * @param u
	 * @param msg
	 */
	@SuppressWarnings("unchecked")
	private void send(HttpServletRequest req,KObject u,HttpActionMsg msg){
		//验证权限
		if (u.getType() < 2) {
			//权限不够
			JOut.err(401,"E401"+Err.ERR_AUTH_FAIL, msg);
			return;
		}
		String t_id = req.getParameter("tid");
		String t_json = req.getParameter("task_tu_json_h");
		if (!StringUtil.isDigits(t_id) || !StringUtil.isStringWithLen(t_json, 2)) {
			JOut.err(403,"E403"+Err.ERR_PARAS, msg);
			return;
		}
		try {
			ArrayList<HashMap<String,Object>> json = (ArrayList<HashMap<String,Object>>) JSON.read(t_json);
			if (json==null || json.isEmpty()) {
				JOut.err(403,"E403"+ Err.ERR_JSON, msg);
				return;
			}
			Iterator<HashMap<String,Object>> it = json.iterator();
			HashMap<String,Object> q = new HashMap<String, Object>(4);
			HashMap<String,Object> set = new HashMap<String, Object>(4);
			HashMap<String,Object> sett = new HashMap<String, Object>(4);
			boolean re = true;
			while (it.hasNext()) {
				HashMap<String,Object> m = it.next();
				String tester = (String)m.get("n");
				ArrayList<String> tus = (ArrayList<String>)m.get("us");
				Iterator<String> itr = tus.iterator();
				while (itr.hasNext()) {
					long uid = Long.parseLong(itr.next());
					q.put("_id", uid);
					//必须是待测状态
					q.put("state", TASK_STATE_NEW);
					sett.put("tester", tester);
					set.put("$set", sett);
					re = TestUnit.dao.updateOne(q, set);
				}
			}
			if (re) {
				ActionMsg atask = new ActionMsg("tTaskTask");
				atask.addData(TaskManager.TASK_TYPE, TaskManager.TASK_TYPE_EXE_POOL);
				atask.addData("tid", Long.parseLong(t_id));
				atask.addData("json", json);
				atask.addData("act", "send");
				HashMap<String,Object> logmsg = new HashMap<String, Object>();
				logmsg.put("time", System.currentTimeMillis());
				logmsg.put("user", u.getName());
				logmsg.put("info", "更新任务分配");
				atask.addData("logmsg",logmsg);
				TaskManager.makeNewTask("TTaskTask-send_"+t_id, atask);
				msg.addData("[print]", "ok");
			}else{
				JOut.err(500,"E500"+Err.ERR_SEND_TESTUNIT, msg);
				return;
			}
		} catch (Exception e) {
			JOut.err(403,"E403"+Err.ERR_SEND_TESTUNIT, msg);
			return;
		}
		
	}
	
	/**
	 * 执行TestUnit,测试项给结果,给评价,问题归总
	 * @param req
	 * @param u
	 * @param msg
	 */
	@SuppressWarnings("unchecked")
	private void exec(HttpServletRequest req,KObject u,HttpActionMsg msg){
		//验证权限
		int userType = u.getType();
		if (userType < 2 || (userType >4 && userType !=99)) {
			//权限不够
			JOut.err(401,"E401"+Err.ERR_AUTH_FAIL, msg);
			return;
		}
		
		String tu_id = req.getParameter("tu_id");
		String u_json = req.getParameter("json");
		String u_rank = req.getParameter("rank");
		String u_sys = req.getParameter("sys");
		String files = req.getParameter("ff");
		if (!StringUtil.isDigits(tu_id) || !StringUtil.isDigits(u_sys) || !StringUtil.isStringWithLen(u_json, 2) || !StringUtil.isDigits(u_rank) ) {
			JOut.err(403,"E403"+Err.ERR_PARAS, msg);
			return;
		}
		long tuid=Long.parseLong(tu_id);
		int rank=Integer.parseInt(u_rank);
		int sys = Integer.parseInt(u_sys);
		KObject tu = TestUnit.dao.findOne(tuid);
		KObject task = dao.findOne((Long)tu.getProp("TID"));
		if (task.getState() != 1 || (userType==2 && !tu.getProp("tester").equals(u.getName()))) {
			//非测试人员自己的任务或非测试中的任务
			JOut.err(401,"E401"+Err.ERR_AUTH_FAIL, msg);
			return;
		}
		try {
			ArrayList<HashMap<String,Object>> json = (ArrayList<HashMap<String,Object>>) JSON.read(u_json);
			if (json==null || json.isEmpty()) {
				JOut.err(403,"E403"+ Err.ERR_JSON, msg);
				return;
			}
			ArrayList<HashMap<String,Object>> res = TestCase.checkJson(sys, json);
			HashMap<String,Object> result = res.remove(0);
			int state = Integer.parseInt(result.get("re").toString());
			
			HashMap<String,Object> q = new HashMap<String, Object>();
			HashMap<String,Object> set = new HashMap<String, Object>(4);
			HashMap<String,Object> sett = new HashMap<String, Object>();
			q.put("_id", tuid);
			sett.put("state", state);
			sett.put("re", res);
			sett.put("rank", rank);
			if (StringUtil.isStringWithLen(files, 2)) {
				sett.put("attachs", files);
			}
			set.put("$set", sett);
			if(TestUnit.dao.updateOne(q, set)){
				ActionMsg atask = new ActionMsg("tTaskTask");
				atask.addData(TaskManager.TASK_TYPE, TaskManager.TASK_TYPE_EXE_POOL);
				atask.addData("tuid", tuid);
				atask.addData("tester", u.getName());
				atask.addData("act", "exec");
				TaskManager.makeNewTask("TTaskTask-exec-tuid_"+tuid, atask);
				msg.addData("[print]", "ok");
			}else{
				JOut.err(500,"E500"+Err.ERR_EXEC_TESTUNIT, msg);
			}
		} catch (Exception e) {
			log.error("TTask exec error.", e);
			JOut.err(403,"E403"+Err.ERR_EXEC_TESTUNIT, msg);
			return;
		}
		
	}
	
	
	/**
	 * 汇总某一任务的所有TestUnit问题,json输出,要求组长及以上权限
	 * @param req
	 * @param u
	 * @param msg
	 */
	private void summary(HttpServletRequest req,KObject u,HttpActionMsg msg){
		//验证权限
		if (u.getType() < 3) {
			//权限不够
			JOut.err(401,"E401"+Err.ERR_AUTH_FAIL, msg);
			return;
		}
		String t_id = req.getParameter("tid");
		String u_sys = req.getParameter("sys");
		if (!StringUtil.isDigits(t_id) || !StringUtil.isDigits(u_sys)) {
			JOut.err(403,"E403"+Err.ERR_PARAS, msg);
			return;
		}
		long tid = Long.parseLong(t_id);
		int sys = Integer.parseInt(u_sys);
		String r = this.taskSummary(tid,sys);
		if (r!=null) {
			msg.addData("[print]", r);
			return;
		}
		JOut.err(500,"E500"+Err.ERR_SUMMARY_TAST, msg);
	}
	
	/**
	 * 汇总某一任务的所有TestUnit问题,json输出
	 * @param tid
	 * @param sys
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private final String taskSummary(long tid,int sys){
		HashMap<String,Object> q = new HashMap<String, Object>();
		q.put("TID", tid);
		HashMap<String,Object> state = new HashMap<String, Object>(4);
		state.put("$gte", TASK_STATE_NEW);
		q.put("state", state);
		HashMap<String, Object> res = new HashMap<String,Object>();
		try {
			ArrayList<KObject> tus = TestUnit.dao.queryKObj(q, null, null, 0, 0, null);
			Iterator<KObject> it = tus.iterator();
			StringBuilder sb = new StringBuilder("");
			while (it.hasNext()) {
				KObject tu = (KObject) it.next();
				//未通过或部分通过的TU
				if (tu.getState()>2) {
					//取得re中的原因
					ArrayList<HashMap<String,Object>> reList = (ArrayList<HashMap<String,Object>>) tu.getProp("re");
					Iterator<HashMap<String,Object>> itr = reList.iterator();
					while (itr.hasNext()) {
						HashMap<String,Object> re = itr.next();
						if (Integer.parseInt(re.get("re").toString())>2) {
							String caseId = re.get("caseId").toString();
							ArrayList<HashMap<String,Object>> old = (ArrayList<HashMap<String, Object>>) res.get(caseId);
							if (old==null) {
								//在第一个位置加入case的信息
								old = new ArrayList<HashMap<String,Object>>();
								old.add(TestCase.findCase(sys, Integer.parseInt(caseId)));
							}
							re.put("phone", tu.getProp("phone"));
							old.add(re);
							res.put(caseId, old);
						}
					}
					if (StringUtil.isStringWithLen(tu.getProp("attachs"), 2)) {
						sb.append(",").append(tu.getProp("attachs"));
					}
				}
			}
			if (sb.length()>0) {
				res.put("attachs", sb.toString());
			}
			String reStr = (res.isEmpty())?"":JSON.write(res);
			return reStr;
		} catch (Exception e) {
			log.error("E500"+Err.ERR_SUMMARY_TAST,e);
		}
		return null;
	}
	
	
//	/**
//	 * 任务讨论,注意一个产品对应一个主题
//	 * @param req
//	 * @param u
//	 * @param msg
//	 */
//	private void comm(HttpServletRequest req,KObject u,HttpActionMsg msg){
//		//
//	}
	
	/**
	 * 退回或确认结果并通知任务创建者
	 * @param req
	 * @param u
	 * @param msg
	 */
	@SuppressWarnings("unchecked")
	private void finish(HttpServletRequest req,KObject u,HttpActionMsg msg){
		//验证权限
		int userType = u.getType();
		if (userType != 4 && userType != 99) {
			//权限不够
			JOut.err(401,"E401"+Err.ERR_AUTH_FAIL, msg);
			return;
		}
		String t_id = req.getParameter("tid");
		String tu_re = req.getParameter("tu_pass");
		String task_info = req.getParameter("task_info");
		String task_operator = req.getParameter("task_operator");
		if (!StringUtil.isDigits(t_id) || !StringUtil.isDigits(tu_re) 
				|| !StringUtil.isStringWithLen(task_operator, 1)) {
			JOut.err(403,"E403"+Err.ERR_PARAS, msg);
			return;
		}
		if(task_info==null){task_info="";}
		long tid = Long.parseLong(t_id);
		int tuRE = Integer.parseInt(tu_re);
		HashMap<String,Object> q = new HashMap<String, Object>(4);
		HashMap<String,Object> set = new HashMap<String, Object>(4);
		HashMap<String,Object> update = new HashMap<String, Object>();
		q.put("_id", tid);
		if (tuRE == 2 || tuRE == 4) {
			//通过或部分通过
			update.put("operator", task_operator);
			update.put("isOnline", 1);
		}else if(tuRE==9){
			//不通过,状态置为待反馈
			update.put("state", TASK_STATE_NEED_MOD);
			//long pid = (Long)(dao.findOne(tid).getProp("PID"));
			KObject task = dao.findOne(tid);
			task_operator = Company.dao.findOne(task.getProp("company").toString()).getProp("mainUser").toString();
			update.put("operator", task_operator);
			
		}else if(tuRE==-3){
			//退回
			HashMap<String,Object> slice = new HashMap<String, Object>();
			slice.put("$slice", -1);
			HashMap<String,Object> last = new HashMap<String, Object>(2);
			last.put("log",slice);
			last.put("_id", 1);
			HashMap<String,Object> lastLog = dao.query(q, last, null, 0, 0, null).get(0);
			if (lastLog != null) {
				ArrayList<HashMap<String,Object>> logs = (ArrayList<HashMap<String, Object>>) lastLog.get("log");
				if (logs != null &&  !logs.isEmpty()) {
					task_operator = logs.get(0).get("user").toString();
				}else{
					task_operator = lastLog.get("creatorName").toString();
				}
			}
			update.put("operator", task_operator);
			update.put("state", TASK_STATE_TEST);
			task_info = "退回组长:"+task_operator;
		}else if(tuRE==TASK_STATE_DROP){
			//放弃
			update.put("state", TASK_STATE_DROP);
		}else{
			JOut.err(403,"E403"+Err.ERR_PARAS, msg);
			return;
		}
		HashMap<String,Object> logmsg = new HashMap<String, Object>();
		logmsg.put("time", System.currentTimeMillis());
		logmsg.put("user", u.getName());
		logmsg.put("info", "确认完成 - "+task_info);
		HashMap<String,Object> push = new HashMap<String, Object>(2);
		push.put("log", logmsg);
		set.put("$push", push);
		set.put("$set", update);
		if(dao.updateOne(q, set)){
			ActionMsg atask = new ActionMsg("tTaskTask");
			atask.addData(TaskManager.TASK_TYPE, TaskManager.TASK_TYPE_EXE_POOL);
			atask.addData("tid", tid);
			atask.addData("re", tuRE);
			atask.addData("info", task_info);
			atask.addData("operator", task_operator);
			atask.addData("uid", u.getId());
			atask.addData("act", "finish");
			TaskManager.makeNewTask("TTaskTask-finish_"+tid, atask);
			msg.addData("[print]", "ok");
			return;
		}
		JOut.err(500,"E500"+Err.ERR_FINISH_TASK, msg);
	}
	
	/**
	 * 上线或退回
	 * @param req
	 * @param u
	 * @param msg
	 */
	@SuppressWarnings("unchecked")
	private void online(HttpServletRequest req,KObject u,HttpActionMsg msg){
		//验证权限
		int userType = u.getType();
		if (userType != 12 && userType != 99) {
			//权限不够
			JOut.err(401,"E401"+Err.ERR_AUTH_FAIL, msg);
			return;
		}
		String t_id = req.getParameter("tid");
		String tu_re = req.getParameter("tu_re");
		String task_info = req.getParameter("task_info");
		if (!StringUtil.isDigits(t_id) || !StringUtil.isDigits(tu_re)) {
			JOut.err(403,"E403"+Err.ERR_PARAS, msg);
			return;
		}
		if(task_info==null){task_info="";}
		long tid = Long.parseLong(t_id);
		int tuRE = Integer.parseInt(tu_re);
		HashMap<String,Object> q = new HashMap<String, Object>(4);
		HashMap<String,Object> set = new HashMap<String, Object>(4);
		HashMap<String,Object> update = new HashMap<String, Object>();
		String task_operator = "曹雨";
		q.put("_id", tid);
		if (tuRE == TASK_STATE_PASS|| tuRE == TASK_STATE_PASS_PART) {
			//通过或部分通过
			update.put("isOnline", 2);
			update.put("state", tuRE);
		}else if(tuRE==TASK_STATE_DROP){
			//放弃
			update.put("state", TASK_STATE_DROP);
		}else if(tuRE==-3){
			//退回
			HashMap<String,Object> slice = new HashMap<String, Object>();
			slice.put("_id", 1);
			slice.put("creatorName", 1);
			HashMap<String,Object> last = new HashMap<String, Object>(2);
			last.put("log", -1);
			slice.put("$slice", last);
			HashMap<String,Object> lastLog = dao.query(q, slice, null, 0, 0, null).get(0);
			if (lastLog != null) {
				ArrayList<HashMap<String,Object>> logs = (ArrayList<HashMap<String, Object>>) lastLog.get("log");
				if (logs != null &&  !logs.isEmpty()) {
					task_operator = logs.get(0).get("user").toString();
				}else{
					task_operator = lastLog.get("creatorName").toString();
				}
			}
			update.put("operator", task_operator);
		}else{
			JOut.err(403,"E403"+Err.ERR_PARAS, msg);
			return;
		}
		HashMap<String,Object> logmsg = new HashMap<String, Object>();
		logmsg.put("time", System.currentTimeMillis());
		logmsg.put("user", u.getName());
		logmsg.put("info", "上线 - "+task_info);
		HashMap<String,Object> push = new HashMap<String, Object>(2);
		push.put("log", logmsg);
		set.put("$push", push);
		set.put("$set", update);
		if(dao.updateOne(q, set)){
			ActionMsg atask = new ActionMsg("tTaskTask");
			atask.addData(TaskManager.TASK_TYPE, TaskManager.TASK_TYPE_EXE_POOL);
			atask.addData("tid", tid);
			atask.addData("uid", u.getId());
			atask.addData("re", tuRE);
			if (tuRE==-3) {
				atask.addData("operator", task_operator);
			}
			atask.addData("act", "online");
			TaskManager.makeNewTask("TTaskTask-online_"+tid, atask);
			msg.addData("[print]", "ok");
			return;
		}
		JOut.err(500,"E500"+Err.ERR_ONLINE_TASK, msg);
	}
	
	/**
	 * 任务问题定版(state置为6),提交权限为4或以上人员确认
	 * @param req
	 * @param u
	 * @param msg
	 */
	private void confirm(HttpServletRequest req,KObject u,HttpActionMsg msg){
		//验证权限
		if (u.getType() < 3) {
			//权限不够
			JOut.err(401,"E401"+Err.ERR_AUTH_FAIL, msg);
			return;
		}
		String t_id = req.getParameter("tid");
		String u_sys = req.getParameter("sys");
		String task_operator = req.getParameter("task_operator");
		
		if (!StringUtil.isDigits(t_id) || !StringUtil.isDigits(u_sys) || !StringUtil.isStringWithLen(task_operator, 1)) {
			JOut.err(403,"E403"+Err.ERR_PARAS, msg);
			return;
		}
		long tid = Long.parseLong(t_id);
		int sys = Integer.parseInt(u_sys);
		//重新汇总
		String reJson = this.taskSummary(tid, sys);
		//更新任务属性
		HashMap<String,Object> q = new HashMap<String, Object>(4);
		HashMap<String,Object> set = new HashMap<String, Object>(4);
		HashMap<String,Object> sett = new HashMap<String, Object>();
		q.put("_id", tid);
		sett.put("state", TASK_STATE_CONFIRM);
		sett.put("result", reJson);
		sett.put("operator", task_operator);
		HashMap<String,Object> logmsg = new HashMap<String, Object>();
		logmsg.put("time", System.currentTimeMillis());
		logmsg.put("user", u.getName());
		logmsg.put("info", "汇总结果");
		HashMap<String,Object> push = new HashMap<String, Object>(2);
		push.put("log", logmsg);
		set.put("$push", push);
		set.put("$set", sett);
		if(dao.updateOne(q, set)){
			ActionMsg atask = new ActionMsg("tTaskTask");
			atask.addData(TaskManager.TASK_TYPE, TaskManager.TASK_TYPE_EXE_POOL);
			atask.addData("tid", tid);
			atask.addData("operator", task_operator);
			atask.addData("uid", u.getId());
			atask.addData("act", "confirm");
			TaskManager.makeNewTask("TTaskTask-confirm_"+tid, atask);
			msg.addData("[print]", "ok");
			return;
		}
		JOut.err(500,"E500"+Err.ERR_CONFIRM_TASK, msg);
	}
	
//	/**
//	 * 更新任务,改变状态(删除,暂停,取消等),调整执行人,增加说明,修改TestUnit
//	 * @param req
//	 * @param u
//	 * @param msg
//	 */
//	private void update(HttpServletRequest req,KObject u,HttpActionMsg msg){
//		//
//	}
	
	/**
	 * 删除任务
	 * @param req
	 * @param u
	 * @param msg
	 */
	private void del(HttpServletRequest req,KObject u,HttpActionMsg msg){
		if (u.getType() < 4) {
			//权限不够
			JOut.err(401, msg);
			return;
		}
		if (StringUtil.isDigits(req.getParameter("id"))) {
			long id = Long.parseLong(req.getParameter("id"));
			if (dao.deleteOne(id) !=null) {
				ActionMsg atask = new ActionMsg("tTaskTask");
				atask.addData(TaskManager.TASK_TYPE, TaskManager.TASK_TYPE_EXE_POOL);
				atask.addData("taskId", id);
				atask.addData("act", "del");
				TaskManager.makeNewTask("TTaskTask-del_"+id, atask);
				msg.addData("[print]", "ok");
				return;
			}
		}
		JOut.err(403, msg);
	}
	
	/**
	 * 放弃任务
	 * @param req
	 * @param u
	 * @param msg
	 */
	private void drop(HttpServletRequest req,KObject u,HttpActionMsg msg){
		
		if (StringUtil.isDigits(req.getParameter("id"))) {
			long id = Long.parseLong(req.getParameter("id"));
			if (u.getType() < 4) {
				KObject task = dao.findOne(id);
				if (!task.getProp("company").equals(u.getProp("company"))) {
					//权限不够
					JOut.err(401, msg);
					return;
				}
			}
			HashMap<String,Object> query = new HashMap<String, Object>(2);
			query.put("_id", id);
			HashMap<String,Object> set = new HashMap<String, Object>(2);
			HashMap<String,Object> drop = new HashMap<String, Object>(2);
			drop.put("state", TASK_STATE_DROP);
			set.put("$set", drop);
			if (dao.updateOne(query, set)) {
				ActionMsg atask = new ActionMsg("tTaskTask");
				atask.addData(TaskManager.TASK_TYPE, TaskManager.TASK_TYPE_EXE_POOL);
				atask.addData("taskId", id);
				atask.addData("act", "drop");
				TaskManager.makeNewTask("TTaskTask-drop_"+id, atask);
				msg.addData("[print]", "ok");
				return;
			}
		}
		JOut.err(403, msg);
	}
	/**
	 * 处理任务添加
	 * @param req
	 * @param u
	 * @param msg
	 */
	@SuppressWarnings("unchecked")
	private void add(HttpServletRequest req,KObject u,HttpActionMsg msg){
		String task_info = req.getParameter("task_info");
		String task_level = req.getParameter("task_level");
		String task_operator = req.getParameter("task_operator");
		String task_p_json_h = req.getParameter("task_p_json_h");
		String task_type_h = req.getParameter("task_type_h");
		//验证
		if(!StringUtil.isStringWithLen(task_info, 1) || 
			!StringUtil.isDigits(task_type_h) ||
			!StringUtil.isStringWithLen(task_operator, 2) || 
			!StringUtil.isStringWithLen(task_p_json_h, 5) 
		){
			JOut.err(403,"E403"+Err.ERR_PARAS, msg);
			return;
		}
		int taskLevel = StringUtil.isDigits(task_level)&& (u.getType()>1)?Integer.parseInt(task_level):0;
		task_info = StringUtil.repstr1(task_info);
//		try {
//			task_p_json_h = URLDecoder.decode(task_p_json_h, "utf-8");
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//			JOut.err(403,"E403"+Err.ERR_PARAS+"-decode", msg);
//			return;
//		}
		HashMap<String,Object> json = (HashMap<String, Object>) JSON.read(task_p_json_h);
		if (Company.dao.findOne(json.get("company").toString()) == null) {
			JOut.err(403,"E403"+Err.ERR_PARAS+"-company", msg);
			return;
		}
		//创建产品确定PID
		long pid = -10;
		if (!json.containsKey("_id")) {
			pid = Product.add(json);
			if(pid<0){
				JOut.err(403,"E403"+ Err.ERR_ADD_PRODUCT_FAIL+pid, msg);
				return;
			}
		}else{
			pid = Long.parseLong(String.valueOf(json.get("_id")));
		}
		//创建任务
		KObject operator = TUser.dao.findOne(task_operator);
		if (operator== null || operator.getType()<1) {
			JOut.err(403,"E403"+ Err.ERR_ADD_OPERATOR_FAIL, msg);
			return;
		}
		KObject task = new KObject();
		task.setName((String)json.get("name"));
		task.setCreatorName(u.getName());
		task.setInfo(task_info);
		task.setProp("operator", operator.getName());
		task.setLevel(taskLevel);
		task.setProp("PID", pid);
		task.setProp("company", json.get("company").toString());
		task.setType(Integer.parseInt(task_type_h));
		HashMap<String,Object> log = new HashMap<String, Object>();
		log.put("time", System.currentTimeMillis());
		log.put("user", u.getName());
		log.put("info", "创建任务 - "+task_info);
		Object[] logs = new Object[]{log};
		task.setProp("log", logs);
		task.setId(dao.getIdm().nextId());
		if(!dao.save(task)){
			JOut.err(500,"E500"+ Err.ERR_ADD_TASK_FAIL, msg);
			return;
		}
		//异步任务:任务通知,上传文件处理
		ActionMsg atask = new ActionMsg("tTaskTask");
		atask.addData(TaskManager.TASK_TYPE, TaskManager.TASK_TYPE_EXE_POOL);
		atask.addData("taskId", task.getId());
		atask.addData("tType", task.getType());
		atask.addData("operatorId", operator.getId());
		atask.addData("pid", pid);
		atask.addData("creatorName", u.getName());
		atask.addData("act", "add");
		if (json.containsKey("files")) {
			atask.addData("files", json.get("files"));
		}
		TaskManager.makeNewTask("TTaskTask-add_"+task.getId(), atask);
		msg.addData("[print]", task.getId());
	}
	
	
	
	
	private void search(String subact,HttpServletRequest req,KObject u,HttpActionMsg msg){
		if (StringUtil.isStringWithLen(req.getParameter("k"), 1)) {
			String key = null;
			try {
				//TODO 针对tomcatURL编码转换
				key = new String(req.getParameter("k").getBytes("ISO-8859-1"),"utf-8").trim();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
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
		if (u.getType() < 1) {
			//权限不够
			JOut.err(401, msg);
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
		//Task
		KObject one = dao.findOne(id);
		if (one== null || one.getState() < 0) {
			JOut.err(404, msg);
			return;
		}
		//产品
		long pid = (Long)one.getProp("PID");
		KObject product = Product.dao.findOne(pid);
		msg.addData("u", u);
		msg.addData("one", one);
		msg.addData("product", product);
		//Task的状态处于待分配(已创建)
		if (one.getState()==0 || (u.getType() == 1 && one.getState()==1)) {
			//显示待分配的文件或URL
			int sys = Integer.parseInt(product.getProp("sys").toString());
			if (sys!=2) {
				HashMap<String,Object> q = new HashMap<String, Object>();
				q.put("TID", one.getId());
				ArrayList<KObject> files = GameFile.dao.queryKObj(q, null, null, 0, 0, null);
				msg.addData("files", files);
			}
		}else {
			//查找本Task所属的TestUnit
			HashMap<String,Object> q = new HashMap<String, Object>(2);
			q.put("TID", one.getId());
			ArrayList<KObject> tus = TestUnit.dao.queryKObj(q, null, MongoDao.prop_id_desc, 0, 0, null);
			msg.addData("tus", tus);
		}
		
		//转到编辑时判断权限:是否为任务创建者或type>=4
		if (req.getParameter("edit")!=null && (u.getName().equals(one.getCreatorName()) || u.getType()>=4)) {
			msg.addData("[jsp]", "/WEB-INF/tc/task_edit.jsp");
		}else{
			msg.addData("[jsp]", "/WEB-INF/tc/task_one.jsp");
		}
	}
	
	
	/**
	 * 查看所有任务列表,要求权限为测试员及其以上
	 * @param subact
	 * @param req
	 * @param u
	 * @param msg
	 */
	private void list(String subact,HttpServletRequest req,KObject u,HttpActionMsg msg){
		//验证权限
		int userType = u.getType();
		if (userType < 1) {
			//权限不够
			JOut.err(401,"E401"+Err.ERR_AUTH_FAIL, msg);
			return;
		}
		if (userType==1) {
			//厂家看到的是company为自己公司的任务
			String p_str = req.getParameter("p");
			String pz_str = req.getParameter("pz");
			int page = StringUtil.isDigits(p_str)?Integer.parseInt(p_str):1;
			int pz = StringUtil.isDigits(pz_str)?Integer.parseInt(pz_str):this.pageSize;
			ArrayList<KObject> list = null;
			HashMap<String,Object> q = new HashMap<String, Object>();
			HashMap<String,Object> state = new HashMap<String, Object>(2);
			state.put("$gte", TASK_STATE_NEW);
			q.put("state", state);
			q.put("company", u.getProp("company"));
			list = dao.queryByPage(page,pageSize,q, null, StaticDao.prop_level_id_asc, null);
			msg.addData("u", u);
			msg.addData("list", list);
			msg.addData("pz", pz);
			msg.addData("p", page);
			msg.addData("sub", subact);
			msg.addData("[jsp]", "/WEB-INF/tc/tasks.jsp");
			return;
		}else{
			this.queryPage(StaticDao.prop_state_normal,subact, req, u, msg);
		}
	}
	
	/**
	 * 查看我的任务列表
	 * @param subact
	 * @param req
	 * @param u
	 * @param msg
	 */
	@SuppressWarnings("unchecked")
	private void listMy(String subact,HttpServletRequest req,KObject u,HttpActionMsg msg){
		String p_str = req.getParameter("p");
		String pz_str = req.getParameter("pz");
		int page = StringUtil.isDigits(p_str)?Integer.parseInt(p_str):1;
		int pz = StringUtil.isDigits(pz_str)?Integer.parseInt(pz_str):this.pageSize;
		int userType = u.getType();
		
		ArrayList<KObject> list = null;
		//厂家和测试组看到的是自己的待处理任务
		if ((userType>=1 && userType<=4) || userType>90) {
			ArrayList<Long> taskIds = (ArrayList<Long>) u.getProp("unReadTasks");
			HashMap<String,Object> q = new HashMap<String, Object>();
			HashMap<String,Object> state = new HashMap<String, Object>(4);
			state.put("$gte", TASK_STATE_NEW);
			q.put("state", state);
			HashMap<String,Object> in = new HashMap<String, Object>(4);
			if (taskIds == null) {
				in.put("$eq", 0);
			}else{
				in.put("$in", taskIds);
			}
			q.put("_id", in);
			list = dao.queryByPage(page,pageSize,q, null, StaticDao.prop_level_id_asc, null);
		}
//		else if (userType==1) {
//		//厂家看到的是company为自己公司的任务
//			HashMap<String,Object> q = new HashMap<String, Object>();
//			HashMap<String,Object> state = new HashMap<String, Object>(2);
//			state.put("$gte", TASK_STATE_NEW);
//			q.put("state", state);
//			q.put("company", u.getProp("company"));
//			list = dao.queryByPage(page,pageSize,q, null, StaticDao.prop_level_id_asc, null);
//		}
		else{
		//其他看到的是自己创建的任务
			HashMap<String,Object> q = new HashMap<String, Object>();
			HashMap<String,Object> state = new HashMap<String, Object>(2);
			state.put("$gte", TASK_STATE_NEW);
			q.put("state", state);
			q.put("creatorName", u.getName());
			list = dao.queryByPage(page,pageSize,q, null, StaticDao.prop_level_id_asc, null);
		}
		msg.addData("u", u);
		msg.addData("list", list);
		msg.addData("pz", pz);
		msg.addData("p", page);
		msg.addData("sub", subact);
		msg.addData("[jsp]", "/WEB-INF/tc/tasks.jsp");
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
