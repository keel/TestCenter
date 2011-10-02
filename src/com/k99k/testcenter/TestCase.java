/**
 * 
 */
package com.k99k.testcenter;

import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import com.k99k.khunter.Action;
import com.k99k.khunter.ActionMsg;
import com.k99k.khunter.DaoInterface;
import com.k99k.khunter.DaoManager;
import com.k99k.khunter.HttpActionMsg;
import com.k99k.khunter.KFilter;
import com.k99k.khunter.KObjManager;
import com.k99k.khunter.KObjSchema;
import com.k99k.khunter.KObject;
import com.k99k.khunter.dao.StaticDao;
import com.k99k.tools.JSON;
import com.k99k.tools.StringUtil;

/**
 * 测试项目
 * @author keel
 *
 */
public class TestCase extends Action {

	/**
	 * @param name
	 */
	public TestCase(String name) {
		super(name);
	}
	
	static DaoInterface dao;
	static KObjSchema schema;
	
	/**
	 * 测试项缓存,长度为50,下标为系统数字：0:java,1:android,2:WAP,3:brew,4:mobile,5:ce,6:other
	 */
	@SuppressWarnings("unchecked")
	private ArrayList[] cases = new ArrayList[50];
	
	/**
	 * 初始化所有的TestCase并缓存起来
	 * @see com.k99k.khunter.Action#init()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void init() {
		dao = DaoManager.findDao("TCTestCaseDao");
		schema = KObjManager.findSchema("TCTestCase");
		
		ArrayList<KObject> cl = dao.queryKObj(StaticDao.prop_state_0, null,StaticDao.prop_id , 0, 0, null);
		Iterator<KObject> it = cl.iterator();
		while (it.hasNext()) {
			KObject kobj = it.next();
			//type表示系统：0:java,1:android,2:WAP,3:brew,4:mobile,5:ce,6:other
			int type = Integer.parseInt(kobj.getType());
			if (cases[type] == null) {
				cases[type] = new ArrayList<KObject>(30);
			}
			cases[type].add(kobj);
		}
		super.init();
	}

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
		if (StringUtil.isDigits(subact)) {
			this.list(subact, req, u, httpmsg);
		}
		
		return super.act(msg);
	}

	/* (non-Javadoc)
	 * @see com.k99k.khunter.Action#exit()
	 */
	@Override
	public void exit() {
		cases = new ArrayList[50];
		super.exit();
	}

	/**
	 * 根据系统查看TestCase列表,返回json形式的列表
	 * @param msg
	 */
	private void list(String subact,HttpServletRequest req,KObject u,HttpActionMsg msg){
		int sys = Integer.parseInt(subact);
		if (cases[sys] == null) {
			msg.addData("[print]", "");
			return;
		}
		String re = JSON.write(cases[sys]);
		msg.addData("[print]", re);
	}
	
	
	
	
}
