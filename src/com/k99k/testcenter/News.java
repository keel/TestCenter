/**
 * 
 */
package com.k99k.testcenter;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;

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
	
	static DaoInterface dao;
	static KObjSchema schema;

	/* (non-Javadoc)
	 * @see com.k99k.khunter.Action#act(com.k99k.khunter.ActionMsg)
	 */
	@SuppressWarnings("unchecked")
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
			String p_str = req.getParameter("p");
			String pz_str = req.getParameter("pz");
			int page = StringUtil.isDigits(p_str)?Integer.parseInt(p_str):1;
			int pz = StringUtil.isDigits(pz_str)?Integer.parseInt(pz_str):this.pageSize;
			ArrayList<KObject> list = StaticDao.loadNews(page, pz);
			msg.addData("u", u);
			msg.addData("list", list);
			msg.addData("pz", pz);
			msg.addData("p", page);
			msg.addData("[jsp]", "/WEB-INF/tc/news.jsp");
			return super.act(msg);
		}else if (subact.equals("new")) {
			msg.addData("u", u);
			if (u.getType() < 10 || u.getLevel()<1) {
				//权限不够
				JOut.err(401, httpmsg);
				return super.act(msg);
			}
			msg.addData("[jsp]", "/WEB-INF/tc/news_add.jsp");
			return super.act(msg);
		}else if (StringUtil.isDigits(subact)) {
			msg.addData("u", u);
			long id = Long.parseLong(subact);
			KObject news_one = dao.findOne(id);
			if (news_one == null || news_one.getState() == -1) {
				JOut.err(404, httpmsg);
				return super.act(msg);
			}
			int type = news_one.getType();
			int userType = u.getType();
			if (type > userType) {
				//阅读权限不够
				JOut.err(403, httpmsg);
				return super.act(msg);
			}
			msg.addData("news_one", news_one);
			if (req.getParameter("edit")!=null && u.getLevel()>=1) {
				msg.addData("[jsp]", "/WEB-INF/tc/news_edit.jsp");
			}else{
				Object unReads = u.getProp("unReadNews");
				ArrayList<Long> unreadIds = (unReads==null)?null:(ArrayList<Long>)unReads;
				StaticDao.readOneNews(u.getId(), id,unreadIds);
				msg.addData("[jsp]", "/WEB-INF/tc/news_one.jsp");
			}
			return super.act(msg);
		}else if(subact.equals("add")){
			if (u.getType() < 4) {
				//权限不够
				JOut.err(401, httpmsg);
				return super.act(msg);
			}
			String name = req.getParameter("news_name");
			String text = req.getParameter("news_text");
			String files = req.getParameter("news_files");
			int level = (StringUtil.isDigits(req.getParameter("news_level")))?Integer.parseInt(req.getParameter("news_level")):0;
			int type = (StringUtil.isDigits(req.getParameter("news_type")))?Integer.parseInt(req.getParameter("news_type").trim()):0;
			if (StringUtil.isStringWithLen(name, 3) && StringUtil.isStringWithLen(text, 2)) {
				KObject kobj = schema.createEmptyKObj(dao);
				kobj.setName(StringUtil.repstr1(name.trim()));
				kobj.setProp("text", StringUtil.repstr1(text.trim()));
				kobj.setLevel(level);
				kobj.setType(type);
				kobj.setCreatorName(u.getName());
				if (StringUtil.isStringWithLen(files, 1)) {
					boolean enc = true;
					try {
						files = URLDecoder.decode(files, "utf-8");
					} catch (UnsupportedEncodingException e) {
						enc = false;
					}
					if (enc) {
						String[] fs = files.split(",");
						kobj.setProp("files", fs);
						//生成下载文件
						for (int i = 0; i < fs.length; i++) {
							KObject newf = Download.schema.createEmptyKObj(Download.dao);
							String f = fs[i];
							int po = f.lastIndexOf(".");
							newf.setName(f.substring(0,po));
							newf.setProp("fileName", f);
							newf.setProp("type", f.substring(po+1));
							newf.setProp("creatorName", u.getName());
							Download.dao.save(newf);
						}
						
					}
				}
				if (dao.save(kobj)) {
					re = String.valueOf(kobj.getId());
					msg.addData("[print]", re);
					ActionMsg task = new ActionMsg("newsTask");
					task.addData(TaskManager.TASK_TYPE, TaskManager.TASK_TYPE_EXE_POOL);
					task.addData("newsId", kobj.getId());
					TaskManager.makeNewTask("TCNewsTask:"+kobj.getId(), task);
					return super.act(msg);
				}
			}
			JOut.err(401, httpmsg);
			return super.act(msg);
		}else if(subact.equals("update")){
			if (u.getType() < 4) {
				//权限不够
				JOut.err(401, httpmsg);
				return super.act(msg);
			}
			if (StringUtil.isDigits(req.getParameter("id"))) {
				long id = Long.parseLong(req.getParameter("id"));
				String name = req.getParameter("news_name");
				String text = req.getParameter("news_text");
				String files = req.getParameter("news_files");
				int level = (StringUtil.isDigits(req.getParameter("news_level")))?Integer.parseInt(req.getParameter("news_level")):0;
				int type = (StringUtil.isDigits(req.getParameter("news_type")))?Integer.parseInt(req.getParameter("news_type")):0;
				if (StringUtil.isStringWithLen(name, 3) && StringUtil.isStringWithLen(text, 2)) {
					KObject kobj = dao.findOne(id);
					kobj.setName(StringUtil.repstr1(name.trim()));
					kobj.setProp("text", StringUtil.repstr1(text.trim()));
					kobj.setLevel(level);
					kobj.setType(type);
					if (StringUtil.isStringWithLen(files, 1)) {
						boolean enc = true;
						try {
							files = URLDecoder.decode(files, "utf-8");
						} catch (UnsupportedEncodingException e) {
							enc = false;
						}
						if (enc) {
							String[] fs = files.split(",");
							ArrayList<String> oldFiles = (ArrayList<String>) kobj.getProp("files");
							ArrayList<String> newFiles = new ArrayList<String>();
							for (int i = 0; i < fs.length; i++) {
								if (!oldFiles.contains(fs[i])) {
									newFiles.add(fs[i]);
								}
							}
							kobj.setProp("files", fs);
							//生成下载文件
							Iterator<String> it = newFiles.iterator();
							while (it.hasNext()) {
								String f = it.next();
								KObject newf = Download.schema.createEmptyKObj(Download.dao);
								int po = f.lastIndexOf(".");
								newf.setName(f.substring(0,po));
								newf.setProp("fileName", f);
								newf.setProp("type", f.substring(po+1));
								newf.setProp("creatorName", u.getName());
								Download.dao.save(newf);
							}
						}
					}
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
			if (u.getType() < 4) {
				//权限不够
				JOut.err(401, httpmsg);
				return super.act(msg);
			}
			if (StringUtil.isDigits(req.getParameter("id"))) {
				long id = Long.parseLong(req.getParameter("id"));
				if (dao.deleteOne(id) !=null) {
					re = "ok";
					msg.addData("[print]", re);
					return super.act(msg);
				}
			}
			JOut.err(403, httpmsg);
			return super.act(msg);
		}else if(subact.equals("search")){
			if (StringUtil.isStringWithLen(req.getParameter("k"), 1)) {
				String key = null;
				try {
					//TODO 针对tomcatURL编码转换
					key = new String(req.getParameter("k").getBytes("ISO-8859-1"),"utf-8").trim();
				} catch (UnsupportedEncodingException e) {
				}
				String p_str = req.getParameter("p");
				String pz_str = req.getParameter("pz");
				int page = StringUtil.isDigits(p_str)?Integer.parseInt(p_str):1;
				int pz = StringUtil.isDigits(pz_str)?Integer.parseInt(pz_str):this.pageSize;
				//(StringUtil.isStringWithLen(req.getParameter("by"), 1))?req.getParameter("by").trim():"name";
//				re = MongoDao.writeKObjList(StaticDao.search(page, pz,search));
//				msg.addData("[print]", re);
				ArrayList<KObject> list = StaticDao.searchNewsByName(page, pz,key);
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
			JOut.err(404, httpmsg);
//			msg.addData("u", u);
//			msg.addData("[jsp]", "/WEB-INF/tc/news.jsp");
		}
		
		return super.act(msg);
	}
	
	

	/* (non-Javadoc)
	 * @see com.k99k.khunter.Action#init()
	 */
	@Override
	public void init() {
		dao = DaoManager.findDao("TCNewsDao");
		schema = KObjManager.findSchema("TCNews");
		super.init();
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
