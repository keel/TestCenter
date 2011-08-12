/**
 * 
 */
package com.k99k.khunter;

/**
 * @author keel
 *
 */
public interface DataSourceInterface {

	public String getName();
	
	public boolean init();
	
	public boolean reset();
	
	public void exit();
}
