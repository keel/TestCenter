/**
 * 
 */
package com.k99k.testcenter;

import com.k99k.khunter.Action;
import com.k99k.khunter.ActionMsg;
import com.k99k.khunter.DaoInterface;
import com.k99k.khunter.DaoManager;
import com.k99k.khunter.KObjManager;
import com.k99k.khunter.KObjSchema;

/**
 * 终端
 * @author keel
 *
 */
public class Phone extends Action {

	/**
	 * @param name
	 */
	public Phone(String name) {
		super(name);
	}
	static DaoInterface dao;
	static KObjSchema schema;
	/* (non-Javadoc)
	 * @see com.k99k.khunter.Action#act(com.k99k.khunter.ActionMsg)
	 */
	@Override
	public ActionMsg act(ActionMsg msg) {
		return super.act(msg);
	}

	/* (non-Javadoc)
	 * @see com.k99k.khunter.Action#init()
	 */
	@Override
	public void init() {
		dao = DaoManager.findDao("TCPhoneDao");
		schema = KObjManager.findSchema("TCPhone");
		super.init();
	}
	
	

}
