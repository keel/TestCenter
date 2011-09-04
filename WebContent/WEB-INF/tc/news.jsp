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
if(list != null){
	KObject count = list.remove(0);
	int cc = Integer.parseInt(count.getId()+"");
	pn = (cc%pz>0)?(cc/pz+1):cc/pz;
}
out.print(JSPOut.out("head0","0","公告"));
%>
<script src="<%=sPrefix%>/js/pagenav.min.js" type="text/javascript"></script>
<script type="text/javascript">

function delNews(id){
	var r=confirm("确认删除此条公告吗？\r\n\r\n"+$("#news_"+id+" a").text());
	if (r==true){
		$.post("<%=prefix %>/news/del", "id="+id ,function(data) {
			if(data=="ok"){alert("删除成功");window.location = window.location;};
		});
	}
	return false;
}
function search(){
	var k = $("#search_key").val();
	if(k!=null && $.trim(k).length>1){
	window.location="<%=prefix %>/news/search?k="+k;
	}else{window.location="<%=prefix %>/news";}
}
$(function(){
	$("#side_gg a").addClass("sideON");
	$("#search_key").keypress(function(event) {
		if ( event.which == 13 ) {
			search();
		}
	});
	pageNav.fn = function(p,pn){
	    if(p != <%=p%>){
	    	window.location = "<%=prefix%>/news?p="+p+"&pz="+<%=pz%>;
	    }
	};
	pageNav.go(<%=p%>,<%=pn%>);
	var unread = <%=user.getProp("unReadNews")%>;
	if(unread && unread.length>0){
		for(var i=0,j=unread.length;i<j;i++){
			$("#news_"+unread[i]+" a").prepend("(未读) ").addClass("red");
		}
	}
});

</script>
<% out.println(JSPOut.out("main0","0",user.getName())); %>
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
int usertype = Integer.parseInt(user.getType());
if(usertype>=4){%><div style="float:right;padding:2px 0 0 0;"><a href="<%=prefix%>/news/new" class="aButton">新建公告</a></div><%} %>
</div>

<div>
<table width="100%" class="table_list">
<tr>
<th style="width:50px;">ID</th><th>标题</th><th style="width:140px;">时间</th><th style="width:60px;">发布人</th><%if(usertype>=4){%><th style="width:100px;">操作</th><%} %>
</tr>
<%
if(list==null){out.print("<td></td><td>暂无</td><td> </td><td> </td>");if(usertype>=4){out.print("<td></td>");}}
else{
	StringBuilder sb = new StringBuilder();
	Iterator<KObject> it = list.iterator();
	while(it.hasNext()){
		KObject gg = it.next();
		if(usertype<Integer.parseInt(gg.getType())){
			continue;
		}
		sb.append("<tr><td>").append(gg.getId()).append("<td style='text-align: left;' id='news_").append(gg.getId()).append("'><a href='");
		sb.append(prefix).append("/news/").append(gg.getId()).append("' class='fullA");
		if(gg.getLevel()>0){
			sb.append(" blueBold'>(置顶) ");
		}else{sb.append("'>");}
		sb.append(gg.getName()).append("</a></td><td>").append(StringUtil.getFormatDateString("yyyy-MM-dd hh:mm:ss",gg.getCreateTime()));
		sb.append("</td><td>").append(gg.getCreatorName()).append("</td>");
		if(usertype>=4){
			sb.append("<td><a href='").append(prefix).append("/news/").append(gg.getId());
			sb.append("?edit=true' class='aButton'>编辑</a><a href='javascript:delNews(").append(gg.getId()).append(");' class='aButton'>删除</a></td>");
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