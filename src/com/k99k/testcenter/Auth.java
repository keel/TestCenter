/**
 * 
 */
package com.k99k.testcenter;

import java.util.HashMap;
import com.k99k.khunter.Action;
import com.k99k.khunter.ActionMsg;
import com.k99k.khunter.HttpActionMsg;
import com.k99k.khunter.KFilter;
import com.k99k.khunter.KObject;
import com.k99k.khunter.WebTool;
import com.k99k.khunter.dao.StaticDao;
import com.k99k.tools.JSON;
import com.k99k.tools.encrypter.Base64Coder;
import com.k99k.khunter.JOut;

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
	
	private static final HashMap<String,Permission> perMap = new HashMap<String, Permission>();
	
	/* (non-Javadoc)
	 * @see com.k99k.khunter.Action#act(com.k99k.khunter.ActionMsg)
	 */
	@Override
	public ActionMsg act(ActionMsg msg) {
		HttpActionMsg httpmsg = (HttpActionMsg)msg;
		String subact = KFilter.actPath(msg, 2, "");
		//处理登录
		if (subact.equals("login")) {
			//cookie
			String uName = httpmsg.getHttpReq().getParameter("uName");
			String uPwd = httpmsg.getHttpReq().getParameter("uPwd");
			KObject user = StaticDao.checkUser(uName, uPwd);
			if (user != null) {
				//是否保存登录状态
				String cookie = httpmsg.getHttpReq().getParameter("saveLogin");
				if (cookie != null && cookie.equals("true")) {
					WebTool.setCookie("tcu", Base64Coder.encodeString(uName+":"+uPwd+":"+System.currentTimeMillis()), httpmsg.getHttpResp());
				}else{
					//默认20分钟
					WebTool.setCookie("tcu", Base64Coder.encodeString(uName+":"+uPwd+":"+System.currentTimeMillis()),60*20, httpmsg.getHttpResp());
				}
				//返回ok
				JOut.txtOut("ok", httpmsg);
			}else{
				JOut.err(401, httpmsg);
			}
			return super.act(msg);
		}
		//处理注销
		else if(subact.equals("logout")){
			WebTool.removeCookie("tcu", httpmsg.getHttpResp());
			msg.addData("[redirect]", "/login");
			return super.act(msg);
		}
		//其他就根据cookie决定是否显示login.jsp
		else{
			KObject u = checkCookieLogin(httpmsg);
			if (u != null) {
				msg.addData("u", u);
				msg.addData("[redirect]", "/news");
			}else{
				msg.addData("[jsp]", "/WEB-INF/tc/login.jsp");
			}
			return super.act(msg);
		}
	}
	
	/**
	 * 验证cookie是否登录
	 * @param httpmsg
	 * @return User(KObject),失败返回null
	 */
	public static final KObject checkCookieLogin(HttpActionMsg httpmsg){
		String coStr = WebTool.getCookieValue(httpmsg.getHttpReq().getCookies(),"tcu","");
		if (!coStr.equals("")) {
			String[] u_p = Base64Coder.decodeString(coStr).split(":");
			if (u_p.length >= 2) {
				return StaticDao.checkUser(u_p[0], u_p[1]);
			}
		}
		return null;
	}
	
	public static final boolean checkPermission(String act,int userType,int userLevel){
		Permission p = perMap.get(act);
		return p.check(userType, userLevel);
	}
	
	public static final void regPermission(String act,int[] userType,int[] userLevel){
		Permission p = new Permission(act, userType, userLevel);
		perMap.put(act, p);
	}
	
	@SuppressWarnings("unchecked")
	public static final HashMap<String,Object> list(){
		return (HashMap<String, Object>) perMap.clone();
	}
	
	static class Permission{
		HashMap<Integer,Integer> levels = new HashMap<Integer, Integer>();
		HashMap<Integer,Integer> types = new HashMap<Integer, Integer>();
		String act;
		Permission(String act,int[] userType,int[] userLevel){
			this.act = act;
			for (int i = 0; i < userLevel.length; i++) {
				this.levels.put(userLevel[i], 1);
			}
			for (int i = 0; i < userType.length; i++) {
				this.types.put(userType[i], 1);
			}
		}
		
		public boolean check(int userType,int userLevel){
			if (userType == 99) {
				//超级管理员
				return true;
			}
			//符合指定权限等级
			if (this.levels.containsKey(userLevel)) {
				//符合指定用户类型
				if (this.types.containsKey(userType)) {
					return true;
				}
			}
			return false;
		}
		
		@Override
		public String toString() {
			HashMap<String,Object> m = new HashMap<String, Object>();
			m.put("act", this.act);
			m.put("levels", this.levels);
			m.put("types", this.types);
			return JSON.write(m);
		}
	}
	
	
	
	/* (non-Javadoc)
	 * @see com.k99k.khunter.Action#init()
	 */
	@Override
	public void init() {
		super.init();
	}

	public static void main(String[] args) {
		int[] ty = new int[]{0,1,3,5};
		int[] le = new int[]{0,1,2};
		int[] ty2 = new int[]{6,7};
		int[] le2 = new int[]{0,1,2,3};
		String act1 = "login";
		String act2 = "reg";
		Auth.regPermission(act1,ty, le);
		Auth.regPermission(act2,ty2, le2);
		System.out.println(Auth.checkPermission(act1, 1, 1));
		System.out.println(Auth.checkPermission(act2, 1, 1));
		
	}

}
