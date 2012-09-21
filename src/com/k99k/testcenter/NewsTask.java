/**
 * 
 */
package com.k99k.testcenter;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.k99k.khunter.Action;
import com.k99k.khunter.ActionMsg;
import com.k99k.tools.StringUtil;

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
		
		//短信通知
		if (StringUtil.isStringWithLen(msg.getData("notice"), 1)) {
			
		}
		
		//邮件通知
		
		
		
		return super.act(msg);
	}
	
	

}
