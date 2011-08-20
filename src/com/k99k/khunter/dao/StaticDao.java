/**
 * 
 */
package com.k99k.khunter.dao;


import org.apache.log4j.Logger;
import com.k99k.khunter.DaoInterface;
import com.k99k.khunter.DaoManager;
import com.k99k.khunter.DataSourceInterface;
import com.k99k.khunter.KObject;
import com.k99k.khunter.MongoDao;

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
	
	public static final void initS(){
		tcUserDao = DaoManager.findDao("TCUserDao");
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
