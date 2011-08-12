package com.k99k.khunter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

/**
 * 处理由KFilter过来的请求,由Action进行处理,实际是一个Action执行者
 */
public final class ActionServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	static final Logger log = Logger.getLogger(ActionServlet.class);
	
	private static String ini;
	
	private static int rootNum = 0;

//	static boolean isInited = false;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ActionServlet() {
        super();
    }
    
    public static final String getIni(){
    	return ini;
    }

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		ini = config.getInitParameter("ini");
		rootNum = Integer.parseInt(config.getInitParameter("rootNum"));
		boolean initOK = HTManager.init(ini);
		if (!initOK) {
			log.error("---------KHunter init failed!!!------------");
		}
	}

	/**
	 * @see Servlet#destroy()
	 */
	public void destroy() {
		// 清理ActionServlet
		HTManager.exit();
	}
	
	/**
	 * 设置输入输出的编码
	 * @param charset
	 * @param req
	 * @param resp
	 * @throws UnsupportedEncodingException
	 */
	public static final void setCharset(String charset,HttpServletRequest req, HttpServletResponse resp) throws UnsupportedEncodingException{
		req.setCharacterEncoding(charset);
		resp.setCharacterEncoding(charset);
		resp.setHeader("Content-Encoding",charset);
		resp.setHeader("content-type","text/html; charset="+charset);
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		//全程使用utf-8
		//setCharset("utf-8",req,resp);
		
		
		
		
		//TODO get方式测试用
		this.doPost(req, resp);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		//全程使用utf-8
		setCharset("utf-8",req,resp);
		try {
			//由act参数定位Action,此参数在Filter中验证
//			String actName = req.getParameter("act");
			String requrl = req.getRequestURL().toString();
			//pathArr[0]为域名，pathArr[1]为第一个路径,后面继续为路径,最后面为参数
			String[] pathArr = requrl.substring(requrl.indexOf("//")+2).split("\\/");
//			if (actName == null) {
//				resp.setStatus(404);
//				resp.getWriter().print("404 - 1");
//				return;
//			}
			int rn = 1+rootNum;
			String actName = (pathArr.length <= rn) ? "" : pathArr[rn];
			ActionMsg msg = new HttpActionMsg(actName, req, resp);
			Action action = ActionManager.findAction(actName);
			if (action == null) {
				resp.setStatus(404);
				resp.getWriter().print("404 - 2");
				return;
			}
			msg.addData("[pathArr]", pathArr);
			//执行action
			msg = action.act(msg);
			//是否打印
			if (msg.getData("[print]") != null) {
				resp.getWriter().print(msg.getData("[print]"));
				return;
			}
			//是否发向JSP
			else if (msg.getData("[jsp]") != null) {
				String to = (String) msg.getData("[jsp]");
//				Object o = msg.getData("[jspAttr]");
//				if (o != null) {
//					req.setAttribute("[jspAttr]", o);
//				}
				req.setAttribute("[jspAttr]", msg);
				RequestDispatcher rd = req.getRequestDispatcher(to);
				rd.forward(req, resp);
				return;
			}
			//是否跳转
			else if (msg.getData("[redirect]") != null) {
				String redirect = (String) msg.getData("[redirect]");
				resp.sendRedirect(redirect);
				return;
			}else{
				resp.setStatus(404);
				resp.getWriter().print("404 - 3");
			}
		} catch (Exception e) {
			log.error("Action servlet error!", e);
			resp.setStatus(404);
			resp.getWriter().print("500 - System error! please contact administrator.");
			return;
		}

	}

//	/**
//	 * 从msg的[pathArr]中定位子Action的actName
//	 * @param msg
//	 * @param pathNum
//	 * @param defaultStr
//	 * @return subact 子Action的actName
//	 */
//	public static final String actPath(ActionMsg msg,int pathNum,String defaultStr){
//		//FIXME 测试时多计算了Servlet
//		pathNum = pathNum+rootNum;
//		String[] pathArr = (String[]) msg.getData("[pathArr]");
//		String subact = (pathArr.length <= (pathNum+1)) ? defaultStr : pathArr[pathNum];
//		return subact;
//	}
	
	
}
