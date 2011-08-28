/**
 * 
 */
package com.k99k.khunter.dao;


import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import com.k99k.khunter.DaoInterface;
import com.k99k.khunter.DaoManager;
import com.k99k.khunter.DataSourceInterface;
import com.k99k.khunter.KObject;
import com.k99k.khunter.MongoDao;
import com.mongodb.BasicDBObject;

/**
 * 静态方法执行的Dao，执有多个DaoManager的Dao对象,需要在最后一个Action中初始化
 * @author keel
 *
 */
public class StaticDao extends MongoDao {

	/**
	 * @param daoName
	 * @param dataSource
	 */
	public StaticDao(String daoName, DataSourceInterface dataSource) {
		super(daoName, dataSource);
	}
	
	static final Logger log = Logger.getLogger(StaticDao.class);
	
	static DaoInterface tcUserDao;
	static DaoInterface tcNewsDao;
	
	public static final BasicDBObject prop_level_id_desc = new BasicDBObject("level",-1).append("_id",-1);

	
	public static final void initS(){
		tcUserDao = DaoManager.findDao("TCUserDao");
		tcNewsDao = DaoManager.findDao("TCNewsDao");
	}
	
	public static final KObject checkUser(String name,String pwd){
		if (name != null && pwd != null && name.toString().trim().length()>3 && pwd.toString().trim().length()>=6) {
			KObject ko = tcUserDao.findOne(name);
			if (ko != null && ko.getProp("pwd").equals(pwd)) {
				return ko;
			}
		}
		return null;
	}
	
	/**
	 * 处理阅读后未读公告的-1
	 * @param userId
	 * @param newsId
	 * @param unreadIds ArrayList<Long>
	 */
	public static final void readOneNews(long userId,long newsId,ArrayList<Long> unreadIds){
		boolean unread = false;
		if (unreadIds == null || unreadIds.isEmpty()) {
			return;
		}
		Iterator<Long> it = unreadIds.iterator();
		while (it.hasNext()) {
			long id = it.next();
			if (newsId == id) {
				unread = true;
				break;
			}
		}
		if (unread) {
			HashMap<String,Object> set = new HashMap<String, Object>(4);
			HashMap<String,Object> pull = new HashMap<String, Object>(2);
			pull.put("unReadNews", newsId);
			set.put("$pull", pull);
			HashMap<String,Object> inc = new HashMap<String, Object>(2);
			inc.put("newNews", -1);
			set.put("$inc", inc);
			HashMap<String,Object> query = new HashMap<String, Object>(4);
			query.put("_id", userId);
			tcUserDao.update(query, set, false, false);
		}
	}
	
	public static final ArrayList<KObject> loadNews(int page,int pageSize){
		return tcNewsDao.queryByPage(page,pageSize,prop_state_0, null, prop_level_id_desc, null);
	}
	
	public static final ArrayList<KObject> searchNewsByName(int page,int pageSize,String key){
		Pattern p = Pattern.compile(key);  
		HashMap<String,Object> search = new HashMap<String, Object>(2);
		search.put("name", p);
		return tcNewsDao.queryByPage(page,pageSize,search, null, prop_level_id_desc, null);
	}
	
//	public static final boolean login(String uName,String uPwd){
//		if (uName != null && uPwd != null && uName.toString().trim().length()>3 && uPwd.toString().trim().length()>=6) {
//			HashMap<String,Object> m = tcUserDao.findOneMap(new BasicDBObject("name", uName).append("pwd", uPwd),new BasicDBObject("_id", 1));
//			if (m != null) {
//				return true;
//			}
//		}
//		return false;
//	}
	

	
}
