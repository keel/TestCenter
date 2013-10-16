/**
 * 
 */
package com.k99k.testcenter;

import org.apache.log4j.Logger;

import com.k99k.khunter.Action;
import com.k99k.khunter.ActionMsg;
import com.k99k.tools.SendMail;
import com.k99k.tools.StringUtil;

/**
 * 处理邮件发送任务的Task,要求dests(String[]),subject,content
 * @author keel
 *
 */
public class EMail extends Action {

	/**
	 * @param name
	 */
	public EMail(String name) {
		super(name);
	}
	static final Logger log = Logger.getLogger(EMail.class);
	private SendMail mail = new SendMail();
	private Thread t;
	
	private String server;
	private String from;
	private int port;
	private String user;
	private String pwd;

	/* (non-Javadoc)
	 * @see com.k99k.khunter.Action#act(com.k99k.khunter.ActionMsg)
	 */
	@Override
	public ActionMsg act(ActionMsg msg) {
		String[] dests = (String[])msg.getData("dests");
		String content = msg.getData("content").toString();
		String subject = msg.getData("subject").toString();
		if (dests==null || dests.length==0 || !StringUtil.isStringWithLen(subject, 2) || !StringUtil.isStringWithLen(content, 3)) {
			log.error("Email paras err,not send. dests:"+dests+" subject:"+subject);
			return super.act(msg);
		}
		for (int i = 0; i < dests.length; i++) {
			if (StringUtil.isStringWithLen(dests[i], 3)) {
				//##暂停邮件提醒
				//log.error("暂停邮件提醒");
				this.mail.addTask(dests[i], subject, content);
			}else{
				log.error("Email adress is empty,not send. subject:"+subject);
			}
		}
		return super.act(msg);
	}

	/* (non-Javadoc)
	 * @see com.k99k.khunter.Action#exit()
	 */
	@Override
	public void exit() {
		this.mail.setRunFlag(false);
		t.interrupt();
		super.exit();
	}

	/* (non-Javadoc)
	 * @see com.k99k.khunter.Action#init()
	 */
	@Override
	public void init() {
		super.init();
		this.mail.init(this.server, this.from, this.port, this.user, this.pwd);
		t = new Thread(this.mail,"EMAIL");
		t.start();
	}

	/**
	 * @return the server
	 */
	public final String getServer() {
		return server;
	}

	/**
	 * @param server the server to set
	 */
	public final void setServer(String server) {
		this.server = server;
	}

	/**
	 * @return the from
	 */
	public final String getFrom() {
		return from;
	}

	/**
	 * @param from the from to set
	 */
	public final void setFrom(String from) {
		this.from = from;
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



}
