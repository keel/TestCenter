/**
 * 
 */
package com.k99k.khunter;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.k99k.tools.CnToSpell;
import com.k99k.tools.JSON;
import com.k99k.tools.Net;
import com.k99k.tools.StringUtil;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.MongoOptions;
import com.mongodb.ServerAddress;

/**
 * MongoDB连接处理器，单例
 * @author keel
 *
 */
public final class MongoConn implements DataSourceInterface{

	static final Logger log = Logger.getLogger(MongoConn.class);
	
	private String ip = "127.0.0.1";
	private int port = 27017;
	private String dbName = "tc";
	private String user = "keel";
	private String pwd = "jsinfo2901_A";
	
	private int connectionsPerHost = 50;
	private int threadsAllowedToBlockForConnectionMultiplier = 50;
	private int maxWaitTime = 5000;
	
	static Mongo mongo;
	private DB db;
	
	private String name = "mongodb_local";
	
	public MongoConn() {
		//init();
	}
	
	/**
	 * 引入新CP
	 * @param ip
	 * @param cpid
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final void importNewCompany(String ip,String[] cpid){
		MongoConn mongo = new MongoConn();
		mongo.setIp(ip);
		mongo.setPort(27017);
		mongo.setDbName("tc");
		mongo.setUser("keel");
		mongo.setPwd("jsGame_1810");
		if (mongo.init()) {
			DBCollection coc = mongo.getColl("TCCompany");
			DBCollection cuc = mongo.getColl("TCUser");
			//确定数据库表中的最大id
			DBCursor cur = cuc.find(new BasicDBObject(),new BasicDBObject("_id",1)).sort(new BasicDBObject("_id",-1)).limit(1);
			long lastId = 0;
			if (cur.hasNext()) {
				DBObject cc = (DBObject) cur.next();
				lastId = Long.parseLong(cc.get("_id").toString())+1;
			}
			cur = coc.find(new BasicDBObject(),new BasicDBObject("_id",1)).sort(new BasicDBObject("_id",-1)).limit(1);
			long lastcId = 0;
			if (cur.hasNext()) {
				DBObject cc = (DBObject) cur.next();
				lastcId = Long.parseLong(cc.get("_id").toString())+1;
			}
			//插入对象
			for (int i = 0; i < cpid.length; i++) {
				//获取公司接口信息
				String url = "http://202.102.39.9/MIS/v/entitytest/cps?startIndex=0&pageSize=10&cpId="+cpid[i];
				String comInfo = Net.getUrlContent(url, 3000, false, "utf-8");
				if (!StringUtil.isStringWithLen(comInfo, 10)) {
					System.out.println("获取公司接口数据失败:"+cpid[i]);
					continue;
				}
				
				HashMap<String,Object> comMap = (HashMap<String, Object>) JSON.read(comInfo);
				comMap = (HashMap<String, Object>) (((ArrayList)comMap.get("rows")).get(0));
				if (comMap ==  null || comMap.isEmpty()) {
					System.out.println("此公司不存在或无法从接口获取:"+cpid[i]);
					continue;
				}
				
				DBObject co = new BasicDBObject();
				co.put("name", cpid[i]);
				co.put("pwd", "egame");
				co.put("type", 1);
				co.put("level", 0);
				co.put("info", comMap.get("cpName").toString());
				co.put("phoneNumber", comMap.get("loginMobilePhone").toString());
				co.put("email", comMap.get("accessMail").toString());
				co.put("company", comMap.get("cpShortName").toString());
				co.put("newNews", 0);
				co.put("newTasks", 0);
				co.put("qq", "");
				co.put("state", 0);
				co.put("groupID", 0);
				co.put("groupLeader", 0);
				
				//更新TCUser

				co.put("_id", lastId);
				cuc.save(co);
				//更新TCCompany
				
				DBObject co2 = new BasicDBObject();
				String mainUser = co.get("name").toString();
				String name = co.get("company").toString();
				co2.put("_id", lastcId);
				co2.put("shortName", CnToSpell.getLetter(name));
				co2.put("mainUser", mainUser);
				co2.put("name", name);
				co2.put("state", 0);
				co2.put("level", 0);
				co2.put("type", 0);
				co2.put("version", 1);
				coc.save(co2);
				System.out.println("add ok:"+cpid[i]+" _id-u:"+lastId+" _id-c:"+lastcId);
				lastId++;
				lastcId++;
			}
			
		}
		
		mongo.close();
	}
	

	public static void main(String[] args) {
//		String[] cps = new String[]{
//				"C22003",
//				"C11146",
//				"C11147",
//				"C11148",
//				"C11149",
//				"C32060",
//				"C11150",
//				"C32061",
//				"C31020"
//		};
//		MongoConn.importNewCompany("202.102.40.43", cps);
		
		
		
		//test for mongolab.com test
		MongoConn mongo = new MongoConn();
		mongo.setIp("127.0.0.1");
//		mongo.setIp("202.102.40.43");
		mongo.setPort(27017);
		//mongo.setPort(27137);
		mongo.setDbName("tc");
		mongo.setUser("keel");
		mongo.setPwd("jsGame_1810");
		if (mongo.init()) {
			DBCollection co = mongo.getColl("TCTopic");
			DBCursor cur = co.find();
			while (cur.hasNext()) {
				DBObject cc = (DBObject) cur.next();
				cc.put("updateTime", Long.parseLong(cc.get("createTime").toString()));
				co.save(cc);
			}
		}
			//-----------------------新公司导入----------------
//			DBCollection co = mongo.getColl("TCCompany");
//			DBCollection cu = mongo.getColl("TCUser");
//			//在TCUser中的新公司的_id开始
//			long userStart = 382;
//			DBCursor cur = cu.find(new BasicDBObject("_id",new BasicDBObject("$gte",userStart)));
//			//TCCompany中的新公司_id
//			long i = 383;
//			while (cur.hasNext()) {
//				//System.out.println(cur.next());
//				DBObject co2 = new BasicDBObject();
//				DBObject cc = (DBObject) cur.next();
//				String mainUser = cc.get("name").toString();
//				String name = cc.get("company").toString();
//				
//				co2.put("_id", i);
//				co2.put("shortName", CnToSpell.getLetter(name));
//				co2.put("mainUser", mainUser);
//				co2.put("name", name);
//				co2.put("state", 0);
//				co2.put("level", 0);
//				co2.put("type", 0);
//				co2.put("version", 1);
//				
//				co.save(co2);
//				
//				System.out.println(co2);
//				
//				i++;
//			}
			//-----------------------新公司导入结束----------------
			//-----------------修正测试不通过状态9为3---------------
			
//			DBCollection co = mongo.getColl("TCTestUnit");
//			DBCursor cur = co.find(new BasicDBObject(),new BasicDBObject("re",1));
//			while (cur.hasNext()) {
//				BasicDBObject c = (BasicDBObject) cur.next();
//				if (c.containsField("re")) {
//					long id = c.getLong("_id");
//					BasicDBList cc = (BasicDBList) c.get("re");
//					if (cc != null && !cc.isEmpty()) {
//						Iterator<Object> it = cc.iterator();
//						boolean willUpdate = false;
//						while (it.hasNext()) {
//							DBObject m = (DBObject) it.next();
//							if (Integer.parseInt(m.get("re").toString()) == 9) {
//								m.put("re", 3L);
//							}
//							willUpdate = true;
//						}
//						if (willUpdate) {
//							co.update(new BasicDBObject("_id",id), new BasicDBObject("$set",new BasicDBObject("re",cc)));
//						}
//					}
//				}
//			}
//			System.out.println("TCTestUnit ok.");
//			co = mongo.getColl("TCTask");
//			cur = co.find(new BasicDBObject(),new BasicDBObject("result",1));
//			while (cur.hasNext()) {
//				BasicDBObject c = (BasicDBObject) cur.next();
//				if (c.containsField("result")) {
//					String restr = (String) c.get("result");
//					long id = Long.parseLong(c.getString("_id"));
//					HashMap<String,Object> re = (HashMap<String, Object>) JSON.read(restr);
//					if (re!=null && !re.isEmpty()) {
//						Iterator<Entry<String,Object>> it = re.entrySet().iterator();
//						while (it.hasNext()) {
//							Entry<String,Object> e = it.next();
//							String key = e.getKey();
//							Object oe = e.getValue();
//							if (oe instanceof String) {
//								continue;
//							}
//							ArrayList ls = (ArrayList)oe;
//							Iterator<Object> it2 = ls.iterator();
//							while(it2.hasNext()){
//								HashMap r = (HashMap)it2.next();
//								if (r.containsKey("re")) {
//									int ree =  Integer.parseInt(r.get("re").toString());
//									if (ree == 9) {
//										r.put("re", 3);
//									}
//								}
//							}
//						}
//					}
//					co.update(new BasicDBObject("_id",id), new BasicDBObject("$set",new BasicDBObject("result",JSON.write(re))));
//				}
//			}
//			System.out.println("TCTask ok.");
//			//-----------------修正测试不通过状态9为3 结束---------------
//			
//			
//		mongo.close();
//		}else{
//			System.out.println("err!");
//		}
	}
	
	/**
	 * @param ip
	 * @param port
	 * @param dbName
	 * @param user
	 * @param pwd
	 */
	public MongoConn(String ip, int port, String dbName, String user, String pwd) {
		super();
		this.ip = ip;
		this.port = port;
		this.dbName = dbName;
		this.user = user;
		this.pwd = pwd;
		//init();
	}

	/**
	 * 获取一个数据库连接,失败时返回null,本方法自身不处理失败情况
	 * @param colName
	 * @return DBCollection
	 */
	public DBCollection getColl(String colName){
		try {
			return db.getCollection(colName);
		} catch (Exception e) {
			log.error("getColl error!!", e);
			return null;
		}
	}
	
	
	/**
	 * 创建新表结构,先drop,后创建,同时生成索引 ,最后清除数据
	 * @param kc KObjConfig
	 * @return
	 */
	public boolean buildNewTable(KObjConfig kc){
		try {
			DaoInterface dao = kc.getDaoConfig().findDao();
			DBCollection coll = this.getColl(dao.getTableName());
			coll.drop();
			coll = this.getColl(dao.getTableName());
			KObject kobj = kc.getKobjSchema().createDefaultKObj();
			coll.save(new MongoWrapper(kobj));
			//index
			HashMap<String,KObjIndex> ins = kc.getKobjSchema().getIndexes();
			Iterator<Map.Entry<String, KObjIndex>> it = ins.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, KObjIndex> entry = it.next();
				KObjIndex ki = entry.getValue();
				coll.ensureIndex(new BasicDBObject(ki.getCol(), (ki.isAsc())?1:-1),ki.getCol(), ki.isUnique());
			}
			// 再清除
			coll.remove(new MongoWrapper(kobj));
		} catch (Exception e) {
			log.error("buildNewTable error!!", e);
			return false;
		}
		return true;
	}
	
	
	/**
	 * 创建DB对象
	 */
	public final boolean init(){
		try {
			if (mongo == null || !mongo.getConnector().isOpen() ) {
				ServerAddress sadd = new ServerAddress(this.ip, this.port);
				MongoOptions opt = new MongoOptions();
				opt.autoConnectRetry = false;
				opt.connectionsPerHost = this.connectionsPerHost;
				opt.threadsAllowedToBlockForConnectionMultiplier = this.threadsAllowedToBlockForConnectionMultiplier;
				opt.maxWaitTime = this.maxWaitTime;
				mongo = new Mongo(sadd,opt);
			}
			db = mongo.getDB(this.dbName);
			boolean auth = false;
			if (!db.isAuthenticated()) {
				auth = db.authenticate(this.user, this.pwd.toCharArray());
			}else{
				auth = true;
			}
			if (!auth) {
				log.error("auth error! user:"+this.user);
			}else{
				log.info("=========== mongoConn init OK! ============");
				return true;
			}
		} catch (UnknownHostException e) {
			log.error("init error!!", e);
			return false;
		} catch (MongoException e) {
			log.error("init error!!", e);
			return false;
		} catch (Exception e) {
			log.error("init error!!", e);
			return false;
		}
		return false;
	}
	
	

	
	/**
	 * 获取DB
	 * @return DB
	 */
	public final DB getDB(){
		return db;
	}
	
	/**
	 * 重新初始化Mongo及DB,可在参数变动后调用
	 * @return 是否初始化成功
	 */
	public final boolean reset(){
		if (mongo != null) {
			mongo.close();
		}
		return init();
	}
	
	/**
	 * 关闭Mongo
	 */
	public final void close(){
		mongo.close();
	}

	/**
	 * @return the ip
	 */
	public final String getIp() {
		return ip;
	}

	/**
	 * @param ip the ip to set
	 */
	public final void setIp(String ip) {
		this.ip = ip;
	}

	/**
	 * @return the port
	 */
	public final int getPort() {
		return port;
	}

	/**
	 * @param port the port to set
	 */
	public final void setPort(int port) {
		this.port = port;
	}

	/**
	 * @return the dbName
	 */
	public final String getDbName() {
		return dbName;
	}

	/**
	 * @param dbName the dbName to set
	 */
	public final void setDbName(String dbName) {
		this.dbName = dbName;
	}

	/**
	 * @return the user
	 */
	public final String getUser() {
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public final void setUser(String user) {
		this.user = user;
	}

	/**
	 * @return the pwd
	 */
	public final String getPwd() {
		return pwd;
	}

	/**
	 * @param pwd the pwd to set
	 */
	public final void setPwd(String pwd) {
		this.pwd = pwd;
	}

	/**
	 * @return the connectionsPerHost
	 */
	public final int getConnectionsPerHost() {
		return connectionsPerHost;
	}

	/**
	 * @param connectionsPerHost the connectionsPerHost to set
	 */
	public final void setConnectionsPerHost(int connectionsPerHost) {
		this.connectionsPerHost = connectionsPerHost;
	}

	/**
	 * @return the threadsAllowedToBlockForConnectionMultiplier
	 */
	public final int getThreadsAllowedToBlockForConnectionMultiplier() {
		return threadsAllowedToBlockForConnectionMultiplier;
	}

	/**
	 * @param threadsAllowedToBlockForConnectionMultiplier the threadsAllowedToBlockForConnectionMultiplier to set
	 */
	public final void setThreadsAllowedToBlockForConnectionMultiplier(
			int threadsAllowedToBlockForConnectionMultiplier) {
		this.threadsAllowedToBlockForConnectionMultiplier = threadsAllowedToBlockForConnectionMultiplier;
	}

	/**
	 * @return the maxWaitTime
	 */
	public final int getMaxWaitTime() {
		return maxWaitTime;
	}

	/**
	 * @param maxWaitTime the maxWaitTime to set
	 */
	public final void setMaxWaitTime(int maxWaitTime) {
		this.maxWaitTime = maxWaitTime;
	}

	/**
	 * @return the name
	 */
	public final String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public final void setName(String name) {
		this.name = name;
	}

	@Override
	public void exit() {
		mongo.close();
	}
	
	
	
}
