/**
 * 
 */
package com.k99k.testcenter;

import java.util.ArrayList;
import java.util.HashMap;
import org.apache.log4j.Logger;
import com.k99k.khunter.Action;
import com.k99k.tools.JSON;
import com.k99k.tools.Net;

/**
 * 与爱游戏平台的接口
 * @author keel
 *
 */
public class EGame extends Action {

	/**
	 * @param name
	 */
	public EGame(String name) {
		super(name);
	}
	static final Logger log = Logger.getLogger(EGame.class);
	
	/**
	 * 公司接口URL
	 */
	private static  String companyUrl;
	
	/**
	 * 产品接口URL
	 */
	private static  String productUrl;
	
	/**
	 * 终端接口URL
	 */
	private static String handsetUrl;
	
	/**
	 * 短代信息URL接口
	 */
	private static String feeUrl;
	
	
	/**
	 * 获取CP信息
	 * @param cpid
	 * @return HashMap形式的company json
	 */
	public static final HashMap<String,String> getCompany(String cpid){
		String url = companyUrl+"&cpid="+cpid;
		return getUrlJson(url);
	}
	
	/**
	 * 获取CP信息
	 * @param id
	 * @return HashMap形式的json
	 */
	public static final HashMap<String,String> getProduct(long id){
		String url = productUrl+"&productId="+id;
		return getUrlJson(url);
	}
	/**
	 * 获取终端信息
	 * @param mode
	 * @return HashMap形式的json
	 */
	public static final HashMap<String,String> getHandset(String mode){
		String url = handsetUrl+"&model="+mode;
		return getUrlJson(url);
	}
	
	/**
	 * 获取短代信息
	 * @param id
	 * @return HashMap形式的json
	 */
	@SuppressWarnings("unchecked")
	public static final ArrayList<HashMap<String,String>> getFee(long pid){
		String url = feeUrl+"?productId="+pid;
		String re = Net.getUrlContent(url, 3000, false, "utf-8");
		if (re.equals("")) {
			return null;
		}
		Object j = JSON.read(re);
		try {
			if (j instanceof HashMap) {
				HashMap<String,Object> json = (HashMap<String,Object>)j;
				if (json.containsKey("rows")) {
					ArrayList<HashMap<String,String>> list = (ArrayList<HashMap<String,String>>) json.get("rows");
					if (list!=null && list.size()>=1) {
						return list;
					}
				}
			}
		} catch (Exception e) {
			log.error("Egame.getUrlJson failed!url:"+url, e);
			return null;
		}
		return null;
	}
	
	/**
	 * 获取指定url并解析json,返回目的json,3秒超时
	 * @param url
	 * @return HashMap<String,String> 目的json,失败返回null
	 */
	@SuppressWarnings("unchecked")
	private final static HashMap<String,String> getUrlJson(String url){
		String re = Net.getUrlContent(url, 3000, false, "utf-8");
		if (re.equals("")) {
			return null;
		}
		Object j = JSON.read(re);
		try {
			if (j instanceof HashMap) {
				HashMap<String,Object> json = (HashMap<String,Object>)j;
				if (json.containsKey("rows")) {
					ArrayList<Object> list = (ArrayList<Object>) json.get("rows");
					if (list!=null && list.size()>=1) {
						HashMap<String,String> comJson = (HashMap<String, String>) list.get(0);
						if (comJson != null) {
							return comJson;
						}
					}
				}
			}
		} catch (Exception e) {
			log.error("Egame.getUrlJson failed!url:"+url, e);
			return null;
		}
		return null;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		EGame.companyUrl = "http://202.102.111.18/MIS/v/entitytest/cps?startIndex=0&pageSize=1";
		EGame.productUrl = "http://202.102.111.18/MIS/v/entitytest/products?startIndex=0&pageSize=1";
		EGame.handsetUrl = "http://202.102.111.18/MIS/v/entitytest/models?startIndex=0&pageSize=1";
		EGame.feeUrl = "http://202.102.111.18/MIS/v/entitytest/consumecodes";
		
		
		String comId = "C22001";
		long pid = 219230;
		String mod = "E63V";
		long feePid = 142;
		
		HashMap<String,String> re = null;
		
		re = getCompany(comId);
		System.out.println(JSON.writeFormat(re));
		re = getProduct(pid);
		System.out.println(JSON.writeFormat(re));
		re = getHandset(mod);
		System.out.println(JSON.writeFormat(re));
		ArrayList<HashMap<String,String>> li = getFee(feePid);
		System.out.println(JSON.writeFormat(li,2));
		
		
	}

	/**
	 * @return the companyUrl
	 */
	public static final String getCompanyUrl() {
		return companyUrl;
	}

	/**
	 * @param companyUrl the companyUrl to set
	 */
	public static final void setCompanyUrl(String url) {
		companyUrl = url;
	}

	/**
	 * @return the productUrl
	 */
	public static final String getProductUrl() {
		return productUrl;
	}

	/**
	 * @param productUrl the productUrl to set
	 */
	public static final void setProductUrl(String url) {
		productUrl = url;
	}

	/**
	 * @return the handsetUrl
	 */
	public static final String getHandsetUrl() {
		return handsetUrl;
	}

	/**
	 * @param handsetUrl the handsetUrl to set
	 */
	public static final void setHandsetUrl(String url) {
		handsetUrl = url;
	}

	/**
	 * @return the feeUrl
	 */
	public static final String getFeeUrl() {
		return feeUrl;
	}

	/**
	 * @param feeUrl the feeUrl to set
	 */
	public static final void setFeeUrl(String url) {
		feeUrl = url;
	}


}
