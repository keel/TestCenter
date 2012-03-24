<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="java.util.*,com.k99k.khunter.*,com.k99k.tools.*" session="false" %>
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
KObject user_one = (KObject)data.getData("one");
out.print(JSPOut.out("head0","0",user_one.getName()));%>
<script type="text/javascript">
$(function(){
	$("#side_gg a").addClass("sideON");
});
</script>
<%out.print(JSPOut.out("main0",new String[]{"0","1"},new String[]{user.getName(),user.getProp("company").toString()})); %>
<jsp:include page="sidenav.jsp" flush="false" > 
  <jsp:param name="lv" value="<%=user.getLevel() %>" /> 
  <jsp:param name="type" value="<%=user.getType() %>" /> 
  <jsp:param name="gg" value='<%=user.getProp("newNews").toString() %>' /> 
  <jsp:param name="tt" value='<%=user.getProp("newTasks").toString() %>' /> 
</jsp:include>

		<div id="mainContent">
<div class="abox">
<div class="aboxTitle"><div><%=user_one.getName() %></div> </div>
<div class="aboxSub"><div style="color:#6E747B;float:left;padding-top:7px;"> <%=user_one.getProp("company") %>  </div>&nbsp;
<%if(user.getType()>10 || user.getId() == user_one.getId()){ 
	String edit = prefix+"/user/one?u="+user_one.getName()+"&edit=true";
%>
<a href="<%=edit%>" class="aButton">编辑</a>
<%} %>
</div>
<div class="aboxContent" style="padding:20px;">
<div>用户名: <%=user_one.getName() %></div>
<div>公司名: <%=user_one.getProp("company") %></div>
<div>手机号码: <%=user_one.getProp("phoneNumber") %></div>
<div>电子邮箱: <%=user_one.getProp("email") %></div>
<div>QQ: <%=user_one.getProp("qq") %></div>
<br />
<div><a href="javascript:history.go(-1);" class="aButton tx_center">返回前一页</a></div>
</div>
</div>
		<div class="clear"></div>
		</div>
<% out.println(JSPOut.out("foot0")); %>