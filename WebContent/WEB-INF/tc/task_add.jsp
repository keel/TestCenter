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
<link rel="stylesheet" href="<%=sPrefix %>/css/jquery.autocomplete.css" type="text/css" media="screen" />
<script src="<%=sPrefix %>/fancybox/jquery.fancybox-1.3.4.pack.js" type="text/javascript"></script>
<script src="<%=sPrefix %>/js/swfupload.min.js" type="text/javascript"></script>
<script src="<%=sPrefix %>/js/swfupload_tc.js" type="text/javascript"></script>
<script src="<%=sPrefix %>/js/jquery.validate.min.js" type="text/javascript"></script>
<script src="<%=sPrefix %>/js/jquery.autocomplete.js" type="text/javascript"></script>
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
$.validator.setDefaults({
	//处理radio选择的错误提示
	errorPlacement : function(label, element){
	if(element.attr("name")=="task_type"){$("#task_type_err").append(label);}else{element.after(label);}
	}
});
//开始验证
$('#add_form').validate({
    /* 设置验证规则 */
    rules: {
		task_info: {
            rangelength:[1,500]
        },
        task_type:{
            required:true
        },
        task_company:{
        	required:true,rangelength:[1,50]
        }
    }
});
var sucFn = function(file, serverData){
	var re = serverData;
	swfu.startProg = false;
	var i  =($.hasFileIndex) ? ($.hasFileIndex+file.index) :file.index;
	if(re.length>=file.name.length){
	swfok("<div class='file_upload' id='fu_"+i+"'>"+file.name+" <span class='greenBold'>上传成功!</span> [ <a href='javascript:delFile(\""+i+"\");'>删除 </a> ][ <a href='javascript:delFile(\""+i+"\");'>选择机型 </a> ]<span class=\"files_name\">"+file.name+"</span></div>");
	}else{swfok("<div class='file_upload file_upload_ERR'>"+file.name+" 上传失败!</div>");}
};
initUpload("<%=user.getName() %>",sucFn,"*.apk;*.jar;*.jad;*.zip");
$("#task_company").autocomplete("<%=prefix %>/company/find",
	{cacheLength:10,matchSubset:1,matchContains:1,minChars:2,
	formatItem:function(row){return row[1];},
	onItemSelect:function(li){$("#task_company").val($(li).text());},
	chk:function(v){return (escape(v).indexOf("%u") < 0);}
	});

$("#task_p_search").autocomplete("<%=prefix %>/product/find",
		{cacheLength:10,matchSubset:1,matchContains:1,minChars:2,
		formatItem:function(row){return row[1];},
		onItemSelect:function(li){$("#task_p_search").val($(li).text());},
		chk:function(v){return (escape(v).indexOf("%u") < 0);},
		extraParams:addCompany
		});

});
function addCompany(){
	return {c:$("#task_company").val()};
}
function aSubmit(){
	var ff = [];
	$(".files_name").each(function(){
		ff.push(encodeURIComponent($(this).text()));
	});
	//console.log(ff);
	$("#news_files").val(ff.join(","));
	$("#news_form").submit();
};
function pSearch(){
	
}
function urlSet(){
	
}
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
<p id="chooseCompany">
<label for="task_company">选择公司：</label><span class="red">*</span><span class="gray">(输入公司名称拼音首字母检索,如:输入jsdx,检索“江苏电信”)</span><br />
<input type="text" id="task_company" name="task_company" style="width:90%;padding:5px;margin:0;" />
</p>
<p id="chooseType">任务类型：<span class="red">*</span><br />
<input type="radio" name="task_type" id="type0" value="0" /><label for="type0">新产品</label>
<input type="radio" name="task_type" id="type1" value="1" /><label for="type1">修改后复测</label>
<input type="radio" name="task_type" id="type2" value="2" /><label for="type2">新机型适配</label>
<input type="radio" name="task_type" id="type3" value="3" /><label for="type3">DEMO测试</label>
<input type="radio" name="task_type" id="type4" value="4" /><label for="type4">指定复测</label>
<input type="radio" name="task_type" id="type5" value="5" /><label for="type5">拨测</label>
<input type="radio" name="task_type" id="type6" value="6" /><label for="type6">其他</label>
<br /><span id="task_type_err"></span>
</p>
<div class="inBox">
    <div class="inBoxTitle">产品信息</div> 
    <div class="inBoxContent">
    	<div class="inBoxLine"><label for="task_name">产品名称:</label><span class="red">*</span><span class="gray">(注意要与业务管理平台完全一致)</span><br /><input type="text" name="task_name" id="task_name" /></div> 
    	<div class="inBoxLine"><label for="task_p_id">产品ID:</label><span class="red">*</span><span class="gray">(注意要与业务管理平台完全一致)</span><br /><input type="text" name="task_p_id" id="task_p_id" /> </div> 
    	<div class="inBoxLine"><label for="task_p_sys">操作系统:</label><span class="red">*</span><select name="task_p_sys" id="task_p_sys"><option value="0">KJava</option><option value="1">Android</option><option value="2">WAP</option><option value="3">Brew</option><option value="4">Windows mobile</option><option value="5">Windows CE</option><option value="6">其他</option></select></div> 
    	<div class="inBoxLine"><label for="task_p_type">产品计费类型:</label><span class="red">*</span><select name="task_p_type" id="task_p_type"><option value="0">免费</option><option value="1">短代</option><option value="2">点数</option><option value="3">按次下载</option><option value="4">进游戏包</option></select></div> 
    	<div class="inBoxLine"><label for="task_p_acc">接口调测情况:</label><span class="red">*</span><input type="radio" name="task_p_acc" id="task_p_acc0" value="0" /><label for="task_p_acc0">未调测</label>
    	<input type="radio" name="task_p_acc" id="task_p_acc1" value="1" /><label for="task_p_acc1">已调通</label>
    	<input type="radio" name="task_p_acc" id="task_p_acc2" value="2" /><label for="task_p_acc2">调测中</label></div> 
    	<div class="inBoxLine"><label for="task_p_fee">计费点描述:</label><span class="gray">(注意要与业务管理平台完全一致)</span><br /><textarea name="task_p_fee" id="task_p_fee" rows="3" cols="3" style="height:100px;"></textarea> </div> 
    	<a href="javascript:saveP();" class="aButton tx_center" style="width:60px;">确定</a>
    </div>
</div>


<br />



<div class="inBox">
    <div class="inBoxTitle">产品选择</div> 
    <div class="inBoxContent">
    	<label for="task_p_search">产品名称检索：</label><span class="red">*</span><span class="gray">(输入产品名称拼音首字母检索)</span><br />
    	<input type="text" name="task_p_search" id="task_p_search" style="width:300px;" /><a href="javascript:pSearch();" class="aButton">确定</a>
    </div>
</div>



<br />

<div class="inBox">
    <div class="inBoxTitle">游戏实体包上传或URL设置</div> 
    <div class="inBoxContent">
	<form name="fileupload" id="fileupload" action="<%=prefix %>/upload" method="post" enctype="multipart/form-data">
		<div id="swfBT" class="inBoxLine">
			<div id="spanSWFUploadButton">请稍候...</div> 
			<span id="uploadInfo"> &nbsp;文件最大不超过100M,格式限定为apk,jar,jad,zip</span>
		</div>
		<div id="upFiles"></div>
	</form>
	<div id="urlSet">
		<label for="task_p_url">URL设置：</label><span class="red">*</span><span class="gray">(请输入WAP游戏的入口URL)</span><br />
		<input type="text" name="task_p_url" id="task_p_url" style="width:300px;" />
		<a href="javascript:urlSet();" class="aButton">确定</a>
	</div>
    </div>
</div>



<br />



<p><label for="task_info">任务说明：</label><br />
<textarea id="task_info" name="task_info" rows="3" cols="3" style="height:60px;"></textarea></p>
<p>任务优先级：
<select name="task_level"><option value="0">普通</option><option value="1">1</option><option value="2">2</option><option value="3">3</option><option value="4">4</option></select>
</p>
<p>下一流程处理人：
<select name="task_operator"><option value="0">普通</option><option value="1">1</option><option value="2">2</option><option value="3">3</option><option value="4">4</option></select>
</p>

<input type="hidden" id="news_files" name="news_files" value="" />
</form>


<p><a href="javascript:aSubmit();" id="submitBT" class="aButton tx_center" style="width:60px;">创建任务</a><a href="<%=prefix%>/news" class="aButton tx_center" style="width:60px;">返回</a></p>


<div id="hide">

<div id="choosePhone" class="inBox">
<div style="padding:10px;">
<div id="selectedPhones">
	<div class="inBoxTitle">已选中机型：</div>
	<div class="inBoxContent" style="padding:10px;border-bottom: 1px dotted #aaa;background-color:#FFF;">
		<a class="phone phone1" href="javascript:void(0);">moto xt800</a>
	</div>
</div>
<br />
<div id="phones">
	<div id="phoneCates" class="inBoxTitle">机型组：
		<a class="aButton" href="javascript:void(0);">代表机型</a><a class="aButton" href="javascript:void(0);">240x320</a><a class="aButton" href="javascript:void(0);">320x480</a>
	</div>
	<div class="inBoxContent" style="padding:10px;border-bottom: 1px dotted #aaa;background-color:#FFF;">
		<a class="phone" href="javascript:void(0);">moto xt800</a> 
		<a class="phone" href="javascript:void(0);">moto xt800</a> 
		<a class="phone" href="javascript:void(0);">moto xt800</a>
	</div>
</div>
</div>
</div>

</div>





</div>
</div>
		<div class="clear"></div>
		</div>
<% out.println(JSPOut.out("foot0")); %>