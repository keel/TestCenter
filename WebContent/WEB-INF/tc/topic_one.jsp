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
ArrayList<KObject> comms = (ArrayList<KObject>)data.getData("comms");
String sub = catesArr[(Integer)one.getProp("cate")];
String tag = "";
Object tags = one.getProp("tags");
if(tags!=null){
	ArrayList<Object> tagls = (ArrayList<Object>)tags;
	if(!tagls.isEmpty()){tag = tagls.get(0).toString();}
}
int pn = 0;
int pz = Integer.parseInt(String.valueOf(data.getData("pz")));
int p = Integer.parseInt(String.valueOf(data.getData("p")));
if(comms != null){
	KObject count = comms.remove(0);
	int cc = Integer.parseInt(count.getId()+"");
	pn = (cc%pz>0)?(cc/pz+1):cc/pz;
}
String lo = prefix + "/topic/" + sub + ((!tag.equals(""))?"/"+tag:"");
out.print(JSPOut.out("head0","0",one.getName()));%>
<link rel="stylesheet" href="<%=sPrefix %>/fancybox/jquery.fancybox-1.3.4.css" type="text/css" media="screen" />
<script src="<%=sPrefix %>/fancybox/jquery.fancybox-1.3.4.pack.js" type="text/javascript"></script>
<script src="<%=sPrefix %>/js/jquery.validate.min.js" type="text/javascript"></script>
<script src="<%=sPrefix%>/js/pagenav.min.js" type="text/javascript"></script>
<script type="text/javascript">
$.sPrefix = "<%=sPrefix %>";$.prefix="<%=prefix %>";
$.sub="<%=sub%>";$.tag="<%=tag%>";
$.lo = "<%=prefix+"/topic/"+one.getId() %>";
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
function aSubmit(){
	$("#replyForm").submit();	
}
$(function(){
	var tar = "#side_topic_"+$.sub;
	if($.tag!=""){tar=tar+"_"+$.tag;}tar+=" a";
	$(tar).addClass("sideON");
	$(".t_text").each(function(){showHtml(this);});

	pageNav.fn = function(p,pn){
	    if(p != <%=p%>){
	    	window.location = $.lo+"?p="+p+"&pz="+<%=pz%>;
	    }
	};
	pageNav.go(<%=p%>,<%=pn%>);

	//处理请求
	var failed = "<div class='reErr'>回复发表失败! &nbsp;<a href='javascript:$.fancybox.close();' class=\"aButton\">关闭</a></div>";
	$.validator.dealAjax = {
		bt:$("#replyBT"),
		loading:function(){abox("提交回复","请稍候...");},
		ok:function(data){
			if(!isNaN(data)){
				abox("发表回复","<div class='reOk'>回复发表成功！ &nbsp;<a href=\"javascript:window.location ='"+$.lo+"';\" class=\"aButton\">查看</a></div>");
			}else{abox("发表回复",failed);};
		},
		err:function(){
			abox("发表回复",failed);
		}
	};
	//开始验证
	$('#replyForm').validate({
	    rules: {
	        c_text:{
	            required:true,
	            rangelength:[6,2000]
	        }
	    }
	});
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
<div><pre class="t_text"><%=one.getProp("text") %></pre></div>
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
<div id="comms">
<%
if(comms != null && !comms.isEmpty()) {
	sb = new StringBuilder();
	Iterator<KObject> it = comms.iterator();
	while(it.hasNext()){
		KObject comm = it.next();
		sb.append("<div class=\"comm\" id=\"c_").append(comm.getId()).append("\"><div class=\"commTitle\"><a href=\"").append(prefix).append("/user/one?u=").append(comm.getCreatorName()).append("\" class=\"sideON\">");
		sb.append(comm.getCreatorName()).append("</a>  发表于  <span class=\"blue bold\">").append(StringUtil.getFormatDateString("yyyy-MM-dd hh:mm:ss",comm.getCreateTime()));
		sb.append("</span> </div><div><pre class=\"t_text\">").append(comm.getProp("text"));
		sb.append("</pre></div></div>\r\n");
	}
	sb.append("<div id=\"pageNav\" style=\"padding:5px 20px;\"></div>");
	out.print(sb);
}
if(StringUtil.objToNonNegativeInt(one.getProp("lock")) < 1){
%>
<div id="reply" style="padding:10px 20px;border-top:1px dotted #ccc;">
<form action='<%=prefix+"/comm/"+one.getId()+"/a_a" %>' id="replyForm">
<div class="bold">发表回复：</div>
<textarea rows="3" cols="3" style="height:100px;" name="c_text"></textarea><br />
<a class="aButton" href="javascript:aSubmit();" id="replyBT" style="margin-top:5px;">提交回复</a>
</form>
</div>
<%} %>
</div>
<div class="aboxSub2"><a href="<%=lo%>" class="aButton">返回列表</a></div>
</div>
		<div class="clear"></div>
		</div>
<% out.println(JSPOut.out("foot0")); %>