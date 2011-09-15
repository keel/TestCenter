/**
 * 
 */
package com.k99k.khunter.dao;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import com.k99k.khunter.DaoInterface;
import com.k99k.khunter.DaoManager;
import com.k99k.khunter.DataSourceInterface;
import com.k99k.khunter.KObject;
import com.k99k.khunter.MongoDao;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

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
	
	/**
	 * {level:-1,_id:-1}
	 */
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
		search.put("state",0);
		return tcNewsDao.queryByPage(page,pageSize,search, null, prop_level_id_desc, null);
	}
	
	private static final BasicDBObject prop_queryStr_fields = new BasicDBObject("name",1);
	private static final BasicDBObject prop_queryGroup_fields = new BasicDBObject("name",1);
	private static final BasicDBObject prop_queryGroup_sort = new BasicDBObject("group",1);
	static{
		prop_queryStr_fields.put("shortName", 1);
		prop_queryGroup_fields.put("shortName", 1);
		prop_queryGroup_fields.put("group", 1);
	}
	
	
	/**
	 * 通用的单字符串内容查找过程,查找的字段为{name:1,shortName:1}
	 * @param dao 必须有
	 * @param query 必须有,为null则为默认查询
	 * @param sortBy 无则为null
	 * @param skip 无则为0
	 * @param len 无则为0
	 * @param hint 无则为null
	 * @return String 各项用\n分开
	 */
	@SuppressWarnings("unchecked")
	public static final String queryStr(DaoInterface dao,HashMap<String,Object> query,HashMap<String,Object> sortBy,int skip,int len,HashMap<String,Object> hint){
		try {
			BasicDBObject q = (query==null)?prop_empty:(query instanceof BasicDBObject)?(BasicDBObject)query:new BasicDBObject(query);
			BasicDBObject sort = (sortBy==null)?null:new BasicDBObject(sortBy);
			BasicDBObject hin = (hint==null)?null:new BasicDBObject(hint);
			
			StringBuilder sb = new StringBuilder();
			DBCollection coll = dao.getColl();
			DBCursor cur = null;
			if (sortBy != null) {
				cur = coll.find(q, prop_queryStr_fields).sort(sort).skip(skip).limit(len).hint(hin);
			} else {
				cur = coll.find(q, prop_queryStr_fields).skip(skip).limit(len).hint(hin);
			}
	        while(cur.hasNext()) {
	        	HashMap<String, Object> m = (HashMap<String, Object>) cur.next();
	        	sb.append((String)m.get("shortName")).append("|").append((String)m.get("name")).append("\n");
	        }
	        return sb.toString();
		} catch (Exception e) {
			log.error("query string error!", e);
			return null;
		}
	}
	
	/**
	 * json形式的查找,查找的字段为{name:1,shortName:1,group:1},sortby:prop_queryGroup_sort
	 * @param dao
	 * @param query
	 * @param skip
	 * @param len
	 * @param hint
	 * @return json string
	 */
	@SuppressWarnings("unchecked")
	public static final String queryGroupJson(DaoInterface dao,HashMap<String,Object> query,int skip,int len,HashMap<String,Object> hint){
		try {
			BasicDBObject q = (query==null)?prop_empty:(query instanceof BasicDBObject)?(BasicDBObject)query:new BasicDBObject(query);
			BasicDBObject hin = (hint==null)?null:new BasicDBObject(hint);
			StringBuilder sb = new StringBuilder();
			DBCollection coll = dao.getColl();
			DBCursor cur = null;
			cur = coll.find(q, prop_queryGroup_fields).sort(prop_queryGroup_sort).skip(skip).limit(len).hint(hin);
			sb.append("[{\"g\":-1,\"d\":[");
			int groupC = -1;
	        while(cur.hasNext()) {
	        	HashMap<String, Object> m = (HashMap<String, Object>) cur.next();
	        	int g = (Integer)m.get("group");
	        	if (g != groupC) {
					sb.append("]},{\"g\":").append(g).append(",\"d\":[\"").append((String)m.get("name")).append("\"");
					groupC = g;
				}else{
					sb.append(",\"").append((String)m.get("name")).append("\"");
				}
	        }
	        sb.append("]}]");
	        return sb.toString();
		} catch (Exception e) {
			log.error("queryGroupJson error!", e);
			return null;
		}
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
