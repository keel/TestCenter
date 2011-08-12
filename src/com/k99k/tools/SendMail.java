/**
 * 
 */
package com.k99k.tools;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.Logger;

/**
 * Email发送客户端,使用javamail实现,需要log4j支持.可作为线程运行或单独执行发送.<br />
 * 单发：首先对发送方信息进行初始化init(),然后执行sendMail即可发送。<br />
 * 线程：首先init().然后run(),通过addTask()添加发送任务。setSleep可控制任务间隔时间.
 * @author keel
 * 
 */
public class SendMail implements Runnable {

	/**
	 * 
	 */
	public SendMail() {
	}

	static final Logger log = Logger.getLogger(SendMail.class);

	private String from;
	private String server;
	private int port;

	private String user;
	private String pwd;

	// Create properties for the Session
	private Properties props;
	
	private int sleep = 1000;

	// Get a session
	// private Session session;

	/**
	 * 发送队列,内容项为String[]{to,subject,txt}
	 */
	private CopyOnWriteArrayList<String[]> sendList = new CopyOnWriteArrayList<String[]>();

	/**
	 * 运行标识，只有在init后才为true
	 */
	private boolean runFlag = false;

	public static void main(String[] args) {
		SendMail mail = new SendMail();
		mail.init("smtp.126.com", "keelsike@126.com", 25, "xxx", "xxx");
		Thread t = new Thread(mail);
		t.start();
		mail.addTask("keel.sike@gmail.com", "这就是just for me", "内容文本, http://www.163.com");
	}
	
	/**
	 * 初始化,发送方的相关信息
	 * @param SMTPServer
	 * @param from
	 * @param port
	 * @param user
	 * @param pwd
	 */
	public void init(String SMTPServer, String from, int port, String user,
			String pwd) {
		this.server = SMTPServer;
		this.from = from;
		this.port = port;
		this.user = user;
		this.pwd = pwd;
		props = new Properties();
		// If using static Transport.send(),
		// need to specify the mail server here
		props.put("mail.smtp.host", server);
		props.put("mail.smtp.auth", "true");
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.port", port + "");

		// To see what is going on behind the scene
		//debug模式开启
		//props.put("mail.debug", "true");

		// session = Session.getInstance(props);
		runFlag = true;
	}

	/**
	 * 发送一封邮件
	 * @param to 接收方邮件地址
	 * @param subject 邮件标题
	 * @param txt 邮件正文
	 */
	public final void sendMail(String to, String subject, String txt) {
		try {

			Session session = Session.getInstance(props);

			// Instantiate a message
			Message msg = new MimeMessage(session);

			// Set message attributes
			msg.setFrom(new InternetAddress(from));
			InternetAddress[] address = { new InternetAddress(to) };
			msg.setRecipients(Message.RecipientType.TO, address);
			// Parse a comma-separated list of email addresses. Be strict.
			//msg.setRecipients(Message.RecipientType.CC, InternetAddress.parse(to, true));
			// Parse comma/space-separated list. Cut some slack.
			//msg.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(to, false));

			msg.setSubject(subject);
			msg.setSentDate(new Date());

			// Set message content and send
			msg.setText(txt);
			msg.saveChanges();

			// Get a Transport object to send e-mail
			Transport bus = session.getTransport("smtp");
			bus.connect(this.server, user, pwd);
			bus.sendMessage(msg, address);
			bus.close();
			log.info("Mail sent to:[" + to + "][" + subject + "]");
		} catch (Exception e) {
			log.error("Mail sent failed. to:[" + to + "][" + subject + "]", e);
		}
	}
	
	/**
	 * 加入发送任务
	 * @param to 对方邮件地址
	 * @param subject 邮件标题
	 * @param txt 邮件正文
	 */
	public final void addTask(String to,String subject,String txt){
		String[] task = new String[]{to,subject,txt};
		this.sendList.add(task);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		while (runFlag) {
			for (Iterator<String[]> it = this.sendList.iterator(); it.hasNext() && runFlag;) {
				String[] task = it.next();
				if (task.length == 3) {
					this.sendMail(task[0], task[1], task[2]);
				}
				this.sendList.remove(task);
			}
			
			try {
				Thread.sleep(sleep);
			} catch (InterruptedException e) {
			}
		}
	}

	// A simple multipart/mixed e-mail. Both body parts are text/plain.
	public static void setMultipartContent(Message msg)
			throws MessagingException {
		// Create and fill first part
		MimeBodyPart p1 = new MimeBodyPart();
		p1.setText("This is part one of a test multipart e-mail.");

		// Create and fill second part
		MimeBodyPart p2 = new MimeBodyPart();
		// Here is how to set a charset on textual content
		p2.setText("This is the second part", "us-ascii");

		// Create the Multipart. Add BodyParts to it.
		Multipart mp = new MimeMultipart();
		mp.addBodyPart(p1);
		mp.addBodyPart(p2);

		// Set Multipart as the message's content
		msg.setContent(mp);
	}

	// Set a file as an attachment. Uses JAF FileDataSource.
	public static void setFileAsAttachment(Message msg, String filename)
			throws MessagingException {

		// Create and fill first part
		MimeBodyPart p1 = new MimeBodyPart();
		p1.setText("This is part one of a test multipart e-mail."
				+ "The second part is file as an attachment");

		// Create second part
		MimeBodyPart p2 = new MimeBodyPart();

		// Put a file in the second part
		FileDataSource fds = new FileDataSource(filename);
		p2.setDataHandler(new DataHandler(fds));
		p2.setFileName(fds.getName());

		// Create the Multipart. Add BodyParts to it.
		Multipart mp = new MimeMultipart();
		mp.addBodyPart(p1);
		mp.addBodyPart(p2);

		// Set Multipart as the message's content
		msg.setContent(mp);
	}

	// Set a single part html content.
	// Sending data of any type is similar.
	public static void setHTMLContent(Message msg) throws MessagingException {

		String html = "<html><head><title>" + msg.getSubject()
				+ "</title></head><body><h1>" + msg.getSubject()
				+ "</h1><p>This is a test of sending an HTML e-mail"
				+ " through Java.</body></html>";

		// HTMLDataSource is an inner class
		msg.setDataHandler(new DataHandler(new HTMLDataSource(html)));
	}

	/*
	 * Inner class to act as a JAF datasource to send HTML e-mail content
	 */
	static class HTMLDataSource implements DataSource {
		private String html;

		public HTMLDataSource(String htmlString) {
			html = htmlString;
		}

		// Return html string in an InputStream.
		// A new stream must be returned each time.
		public InputStream getInputStream() throws IOException {
			if (html == null)
				throw new IOException("Null HTML");
			return new ByteArrayInputStream(html.getBytes());
		}

		public OutputStream getOutputStream() throws IOException {
			throw new IOException("This DataHandler cannot write HTML");
		}

		public String getContentType() {
			return "text/html";
		}

		public String getName() {
			return "JAF text/html dataSource to send e-mail only";
		}
	}

	/**
	 * @return the from
	 */
	public final String getFrom() {
		return from;
	}

	/**
	 * @param from
	 *            the from to set
	 */
	public final void setFrom(String from) {
		this.from = from;
	}

	/**
	 * @return the server
	 */
	public final String getServer() {
		return server;
	}

	/**
	 * @param server
	 *            the server to set
	 */
	public final void setServer(String server) {
		this.server = server;
	}

	/**
	 * @return the port
	 */
	public final int getPort() {
		return port;
	}

	/**
	 * @param port
	 *            the port to set
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
	 * @param user
	 *            the user to set
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
	 * @param pwd
	 *            the pwd to set
	 */
	public final void setPwd(String pwd) {
		this.pwd = pwd;
	}

	/**
	 * @return the sleep
	 */
	public final int getSleep() {
		return sleep;
	}

	/**
	 * @param sleep the sleep to set
	 */
	public final void setSleep(int sleep) {
		this.sleep = sleep;
	}

	/**
	 * @return the runFlag
	 */
	public final boolean isRun() {
		return runFlag;
	}

	/**
	 * @param runFlag the runFlag to set
	 */
	public final void setRunFlag(boolean runFlag) {
		this.runFlag = runFlag;
	}

}
