<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="java.util.*,com.k99k.testcenter.*,com.k99k.khunter.*,com.k99k.tools.*" session="false" %>
<%
String sPrefix = KFilter.getStaticPrefix();
String prefix = KFilter.getPrefix();
Object o = request.getAttribute("[jspAttr]");
HttpActionMsg data = null;
if(o != null ){
	data = (HttpActionMsg)o;
}else{
	out.print("ERROR:100404");
	return;
}
KObject user = (KObject)data.getData("u");
Object lo = data.getData("list");
ArrayList<KObject> list = (lo==null)?null:(ArrayList<KObject>)lo;
int pn = 0;
int pz = Integer.parseInt(String.valueOf(data.getData("pz")));
int p = Integer.parseInt(String.valueOf(data.getData("p")));
String sub = String.valueOf(data.getData("sub"));
if(list != null){
	KObject count = list.remove(0);
	int cc = Integer.parseInt(count.getId()+"");
	pn = (cc%pz>0)?(cc/pz+1):cc/pz;
}
String title = (sub.equals("my")) ? "我的任务" : "任务管理";
out.print(JSPOut.out("head0","0",title));
%>
<script src="<%=sPrefix%>/js/pagenav.min.js" type="text/javascript"></script>
<script type="text/javascript">
<!--
$.isMy = <%=(sub.equals("my"))?"true":"false" %>;
function del(id){
	var r=confirm("确认删除此条任务吗？\r\n\r\n["+$("#task_"+id+" a").text()+"]");
	if (r==true){
		$.post("<%=prefix %>/tasks/a_d", "id="+id ,function(data) {
			if(data=="ok"){alert("删除成功");window.location = window.location;};
		});
	}
	return;
}
function search(){
	var k = $("#search_key").val(),s=$("#searchState").val(),c=$("#search_com").val();
	var lo = "<%=prefix %>/tasks/",lo2=lo+"a_s?a=0";
	if($.isMy){lo+="my/";};
	if(k!=null && $.trim(k).length>=1){
		lo2=lo2+"&k="+$.trim(k);
	} if(s!=null && $.trim(s).length>=1 && s != "99"){
		lo2=lo2+"&searchState="+$.trim(s);
	} if(c!=null && $.trim(c).length>=1){
		lo2=lo2+"&com="+$.trim(c);
	}
	window.location=lo2;
}
$(function(){
	var tar = ($.isMy) ? "#side_mytask a" : "#side_task a";
	$(tar).addClass("sideON");
	$("#search_key").keypress(function(event) {
		if ( event.which == 13 ) {
			search();
		}
	});
	pageNav.fn = function(p,pn){
	    if(p != <%=p%>){
	    	var lo = window.location.href;
	    	lo = lo.replace(/[\?|\&]p=[\d]+\&pz=[\d]+/g,"");
	    	if(lo.indexOf("?")<0){lo+="?";}else{lo+="&";};
	    	lo=lo+"p="+p+"&pz="+<%=pz%>;
	    	//window.location = "<%=prefix%>/tasks"+($.isMy?"/my":"")+"?p="+p+"&pz="+<%=pz%>;
	    	window.location = lo;
	    }
	};
	pageNav.go(<%=p%>,<%=pn%>);
	var unread = <%=user.getProp("unReadTasks")%>;
	if(unread && unread.length>0){
		for(var i=0,j=unread.length;i<j;i++){
			$("#task_"+unread[i]+" a").prepend("(待处理) ").addClass("red");
		}
	}
});
-->
</script>
<% out.println(JSPOut.out("main0",new String[]{"0","1"},new String[]{user.getName(),user.getProp("company").toString()})); %>
<jsp:include page="sidenav.jsp" flush="false" > 
  <jsp:param name="lv" value="<%=user.getLevel() %>" /> 
  <jsp:param name="type" value="<%=user.getType() %>" /> 
  <jsp:param name="gg" value='<%=user.getProp("newNews").toString() %>' /> 
  <jsp:param name="tt" value='<%=user.getProp("newTasks").toString() %>' /> 
</jsp:include>

		<div id="mainContent">
<div class="search">
任务名:<input id="search_key" type="text" /> 
<%int usertype = user.getType();
if(usertype>1){
%>
公司:<input id="search_com" type="text" /> 
<%} %>
状态:<select id="searchState" name="searchState"><option value="99">全部</option><option value="0">待测</option><option value="1">测试中</option><option value="2">通过</option><option value="3">待反馈</option><option value="4">部分通过</option><option value="5">暂停</option><option value="6">结果确认中</option><option value="8">已反馈</option></select>
<a href="javascript:search();" class="aButton">搜索</a>
<%
boolean canEdit = (usertype>=4);
String ismy = (sub.equals("my")) ? "?ismy=true" : "";
if(usertype>1){
%><span style="padding-left:20px;"><a href="<%=prefix%>/tasks/add2<%=ismy%>" class="aButton">创建新任务</a></span>
<%} %>
</div>

<div>
<table width="100%" class="table_list" cellpadding="0" cellspacing="1">
<tr>
<th style="width:80px;">ID</th><th>任务名</th><th style="width:160px;">公司</th><th style="width:50px;">次数</th><th style="width:50px;">更新</th><th style="width:50px;">待办人</th><th style="width:70px;">状态</th><%if(canEdit){%><th style="width:50px;">操作</th><%} %>
</tr>
<%
if(list==null){out.print("<tr><td></td><td>暂无</td><td> </td><td> </td><td> </td><td> </td><td> </td>");if(canEdit){out.print("<td></td>");};out.print("</tr>");}
else{
	StringBuilder sb = new StringBuilder();
	String[] states = new String[]{"待测","测试中","通过","待反馈","部分通过","暂停","结果确认中","驳回","已反馈"};
	Iterator<KObject> it = list.iterator();
	while(it.hasNext()){
		KObject gg = it.next();
		sb.append("<tr><td>").append(gg.getType()).append(" - ").append(gg.getId()).append("</td><td style='text-align: left;' id='task_").append(gg.getId()).append("'><a href='");
		sb.append(prefix).append("/tasks/").append(gg.getId());
		if(sub.equals("my")){
			sb.append("?my=true");
		}
		sb.append("' class='fullA");
		if(gg.getLevel()>0){
			sb.append(" purpleBold'>(重要) ");
		}else{sb.append("'>");}
		sb.append(gg.getName()).append("</a></td><td style='font-size:12px;'><a href='").append(prefix).append("/user/one?c=").append(gg.getProp("company")).append("'>").append(gg.getProp("company"));
		sb.append("</td><td>").append(gg.getProp("testTimes"));
		sb.append("</td><td>").append((gg.getProp("updateTimes")==null)?"0":gg.getProp("updateTimes"));
		sb.append("</td><td><a href='").append(prefix).append("/user/one?u=").append(gg.getProp("operator")).append("'>").append(gg.getProp("operator"));
		sb.append("</a></td><td>").append(states[gg.getState()]).append("</td>");
		if(canEdit){
			sb.append("<td><a href='javascript:del(").append(gg.getId()).append(");' class='aButton'>删除</a></td>");
		}
		sb.append("</tr>\n");
	}
	out.print(sb);
}
%>
</table>
<div id="pageNav" style="float:right;padding:10px;"></div>
</div>
		<div class="clear"></div>
		</div>
<% out.println(JSPOut.out("foot0")); %>