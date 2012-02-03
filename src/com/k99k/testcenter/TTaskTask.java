/**
 * 
 */
package com.k99k.testcenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.k99k.khunter.Action;
import com.k99k.khunter.ActionMsg;
import com.k99k.khunter.KObject;
import com.k99k.khunter.TaskManager;
import com.k99k.tools.JSON;
import com.k99k.tools.StringUtil;

/**
 * 处理TTask时的异步任务
 * @author keel
 *
 */
public class TTaskTask extends Action {

	/**
	 * @param name
	 */
	public TTaskTask(String name) {
		super(name);
	}
	static final Logger log = Logger.getLogger(TTaskTask.class);

	/* (non-Javadoc)
	 * @see com.k99k.khunter.Action#act(com.k99k.khunter.ActionMsg)
	 */
	@Override
	public ActionMsg act(ActionMsg msg) {
		String act = (String)msg.getData("act");
		if (act.equals("add")) {
			this.add(msg);
		}else if(act.equals("del") || act.equals("drop")){
			this.del(msg);
		}else if(act.equals("appoint")){
			this.appoint(msg);
		}else if(act.equals("send")){
			this.send(msg);
		}else if(act.equals("exec")){
			this.exec(msg);
		}else if(act.equals("confirm")){
			this.confirm(msg);
		}else if(act.equals("finish")){
			this.finish(msg);
		}else if(act.equals("online")){
			this.online(msg);
		}else if(act.equals("back")){
			this.back(msg);
		}
		
		
		
		
		return super.act(msg);
	}
	

	/**
	 * 退回任务创建人
	 * @param msg
	 */
	private void back(ActionMsg msg){
		//long userid = (Long)msg.getData("uid");
		String creator = (String)msg.getData("creator");
		String operator = (String)msg.getData("operator");
		long tid = (Long)msg.getData("tid");
//		int re = (Integer)msg.getData("re");
//		long uid = (Long)msg.getData("uid");
//		if (re == -3) {
//			
//		}
		HashMap<String,Object> query = new HashMap<String, Object>(4);
		query.put("name", operator);
		query.put("unReadTasks", tid);
		HashMap<String,Object> set = new HashMap<String, Object>(4);
		HashMap<String,Object> pull = new HashMap<String, Object>(2);
		pull.put("unReadTasks", tid);
		HashMap<String,Object> inc = new HashMap<String, Object>(2);
		inc.put("newTasks", -1);
		set.put("$pull", pull);
		set.put("$inc", inc);
		TUser.dao.updateOne(query, set);
		//更新待办人
		query = new HashMap<String, Object>(2);
		query.put("name", creator);
		inc.put("newTasks", 1);
		set.remove("$pull");
		set.put("$push", pull);
		set.put("$inc", inc);
		TUser.dao.updateOne(query, set);
	}
	
	/**
	 * 上线后的更新
	 * @param msg
	 */
	private void online(ActionMsg msg){
		//long userid = (Long)msg.getData("uid");
		String operator = (String)msg.getData("operator");
		long tid = (Long)msg.getData("tid");
//		int re = (Integer)msg.getData("re");
		long uid = (Long)msg.getData("uid");
//		if (re == -3) {
//			
//		}
		HashMap<String,Object> query = new HashMap<String, Object>(4);
		query.put("_id", uid);
		query.put("unReadTasks", tid);
		HashMap<String,Object> set = new HashMap<String, Object>(4);
		HashMap<String,Object> pull = new HashMap<String, Object>(2);
		pull.put("unReadTasks", tid);
		HashMap<String,Object> inc = new HashMap<String, Object>(2);
		inc.put("newTasks", -1);
		set.put("$pull", pull);
		set.put("$inc", inc);
		TUser.dao.updateOne(query, set);
		//更新待办人
		query = new HashMap<String, Object>(2);
		query.put("name", operator);
		inc.put("newTasks", 1);
		set.remove("$pull");
		set.put("$push", pull);
		set.put("$inc", inc);
		TUser.dao.updateOne(query, set);
	}
	
	/**
	 * 汇总结果后的更新
	 * @param msg
	 */
	private void finish(ActionMsg msg){
		//long userid = (Long)msg.getData("uid");
		String operator = (String)msg.getData("operator");
		if (!StringUtil.isDigits(msg.getData("re")) || !StringUtil.isStringWithLen(operator, 1)) {
			log.error("finish para error. t_re:"+msg.getData("re")+" operator:"+operator);
			return;
		}
		long tid = (Long)msg.getData("tid");
		int re = (Integer)msg.getData("re");
		long uid = (Long)msg.getData("uid");
		String info = msg.getData("info").toString();
		if (re != -3) {
			//处理已办,更新所有未测的TestUnit所涉及的测试人员,将待办任务去除
			HashMap<String,Object> query = new HashMap<String, Object>(4);
			//query.put("_id", userid);
			query.put("unReadTasks", tid);
			HashMap<String,Object> set = new HashMap<String, Object>(4);
			HashMap<String,Object> pull = new HashMap<String, Object>(2);
			pull.put("unReadTasks", tid);
			HashMap<String,Object> inc = new HashMap<String, Object>(2);
			inc.put("newTasks", -1);
			set.put("$pull", pull);
			set.put("$inc", inc);
			TUser.dao.update(query, set,false,true);
			//更新待办人
			query = new HashMap<String, Object>(2);
			query.put("name", operator);
			inc.put("newTasks", 1);
			set.remove("$pull");
			set.put("$push", pull);
			set.put("$inc", inc);
			TUser.dao.updateOne(query, set);
			
			
		}else{
			HashMap<String,Object> query = new HashMap<String, Object>(4);
			query.put("_id", uid);
			query.put("unReadTasks", tid);
			HashMap<String,Object> set = new HashMap<String, Object>(4);
			HashMap<String,Object> pull = new HashMap<String, Object>(2);
			pull.put("unReadTasks", tid);
			HashMap<String,Object> inc = new HashMap<String, Object>(2);
			inc.put("newTasks", -1);
			set.put("$pull", pull);
			set.put("$inc", inc);
			TUser.dao.updateOne(query, set);
			//更新待办人
			query = new HashMap<String, Object>(2);
			query.put("name", operator);
			inc.put("newTasks", 1);
			set.remove("$pull");
			set.put("$push", pull);
			set.put("$inc", inc);
			TUser.dao.updateOne(query, set);
			
		}
		
		if (re == 9) {
			//发出邮件和短信通知
			KObject task = TTask.dao.findOne(tid);
			HashMap<String,Object> q = new HashMap<String, Object>(2);
			q.put("company", task.getProp("company").toString());
			HashMap<String,Object> f = new HashMap<String, Object>(4);
			f.put("phoneNumber", 1);
			f.put("email", 1);
			ArrayList<HashMap<String,Object>> us = TUser.dao.query(q, f, null, 0, 0, null);
			int len = us.size();
			if (len > 0) {
				String[] dests_phone = new String[len];
				String[] dests_email = new String[len];
				Iterator<HashMap<String,Object>> it = us.iterator();
				int i = 0;
				while (it.hasNext()) {
					HashMap<java.lang.String, java.lang.Object> m = it.next();
					dests_phone[i] = m.get("phoneNumber").toString();
					dests_email[i] = m.get("email").toString();
					i++;
				}
				
				ActionMsg atask = new ActionMsg("sms");
				atask.addData(TaskManager.TASK_TYPE, TaskManager.TASK_TYPE_EXE_SINGLE);
				atask.addData("dests", dests_phone);
				atask.addData("content", info);
				TaskManager.makeNewTask("sms Task:"+tid+System.currentTimeMillis(), atask);
				
				ActionMsg atask1 = new ActionMsg("email");
				atask1.addData(TaskManager.TASK_TYPE, TaskManager.TASK_TYPE_EXE_SINGLE);
				atask1.addData("dests", dests_email);
				atask1.addData("content", info);
				atask1.addData("subject", "[中国电信游戏运营中心]请反馈产品问题:"+task.getName());
				TaskManager.makeNewTask("email Task:"+tid+System.currentTimeMillis(), atask1);
				
			}
			
		}
		
		
	}
	
	/**
	 * 汇总结果后的更新
	 * @param msg
	 */
	private void confirm(ActionMsg msg){
		long userid = (Long)msg.getData("uid");
		String operator = (String)msg.getData("operator");
		long tid = (Long)msg.getData("tid");
		//处理已办
		HashMap<String,Object> query = new HashMap<String, Object>(4);
		query.put("_id", userid);
		query.put("unReadTasks", tid);
		HashMap<String,Object> set = new HashMap<String, Object>(4);
		HashMap<String,Object> pull = new HashMap<String, Object>(2);
		pull.put("unReadTasks", tid);
		HashMap<String,Object> inc = new HashMap<String, Object>(2);
		inc.put("newTasks", -1);
		set.put("$pull", pull);
		set.put("$inc", inc);
		TUser.dao.updateOne(query, set);
		//更新待办人
		query = new HashMap<String, Object>(2);
		query.put("name", operator);
		inc.put("newTasks", 1);
		set.remove("$pull");
		set.put("$push", pull);
		set.put("$inc", inc);
		TUser.dao.updateOne(query, set);
	}
	/**
	 * 处理TestUnit执行完成后的更新
	 * @param msg
	 */
	private void exec(ActionMsg msg){
		long tuid = (Long)msg.getData("tuid");
		String tester = (String)msg.getData("tester");
		
		KObject tu = TestUnit.dao.findOne(tuid);
		if (tu==null) {
			log.error("exec tuid not found. tuid:"+tuid+" tester:"+tester);
			return;
		}
		long tid = Long.parseLong(tu.getProp("TID").toString());
		HashMap<String,Object> q = new HashMap<String, Object>(6);
		q.put("tester", tester);
		q.put("TID", tid);
		q.put("state", 0);
		int size = TestUnit.dao.count(q);
		if (size == 0) {
			//更新tester的待办任务
			q = new HashMap<String, Object>(4);
			HashMap<String,Object> set = new HashMap<String, Object>(4);
			HashMap<String,Object> inc = new HashMap<String, Object>(4);
			inc.put("newTasks", -1);
			set.put("$inc", inc);
			q.put("name", tester);
			q.put("unReadTasks", tid);
			HashMap<String,Object> pull = new HashMap<String, Object>(4);
			pull.put("unReadTasks", tid);
			set.put("$pull", pull);
			TUser.dao.updateOne(q, set);
		}
		
		
	}
	
	/**
	 * 处理TestUnit执行人变更
	 * @param msg
	 */
	@SuppressWarnings("unchecked")
	private void send(ActionMsg msg){
		long tid = (Long)msg.getData("tid");
		ArrayList<HashMap<String,Object>> json = (ArrayList<HashMap<String,Object>>)msg.getData("json");
		HashMap<String,Object> logmsg = (HashMap<String,Object>)msg.getData("logmsg");
		//处理已办
		if (json==null || json.isEmpty()) {
			return;
		}
		
		//需要先清除此任务所涉及的所有测试人员任务
		HashMap<String,Object> query = new HashMap<String, Object>(4);
		query.put("unReadTasks", tid);
		query.put("type", 2);
		HashMap<String,Object> set1 = new HashMap<String, Object>(4);
		HashMap<String,Object> pull1 = new HashMap<String, Object>(2);
		pull1.put("unReadTasks", tid);
		set1.put("$pull", pull1);
		HashMap<String,Object> inc1 = new HashMap<String, Object>(2);
		inc1.put("newTasks", -1);
		set1.put("$inc", inc1);
		boolean re = TUser.dao.update(query, set1, false, true);
		if (!re) {
			log.error("remove testers task failed. task ID:"+tid);
		}
		
		
		//
		Iterator<HashMap<String,Object>> it = json.iterator();
		HashMap<String,Object> q = new HashMap<String, Object>(4);
		HashMap<String,Object> set = new HashMap<String, Object>(4);
		HashMap<String,Object> inc = new HashMap<String, Object>(4);
		inc.put("newTasks", 1);
		set.put("$inc", inc);
		HashMap<String,Object> push = new HashMap<String, Object>(4);
		push.put("unReadTasks", tid);
		set.put("$push", push);
		while (it.hasNext()) {
			HashMap<String,Object> m = it.next();
			String tester = (String)m.get("n");
			q.put("name", tester);
			HashMap<String,Object> ne = new HashMap<String, Object>(2);
			ne.put("$ne", tid);
			q.put("unReadTasks", ne);
			TUser.dao.updateOne(q, set);
		}
		q = new HashMap<String, Object>(2);
		set = new HashMap<String, Object>(4);
		push = new HashMap<String, Object>(4);
		push.put("log", logmsg);
		set.put("$push", push);
		q.put("_id", tid);
		TTask.dao.updateOne(q, set);
	}
	
	private void appoint(ActionMsg msg){
		String userName = (String)msg.getData("uName");
		long operatorId = (Long)msg.getData("oid");
		long tid = (Long)msg.getData("tid");
		//处理已办
		HashMap<String,Object> query = new HashMap<String, Object>(2);
		query.put("name", userName);
		HashMap<String,Object> set = new HashMap<String, Object>(4);
		HashMap<String,Object> pull = new HashMap<String, Object>(2);
		pull.put("unReadTasks", tid);
		HashMap<String,Object> inc = new HashMap<String, Object>(2);
		inc.put("newTasks", -1);
		set.put("$pull", pull);
		set.put("$inc", inc);
		TUser.dao.updateOne(query, set);
		//更新待办人
		query.put("_id", operatorId);
		query.remove("name");
		inc.put("newTasks", 1);
		set.remove("$pull");
		set.put("$push", pull);
		set.put("$inc", inc);
		TUser.dao.updateOne(query, set);
	}
	/**
	 * 删除任务,将此任务涉及的未处理用户清空
	 * @param msg
	 */
	private void del(ActionMsg msg){
		long tid = (Long)msg.getData("taskId");
		HashMap<String,Object> query = new HashMap<String, Object>(4);
		query.put("unReadTasks", tid);
		HashMap<String,Object> set = new HashMap<String, Object>(4);
		HashMap<String,Object> pull = new HashMap<String, Object>(2);
		pull.put("unReadTasks", tid);
		set.put("$pull", pull);
		HashMap<String,Object> inc = new HashMap<String, Object>(2);
		inc.put("newTasks", -1);
		set.put("$inc", inc);
		boolean re = TUser.dao.update(query, set, false, true);
		if (!re) {
			log.error("update all users unread tasks failed. task ID:"+tid);
		}
	}
	
	/**
	 * 新增任务时的异步操作
	 * @param msg
	 */
	@SuppressWarnings("unchecked")
	private void add(ActionMsg msg){
		long tid = (Long)msg.getData("taskId");
		long operatorId = (Long)msg.getData("operatorId");
		long pid = (Long)msg.getData("pid");
		String creatorName = (String)msg.getData("creatorName");
		//operator的未读任务数+1
		HashMap<String,Object> query = new HashMap<String, Object>(4);
		query.put("_id", operatorId);
		HashMap<String,Object> set = new HashMap<String, Object>(4);
		HashMap<String,Object> push = new HashMap<String, Object>(2);
		push.put("unReadTasks", tid);
		set.put("$push", push);
		HashMap<String,Object> inc = new HashMap<String, Object>(2);
		inc.put("newTasks", 1);
		set.put("$inc", inc);
		boolean re = TUser.dao.update(query, set, false, true);
		if (!re) {
			log.error("update operator's unread task failed. task ID:"+tid);
		}
		//处理上传文件
		Object fo = msg.getData("files");
		if (fo != null) {
			try {
				ArrayList<HashMap<String,Object>> files = (ArrayList<HashMap<String, Object>>)fo;
				Iterator<HashMap<String,Object>> it = files.iterator();
				while (it.hasNext()) {
					HashMap<String,Object> f = it.next();
					f.put("PID", pid);
					f.put("TID", tid);
					f.put("state", 0);
					f.put("creatorName", creatorName);
					KObject kobj = new KObject();
					if(GameFile.schema.setPropFromMapForCreate(f, kobj) && GameFile.dao.add(kobj)){
					}else{
						log.error("GameFile add failed:"+JSON.write(f));
					}
				}
			} catch (Exception e) {
				log.error("deal upload files failed. task ID:"+tid);
			}
		}
		//如果非首次创建的产品(type==2)，更新待反馈状态
		int type = StringUtil.isDigits(msg.getData("tType"))?Integer.parseInt(msg.getData("tType").toString()):0;
		if (type == 2 && pid>0) {
			//先找到之前待反馈任务
			query = new HashMap<String, Object>(4);
			set = new HashMap<String, Object>(4);
			query.put("PID", pid);
			query.put("state", TTask.TASK_STATE_NEED_MOD);
			ArrayList<KObject> ts = TTask.dao.queryKObj(query, null, null, 0, 0, null);
			Iterator<KObject> it = ts.iterator();
			HashMap<String, Object> q = new HashMap<String, Object>(2);
			while (it.hasNext()) {
				KObject ta = it.next();
				HashMap<String,Object> updateTask = new HashMap<String, Object>(2);
				updateTask.put("state", TTask.TASK_STATE_BACKED);
				set.put("$set", updateTask);
				q.put("_id", ta.getId());
				TTask.dao.update(query, set, false, true);
				//消除待办人
				query = new HashMap<String, Object>(4);
				query.put("unReadTasks", ta.getId());
				HashMap<String,Object> pull = new HashMap<String, Object>(2);
				pull.put("unReadTasks", ta.getId());
				set.put("$pull", pull);
				inc = new HashMap<String, Object>(2);
				inc.put("newTasks", -1);
				set.put("$inc", inc);
				TUser.dao.update(query, set, false, true);
			}
		}
	}
}
