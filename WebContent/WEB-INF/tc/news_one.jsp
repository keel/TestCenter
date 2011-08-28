<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="com.k99k.khunter.*,com.k99k.tools.*" session="false" %>
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
KObject news_one = (KObject)data.getData("news_one");
out.print(JSPOut.out("head0","0","公告"));%>
<script type="text/javascript">
function showHtml(target) {
	var s = $(target).html(),pa = $(target).parent();;
	$(target).remove();
	s=s.replace(/\x20/g,"&nbsp;");
	s=s.replace(/\n/g,"<br />") ;
	s=s.replace( /(http[s]?:\/\/[\w-]*(\.[\w-]*)+)/ig ,"<a href='$1' target='_blank'>$1</a>") ;
	pa.append($("<div>"+s+"</div>"));
}
$(function(){
	$("#side_gg a").addClass("sideON");
	showHtml("#news_text");
});
</script>
<%out.print(JSPOut.out("main0","0",user.getName())); %>
<jsp:include page="sidenav.jsp" flush="false" > 
  <jsp:param name="lv" value="<%=user.getLevel() %>" /> 
  <jsp:param name="type" value="<%=user.getType() %>" /> 
  <jsp:param name="gg" value='<%=user.getProp("newNews").toString() %>' /> 
  <jsp:param name="tt" value='<%=user.getProp("newTasks").toString() %>' /> 
</jsp:include>

		<div id="mainContent">
<div class="abox">
<div class="aboxTitle"><div style="float:left;"><%=news_one.getName() %></div> <div style="font-size:12px;color:#ccc;float:right;padding-top:3px;"> <%=news_one.getCreatorName() %> &nbsp;&nbsp; ( <%=StringUtil.getFormatDateString("yyyy-MM-dd hh:mm:ss",news_one.getCreateTime()) %> ) </div></div>
<div class="aboxContent" style="padding:20px;"><pre id="news_text"><%=news_one.getProp("text") %></pre></div>
<div style="padding:20px;"><a href="<%=prefix%>/news" class="aButton">返回公告列表</a></div>
</div>
		<div class="clear"></div>
		</div>
<% out.println(JSPOut.out("foot0")); %>