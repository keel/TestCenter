/**
 * 
 */
package com.k99k.testcenter;

import com.k99k.khunter.Action;
import com.k99k.khunter.ActionMsg;

/**
 * 登录及权限验证Action
 * @author keel
 *
 */
public class Auth extends Action {

	/**
	 * @param name
	 */
	public Auth(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see com.k99k.khunter.Action#act(com.k99k.khunter.ActionMsg)
	 */
	@Override
	public ActionMsg act(ActionMsg msg) {
		//处理登录
		
		//处理注销
		
		return super.act(msg);
	}
	
	public static final boolean checkPermission(int per){
		
		return true;
	}

}
