/**
 * 
 */
package com.k99k.testcenter;

import com.k99k.khunter.Action;
import com.k99k.khunter.ActionMsg;

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

	/* (non-Javadoc)
	 * @see com.k99k.khunter.Action#act(com.k99k.khunter.ActionMsg)
	 */
	@Override
	public ActionMsg act(ActionMsg msg) {
		long ggId = (Long)msg.getData("newsId");
		//所有用户的未读公告数+1
		
		
		return super.act(msg);
	}
	
	

}
