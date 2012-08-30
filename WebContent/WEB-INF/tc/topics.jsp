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
Object lo = data.getData("list");
ArrayList<KObject> list = (lo==null)?null:(ArrayList<KObject>)lo;
int pn = 0;
int pz = Integer.parseInt(String.valueOf(data.getData("pz")));
int p = Integer.parseInt(String.valueOf(data.getData("p")));
String sub = String.valueOf(data.getData("sub"));
String tag = (data.getData("tag")!=null)?String.valueOf(data.getData("tag")):"";
if(list != null){
	KObject count = list.remove(0);
	int cc = Integer.parseInt(count.getId()+"");
	pn = (cc%pz>0)?(cc/pz+1):cc/pz;
}
String title = String.valueOf(data.getData("title"));
out.print(JSPOut.out("head0","0",title));
%>
<script src="<%=sPrefix%>/js/pagenav.min.js" type="text/javascript"></script>
<script type="text/javascript">
$.sub="<%=sub%>";$.tag="<%=tag%>";
var lo = "<%=prefix %>/topic/"+$.sub;
if($.tag!=""){lo=lo+"/"+$.tag;}
$.lo = lo;
function search(){
	var k = $("#search_key").val();
	if(k!=null && $.trim(k).length>1){
		window.location="<%=prefix %>/topic/a_s?sub="+$.sub+"&tag="+$.tag+"&k="+k;
	}else{window.location=lo;}
}
$(function(){
	var tar = "#side_topic_"+$.sub;
	if($.tag!=""){tar=tar+"_"+$.tag;};tar+=" a";
	$(tar).addClass("sideON");
	$("#search_key").keypress(function(event) {
		if ( event.which == 13 ) {
			search();
		}
	});
	pageNav.fn = function(p,pn){
	    if(p != <%=p%>){
	    	window.location = $.lo+"?p="+p+"&pz="+<%=pz%>;
	    }
	};
	pageNav.go(<%=p%>,<%=pn%>);
});

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
查询:<select><option value="title">标题</option></select> <input id="search_key" type="text" /><a href="javascript:search();" class="aButton">搜索</a>
<%
int usertype = user.getType();
boolean canEdit = (usertype>=4);
String para = sub;
if(!tag.equals("")){para=para+"/"+tag;}
if(sub.equals("pub") || sub.equals("company") || user.getType()>10 ){
%>
<span style="padding-left:20px;"><a href="<%=prefix%>/topic/add/<%=para%>" class="aButton">创建新话题</a></span>
<%} %>
</div>

<div>
<table width="100%" class="table_list" cellpadding="0" cellspacing="1">
<tr>
<th style="width:50px;">ID</th><th>标题</th><th style="width:160px;">发布时间</th><th style="width:80px;">创建人</th><th style="width:60px;">回复数</th>
</tr>
<%
if(list==null){out.print("<td></td><td>暂无</td><td> </td><td> </td><td></td>");}
else{
	StringBuilder sb = new StringBuilder();
	Iterator<KObject> it = list.iterator();
	while(it.hasNext()){
		KObject gg = it.next();
		if(usertype<gg.getType()){
			continue;
		}
		sb.append("<tr><td>").append(gg.getId()).append("<td style='text-align: left;' id='topic_").append(gg.getId()).append("'><a href='");
		sb.append(prefix).append("/topic/").append(gg.getId());
		sb.append("' class='fullA");
		if(gg.getLevel()>0){
			sb.append(" topicLv").append(gg.getLevel()).append("'>(重要) ");
		}else{sb.append("'>");}
		sb.append(gg.getName()).append("</a></td><td>").append(StringUtil.getFormatDateString("yyyy-MM-dd hh:mm:ss",gg.getCreateTime()));
		sb.append("</td><td><a href='").append(prefix).append("/user/one?u=").append(gg.getCreatorName()).append("'>").append(gg.getCreatorName()).append("</a></td>");
		sb.append("<td>").append(gg.getProp("commsCount")).append("</td>");
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