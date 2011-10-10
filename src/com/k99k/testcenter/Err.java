/**
 * 
 */
package com.k99k.testcenter;

/**
 * 错误码
 * @author keel
 *
 */
public class Err {

	/**
	 * 
	 */
	public Err() {
	}
	
	public static final int OK = 0;
	public static final int ERR_PARAS = 101;
	public static final int ERR_AUTH_FAIL = 103;
	public static final int ERR_JSON = 104;
	public static final int ERR_DB_UPDATE = 105;
	
	public static final int ERR_ADD_PRODUCT_FAIL = 201;
	public static final int ERR_ADD_OPERATOR_FAIL = 202;
	public static final int ERR_ADD_TASK_FAIL = 203;
	public static final int ERR_ADD_TESTUNIT = 204;
	public static final int ERR_SEND_TESTUNIT = 205;
	public static final int ERR_EXEC_TESTUNIT = 206;
	public static final int ERR_SUMMARY_TAST = 207;
	public static final int ERR_CONFIRM_TASK = 208;
	public static final int ERR_FINISH_TASK = 209;
	public static final int ERR_ONLINE_TASK = 210;
	

}
