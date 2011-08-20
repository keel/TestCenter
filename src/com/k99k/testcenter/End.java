/**
 * 
 */
package com.k99k.testcenter;

import com.k99k.khunter.Action;
import com.k99k.khunter.dao.StaticDao;

/**
 * 最后一个Action,负责Action初始化后的收尾工作
 * @author keel
 *
 */
public class End extends Action {

	/**
	 * @param name
	 */
	public End(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see com.k99k.khunter.Action#init()
	 */
	@Override
	public void init() {
		super.init();
		//初始化StaticDao
		StaticDao.initS();
	}
	
	

}
