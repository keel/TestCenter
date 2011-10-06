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
import com.k99k.tools.JSON;

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
		}else if(act.equals("del")){
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
		}
		
		
		
		
		return super.act(msg);
	}
	
	/**
	 * 汇总结果后的更新
	 * @param msg
	 */
	private void finish(ActionMsg msg){
		//long userid = (Long)msg.getData("uid");
		String operator = (String)msg.getData("operator");
		long tid = (Long)msg.getData("tid");
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
		//发出邮件和短信通知
		
		
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
		long userid = (Long)msg.getData("uid");
		long operatorId = (Long)msg.getData("oid");
		long tid = (Long)msg.getData("tid");
		//处理已办
		HashMap<String,Object> query = new HashMap<String, Object>(2);
		query.put("_id", userid);
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
			log.error("Del task. - update all users unread tasks failed. task ID:"+tid);
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
	}
}
