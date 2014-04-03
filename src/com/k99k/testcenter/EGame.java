/**
 * 
 */
package com.k99k.testcenter;

import it.sauronsoftware.ftp4j.FTPClient;

import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import com.k99k.khunter.Action;
import com.k99k.khunter.ActionMsg;
import com.k99k.khunter.HttpActionMsg;
import com.k99k.khunter.JOut;
import com.k99k.khunter.KFilter;
import com.k99k.khunter.KObject;
import com.k99k.khunter.dao.StaticDao;
import com.k99k.tools.JSON;
import com.k99k.tools.Net;
import com.k99k.tools.StringUtil;
import com.k99k.tools.encrypter.Encrypter;

/**
 * 与爱游戏平台的接口
 * @author keel
 *
 */
public class EGame extends Action {

	/**
	 * @param name
	 */
	public EGame(String name) {
		super(name);
	}
	static final Logger log = Logger.getLogger(EGame.class);
	
	/**
	 * 公司接口URL
	 */
	private static  String companyUrl;
	
	/**
	 * 产品接口URL
	 */
	private static  String productUrl;
	
//	/**
//	 * 终端接口URL
//	 */
//	private static String handsetUrl;
	
	/**
	 * 短代信息URL接口
	 */
	private static String feeUrl;

	/**
	 * 登录状态保持时间,默认40分钟
	 */
	private static final long cookieTime = 40*60*1000;
	
	
	
	/* (non-Javadoc)
	 * @see com.k99k.khunter.Action#act(com.k99k.khunter.ActionMsg)
	 */
	@Override
	public ActionMsg act(ActionMsg msg) {
		HttpActionMsg httpmsg = (HttpActionMsg)msg;
		HttpServletRequest req = httpmsg.getHttpReq();
		//限IP
		/*
		if (!req.getRemoteAddr().equals("202.102.xxx.xxx")) {
			JOut.err(401,"E401"+Err.ERR_IP, httpmsg);
			return super.act(msg);
		}*/
		String subact = KFilter.actPath(msg, 2, "");
		//判断用户登录
		//Auth.logout(httpmsg, httpmsg.getHttpResp());
		KObject u = checkLogin(httpmsg);
		if (u == null) {
			return super.act(msg);
		}
		//新建任务
		if (subact.equals("newtask")) {
			
			newtask(req,u, httpmsg,false);
		}else if (subact.equals("task")) {
			this.task(req,u, httpmsg);
		}else if (subact.equals("test")) {
			this.test(req,u, httpmsg);
		}else{
			//转到测试首页
			this.toTC(req,u, httpmsg);
		}
		return super.act(msg);
	}
	
	private void test(HttpServletRequest req,KObject user,HttpActionMsg msg){
		String dir = "/"+req.getParameter("te");
		FTPClient fc = new FTPClient();
		try {
			fc.connect("202.102.39.14");
			fc.login("shitibao", "shitibao123");
			
			String[] ds = dir.split("/");
			for (int i = 0; i < ds.length; i++) {
				if (ds[i].length()>0 && !fc.currentDirectory().equals(ds[i])) {
					try {
						fc.createDirectory(ds[i]);
					} catch (Exception e) {
					}
					fc.changeDirectory(ds[i]);
				}
			}
			fc.disconnect(true);
			msg.addData("[print]", "create OK!"+dir);
		} catch (Exception e) {
			log.error("fc error.", e);
			msg.addData("[print]", "create failed!"+dir);
			return;
		}
	}
	
	
	/**
	 * 判断登录状态,成功则返回TUser,失败则返回 null
	 * @param httpmsg
	 * @return
	 */
	private KObject checkLogin(HttpActionMsg httpmsg){
		//KObject u = Auth.checkCookieLogin(httpmsg);
		KObject u = null;
		//if (u == null) {
			//验证参数
			if (!StringUtil.isStringWithLen(httpmsg.getHttpReq().getParameter("t"), 5)) {
				JOut.err(403,"E403"+Err.ERR_PARAS, httpmsg);
				return null;
			}
			//验证登录参数
			String enc = httpmsg.getHttpReq().getParameter("t").trim();
			String t = Encrypter.decrypt(enc);
			if (!StringUtil.isStringWithLen(t, 5)) {
				JOut.err(403,"E403"+Err.ERR_EGAME_DECODE, httpmsg);
				return null;
			}
			String[] tt = t.split("#");
			if (tt.length>=2 && StringUtil.isDigits(tt[1]) ) {
				//long userId = Long.parseLong(tt[0]);
				long loginTime = Long.parseLong(tt[1]);
				//判断登录时间是否已超时
				if (System.currentTimeMillis()-loginTime <= cookieTime) {
					//使用CPID
					u = TUser.dao.findOne(tt[0]);
					if (u == null) {
						//未同步时直接同步
						if (StaticDao.syncCompany(tt[0])) {
							u = TUser.dao.findOne(tt[0]);
						}
					}
					if (u != null) {
						//如果带产品id,则加到user的属性中,注意后期处理时要清除
						if (tt.length == 3 && StringUtil.isDigits(tt[2])) {
							u.setProp("pid", tt[2]);
						}
						Auth.setLoginState(tt[0], "egame",loginTime,Integer.parseInt(String.valueOf(cookieTime)),httpmsg.getHttpResp());
						return u;
					}
				}else{
					//FIXME 可跳转到爱游戏登录页
					JOut.err(401,"E401"+Err.ERR_EGAME_LOGIN_OUTOFTIME,httpmsg);
					return null;
				}
				
			}
			JOut.err(401,"E401"+Err.ERR_EGAME_USER_NOT_FOUND,httpmsg);
			return null;
		//}
		//return u;
	}
	
	/**
	 * 转到具体任务页,即转到该产品ID的最新任务
	 * @param req
	 * @param user
	 * @param msg
	 */
	private void task(HttpServletRequest req,KObject user,HttpActionMsg msg){
		long pid = parsePID(req,user,msg);
		if (pid == 0) {
			return;
		}
		HashMap<String,Object> query = new HashMap<String, Object>(2);
		query.put("PID", pid);
		HashMap<String,Object> sort = new HashMap<String, Object>(2);
		sort.put("_id", -1);
		HashMap<String,Object> column = new HashMap<String, Object>(2);
		column.put("_id", 1);
		
		ArrayList<HashMap<String,Object>> re = TTask.dao.query(query, column, sort, 0, 1, null);
		if (re == null || re.size() ==0) {
			JOut.err(403,"E403"+Err.ERR_EGAME_T_NOT_FOUND, msg);
			return;
		} 
		long tid = (Long)(re.get(0).get("_id"));
		msg.removeData("[print]");
		msg.removeData("[jsp]");
		msg.addData("[redirect]", "/tasks/"+tid);
	}
	
	
	/**
	 * 创建新测试任务
	 * @param req
	 * @param user
	 * @param msg
	 * @param isReply 是否是再次提交
	 */
	static void newtask(HttpServletRequest req,KObject user,HttpActionMsg msg,boolean isReply){
		if (user.getType() < 1) {
			//权限不够
			JOut.err(401, msg);
			return;
		}
		long pid = 0;
		if (!isReply) {
			pid = parsePID(req,user,msg);
			if (pid == 0) {
				return;
			}
		}else{
			String opid = req.getParameter("pid");
			if (!StringUtil.isDigits(opid)) {
				JOut.err(403, msg);
				return;
			}
			pid = Long.parseLong(opid);
		}
		
		if (!isReply) {
			//先确定此产品有无在测试平台提交任务,如果有则直接转到查看
			HashMap<String,Object> query = new HashMap<String, Object>(4);
			query.put("PID", pid);
			HashMap<String,Object> gte = new HashMap<String, Object>(2);
			gte.put("$gte", 0);
			query.put("state", gte);
			ArrayList<HashMap<String,Object>> re = TTask.dao.query(query, StaticDao.prop_id, StaticDao.prop_id_desc, 0, 1, null);
			if (re != null && re.size() >0) {
				long tid = (Long)(re.get(0).get("_id"));
				msg.removeData("[print]");
				msg.removeData("[jsp]");
				msg.addData("[redirect]", "/tasks/"+tid);
				return;
			}
		}
		
		
		//--------------------------------
		//创建产品
		//--------------------------------
		
		//直接从接口获取产品
		HashMap<String,String> pmap = getProduct(pid);
		if (pmap == null) {
			//接口获取失败
			JOut.err(500,"E500"+Err.ERR_EGAME_PRODUCT,msg);
			return;
		}
		//加入真正的公司名称
		pmap.put("cpName", Company.egameIds.get(pmap.get("venderCode").toString()));
		//默认为此产品的首次测试,添加操作时会进行判断
		//boolean isOld = Product.dao.checkId(pid);
		pmap.put("task_type", "0");
		if (String.valueOf(pmap.get("payType")).equals("2")) {
			//获取短代信息
			ArrayList<HashMap<String,String>> fee = getFee(pid);
			if (fee != null) {
				msg.addData("fee", fee);
			}
		}
		
		//查找目录产品中已存在的测试通过的实体包
		HashMap<String,Object> query = new HashMap<String, Object>(4);
		query.put("PID", pid);
		query.put("state", 1);
		ArrayList<KObject> re = GameFile.dao.queryKObj(query, null, StaticDao.prop_id, 0, 0, null);
		if (re != null && re.size() >0) {
			msg.addData("files", re);
		}
		
		//数据库中无此产品时使用pmap
		msg.addData("pmap", pmap);
		
		/*先从数据库查找的方式
		KObject p = Product.dao.findOne(pid);
		if (p == null) {
			HashMap<String,String> pmap = getProduct(pid);
			if (pmap == null) {
				JOut.err(403,"E403"+Err.ERR_EGAME_PRODUCT,msg);
				return;
			}
			if (pmap.get("payType").equals("根据关卡或道具计费")) {
				//获取短代信息
				ArrayList<HashMap<String,String>> fee = getFee(pid);
				msg.addData("fee", fee);
			}
			//数据库中无此产品时使用pmap
			msg.addData("pmap", pmap);
		}else{
			//数据库中能找到产品时用product
			msg.addData("product", p);
		}*/
		msg.addData("u", user);
		msg.addData("[jsp]", "/WEB-INF/tc/task_add.jsp");
	}
	
	/**
	 * 获取PID
	 * @param req
	 * @param user
	 * @param msg
	 * @return
	 */
	private static long parsePID(HttpServletRequest req,KObject user,HttpActionMsg msg){
		Object pidobj = user.getProp("pid");
		long pid = 0;
		if (!StringUtil.isDigits(pidobj)) {
			//从参数解出pid
			String enc = msg.getHttpReq().getParameter("t").trim();
			String t = Encrypter.decrypt(enc);
			if (!StringUtil.isStringWithLen(t, 5)) {
				JOut.err(403,"E403"+Err.ERR_EGAME_DECODE, msg);
				return 0;
			}
			String[] tt = t.split("#");
			if (tt.length == 3 && StringUtil.isDigits(tt[2])) {
				//pidobj = tt[2];
				pid = Long.parseLong(tt[2]);
			}else{
				JOut.err(403,"E403"+Err.ERR_EGAME_T_ERR,msg);
				return 0;
			}
		}else{
			pid = Long.parseLong(user.removeProp("pid").toString());
		}
		return pid;
	}
	
	/**
	 * 跳转到测试首页，即公告页
	 * @param req
	 * @param user
	 * @param msg
	 */
	private void toTC(HttpServletRequest req,KObject user,HttpActionMsg msg){
		String p_str = req.getParameter("p");
		String pz_str = req.getParameter("pz");
		int page = StringUtil.isDigits(p_str)?Integer.parseInt(p_str):1;
		int pz = StringUtil.isDigits(pz_str)?Integer.parseInt(pz_str):News.getPageSize();
		ArrayList<KObject> list = StaticDao.loadNews(page, pz);
		msg.addData("u", user);
		msg.addData("list", list);
		msg.addData("pz", pz);
		msg.addData("p", page);
		msg.addData("[jsp]", "/WEB-INF/tc/news.jsp");
	}
	

	/**
	 * 获取CP信息
	 * @param cpid
	 * @return HashMap形式的company json
	 */
	public static final HashMap<String,String> getCompany(String cpid){
		String url = companyUrl+"&cp_id="+cpid;
		return getUrlJson(url);
	}
	
	/**
	 * 获取产品信息
	 * @param id
	 * @return HashMap形式的json
	 */
	public static final HashMap<String,String> getProduct(long pid){
		String url = productUrl+"&app_id="+pid;
		return getUrlJson(url);
	}
//	/**
//	 * 获取终端信息
//	 * @param mode
//	 * @return HashMap形式的json
//	 */
//	public static final HashMap<String,String> getHandset(String mode){
//		String url = handsetUrl+"&model="+mode;
//		return getUrlJson(url);
//	}
	
	/**
	 * 获取短代信息
	 * @param id
	 * @return HashMap形式的json
	 */
	@SuppressWarnings("unchecked")
	public static final ArrayList<HashMap<String,String>> getFee(long pid){
		String url = feeUrl+"&app_id="+pid;
		String re = Net.getUrlContent(url, 3000, false, "utf-8");
		if (re.equals("")) {
			return null;
		}
		//替换掉\"号
		re = re.replaceAll("\\\\\\\"", "“").replaceAll("\\\\", ",");
		Object j = JSON.read(re);
		try {
			if (j instanceof HashMap) {
				HashMap<String,Object> json = (HashMap<String,Object>)j;
				if (json.containsKey("data")) {
					ArrayList<HashMap<String,String>> list = (ArrayList<HashMap<String,String>>) json.get("data");
					if (list!=null && list.size()>=1) {
						return list;
					}
				}
			}
		} catch (Exception e) {
			log.error("Egame.getUrlJson failed!url:"+url, e);
			return null;
		}
		return null;
	}
	
	/**
	 * 获取指定url并解析json,返回目的json,3秒超时
	 * @param url
	 * @return HashMap<String,String> 目的json,失败返回null
	 */
	@SuppressWarnings("unchecked")
	private final static HashMap<String,String> getUrlJson(String url){
//		System.out.println("egameURL:"+url);
		String re = Net.getUrlContent(url, 3000, false, "utf-8");
		if (re.equals("")) {
			return null;
		}
		Object j = JSON.read(re);
		try {
			if (j instanceof HashMap) {
				HashMap<String,Object> json = (HashMap<String,Object>)j;
				if (json.containsKey("data")) {
					ArrayList<Object> list = (ArrayList<Object>) json.get("data");
					if (list!=null && list.size()>=1) {
						HashMap<String,String> comJson = (HashMap<String, String>) list.get(0);
						if (comJson != null) {
							return comJson;
						}
					}
				}
			}
		} catch (Exception e) {
			log.error("Egame.getUrlJson failed!url:"+url, e);
			return null;
		}
		return null;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
//		EGame.companyUrl = "http://202.102.39.9:81/MIS/v/entitytest/cps?startIndex=0&pageSize=1";
//		EGame.productUrl = "http://202.102.39.9:81/MIS/v/entitytest/products?startIndex=0&pageSize=1";
//		EGame.handsetUrl = "http://202.102.39.9:81/MIS/v/entitytest/models?startIndex=0&pageSize=1";
//		EGame.feeUrl = "http://202.102.39.9:81/MIS/v/entitytest/consumecodes";
		/*
		EGame.companyUrl = "http://202.102.39.18:9087/Business/entitytest/cps.do?a=1";
		EGame.productUrl = "http://202.102.39.18:9087/Business/entitytest/products.do?startIndex=0&pageSize=10";
		EGame.handsetUrl = "http://202.102.39.18:9087/Business/entitytest/models.do?a=1";
		EGame.feeUrl = "http://202.102.39.18:9087/Business/entitytest/consumecodes.do";
		
		
//		String comId = "C11040";
//		long pid = 228503;
//		String mod = "E63V";
		long feePid = 235540;
//		
//		HashMap<String,String> re = null;
//		
//		re = getCompany(comId);
//		System.out.println(JSON.writeFormat(re));
//		re = getProduct(pid);
//		System.out.println(JSON.writeFormat(re));
//		re = getHandset(mod);
//		System.out.println(JSON.writeFormat(re));
		ArrayList<HashMap<String,String>> li = getFee(feePid);
		System.out.println(JSON.write(li));
		*/
		String s = "DaFNvluWBlNX563y0hhtdUramrk2eMEUcPtlohMALjU*";
		s = "22F2qoO_uSdtUU71ae2AUwQazjfI5twiP1QhxQ1H_KY*";
		s= Encrypter.decrypt(s);
		System.out.println(s);
	}

	/**
	 * @return the companyUrl
	 */
	public static final String getCompanyUrl() {
		return companyUrl;
	}

	/**
	 * @param companyUrl the companyUrl to set
	 */
	public static final void setCompanyUrl(String url) {
		companyUrl = url;
	}

	/**
	 * @return the productUrl
	 */
	public static final String getProductUrl() {
		return productUrl;
	}

	/**
	 * @param productUrl the productUrl to set
	 */
	public static final void setProductUrl(String url) {
		productUrl = url;
	}

//	/**
//	 * @return the handsetUrl
//	 */
//	public static final String getHandsetUrl() {
//		return handsetUrl;
//	}
//
//	/**
//	 * @param handsetUrl the handsetUrl to set
//	 */
//	public static final void setHandsetUrl(String url) {
//		handsetUrl = url;
//	}

	/**
	 * @return the feeUrl
	 */
	public static final String getFeeUrl() {
		return feeUrl;
	}

	/**
	 * @param feeUrl the feeUrl to set
	 */
	public static final void setFeeUrl(String url) {
		feeUrl = url;
	}


}
