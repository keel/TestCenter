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
Boolean ismy = request.getParameter("ismy")!=null && request.getParameter("ismy").equals("true");
out.print(JSPOut.out("head0","0","创建新任务"));%>
<link rel="stylesheet" href="<%=sPrefix %>/fancybox/jquery.fancybox-1.3.4.css" type="text/css" media="screen" />
<script src="<%=sPrefix %>/fancybox/jquery.fancybox-1.3.4.pack.js" type="text/javascript"></script>
<script src="<%=sPrefix %>/js/swfupload.min.js" type="text/javascript"></script>
<script src="<%=sPrefix %>/js/swfupload_tc.js" type="text/javascript"></script>
<script src="<%=sPrefix %>/js/jquery.validate.min.js" type="text/javascript"></script>
<script type="text/javascript">
$.sPrefix = "<%=sPrefix %>";$.prefix="<%=prefix %>";
$.isMy = <%=(ismy)?"true":"false" %>;
$(function(){
	var tar = ($.isMy) ? "#side_mytask a" : "#side_task a";
	$(tar).addClass("sideON");
//处理请求
$.validator.dealAjax = {
	bt:$("#submitBT"),
	loading:function(){abox("创建任务","请稍候...");},
	ok:function(data){
		if(!isNaN(data)){
			var bt1 = "<a href=\"javascript:window.location='<%=prefix%>/tasks/"+data+"';\" class=\"aButton\">查看任务</a>";
			abox("创建任务","<div class='reOk'>创建任务成功！ &nbsp;"+bt1+" <a href=\"javascript:window.location =('<%=prefix %>/tasks');\" class=\"aButton\">返回列表</a></div>");
		}else{abox("创建任务","<div class='reErr'>创建任务失败! &nbsp;<a href='javascript:$.fancybox.close();' class=\"aButton\">关闭</a></div>");};
	},
	err:function(){
		abox("创建任务","<div class='reErr'>创建任务失败! &nbsp;<a href='javascript:$.fancybox.close();' class=\"aButton\">关闭</a></div>");
	}
};
//开始验证
$('#add_form').validate({
    /* 设置验证规则 */
    rules: {
		task_info: {
            rangelength:[1,500]
        },
        task_type:{
            required:true
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
<div class="aboxTitle">创建新任务</div>
<div class="aboxContent">
<form action="<%=prefix%>/tasks/a_a" method="post" id="add_form">
<p>任务类型：<span class="red">*</span><br />
<input type="radio" name="task_type" id="type0" value="0" /><label for="type0">新产品</label>
<input type="radio" name="task_type" id="type1" value="1" /><label for="type1">修改后复测</label>
<input type="radio" name="task_type" id="type2" value="2" /><label for="type2">新机型适配</label>
<input type="radio" name="task_type" id="type3" value="3" /><label for="type3">DEMO测试</label>
<input type="radio" name="task_type" id="type4" value="4" /><label for="type4">指定复测</label>
<input type="radio" name="task_type" id="type5" value="5" /><label for="type5">拨测</label>
<input type="radio" name="task_type" id="type6" value="6" /><label for="type6">其他</label>
</p>
<p>任务说明：<br />
<textarea name="task_info" rows="3" cols="3" style="height:60px;"></textarea></p>
<p>任务优先级：
<select name="task_level"><option value="0">普通</option><option value="1">1</option><option value="2">2</option><option value="3">3</option><option value="4">4</option></select>
</p>
<p>下一流程处理人：
<select name="task_operator"><option value="0">普通</option><option value="1">1</option><option value="2">2</option><option value="3">3</option><option value="4">4</option></select>
</p>
<p>
<fieldset> 
    <legend class="legend">产品信息</legend> 
    padding-top:100px; 
</fieldset>
</p>
<input type="hidden" id="news_files" name="news_files" value="" />
</form>
<form name="fileupload" id="fileupload" action="<%=prefix %>/upload" method="post" enctype="multipart/form-data">
<fieldset> 
    <legend class="legend">测试实体包或URL</legend> 
	<div id="swfBT">
		<div id="spanSWFUploadButton">请稍候...</div> 
		<span id="uploadInfo"> &nbsp;文件最大不超过3M,格式限定为jpg,png,gif</span>
	</div>
	<div id="upFiles"></div>
</fieldset>
</form>




<p><a href="javascript:aSubmit();" id="submitBT" class="aButton tx_center" style="width:60px;">保存</a><a href="<%=prefix%>/news" class="aButton tx_center" style="width:60px;">返回</a></p>

</div>
</div>
		<div class="clear"></div>
		</div>
<% out.println(JSPOut.out("foot0")); %>