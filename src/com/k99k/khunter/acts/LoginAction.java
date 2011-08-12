/**
 * 
 */
package com.k99k.khunter.acts;

import com.k99k.khunter.Action;
import com.k99k.khunter.ActionMsg;
import com.k99k.khunter.HTUser;
import com.k99k.khunter.TaskManager;
import com.k99k.khunter.dao.MongoUserDao;

/**
 * 登录Action
 * @author keel
 *
 */
public class LoginAction extends Action {

	/**
	 * @param name
	 */
	public LoginAction(String name) {
		super(name);
	}
	
	private MongoUserDao userDao;

	/**
	 * FIXME 执行登录操作,若为新用户则自动注册
	 * @see com.k99k.khunter.Action#act(com.k99k.khunter.ActionMsg)
	 */
	@Override
	public ActionMsg act(ActionMsg msg) {
		
		//FIXME 实现login操作
		
		//从数据源获取用户信息
		
		//如果数据源无此用户数据,则进行注册操作
		
		//有此用户数据则进行验证
		
		//同步用户数据,根据时间计算用户当前状态
		//体力值,由时间计算存储值和当前值
		//判断是否有消息(战斗，采集)
		//状态和位置是否更新
		//宠物的状态及信息
		
		//结果为成功和失败两种,成功则直接返回,失败也可直接返回或转到处理失败的Action
		
		
		msg.addData("something", "nothing");
		msg.addData("dao", this.userDao.getName());
		msg.addData("dataSource", this.userDao.getDataSource().getName());
		//TODO coll传递问题不在mongodb中
		HTUser user =  this.userDao.findUser(2);

		
		//创建Task方式:直接新建ActionMsg,一般推荐此用法,以免后面的action改变msg的相关值
		ActionMsg logMsg = new ActionMsg("log",8);
		//logMsg.addData(TaskManager.TASK_TYPE, TaskManager.TASK_TYPE_EXE_POOL);
		
		logMsg.addData(TaskManager.TASK_TYPE, TaskManager.TASK_TYPE_SCHEDULE_POOL);
		logMsg.addData(TaskManager.TASK_DELAY, 5000);
		logMsg.addData(TaskManager.TASK_CANCEL, false);
		logMsg.addData("user", user.toString());
		boolean taskBuilt =TaskManager.makeNewTask("logMsg#1", logMsg);
		msg.addData("taskBuilt#log", taskBuilt);
		
		//错误的创建Task方式:更改现有msg的ActionName,后面的操作中会改变msg的相关值,可能引发错误!
//		msg.setActitonName("log");
//		msg.addData(TaskManager.TASK_TYPE, TaskManager.TASK_TYPE_EXE_POOL);
//		TaskManager.makeNewTask("logMsg#2", msg);

		
		//Action的第一个用法:串连
		//msg.setNextAction(ActionManager.findAction("log"));
		
		//Action的第二个用法:直接调用其act方法
		//Action a = ActionManager.findAction("log");
		//a.act(msg);
		
		//--------------
		//输出
		//--------------
		//直接print
		msg.addData("[print]",msg.toString());
		
		//jsp方式输出
//		msg.addData("[jsp]", "/WEB-INF/test.jsp");
//		msg.addData("jspAttr", user);
		
		
		return super.act(msg);
	}

	/**
	 * @return the userDao
	 */
	public final MongoUserDao getUserDao() {
		return userDao;
	}

	/**
	 * @param userDao the userDao to set
	 */
	public final void setUserDao(MongoUserDao userDao) {
		this.userDao = userDao;
	}

	@Override
	public void exit() {
		this.userDao = null;
	}

	@Override
	public String getIniPath() {
		return null;
	}

	@Override
	public void init() {
		
	}
	
	

}
