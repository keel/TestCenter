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
KObject news_one = (KObject)data.getData("news_one");
out.print(JSPOut.out("head0","0","公告-编辑"));%>
<link rel="stylesheet" href="<%=sPrefix %>/fancybox/jquery.fancybox-1.3.4.css" type="text/css" media="screen" />
<script src="<%=sPrefix %>/fancybox/jquery.fancybox-1.3.4.pack.js" type="text/javascript"></script>
<script src="<%=sPrefix %>/js/swfupload.min.js" type="text/javascript"></script>
<script src="<%=sPrefix %>/js/swfupload_tc.js" type="text/javascript"></script>
<script src="<%=sPrefix %>/js/jquery.validate.min.js" type="text/javascript"></script>
<script type="text/javascript">
$.sPrefix = "<%=sPrefix %>";$.prefix="<%=prefix %>";
$(function(){
	$("#side_gg a").addClass("sideON");
//处理请求
$.validator.dealAjax = {
	bt:$("#submitBT"),
	ok:function(data){
		if(data=="ok"){abox("公告编辑","<div class='reOk'>公告更新成功！ &nbsp;<a href=\"javascript:window.location='<%=prefix%>/news/<%=news_one.getId()%>';\" class=\"aButton\">查看公告</a> <a href=\"javascript:window.location =('<%=prefix %>/news');\" class=\"aButton\">返回列表</a></div>");};
	},
	err:function(){
		abox("公告编辑","<div class='reErr'>公告更新失败! &nbsp;<a href='javascript:$.fancybox.close();' class=\"aButton\">关闭</a></div>");
	}
};
//开始验证
$('#news_form').validate({
    /* 设置验证规则 */
    rules: {
		news_name: {
            required:true,
            rangelength:[2,50]
        },
        news_text:{
            required:true,
            rangelength:[6,8000]
        }
    }
});
initUpload("<%=user.getName() %>");
//初始化select
var level = <%=news_one.getLevel()%>;
var type = <%=news_one.getType()%>;
$("#news_type").val(type);
$("#news_level").val(level);
$.hasFileIndex = $(".files_name").length;
});
function aSubmit(){
	var ff = [];
	$(".files_name").each(function(){
		ff.push(encodeURIComponent($(this).text()));
	});
	$("#news_files").val(ff.join(","));
	$("#news_form").submit();
};
</script>
<%out.print(JSPOut.out("main0",new String[]{"0","1"},new String[]{user.getName(),user.getProp("company").toString()})); %>
<jsp:include page="sidenav.jsp" flush="false" > 
  <jsp:param name="lv" value="<%=user.getLevel() %>" /> 
  <jsp:param name="type" value='<%=String.valueOf(user.getType()) %>' /> 
  <jsp:param name="gg" value='<%=user.getProp("newNews").toString() %>' /> 
  <jsp:param name="tt" value='<%=user.getProp("newTasks").toString() %>' /> 
</jsp:include>

		<div id="mainContent">
<div class="abox">
<div class="aboxTitle">编辑公告</div>
<div class="aboxContent">
<form action="<%=prefix%>/news/update" method="post" id="news_form">
<p>标题：<br />
<input type="text" name="news_name" style="width:90%;padding:5px;margin:0;" value="<%=news_one.getName()%>" /></p>

<p>内容：<br />
<textarea name="news_text" rows="3" cols="3" style="height:200px;"><%=news_one.getProp("text")%></textarea></p>
<p>显示级别：
<select name="news_type" id="news_type"><option value="0">所有人</option><option value="1">厂家</option><option value="2">测试员</option><option value="3">组长</option><option value="4">管理员</option></select>
置顶级别(数字最大的在顶部)：
<select name="news_level" id="news_level"><option value="0">无</option><option value="1">1</option><option value="2">2</option><option value="3">3</option></select>
<input type="hidden" name="id" value="<%=news_one.getId()%>" />
</p>
<input type="hidden" id="news_files" name="news_files" value="" />
</form>
<form name="fileupload" id="fileupload" action="<%=prefix %>/upload" method="post" enctype="multipart/form-data">
	<div id="swfBT">
		<div id="spanSWFUploadButton">载入中...</div> 
		<span id="uploadInfo">图片最大不超过3M,图片格式为jpg,png,gif</span>
	</div>
	<div id="upFiles"></div>
</form>
<%
StringBuilder sb = new StringBuilder();
Object o_f = news_one.getProp("files");
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