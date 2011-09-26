/**
 * 
 */
package com.k99k.testcenter;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.k99k.khunter.Action;
import com.k99k.khunter.ActionMsg;

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
		long ggId = (Long)msg.getData("taskId");
		long operatorId = (Long)msg.getData("operatorId");
		//operator的未读任务数+1
		HashMap<String,Object> query = new HashMap<String, Object>(4);
		query.put("_id", operatorId);
		HashMap<String,Object> set = new HashMap<String, Object>(4);
		HashMap<String,Object> push = new HashMap<String, Object>(2);
		push.put("unReadTasks", ggId);
		set.put("$push", push);
		HashMap<String,Object> inc = new HashMap<String, Object>(2);
		inc.put("newTasks", 1);
		set.put("$inc", inc);
		boolean re = TUser.dao.update(query, set, false, true);
		if (!re) {
			log.error("update operator's unread task failed. task ID:"+ggId);
		}
		return super.act(msg);
	}
}
