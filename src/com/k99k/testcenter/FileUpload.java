/**
 * 
 */
package com.k99k.testcenter;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.k99k.khunter.Action;
import com.k99k.khunter.ActionMsg;
import com.k99k.khunter.HttpActionMsg;
import com.k99k.khunter.JOut;
import com.k99k.khunter.acts.Uploader;
import com.k99k.tools.StringUtil;

/**
 * 文件上传,利用com.k99k.khunter.Uploader实现
 * @author keel
 *
 */
public class FileUpload extends Action {

	/**
	 * @param name
	 */
	public FileUpload(String name) {
		super(name);
	}
	static final Logger log = Logger.getLogger(FileUpload.class);
	
	/**
	 * 保存文件的路径
	 */
	private String savePath = "";
	private String tempSavePath = "";

	/* (non-Javadoc)
	 * @see com.k99k.khunter.Action#act(com.k99k.khunter.ActionMsg)
	 */
	@Override
	public ActionMsg act(ActionMsg msg) {
		HttpActionMsg httpmsg = (HttpActionMsg)msg;
		HttpServletRequest req = httpmsg.getHttpReq();
		String file = req.getParameter("f");
		String newName = req.getParameter("n");
		
		if (!StringUtil.isStringWithLen(file, 1)) {
			JOut.err(400, httpmsg);
			return super.act(msg);
		}
		try {
			file = URLDecoder.decode(file, "utf-8");
		} catch (UnsupportedEncodingException e) {
			JOut.err(400, httpmsg);
			return super.act(msg);
		}
		String newFileName = (newName==null)?file:newName.trim();
		//log.info("f:"+file);
		String toPath = (this.tempSavePath.equals("")) ? this.savePath : this.tempSavePath;
		String re = Uploader.upload(req,toPath,file,newFileName);
		//log.info("upload ok :"+re);
		msg.addData("[print]", re);
		return super.act(msg);
	}
	
	/**
	 * @return the savePath
	 */
	public final String getSavePath() {
		return savePath;
	}

	/**
	 * @param savePath the savePath to set
	 */
	public final void setSavePath(String savePath) {
		this.savePath = savePath;
	}

	public String getTempSavePath() {
		return tempSavePath;
	}

	public void setTempSavePath(String tempSavePath) {
		this.tempSavePath = tempSavePath;
	}

	
}
