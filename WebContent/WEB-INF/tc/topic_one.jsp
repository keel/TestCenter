<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="java.util.*,com.k99k.khunter.*,com.k99k.tools.*" session="false" %>
<%!
static final String[] catesArr = new String[]{"pub","company","doc"};
 %>
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
KObject one = (KObject)data.getData("one");
String sub = catesArr[(Integer)one.getProp("cate")];
String tag = "";
Object tags = one.getProp("tags");
if(tags!=null){
	ArrayList<Object> tagls = (ArrayList<Object>)tags;
	if(!tagls.isEmpty()){tag = tagls.get(0).toString();}
}
String lo = prefix + "/topic/" + sub + ((!tag.equals(""))?"/"+tag:"");
out.print(JSPOut.out("head0","0",one.getName()));%>
<script src="<%=sPrefix %>/js/swfupload.min.js" type="text/javascript"></script>
<script src="<%=sPrefix %>/js/swfupload_tc.js" type="text/javascript"></script>
<script type="text/javascript">
$.sPrefix = "<%=sPrefix %>";$.prefix="<%=prefix %>";
$.sub="<%=sub%>";$.tag="<%=tag%>";
function del(id){
	var r=confirm("确认删除此条话题吗？\r\n\r\n["+$(".aboxTitle>div").text()+"]");
	if (r==true){
		$.post("<%=prefix %>/news/del", "id="+id ,function(data) {
			if(data=="ok"){alert("删除成功");window.location = "<%=prefix %>/news";};
		});
	}
	return;
}
function showHtml(target) {
	var s = $(target).html(),pa = $(target).parent();;
	$(target).remove();
	s=s.replace(/\x20/g,"&nbsp;");
	s=s.replace(/\n/g,"<br />") ;
	s=s.replace( /(http[s]?:\/\/[\w-\/]*(\.[\w-\/]*)+)/ig ,"<a href='$1' target='_blank'>$1</a>") ;
	pa.append($("<div>"+s+"</div>"));
}
$(function(){
	var tar = "#side_topic_"+$.sub;
	if($.tag!=""){tar=tar+"_"+$.tag;}tar+=" a";
	$(tar).addClass("sideON");
	showHtml("#t_text");
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
<div class="aboxTitle"><div><%=one.getName() %></div> </div>
<div class="aboxSub"><div style="color:#6E747B;float:left;padding-top:7px;"> <%=one.getCreatorName() %> &nbsp; 发布于： <%=StringUtil.getFormatDateString("yyyy-MM-dd hh:mm:ss",one.getCreateTime()) %>  </div>
<%if(user.getType()>10){ 
	String ggid = String.valueOf(one.getId());
	String edit = prefix+"/topic/"+ggid+"?edit=true";
%>
<a href="<%=edit%>" class="aButton">编辑</a>
<a href="javascript:del(<%=ggid%>);" class="aButton">删除</a>
<%} %>
<a href="<%=lo%>" class="aButton">返回列表</a></div>
<div class="aboxContent" style="padding:20px;">
<div><pre id="t_text"><%=one.getProp("text") %></pre></div>
<%
StringBuilder sb = new StringBuilder();
Object o_f = one.getProp("files");
if(o_f !=null){
	ArrayList<String> fileList = (ArrayList<String>)o_f;
	if(fileList != null && !fileList.isEmpty()){
		sb.append("<div class='bold' style='padding-top:10px;'>文件列表</div>");
		Iterator<String> it = fileList.iterator();
		while(it.hasNext()){
			String f=it.next();
			sb.append("<div class='file_upload'><a href='").append(prefix).append("/file/").append(f).append("'>").append(f).append("</a></div>");
		}
		out.print(sb);
	}
}
%>
</div>
<div class="aboxSub2"><a href="<%=lo%>" class="aButton">返回列表</a></div>
</div>
		<div class="clear"></div>
		</div>
<% out.println(JSPOut.out("foot0")); %>