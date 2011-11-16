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
KObject one = (KObject)data.getData("one");
out.print(JSPOut.out("head0","0","回复-编辑"));%>
<link rel="stylesheet" href="<%=sPrefix %>/fancybox/jquery.fancybox-1.3.4.css" type="text/css" media="screen" />
<script src="<%=sPrefix %>/fancybox/jquery.fancybox-1.3.4.pack.js" type="text/javascript"></script>
<script src="<%=sPrefix %>/js/jquery.validate.min.js" type="text/javascript"></script>
<script type="text/javascript">
$.sPrefix = "<%=sPrefix %>";$.prefix="<%=prefix %>";
$(function(){
	
//处理请求
$.validator.dealAjax = {
	bt:$("#submitBT"),
	loading:function(){abox("回复编辑","请稍候...");},
	ok:function(data){
		if(!isNaN(data)){
			var bt1 = "<a href=\"javascript:window.location='<%=prefix%>/topic/"+data+"';\" class=\"aButton\">查看话题</a>";
			abox("回复编辑","<div class='reOk'>回复编辑成功！ &nbsp;"+bt1+" <a href=\"javascript:window.location =('"+$.lo+"');\" class=\"aButton\">返回列表</a></div>");
		}else{abox("回复编辑","<div class='reErr'>回复编辑失败! &nbsp;<a href='javascript:$.fancybox.close();' class=\"aButton\">关闭</a></div>");};
	},
	err:function(){
		abox("回复编辑","<div class='reErr'>回复编辑失败! &nbsp;<a href='javascript:$.fancybox.close();' class=\"aButton\">关闭</a></div>");
	}
};
//开始验证
$('#comm_form').validate({
    rules: {
        c_text:{
            required:true,
            rangelength:[6,2000]
        }
    }
});
});
function aSubmit(){
	$("#comm_form").submit();
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
<div class="aboxTitle">编辑回复</div>
<div class="aboxContent">
<form action='<%=prefix+"/comm/"+one.getId()%>/a_u' method='post' id="comm_form">

<p>内容：<span class="red">*</span><br />
<textarea name="c_text" rows="3" cols="3" style="height:200px;"><%= one.getProp("text")%></textarea></p>
<input type="hidden" id="topic_id" name="topic_id" value="<%=one.getId()%>" />
</form>

<p><a href="javascript:aSubmit();" id="submitBT" class="aButton tx_center" style="width:60px;">保存</a><a href="javascript:history.go(-1);" class="aButton tx_center" style="width:60px;">返回</a></p>

</div>
</div>
		<div class="clear"></div>
		</div>
<% out.println(JSPOut.out("foot0")); %>