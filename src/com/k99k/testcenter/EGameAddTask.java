/**
 * 
 */
package com.k99k.testcenter;

import com.k99k.tools.encrypter.Encrypter;

/**
 * 生成添加任务的链接
 * @author keel
 *
 */
public class EGameAddTask {

	/**
	 * 
	 */
	public EGameAddTask() {
	}
	
	/**
	 * 生成添加测试任务URL
	 * @param productId
	 * @param userId
	 * @param loginTime
	 * @return
	 */
	public static final String encodeTaskPara(long productId,long userId,long loginTime){
		String src = new StringBuilder(String.valueOf(userId)).append("#").append(loginTime).append("#").append(productId).toString();
		String des = Encrypter.encrypt(src);
		return des;
	}
	
	/**
	 * 生成登录到测试系统的参数
	 * @param userId
	 * @param loginTime
	 * @return
	 */
	public static final String encodeUserPara(long userId,long loginTime){
		String src = new StringBuilder(String.valueOf(userId)).append("#").append(loginTime).toString();
		String des = Encrypter.encrypt(src);
		return des;
	}
	
	/**
	 * 解密
	 * @param urlPara
	 * @return
	 */
	public static final String decodeUrlPara(String urlPara){
		return Encrypter.decrypt(urlPara);
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		long productId = 145;
		long userId = 5;
		long nowTime = System.currentTimeMillis();
		String des = encodeTaskPara(productId,userId,nowTime);
		System.out.println("des:"+des);
		String src = decodeUrlPara(des);
		System.out.println("src:"+src);
		System.out.println("--------------");
		des = encodeUserPara(userId,nowTime);
		System.out.println("des:"+des);
		src = decodeUrlPara(des);
		System.out.println("src:"+src);
	}

}
