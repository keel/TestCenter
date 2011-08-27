/**
 * 
 */
package com.k99k.testcenter;

import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import com.k99k.khunter.Action;
import com.k99k.khunter.ActionMsg;
import com.k99k.khunter.DaoInterface;
import com.k99k.khunter.DaoManager;
import com.k99k.khunter.HttpActionMsg;
import com.k99k.khunter.JOut;
import com.k99k.khunter.KFilter;
import com.k99k.khunter.KObjManager;
import com.k99k.khunter.KObject;
import com.k99k.khunter.MongoDao;
import com.k99k.khunter.TaskManager;
import com.k99k.khunter.dao.StaticDao;
import com.k99k.tools.StringUtil;

/**
 * 公告 FIXME 所有操作需要Log
 * @author keel
 *
 */
public class News extends Action {

	/**
	 * @param name
	 */
	public News(String name) {
		super(name);
	}
	
	private int pageSize = 30;

	/* (non-Javadoc)
	 * @see com.k99k.khunter.Action#act(com.k99k.khunter.ActionMsg)
	 */
	@Override
	public ActionMsg act(ActionMsg msg) {
		HttpActionMsg httpmsg = (HttpActionMsg)msg;
		HttpServletRequest req = httpmsg.getHttpReq();
		String subact = KFilter.actPath(msg, 2, "");
		KObject u = Auth.checkCookieLogin(httpmsg);
		String re = "err";
		if (u == null) {
			msg.addData("[redirect]", "/login");
			return super.act(msg);
		}
		//子动作
		if(subact.equals("")){
			msg.addData("u", u);
			String p_str = req.getParameter("p");
			String pz_str = req.getParameter("pz");
			int page = StringUtil.isDigits(p_str)?Integer.parseInt(p_str):1;
			int pz = StringUtil.isDigits(pz_str)?Integer.parseInt(pz_str):this.pageSize;
			ArrayList<KObject> list = StaticDao.loadNews(page, pz);
			msg.addData("list", list);
			msg.addData("pz", pz);
			msg.addData("p", page);
			msg.addData("[jsp]", "/WEB-INF/tc/news.jsp");
			return super.act(msg);
		}else if (subact.equals("new")) {
			msg.addData("u", u);
			if (Integer.parseInt(u.getType()) < 4) {
				//权限不够
				JOut.err(403, httpmsg);
				return super.act(msg);
			}
			msg.addData("[jsp]", "/WEB-INF/tc/news_add.jsp");
			return super.act(msg);
		}else if (StringUtil.isDigits(subact)) {
			msg.addData("u", u);
			long id = Long.parseLong(subact);
			KObject news_one = DaoManager.findDao("TCNewsDao").findOne(id);
			if (news_one == null) {
				JOut.err(404, httpmsg);
				return super.act(msg);
			}
			int type = Integer.parseInt(news_one.getType());
			int userType = Integer.parseInt(u.getType());
			if (type > userType) {
				//阅读权限不够
				JOut.err(403, httpmsg);
				return super.act(msg);
			}
			msg.addData("news_one", news_one);
			if (req.getParameter("edit")!=null && u.getLevel()>=1) {
				msg.addData("[jsp]", "/WEB-INF/tc/news_edit.jsp");
			}else{
				msg.addData("[jsp]", "/WEB-INF/tc/news_one.jsp");
			}
			return super.act(msg);
		}else if(subact.equals("add")){
			if (Integer.parseInt(u.getType()) < 4) {
				//权限不够
				JOut.err(403, httpmsg);
				return super.act(msg);
			}
			String name = req.getParameter("news_name");
			String text = req.getParameter("news_text");
			int level = (StringUtil.isDigits(req.getParameter("news_level")))?Integer.parseInt(req.getParameter("news_level")):0;
			String type = (StringUtil.isDigits(req.getParameter("news_type")))?req.getParameter("news_type"):"0";
			if (StringUtil.isStringWithLen(name, 3) && StringUtil.isStringWithLen(text, 2)) {
				KObject kobj = KObjManager.findSchema("TCNews").createEmptyKObj();
				kobj.setName(name.trim());
				kobj.setProp("text", text.trim());
				kobj.setLevel(level);
				kobj.setType(type);
				kobj.setCreatorName(u.getName());
				if (DaoManager.findDao("TCNewsDao").save(kobj)) {
					re = "ok";
					msg.addData("[print]", re);
					ActionMsg task = new ActionMsg("newsTask");
					task.addData(TaskManager.TASK_TYPE, TaskManager.TASK_TYPE_EXE_POOL);
					task.addData("ggId", kobj.getId());
					TaskManager.makeNewTask("TCNewsTask:"+kobj.getId(), task);
					return super.act(msg);
				}
			}
			JOut.err(401, httpmsg);
			return super.act(msg);
		}else if(subact.equals("update")){
			if (Integer.parseInt(u.getType()) < 4) {
				//权限不够
				JOut.err(403, httpmsg);
				return super.act(msg);
			}
			if (StringUtil.isDigits(req.getParameter("id"))) {
				long id = Long.parseLong(req.getParameter("id"));
				String name = req.getParameter("news_name");
				String text = req.getParameter("news_text");
				int level = (StringUtil.isDigits(req.getParameter("news_level")))?Integer.parseInt(req.getParameter("news_level")):0;
				String type = (StringUtil.isDigits(req.getParameter("news_type")))?req.getParameter("news_type"):"0";
				if (StringUtil.isStringWithLen(name, 3) && StringUtil.isStringWithLen(text, 2)) {
					DaoInterface dao = DaoManager.findDao("TCNewsDao");
					KObject kobj = dao.findOne(id);
					kobj.setName(name.trim());
					kobj.setProp("text", text.trim());
					kobj.setLevel(level);
					kobj.setType(type);
					if (dao.save(kobj)) {
						re = "ok";
						msg.addData("[print]", re);
						return super.act(msg);
					}
				}
			}
			JOut.err(401, httpmsg);
			return super.act(msg);
		}else if(subact.equals("del")){
			if (Integer.parseInt(u.getType()) < 4) {
				//权限不够
				JOut.err(403, httpmsg);
				return super.act(msg);
			}
			if (StringUtil.isDigits(req.getParameter("id"))) {
				long id = Long.parseLong(req.getParameter("id"));
				if (DaoManager.findDao("TCNewsDao").deleteOne(id) !=null) {
					re = "ok";
					msg.addData("[print]", re);
					return super.act(msg);
				}
			}
			JOut.err(401, httpmsg);
			return super.act(msg);
		}else if(subact.equals("search")){
			if (StringUtil.isStringWithLen(req.getParameter("k"), 1)) {
				String p_str = req.getParameter("p");
				String pz_str = req.getParameter("pz");
				int page = StringUtil.isDigits(p_str)?Integer.parseInt(p_str):1;
				int pz = StringUtil.isDigits(pz_str)?Integer.parseInt(pz_str):this.pageSize;
				String by = "name";//(StringUtil.isStringWithLen(req.getParameter("by"), 1))?req.getParameter("by").trim():"name";
				String key = req.getParameter("k").trim();
				HashMap<String,Object> search = new HashMap<String, Object>();
				search.put(by, key);
//				re = MongoDao.writeKObjList(StaticDao.search(page, pz,search));
//				msg.addData("[print]", re);
				ArrayList<KObject> list = StaticDao.loadNews(page, pz);
				msg.addData("u", u);
				msg.addData("list", list);
				msg.addData("pz", pz);
				msg.addData("p", page);
				msg.addData("[jsp]", "/WEB-INF/tc/news.jsp");
				return super.act(msg);
			}
			JOut.err(401, httpmsg);
			return super.act(msg);
		}else if (subact.equals("load")) {
			//载入某页公告
			String p_str = req.getParameter("p");
			String pz_str = req.getParameter("pz");
			int page = StringUtil.isDigits(p_str)?Integer.parseInt(p_str):1;
			int pz = StringUtil.isDigits(pz_str)?Integer.parseInt(pz_str):this.pageSize;
			re = MongoDao.writeKObjList(StaticDao.loadNews(page, pz));
			msg.addData("[print]", re);
			return super.act(msg);
		}else{
			msg.addData("u", u);
			msg.addData("[jsp]", "/WEB-INF/tc/news.jsp");
		}
		
		return super.act(msg);
	}

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
