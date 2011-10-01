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
out.print(JSPOut.out("head0","0","创建新测试任务"));%>
<link rel="stylesheet" href="<%=sPrefix %>/fancybox/jquery.fancybox-1.3.4.css" type="text/css" media="screen" />
<link rel="stylesheet" href="<%=sPrefix %>/css/jquery.autocomplete.css" type="text/css" media="screen" />
<script src="<%=sPrefix %>/fancybox/jquery.fancybox-1.3.4.pack.js" type="text/javascript"></script>
<script src="<%=sPrefix %>/js/swfupload.min.js" type="text/javascript"></script>
<script src="<%=sPrefix %>/js/swfupload_tc.js" type="text/javascript"></script>
<script src="<%=sPrefix %>/js/jquery.validate.min.js" type="text/javascript"></script>
<script src="<%=sPrefix %>/js/jquery.json-2.3.min.js" type="text/javascript"></script>
<script src="<%=sPrefix %>/js/jquery.autocomplete.js" type="text/javascript"></script>
<script src="<%=sPrefix %>/js/tc.add_task.js" type="text/javascript"></script>
<script type="text/javascript">
$.sPrefix = "<%=sPrefix %>";$.prefix="<%=prefix %>";
$.isMy = <%=(ismy)?"true":"false" %>;
$.userType = <%=user.getType() %>;
$.company = "<%=user.getProp("company") %>";
$(function(){
	var tar = ($.isMy) ? "#side_mytask a" : "#side_task a";
	$(tar).addClass("sideON");
	//是否显示选择公司
	if($.userType>=4){$("#chooseCompany")[0].need = true;gs(true);}
	$(".prev1").hide();
	//任务类型
	$("input[name='task_type']").click(function(){
		var t = $(this).val();$.t = t;
		if(t<=1){if(t==0){$("#t_pid").show();}else{$("#task_p_id").val("0");$("#t_pid").hide();};$("#productFS2").appendTo($("#hide"));$("#productFS1").appendTo($("#task_new"));}
		else{$("#productFS1").appendTo($("#hide"));$("#productFS2").appendTo($("#task_new"));}
	});
	//产品
	var acc = $("#task_p_acc0").parent();acc.hide();
	$("input[name='task_p_net']").click(function(){if($(this).val()!=0){acc.show();}else{acc.hide();}});
	
	
//处理请求
$.validator.dealAjax = {
	bt:$("#submitBT"),
	loading:function(){abox("创建任务","请稍候...");},
	ok:function(data){
		if(!isNaN(data)){
			var bt1 = "<a href=\"javascript:window.location='<%=prefix%>/tasks/"+data+"';\" class=\"aButton\">查看任务</a>";
			abox("创建任务","<div class='reOk'>创建任务成功！ &nbsp;"+bt1+" <a href=\"javascript:window.location =('<%=prefix %>/tasks');\" class=\"aButton\">返回列表</a></div>");
		}else{abox("创建任务","<div class='reErr'>创建任务失败! "+data+" &nbsp;<a href='javascript:$.fancybox.close();' class=\"aButton\">关闭</a></div>");};
	},
	err:function(xhr){
		abox("创建任务","<div class='reErr'>创建任务失败! 错误码:"+xhr.responseText+" &nbsp;<a href='javascript:$.fancybox.close();' class=\"aButton\">关闭</a></div>");
	}
};
//当form的action为""时,调用 $.validator.over()而不提交form
$.validator.over = function(form){
	switch (form.id) {
	case "productForm":
		$("#productForm input,#productForm select,#productForm textarea").each(function(i){
			var v = (this.nodeName=="SELECT")?$(this).find("option:selected").text():($(this).attr("type")=="radio")?$("input[name='"+this.name+"']:checked").next().text():$(this).val();
			$("#"+this.name+"_v").html(v);$("#"+this.name+"_h").val($(this).val());
		});
		pJSON.name = $("#task_name").val();
		pJSON.productID = $("#task_p_id").val();
		pJSON.sys = $("#task_p_sys").val();
		pJSON.type = $("#task_p_type").val();
		pJSON.netType = $("input[name='task_p_net']:checked").val();
		pJSON.netPort = (pJSON.netType>0)?$("input[name='task_p_acc']:checked").val():0;
		pJSON.feeInfo = $("#task_p_fee").val();
		pJSON.company = ($("#task_company").val() == "")?$.company:$("#task_company").val();
		next(1);
		$("#productFS1").appendTo($("#hide"));$("#productFS3").appendTo($("#task_new"));$("#chooseType").hide();
		break;
	}
};

$('#productForm').validate({
    rules: {
		task_name: {required:true,rangelength:[1,100]},
		task_p_id: {required:true,number:true},
		task_p_sys: {required:true},
		task_p_type: {required:true},
		task_p_net: {required:true},
		task_p_fee: {required:true,rangelength:[1,1500]}
    }
});
$('#add_form').validate({
    rules: {
		task_info: {required:true}
    }
});

$("#task_p_sys").change(function(){
	if($(this).val()==2){
		$("#task_p_net2").attr("checked","checked");$("#task_p_net0,#task_p_net1").attr("disabled","disabled");$("#task_p_net2").removeAttr("disabled");
	}else{$("#task_p_net2").removeAttr("checked");$("#task_p_net0,#task_p_net1").removeAttr("disabled");$("#task_p_net2").attr("disabled","disabled");}
});

var sucFn = function(file, serverData){
	var re = serverData;
	swfu.startProg = false;
	var i  =($.hasFileIndex) ? ($.hasFileIndex+file.index) :file.index;
	if(re.length>=18){
	swfok("<div class='file_upload' id='fu_"+i+"'><span class='filename'>"+file.name+"</span><span class='newname hide'>"+re+"</span><span class='size hide'>"+file.size+"</span> <span class='u_ok'><span class='greenBold'>上传成功!</span> [ <a href='javascript:delFile(\""+i+"\");'>删除 </a> ][ <a href='javascript:choosePhType(\""+i+"\");'>适配机型组</a> ]<span class=\"files_name\">"+file.name+"</span></span></div>");
	$.hasFileIndex = i;
	}else{swfok("<div class='file_upload file_upload_ERR'>"+file.name+" 上传失败!</div>");}
};
initUpload("<%=user.getName() %>",sucFn,"*.apk;*.jar;*.jad;*.zip");
swfu.newfile = function(file){
	return '<%=user.getId()+"_"+System.currentTimeMillis() %>'+"_"+file.index+file.type;
};

$("#task_company").autocomplete("<%=prefix %>/company/find",
	{cacheLength:20,matchSubset:1,matchContains:1,minChars:2,
	formatItem:function(row){return row[1];},
	onItemSelect:function(li){$("#task_company").val($(li).text());},
	chk:function(v){return (escape(v).indexOf("%u") < 0);}
});
$("#task_p_search").autocomplete("<%=prefix %>/product/find",
	{cacheLength:20,matchSubset:1,matchContains:1,minChars:2,
	formatItem:function(row){return row[1];},
	onItemSelect:function(li){$("#task_p_search").val($(li).text());},
	chk:function(v){if($("#task_company").val().length<2){alert("请先确定[公司],再选择[产品]!");$("#task_p_search").val("");$("#task_company").focus();return false;}else{return (escape(v).indexOf("%u") < 0);};},
	extraParams:addCompany
});
<% //如果指定了pid
String pidstr = request.getParameter("pid");
if(StringUtil.isDigits(pidstr)){
StringBuilder skipP = new StringBuilder();
skipP.append("pSelect(").append(pidstr).append(");$(\"#p_e\").remove();$('#type2').attr('checked','checked')");
out.print(skipP);
}
%>
});
//-------------------------------------

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
<div class="aboxContent" id="task_new">
<p id="chooseCompany" class="hide">
<label for="task_company">选择公司：</label><span class="red">*</span><span class="gray">(输入公司名称拼音首字母检索,如:输入jsdx,检索“江苏电信”)</span><br />
<input type="text" id="task_company" name="task_company" style="width:90%;padding:5px;margin:0;" /><br />
</p>
<p id="chooseType">任务类型：<span class="red">*</span><br />
<input type="radio" name="task_type" id="type0" value="0" /><label for="type0">新产品测试</label>
<input type="radio" name="task_type" id="type1" value="1" /><label for="type1">DEMO测试</label>
<input type="radio" name="task_type" id="type2" value="2" /><label for="type2">修正后复测</label>
<input type="radio" name="task_type" id="type3" value="3" /><label for="type3">新机型适配</label>
<input type="radio" name="task_type" id="type4" value="4" /><label for="type4">指定复测</label>
<input type="radio" name="task_type" id="type5" value="5" /><label for="type5">拨测</label>
<input type="radio" name="task_type" id="type6" value="6" /><label for="type6">其他</label>
<br /><span id="task_type_err"></span>
</p>

</div>
</div>

<div id="hide" class="hide">
<!-- 
JSON方式获取该系统的所有机型和分组数据,考虑在服务端使用缓存;
js解析json后生成机型组对象和机型对象,分别进行填充,机型按分组进行隐藏和显示;
机型对象有选中和非选中两个状态,点击后的动作不同;
确定选择时将phone1的对象进行收集,生成json填充到隐藏的textarea中去,同时将选中的对象复制到对应文件后面;
需要有个reset方法来重置初始对象而不需要重新获取服务器数据;
搜索需要实现;

任务，产品为分别两个表单，分别进行验证检测;

真正最后提交的字段包括2个JSON数据字段：产品和机型(testUnit创建用);
 -->
<div class="inBox" id="productFS1">
    <div class="inBoxTitle">产品信息</div> 
    <form action="" id="productForm">
    <div class="inBoxContent">
    	<div class="inBoxLine"><label for="task_name">产品名称:</label><span class="red">*</span><span class="gray">(注意要与业务管理平台完全一致)</span><br /><input type="text" name="task_name" id="task_name" /></div> 
    	<div class="inBoxLine" id="t_pid"><label for="task_p_id">业务平台产品ID:</label><span class="red">*</span><span class="gray">(注意要与业务管理平台完全一致)</span><br /><input type="text" name="task_p_id" id="task_p_id" /> </div> 
    	<div class="inBoxLine"><label for="task_p_sys">操作系统:</label><span class="red">*</span><select name="task_p_sys" id="task_p_sys"><option value="0">KJava</option><option value="1">Android</option><option value="2">WAP</option><option value="3">Brew</option><option value="4">Windows mobile</option><option value="5">Windows CE</option><option value="6">其他</option></select></div> 
    	<div class="inBoxLine"><label for="task_p_type">产品类型:</label><span class="red">*</span><select name="task_p_type" id="task_p_type"><option value="0">免费</option><option value="1">短代</option><option value="2">点数</option><option value="3">按次下载</option><option value="4">进游戏包</option></select></div> 
    	<div class="inBoxLine"><label for="task_p_net">联网情况:</label><span class="red">*</span>
    	<input type="radio" name="task_p_net" id="task_p_net0" value="0" /><label for="task_p_net0">单机</label>
    	<input type="radio" name="task_p_net" id="task_p_net1" value="1" /><label for="task_p_net1">网游</label>
    	<input type="radio" name="task_p_net" id="task_p_net2" value="2" disabled="disabled" /><label for="task_p_net2">WAP</label></div> 
    	<div class="inBoxLine"><label for="task_p_acc">接口调测情况:</label>
    	<input type="radio" name="task_p_acc" id="task_p_acc0" value="0" checked="checked" /><label for="task_p_acc0">未调测</label>
    	<input type="radio" name="task_p_acc" id="task_p_acc1" value="1" /><label for="task_p_acc1">已调通</label>
    	<input type="radio" name="task_p_acc" id="task_p_acc2" value="2" /><label for="task_p_acc2">调测中</label></div> 
    	<div class="inBoxLine"><label for="task_p_fee">计费点描述:</label><span class="gray">(注意要与业务管理平台完全一致)</span><br /><textarea name="task_p_fee" id="task_p_fee" rows="3" cols="3" style="height:100px;"></textarea> </div> 
    	<a href="javascript:saveP();" class="aButton tx_center" style="width:60px;">确定</a> <a href="javascript:pre(1);" class="aButton tx_center prev1">上一步</a>
    </div>
    </form>
</div>

<div class="inBox" id="productFS3">
    <div class="inBoxTitle">产品信息</div> 
    <div class="inBoxContent">
    	<div class="inBoxLine">产品名称: <span id="task_name_v" class="blueBold"></span> 产品ID: <span id="task_p_id_v" class="blueBold"></span> 操作系统: <span id="task_p_sys_v" class="blueBold"></span></div> 
    	<div class="inBoxLine">产品计费类型: <span id="task_p_type_v" class="blueBold"></span> 联网情况: <span id="task_p_net_v" class="blueBold"></span> 接口调测情况: <span id="task_p_acc_v" class="blueBold"></span></div> 
    	<div class="inBoxLine">计费点描述: <br /><span id="task_p_fee_v" class="blueBold"></span></div>
    	<a href="javascript:next(2);" class="aButton tx_center next1">下一步：上传文件或设置WAP产品url</a>
    	<a href="javascript:editP();" class="aButton tx_center" style="width:60px;" id="p_e">更改</a> 
    </div>
</div>

<div class="inBox" id="productFS2">
    <div class="inBoxTitle">产品选择</div> 
    <div class="inBoxContent">
    	<label for="task_p_search">产品名称检索：</label><span class="red">*</span><span class="gray">(输入产品名称拼音首字母检索)</span><br />
    	<input type="text" name="task_p_search" id="task_p_search" style="width:300px;" /><a href="javascript:pSelect();" class="aButton">确定</a>   <a href="javascript:pre(1);" class="aButton tx_center prev1">上一步</a>
    </div>
</div>
 
<div class="inBox" id="uploadFS">
    <div class="inBoxTitle">游戏实体包上传或URL设置</div> 
    <div class="inBoxContent">
	<form name="fileupload" id="fileupload" action="<%=prefix %>/upload" method="post" enctype="multipart/form-data">
		<div id="swfBT" class="inBoxLine">
			<div id="spanSWFUploadButton">请稍候...</div> 
			<span id="uploadInfo"> &nbsp;文件最大不超过100M,格式限定为apk,jar,jad,zip,按住Ctrl键可多选</span>
		</div>
		<div id="upFiles"></div>
		<br /><a href="javascript:filesSet();" class="aButton">确定</a> <a href="javascript:pre(2);" class="aButton tx_center pre2">上一步</a> 
	</form>
	<div id="urlSet">
		<label for="task_p_url">URL设置：</label><span class="red">*</span><span class="gray">(请输入WAP游戏的入口URL)</span><br />
		http://<span class="blueBold"></span><span id="urlInput"><input type="text" name="task_p_url" id="task_p_url" style="width:300px;" />
		<a href="javascript:urlSet();" class="aButton">确定</a> <a href="javascript:pre(2);" class="aButton tx_center pre2">上一步</a></span>
	</div>
    </div>
</div>

<div id="taskFS">
<form action="<%=prefix%>/tasks/a_a" method="post" id="add_form">
<p><label for="task_info">任务说明：</label><br />
<textarea id="task_info" name="task_info" rows="3" cols="3" style="height:60px;"></textarea></p>
<p>任务优先级：
<select name="task_level"><option value="0">普通</option><option value="1">1</option><option value="2">2</option><option value="3">3</option><option value="4">4</option></select>
</p>
<p>下一流程处理人：
<select name="task_operator"><option value="曹雨">曹雨</option></select>
</p>
<input type="hidden" id="task_type_h" name="task_type_h" value="" />
<textarea rows="1" cols="1" class="hide" name="task_p_json_h" id="task_p_json_h"></textarea>
</form>


<p><a href="javascript:aSubmit();" id="submitBT" class="aButton tx_center" style="width:60px;">创建任务</a> <a href="javascript:pre(3);" class="aButton tx_center" style="width:60px;">上一步</a></p>

</div>
<!-- end of hide -->
</div>
		<div class="clear"></div>
		</div>
<% out.println(JSPOut.out("foot0")); %>