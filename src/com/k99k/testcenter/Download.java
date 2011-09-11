/**
 * 
 */
package com.k99k.testcenter;

import javax.servlet.http.HttpServletRequest;

import com.k99k.khunter.Action;
import com.k99k.khunter.ActionMsg;
import com.k99k.khunter.DaoInterface;
import com.k99k.khunter.DaoManager;
import com.k99k.khunter.HttpActionMsg;
import com.k99k.khunter.KFilter;

/**
 * 文件下载处理(权限验证,文件形式下载)
 * @author keel
 *
 */
public class Download extends Action {

	/**
	 * @param name
	 */
	public Download(String name) {
		super(name);
	}
	
	static DaoInterface dao;

	/* (non-Javadoc)
	 * @see com.k99k.khunter.Action#act(com.k99k.khunter.ActionMsg)
	 */
	@Override
	public ActionMsg act(ActionMsg msg) {
		HttpActionMsg httpmsg = (HttpActionMsg)msg;
		HttpServletRequest req = httpmsg.getHttpReq();
		String fileName = KFilter.actPath(msg, 2, "");
		//TODO 应该根据ID指定具体的文件
		
		return super.act(msg);
	}
	
	
	
	/* (non-Javadoc)
	 * @see com.k99k.khunter.Action#init()
	 */
	@Override
	public void init() {
		dao = DaoManager.findDao("TCFile");
		super.init();
	}

	private String downPath;

	/**
	 * @return the downPath
	 */
	public final String getDownPath() {
		return downPath;
	}

	/**
	 * @param downPath the downPath to set
	 */
	public final void setDownPath(String downPath) {
		this.downPath = downPath;
	}
	

}
