/**
 * 
 */
package com.k99k.testcenter;

import org.apache.log4j.Logger;

import com.k99k.khunter.Action;
import com.k99k.khunter.ActionMsg;
import com.k99k.tools.Sms;
import com.k99k.tools.StringUtil;

/**
 * 处理短信发送任务的Task,需要dests(String[]),content
 * @author keel
 *
 */
public class SMS extends Action {

	/**
	 * @param name
	 */
	public SMS(String name) {
		super(name);
	}
	
	static final Logger log = Logger.getLogger(EMail.class);

	/* (non-Javadoc)
	 * @see com.k99k.khunter.Action#act(com.k99k.khunter.ActionMsg)
	 */
	@Override
	public ActionMsg act(ActionMsg msg) {
		String[] dests = (String[])msg.getData("dests");
		String content = msg.getData("content").toString();
		if (dests == null || dests.length==0 || !StringUtil.isStringWithLen(content, 1)) {
			log.error("SMS paras error. dests:"+dests+" content:"+content);
			return super.act(msg);
		}
		
		/* 短信能力暂时停止
		for (int i = 0; i < dests.length; i++) {
			if (StringUtil.isStringWithLen(dests[i], 10)) {
				if(!Sms.sendOne(dests[i], content)){
					log.error("SMS send error. dest:"+dests[i]+" content:"+content);
				}else{
					log.info("SMS sent to:[" + dests[i] + "][" + content + "]");
				}
			}else{
				log.error("SMS dest is empty. passed. content:"+content);
			}
		}*/
		return super.act(msg);
	}
	
	
	

}
