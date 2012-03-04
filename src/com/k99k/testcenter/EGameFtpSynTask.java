/**
 * 
 */
package com.k99k.testcenter;

import it.sauronsoftware.ftp4j.FTPClient;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.k99k.khunter.Action;
import com.k99k.khunter.ActionMsg;

/**
 * 游戏平台FTP同步产品接口
 * @author keel
 *
 */
public class EGameFtpSynTask extends Action {

	/**
	 * @param name
	 */
	public EGameFtpSynTask(String name) {
		super(name);
	}
	static final Logger log = Logger.getLogger(EGameFtpSynTask.class);
	
	private String ip;
	
	private int port;
	
	private String user;
	
	private String pwd;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}
	
	/* (non-Javadoc)
	 * @see com.k99k.khunter.Action#act(com.k99k.khunter.ActionMsg)
	 */
	@Override
	public ActionMsg act(ActionMsg msg) {
		//从msg中获取pid,从TCTestUnit中找到测试通过的文件名,适配机型,fileId,
		//然后以fileId从TCGameFile中找到真实文件名
		
		//生成适配对应文件
		
		//生成文件上传序列对应路径
		
		//开始上传
		
		return super.act(msg);
	}
	
	

	/* (non-Javadoc)
	 * @see com.k99k.khunter.Action#init()
	 */
	@Override
	public void init() {
		super.init();
		//创建一个循环任务
		
		
	}

	/**
	 * FIXME 创建本次同步的文件.
	 * @return 是否创建成功
	 */
	private boolean createSynFiles(){
		//创建时间文件夹和PID文件夹
		
		//移动文件包到待上传目录
		
		//生成config.csv文件
		
		return false;
	}
	
	/**
	 * 上传文件序列
	 * @param client FTPClient
	 * @param f2f HashMap形式的文件序列,key:本地文件完整路径,value:远程目标文件路径
	 * @throws Exception
	 */
	private final void uploadFiles(FTPClient client,HashMap<String,String> f2f) throws Exception{
		try {
			
			Iterator<Entry<String,String>> iter = f2f.entrySet().iterator(); 
			while (iter.hasNext()) { 
			    Entry<String,String> entry = iter.next(); 
			    String src = entry.getKey(); 
			    String dest = entry.getValue(); 
			    
			    String targetDir = dest.substring(0,dest.lastIndexOf("/"));
			    
			    if (!client.currentDirectory().equals(targetDir)) {
			    	//移动至目标文件夹,若无则创建
			    	try {
			    		client.changeDirectory(targetDir);
			    	} catch (Exception e) {
			    		client.createDirectory(targetDir);
			    		client.changeDirectory(targetDir);
			    	}
				}
				
				File srcF = new File(src);
				client.upload(srcF);
				log.info("upload src["+src+"] to ["+dest+"]");
				
			} 
			
		} catch (Exception e) {
			log.error("uploadFile error:", e);
		}
		
	}
	
	/**
	 * 上传文件夹及下面的所有文件
	 * @param client FTPClient
	 * @param srcDir 本地文件夹
	 * @param targetDir 远程目标文件夹
	 * @throws Exception
	 */
	private final void uploadFile(FTPClient client,String srcDir,String targetDir) throws Exception{
		try {
			
			//移动至目标文件夹,若无则创建
			try {
				client.changeDirectory(targetDir);
			} catch (Exception e) {
				client.createDirectory(targetDir);
				client.changeDirectory(targetDir);
			}
			
			log.info("remotePath----:"+client.currentDirectory());
			
			File dirf = new File(srcDir);
			File[] fileList  = dirf.listFiles();
			for (int i = 0; i < fileList.length; i++) {
				if (fileList[i].isFile()) {
					//上传文件
					client.upload(fileList[i]);
					log.info(fileList[i].getName());
				}else if(fileList[i].isDirectory()){
					//上传文件夹
					String children = srcDir+"/"+fileList[i].getName();
					String remote = targetDir+"/"+fileList[i].getName();
					uploadFile(client,children,remote);
				}
			}
		} catch (Exception e) {
			log.error("uploadFile error:", e);
		}
		
	}
	

	/**
	 * 上传同步文件到ftp服务器
	 * @param targetPath
	 * @param srcPathTo
	 *  
	 */
	private final boolean synFtps(String targetPath,String srcPathTo){
		boolean re = true;
		try {
			FTPClient client = new FTPClient();
			client.connect(this.ip,this.port);
			client.login(this.user,this.pwd);
			log.info("synftp:"+client.getHost()+" srcPathTo:"+srcPathTo);
			uploadFile(client,srcPathTo,targetPath);	
			client.disconnect(true);
		} catch (Exception e) {
			log.error("synftps error!", e);
			re = false;
		} 
		return re;
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
