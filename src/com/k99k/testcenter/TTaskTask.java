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
		}
		
		
		
		return super.act(msg);
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
					if(GameFile.schema.setPropFromMapForCreate(f, kobj)){
						GameFile.dao.add(kobj);
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
