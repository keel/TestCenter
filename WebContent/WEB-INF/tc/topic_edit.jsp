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
String pName = (String)data.getData("pName");
String sub = catesArr[(Integer)one.getProp("cate")];
String tag = "";
Object tags = one.getProp("tags");
if(tags!=null){
	ArrayList<Object> tagls = (ArrayList<Object>)tags;
	if(!tagls.isEmpty()){tag = tagls.get(0).toString();}
}
out.print(JSPOut.out("head0","0","话题-编辑"));%>
<link rel="stylesheet" href="<%=sPrefix %>/fancybox/jquery.fancybox-1.3.4.css" type="text/css" media="screen" />
<script src="<%=sPrefix %>/fancybox/jquery.fancybox-1.3.4.pack.js" type="text/javascript"></script>
<script src="<%=sPrefix %>/js/swfupload.min.js" type="text/javascript"></script>
<script src="<%=sPrefix %>/js/swfupload_tc.js" type="text/javascript"></script>
<script src="<%=sPrefix %>/js/jquery.validate.min.js" type="text/javascript"></script>
<script type="text/javascript">
$.sPrefix = "<%=sPrefix %>";$.prefix="<%=prefix %>";
$.sub="<%=sub%>";$.tag="<%=tag%>";
$.lo=$.prefix+"/topic"+(($.sub)?"/"+$.sub:"")+(($.tag)?"/"+$.tag:"");
$(function(){
	var tar = "#side_topic_"+$.sub;
	if($.tag!=""){tar=tar+"_"+$.tag;}tar+=" a";
	$(tar).addClass("sideON");
<%
if(StringUtil.isStringWithLen(pName,2)){
	out.println("$('#t_name').val('"+pName+"').attr('readOnly','true').addClass('gray');");	
}
%>
	
//处理请求
$.validator.dealAjax = {
	bt:$("#submitBT"),
	loading:function(){abox("话题编辑","请稍候...");},
	ok:function(data){
		if(!isNaN(data)){
			var bt1 = "<a href=\"javascript:window.location='<%=prefix%>/topic/"+data+"';\" class=\"aButton\">查看话题</a>";
			abox("话题编辑","<div class='reOk'>话题编辑成功！ &nbsp;"+bt1+" <a href=\"javascript:window.location =('"+$.lo+"');\" class=\"aButton\">返回列表</a></div>");
		}else{abox("话题编辑","<div class='reErr'>话题编辑失败! &nbsp;<a href='javascript:$.fancybox.close();' class=\"aButton\">关闭</a></div>");};
	},
	err:function(){
		abox("话题编辑","<div class='reErr'>话题编辑失败! &nbsp;<a href='javascript:$.fancybox.close();' class=\"aButton\">关闭</a></div>");
	}
};
//开始验证
$('#topic_form').validate({
    /* 设置验证规则 */
    rules: {
		t_name: {
            required:true,
            rangelength:[2,50]
        },
        t_text:{
            required:true,
            rangelength:[6,2000]
        }
    }
});
initUpload("<%=user.getName() %>",null,null,null,null,$.prefix+"/upload2");
var type = <%=one.getType()%>;
$("#t_type").val(type);
var level = <%=one.getLevel()%>;
$("#t_level").val(level);
var lock = <%=one.getProp("lock")%>;
$("#t_lock").val(lock);
});
function aSubmit(){
	var ff = [];
	$(".files_name").each(function(){
		ff.push(encodeURIComponent($(this).text()));
	});
	//console.log(ff);
	$("#news_files").val(ff.join(","));
	$("#topic_form").submit();
};
</script>
<%out.print(JSPOut.out("main0","0",user.getName())); %>
<jsp:include page="sidenav.jsp" flush="false" > 
  <jsp:param name="lv" value="<%=user.getLevel() %>" /> 
  <jsp:param name="type" value='<%=String.valueOf(user.getType()) %>' /> 
  <jsp:param name="gg" value='<%=user.getProp("newNews").toString() %>' /> 
  <jsp:param name="tt" value='<%=user.getProp("newTasks").toString() %>' /> 
</jsp:include>

		<div id="mainContent">
<div class="abox">
<div class="aboxTitle">编辑话题</div>
<div class="aboxContent">
<form action="<%=prefix%>/topic/a_u" method="post" id="topic_form">
<p>标题：<span class="red">*</span><br />
<input type="text" id="t_name" name="t_name" style="width:90%;padding:5px;margin:0;" value="<%= one.getName()%>" /></p>

<p>内容：<span class="red">*</span><br />
<textarea name="t_text" rows="3" cols="3" style="height:200px;"><%= one.getProp("text")%></textarea></p>
<% if(user.getType()>10){ %>
<p>显示级别：
<select name="t_type" id="t_type"><option value="0">所有人</option><option value="1">厂家</option><option value="2">测试员</option><option value="3">组长</option><option value="4">管理员</option></select>
置顶级别(数字最大的在顶部)：
<select name="t_level" id="t_level"><option value="0">无</option><option value="1">1</option><option value="2">2</option><option value="3">3</option></select>
回复：
<select name="t_lock" id="t_lock"><option value="0">允许</option><option value="1">禁止</option></select>
</p>
<% }%>
<input type="hidden" id="news_files" name="news_files" value="" />
<input type="hidden" id="topic_id" name="topic_id" value="<%=one.getId()%>" />
<input type="hidden" id="t_cate" name="t_cate" value="<%=one.getProp("cate")%>" />
</form>

<form name="fileupload" id="fileupload" action="<%=prefix %>/upload" method="post" enctype="multipart/form-data">
	<div id="swfBT">
		<div id="spanSWFUploadButton">请稍候...</div> 
		<span id="uploadInfo"> &nbsp;&nbsp;注:文件最大不超过100M,格式限定为rar,zip,apk,jpg,gif,png,jar,doc,docx,xls,xlsx,ppt,pptx,txt</span>
	</div>
	<div id="upFiles"></div>
</form>
<%
StringBuilder sb = new StringBuilder();
Object o_f = one.getProp("files");
if(o_f !=null){
	ArrayList<String> fileList = (ArrayList<String>)o_f;
	int i = 0;
	if(fileList != null && !fileList.isEmpty()){
		sb.append("<div class='bold' style='padding-top:10px;'>文件列表</div>");
		Iterator<String> it = fileList.iterator();
		while(it.hasNext()){
			String f=it.next();
			sb.append("<div class='file_upload' id='fu_").append(i).append("'><a href='").append(prefix).append("/file/").append(f).append("'>").append(f).append("</a> [ <a href='javascript:delFile(").append(i).append(");'>删除</a> ]<span class=\"files_name\">").append(f).append("</span></div>");
			i++;
		}
		out.print(sb);
	}
}
%>
<p><a href="javascript:aSubmit();" id="submitBT" class="aButton tx_center" style="width:60px;">保存</a><a href="javascript:history.go(-1);" class="aButton tx_center" style="width:60px;">返回</a></p>

</div>
</div>
		<div class="clear"></div>
		</div>
<% out.println(JSPOut.out("foot0")); %>