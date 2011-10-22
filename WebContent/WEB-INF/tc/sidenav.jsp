<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="com.k99k.khunter.*,com.k99k.tools.*" session="false" %>
<%!
String prefix = KFilter.getPrefix();
%><%
int level = (StringUtil.isDigits(request.getParameter("lv")))? Integer.parseInt(request.getParameter("lv")):0;
int type = (StringUtil.isDigits(request.getParameter("type")))? Integer.parseInt(request.getParameter("type")):0;
int gg = (StringUtil.isDigits(request.getParameter("gg")))? Integer.parseInt(request.getParameter("gg")):0;
int tt = (StringUtil.isDigits(request.getParameter("tt")))? Integer.parseInt(request.getParameter("tt")):0;
%><div id="sideNav">
<div>用户信息</div>
<ul>
<li id="side_pwd"><a href="<%=prefix %>/user/edit" >修改用户信息</a></li>
<li id="side_logout"><a href="<%=prefix %>/auth/logout" >注销</a></li>
</ul>
<div>测试管理</div>
<ul>
<li id="side_gg"><a href="<%=prefix %>/news" >公告
<%if(gg>0){ %><span id="newNews" class="bold red">(<%=gg %>)</span><%} %>
</a></li>
<li id="side_mytask"><a href="<%=prefix %>/tasks/my" >我的任务
<%if(tt>0){ %><span id="newTasks" class="bold red">(<%=tt %>)</span><%} %>
</a></li>
<%
if(type>=2){
%>
<li id="side_task"><a href="<%=prefix %>/tasks" >任务管理</a></li>
<%
}
%>
<li id="side_product"><a href="<%=prefix %>/products" >产品管理</a></li>
</ul>

<div>相关文档</div>
<ul>
<li id="side_proc"><a href="<%=prefix %>/proc" >流程说明</a></li>
<li id="side_handset"><a href="<%=prefix %>/handset" >手机终端信息</a></li>
<li id="side_faq"><a href="<%=prefix %>/faq" >适配常见问题</a></li>
<li id="side_api"><a href="<%=prefix %>/api" >电信接口</a></li>
</ul>

<div>讨论</div>
<ul>
<li id="side_topics"><a href="<%=prefix %>/topics" >公共讨论</a></li>
<li id="side_tags"><a href="<%=prefix %>/topics/tags" >专项问题</a></li>
</ul>

<%
if(type>=4){
%>
<div>统计查询</div>
<ul>
<li id="side_search"><a href="<%=prefix %>/search" >查询</a></li>
<li id="side_dayreport"><a href="<%=prefix %>/report/day" >日报表</a></li>
<li id="side_weekreport"><a href="<%=prefix %>/report/week" >周报表</a></li>
<li id="side_monthreport"><a href="<%=prefix %>/report/month" >月报表</a></li>
</ul>
<%};
if(type>=5){
%>
<div>数据维护</div>
<ul>
<li id="side_admin_handset"><a href="<%=prefix %>/admin/handset" >终端信息</a></li>
<li id="side_admin_company"><a href="<%=prefix %>/admin/company" >厂商信息</a></li>
<li id="side_admin_user"><a href="<%=prefix %>/admin/user" >用户管理</a></li>
<li id="side_admin_product"><a href="<%=prefix %>/admin/product" >产品库*</a></li>
<%
if(type>=6){
%>
<li id="side_admin_doc"><a href="<%=prefix %>/admin/doc" >文档管理</a></li>
<li id="side_admin_topic"><a href="<%=prefix %>/admin/topic" >话题管理</a></li>
<li id="side_admin_testunit"><a href="<%=prefix %>/admin/testunit" >测试项</a></li>
<%} %>
</ul>
<%};%>
</div>