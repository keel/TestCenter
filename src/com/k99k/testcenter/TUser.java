/**
 * 
 */
package com.k99k.testcenter;

import org.apache.log4j.Logger;

import com.k99k.khunter.Action;
import com.k99k.khunter.DaoInterface;
import com.k99k.khunter.DaoManager;
import com.k99k.khunter.KObjManager;
import com.k99k.khunter.KObjSchema;

/**
 * TUser
 * @author keel
 *
 */
public class TUser extends Action  {
	/**
	 * @param name
	 */
	public TUser(String name) {
		super(name);
	}
	static final Logger log = Logger.getLogger(TUser.class);
	
	static DaoInterface dao;
	static KObjSchema schema;
	

	/* (non-Javadoc)
	 * @see com.k99k.khunter.Action#init()
	 */
	@Override
	public void init() {
		dao = DaoManager.findDao("TCUserDao");
		schema = KObjManager.findSchema("TCUser");
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
