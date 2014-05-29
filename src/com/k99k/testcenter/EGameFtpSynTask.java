/**
 * 
 */
package com.k99k.testcenter;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.k99k.khunter.Action;
import com.k99k.khunter.ActionMsg;
import com.k99k.khunter.HTManager;
import com.k99k.khunter.KObject;
import com.k99k.khunter.TaskManager;
import com.k99k.khunter.dao.StaticDao;
import com.k99k.tools.IO;
import com.k99k.tools.StringUtil;

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
	
	private String localPath;
	
	/**
	 * 类似： "/new/"
	 */
	private String newProductRemotePrePath = "/";
	
	/**
	 * 类似:  "/update/"
	 */
	private String updateProductRemotePrePath = "/";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//开始上传
		System.setProperty("java.net.preferIPv4Stack", "true");
		FTPClient fc = new FTPClient();
		HashMap<String,String> f2f = new HashMap<String, String>();
		f2f.put("f:/tomcat_6/webapps/ROOT/tc/gamefiles/76_1351761648889_0.apk", "/76_1351761648889_0.apk");
		try {
			fc.setPassive(false);
			fc.connect("202.102.39.14");
			fc.login("shitibao", "shitibao123");
			uploadFiles(fc,f2f);
			fc.disconnect(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	/* (non-Javadoc)
	 * @see com.k99k.khunter.Action#act(com.k99k.khunter.ActionMsg)
	 */
	@Override
	public ActionMsg act(ActionMsg msg) {
		//从msg中获取tid,从TestUnit中找到测试通过的文件名,适配机型,fileId,
		Object pid = msg.getData("pid");
		Object tid = msg.getData("tid");
		//Object isUpdateS = msg.getData("isUpdate");
		if (!StringUtil.isDigits(tid) || !StringUtil.isDigits(pid)) {
			log.error("EGameFtpSynTask faild. tid or pid not exsit.");
			return super.act(msg);
		}
//		boolean isUpdate = false;
//		if (isUpdateS != null && isUpdateS.toString().equals("true")) {
//			isUpdate = true;
//		}
		long tidL = Long.parseLong(String.valueOf(tid));
		//从TestUnit中找到测试通过的文件名,适配机型,fileId
		HashMap<String,Object> q = new HashMap<String, Object>();
		HashMap<String,Object> in = new HashMap<String, Object>();
		in.put("$in", new int[]{2,4});
		q.put("TID", tidL);
		q.put("state", in);
		ArrayList<HashMap<String,Object>> re = TestUnit.dao.query(q, StaticDao.fields_ftp_tid, null,0, 0, null);
		if (re == null || re.size() == 0) {
			log.error("EGameFtpSynTask faild. ERR_EGAME_FTP_TASK_NOT_FOUND.");
			return super.act(msg);
		}
		//用于生成适配对应文件的StringBuffer
		StringBuffer fsb = new StringBuffer();
		//用于生成文件上传序列的HashMap
		HashMap<String,String> f2f = new HashMap<String, String>();
		KObject task = TTask.dao.findOne(tidL);
		boolean isUpdate = task.getType()>=7;
		String remotePath = (isUpdate)?
				(this.updateProductRemotePrePath+StringUtil.getFormatDateString("yyyyMMdd")+"/"+pid+"/")
				:(this.newProductRemotePrePath+StringUtil.getFormatDateString("yyyyMMdd")+"/"+pid+"/");
		
		//清空query条件
		q = new HashMap<String, Object>(2);
		StringBuilder fileString = new StringBuilder();
		for (Iterator<HashMap<String, Object>> it = re.iterator(); it.hasNext();) {
			HashMap<String, Object> map = it.next();
			String fileId = map.get("fileId").toString();
			//String gFile = map.get("gFile").toString();
			String phone = map.get("phone").toString();
			
			//机型组
			int isGroup = 0;
			if (phone.substring(0,1).equals("#")) {
				isGroup = 1;
				//phone = phone.substring(1);
			}
			
			//以fileId从TCGameFile中找到真实文件名
			q.put("_id", Long.parseLong(fileId));
			map = GameFile.dao.findOneMap(q, StaticDao.fields_ftp_fileName);
			String fileName = map.get("fileName").toString();
			//适配对应文件,使用egameId
			fsb.append(fileName).append(",").append(Phone.egameIds.get(phone)).append(",").append(isGroup).append("\r\n");
			//文件上传序列
			String localFile = this.localPath+pid+"/"+fileName;
			f2f.put(localFile, remotePath+fileName);
			fileString.append(localFile).append(";");
		}
		
		//生成适配对应文件
		String local = this.localPath+pid+"/";
		String csv = local+"config_"+tid+"_"+System.currentTimeMillis()+".csv";
		String csv2 = local+"config2_"+tid+"_"+System.currentTimeMillis()+".csv";
		
		//生成参数适配文件
		
		try {
			IO.makeDir(local);
			IO.writeTxt(fsb.toString(), "utf-8", csv);
			if (task.containsProp("synFileParas")) {
				String config2 = task.getProp("synFileParas").toString();
				IO.writeTxt(config2, "utf-8", csv2);
				f2f.put(csv2, remotePath+"config2.csv");
			}
			
		} catch (IOException e) {
			log.error("EGameFtpSynTask faild.writeTxt error. ");
			return super.act(msg);
		}
		f2f.put(csv, remotePath+"config.csv");
		
		//开始上传
		System.setProperty("java.net.preferIPv4Stack", "true");
		if (HTManager.debug) {
			
			log.info("debug mode, do not ftpsync.");
			return super.act(msg);
		}
		
		
		
		FTPClient fc = new FTPClient();
		try {
			fc.setPassive(true);
			fc.connect(this.ip);
			fc.login(this.user, this.pwd);
			uploadFiles(fc,f2f);
			fc.disconnect(true);
		} catch (Exception e) {
			Object o = msg.getData("tryTimes");
			int tTimes = 0;
			if (o != null) {
				tTimes = Integer.parseInt(o.toString());
			}
			
			log.error("uploadFiles error.tryTimes:"+tTimes, e);
			log.error("error tid:"+tidL+",pid:"+pid+",localfiles:"+fileString+",\r\n Will try again after 30min:"+msg.getActitonName());
			
			if (tTimes < 10) {
				tTimes++;
				msg.removeData(TaskManager.TASK_TYPE);
				msg.addData(TaskManager.TASK_TYPE, TaskManager.TASK_TYPE_SCHEDULE_POOL);
				msg.addData(TaskManager.TASK_DELAY, 30*60*1000);
				msg.addData("tryTimes", tTimes);
				TaskManager.makeNewTask("egameFtp-"+tid+"-"+System.currentTimeMillis(), msg);
			}
			return super.act(msg);
		}
		return super.act(msg);
	}
	
	

	/* (non-Javadoc)
	 * @see com.k99k.khunter.Action#init()
	 */
	@Override
	public void init() {
		super.init();
		
	}
	
	/**
	 * 上传文件序列,从远端根目录开始定位
	 * @param client FTPClient
	 * @param f2f HashMap形式的文件序列,key:本地文件完整路径,value:远程目标文件路径
	 */
	static final void uploadFiles(FTPClient client,HashMap<String,String> f2f) throws Exception{
		Iterator<Entry<String,String>> iter = f2f.entrySet().iterator(); 
		while (iter.hasNext()) { 
		    Entry<String,String> entry = iter.next(); 
		    final String src = entry.getKey(); 
		    final String dest = entry.getValue(); 
		    client.changeDirectory("/");
		    int targetDirSplit = dest.lastIndexOf("/");
		    String targetDir = dest.substring(0,targetDirSplit);
		    String targetFileName = dest.substring(targetDirSplit+1);
		    if (!client.currentDirectory().equals(targetDir)) {
		    	//移动至目标文件夹,若无则创建
		    	String[] ds = targetDir.split("/");
				for (int i = 0; i < ds.length; i++) {
					if (ds[i].length()>0 && !client.currentDirectory().equals(ds[i])) {
						try {
							client.createDirectory(ds[i]);
						} catch (Exception e) {
						}
						client.changeDirectory(ds[i]);
					}
				}
			}
		    
		    InputStream in = new FileInputStream(src);
			//做好失败和成功记录
			client.upload(targetFileName,in,0,0,new FTPDataTransferListener() {
				
				@Override
				public void transferred(int length) {
					
				}
				
				@Override
				public void started() {
					
				}
				
				@Override
				public void failed() {
					log.info("FTP upload failed!["+src+"] to ["+dest+"]");
				}
				
				@Override
				public void completed() {
					log.info("upload OK! ["+src+"] to ["+dest+"]");
				}
				
				@Override
				public void aborted() {
					
				}
			});
		} 
	}
	
	/**
	 * 上传文件夹及下面的所有文件
	 * @param client FTPClient
	 * @param srcDir 本地文件夹
	 * @param targetDir 远程目标文件夹
	 * @throws Exception
	 */
	static final void uploadDir(FTPClient client,String srcDir,String targetDir) throws Exception{
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
					uploadDir(client,children,remote);
				}
			}
		} catch (Exception e) {
			log.error("uploadFile error:", e);
		}
		
	}
	

//	/**
//	 * 上传同步文件到ftp服务器
//	 * @param targetPath
//	 * @param srcPathTo
//	 *  
//	 */
//	final boolean synFtps(String targetPath,String srcPathTo){
//		boolean re = true;
//		try {
//			FTPClient client = new FTPClient();
//			client.connect(this.ip,this.port);
//			client.login(this.user,this.pwd);
//			log.info("synftp:"+client.getHost()+" srcPathTo:"+srcPathTo);
//			uploadFile(client,srcPathTo,targetPath);	
//			client.disconnect(true);
//		} catch (Exception e) {
//			log.error("synftps error!", e);
//			re = false;
//		} 
//		return re;
//	}
	
	

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


	/**
	 * @return the localPath
	 */
	public final String getLocalPath() {
		return localPath;
	}


	/**
	 * @param localPath the localPath to set
	 */
	public final void setLocalPath(String localPath) {
		this.localPath = localPath;
	}


	/**
	 * @return the newProductRemotePrePath
	 */
	public final String getNewProductRemotePrePath() {
		return newProductRemotePrePath;
	}


	/**
	 * @param newProductRemotePrePath the newProductRemotePrePath to set
	 */
	public final void setNewProductRemotePrePath(String newProductRemotePrePath) {
		this.newProductRemotePrePath = newProductRemotePrePath;
	}


	/**
	 * @return the updateProductRemotePrePath
	 */
	public final String getUpdateProductRemotePrePath() {
		return updateProductRemotePrePath;
	}


	/**
	 * @param updateProductRemotePrePath the updateProductRemotePrePath to set
	 */
	public final void setUpdateProductRemotePrePath(
			String updateProductRemotePrePath) {
		this.updateProductRemotePrePath = updateProductRemotePrePath;
	}

	
}
