/**
 * 
 */
package com.k99k.testcenter;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.k99k.tools.encrypter.Encrypter;

/**
 * 生成添加任务的链接
 * @author keel
 *
 */
public class EGameAddTask extends HttpServlet {

	private static final long serialVersionUID = 1L;


	/**
	 * 
	 */
	public EGameAddTask() {
	}
	
	private String url = "http://202.102.40.43/egame/";
	
	
	
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		//------------------------------
		//从url或session中取出以下参数
		//------------------------------
		//跳转到的目标地址
		String target = req.getParameter("target");
		//游戏产品ID
		String pid = req.getParameter("pid");
		//用户ID，可从登录session中获取，注意必须是已登录状态，非登录状态时直接跳转到平台的登录页
		String userId = req.getParameter("uid");
		
		if (target.equals("newtask")) {
			//跳转到创建新任务
			resp.sendRedirect(this.url+"newtask?t="+encodeTaskPara(Long.parseLong(pid), Long.parseLong(userId), System.currentTimeMillis()));
			return;
		}else if(target.equals("task")){
			//跳转到查看该产品的测试任务
			resp.sendRedirect(this.url+"task?t="+encodeTaskPara(Long.parseLong(pid), Long.parseLong(userId), System.currentTimeMillis()));
			return;
		}else if(target.equals("test")){
			//跳转到测试系统首页
			resp.sendRedirect(this.url+"?t="+encodeUserPara(Long.parseLong(userId), System.currentTimeMillis()));
			return;
		}
		
		
		super.doGet(req, resp);
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
	 * 测试
	 * @param args
	 */
	public static void main(String[] args) {
		long productId = 145;
		long userId = 81;
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
