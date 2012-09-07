/**
 * 
 */
package com.k99k.khunter.dao;


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
import com.k99k.testcenter.Product;
import com.k99k.testcenter.TTask;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;

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
	static DaoInterface phoneDao;
	static DaoInterface phoneGroupDao;
	static DaoInterface productDao;
	static DaoInterface taskDao;
	static DaoInterface taskUnitDao;
	
	/**
	 * {level:-1,_id:-1}
	 */
	public static final BasicDBObject prop_level_id_desc = new BasicDBObject("level",-1).append("_id",-1);

	
	/**
	 * {level:-1,_id:1}
	 */
	public static final BasicDBObject prop_level_id_asc = new BasicDBObject("level",-1).append("_id",1);
//	/**
//	 * {level:-1,state:-1,_id:1}
//	 */
//	public static final BasicDBObject prop_level_state_id_asc = new BasicDBObject("level",-1).append("state",-1).append("_id",1);
	/**
	 * {level:-1,updateTime:-1,_id:-1}
	 */
	public static final BasicDBObject prop_level_updateTime_id_desc = new BasicDBObject("level",-1).append("updateTime", -1).append("_id",-1);

	
	/**
	 * {$inc:{commsCount:1}}
	 */
	public static final BasicDBObject prop_topic_comm_inc = new BasicDBObject("$inc",new BasicDBObject("commsCount",1));

	/**
	 * {gFile:1,fileId:1,phone:1}
	 */
	public static final BasicDBObject fields_ftp_tid = new BasicDBObject("fileId",1).append("gFile", 1).append("phone", 1);

	/**
	 * {fileName:1}
	 */
	public static final BasicDBObject fields_ftp_fileName = new BasicDBObject("fileName",1);

	/**
	 * { >=0 }
	 */
	public static final BasicDBObject prop_normal = new BasicDBObject("$gte",0);
	
	/**
	 * {testTimes:1}
	 */
	public static final BasicDBObject prop_testTimes = new BasicDBObject("testTimes",1);

	
	public static final void initS(){
		tcUserDao = DaoManager.findDao("TCUserDao");
		tcNewsDao = DaoManager.findDao("TCNewsDao");
		phoneDao = DaoManager.findDao("TCPhoneDao");
		phoneGroupDao = DaoManager.findDao("TCPhoneGroupDao");
		productDao = DaoManager.findDao("TCProductDao");
		taskDao = DaoManager.findDao("TCTaskDao");
		taskUnitDao = DaoManager.findDao("TCTestUnitDao");
	}
	
	public static final KObject checkUser(String name,String pwd){
		if (name != null && pwd != null && name.trim().length()>1 && pwd.trim().length()>=6) {
			KObject ko = tcUserDao.findOne(name.trim());
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
	
	/**
	 * {name:1}
	 */
	private static final BasicDBObject prop_queryStr_fields = new BasicDBObject("name",1);
	/**
	 * {name:1}
	 */
	private static final BasicDBObject prop_queryGroup_fields = new BasicDBObject("name",1);
	/**
	 * {group:1}
	 */
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
	        	int g = Integer.parseInt(m.get("group").toString());
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
	
	/**
	 * json形式的查找,查找的字段为{name:1,shortName:1,group:1},sortby:prop_queryGroup_sort
	 * 针对WAP游戏(sys==2)时单独按归类生成json
	 * @param sys
	 * @param query
	 * @param skip
	 * @param len
	 * @param hint
	 * @return json string
	 */
	@SuppressWarnings("unchecked")
	public static final String queryPhoneGroupJson(int sys,int skip,int len,HashMap<String,Object> hint){
		try {
			HashMap<String,Object> query = new HashMap<String, Object>(2);
			query.put("type", sys);
			BasicDBObject q = (query==null)?prop_empty:(query instanceof BasicDBObject)?(BasicDBObject)query:new BasicDBObject(query);
			BasicDBObject hin = (hint==null)?null:new BasicDBObject(hint);
			StringBuilder sb = new StringBuilder();
			StringBuilder sb1 = new StringBuilder();
			StringBuilder sb2 = new StringBuilder();
			DBCollection coll = phoneDao.getColl();
			DBCursor cur = null;
			cur = coll.find(q, prop_queryGroup_fields).sort(prop_queryGroup_sort).skip(skip).limit(len).hint(hin);
			sb.append("{\"gg\":[{\"g\":-1,\"d\":[");
			int groupC = -1;
			if (sys != 2) {
				 while(cur.hasNext()) {
		        	HashMap<String, Object> m = (HashMap<String, Object>) cur.next();
		        	int g = Integer.parseInt(m.get("group").toString());
		        	if (g != groupC) {
						sb.append("]},{\"g\":").append(g).append(",\"d\":[\"").append((String)m.get("name")).append("\"");
						groupC = g;
					}else{
						sb.append(",\"").append((String)m.get("name")).append("\"");
					}
		        }
			}else{
				sb.append("]},{\"g\":0,\"d\":[");
			}
	       
	        //sb2.append("]}],\"aa\":{");
	        //增加phoneGroup代表机型组等
	        coll = phoneGroupDao.getColl();
	        cur = coll.find(q);
	        while(cur.hasNext()) {
	        	HashMap<String, Object> m = (HashMap<String, Object>) cur.next();
	        	ArrayList<String> li = (ArrayList<String>)m.get("phone");
	        	if (!li.isEmpty()) {
	        		//900+id作为g的序号
//					sb.append(",{\"g\":").append(900+Integer.parseInt(m.get("_id").toString()));
//					sb.append(",\"n\":\"").append(m.get("name")).append("\",\"d\":[");
	        		//sb2.append("\"").append(m.get("name")).append("\":[");
					Iterator<String> it = li.iterator();
					while (it.hasNext()) {
						String ph = it.next();
						sb1.append("\"").append(ph).append("\",");
					}
					sb1.deleteCharAt(sb1.length()-1);
					sb2.append(sb1).append("]");
				}
	        }
	        //------------------------
	        if (sys==2) {
	        	sb.append(sb1);
			}
	        sb.append("]},{\"g\":10,\"d\":[").append(sb2).append("}]}");
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
	
	/**
	 * 时间段测试情况分析查询
	 * @param start 起始毫秒数
	 * @param end 结束毫秒数
	 * @return json string
	 */
	public static final String analysisPeriod(long start,long end){
		StringBuilder sb = new StringBuilder();
		HashMap<String,Object> q = new HashMap<String, Object>(4);
		HashMap<String,Object> period = new HashMap<String, Object>(4);
		period.put("$gte", start);
		period.put("$lte", end);
		q.put("updateTime", period);
		
		//构造json
		sb.append("{");
		//通过状态数，部分通过状态数，待反馈状态，待测，测试中，放弃
		q.put("state", TTask.TASK_STATE_PASS);
		int pass = productDao.count(q);
		sb.append("\"pass\":").append(pass).append(",");
		q.put("state", TTask.TASK_STATE_PASS_PART);
		int pass_part = productDao.count(q);
		sb.append("\"pass_part\":").append(pass_part).append(",");
		q.put("state", TTask.TASK_STATE_NEED_MOD);
		int need_back = productDao.count(q);
		sb.append("\"need_back\":").append(need_back).append(",");
		q.put("state", TTask.TASK_STATE_TEST);
		int testing = productDao.count(q);
		sb.append("\"testing\":").append(testing).append(",");
		q.put("state", TTask.TASK_STATE_DROP);
		int droped = productDao.count(q);
		sb.append("\"droped\":").append(droped).append(",");
		//测试总数,除去放弃的
		int sum = pass + pass_part + need_back + testing;
		sb.append("\"sum\":").append(sum).append(",");
		
		//测试任务数,已创建任务但还未执行,等于接收到的新任务数
		q.put("state", TTask.TASK_STATE_NEW);
		int willDo = taskDao.count(q);
		sb.append("\"taskWillDo\":").append(willDo).append(",");
		//测试任务数,正在执行中
		q.put("state", TTask.TASK_STATE_TEST);
		int taskTesting = taskDao.count(q);
		sb.append("\"taskTesting\":").append(taskTesting).append(",");
		HashMap<String,Object> doneQ = new HashMap<String, Object>(2);
		doneQ.put("$gt", TTask.TASK_STATE_TEST);
		q.put("state", doneQ);
		int taskDone = taskDao.count(q);
		sb.append("\"taskDone\":").append(taskDone).append(",");
		
		
		//执行过的测试单元数
		doneQ.put("$gt", TTask.TASK_STATE_NEW);
		q.put("state", doneQ);
		int tuDone = taskUnitDao.count(q);
		sb.append("\"tuDone\":").append(tuDone).append("}");
		
		return sb.toString();
	}

	
}
