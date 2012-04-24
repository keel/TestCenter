/**
 * 
 */
package com.k99k.khunter;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
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
	
	/*
	public static void main(String[] args) {
		//test for mongolab.com test
		MongoConn mongo = new MongoConn();
		mongo.setIp("127.0.0.1");
		mongo.setPort(27017);
		//mongo.setPort(27137);
		mongo.setDbName("tc");
		mongo.setUser("keel");
		mongo.setPwd("jsGame_1810");
		if (mongo.init()) {
			//DBCollection conn = mongo.getColl("cp1");
			DBCollection co = mongo.getColl("TCCompany");
			DBCollection cu = mongo.getColl("TCUser");
			DBCursor cur = cu.find();
			long i = 8;
			while (cur.hasNext()) {
				//System.out.println(cur.next());
				DBObject co2 = new BasicDBObject();
				DBObject cc = (DBObject) cur.next();
				String mainUser = cc.get("name").toString();
				String name = cc.get("company").toString();
				
				co2.put("_id", i);
				co2.put("shortName", CnToSpell.getLetter(name));
				co2.put("mainUser", mainUser);
				co2.put("name", name);
				co2.put("state", 0);
				co2.put("level", 0);
				co2.put("type", 0);
				co2.put("version", 1);
				
				co.save(co2);
				
				//conn.save(cc);
				System.out.println(co2);
				
				
				
//				cc.put("name", cpName);
//				cc.put("mainUser", cpid);
//				cc.put("cpId", cpid);
//				cc.removeField("cpid");
//				cc.removeField("cpName");
				co.save(cc);
				
//				cc.put("_id", i);
//				cc.put("name", cpid);
//				cc.put("type", 11);
//				cc.put("phoneNumber", "");
//				cc.put("pwd", "egame");
//				cc.put("company", cpName);
//				cc.put("egameID", cpid);
//				cc.removeField("cpid");
//				cc.removeField("cpName");
//				cc.removeField("shortName");
//				cu.save(cc);
				
				//System.out.println(cc);
				i++;
			}
		}else{
			System.out.println("err!");
		}
	}
	*/
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
