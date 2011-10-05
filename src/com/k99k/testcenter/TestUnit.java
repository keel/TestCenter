/**
 * 
 */
package com.k99k.testcenter;

import javax.servlet.http.HttpServletRequest;

import com.k99k.khunter.Action;
import com.k99k.khunter.ActionMsg;
import com.k99k.khunter.DaoInterface;
import com.k99k.khunter.DaoManager;
import com.k99k.khunter.HttpActionMsg;
import com.k99k.khunter.JOut;
import com.k99k.khunter.KFilter;
import com.k99k.khunter.KObjManager;
import com.k99k.khunter.KObjSchema;
import com.k99k.khunter.KObject;
import com.k99k.tools.StringUtil;

/**
 * @author keel
 *
 */
public class TestUnit extends Action {

	/**
	 * @param name
	 */
	public TestUnit(String name) {
		super(name);
	}
	static DaoInterface dao;
	static KObjSchema schema;
	
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
			this.one(subact, req, u, httpmsg);
		}else if(subact.equals("exec")){
			this.exec(req, u, httpmsg);
		}else if(subact.equals("cancel")){
			this.cancel(req, u, httpmsg);
		}
		return super.act(msg);
	}
	/* (non-Javadoc)
	 * @see com.k99k.khunter.Action#init()
	 */
	@Override
	public void init() {
		dao = DaoManager.findDao("TCTestUnitDao");
		schema = KObjManager.findSchema("TCTestUnit");
		super.init();
	}
	
	/**
	 * 查看某个TestUnit
	 * @param msg
	 */
	private void one(String subact,HttpServletRequest req,KObject u,HttpActionMsg msg){
		long id = Long.parseLong(subact);
		//TaskUnit
		KObject one = dao.findOne(id);
		if (one== null || one.getState() == -1) {
			JOut.err(404, msg);
			return;
		}
		long tid = (Long)one.getProp("TID");
		KObject task = TTask.dao.findOne(tid);
		KObject product = Product.dao.findOne((Long)one.getProp("PID"));
		msg.addData("u", u);
		msg.addData("one", one);
		msg.addData("product", product);
		msg.addData("task", task);
		msg.addData("cases", TestCase.findCaseList(Integer.parseInt(product.getProp("sys").toString())));
		msg.addData("[jsp]", "/WEB-INF/tc/testunit.jsp");
	}
	
	/**
	 * 处理TestUnit结果
	 * @param req
	 * @param u
	 * @param msg
	 */
	private void exec(HttpServletRequest req,KObject u,HttpActionMsg msg){
		
	}
	
	/**
	 * 取消某个TestUnit
	 * @param req
	 * @param u
	 * @param msg
	 */
	private void cancel(HttpServletRequest req,KObject u,HttpActionMsg msg){
		
	}
	
}
