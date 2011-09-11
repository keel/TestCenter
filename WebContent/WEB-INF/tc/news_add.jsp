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
out.print(JSPOut.out("head0","0","公告-新建"));%>
<link rel="stylesheet" href="<%=sPrefix %>/fancybox/jquery.fancybox-1.3.4.css" type="text/css" media="screen" />
<script src="<%=sPrefix %>/fancybox/jquery.fancybox-1.3.4.pack.js" type="text/javascript"></script>
<script src="<%=sPrefix %>/js/swfupload.min.js" type="text/javascript"></script>
<script src="<%=sPrefix %>/js/swfupload_tc.js" type="text/javascript"></script>
<script src="<%=sPrefix %>/js/jquery.validate.min.js" type="text/javascript"></script>
<script type="text/javascript"><!--
$.sPrefix = "<%=sPrefix %>";$.prefix="<%=prefix %>";
$(function(){
	$("#side_gg a").addClass("sideON");
//处理请求
$.validator.dealAjax = {
	bt:$("#submitBT"),
	loading:function(){abox("公告发表","请稍候...");},
	ok:function(data){
		if(!isNaN(data)){
			var bt1 = "<a href=\"javascript:window.location='<%=prefix%>/news/"+data+"';\" class=\"aButton\">查看公告</a>";
			abox("公告发表","<div class='reOk'>公告发表成功！ &nbsp;"+bt1+" <a href=\"javascript:window.location =('<%=prefix %>/news');\" class=\"aButton\">返回列表</a></div>");
		}else{abox("公告发表","<div class='reErr'>公告发表失败! &nbsp;<a href='javascript:$.fancybox.close();' class=\"aButton\">关闭</a></div>");};
	},
	err:function(){
		abox("公告发表","<div class='reErr'>公告发表失败! &nbsp;<a href='javascript:$.fancybox.close();' class=\"aButton\">关闭</a></div>");
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
            rangelength:[6,2000]
        }
    }
});
initUpload("<%=user.getName() %>");
});
function aSubmit(){
	var ff = [];
	$(".files_name").each(function(){
		ff.push(encodeURIComponent($(this).text()));
	});
	//console.log(ff);
	$("#news_files").val(ff.join(","));
	$("#news_form").submit();
};
-->
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
<div class="aboxTitle">新建公告</div>
<div class="aboxContent">
<form action="<%=prefix%>/news/add" method="post" id="news_form">
<p>标题：<br />
<input type="text" name="news_name" style="width:90%;padding:5px;margin:0;" /></p>

<p>内容：<br />
<textarea name="news_text" rows="3" cols="3" style="height:200px;"></textarea></p>
<p>显示级别：
<select name="news_type"><option value="0">所有人</option><option value="1">厂家</option><option value="2">测试员</option><option value="3">组长</option><option value="4">管理员</option></select>
置顶级别(数字最大的在顶部)：
<select name="news_level"><option value="0">无</option><option value="1">1</option><option value="2">2</option><option value="3">3</option></select>
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
<p><a href="javascript:aSubmit();" id="submitBT" class="aButton tx_center" style="width:60px;">保存</a><a href="<%=prefix%>/news" class="aButton tx_center" style="width:60px;">返回</a></p>

</div>
</div>
		<div class="clear"></div>
		</div>
<% out.println(JSPOut.out("foot0")); %>