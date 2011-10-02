/**
 * 
 */
package com.k99k.testcenter;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

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
import com.k99k.tools.JSON;
import com.k99k.tools.StringUtil;

/**
 * 测试任务
 * @author keel
 *
 */
public class TTask extends Action {

	/**
	 * @param name
	 */
	public TTask(String name) {
		super(name);
	}
	static final Logger log = Logger.getLogger(TTask.class);
	static DaoInterface dao;
	static KObjSchema schema;

	/* (non-Javadoc)
	 * @see com.k99k.khunter.Action#act(com.k99k.khunter.ActionMsg)
	 */
	@Override
	public ActionMsg act(ActionMsg msg) {
		HttpActionMsg httpmsg = (HttpActionMsg)msg;
		HttpServletRequest req = httpmsg.getHttpReq();
		String subact = KFilter.actPath(msg, 2, "");
		KObject u = Auth.checkCookieLogin(httpmsg);
		if (u == null) {
			msg.addData("[redirect]", "/login");
			return super.act(msg);
		}
		if (subact.equals("")) {
			this.list(subact,req, u, httpmsg);
		}else if (subact.equals("my")) {
			String subact2 = KFilter.actPath(msg, 3, "");
			if (subact2.equals("")) {
				this.list(subact,req, u, httpmsg);
			}else if(subact2.equals("a_s")){
				this.search(subact,req, u, httpmsg);
			}
		}else if(subact.equals("add")){
			msg.addData("sub", subact);
			this.toAdd(u, httpmsg);
		}else if (StringUtil.isDigits(subact)) {
			this.one(subact, req, u, httpmsg);
		}else if(subact.equals("a_a")){
			this.add(req, u, httpmsg);
		}else if(subact.equals("a_p")){
			this.appoint(req, u, httpmsg);
		}else if(subact.equals("a_exec")){
			this.exec(req, u, httpmsg);
		}else if(subact.equals("a_send")){
			this.send(req, u, httpmsg);
		}else if(subact.equals("a_comm")){
			this.comm(req, u, httpmsg);
		}else if(subact.equals("a_u")){
			this.update(req, u, httpmsg);
		}else if(subact.equals("a_d")){
			this.del(req, u, httpmsg);
		}else if(subact.equals("a_confirm")){
			this.confirm(req, u, httpmsg);
		}else if(subact.equals("a_s")){
			this.search(subact,req, u, httpmsg);
		}else{
			JOut.err(404, httpmsg);
		}
		return super.act(msg);
	}
	
	/**
	 * 指派任务,确定测试机型,生成TestUnit,确定执行人,调整等级,说明等
	 * @param req
	 * @param u
	 * @param msg
	 */
	@SuppressWarnings("unchecked")
	private void appoint(HttpServletRequest req,KObject u,HttpActionMsg msg){
		//验证权限
		if (Integer.parseInt(u.getType()) < 4) {
			//权限不够
			JOut.err(401,"E401"+Err.ERR_AUTH_FAIL, msg);
			return;
		}
		//
		String task_id = req.getParameter("tid");
		String task_info = req.getParameter("task_info");
		String task_level = req.getParameter("task_level");
		String task_operator = req.getParameter("task_operator");
		String task_tu_json_h = req.getParameter("task_tu_json_h");
		//验证
		if(!StringUtil.isDigits(task_id) ||
			!StringUtil.isStringWithLen(task_info, 1) || 
			!StringUtil.isDigits(task_level) ||
			!StringUtil.isStringWithLen(task_operator, 2) || 
			!StringUtil.isStringWithLen(task_tu_json_h, 3) 
		){
			JOut.err(403,"E403"+Err.ERR_PARAS, msg);
			return;
		}
		long tid = Long.parseLong(task_id);
		KObject task = dao.findOne(tid);
		if (task==null) {
			JOut.err(401,"E401"+Err.ERR_PARAS, msg);
			return;
		}
		KObject operator = TUser.dao.findOne(task_operator);
		if (operator== null || Integer.parseInt(operator.getType())<1) {
			JOut.err(403,"E403"+ Err.ERR_ADD_OPERATOR_FAIL, msg);
			return;
		}
		int level = Integer.parseInt(task_level);
		//生成TestUnit
		try {
			ArrayList<HashMap<String,Object>> json = (ArrayList<HashMap<String,Object>>) JSON.read(task_tu_json_h);
			Iterator<HashMap<String,Object>> it = json.iterator();
			KObject tu = new KObject();
			tu.setProp("TID", tid);
			tu.setProp("PID", task.getProp("PID"));
			tu.setProp("tester", operator.getName());
			tu.setInfo(task_info);
			tu.setLevel(level);
			tu.setCreatorName(u.getName());
			while (it.hasNext()) {
				HashMap<String,Object> map = it.next();
				String gFile = map.get("gFile").toString();
				long fileId = Long.parseLong(map.get("fileId").toString());
				ArrayList<String> phList = (ArrayList<String>)map.get("phone");
				Iterator<String> li = phList.iterator();
				tu.setProp("gFile", gFile);
				tu.setProp("fileId", fileId);
				while (li.hasNext()) {
					String ph = li.next();
					tu.setProp("phone", ph);
					if(!TestUnit.dao.add(tu)){
						JOut.err(500,"E500"+ Err.ERR_DB_UPDATE, msg);
						return;
					}
				}
			}
		} catch (Exception e) {
			JOut.err(403,"E403"+ Err.ERR_ADD_TESTUNIT, msg);
			return;
		}
		//更新任务属性
		
		HashMap<String,Object> q = new HashMap<String, Object>(2);
		q.put("_id", tid);
		HashMap<String,Object> set = new HashMap<String, Object>();
		set.put("level", level);
		set.put("operator", task_operator);
		set.put("state", 1);
		HashMap<String,Object> logmsg = new HashMap<String, Object>();
		logmsg.put("time", System.currentTimeMillis());
		logmsg.put("user", u.getName());
		logmsg.put("info", "分配任务 - "+task_info);
		HashMap<String,Object> push = new HashMap<String, Object>(2);
		push.put("log", logmsg);
		HashMap<String,Object> update = new HashMap<String, Object>();
		update.put("$set", set);
		update.put("$push", push);
		if(dao.update(q, update, false, false)){
			//清除自己待办任务,指定为一下执行人
			ActionMsg atask = new ActionMsg("tTaskTask");
			atask.addData(TaskManager.TASK_TYPE, TaskManager.TASK_TYPE_EXE_POOL);
			atask.addData("tid", tid);
			atask.addData("oid", operator.getId());
			atask.addData("uid", u.getId());
			atask.addData("act", "appoint");
			TaskManager.makeNewTask("TTaskTask:"+tid, atask);
			msg.addData("[print]", task.getId());
		}else{
			log.error("appoint task faild:"+tid);
			JOut.err(500,"E500"+ Err.ERR_DB_UPDATE, msg);
			return;
		}
		
		
	}
	
	/**
	 * 转发TestUnit,增加说明
	 * @param req
	 * @param u
	 * @param msg
	 */
	private void send(HttpServletRequest req,KObject u,HttpActionMsg msg){
		//
	}
	
	/**
	 * 执行任务,测试项给结果,给评价,问题归总
	 * @param req
	 * @param u
	 * @param msg
	 */
	private void exec(HttpServletRequest req,KObject u,HttpActionMsg msg){
		//
	}
	
	/**
	 * 任务讨论,注意一个产品对应一个主题
	 * @param req
	 * @param u
	 * @param msg
	 */
	private void comm(HttpServletRequest req,KObject u,HttpActionMsg msg){
		//
	}
	
	/**
	 * 任务确认,退回或确认结果并通知任务创建者
	 * @param req
	 * @param u
	 * @param msg
	 */
	private void confirm(HttpServletRequest req,KObject u,HttpActionMsg msg){
		//
	}
	
	/**
	 * 更新任务,改变状态(删除,暂停,取消等),调整执行人,增加说明,修改TestUnit
	 * @param req
	 * @param u
	 * @param msg
	 */
	private void update(HttpServletRequest req,KObject u,HttpActionMsg msg){
		//
	}
	
	/**
	 * 删除任务
	 * @param req
	 * @param u
	 * @param msg
	 */
	private void del(HttpServletRequest req,KObject u,HttpActionMsg msg){
		if (Integer.parseInt(u.getType()) < 4) {
			//权限不够
			JOut.err(401, msg);
			return;
		}
		if (StringUtil.isDigits(req.getParameter("id"))) {
			long id = Long.parseLong(req.getParameter("id"));
			if (dao.deleteOne(id) !=null) {
				ActionMsg atask = new ActionMsg("tTaskTask");
				atask.addData(TaskManager.TASK_TYPE, TaskManager.TASK_TYPE_EXE_POOL);
				atask.addData("taskId", id);
				atask.addData("act", "del");
				TaskManager.makeNewTask("TTaskTask:"+id, atask);
				msg.addData("[print]", "ok");
				return;
			}
		}
		JOut.err(403, msg);
	}
	/**
	 * 处理任务添加
	 * @param req
	 * @param u
	 * @param msg
	 */
	@SuppressWarnings("unchecked")
	private void add(HttpServletRequest req,KObject u,HttpActionMsg msg){
		String task_info = req.getParameter("task_info");
		String task_level = req.getParameter("task_level");
		String task_operator = req.getParameter("task_operator");
		String task_p_json_h = req.getParameter("task_p_json_h");
		String task_type_h = req.getParameter("task_type_h");
		//验证
		if(!StringUtil.isStringWithLen(task_info, 1) || 
			!StringUtil.isDigits(task_level) ||
			!StringUtil.isDigits(task_type_h) ||
			!StringUtil.isStringWithLen(task_operator, 2) || 
			!StringUtil.isStringWithLen(task_p_json_h, 5) 
		){
			JOut.err(403,"E403"+Err.ERR_PARAS, msg);
			return;
		}
		HashMap<String,Object> json = (HashMap<String, Object>) JSON.read(task_p_json_h);
		if (Company.dao.findOne(json.get("company").toString()) == null) {
			JOut.err(403,"E403"+Err.ERR_PARAS+"-company", msg);
			return;
		}
		//创建产品确定PID
		long pid = -10;
		if (!json.containsKey("_id")) {
			pid = Product.add(json);
			if(pid<0){
				JOut.err(403,"E403"+ Err.ERR_ADD_PRODUCT_FAIL+pid, msg);
				return;
			}
		}else{
			pid = Long.parseLong(String.valueOf(json.get("_id")));
		}
		//创建任务
		KObject operator = TUser.dao.findOne(task_operator);
		if (operator== null || Integer.parseInt(operator.getType())<1) {
			JOut.err(403,"E403"+ Err.ERR_ADD_OPERATOR_FAIL, msg);
			return;
		}
		KObject task = new KObject();
		task.setName((String)json.get("name"));
		task.setCreatorName(u.getName());
		task.setInfo(task_info);
		task.setProp("operator", task_operator);
		task.setLevel(Integer.parseInt(task_level));
		task.setProp("PID", pid);
		task.setType(task_type_h);
		HashMap<String,Object> log = new HashMap<String, Object>();
		log.put("time", System.currentTimeMillis());
		log.put("user", u.getName());
		log.put("info", "创建任务 - "+task_info);
		Object[] logs = new Object[]{log};
		task.setProp("log", logs);
		task.setId(dao.getIdm().nextId());
		if(!dao.save(task)){
			JOut.err(500,"E500"+ Err.ERR_ADD_TASK_FAIL, msg);
			return;
		}
		//异步任务:任务通知,上传文件处理
		ActionMsg atask = new ActionMsg("tTaskTask");
		atask.addData(TaskManager.TASK_TYPE, TaskManager.TASK_TYPE_EXE_POOL);
		atask.addData("taskId", task.getId());
		atask.addData("operatorId", operator.getId());
		atask.addData("pid", pid);
		atask.addData("creatorName", u.getName());
		atask.addData("act", "add");
		if (json.containsKey("files")) {
			atask.addData("files", json.get("files"));
		}
		TaskManager.makeNewTask("TTaskTask:"+task.getId(), atask);
		msg.addData("[print]", task.getId());
	}
	
	
	
	
	private void search(String subact,HttpServletRequest req,KObject u,HttpActionMsg msg){
		if (StringUtil.isStringWithLen(req.getParameter("k"), 1)) {
			String key = null;
			try {
				//TODO 针对tomcatURL编码转换
				key = new String(req.getParameter("k").getBytes("ISO-8859-1"),"utf-8").trim();
			} catch (UnsupportedEncodingException e) {
			}
			HashMap<String,Object> query = new HashMap<String, Object>(2);
			Pattern p = Pattern.compile(key);
			query.put("name", p);
			query.putAll(StaticDao.prop_state_normal);
			this.queryPage(query,subact, req, u, msg);
			return;
		}else{
			JOut.err(401, msg);
		}
	}
	
	/**
	 * 转到增加页
	 * @param u
	 * @param msg
	 */
	private void toAdd(KObject u,HttpActionMsg msg){
		if (Integer.parseInt(u.getType()) < 1) {
			//权限不够
			JOut.err(401, msg);
			return;
		}
		msg.addData("u", u);
		msg.addData("[jsp]", "/WEB-INF/tc/task_add.jsp");
	}
	
	/**
	 * 查看单个任务
	 * @param subact
	 * @param req
	 * @param u
	 * @param msg HttpActionMsg
	 */
	private void one(String subact,HttpServletRequest req,KObject u,HttpActionMsg msg){
		long id = Long.parseLong(subact);
		//Task
		KObject one = dao.findOne(id);
		if (one== null || one.getState() == -1) {
			JOut.err(404, msg);
			return;
		}
		//产品
		long pid = (Long)one.getProp("PID");
		KObject product = Product.dao.findOne(pid);
		msg.addData("u", u);
		msg.addData("one", one);
		msg.addData("product", product);
		//Task的状态处于待分配(已创建)
		if (one.getState()==0) {
			//显示待分配的文件或URL
			int sys = Integer.parseInt(product.getProp("sys").toString());
			if (sys!=2) {
				HashMap<String,Object> q = new HashMap<String, Object>();
				q.put("TID", one.getId());
				ArrayList<KObject> files = GameFile.dao.queryKObj(q, null, null, 0, 0, null);
				msg.addData("files", files);
			}
		}else {
			//查找本Task所属的TestUnit
			HashMap<String,Object> q = new HashMap<String, Object>(2);
			q.put("TID", one.getId());
			ArrayList<KObject> tus = TestUnit.dao.queryKObj(q, null, MongoDao.prop_id_desc, 0, 0, null);
			msg.addData("tus", tus);
		}
		
		//转到编辑时判断权限:是否为任务创建者或type>=4
		if (req.getParameter("edit")!=null && (u.getName().equals(one.getCreatorName()) || Integer.parseInt(u.getType())>=4)) {
			msg.addData("[jsp]", "/WEB-INF/tc/task_edit.jsp");
		}else{
			msg.addData("[jsp]", "/WEB-INF/tc/task_one.jsp");
		}
	}
	
	
	/**
	 * 查看列表
	 * FIXME 需要权限验证和分出一个查看自己任务的方法
	 * @param subact
	 * @param req
	 * @param u
	 * @param msg
	 */
	private void list(String subact,HttpServletRequest req,KObject u,HttpActionMsg msg){
		this.queryPage(StaticDao.prop_state_normal,subact, req, u, msg);
	}
	
	/**
	 * 按条件查看列表
	 * @param query
	 * @param subact
	 * @param req
	 * @param u
	 * @param msg
	 */
	private void queryPage(HashMap<String,Object> query,String subact,HttpServletRequest req,KObject u,HttpActionMsg msg){
		String p_str = req.getParameter("p");
		String pz_str = req.getParameter("pz");
		int page = StringUtil.isDigits(p_str)?Integer.parseInt(p_str):1;
		int pz = StringUtil.isDigits(pz_str)?Integer.parseInt(pz_str):this.pageSize;
		ArrayList<KObject> list = dao.queryByPage(page,pageSize,query, null, StaticDao.prop_level_id_desc, null);
		msg.addData("u", u);
		msg.addData("list", list);
		msg.addData("pz", pz);
		msg.addData("p", page);
		msg.addData("sub", subact);
		msg.addData("[jsp]", "/WEB-INF/tc/tasks.jsp");
	}
	
	/* (non-Javadoc)
	 * @see com.k99k.khunter.Action#init()
	 */
	@Override
	public void init() {
		dao = DaoManager.findDao("TCTaskDao");
		schema = KObjManager.findSchema("TCTask");
		super.init();
	}

	private int pageSize = 30;

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
