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
	private String dbName = "tc3"; //测试数据库
	private String user = "keel";
	private String pwd = "jsGame_1810";
	
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
	 * 引入新CP(老)
	 * @param ip
	 * @param cpid
	
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
	 */
	
	/**
	 * 引入新CP
	 * @param ip
	 * @param cpid
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final void importNewCompany2(String ip,String[] cpid){
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
				
				//确定cpid是否已存在，如存在则跳过 --如果去除此段可对已存在用户进行更新
				cur = coc.find(new BasicDBObject("mainUser",cpid[i]));
				if (cur.hasNext()) {
					//skip
					System.out.println("已存在此公司,跳过:"+cpid[i]);
					continue;
				}
				
				//获取公司接口信息
				String url = "http://202.102.39.9:82/Business/entitytest/cps.do?cpId="+cpid[i];
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
				co.put("info", comMap.get("cnName").toString());
				co.put("phoneNumber", comMap.get("linkPhone").toString());
				co.put("email", comMap.get("linkEmail").toString());
				co.put("company", comMap.get("shortName").toString());
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
	
	/**
	 * 修改测试人员名称及关联测试对象
	 * @param orgName
	 * @param newName
	 */
	public static final void changeTestUserName(MongoConn mongo,String orgName,String newName){
		
		if (mongo.init()) {
			//更新TCUser,TCTask,TCTestUnit.tester
			DBCollection coll = mongo.getColl("TCUser");
			coll.update(new BasicDBObject("name",orgName),  new BasicDBObject("$set",new BasicDBObject("name",newName)), false, true);
			System.out.println("TCUser updated.");
			
			
			
//			coll = mongo.getColl("TCTask");
//			coll.update(new BasicDBObject("log.user",orgName),  new BasicDBObject("$set",new BasicDBObject("log.$.user",newName)), false, true);
//			System.out.println("TCTask updated.");
			
			
			
			coll = mongo.getColl("TCTestUnit");
			coll.update(new BasicDBObject("tester",orgName),  new BasicDBObject("$set",new BasicDBObject("tester",newName)), false, true);
			System.out.println("TCTestUnit updated.");
			
			
		}
	}
	
	/**
	 * 新增机型组,拆分
	 */
	public void newGroupSplit(String newGroup,String newShortName,long newEgameId,String oldGroup,HashMap<String,Object> newParas){
		long lastId = 0;
		DBCollection coll = this.getColl("TCPhone");
		DBCursor cur = coll.find().limit(1).sort(new BasicDBObject("_id",-1));
		if (cur.hasNext()) {
			lastId = (Long) cur.next().get("_id");
		}else {
			System.out.println("empty table TCPhone");
			return;
		}
		cur = coll.find(new BasicDBObject("name",oldGroup));
		if (cur.hasNext()) {
			DBObject c = cur.next();
			lastId++;
			c.put("_id", lastId);
			c.put("name", newGroup);
			c.put("shortName", newShortName);
			if (!newParas.isEmpty()) {
				Iterator it = newParas.entrySet().iterator();
				while(it.hasNext()){
					Entry en = (Entry) it.next();
					c.put(en.getKey().toString(), en.getValue());
				}
			}
			coll.insert(c);
		}
		//目前通过和未测游戏新增拆分出来的机型组
		 coll = this.getColl("TCTestUnit");
		 cur = coll.find().limit(1).sort(new BasicDBObject("_id",-1));
			if (cur.hasNext()) {
				lastId = (Long) cur.next().get("_id");
			}else {
				System.out.println("empty table TCTestUnit");
				return;
			}
		 HashMap<String,Object> q = new HashMap<String, Object>();
		HashMap<String,Object> in = new HashMap<String, Object>();
		in.put("$in", new int[]{0,2,4});
		q.put("state", in);
		q.put("phone", oldGroup);
		 cur = coll.find(new BasicDBObject(q));
		while (cur.hasNext()) {
			DBObject c = cur.next();
			lastId++;
			c.put("_id", lastId);
			c.put("phone", newGroup);
			c.put("egameId", newEgameId);
			coll.insert(c);
		}
	}
	/**
	 * 机型组合并
	 */
	public  void newGroupMerge(String newGroup,String oldGroup,HashMap<String,Object> newParas){
		DBCollection coll = this.getColl("TCPhone");
		//coll.update(new BasicDBObject("name",oldGroup),new BasicDBObject("state",-1),false,false);
		coll.remove(new BasicDBObject("name",oldGroup));
		//将被合的机型组改为新机型组
		 coll = this.getColl("TCTestUnit");
		HashMap<String,Object> q = new HashMap<String, Object>();
		HashMap<String,Object> in = new HashMap<String, Object>();
		in.put("$in", new int[]{0,2,4});
		q.put("state", in);
		q.put("phone", oldGroup);
		DBCursor cur = coll.find(new BasicDBObject(q));
		while (cur.hasNext()) {
			DBObject c = cur.next();
			String state = c.get("state").toString();
			if (state.trim().equals("0")) {
				coll.remove(c);
				continue;
			}
			c.put("phone", newGroup);
			coll.save(c);
		}
	}
	
	
	
	/**
	 * 比较机型组通过结果
	 * @param mongo
	 */
	public static void cc(MongoConn mongo){
		
		if (mongo.init()) {
			String p2 = "#三星I579";
			String p1= "#酷派5855";
			DBCollection coll = mongo.getColl("TCTestUnit");
			DBCursor cur = coll.find(new BasicDBObject("phone",p1).append("state", 2));
			long re = 0;
			long cc = 0;
			while(cur.hasNext()){
				DBObject d = cur.next();
//				if (d.get("PID") == null) {
//					continue;
//				}
				int pid = Integer.parseInt(d.get("PID").toString());
				DBCursor cur2 = coll.find(new BasicDBObject("PID",pid).append("phone",p2).append("state", 9));
				if (cur2.hasNext()) {
					DBObject d1 = cur2.next();
					re++;
				}
				cc++;
			}
			System.out.println(p1+":"+cc);
			System.out.println(p2+":"+re);
//			long cc = coll.count(new BasicDBObject("state",3).append("phone", "华为C8650+"));
//			System.out.println("华为C8650+:"+cc);
//			cc = coll.count(new BasicDBObject("state",3).append("phone", "中兴N760"));
//			System.out.println("中兴N760:"+cc);
		}
		mongo.close();
	}
	
	
	/**
	 * 平台升级割接,增加机型组支持
	 * @param ip
	 */
	public static void newEgame(String ip){
		String[] pGroup = {"华为C8500", "中兴N600", "中兴N606", "三星I559", "华为C8600", "酷派D539", "华为C8650+", "中兴N760", "三星I579", "三星I909MR", "酷派5855", "摩托XT800", "摩托XT800+", "摩托XT882", "摩托XT928"};
		long[] pGroupEgameId = {1140, 1141, 1142, 1143, 1144, 1145, 1146, 1147, 1148, 1149, 1150, 1151, 1152, 1153, 1154};
		//获取原phone表中的记录,改为机型组的信息，再加入表中
		HashMap<String,Long> newPG = new HashMap<String, Long>();
		for (int i = 0; i < pGroup.length; i++) {
			newPG.put(pGroup[i], pGroupEgameId[i]);
		}
		try {
			MongoConn mongo = new MongoConn();
			mongo.setIp(ip);
			mongo.setPort(27017);
			mongo.setDbName("tc");
			mongo.setUser("keel");
			mongo.setPwd("jsGame_1810");
			if (mongo.init()) {
				DBCollection coll = mongo.getColl("TCPhone");
				DBCursor cur = null;
				BasicDBObject q = new BasicDBObject();
				cur = coll.find(q).sort(new BasicDBObject("_id",-1)).limit(1);
				long maxId = 0;
				if (cur.hasNext()) {
					maxId = (Long) cur.next().get("_id");
				}
				System.out.println("TCPhone maxId:"+maxId);
				
				cur = coll.find(q);
				while (cur.hasNext()) {
					DBObject c = cur.next();
					String cn = (String) c.get("name");
					//对"华为C8650+"单独处理
					if (cn.equals("华为C8650")) {
						maxId++;
						c.put("_id", maxId);
						c.put("name", "华为C8650+");
						c.put("shortName", "C8650+");
						c.put("egameId", 1866);
						coll.insert(c);
						System.out.println(maxId+" add:"+c.get("name"));
						maxId++;
						c.removeField("_id");
						c.put("name", "#华为C8650+");
						c.put("group", 10);
						c.put("egameId", newPG.get("华为C8650+"));
						c.put("_id", maxId);
						coll.insert(c);
						System.out.println(maxId+" add:"+c.get("name"));
					}else if (newPG.containsKey(cn)) {
						c.removeField("_id");
						c.put("name", "#" + cn);
						c.put("group", 10);
						c.put("egameId", newPG.get(cn));
						maxId++;
						c.put("_id", maxId);
						coll.insert(c);
						System.out.println(maxId+" add:"+c.get("name"));
					}

				}
				// 更新TCPhoneGroup表的android主机型
				coll = mongo.getColl("TCPhoneGroup");
				String[] pGroupS = new String[pGroup.length];
				for (int i = 0; i < pGroup.length; i++) {
					pGroupS[i] ="#"+pGroup[i];
				}
				q = new BasicDBObject("_id", 2L);
				BasicDBObject set = new BasicDBObject();
				BasicDBObject update = new BasicDBObject();
				set.put("phone", pGroupS);
				update.put("$set", set);
				coll.update(q, update);
				System.out.println("phoneGroup updated.");
				//fee信息变更
				coll = mongo.getColl("TCProduct");
				q = new BasicDBObject();
				cur = coll.find(q);
				while (cur.hasNext()) {
					DBObject c = cur.next();
					String feeInfo = null;
					if (c.get("feeInfo") instanceof BasicDBList) {
						feeInfo = JSON.write(c);
					}else{
						feeInfo = (String) c.get("feeInfo");
					}
					feeInfo = feeInfo.replace("\"id\":", "\"consumeId\":");
					feeInfo = feeInfo.replace("\"consumecode\":", "\"consumeCode\":");
					feeInfo = feeInfo.replace("\"consumecodedsc\":", "\"description\":");
					feeInfo = feeInfo.replace("\"consumecodename\":", "\"consumeName\":");
					feeInfo = feeInfo.replace("\"fee\":", "\"price\":");
					feeInfo = feeInfo.replace("\"memo\":", "\"feeType\":");
					feeInfo = feeInfo.replace("\"notecode\":", "\"smcode\":");
					feeInfo = feeInfo.replace("\"paychanel\":", "\"buyGuide\":");
					feeInfo = feeInfo.replace("\"serviceid\":", "\"gameId\":");
					feeInfo = feeInfo.replace("\"triger\":", "\"trigerCondition\":");
					feeInfo = feeInfo.replace("\"typeflag\":", "\"consumeTypeName\":");
					c.put("feeInfo", feeInfo);
					coll.save(c);
				}
				System.out.println("feeInfo updated.");
				
				//testCase增加
				BasicDBObject[] newCases = {
						new BasicDBObject("name","游戏安装").append("caseId", 1).append("level", 10).append("info", "1.1	能正常安装游戏到测试终端。<br /> 1.2	Android单机游戏安装时验证权限，一般要求不能有“网络通讯”、“短信接收”、“访问通讯录”等无关权限。如有特殊情况(如嵌入爱游戏SDK等)需要在提交测试时说明。<br /> 1.3	如果是需要加载数据包的单机游戏，允许“网络通讯”权限，允许游戏第一次运行时联网下载数据包。<br /> 1.4	安装完成后手机中只能出现一个游戏图标。").append("state", 0),
						new BasicDBObject("name","游戏启动").append("caseId", 2).append("level", 10).append("info", "2.1	运行游戏程序，在启动中无长时间停顿和异常挂起。<br /> 2.2	游戏启动进入主画面耗时3秒以上，需提供进度提示。<br /> 2.3	开机界面中应出现“爱游戏”LOGO，不能出现其它运营商LOGO。").append("state", 0),
						new BasicDBObject("name","游戏名称").append("caseId", 3).append("level", 10).append("info", "3.1	游戏安装后在手机内显示的程序名称、游戏运行时显示名称应与在爱游戏平台申报的名称以及游戏内容一致。").append("state", 0),
						new BasicDBObject("name","游戏菜单").append("caseId", 4).append("level", 2).append("info", "4.1	游戏“更多游戏”链接的指向游戏平台。<br /> 4.2	游戏帮助：游戏玩法的详细说明，帮助中必须包含：应用中文说明、按键说明。<br /> 4.3	关于:包含应用中文名称、应用类型、公司名称、客服电话、免责声明、版本号。").append("state", 0),
						new BasicDBObject("name","屏幕适配").append("caseId", 5).append("level", 10).append("info", "5.1	屏幕画面与测试终端适配，不影响游戏运行和操作，游戏功能项必须全部完整显示，并保证完整的视觉效果。").append("state", 0),
						new BasicDBObject("name","游戏使用").append("caseId", 6).append("level", 10).append("info", "6.1	各功能键使用（触摸）正常；能正常打开菜单进行操作；在游戏操作中无报错、死机、反应过慢、自动退出等异常情况。<br /> 6.2	特殊功能：如终端支持重力感应，且在游戏中有应用，按操作说明能正常游戏。比如支持横竖屏切换，适配正常，游戏进度正常。如终端支持GPS，且游戏有调用GPS模块，按操作说明能正常游戏，如产生流量资费需要先提示说明。<br /> 6.3	游戏声音：游戏暂停时不能有游戏声音，如果有声音开关必须能正常使用。<br /> 6.4	游戏内输入文字等信息时，能正常调用输入法正常输入。<br /> 6.5	JAVA游戏在触屏手机上使用必须有虚拟键盘。").append("state", 0),
						new BasicDBObject("name","游戏文字及内容信息安全").append("caseId", 7).append("level", 10).append("info", "7.1	游戏中所有提示、对话、说明均为中文。<br /> 7.2	无乱码，无明显文字错误。<br /> 7.3	无粗口及涉嫌淫秽、赌博、暴力、政治等文字内容；<br /> 7.4	游戏中不能存在与本游戏无关的宣传广告和链接。").append("state", 0),
						new BasicDBObject("name","游戏中断").append("caseId", 8).append("level", 10).append("info", "8.1	游戏运行中手机待机、关闭屏幕、手机来电或有其他优先操作，游戏必须暂停。<br /> 8.2	游戏中手机来电时，来电提示正常。接听、挂断电话等操作后，返回游戏，游戏正常运行。<br /> 8.3	游戏中手机来短信时，短信提示正常。回复短信后，返回游戏，游戏正常运行。").append("state", 0),
						new BasicDBObject("name","计费").append("caseId", 9).append("level", 10).append("info", "9.1	游戏计费必须与平台申报的计费点完全一致，无重复计费及扣费差异（多扣或少扣）。<br /> 9.2	Android游戏必须使用短代SDK。").append("state", 0),
						new BasicDBObject("name","游戏退出").append("caseId", 10).append("level", 10).append("info", "10.1	可使用菜单退出或使用手机自带退出键退出，如果有需要保存进度的游戏必须提供保存功能，退出后可继续游戏。").append("state", 0),
						new BasicDBObject("name","游戏卸载").append("caseId", 11).append("level", 10).append("info", "11.1	能正常卸载已经安装的游戏。").append("state", 0),
						new BasicDBObject("name","WAP游戏").append("caseId", 12).append("level", 10).append("info", "12.1	游戏登录时必须使用爱游戏登录(同步)接口。<br /> 12.2	必须有帮助说明。<br /> 12.3	必须有“返回爱游戏”链接。<br /> 12.4	游戏内不能有指向游戏以外的链接（指向爱游戏平台除外）。").append("state", 0)
						
				};
//				HashMap<Long,BasicDBObject> newCaseMap = new HashMap<Long, BasicDBObject>();
//				newCaseMap.put(3L, newCases[0]);
//				newCaseMap.put(4L, newCases[1]);
//				newCaseMap.put(6L, newCases[2]);
//				newCaseMap.put(10L, newCases[3]);
//				newCaseMap.put(7L, newCases[4]);
//				newCaseMap.put(8L, newCases[5]);
//				newCaseMap.put(13L, newCases[6]);
//				newCaseMap.put(15L, newCases[7]);
//				newCaseMap.put(16L, newCases[8]);
//				newCaseMap.put(17L, newCases[9]);
//				newCaseMap.put(18L, newCases[10]);
//				newCaseMap.put(999L, newCases[11]);
//				
//				newCaseMap.put(20L, newCases[0]);
//				newCaseMap.put(21L, newCases[1]);
//				newCaseMap.put(23L, newCases[2]);
//				newCaseMap.put(26L, newCases[3]);
//				newCaseMap.put(24L, newCases[4]);
//				newCaseMap.put(25L, newCases[5]);
//				newCaseMap.put(29L, newCases[6]);
//				newCaseMap.put(31L, newCases[7]);
//				newCaseMap.put(32L, newCases[8]);
//				newCaseMap.put(33L, newCases[9]);
//				newCaseMap.put(34L, newCases[10]);
				
				coll = mongo.getColl("TCTestCase");
				//将老所有的state改为-1
				q = new BasicDBObject();
				set = new BasicDBObject();
				update = new BasicDBObject(); 
				set.put("state", -1);
				update.put("$set", set);
				coll.update(q, update,false,true);
				System.out.println("old TCTestCase'state updated.");
				//添加新的TestCase
				int mId = 36;
				for (int i = 0; i < newCases.length; i++) {
					newCases[i].append("type", 0);
					newCases[i].append("_id", mId);
					coll.insert(newCases[i]);
					mId++;
				}
				for (int i = 0; i < newCases.length; i++) {
					newCases[i].append("type", 1);
					newCases[i].append("_id", mId);
					coll.insert(newCases[i]);
					mId++;
				}
				System.out.println("new TCTestCase added.");
				//更新老的结果记录
				HashMap<String,String> repla = new HashMap<String, String>();
				repla.put("1", "1");
				repla.put("2", "1");
				repla.put("3", "1");
				repla.put("4", "2");
				repla.put("6", "3");
				repla.put("7", "5");
				repla.put("8", "6");
				repla.put("9", "6");
				repla.put("10", "4");
				repla.put("11", "4");
				repla.put("12", "4");
				repla.put("13", "7");
				repla.put("14", "8");
				repla.put("15", "8");
				repla.put("16", "9");
				repla.put("17", "10");
				repla.put("18", "11");
				repla.put("19", "1");
				repla.put("20", "1");
				repla.put("21", "2");
				repla.put("22", "2");
				repla.put("23", "3");
				repla.put("24", "5");
				repla.put("25", "6");
				repla.put("26", "4");
				repla.put("27", "4");
				repla.put("28", "4");
				repla.put("29", "7");
				repla.put("30", "8");
				repla.put("31", "8");
				repla.put("32", "9");
				repla.put("33", "10");
				repla.put("34", "11");
				
				coll = mongo.getColl("TCTestUnit");
				q = new BasicDBObject();
				cur = coll.find(q);
				while (cur.hasNext()) {
					DBObject c = cur.next();
					if (c.get("re")!=null ) {
						BasicDBList ls = (BasicDBList) c.get("re");
						if (ls.size()>0) {
							for (int i = 0; i < ls.size(); i++) {
								DBObject r = (DBObject) ls.get(i);
								String rr = String.valueOf(r.get("caseId"));
								if (repla.containsKey(rr)) {
									r.put("caseId", repla.get(rr));
								}
							}
							//c.put("re", ls);
							coll.save(c);
						}
					}
				}
				System.out.println("TCTestUnit updated.");
//				coll = mongo.getColl("TCTask");
//				q = new BasicDBObject();
//				cur = coll.find(q);
//				while (cur.hasNext()) {
//					DBObject c =  cur.next();
//					String re = (String)c.get("result");
//					if (StringUtil.isStringWithLen(re, 2)) {
//						HashMap<String,Object> result = (HashMap<String, Object>) JSON.read(re);
//						if (result == null) {
//							System.out.println(re);
//							continue;
//						}
//						HashMap<String,Object> nr = new HashMap<String, Object>();
//						Iterator<Entry<String, Object>> it = result.entrySet().iterator();
//						while (it.hasNext()) {
//							Entry<String, Object> entry = it.next();
//							String k = entry.getKey().toString();
//							if (repla.containsKey(k)) {
//								String n = repla.get(k);
//								ArrayList<HashMap<String,Object>> l = (ArrayList<HashMap<String, Object>>) entry.getValue();
//								for (int i = 0; i < l.size(); i++) {
//									HashMap<String,Object> m = l.get(i);
//									m.put("caseId", n);
//								}
//								nr.put(n, l);
//							}
//						}
//						c.put("result", JSON.write(nr));
//						coll.save(c);
//					}
//				}
//				System.out.println("TCTask updated.");
				
			}
			System.out.println("finished-----");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	
	public static void updatePGroup(String ip){
		String[] pGroup = {"华为C8500", "中兴N600", "中兴N606", "三星I559", "华为C8600", "酷派D539", "华为C8650+", "中兴N760", "三星I579", "三星I909MR", "酷派5855", "摩托XT800", "摩托XT800+", "摩托XT882", "摩托XT928"};
		HashMap<String,String> map = new HashMap<String, String>();
		for (int i = 0; i < pGroup.length; i++) {
			map.put(pGroup[i], pGroup[i]);
		}
		MongoConn mongo = new MongoConn();
		mongo.setIp(ip);
		mongo.setPort(27017);
		mongo.setDbName("tc");
		mongo.setUser("keel");
		mongo.setPwd("jsGame_1810");
		if (mongo.init()) {
			DBCollection coll = mongo.getColl("TCTestUnit");
			DBCursor cur = null;
			BasicDBObject q = new BasicDBObject();
			BasicDBObject state = new BasicDBObject("$gt",2000L);
			q.put("TID", state);
			cur = coll.find(q);
			while (cur.hasNext()) {
				DBObject c = cur.next();
				String p = c.get("phone").toString();
				if (map.containsKey(p)) {
					c.put("phone", "#"+p);
					coll.save(c);
				}
			}
		}
		
		
	}
	
	public static void main(String[] args) {
		String ip = "127.0.0.1";
//		ip = "202.102.40.43";
		
		
		String[] cps = new String[]{
				"C35001",
				"C09033",
				"C00111",
				"C00113",
				"C00137",
				"C00128",
				"C00138",
				"C00151",
				"C00153",
				"C00159",
				"C11001",
				"C32009",
				"C11030",
				"C32017",
				"C44014",
				"C11037",
				"C11052",
				"C44027",
				"C11045",
				"C11060",
				"C44030",
				"C32029",
				"C11080",
				"C31012",
				"C32063",
				"C11087",
				"C11158",
				"C31020",
				"C09116",
				"C09119",
				"C35011",
				"C12006",
				"C00188",
				"C43003",
				"C41002",
				"C11114",
				"C11145",
				"C32050",
				"C11134",
				"C09074",
				"C09079",
				"C09084",
				"C51011",
				"C11130",
				"C11111",
				"C11136",
				"C31019",
				"C09131",
				"C09132",
				"C09135",
				"C09137",
				"C09139",
				"C09142",
				"C09144",
				"C09145",
				"C09146",
				"C09016",
				"C09062",
				"C09057",
				"C00126",
				"C00133",
				"C09127",
				"C11061",
				"C11074",
				"C11066",
				"C00180",
				"C00181",
				"C32045",
				"C44033",
				"C11075",
				"C09108",
				"C11146",
				"C00185",
				"C11151",
				"C32062",
				"C35007",
				"C11103",
				"C11115",
				"C32061",
				"C11132",
				"C31014",
				"C44050",
				"C09065",
				"C09067",
				"C09069",
				"C33007",
				"C11099",
				"C11090",
				"C34003",
				"C09090",
				"C09092",
				"C41001",
				"C23001",
				"C36001",
				"C11118",
				"C11128",
				"C50001",
				"C35005",
				"C09123",
				"C11124",
				"C11148",
				"C22001",
				"C11002",
				"C13001",
				"C11010",
				"C09061",
				"C09002",
				"C00134",
				"C00167",
				"C00172",
				"C00173",
				"C00120",
				"C00124",
				"C00175",
				"C00107",
				"C00140",
				"C00152",
				"C00160",
				"C00161",
				"C21001",
				"C32011",
				"C11031",
				"C32014",
				"C44016",
				"C09102",
				"C35008",
				"C09086",
				"C32046",
				"C61003",
				"C32057",
				"C00174",
				"C09063",
				"C00165",
				"C32001",
				"C09001",
				"C09022",
				"C00171",
				"C00115",
				"C00119",
				"C00123",
				"C00142",
				"C00150",
				"C09015",
				"C32008",
				"C32012",
				"C11017",
				"C44010",
				"C11019",
				"C31001",
				"C31007",
				"C32018",
				"C44013",
				"C44018",
				"C11039",
				"C11050",
				"C33003",
				"C44022",
				"C11054",
				"C21004",
				"C11049",
				"C32023",
				"C11036",
				"C51002",
				"C31005",
				"C32028",
				"C51004",
				"C32021",
				"C12003",
				"C11053",
				"C11056",
				"C11067",
				"C51008",
				"C32031",
				"C44032",
				"C44035",
				"C11104",
				"C31018",
				"C00183",
				"C09110",
				"C09118",
				"C09122",
				"C61002",
				"C11122",
				"C00187",
				"C44026",
				"C51005",
				"C11046",
				"C11047",
				"C32020",
				"C11068",
				"C11069",
				"C11070",
				"C32043",
				"C11079",
				"C11150",
				"C32055",
				"C09115",
				"C09117",
				"C37002",
				"C44053",
				"C11107",
				"C12005",
				"C11142",
				"C09076",
				"C09082",
				"C11108",
				"C42004",
				"C34004",
				"C33005",
				"C32049",
				"C31016",
				"C44046",
				"C09080",
				"C37005",
				"C11116",
				"C09098",
				"C09091",
				"C09107",
				"C00178",
				"C11135",
				"C11008",
				"C42001",
				"C09059",
				"C09058",
				"C00169",
				"C44003",
				"C51001",
				"C09004",
				"C09046",
				"C09034",
				"C09025",
				"C00117",
				"C00154",
				"C32007",
				"C11012",
				"C32006",
				"C33001",
				"C11016",
				"C11018",
				"C31006",
				"C11033",
				"C31004",
				"C09064",
				"C11020",
				"C11021",
				"C32013",
				"C32016",
				"C11025",
				"C11026",
				"C11028",
				"C11032",
				"C44019",
				"C44011",
				"C11035",
				"C13003",
				"C32027",
				"C44025",
				"C44024",
				"C37001",
				"C44034",
				"C32033",
				"C32035",
				"C21005",
				"C11077",
				"C33006",
				"C11127",
				"C00184",
				"C11100",
				"C44044",
				"C35009",
				"C09109",
				"C09113",
				"C09121",
				"C31017",
				"C11125",
				"C11117",
				"C11160",
				"C13006",
				"C11139",
				"C11157",
				"C11147",
				"C11121",
				"C44043",
				"C09094",
				"C09070",
				"C09072",
				"C09073",
				"C09075",
				"C09083",
				"C11082",
				"C32044",
				"C35006",
				"C09104",
				"C44047",
				"C09085",
				"C32041",
				"C09096",
				"C11086",
				"C51009",
				"C11112",
				"C44001",
				"C11119",
				"C32056",
				"C09106",
				"C09099",
				"C11097",
				"C09088",
				"C11098",
				"C09097",
				"C22003",
				"C31015",
				"C00177",
				"C11131",
				"C31013",
				"C44038",
				"C43001",
				"C11123",
				"C32042",
				"C09101",
				"C32037",
				"C09124",
				"C42003",
				"C32005",
				"C09056",
				"C09005",
				"C00118",
				"C00141",
				"C00156",
				"C32002",
				"C44002",
				"C11007",
				"C32003",
				"C09036",
				"C44005",
				"C09024",
				"C09021",
				"C09035",
				"C00136",
				"C11004",
				"C09038",
				"C09011",
				"C09051",
				"C09052",
				"C09026",
				"C09049",
				"C00176",
				"C09054",
				"C00110",
				"C00135",
				"C00127",
				"C00155",
				"C00157",
				"C11022",
				"C11023",
				"C11024",
				"C31002",
				"C11038",
				"C11041",
				"C11042",
				"C35002",
				"C11062",
				"C11063",
				"C11044",
				"C13004",
				"C00112",
				"C00166",
				"C00131",
				"C00132",
				"C00139",
				"C44004",
				"C11011",
				"C44008",
				"C32010",
				"C44009",
				"C11029",
				"C11027",
				"C32015",
				"C31003",
				"C44012",
				"C12002",
				"C12001",
				"C44015",
				"C44017",
				"C13002",
				"C11059",
				"C11055",
				"C12004",
				"C44020",
				"C35004",
				"C32026",
				"C32022",
				"C32024",
				"C31010",
				"C11081",
				"C44031",
				"C11078",
				"C11091",
				"C32039",
				"C44037",
				"C43002",
				"C11093",
				"C09095",
				"C00186",
				"C11144",
				"C11141",
				"C22002",
				"C32060",
				"C09066",
				"C09071",
				"C09077",
				"C11143",
				"C11084",
				"C09089",
				"C12007",
				"C44049",
				"C32034",
				"C09055",
				"C09028",
				"C09060",
				"C09003",
				"C09000",
				"C09010",
				"C09048",
				"C09044",
				"C09045",
				"C11009",
				"C44006",
				"C11003",
				"C11006",
				"C09019",
				"C00143",
				"C00168",
				"C00170",
				"C00116",
				"C00121",
				"C00122",
				"C00125",
				"C00129",
				"C00130",
				"C00158",
				"C09013",
				"C44007",
				"C11013",
				"C21002",
				"C32004",
				"C11014",
				"C11015",
				"C11034",
				"C21003",
				"C51003",
				"C11048",
				"C11057",
				"C44028",
				"C44029",
				"C35003",
				"C32025",
				"C44023",
				"C31009",
				"C13005",
				"C31008",
				"C11051",
				"C33004",
				"C32032",
				"C11072",
				"C11076",
				"C42002",
				"C00182",
				"C44040",
				"C61004",
				"C11092",
				"C32047",
				"C11089",
				"C09111",
				"C09120",
				"C11085",
				"C11140",
				"C11149",
				"C09081",
				"C37003",
				"C09103",
				"C09105",
				"C32038",
				"C11064",
				"C11065",
				"C51006",
				"C31011",
				"C32030",
				"C51007",
				"C11071",
				"C11137",
				"C00179",
				"C43004",
				"C11101",
				"C32036",
				"C44051",
				"C35010",
				"C09112",
				"C09114",
				"C34002",
				"C44036",
				"C34001",
				"C44048",
				"C33008",
				"C53001",
				"C09068",
				"C11096",
				"C11129",
				"C32054",
				"C11088",
				"C09093",
				"C11102",
				"C11138",
				"C11105",
				"C11095",
				"C52002",
				"C32051",
				"C37004",
				"C44045",
				"C09087",
				"C09100",
				"C32058",
				"C11106",
				"C32048",
				"C11110",
				"C32040",
				"C11109",
				"C09125",
				"C32052",
				"C09159",
				"C09160",
				"C11005",
				"C09169",
				"C09170",
				"C09174",
				"C09182",
				"C09184",
				"C09189",
				"C09190",
				"C09130",
				"C09134",
				"C11073",
				"C33002",
				"C00162",
				"C09150",
				"C09162",
				"C09164",
				"C09166",
				"C09175",
				"C09176",
				"C09188",
				"C09148",
				"C09151",
				"C09165",
				"C09178",
				"C09183",
				"C09191",
				"C09197",
				"C09198",
				"C09200",
				"C09201",
				"C32053",
				"C09128",
				"C09129",
				"C09126",
				"C09133",
				"C09136",
				"C09138",
				"C09140",
				"C09141",
				"C09149",
				"C09153",
				"C11040",
				"C09147",
				"C09154",
				"C09155",
				"C09157",
				"C09158",
				"C09161",
				"C09163",
				"C09167",
				"C09177",
				"C09179",
				"C09180",
				"C11163",
				"C09181",
				"C09192",
				"C09143",
				"C09152",
				"C09168",
				"C09171",
				"C09172",
				"C09173",
				"C09185",
				"C09186",
				"C09187",
				"C09193",
				"C09194",
				"C09202",
				"C09212",
				"C09225",
				"C09226",
				"C09237",
				"C09238",
				"C09240",
				"C09242",
				"C09196",
				"C09209",
				"C09210",
				"C09229",
				"C09247",
				"C09254",
				"C09256",
				"C09203",
				"C09204",
				"C09195",
				"C09219",
				"C09227",
				"C09234",
				"C09241",
				"C09221",
				"C09230",
				"C09231",
				"C09232",
				"C09235",
				"C09246",
				"C09211",
				"C09213",
				"C09223",
				"C09233",
				"C09236",
				"C09207",
				"C09208",
				"C09216",
				"C09217",
				"C09218",
				"C09224",
				"C09156",
				"C09199",
				"C09214",
				"C09220",
				"C09222",
				"C09228",
				"C09245",
				"C09248",
				"C09205",
				"C09206",
				"C09215",
				"C09239",
				"C09250",
				"C09251",
				"C09253",
				"C09269",
				"C09295",
				"C09297",
				"C09299",
				"C09300",
				"C09304",
				"C09274",
				"C09275",
				"C09276",
				"C09277",
				"C09243",
				"C09278",
				"C09260",
				"C09261",
				"C09263",
				"C09270",
				"C09272",
				"C09244",
				"C09258",
				"C09267",
				"C09252",
				"C09281",
				"C09249",
				"C09257",
				"C09264",
				"C09255",
				"C09271",
				"C09290",
				"C09293",
				"C09294",
				"C09266",
				"C09268",
				"C09279",
				"C09280",
				"C09273",
				"C09282",
				"C09292",
				"C09259",
				"C09283",
				"C09284",
				"C09285",
				"C09286",
				"C09262",
				"C09287",
				"C09289",
				"C09323",
				"C09325",
				"C09320",
				"C09330",
				"C09334",
				"C09307",
				"C09312",
				"C09303",
				"C09339",
				"C09347",
				"C09348",
				"C09265",
				"C09316",
				"C09317",
				"C09291",
				"C09352",
				"C09353",
				"C09288",
				"C09322",
				"C09333",
				"C09340",
				"C09341",
				"C09343",
				"C09311",
				"C09319",
				"C09328",
				"C09329",
				"C09346",
				"C09350",
				"C09298",
				"C09302",
				"C09318",
				"C09326",
				"C09332",
				"C09336",
				"C09306",
				"C09315",
				"C09321",
				"C09324",
				"C09331",
				"C09354",
				"C09305",
				"C09296",
				"C09308",
				"C09310",
				"C09313",
				"C09314",
				"C09369",
				"C09371",
				"C09375",
				"C09378",
				"C09386",
				"C09387",
				"C09381",
				"C09355",
				"C09356",
				"C09338",
				"C09345",
				"C09367",
				"C09351",
				"C09342",
				"C09357",
				"C09370",
				"C09344",
				"C09373",
				"C09376",
				"C09358",
				"C09360",
				"C09372",
				"C09379",
				"C09383",
				"C09337",
				"C09361",
				"C09335",
				"C09363",
				"C09377",
				"C09384",
				"C09390",
				"C09349",
				"C09362",
				"C09364",
				"C09368",
				"C09389",
				"C09359",
				"C09365",
				"C09366",
				"C09382",
				"C09327",
				"C09385",
				"C09392",
				"C09395",
				"C09401",
				"C09422",
				"C09425",
				"C09426",
				"C09427",
				"C09429",
				"C09388",
				"C09417",
				"C09431",
				"C09432",
				"C09435",
				"C09397",
				"C09404",
				"C09405",
				"C09419",
				"C09428",
				"C09423",
				"C09420",
				"C09433",
				"C09437",
				"C09424",
				"C09418",
				"C09396",
				"C09402",
				"C09309",
				"C09374",
				"C09403",
				"C09391",
				"C09406",
				"C09409",
				"C09412",
				"C09416",
				"C09400",
				"C09301",
				"C09411",
				"C09413",
				"C09414",
				"C09415",
				"C09393",
				"C09394",
				"C09398",
				"C09380",
				"C09408",
				"C09410",
				"C09441",
				"C09430",
				"C09450",
				"C09464",
				"C09467",
				"C09468",
				"C09472",
				"C09399",
				"C09445",
				"C09452",
				"C09458",
				"C09436",
				"C09462",
				"C09442",
				"C09438",
				"C09440",
				"C09454",
				"C09455",
				"C09477",
				"C09478",
				"C09443",
				"C09434",
				"C09447",
				"C09453",
				"C09456",
				"C09459",
				"C09439",
				"C09449",
				"C09451",
				"C09460",
				"C09461",
				"C09463",
				"C09465",
				"C09446",
				"C09448",
				"C09457",
				"C09471",
				"C09474",
				"C09475",
				"C09476",
				"C09421",
				"C09444",
				"C09407",
				"C09469",
				"C09470",
				"C09489",
				"C09493",
				"C09497",
				"C09508",
				"C09517",
				"C09535",
				"C09482",
				"C09486",
				"C09491",
				"C09495",
				"C09503",
				"C09510",
				"C09479",
				"C09511",
				"C09513",
				"C09525",
				"C09528",
				"C09529",
				"C09483",
				"C09488",
				"C09490",
				"C09492",
				"C09494",
				"C09506",
				"C09501",
				"C09481",
				"C09504",
				"C09514",
				"C09518",
				"C09530",
				"C09531",
				"C09466",
				"C09480",
				"C09498",
				"C09500",
				"C09532",
				"C09533",
				"C09534",
				"C09484",
				"C09496",
				"C09499",
				"C09502",
				"C09505",
				"C09507",
				"C09512",
				"C09515",
				"C09516",
				"C09520",
				"C09522",
				"C09526",
				"C09527",
				"C09523",
				"C09539",
				"C09548",
				"C09551",
				"C09554",
				"C09555",
				"C09536",
				"C09537",
				"C09519",
				"C09541",
				"C09485",
				"C09546",
				"C09549",
				"C09543",
				"C09509",
				"C09553",
				"C09556",
				"C09557",
				"C09559",
				"C09524",
				"C09547",
				"C09542",
				"C09545",
				"C09487",
				"C09550",
				"C09558",
				"C09544",
				"C09521",
				"C09552"



		};
		
		ip = "180.96.63.70";
		MongoConn.importNewCompany2(ip, cps);
		/*
		
		MongoConn mongo = new MongoConn();
		mongo.setIp(ip);
		mongo.setPort(27017);
		mongo.setDbName("tc4");
		mongo.setUser("keel");
		mongo.setPwd("jsGame_1810");
		if (mongo.init()) {
			
			
			HashMap<String,Object> np = new HashMap<String, Object>();
			mongo.newGroupMerge("#华为C8500","#中兴N600",np);
			mongo.newGroupMerge("#三星I559","#中兴N606",np);
			mongo.newGroupMerge("#三星I579","#酷派5855",np);
			mongo.newGroupSplit( "#酷派5860","5860",1341, "#三星I909MR", np);
			mongo.newGroupSplit( "#华为C8812", "C8812",1320,"#三星I909MR", np);
			System.out.println("adjust OK.");
			
			
//			DBCollection coll = mongo.getColl("TCPhone");
//			coll.ins(new BasicDBObject("name","#中兴N606"));
//			coll.remove(new BasicDBObject("name","#中兴N600"));
//			coll.remove(new BasicDBObject("name","#酷派5855"));
		}
		*/
		//cc(mongo);
		/*
//		MongoConn.changeTestUserName(mongo,"鞠云", "刘静");
//		MongoConn.changeTestUserName(mongo,"朱敏", "徐敏");
//		MongoConn.changeTestUserName(mongo,"李德明", "施奇");
//		MongoConn.changeTestUserName(mongo,"张桃", "沙宛龙");
//		MongoConn.changeTestUserName(mongo,"肖航", "朱军");
//		MongoConn.changeTestUserName(mongo,"王浩浩", "刘婷");
//		MongoConn.changeTestUserName(mongo,"熊克松", "傅雪芳");
//		MongoConn.changeTestUserName(mongo,"徐尧", "郭蓬飞");
//		MongoConn.changeTestUserName(mongo,"陈俊", "陈明锐");
//		MongoConn.changeTestUserName(mongo,"朱跃", "汤琴");
//		MongoConn.changeTestUserName(mongo,"杨玉勤", "洪泓");
		MongoConn.changeTestUserName(mongo,"洪泓", "洪鸿");
		
		mongo.close();*/
//		MongoConn.updatePGroup(ip);
		mongo.close();
		System.out.println("--------end------");
		
	/*	
		//test for mongolab.com test
		MongoConn mongo = new MongoConn();
//		mongo.setIp("127.0.0.1");
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
		}*/
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
				log.info("=========== mongoConn init OK!["+this.dbName+"] ============");
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
