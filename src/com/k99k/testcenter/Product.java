/**
 * 
 */
package com.k99k.testcenter;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

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
import com.k99k.tools.CnToSpell;
import com.k99k.tools.StringUtil;

/**
 * @author keel
 *
 */
public class Product extends Action {

	/**
	 * @param name
	 */
	public Product(String name) {
		super(name);
	}
	static final Logger log = Logger.getLogger(Product.class);
	
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
		if (subact.equals("")) {
			this.list(req, u, httpmsg);
		}else if(subact.equals("new")){
			
		}else if(subact.equals("find")){
			this.find(req, u, httpmsg);
		}else if(subact.equals("one")){
			this.one(req, u, httpmsg);
		}else if (StringUtil.isDigits(subact)) {
			
		}else if(subact.equals("a_a")){
			
		}else if(subact.equals("a_u")){
			
		}else if(subact.equals("a_d")){
			
		}else if(subact.equals("a_s")){
			
		}
		return super.act(msg);
	}
	
	/**
	 * 检测参数并添加Product
	 * @param p HashMap
	 * @return pid ,失败则返回负值
	 */
	static long add(HashMap<String,Object> p){
		//增加一个sys与url的联动验证
		Object syso = p.get("sys");
		if(!StringUtil.isDigits(syso)){return -1;}
		int sys = Integer.parseInt(String.valueOf(syso));
		if(sys==2 && !StringUtil.isStringWithLen(p.get("url"), 3)){return -2;}
		Object name = p.get("name");
		if(!StringUtil.isStringWithLen(name, 2)){
			return -4;
		}
		p.put("shortName", CnToSpell.getLetter(name.toString()));
		KObject kobj = new KObject();
		if(schema.setPropFromMapForCreate(p,kobj)){
			return -3;
		}
		kobj.setId(dao.getIdm().nextId());
		if(dao.save(kobj)){
			return kobj.getId();
		}
		return -4;
	}
	
	/**
	 * 查询单个产品(json形式)
	 * @param req
	 * @param u
	 * @param msg
	 */
	private void one(HttpServletRequest req,KObject u,HttpActionMsg msg){
		String pidstr = req.getParameter("pid");
		String p = req.getParameter("p");
		KObject pt = null;
		if (StringUtil.isDigits(pidstr)) {
			pt = dao.findOne(Long.parseLong(pidstr));
		}else if(StringUtil.isStringWithLen(p, 1)){
			//长度少于2直接返回空
			if (!StringUtil.isStringWithLen(p,2)) {
				msg.addData("[print]", "");
				return ;
			}
//			try {
				//p = new String(p.trim().getBytes("ISO-8859-1"),"utf-8");
				p = p.trim();
//			} catch (UnsupportedEncodingException e) {
//				e.printStackTrace();
//			}
			pt = dao.findOne(p);
		}
		//权限不够
		if(u.getType()<2 && !pt.getProp("company").equals(u.getProp("company"))){
			msg.addData("[print]", "");
			return ;
		}
		String re = (pt==null)?"":pt.toString();
		msg.addData("[print]",re);
		return;
	}
	
	/**
	 * 查询产品,按产品名称拼音头字母缩写,根据用户所属的公司和查询参数q
	 * @param req
	 * @param u
	 * @param msg
	 */
	private void find(HttpServletRequest req,KObject u,HttpActionMsg msg){
		String q = req.getParameter("q");
		String c = req.getParameter("c");
		//长度少于2直接返回空
		if (!StringUtil.isStringWithLen(q, 2) || !StringUtil.isStringWithLen(c, 2)) {
			msg.addData("[print]", "");
			return ;
		}
//		try {
			q = q.trim();//new String(q.getBytes("ISO-8859-1"),"utf-8").trim();
			c = c.trim();//new String(c.getBytes("ISO-8859-1"),"utf-8").trim();
//		} catch (UnsupportedEncodingException e1) {
//			e1.printStackTrace();
//			msg.addData("[print]", "");
//			return ;
//		}
		//权限不够
		if(u.getType()<2 && !c.equals((String)u.getProp("company"))){
			msg.addData("[print]", "");
			return ;
		}
		Pattern p = Pattern.compile(q.toLowerCase());
		HashMap<String,Object> query = new HashMap<String, Object>(6);
		query.put("company", c);
		query.put("state", 0);
		query.put("shortName", p);
		String re = StaticDao.queryStr(dao, query,  null, 0, 0, null);
		msg.addData("[print]",re);
		return;
	}
	
	/**
	 * 查看列表
	 * FIXME 权限未验证
	 * @param req
	 * @param u
	 * @param msg
	 */
	private void list(HttpServletRequest req,KObject u,ActionMsg msg){
		String p_str = req.getParameter("p");
		String pz_str = req.getParameter("pz");
		int page = StringUtil.isDigits(p_str)?Integer.parseInt(p_str):1;
		int pz = StringUtil.isDigits(pz_str)?Integer.parseInt(pz_str):this.pageSize;
		ArrayList<KObject> list = dao.queryByPage(page,pageSize,StaticDao.prop_state_normal, null, StaticDao.prop_id_desc, null);
		msg.addData("u", u);
		msg.addData("list", list);
		msg.addData("pz", pz);
		msg.addData("p", page);
		msg.addData("[jsp]", "/WEB-INF/tc/products.jsp");
	}
	

	/* (non-Javadoc)
	 * @see com.k99k.khunter.Action#init()
	 */
	@Override
	public void init() {
		dao = DaoManager.findDao("TCProductDao");
		schema = KObjManager.findSchema("TCProduct");
		super.init();
	}
	
	private int pageSize = 30;

	/**
	 * @return the pageSize
	 */
	public final int getPageSize() {
		return pageSize;
	}

	/**
	 * @param pageSize the pageSize to set
	 */
	public final void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	
	


}
