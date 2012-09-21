/**
 * 
 */
package com.k99k.testcenter;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.k99k.khunter.Action;
import com.k99k.khunter.ActionMsg;
import com.k99k.khunter.TaskManager;
import com.k99k.tools.StringUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * 公告发布的任务
 * @author keel
 *
 */
public class NewsTask extends Action {

	/**
	 * @param name
	 */
	public NewsTask(String name) {
		super(name);
	}
	static final Logger log = Logger.getLogger(NewsTask.class);

	/* (non-Javadoc)
	 * @see com.k99k.khunter.Action#act(com.k99k.khunter.ActionMsg)
	 */
	@Override
	public ActionMsg act(ActionMsg msg) {
		long ggId = (Long)msg.getData("newsId");
		//所有用户的未读公告数+1
		HashMap<String,Object> query = new HashMap<String, Object>(4);
		query.put("state", 0);
		HashMap<String,Object> set = new HashMap<String, Object>(4);
		HashMap<String,Object> push = new HashMap<String, Object>(2);
		push.put("unReadNews", ggId);
		set.put("$push", push);
		HashMap<String,Object> inc = new HashMap<String, Object>(2);
		inc.put("newNews", 1);
		set.put("$inc", inc);
		boolean re = TUser.dao.update(query, set, false, true);
		if (!re) {
			log.error("update all users unread news failed. news ID:"+ggId);
		}
		
		if (StringUtil.isDigits(msg.getData("notice"))) {
			int way = Integer.parseInt(String.valueOf(msg.getData("notice")));
			String smsContent = String.valueOf(msg.getData("notice_sms"));
			String emailContent = String.valueOf(msg.getData("notice_email"));
			String emailSubject = String.valueOf(msg.getData("notice_subject"));
			int type = Integer.parseInt(String.valueOf(msg.getData("userType")));
			
			DBCursor cur = TUser.dao.getColl().find(new BasicDBObject("state",0).append("type", type));
			
			while (cur.hasNext()) {
				DBObject c = cur.next();
				String phone = (String) c.get("phoneNumber");
				String email = (String) c.get("email");
				//短信通知
				if (way % 2 == 0) {
					ActionMsg atask = new ActionMsg("sms");
					atask.addData(TaskManager.TASK_TYPE, TaskManager.TASK_TYPE_EXE_SINGLE);
					atask.addData("dests", new String[]{phone});
					atask.addData("content", smsContent);
					TaskManager.makeNewTask("sms Task-newsNotice:"+phone+System.currentTimeMillis(), atask);
				}
				//邮件通知
				if (way % 3 == 0) {
					ActionMsg atask1 = new ActionMsg("email");
					atask1.addData(TaskManager.TASK_TYPE, TaskManager.TASK_TYPE_EXE_SINGLE);
					atask1.addData("dests", new String[]{email});
					atask1.addData("content", emailContent);
					atask1.addData("subject", emailSubject);
					TaskManager.makeNewTask("email Task-newsNotice:"+email+System.currentTimeMillis(), atask1);
				}
				
			}
			
			
		}
		
		return super.act(msg);
	}
	
	

}
