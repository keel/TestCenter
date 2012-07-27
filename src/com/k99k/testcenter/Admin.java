/**
 * 
 */
package com.k99k.testcenter;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import com.k99k.khunter.Action;
import com.k99k.khunter.ActionMsg;
import com.k99k.khunter.HttpActionMsg;
import com.k99k.khunter.JOut;
import com.k99k.khunter.KFilter;
import com.k99k.khunter.KObject;
import com.k99k.khunter.dao.StaticDao;

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
		if (subact.equals("user")) {
			//用户管理
			this.user(subact, subsub, req, u, httpmsg);
			
		}else{
			JOut.err(404, httpmsg);
		}
		
		
		return super.act(msg);
	}
	
	
	
	private void user(String subact,String tag,HttpServletRequest req,KObject u,HttpActionMsg msg){
		//搜索
		
		//修改
		
		//新增
		
		
		HashMap<String,Object> q = StaticDao.prop_topic_doc;
		if (!tag.equals("")) {
			q.put("tags", tag);
		}
//		this.queryPage(q,subact, req, u, msg);
		msg.addData("tag", tag);
		msg.addData("title", "文档-"+tag);
	}
	

}
